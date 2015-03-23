/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Ordinastie
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.malisis.core.util.chunkcollision;

import gnu.trove.procedure.TLongProcedure;
import gnu.trove.set.hash.TLongHashSet;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;

import net.malisis.core.MalisisCore;
import net.malisis.core.block.BoundingBoxType;
import net.malisis.core.util.Point;
import net.malisis.core.util.RaytraceBlock;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;

/**
 * This class is the enty point for all the chunk collision related calculation.<br>
 * The static methods are called via ASM which then call the process for the corresponding server or client instance.
 *
 * @author Ordinastie
 *
 */
public class ChunkCollision
{
	public static ChunkCollision server = new ChunkCollision(Side.SERVER);
	public static ChunkCollision client = new ChunkCollision(Side.CLIENT);

	//1.8 BlockPos constants
	private static final int NUM_X_BITS = 26;
	private static final int NUM_Z_BITS = NUM_X_BITS;
	private static final int NUM_Y_BITS = 64 - NUM_X_BITS - NUM_Z_BITS;
	private static final int Y_SHIFT = 0 + NUM_Z_BITS;
	private static final int X_SHIFT = Y_SHIFT + NUM_Y_BITS;
	private static final long X_MASK = (1L << NUM_X_BITS) - 1L;
	private static final long Y_MASK = (1L << NUM_Y_BITS) - 1L;
	private static final long Z_MASK = (1L << NUM_Z_BITS) - 1L;

	// /!\ Logical side!
	private Side side;
	private Map<Chunk, TLongHashSet> chunks = new WeakHashMap();
	private CollisionProcedure collisionProcedure = new CollisionProcedure();
	private RayTraceProcedure rayTraceProcedure = new RayTraceProcedure();
	private PlaceBlockProcedure placeBlockProcedure = new PlaceBlockProcedure();

	/**
	 * Instantiates a new {@link ChunkCollision}.
	 *
	 * @param side the side
	 */
	private ChunkCollision(Side side)
	{
		this.side = side;
	}

	/**
	 * Call a {@link ChunkProcedure} for the chunks within the specified range.
	 *
	 * @param world the world
	 * @param minX the min x
	 * @param minZ the min z
	 * @param maxX the max x
	 * @param maxZ the max z
	 * @param procedure the procedure
	 */
	private void callProcedureForChunks(World world, int minX, int minZ, int maxX, int maxZ, ChunkProcedure procedure)
	{
		for (int cx = minX; cx <= maxX; ++cx)
		{
			for (int cz = minZ; cz <= maxZ; ++cz)
			{
				Chunk chunk = world.getChunkFromChunkCoords(cx, cz);
				procedure.set(world, chunk);
				TLongHashSet coords = chunks.get(chunk);
				if (coords != null)
					coords.forEach(procedure);
			}
		}
	}

	//#region updateCollisionCoordinates
	/**
	 * Update chunk collision coordinates.<br>
	 * Called via ASM from {@link Chunk#setBlockIDWithMetadata(int, int, int, Block, int)}
	 *
	 * @param chunk the chunk
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @param old the old
	 * @param block the block
	 */
	public static void updateCollisionCoordinates(Chunk chunk, int x, int y, int z, Block old, Block block)
	{
		ChunkCollision cc = get(chunk.worldObj);
		long coord = getLong(x, y, z);
		if (old instanceof IChunkCollidable)
			cc.removeCoord(chunk, coord);
		if (block instanceof IChunkCollidable)
			cc.addCoord(chunk, coord);
	}

	/**
	 * Adds coordinate for the {@link Chunk}.
	 *
	 * @param chunk the chunk
	 * @param coord the coord
	 */
	private void addCoord(Chunk chunk, long coord)
	{
		TLongHashSet coords = chunks.get(chunk);
		if (coords == null)
		{
			coords = new TLongHashSet();
			chunks.put(chunk, coords);
		}
		coords.add(coord);
	}

	/**
	 * Removes coordinate from the {@link Chunk}.
	 *
	 * @param chunk the chunk
	 * @param coord the coord
	 */
	private void removeCoord(Chunk chunk, long coord)
	{
		TLongHashSet coords = chunks.get(chunk);
		if (coords == null)
			return;
		if (!coords.remove(coord))
			MalisisCore.message("Failed to remove : %s", printCoord(coord));

		if (coords.size() == 0)
			chunks.remove(chunk);
	}

