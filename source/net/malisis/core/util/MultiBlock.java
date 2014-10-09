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

package net.malisis.core.util;

import net.malisis.core.MalisisCore;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * @author Ordinastie
 *
 */
public class MultiBlock
{
	private World world;
	private Block block;
	private ForgeDirection direction;
	private int width;
	private int height;
	private int depth;
	private int x;
	private int y;
	private int z;

	public MultiBlock(ForgeDirection dir, int width, int height, int depth, int x, int y, int z)
	{
		this.direction = dir;
		this.width = width;
		this.height = height;
		this.depth = depth;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	private Block getBlock()
	{
		if (block == null && world != null)
			block = world.getBlock(x, y, z);
		return block;
	}

	public void setWorld(World world)
	{
		this.world = world;
	}

	//	private void setSize(int width, int height, int depth)
	//	{
	//		this.width = width;
	//		this.height = height;
	//		this.depth = depth;
	//	}

	/**
	 * Gets a list of block position for this <code>MultiBlock</code>. Does not include original block position.
	 *
	 * @param x
	 * @param y
	 * @param z
	 * @param dir
	 * @return
	 */
	protected ChunkPosition[] getListPositions()
	{
		ChunkPosition[] pos = new ChunkPosition[width * height * depth - 1];

		int w = width;
		int d = depth;

		if (direction == ForgeDirection.EAST || direction == ForgeDirection.WEST)
		{
			w = depth;
			d = width;
		}

		int sX = x;
		int sY = y;
		int sZ = z;
		int eX = x + w;
		int eY = y + height;
		int eZ = z + d;

		if (direction == ForgeDirection.NORTH)
		{
			sZ = z - d + 1;
			eZ = z + 1;
		}
		else if (direction == ForgeDirection.SOUTH)
		{
			sX = x - w + 1;
			eX = x + 1;
		}
		else if (direction == ForgeDirection.WEST)
		{
			sX = x - w + 1;
			eX = x + 1;
			sZ = z - d + 1;
			eZ = z + 1;
		}

		int n = 0;
		for (int i = sX; i < eX; i++)
			for (int j = sY; j < eY; j++)
				for (int k = sZ; k < eZ; k++)
					if (i != x || j != y || k != z) // excluse origin
						pos[n++] = new ChunkPosition(i, j, k);

		return pos;
	}

	/**
	 * To be called from inside block.onBlockPlacedBy()
	 *
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @param player
	 * @param itemStack
	 */
	public void placeBlocks()
	{
		ChunkPosition[] listPos = getListPositions();
		for (ChunkPosition pos : listPos)
		{
			if (pos == null)
				return;

			if (!getBlock().canPlaceBlockAt(world, pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ))
			{
				//cancel placement : remove block
				world.setBlockToAir(x, y, z);
				return;
			}
		}

		IProvider te = TileEntityUtils.getTileEntity(IProvider.class, world, x, y, z);
		if (te == null)
		{
			MalisisCore.log.error("[MultiBlock] Tried to set multiblock in provider, but no IProvider found at {}, {}, {}", x, y, z);
			return;
		}
		te.setMultiBlock(this);
		for (ChunkPosition pos : listPos)
		{
			world.setBlock(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ, getBlock(), direction.ordinal(), 1);
			te = TileEntityUtils.getTileEntity(IProvider.class, world, pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ);
			te.setMultiBlock(this);
		}
		world.setBlockMetadataWithNotify(x, y, z, direction.ordinal(), 2);
	}

	/**
	 * To be called from inside Block.removedByPlayer()
	 *
	 * @param world
	 * @param player
	 * @param x
	 * @param y
	 * @param z
	 * @param willHarvest
	 * @return
	 */
	public boolean removeBlocks()
	{
		ChunkPosition[] listPos = getListPositions();
		for (ChunkPosition pos : listPos)
			world.setBlock(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ, Blocks.air, 0, 2);
		world.setBlockToAir(x, y, z);

		return true;
	}

	/**
	 * Creates a MultiBlock structure.<br />
	 * To be used from block.onBlockPlacedBy()
	 *
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @param width
	 * @param height
	 * @param depth
	 * @param dir
	 * @return
	 */
	public static MultiBlock create(World world, int x, int y, int z, int width, int height, int depth, ForgeDirection dir)
	{
		MultiBlock mb = new MultiBlock(dir, width, height, depth, x, y, z);
		mb.setWorld(world);
		mb.placeBlocks();

		return mb;
	}

	/**
	 * @param tag
	 */
	public void writeToNBT(NBTTagCompound tag)
	{
		NBTTagCompound mbTag = new NBTTagCompound();
		mbTag.setInteger("direction", direction.ordinal());
		mbTag.setInteger("width", width);
		mbTag.setInteger("height", height);
		mbTag.setInteger("depth", depth);
		mbTag.setInteger("x", x);
		mbTag.setInteger("y", y);
		mbTag.setInteger("z", z);

		tag.setTag("multiBlock", mbTag);
	}

	/**
	 * Creates MultiBlock structure.<br />
	 * To be used from tileEntity.readNBT()
	 *
	 * @param tag
	 * @return
	 */
	public static MultiBlock create(NBTTagCompound tag)
	{
		if (!tag.hasKey("multiBlock"))
		{
			MalisisCore.log.error("[MultiBlock] Couldn't read MultiBlock informations from tag {}", tag);
			return null;
		}
		tag = tag.getCompoundTag("multiBlock");

		ForgeDirection dir = ForgeDirection.getOrientation(tag.getInteger("direction"));
		int width = tag.getInteger("width");
		int height = tag.getInteger("height");
		int depth = tag.getInteger("depth");
		int x = tag.getInteger("x");
		int y = tag.getInteger("y");
		int z = tag.getInteger("z");

		MultiBlock mb = new MultiBlock(dir, width, height, depth, x, y, z);

		return mb;
	}

	public static boolean destroy(World world, int x, int y, int z)
	{
		IProvider te = TileEntityUtils.getTileEntity(IProvider.class, world, x, y, z);
		if (te == null)
		{
			MalisisCore.log.error("[MultiBlock] Couldn't find IProvider at {}, {}, {}", x, y, z);
			return false;
		}

		MultiBlock mb = te.getMultiBlock();
		if (mb == null)
		{
			MalisisCore.log.error("[MultiBlock] No MultiBlock for IProvider at {}, {}, {}", x, y, z);
			return false;
		}

		mb.removeBlocks();

		return true;
	}

	public static interface IProvider
	{
		public void setMultiBlock(MultiBlock multiBlock);

		public MultiBlock getMultiBlock();
	}

}
