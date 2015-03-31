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

import net.malisis.core.util.BlockPos;
import net.malisis.core.util.BlockState;
import net.minecraft.world.World;

/**
 * @author Ordinastie
 *
 */
public abstract class MultiBlock implements Iterable<BlockState>
{
	protected Map<BlockPos, BlockState> states = new HashMap<>();

	protected BlockPos offset;

	public void setOffset(BlockPos offset)
	{
		this.offset = offset;
	}

	public boolean isFromMultiblock(BlockPos pos)
	{
		return getBlockState(pos) != null;
	}

	public BlockState getBlockState(BlockPos pos)
	{
		return states.get(pos);
	}

	public boolean canPlaceBlockAt(World world, BlockPos pos)
	{
		for (BlockState state : this)
		{
			BlockPos p = state.getPos().add(pos);
			if (!state.getBlock().canPlaceBlockAt(world, p.getX(), p.getY(), p.getZ()))
				return false;
		}
		return true;
	}

	public void placeBlocks(World world, BlockPos pos)
	{
		for (BlockState state : this)
		{
			state = state.offset(pos);
			if (!state.getPos().equals(pos))
				state.placeBlock(world, 2);
		}
	}

	public void breakBlocks(World world, BlockPos pos)
	{
		for (BlockState state : this)
		{
			state = state.offset(pos);
			if (!state.getPos().equals(pos))
				state.breakBlock(world, 2);
		}
	}

	@Override
	public Iterator<BlockState> iterator()
	{
		return states.values().iterator();
	}

	protected abstract void buildStates();
}