	//#end updateCollisionCoordinates

	//#region getCollisionBoundinBoxes
	/**
	 * Gets the collision bounding boxes for the intersecting chunks.<br>
	 * Called via ASM from {@link World#getCollidingBoundingBoxes(Entity, AxisAlignedBB)}
	 *
	 * @param world the world
	 * @param mask the mask
	 * @param list the list
	 * @param entity the entity
	 */
	public static void getCollisionBoundingBoxes(World world, AxisAlignedBB mask, List<AxisAlignedBB> list, Entity entity)
	{
		get(world).getCollisionBoxes(world, mask, list, entity);
	}

	/**
	 * Gets the collision boxes for the intersecting chunks.
	 *
	 * @param world the world
	 * @param mask the mask
	 * @param entity the entity
	 * @return the collision boxes
	 */
	private void getCollisionBoxes(World world, AxisAlignedBB mask, List<AxisAlignedBB> list, Entity entity)
	{
		int minX = chunkX(MathHelper.floor_double(mask.minX)) - 1;
		int maxX = chunkX(MathHelper.floor_double(mask.maxX)) + 1;
		int minZ = chunkZ(MathHelper.floor_double(mask.minZ)) - 1;
		int maxZ = chunkZ(MathHelper.floor_double(mask.maxZ)) + 1;

		collisionProcedure.mask = mask;
		collisionProcedure.list = list;

		callProcedureForChunks(world, minX, minZ, maxX, maxZ, collisionProcedure);

		collisionProcedure.clean();
	}

	//#end getCollisionBoundinBoxes

	//#region getRayTraceResult
	/**
	 * Sets the ray trace infos.<br>
	 * Called via ASM at the beginning of {@link World#rayTraceBlocks(Vec3, Vec3, boolean, boolean, boolean)}
	 *
	 * @param world the world
	 * @param src the src
	 * @param dest the dest
	 */
	public static void setRayTraceInfos(World world, Vec3 src, Vec3 dest)
	{
		if (src == null || dest == null)
			return;
		get(world).setRayTraceInfos(new Point(src), new Point(dest));
	}

	public static void setRayTraceInfos(World world, Point src, Point dest)
	{
		get(world).setRayTraceInfos(src, dest);
	}

	/**
	 * Sets the ray trace infos.
	 *
	 * @param src the src
	 * @param dest the dest
	 */
	private void setRayTraceInfos(Point src, Point dest)
	{
		if (src == null || dest == null)
			return;

		rayTraceProcedure.src = src;
		rayTraceProcedure.dest = dest;
	}

	/**
	 * Gets the ray trace result.<br>
	 * Called via ASM from {@link World#rayTraceBlocks(Vec3, Vec3, boolean, boolean, boolean)} before each return.
	 *
	 * @param world the world
	 * @param mop the mop
	 * @return the ray trace result
	 */
	public static MovingObjectPosition getRayTraceResult(World world, MovingObjectPosition mop)
	{
		return get(world).getRayTrace(world, mop);
	}

	/**
	 * Gets the ray trace result.
	 *
	 * @param world the world
	 * @param mop the mop
	 * @return the ray trace
	 */
	private MovingObjectPosition getRayTrace(World world, MovingObjectPosition mop)
	{
		Point src = rayTraceProcedure.src;
		Point dest = rayTraceProcedure.dest;
		if (src == null || dest == null)
			return null;

		int minX = chunkX(MathHelper.floor_double(Math.min(src.x, dest.x)));
		int maxX = chunkX(MathHelper.floor_double(Math.max(src.x, dest.x)) + 1);
		int minZ = chunkZ(MathHelper.floor_double(Math.min(src.z, dest.z)));
		int maxZ = chunkZ(MathHelper.floor_double(Math.max(src.z, dest.z)) + 1);

		rayTraceProcedure.mop = mop;

		callProcedureForChunks(world, minX, minZ, maxX, maxZ, rayTraceProcedure);

		mop = rayTraceProcedure.mop;

		rayTraceProcedure.clean();

		return mop;
	}

