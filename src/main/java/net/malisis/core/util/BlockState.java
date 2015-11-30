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

import java.lang.ref.WeakReference;

import net.malisis.core.util.BlockPos.BlockIterator;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.ForgeDirection;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

/**
 * @author Ordinastie
 *
 */
public class BlockState
{
	private static BlockStateFunction toBlockState = new BlockStateFunction();
	private static BlockPredicate blockFilter = new BlockPredicate();

	protected BlockPos pos;
	protected Block block;
	protected int metadata;

	public BlockState(BlockPos pos, Block block, int metadata)
	{
		this.pos = pos;
		this.block = block;
		this.metadata = metadata;
	}

	public BlockState(BlockPos pos, Block block)
	{
		this(pos, block, 0);
	}

	public BlockState(int x, int y, int z, Block block, int metadata)
	{
		this(new BlockPos(x, y, z), block, metadata);
	}

	public BlockState(int x, int y, int z, Block block)
	{
		this(new BlockPos(x, y, z), block, 0);
	}

	public BlockState(Block block, int metadata)
	{
		this(null, block, metadata);
	}

	public BlockState(Block block)
	{
		this(null, block, 0);
	}

	public BlockState(IBlockAccess world, BlockPos pos)
	{
		this(pos, world.getBlock(pos.getX(), pos.getY(), pos.getZ()), world.getBlockMetadata(pos.getX(), pos.getY(), pos.getZ()));
	}

	public BlockState(IBlockAccess world, int x, int y, int z)
	{
		this(new BlockPos(x, y, z), world.getBlock(x, y, z), world.getBlockMetadata(x, y, z));
	}

	public BlockState(IBlockAccess world, long coord)
	{
		this(world, BlockPos.fromLong(coord));
	}

	public BlockState(BlockPos pos, BlockState state)
	{
		this(pos, state.getBlock(), state.getMetadata());
	}

	public BlockPos getPos()
	{
		return pos;
	}

	public Block getBlock()
	{
		return block;
	}

	public int getMetadata()
	{
		return metadata;
	}

	public int getX()
	{
		return pos.getX();
	}

	public int getY()
	{
		return pos.getY();
	}

	public int getZ()
	{
		return pos.getZ();
	}

	public boolean isAir()
	{
		return getBlock().getMaterial() == Material.air;
	}

	public BlockState offset(BlockPos pos)
	{
		return new BlockState(this.pos.add(pos), this);
	}

	public BlockState rotate(int rotation)
	{
		return new BlockState(this.pos.rotate(rotation), this);
	}

	public void rotateInWorld(World world, int rotation)
	{
		ForgeDirection[] dirs = new ForgeDirection[] { ForgeDirection.NORTH, ForgeDirection.EAST, ForgeDirection.SOUTH, ForgeDirection.WEST };
		block.rotateBlock(world, getX(), getY(), getZ(), dirs[rotation / 90]);
	}

	public void placeBlock(World world)
	{
		world.setBlock(getX(), getY(), getZ(), block, metadata, 3);
	}

	public void placeBlock(World world, int flag)
	{
		world.setBlock(getX(), getY(), getZ(), block, metadata, flag);
	}

	public void breakBlock(World world, int flag)
	{
		world.setBlock(getX(), getY(), getZ(), Blocks.air, 0, flag);
	}

	public boolean matchesWorld(IBlockAccess world)
	{
		return new BlockState(world, pos).equals(this);
	}

	public static Iterable<BlockState> getAllInBox(IBlockAccess world, BlockPos from, BlockPos to, Block block, boolean skipAir)
	{
		FluentIterable<BlockState> it = FluentIterable.from(new BlockIterator(from, to).asIterable()).transform(toBlockState.set(world));
		if (block != null || skipAir)
			it.filter(blockFilter.set(block, skipAir));

		return it;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof BlockState))
			return false;

		BlockState bs = (BlockState) obj;
		return pos.equals(bs.pos) && block == bs.block && metadata == bs.metadata;
	}

	@Override
	public String toString()
	{
		return "[" + pos + "] " + (block != null ? block.getUnlocalizedName().substring(5) + " (" + metadata + ")" : "");
	}

	public static BlockState fromNBT(NBTTagCompound nbt)
	{
		return fromNBT(nbt, "block", "metadata");
	}

	public static BlockState fromNBT(NBTTagCompound nbt, String blockName, String metadataName)
	{
		if (nbt == null)
			return null;

		Block block;
		if (nbt.hasKey(blockName, NBT.TAG_INT))
			block = Block.getBlockById(nbt.getInteger(blockName));
		else
			block = Block.getBlockFromName(nbt.getString(blockName));

		if (block == null)
			return null;

		return new BlockState(block, nbt.getInteger(metadataName));
	}

	public static NBTTagCompound toNBT(NBTTagCompound nbt, BlockState state)
	{
		return toNBT(nbt, state, "block", "metadata");
	}

	public static NBTTagCompound toNBT(NBTTagCompound nbt, BlockState state, String blockName, String metadataName)
	{
		if (state == null)
			return nbt;

		nbt.setString(blockName, Block.blockRegistry.getNameForObject(state.getBlock()).toString());
		nbt.setInteger(metadataName, state.getMetadata());
		return nbt;
	}

	public static class BlockStateFunction implements Function<BlockPos, BlockState>
	{
		public WeakReference<IBlockAccess> world;

		public BlockStateFunction set(IBlockAccess world)
		{
			this.world = new WeakReference<IBlockAccess>(world);
			return this;
		}

		@Override
		public BlockState apply(BlockPos pos)
		{
			return new BlockState(world.get(), pos);
		}
	}

	public static class BlockPredicate implements Predicate<BlockState>
	{
		public Block block;
		public boolean skipAir;

		public BlockPredicate set(Block block, boolean skipAir)
		{
			this.block = block;
			this.skipAir = skipAir;
			return this;
		}

		@Override
		public boolean apply(BlockState state)
		{
			if (block == null)
				return state.getBlock() != Blocks.air;
			else
				return state.getBlock() == block;
		}

	}
}
