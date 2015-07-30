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

package net.malisis.core.util.multiblock;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.malisis.core.renderer.element.Vertex;
import net.malisis.core.util.BlockPosUtils;
import net.malisis.core.util.MBlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;

/**
 * @author Ordinastie
 *
 */
public abstract class MultiBlock implements Iterable<MBlockState>, IBlockAccess
{
	protected Map<BlockPos, MBlockState> states = new HashMap<>();

	protected BlockPos offset;
	protected int rotation;

	public void setOffset(BlockPos offset)
	{
		this.offset = offset;
		buildStates();
	}

	public void setRotation(int rotation)
	{
		this.rotation = rotation;
	}

	public boolean isFromMultiblock(BlockPos pos)
	{
		return getBlockState(pos) != null;
	}

	public MBlockState getState(BlockPos pos)
	{
		pos = BlockPosUtils.rotate(pos, 4 - rotation);
		return states.get(pos);
	}

	public boolean canPlaceBlockAt(World world, BlockPos pos)
	{
		for (MBlockState state : this)
		{
			BlockPos p = state.getPos().add(pos);
			if (!state.getBlock().canPlaceBlockAt(world, p))
				return false;
		}
		return true;
	}

	public void placeBlocks(World world, BlockPos pos)
	{
		for (MBlockState state : this)
		{
			state = state.rotate(rotation).offset(pos);
			if (!state.getPos().equals(pos))
			{
				state.placeBlock(world, 2);
				state.rotateInWorld(world, rotation);
			}
		}
	}

	public void breakBlocks(World world, BlockPos pos)
	{
		for (MBlockState state : this)
		{
			state = state.rotate(rotation).offset(pos);
			if (!state.getPos().equals(pos))
				state.breakBlock(world, 2);
		}
	}

	public boolean isComplete(World world, BlockPos pos)
	{
		return isComplete(world, pos, null);
	}

	public boolean isComplete(World world, BlockPos pos, MBlockState newState)
	{
		for (MBlockState state : this)
		{
			state = state.offset(pos);
			if (!state.matchesWorld(world) && (newState == null || !state.equals(newState)))
				return false;
		}

		return true;
	}

	@Override
	public Iterator<MBlockState> iterator()
	{
		return states.values().iterator();
	}

	protected abstract void buildStates();

	@Override
	public IBlockState getBlockState(BlockPos pos)
	{
		MBlockState state = getState(pos);
		if (state == null)
			return Blocks.air.getDefaultState();
		return state.getBlockState();
	}

	@Override
	public TileEntity getTileEntity(BlockPos pos)
	{
		return null;
	}

	@Override
	public int getCombinedLight(BlockPos pos, int lightValue)
	{
		return Vertex.BRIGHTNESS_MAX;
	}

	@Override
	public boolean isAirBlock(BlockPos pos)
	{
		return getState(pos).getBlock() == Blocks.air;
	}

	@Override
	public BiomeGenBase getBiomeGenForCoords(BlockPos pos)
	{
		return null;
	}

	@Override
	public boolean extendedLevelsInChunkCache()
	{
		return false;
	}

	@Override
	public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default)
	{
		return getBlockState(pos).getBlock().isSideSolid(this, pos, side);
	}

	@Override
	public int getStrongPower(BlockPos pos, EnumFacing direction)
	{
		return 0;
	}

	@Override
	public WorldType getWorldType()
	{
		return null;
	}
}
