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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * This class handle MultiBlock structures. Primary aim is to emulate block bigger than 1x1x1 so that the logic is only concerning the
 * original block.<br>
 * IProviders should only do processes for the one tied to the original placed block.<br >
 * Renderers should check for original block/tileEntity before rendering.
 *
 * @author Ordinastie
 *
 */
public class MultiBlock
{
	protected World world;
	protected Block block;
	protected AxisAlignedBB aabb;
	protected ForgeDirection direction;

	protected int x;
	protected int y;
	protected int z;

	public MultiBlock(int x, int y, int z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public MultiBlock(World world, int x, int y, int z)
	{
		this(x, y, z);
		setWorld(world);
	}

	public MultiBlock(World world, int x, int y, int z, AxisAlignedBB aabb)
	{
		this(x, y, z);
		setWorld(world);
		setBounds(aabb);
	}

	public MultiBlock(World world, int x, int y, int z, int width, int height, int depth)
	{
		this(x, y, z);
		setWorld(world);
		setSize(width, height, depth);
	}

	public MultiBlock(NBTTagCompound tag)
	{
		readFromNBT(tag);
	}

	public int getX()
	{
		return x;
	}

	public int getY()
	{
		return y;
	}

	public int getZ()
	{
		return z;
	}

	public void setBlock(Block block)
	{
		this.block = block;
	}

	/**
	 * @return the Block composing this {@link MultiBlock}.
	 */
	private Block getBlock()
	{
		if (block == null && world != null)
			block = world.getBlock(x, y, z);
		return block;
	}

	/**
	 * Sets the world object for this {@link MultiBlock}.<br>
	 * To be called from the TileEntity.setWorldObj() providing this {@link MultiBlock}.
	 *
	 * @param world the world
	 */
	public void setWorld(World world)
	{
		this.world = world;
	}

	/**
	 * Sets a facing for this {@link MultiBlock}.
	 *
	 * @param direction the direction
	 */
	public void setDirection(ForgeDirection direction)
	{
		this.direction = direction;
	}

	public ForgeDirection getDirection()
	{
		return direction != null ? direction : ForgeDirection.UNKNOWN;
	}

	/**
	 * Checks whether the coordinates passed are the origin of this {@link MultiBlock}.
	 *
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @return true, if the coordinates match the origin
	 */
	public boolean isOrigin(int x, int y, int z)
	{
		return this.x == x && this.y == y && this.z == z;
	}

	/**
	 * Sets the size for this {@link MultiBlock}.
	 *
	 * @param width the width
	 * @param height the height
	 * @param depth the depth
	 */
	public void setSize(int width, int height, int depth)
	{
		int minX = width > 0 ? 0 : width + 1;
		int maxX = width > 0 ? width : 1;
		int minY = height > 0 ? 0 : height + 1;
		int maxY = height > 0 ? height : 1;
		int minZ = depth > 0 ? 0 : depth + 1;
		int maxZ = depth > 0 ? depth : 1;

		setBounds(AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ));
	}

	/**
	 * Sets a bounding box for this {@link MultiBlock}. To be used when the origin needs not to be in a corner.<br>
	 * The AxisAlignedBB must englobe origin point.
	 *
	 * @param aabb the new bounds
	 */
	public void setBounds(AxisAlignedBB aabb)
	{
		if (aabb.minX > 0 || aabb.maxX < 0)
			throw new IllegalArgumentException("Bounds need to contain X origin point");
		if (aabb.maxX - aabb.minX < 1)
			throw new IllegalArgumentException("Width needs to be greater or equal to 1");
		if (aabb.minY > 0 || aabb.maxY < 0)
			throw new IllegalArgumentException("Bounds need to contain Y origin point");
		if (aabb.maxY - aabb.minY < 1)
			throw new IllegalArgumentException("Height needs to be greater or equal to 1");
		if (aabb.minZ > 0 || aabb.maxZ < 0)
			throw new IllegalArgumentException("Bounds needs to contain X origin point");
		if (aabb.maxZ - aabb.minZ < 1)
			throw new IllegalArgumentException("Width need to be greater or equal to 1");

		this.aabb = aabb.copy();
	}