	/**
	 * Gets the closest {@link MovingObjectPosition} to the source.
	 *
	 * @param src the src
	 * @param mop1 the mop1
	 * @param mop2 the mop2
	 * @return the closest
	 */
	private MovingObjectPosition getClosest(Point src, MovingObjectPosition mop1, MovingObjectPosition mop2)
	{
		if (mop1 == null)
			return mop2;
		if (mop2 == null)
			return mop1;

		if (mop1.typeOfHit == MovingObjectType.MISS && mop2.typeOfHit != MovingObjectType.MISS)
			return mop2;
		if (mop1.typeOfHit != MovingObjectType.MISS && mop2.typeOfHit == MovingObjectType.MISS)
			return mop1;

		if (Point.distanceSquared(src, new Point(mop1.hitVec)) > Point.distanceSquared(src, new Point(mop2.hitVec)))
			return mop2;
		return mop1;
	}

	//#end getRayTraceResult

	//#region canPlaceBlockAt
	/**
	 * Checks whether the block can be placed at the position.<br>
	 * Called via ASM from {@link World#canPlaceEntityOnSide(Block, int, int, int, boolean, int, Entity, net.minecraft.item.ItemStack)} at
	 * the begining.
	 *
	 * @param world the world
	 * @param block the block
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @return true, if successful
	 */
	public static boolean canPlaceBlockAt(World world, Block block, int x, int y, int z)
	{
		return get(world).canPlaceBlock(world, block, x, y, z);
	}

	/**
	 * Checks whether the block can be placed at the position.<br>
	 * Tests the block bounding box (boxes if {@link IChunkCollidable}) against the occupied blocks position, then against all the bounding
	 * boxes of the {@link IChunkCollidable} available for those chunks.
	 *
	 * @param world the world
	 * @param block the block
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @return true, if successful
	 */
	public boolean canPlaceBlock(World world, Block block, int x, int y, int z)
	{
		AxisAlignedBB[] aabbs = new AxisAlignedBB[0];
		if (block instanceof IChunkCollidable)
			aabbs = ((IChunkCollidable) block).getBoundingBox(world, x, y, z, BoundingBoxType.CHUNKCOLLISION);
		else
		{
			AxisAlignedBB aabb = block.getCollisionBoundingBoxFromPool(world, x, y, z);
			if (aabb != null)
				aabbs = new AxisAlignedBB[] { aabb.offset(-x, -y, -z) };
		}

		//build set of coordinates intersecting the AABBs
		Set<ChunkPosition> positions = new HashSet<>();
		for (AxisAlignedBB aabb : aabbs)
			if (aabb != null)
				getOverlappingBlocks(positions, aabb.offset(x, y, z));

		int minX = chunkX(x);
		int maxX = chunkX(x);
		int minZ = chunkZ(z);
		int maxZ = chunkZ(z);
		//check against each block position occupied by the AABBs
		for (ChunkPosition coord : positions)
		{
			if (!world.getBlock(coord.chunkPosX, coord.chunkPosY, coord.chunkPosZ).isReplaceable(world, coord.chunkPosX, coord.chunkPosY,
					coord.chunkPosZ))
				return false;

			minX = Math.min(minX, chunkX(coord.chunkPosX));
			maxX = Math.max(maxX, chunkX(coord.chunkPosX));
			minZ = Math.min(minZ, chunkZ(coord.chunkPosZ));
			maxZ = Math.max(maxZ, chunkZ(coord.chunkPosZ));
		}

		//check against each IChunkCollidable for occupied chunks
		placeBlockProcedure.aabbs = aabbs;
		callProcedureForChunks(world, minX - 1, minZ - 1, maxX + 1, maxZ + 1, placeBlockProcedure);

		boolean collide = placeBlockProcedure.collide;
		placeBlockProcedure.clean();

		return !collide;
	}

	/**
	 * Gets the block positions overlapping a specific {@link AxisAlignedBB}.
	 *
	 * @param blocks the blocks
	 * @param aabb the aabb
	 * @return the overlapping blocks
	 */
	private void getOverlappingBlocks(Set<ChunkPosition> blocks, AxisAlignedBB aabb)
	{
		int minX = (int) Math.floor(aabb.minX);
		int maxX = (int) Math.ceil(aabb.maxX);
		int minY = (int) Math.floor(aabb.minY);
		int maxY = (int) Math.ceil(aabb.maxY);
		int minZ = (int) Math.floor(aabb.minZ);
		int maxZ = (int) Math.ceil(aabb.maxZ);

		for (int x = minX; x < maxX; x++)
			for (int y = minY; y < maxY; y++)
				for (int z = minZ; z < maxZ; z++)
					blocks.add(new ChunkPosition(x, y, z));
	}

