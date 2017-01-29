/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Ordinastie
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

package net.malisis.core.block.component;

import java.util.Random;

import net.malisis.core.block.IBlockComponent;
import net.malisis.core.block.IComponentProvider;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * @author Ordinastie
 *
 */
public interface ITickableComponent
{
	public int update(Block block, World world, BlockPos pos, IBlockState state, Random rand);

	public class RandomTickableComponent implements ITickableComponent, IBlockComponent
	{
		private final ITickableComponent tickable;

		private RandomTickableComponent(ITickableComponent tickable)
		{
			this.tickable = tickable;
		}

		@Override
		public void onComponentAdded(IComponentProvider provider)
		{
			if (provider instanceof Block)
				((Block) provider).setTickRandomly(true);
		}

		@Override
		public int update(Block block, World world, BlockPos pos, IBlockState state, Random rand)
		{
			tickable.update(block, world, pos, state, rand);
			return 0;
		}
	}

	public class PeriodicTickableComponent implements ITickableComponent, IBlockComponent
	{
		private final ITickableComponent tickable;
		private final int firstTickDelay;

		public PeriodicTickableComponent(ITickableComponent tickable, int firstTickDelay)
		{
			this.tickable = tickable;
			this.firstTickDelay = firstTickDelay;
		}

		public PeriodicTickableComponent(ITickableComponent tickable)
		{
			this(tickable, 0);
		}

		@Override
		public void onBlockAdded(Block block, World world, BlockPos pos, IBlockState state)
		{
			if (firstTickDelay > 0)
				world.scheduleUpdate(pos, block, 1);
		}

		@Override
		public int update(Block block, World world, BlockPos pos, IBlockState state, Random rand)
		{
			return tickable.update(block, world, pos, state, rand);
		}
	}

}