	public AxisAlignedBB getBounds()
	{
		if (this.aabb == null)
			return null;

		AxisAlignedBB aabb = this.aabb.copy();

		if (direction != null)
		{
			if (direction == ForgeDirection.EAST || direction == ForgeDirection.WEST)
				aabb.setBounds(aabb.minZ, aabb.minY, aabb.minX, aabb.maxZ, aabb.maxY, aabb.maxX);

			double shiftX = 1 - aabb.maxX - aabb.minX;
			double shiftZ = 1 - aabb.maxZ - aabb.minZ;

			if (direction == ForgeDirection.NORTH)
				aabb.offset(0, 0, shiftZ);
			else if (direction == ForgeDirection.SOUTH)
				aabb.offset(shiftX, 0, 0);
			else if (direction == ForgeDirection.WEST)
				aabb.offset(shiftX, 0, shiftZ);
		}

		return aabb;
	}

	public AxisAlignedBB getWorldBounds()
	{
		return getBounds().offset(x, y, z);
	}

	/**
	 * Gets a list of {@link ChunkPosition} for this {@link MultiBlock}. Does not include original block position.
	 *
	 * @return the list of positions
	 */
	protected ChunkPosition[] getListPositions()
	{
		AxisAlignedBB aabb = getBounds();
		if (aabb == null)
			return new ChunkPosition[0];

		int sX = x + (int) aabb.minX;
		int sY = y + (int) aabb.minY;
		int sZ = z + (int) aabb.minZ;
		int eX = x + (int) aabb.maxX;
		int eY = y + (int) aabb.maxY;
		int eZ = z + (int) aabb.maxZ;

		int size = (eX - sX) * (eY - sY) * (eZ - sZ) - 1;
		ChunkPosition[] pos = new ChunkPosition[size >= 0 ? size : 0];

		int n = 0;
		for (int i = sX; i < eX; i++)
			for (int j = sY; j < eY; j++)
				for (int k = sZ; k < eZ; k++)
					if (i != x || j != y || k != z) // exclude origin
						pos[n++] = new ChunkPosition(i, j, k);

		return pos;
	}

	/**
	 * Place Block for every position occupied by this {@link MultiBlock}.<br>
	 * To be called from inside
	 * {@link Block#onBlockPlacedBy(World, int, int, int, net.minecraft.entity.EntityLivingBase, net.minecraft.item.ItemStack)}.
	 *
	 * @return true, if all the blocks could be placed, false otherwise
	 */
	public boolean placeBlocks()
	{
		return placeBlocks(false);
	}

	/**
	 * Place Block for every position occupied by this {@link MultiBlock}.<br>
	 * To be called from inside
	 * {@link Block#onBlockPlacedBy(World, int, int, int, net.minecraft.entity.EntityLivingBase, net.minecraft.item.ItemStack)}.
	 *
	 * @param placeOrigin true if the origin block should be place. The block must be already set
	 * @return true, if all the blocks could be placed, false otherwise
	 */
	public boolean placeBlocks(boolean placeOrigin)
	{
		if (placeOrigin)
		{
			if (getBlock() == null)
			{
				MalisisCore.log.error("[MultiBlock] Tried to set multiblock origin at {}, {}, {}, but no block is set.", x, y, z);
				return false;
			}
			if (getBlock().canPlaceBlockAt(world, x, y, z))
				world.setBlock(x, y, z, getBlock(), 0, 3);
			else
				return false;
		}

		ChunkPosition[] listPos = getListPositions();
		for (ChunkPosition pos : listPos)
		{
			if (pos == null) //should not happen
				return false;

			if (!getBlock().canPlaceBlockAt(world, pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ))
			{
				//cancel placement : remove block
				world.setBlockToAir(x, y, z);
				return false;
			}
		}

		IProvider te = TileEntityUtils.getTileEntity(IProvider.class, world, x, y, z);
		if (te == null)
		{
			MalisisCore.log.error("[MultiBlock] Tried to set multiblock in provider, but no IProvider found at {}, {}, {}", x, y, z);
			return false;
		}
		te.setMultiBlock(this);
		for (ChunkPosition pos : listPos)
		{
			world.setBlock(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ, getBlock(), 0, 1);
			te = TileEntityUtils.getTileEntity(IProvider.class, world, pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ);
			te.setMultiBlock(this);
		}

		//world.setBlockMetadataWithNotify(x, y, z, direction.ordinal(), 2);

		return true;
	}

