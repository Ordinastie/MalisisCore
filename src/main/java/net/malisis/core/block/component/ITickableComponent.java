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
import net.malisis.core.block.MalisisBlock;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * ITickableComponent is used for as callbacks for {@link PeriodicTickableComponent} and {@link RandomTickableComponent}.
 *
 * @author Ordinastie
 */
public interface ITickableComponent
{
	/**
	 * Called when the block updates the component.
	 *
	 * @param block the block
	 * @param world the world
	 * @param pos the pos
	 * @param state the state
	 * @param rand the rand
	 * @return the delay before the next tick, 0 to stop updates (unused for {@link RandomTickableComponent}).
	 */
	public int update(Block block, World world, BlockPos pos, IBlockState state, Random rand);

	/**
	 * RandomTickableComponent when added to {@link MalisisBlock} allows the block to tick randomly.
	 */
	public class RandomTickableComponent implements IBlockComponent
	{
		/** The tickable callback used by this {@link RandomTickableComponent}. */
		private final ITickableComponent tickable;

		/**
		 * Instantiates a new {@link RandomTickableComponent}.
		 *
		 * @param tickable the tickable
		 */
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

		/**
		 * Updates this component and calls the {@link ITickableComponent} callback.<br>
		 * Called from {@link MalisisBlock#randomTick(World, BlockPos, IBlockState, Random)}.
		 *
		 * @param block the block
		 * @param world the world
		 * @param pos the pos
		 * @param state the state
		 * @param rand the rand
		 */
		public void update(Block block, World world, BlockPos pos, IBlockState state, Random rand)
		{
			tickable.update(block, world, pos, state, rand);
		}
	}

	/**
	 * PeriodicTickableComponent when added to {@link MalisisBlock} allows the block to tick periodically.
	 */
	public class PeriodicTickableComponent implements IBlockComponent
	{
		/** The tickable callback used by this {@link PeriodicTickableComponent}. */
		private final ITickableComponent tickable;
		/** Delay before the first tick. */
		private final int firstTickDelay;

		/**
		 * Instantiates a new {@link PeriodicTickableComponent} with the speicified delay before the first tick.
		 *
		 * @param tickable the tickable
		 * @param firstTickDelay delay before the first tick, 0 to not start ticking automatically.
		 */
		public PeriodicTickableComponent(ITickableComponent tickable, int firstTickDelay)
		{
			this.tickable = tickable;
			this.firstTickDelay = firstTickDelay;
		}

		/**
		 * Instantiates a new {@link PeriodicTickableComponent} that will not start ticking automatically.
		 *
		 * @param tickable the tickable
		 */
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

		/**
		 * Updates this component and calls the {@link ITickableComponent} callback.<br>
		 * Called from {@link MalisisBlock#randomTick(World, BlockPos, IBlockState, Random)}.<br>
		 * Return value sets the delay before the next tick, or 0 to stop ticking.
		 *
		 * @param block the block
		 * @param world the world
		 * @param pos the pos
		 * @param state the state
		 * @param rand the rand
		 * @return the delay before the next tick, 0 to stop updates.
		 */
		public int update(Block block, World world, BlockPos pos, IBlockState state, Random rand)
		{
			return tickable.update(block, world, pos, state, rand);
		}
	}

}
