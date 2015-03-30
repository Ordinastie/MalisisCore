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

package net.malisis.core.util.chunkblock;

import gnu.trove.procedure.TLongProcedure;
import gnu.trove.set.hash.TLongHashSet;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import net.malisis.core.MalisisCore;
import net.malisis.core.util.BlockPos;
import net.malisis.core.util.BlockState;
import net.malisis.core.util.chunkcollision.ChunkCollision;
import net.malisis.core.util.chunkcollision.IChunkCollidable;
import net.malisis.core.util.chunklistener.ChunkListener;
import net.malisis.core.util.chunklistener.IBlockListener;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

/**
 * This class is the enty point for all the chunk collision related calculation.<br>
 * The static methods are called via ASM which then call the process for the corresponding server or client instance.
 *
 * @author Ordinastie
 *
 */
public class ChunkBlockHandler implements IChunkBlockHandler
{
	private static ChunkBlockHandler instance = new ChunkBlockHandler();

	private Map<Chunk, TLongHashSet> serverChunks = new WeakHashMap();
	private Map<Chunk, TLongHashSet> clientChunks = new WeakHashMap();
	private List<IChunkBlockHandler> handlers = new ArrayList();

	public ChunkBlockHandler()
	{
		handlers.add(new ChunkListener());
		handlers.add(ChunkCollision.get());
	}

	private TLongHashSet getCoords(Chunk chunk)
	{
		Map<Chunk, TLongHashSet> chunks = chunk.worldObj.isRemote ? clientChunks : serverChunks;
		TLongHashSet coords = chunks.get(chunk);
		if (coords == null)
		{
			coords = new TLongHashSet();
			chunks.put(chunk, coords);
		}
		return coords;
	}

	/**
	 * Call a {@link ChunkProcedure} this specified {@link Chunk}.
	 *
	 * @param chunk the chunk
	 * @param procedure the procedure
	 */
	public void callProcedure(Chunk chunk, ChunkProcedure procedure)
	{
		procedure.set(chunk);
		TLongHashSet coords = getCoords(chunk);
		if (coords != null)
			coords.forEach(procedure);
	}

	public void addHandler(IChunkBlockHandler handler)
	{
		handlers.add(handler);
	}

	//#region updateCoordinates
	/**
	 * Update chunk coordinates.<br>
	 * Called via ASM from {@link Chunk#setBlockIDWithMetadata(int, int, int, Block, int)} Notifies all {@link IBlockListener} for that
	 * chunk.<br>
	 *
	 * @param chunk the chunk
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @param old the old
	 * @param block the block
	 * @return true, if block can be placed, false if canceled
	 */
	public boolean updateCoordinates(Chunk chunk, int x, int y, int z, Block old, Block block)
	{
		boolean canceled = false;
		BlockPos pos = new BlockPos(x, y, z);
		for (IChunkBlockHandler handler : handlers)
			canceled |= handler.updateCoordinates(chunk, pos, old, block);

		//*this* handler needs to be canceled, so it's called last
		if (!canceled)
			updateCoordinates(chunk, pos, old, block);

		return !canceled;

	}

	@Override
	public boolean updateCoordinates(Chunk chunk, BlockPos pos, Block old, Block block)
	{
		if (old instanceof IChunkBlock)
			removeCoord(chunk.worldObj, pos, ((IChunkBlock) old).blockRange());
		if (block instanceof IChunkBlock)
			addCoord(chunk.worldObj, pos, ((IChunkBlock) block).blockRange());

		return true;
	}

	/**
	 * Adds coordinate for the {@link Chunk}s around {@link BlockPos}.
	 *
	 * @param world the world
	 * @param pos the pos
	 * @param size the size
	 */
	private void addCoord(World world, BlockPos pos, int size)
	{
		List<Chunk> affectedChunks = getAffectedChunks(world, pos.getX(), pos.getZ(), size);
		for (Chunk chunk : affectedChunks)
			addCoord(chunk, pos);
	}

	/**
	 * Adds coordinate for the specified {@link Chunk}.
	 *
	 * @param chunk the chunk
	 * @param pos the pos
	 */
	private void addCoord(Chunk chunk, BlockPos pos)
	{
		//MalisisCore.message("Added " + pos + " to " + chunk.xPosition + ", " + chunk.zPosition);
		getCoords(chunk).add(pos.toLong());
	}

	/**
	 * Removes coordinate from the {@link Chunk}s around the {@link BlockPos}.
	 *
	 * @param world the world
	 * @param pos the pos
	 * @param size the size
	 */
	private void removeCoord(World world, BlockPos pos, int size)
	{
		List<Chunk> affectedChunks = getAffectedChunks(world, pos.getX(), pos.getZ(), size);
		for (Chunk chunk : affectedChunks)
			removeCoord(chunk, pos);
	}

