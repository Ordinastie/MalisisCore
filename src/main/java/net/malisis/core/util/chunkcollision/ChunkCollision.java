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

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

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
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;

/**
 * @author Ordinastie
 *
 */
public class ChunkCollision
{
	public static ChunkCollision server = new ChunkCollision(Side.SERVER);
	public static ChunkCollision client = new ChunkCollision(Side.CLIENT);

	//1.8 BlocPos constants
	private static final int NUM_X_BITS = 26;
	private static final int NUM_Z_BITS = NUM_X_BITS;
	private static final int NUM_Y_BITS = 64 - NUM_X_BITS - NUM_Z_BITS;
	private static final int Y_SHIFT = 0 + NUM_Z_BITS;
	private static final int X_SHIFT = Y_SHIFT + NUM_Y_BITS;
	private static final long X_MASK = (1L << NUM_X_BITS) - 1L;
	private static final long Y_MASK = (1L << NUM_Y_BITS) - 1L;
	private static final long Z_MASK = (1L << NUM_Z_BITS) - 1L;

	// /!\ Logical side!
	@SuppressWarnings("unused")
	private Side side;
	private Map<Chunk, TLongHashSet> chunks = new WeakHashMap();
	private CollisionProcedure collisionProcedure = new CollisionProcedure();
	private RayTraceProcedure rayTraceProcedure = new RayTraceProcedure();

	private ChunkCollision(Side side)
	{
		this.side = side;
	}

	private void callProcedureForChunks(World world, int minX, int minZ, int maxX, int maxZ, TLongProcedure procedure)
	{
		for (int cx = minX; cx <= maxX; ++cx)
		{
			for (int cz = minZ; cz <= maxZ; ++cz)
			{
				TLongHashSet coords = chunks.get(world.getChunkFromChunkCoords(cx, cz));
				if (coords != null)
					coords.forEach(procedure);
			}
		}
	}

	//#region updateCollisionCoordinates
	/**
	 * Update chunk collision coordinates.<br>
	 * Called via ASM from {@link Chunk#func_150807_a(int, int, int, Block, int)} (setBlock())
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
		coords.remove(coord);
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
	 * @return the collision bounding boxes
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

		collisionProcedure.world = world;
		collisionProcedure.mask = mask;
		collisionProcedure.list = list;

		callProcedureForChunks(world, minX, minZ, maxX, maxZ, collisionProcedure);

		collisionProcedure.world = null;
		collisionProcedure.mask = null;
		collisionProcedure.list = null;
	}

	/**
	 * The procedure used to check the collision for a block coordinate.<br>
	 */
	private class CollisionProcedure implements TLongProcedure
	{
		private World world;
		private AxisAlignedBB mask;
		private List<AxisAlignedBB> list;

		@Override
		public boolean execute(long coord)
		{
			int x = getX(coord);
			int y = getY(coord);
			int z = getZ(coord);

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
			return false;
		}
	}

	//#end getCollisionBoundinBoxes

	//#region getRayTraceResult
	/**
	 * Sets the ray trace infos.<br>
	 * Called via ASM at the beginning of {@link World#func_147447_a(Vec3, Vec3, boolean, boolean, boolean)} (rayTrace())
	 *
	 * @param world the world
	 * @param src the src
	 * @param dest the dest
	 */
	public static void setRayTraceInfos(World world, Vec3 src, Vec3 dest)
	{
		get(world).setRayTraceInfos(src, dest);
	}

	/**
	 * Sets the ray trace infos.
	 *
	 * @param src the src
	 * @param dest the dest
	 */
	private void setRayTraceInfos(Vec3 src, Vec3 dest)
	{
		if (src == null || dest == null)
			return;

		rayTraceProcedure.src = new Point(src);
		rayTraceProcedure.dest = new Point(dest);
	}

	/**
	 * Gets the ray trace result.<br>
	 * Called via ASM from {@link World#func_147447_a(Vec3, Vec3, boolean, boolean, boolean)} (rayTrace()) before each return.
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

		rayTraceProcedure.src = null;
		rayTraceProcedure.dest = null;

		return rayTraceProcedure.mop;
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

	private class RayTraceProcedure implements TLongProcedure
	{
		private Point src;
		private Point dest;
		private MovingObjectPosition mop;

		@Override
		public boolean execute(long coord)
		{
			RaytraceBlock rt = RaytraceBlock.set(src, dest, getX(coord), getY(coord), getZ(coord));
			mop = getClosest(src, rt.trace(), mop);

			return false;
		}

	}

	//#end getRayTraceResult

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

	@SuppressWarnings("unused")
	private static String printCoord(long coord)
	{
		return getX(coord) + ", " + getY(coord) + ", " + getZ(coord);
	}

	@SuppressWarnings("unused")
	private static String printChunk(Chunk chunk)
	{
		return chunk.xPosition + ", " + chunk.zPosition;
	}
}