	//#end canPlaceBlockAt

	//#region Events
	/**
	 * On data load.
	 *
	 * @param event the event
	 */
	@SubscribeEvent
	public void onDataLoad(ChunkDataEvent.Load event)
	{
		NBTTagCompound nbt = event.getData();
		if (!nbt.hasKey("chunkCollision"))
			return;

		long[] coords = readLongArray(nbt);
		chunks.put(event.getChunk(), new TLongHashSet(coords));
	}

	@SubscribeEvent
	public void onDataSave(ChunkDataEvent.Save event)
	{
		TLongHashSet coords = chunks.get(event.getChunk());
		if (coords == null || coords.size() == 0)
			return;

		NBTTagCompound nbt = event.getData();
		writeLongArray(nbt, coords.toArray());
	}

	/**
	 * Read long array from {@link NBTTagCompound}.<br>
	 * From IvNBTHelper.readNBTLongs()
	 *
	 * @param compound the compound
	 * @return the long[]
	 * @author Ivorius
	 */
	private static long[] readLongArray(NBTTagCompound compound)
	{
		ByteBuf bytes = Unpooled.copiedBuffer(compound.getByteArray("chunkCollision"));
		long[] longs = new long[bytes.capacity() / 8];
		for (int i = 0; i < longs.length; i++)
			longs[i] = bytes.readLong();
		return longs;
	}

	/**
	 * Write long array into {@link NBTTagCompound}.<br>
	 * From IvNBTHelper.writeNBTLongs()
	 *
	 * @param compound the compound
	 * @param longs the longs
	 * @author Ivorius
	 */
	private static void writeLongArray(NBTTagCompound compound, long[] longs)
	{
		ByteBuf bytes = Unpooled.buffer(longs.length * 8);
		for (long aLong : longs)
			bytes.writeLong(aLong);
		compound.setByteArray("chunkCollision", bytes.array());
	}

	/**
	 * Server only.<br>
	 * Sends the chunks coordinates to the client when they get watched by them.
	 *
	 * @param event the event
	 */
	@SubscribeEvent
	public void onChunkWatched(ChunkWatchEvent.Watch event)
	{
		Chunk chunk = event.player.worldObj.getChunkFromChunkCoords(event.chunk.chunkXPos, event.chunk.chunkZPos);
		TLongHashSet coords = chunks.get(chunk);
		if (coords == null || coords.size() == 0)
			return;

		ChunkCollisionMessage.sendCoords(chunk, coords.toArray(), event.player);
	}

	/**
	 * Client only.<br>
	 * Sets the coordinates for a chunk received by {@link ChunkCollisionMessage}.
	 *
	 * @param chunkX the chunk x
	 * @param chunkZ the chunk z
	 * @param coords the coords
	 */
	public void setCoords(int chunkX, int chunkZ, long[] coords)
	{
		Chunk chunk = Minecraft.getMinecraft().theWorld.getChunkFromChunkCoords(chunkX, chunkZ);
		chunks.put(chunk, new TLongHashSet(coords));
	}

	//#end Events

	/**
	 * Gets the right {@link ChunkCollision} instance base on {@link World#isRemote}.
	 *
	 * @param world the world
	 * @return the chunk collision
	 */
	private static ChunkCollision get(World world)
	{
		return world.isRemote ? client : server;
	}

	private static int chunkX(int x)
	{
		return x >> 4;
	}

	private static int chunkZ(int z)
	{
		return z >> 4;
	}

	private static long getLong(int x, int y, int z)
	{
		return (x & X_MASK) << X_SHIFT | (y & Y_MASK) << Y_SHIFT | (z & Z_MASK);
	}

	private static int getX(long coord)
	{
		return (int) (coord << 64 - X_SHIFT - NUM_X_BITS >> 64 - NUM_X_BITS);
	}

	private static int getY(long coord)
	{
		return (int) (coord << 64 - Y_SHIFT - NUM_Y_BITS >> 64 - NUM_Y_BITS);
	}

	private static int getZ(long coord)
	{
		return (int) (coord << 64 - NUM_Z_BITS >> 64 - NUM_Z_BITS);
	}