	/**
	 * Removes coordiante from the specified {@link Chunk}.
	 *
	 * @param chunk the chunk
	 * @param pos the pos
	 */
	private void removeCoord(Chunk chunk, BlockPos pos)
	{
		if (!getCoords(chunk).remove(pos.toLong()))
			MalisisCore.log.error("Failed to remove : {} ({})", pos, pos.toLong());
		//else
		//	MalisisCore.message("Removed " + pos + " from " + chunk.xPosition + ", " + chunk.zPosition);
	}

	//#end updateCoordinates

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
		if (!nbt.hasKey("chunkNotifier"))
			return;

		long[] coords = readLongArray(nbt);
		Map<Chunk, TLongHashSet> chunks = event.getChunk().worldObj.isRemote ? clientChunks : serverChunks;
		chunks.put(event.getChunk(), new TLongHashSet(coords));
	}

	@SubscribeEvent
	public void onDataSave(ChunkDataEvent.Save event)
	{
		TLongHashSet coords = getCoords(event.getChunk());
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
	private long[] readLongArray(NBTTagCompound compound)
	{
		ByteBuf bytes = Unpooled.copiedBuffer(compound.getByteArray("chunkNotifier"));
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
	private void writeLongArray(NBTTagCompound compound, long[] longs)
	{
		ByteBuf bytes = Unpooled.buffer(longs.length * 8);
		for (long aLong : longs)
			bytes.writeLong(aLong);
		compound.setByteArray("chunkNotifier", bytes.array());
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
		TLongHashSet coords = getCoords(chunk);
		if (coords == null || coords.size() == 0)
			return;

		ChunkBlockMessage.sendCoords(chunk, coords.toArray(), event.player);
	}

	/**
	 * Client only.<br>
	 * Sets the coordinates for a chunk received by {@link ChunkBlockMessage}.
	 *
	 * @param chunkX the chunk x
	 * @param chunkZ the chunk z
	 * @param coords the coords
	 */
	public void setCoords(int chunkX, int chunkZ, long[] coords)
	{
		Chunk chunk = Minecraft.getMinecraft().theWorld.getChunkFromChunkCoords(chunkX, chunkZ);
		Map<Chunk, TLongHashSet> chunks = chunk.worldObj.isRemote ? clientChunks : serverChunks;
		chunks.put(chunk, new TLongHashSet(coords));
	}

	//#end Events

	/**
	 * Gets the chunks inside distance from coordinates.
	 *
	 * @param world the world
	 * @param x the x
	 * @param z the z
	 * @param distance the size
	 * @return the chunks
	 */
	public List<Chunk> getAffectedChunks(World world, int x, int z, int distance)
	{
		AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(x - distance, 0, z - distance, x + distance + 1, 1, z + distance + 1);
		return getAffectedChunks(world, aabb);
	}

	public static List<Chunk> getAffectedChunks(World world, AxisAlignedBB... aabbs)
	{
		List<Chunk> chunks = new ArrayList<>();
		for (AxisAlignedBB aabb : aabbs)
		{
			for (int cx = (int) Math.floor(aabb.minX) >> 4; cx <= (int) Math.ceil(aabb.maxX) >> 4; cx++)
			{
				for (int cz = (int) Math.floor(aabb.minZ) >> 4; cz <= (int) Math.ceil(aabb.maxZ) >> 4; cz++)
				{
					if (world.getChunkProvider().chunkExists(cx, cz))
						chunks.add(world.getChunkFromChunkCoords(cx, cz));
				}
			}
		}
		return chunks;
	}

	public static ChunkBlockHandler get()
	{
		return instance;
	}

	public static abstract class ChunkProcedure implements TLongProcedure
	{
		protected World world;
		protected Chunk chunk;
		protected BlockState state;

		protected void set(Chunk chunk)
		{
			this.world = chunk.worldObj;
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
			state = new BlockState(world, coord);

			if (!(state.getBlock() instanceof IChunkBlock))
			{
				MalisisCore.log.info("[ChunkNotificationHandler]  Removing invalid {} coordinate : {} in chunk {},{}",
						world.isRemote ? "client" : "server", state, chunk.xPosition, chunk.zPosition);
				get().removeCoord(chunk, state.getPos());
				return false;
			}

			return true;
		}

		protected void clean()
		{
			world = null;
			chunk = null;
			state = null;
		}
	}

}