	/**
	 * Removes the blocks composing this {@link MultiBlock}, including the origin.
	 *
	 * @return true
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
	 * Write this {@link MultiBlock} informations into the provided NBTTagCompound.
	 *
	 * @param tag the tag
	 */
	public void writeToNBT(NBTTagCompound tag)
	{
		NBTTagCompound mbTag = new NBTTagCompound();
		mbTag.setInteger("x", x);
		mbTag.setInteger("y", y);
		mbTag.setInteger("z", z);
		if (direction != null)
			mbTag.setInteger("direction", direction.ordinal());
		if (aabb != null)
			NBTUtils.writeToNBT(mbTag, aabb);

		tag.setTag("multiBlock", mbTag);
	}

	/**
	 * Creates MultiBlock structure using the provided NBTTagCompound.<br>
	 * To be used from tileEntity.readNBT()
	 *
	 * @param tag the tag
	 */
	public void readFromNBT(NBTTagCompound tag)
	{
		if (!tag.hasKey("multiBlock"))
		{
			MalisisCore.log.error("[MultiBlock] Couldn't read MultiBlock informations from tag {}", tag);
			return;
		}
		tag = tag.getCompoundTag("multiBlock");

		x = tag.getInteger("x");
		y = tag.getInteger("y");
		z = tag.getInteger("z");
		aabb = AxisAlignedBB.getBoundingBox(0, 0, 0, 1, 1, 1);
		aabb = NBTUtils.readFromNBT(tag, aabb);
		if (tag.hasKey("direction"))
			direction = ForgeDirection.getOrientation(tag.getInteger("direction"));

		return;
	}

	/**
	 * Destroy this {@link MultiBlock}. <br>
	 * Will remove all the blocks composing this {@link MultiBlock} structure.<br>
	 * To be called from inside Block.removedByPlayer().
	 *
	 * @param world the world
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @return true if blocks were removed
	 */
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

	/**
	 * Gets the {@link MultiBlock} instance at the specified coordinates.<br>
	 *
	 * @param world the world
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @return the MultiBlock
	 */
	public static MultiBlock getMultiBlock(IBlockAccess world, int x, int y, int z)
	{
		IProvider provider = TileEntityUtils.getTileEntity(IProvider.class, world, x, y, z);
		if (provider == null)
			return null;

		return provider.getMultiBlock();
	}

	/**
	 * Checks whether the coordinates passed are the origin of a {@link MultiBlock}.
	 *
	 * @param world the world
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @return true, if the specified coordinates match the origin
	 */
	public static boolean isOrigin(IBlockAccess world, int x, int y, int z)
	{
		MultiBlock mb = getMultiBlock(world, x, y, z);
		if (mb == null)
			return false;
		return mb.isOrigin(x, y, z);
	}

	public static <T extends TileEntity & IProvider> T getOriginProvider(Class<T> providerClass, IBlockAccess world, int x, int y, int z)
	{
		return getOriginProvider(TileEntityUtils.getTileEntity(providerClass, world, x, y, z));
	}

	public static <T extends TileEntity & IProvider> T getOriginProvider(T provider)
	{
		if (provider == null)
			return null;

		MultiBlock mb = provider.getMultiBlock();
		if (mb == null)
			return null;
		if (provider.xCoord == mb.x && provider.yCoord == mb.y && provider.zCoord == mb.z)
			return provider;

		TileEntity te = provider.getWorldObj().getTileEntity(mb.x, mb.y, mb.z);
		if (te == null || !(te instanceof IProvider))
			return null;
		return (T) te;
	}

	public static interface IProvider
	{
		/**
		 * Sets the {@link MultiBlock} instance for this {@link IProvider}.
		 *
		 * @param multiBlock the MultiBlock
		 */
		public void setMultiBlock(MultiBlock multiBlock);

		/**
		 * @return the {@link MultiBlock} instance of the provider.
		 */
		public MultiBlock getMultiBlock();
	}

}