	private static String printCoord(long coord)
	{
		return getX(coord) + ", " + getY(coord) + ", " + getZ(coord);
	}

	private static String printChunk(Chunk chunk)
	{
		return chunk.xPosition + ", " + chunk.zPosition;
	}

	@Override
	public String toString()
	{
		String s = "Size : " + chunks.size() + " | ";
		for (Entry<Chunk, TLongHashSet> entry : chunks.entrySet())
			s += "[" + printChunk(entry.getKey()) + "=" + entry.getValue().size() + "], ";
		return s;
	}

	public abstract class ChunkProcedure implements TLongProcedure
	{
		protected World world;
		protected Chunk chunk;
		protected int x, y, z;
		protected Block block;

		protected void set(World world, Chunk chunk)
		{
			this.world = world;
			this.chunk = chunk;
		}

		/**
		 * Checks whether the passed coordinate is a valid {@link IChunkCollidable} and that it belong to the current {@link Chunk}.<br>
		 * Also sets the x, y, z and block field for this {@link ChunkProcedure}.
		 *
		 * @param coord the coord
		 * @return true, if successful
		 */
		protected boolean check(long coord)
		{
			x = getX(coord);
			y = getY(coord);
			z = getZ(coord);
			block = world.getBlock(x, y, z);

			if (chunk.xPosition != chunkX(x) || chunk.zPosition != chunkZ(z) || !(block instanceof IChunkCollidable))
			{
				MalisisCore.log.info("[ChunkCollision]  Removing invalid {} coordinate : {} in chunk {} (block {})", side,
						printCoord(coord), printChunk(chunk), block);
				removeCoord(chunk, coord);
				return false;
			}

			return true;
		}

		protected void clean()
		{
			world = null;
			chunk = null;
			block = null;
		}
	}

	/**
	 * The procedure used to check the collision for a {@link IChunkCollidable} coordinate.<br>
	 */
	private class CollisionProcedure extends ChunkProcedure
	{
		private AxisAlignedBB mask;
		private List<AxisAlignedBB> list;

		@Override
		public boolean execute(long coord)
		{
			if (!check(coord))
				return true;

			Block block = world.getBlock(x, y, z);
			if (block instanceof IChunkCollidable)
			{
				AxisAlignedBB[] aabbs = ((IChunkCollidable) block).getBoundingBox(world, x, y, z, BoundingBoxType.CHUNKCOLLISION);
				for (AxisAlignedBB aabb : aabbs)
				{
					if (aabb != null)
					{
						aabb.offset(x, y, z);
						if (mask.intersectsWith(aabb))
							list.add(aabb);
					}
				}
			}
			return true;
		}

		@Override
		protected void clean()
		{
			super.clean();
			mask = null;
			list = null;
		}
	}

	/**
	 * The procedure used to check ray tracing for a {@link IChunkCollidable} coordinate.
	 */
	private class RayTraceProcedure extends ChunkProcedure
	{
		private Point src;
		private Point dest;
		private MovingObjectPosition mop;

		@Override
		public boolean execute(long coord)
		{
			if (!check(coord))
				return true;

			RaytraceBlock rt = RaytraceBlock.set(src, dest, x, y, z);
			mop = getClosest(src, rt.trace(), mop);

			return true;
		}

		@Override
		protected void clean()
		{
			super.clean();
			src = null;
			dest = null;
			mop = null;
		}
	}

	/**
	 * The procedure used to check intersection from {@link IChunkCollidable} coordinates with {@link AxisAlignedBB} array.
	 */
	private class PlaceBlockProcedure extends ChunkProcedure
	{
		private AxisAlignedBB[] aabbs;
		private boolean collide = false;

		@Override
		public boolean execute(long coord)
		{
			if (!check(coord))
				return true;

			AxisAlignedBB[] blockBounds = ((IChunkCollidable) block).getBoundingBox(world, x, y, z, BoundingBoxType.CHUNKCOLLISION);
			for (AxisAlignedBB bb : blockBounds)
				bb.offset(x, y, z);

			for (AxisAlignedBB aabb : aabbs)
				for (AxisAlignedBB bb : blockBounds)
				{
					collide = aabb.intersectsWith(bb);
					if (collide)
						return false;
				}

			return true;
		}

		@Override
		protected void clean()
		{
			super.clean();
			aabbs = null;
			collide = false;
		}

	}
}
