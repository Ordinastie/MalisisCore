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
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

/**
 * @author Ordinastie
 *
 */
public class MBlockState
{
	private static BlockStateFunction toBlockState = new BlockStateFunction();
	private static BlockPredicate blockFilter = new BlockPredicate();

	protected BlockPos pos;
	protected Block block;
	protected IBlockState state;

	public MBlockState(BlockPos pos, IBlockState state)
	{
		this.pos = pos;
		this.block = state.getBlock();
		this.state = state;
	}

	public MBlockState(BlockPos pos, Block block)
	{
		this.pos = pos;
		this.block = block;
		this.state = block.getDefaultState();
	}

	public MBlockState(IBlockState state)
	{
		this.block = state.getBlock();
		this.state = state;
	}

	public MBlockState(Block block)
	{
		this.block = block;
		this.state = block.getDefaultState();
	}

	public MBlockState(IBlockAccess world, BlockPos pos)
	{
		this.pos = pos;
		this.state = world.getBlockState(pos);
		this.block = state.getBlock();
		this.state = block.getActualState(state, world, pos);
	}

	public MBlockState(IBlockAccess world, long coord)
	{
		this(world, BlockPos.fromLong(coord));
	}

	public MBlockState(BlockPos pos, MBlockState state)
	{
		this(pos, state.getBlockState());
	}

	public BlockPos getPos()
	{
		return pos;
	}

	public Block getBlock()
	{
		return block;
	}

	public IBlockState getBlockState()
	{
		return state;
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

	public MBlockState offset(BlockPos pos)
	{
		return new MBlockState(this.pos.add(pos), this);
	}

	public MBlockState rotate(int count)
	{
		IBlockState newState = state;
		for (IProperty prop : (Set<IProperty>) state.getProperties().keySet())
		{
			if (prop instanceof PropertyDirection)
			{
				EnumFacing facing = EnumFacingUtils.rotateFacing((EnumFacing) state.getValue(prop), 4 - count);
				newState = newState.withProperty(prop, facing);
			}
		}

		return new MBlockState(BlockPosUtils.rotate(pos, count), newState);
	}

	public void placeBlock(World world)
	{
		world.setBlockState(pos, state);
	}

	public void placeBlock(World world, int flag)
	{
		world.setBlockState(pos, state, flag);
	}

	public void breakBlock(World world, int flag)
	{
		world.setBlockState(pos, Blocks.air.getDefaultState(), flag);
	}

	public boolean matchesWorld(IBlockAccess world)
	{
		MBlockState mstate = new MBlockState(world, pos);
		return mstate.getBlock() == getBlock()
				&& getBlock().getMetaFromState(mstate.getBlockState()) == getBlock().getMetaFromState(getBlockState());
	}

	public static Iterable<MBlockState> getAllInBox(IBlockAccess world, BlockPos from, BlockPos to, Block block, boolean skipAir)
	{
		FluentIterable<MBlockState> it = FluentIterable.from(BlockPos.getAllInBox(from, to)).transform(toBlockState.set(world));
		if (block != null || skipAir)
			it.filter(blockFilter.set(block, skipAir));

		return it;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof MBlockState))
			return false;

		MBlockState bs = (MBlockState) obj;
		return pos.equals(bs.pos) && block == bs.block && state == bs.state;
	}

	@Override
	public String toString()
	{
		return "[" + pos + "] " + (block != null ? block.getUnlocalizedName().substring(5) + " (" + state + ")" : "");
	}

	public static IBlockState fromNBT(NBTTagCompound nbt)
	{
		return fromNBT(nbt, "block", "metadata");
	}

	public static IBlockState fromNBT(NBTTagCompound nbt, String blockName, String metadataName)
	{
		Block block;
		if (nbt.hasKey(blockName, NBT.TAG_INT))
			block = Block.getBlockById(nbt.getInteger(blockName));
		else
			block = Block.getBlockFromName(nbt.getString(blockName));

		int metadata = nbt.getInteger(metadataName);

		return block.getStateFromMeta(metadata);
	}

	public static NBTTagCompound toNBT(NBTTagCompound nbt, IBlockState state)
	{
		return toNBT(nbt, state, "block", "metadata");
	}

	public static NBTTagCompound toNBT(NBTTagCompound nbt, IBlockState state, String blockName, String metadataName)
	{
		if (state == null)
			return nbt;

		nbt.setString(blockName, Block.blockRegistry.getNameForObject(state.getBlock()).toString());
		nbt.setInteger(metadataName, state.getBlock().getMetaFromState(state));
		return nbt;
	}

	public static class BlockStateFunction implements Function<BlockPos, MBlockState>
	{
		public WeakReference<IBlockAccess> world;

		public BlockStateFunction set(IBlockAccess world)
		{
			this.world = new WeakReference<IBlockAccess>(world);
			return this;
		}

		@Override
		public MBlockState apply(BlockPos pos)
		{
			return new MBlockState(world.get(), pos);
		}
	}

	public static class BlockPredicate implements Predicate<MBlockState>
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
		public boolean apply(MBlockState state)
		{
			if (block == null)
				return state.getBlock() != Blocks.air;
			else
				return state.getBlock() == block;
		}

	}
}
