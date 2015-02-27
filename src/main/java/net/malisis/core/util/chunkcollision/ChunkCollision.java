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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import net.malisis.core.block.BoundingBoxType;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;

import com.google.common.primitives.Ints;

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

	// /!\ Logical side!
	private Side side;
	private Map<Chunk, Set<Integer>> chunks = new WeakHashMap();

	private ChunkCollision(Side side)
	{
		this.side = side;
	}

	private void addCoord(Chunk chunk, int coord)
	{
		Set<Integer> coords = chunks.get(chunk);
		if (coords == null)
		{
			coords = new HashSet<>();
			chunks.put(chunk, coords);
		}
		coords.add(coord);
	}

	private void removeCoord(Chunk chunk, int coord)
	{
		Set<Integer> coords = chunks.get(chunk);
		if (coords == null)
			return;
		coords.remove(coord);
		if (coords.size() == 0)
			chunks.remove(chunk);

	}

	public List<AxisAlignedBB> getCollisionBoxes(World world, AxisAlignedBB mask, Entity entity)
	{
		int tminX = MathHelper.floor_double(mask.minX);
		int tmaxX = MathHelper.floor_double(mask.maxX);
		int tminZ = MathHelper.floor_double(mask.minZ);
		int tmaxZ = MathHelper.floor_double(mask.maxZ);

		int minX = chunkX(tminX) - 1;
		int maxX = chunkX(tmaxX) + 1;
		int minZ = chunkZ(tminZ) - 1;
		int maxZ = chunkZ(tmaxZ) + 1;

		List<AxisAlignedBB> list = new ArrayList<>();

		for (int cx = minX; cx <= maxX; ++cx)
		{
			for (int cz = minZ; cz <= maxZ; ++cz)
			{
				Set<Integer> coords = chunks.get(world.getChunkFromChunkCoords(cx, cz));
				if (coords != null)
				{
					Iterator<Integer> iterator = coords.iterator();
					while (iterator.hasNext())
					{
						int coord = iterator.next();
						int x = getX(cx, coord);
						int y = getY(coord);
						int z = getZ(cz, coord);
						Block block = world.getBlock(x, y, z);
						if (block instanceof IChunkCollidable)
						{
							AxisAlignedBB[] aabbs = ((IChunkCollidable) block).getBoundingBox(world, x, y, z,
									BoundingBoxType.CHUNKCOLLISION);
							filter(list, mask, aabbs, x, y, z);
						}
						else
							iterator.remove();
					}
				}
			}
		}

		return list;
	}

	private void filter(List<AxisAlignedBB> list, AxisAlignedBB mask, AxisAlignedBB[] aabbs, int x, int y, int z)
	{
		for (AxisAlignedBB aabb : aabbs)
		{
			aabb.offset(x, y, z);
			if (aabb != null && mask.intersectsWith(aabb))
				list.add(aabb);
		}
	}

	public void setCoords(int chunkX, int chunkZ, int[] coords)
	{
		if (side != Side.CLIENT)
			return;

		Chunk chunk = Minecraft.getMinecraft().theWorld.getChunkFromChunkCoords(chunkX, chunkZ);
		chunks.put(chunk, new HashSet<>(Ints.asList(coords)));
	}

	@SubscribeEvent
	public void onDataLoad(ChunkDataEvent.Load event)
	{
		if (side == Side.CLIENT)
			return;

		NBTTagCompound nbt = event.getData();
		if (!nbt.hasKey("chunkCollision"))
			return;

		int[] coords = nbt.getIntArray("chunkCollision");
		chunks.put(event.getChunk(), new HashSet(Ints.asList(coords)));
	}

	@SubscribeEvent
	public void onDataSave(ChunkDataEvent.Save event)
	{
		if (side == Side.CLIENT)
			return;

		Collection<Integer> coords = chunks.get(event.getChunk());
		if (coords == null || coords.size() == 0)
			return;

		NBTTagCompound nbt = event.getData();
		nbt.setIntArray("chunkCollision", Ints.toArray(coords));
	}

	@SubscribeEvent
	public void onChunkWatched(ChunkWatchEvent.Watch event)
	{
		Chunk chunk = event.player.worldObj.getChunkFromChunkCoords(event.chunk.chunkXPos, event.chunk.chunkZPos);
		Collection<Integer> coords = chunks.get(chunk);
		if (coords == null || coords.size() == 0)
			return;

		ChunkCollisionMessage.sendCoords(chunk, Ints.toArray(coords), event.player);
	}

	public static ChunkCollision get(World world)
	{
		return world.isRemote ? client : server;
	}

	public static int chunkX(int x)
	{
		return x >> 4;
	}

	public static int chunkZ(int z)
	{
		return z >> 4;
	}

	public static int getInt(int x, int y, int z)
	{
		return (x & 15) | ((y & 255) << 4) | ((z & 15) << 12);
	}

	public static int getX(Chunk chunk, int coord)
	{
		return getX(chunk.xPosition, coord);
	}

	public static int getX(int chunkX, int coord)
	{
		return (chunkX << 4) + (coord & 15);
	}

	public static int getY(int coord)
	{
		return (coord >> 4) & 255;
	}

	public static int getZ(Chunk chunk, int coord)
	{
		return getZ(chunk.zPosition, coord);
	}

	public static int getZ(int chunkZ, int coord)
	{
		return (chunkZ << 4) + ((coord >> 12) & 15);
	}

	public static Block getBlock(Chunk chunk, int coord)
	{
		return chunk.worldObj.getBlock(getX(chunk, coord), getY(coord), getZ(chunk, coord));
	}

	public static String printCoord(Chunk chunk, int coord)
	{
		return getX(chunk, coord) + ", " + getY(coord) + ", " + getZ(chunk, coord);
	}

	public static String printChunk(Chunk chunk)
	{
		return chunk.xPosition + ", " + chunk.zPosition;
	}

	public static void register(World world, int x, int y, int z)
	{
		ChunkCollision cc = get(world);
		Chunk chunk = world.getChunkFromBlockCoords(x, z);
		int coord = getInt(x, y, z);
		cc.addCoord(chunk, coord);
	}

	public static void updateChunkCollision(Chunk chunk, int x, int y, int z, Block old, Block block)
	{
		ChunkCollision cc = get(chunk.worldObj);
		int coord = getInt(chunk.xPosition * 16 + x, y, chunk.zPosition * 16 + z);
		if (old instanceof IChunkCollidable)
			cc.removeCoord(chunk, coord);
		if (block instanceof IChunkCollidable)
			cc.addCoord(chunk, coord);
	}

	public static void getCollisionBoundingBoxes(World world, AxisAlignedBB mask, List<AxisAlignedBB> list, Entity entity)
	{
		list.addAll(get(world).getCollisionBoxes(world, mask, entity));
	}

}
