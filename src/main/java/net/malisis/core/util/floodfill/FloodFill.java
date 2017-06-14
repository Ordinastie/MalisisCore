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

package net.malisis.core.util.floodfill;

import static com.google.common.base.Preconditions.*;

import java.util.ArrayDeque;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

import com.google.common.collect.ImmutableSet;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * The FloodFill tool gives a customizable way to perform flood fill algorithm in the world.<br>
 * A custom predicate can be provided to check if whether a position should be processed. And a consumer can also be used for each processed
 * position if needed.
 *
 * @author Ordinastie
 */
public class FloodFill
{
	/** {@link World} to perform the flood fill. */
	protected World world;
	/** Starting position for the flood fill. */
	protected BlockPos origin;
	/** {@link IBlockState} of the origin position . */
	protected IBlockState originState;

	/** Predicate to check whether a position should be processed. */
	protected BiPredicate<World, BlockPos> shouldProcess;
	/** Function to execute for each position processed. */
	protected BiConsumer<World, BlockPos> onProcess;
	/** Directions to use when expending the positions to be processed. */
	protected EnumSet<EnumFacing> searchDirs;
	/** Maximum number of position to process. */
	protected int countLimit;

	/** List of positions already processed. */
	protected Set<BlockPos> processed = new HashSet<>();
	/** List of position that has yet to be processed. */
	protected ArrayDeque<BlockPos> toProcess = new ArrayDeque<>();

	/**
	 * Instantiates a new {@link FloodFill}.
	 *
	 * @param world the world
	 * @param origin the origin
	 * @param shouldProcess the should parse
	 * @param onProcess the on parse
	 * @param searchDirs the search dirs
	 * @param countLimit the count limit
	 */
	protected FloodFill(World world, BlockPos origin, BiPredicate<World, BlockPos> shouldProcess, BiConsumer<World, BlockPos> onProcess, EnumSet<EnumFacing> searchDirs, int countLimit)
	{
		this.world = world;
		this.origin = origin;
		this.originState = world.getBlockState(origin);
		this.shouldProcess = shouldProcess;
		this.onProcess = onProcess;
		this.searchDirs = searchDirs;
		this.countLimit = countLimit;

		toProcess.add(origin);
	}

	/**
	 * Gets the stating position of this {@link FloodFill}.
	 *
	 * @return the origin
	 */
	public BlockPos getOrigin()
	{
		return origin;
	}

	/**
	 * Gets the origin {@link IBlockState} for this {@link FloodFill}.
	 *
	 * @return the origin state
	 */
	public IBlockState getOriginState()
	{
		return originState;
	}

	/**
	 * Gets the list of {@link BlockPos} already processed.
	 *
	 * @return the parsed
	 */
	public Set<BlockPos> getProcessed()
	{
		return processed;
		//return ImmutableSet.copyOf(parsed);
	}

	public Set<BlockPos> getToProcess()
	{
		return ImmutableSet.copyOf(toProcess);
	}

	/**
	 * Performs the algorithm completely, until no more position is left to process, or until {@link #countLimit} is reached.
	 */
	public void processAll()
	{
		while (process());
	}

	/**
	 * Processes all currently known positions to be processed, or until {@link #countLimit} is reached.
	 *
	 * @return true, if there are more position to process
	 */
	public boolean processStep()
	{
		return process(toProcess.size());
	}

	/**
	 * Processes <code>maxCount</code> positions, or until
	 *
	 * @param maxCount the max count
	 * @return true, if there are more position to process
	 */
	public boolean process(int maxCount)
	{
		while (maxCount-- > 0 && process());
		return toProcess.size() != 0;
	}

	/**
	 * Processes a single position.
	 *
	 * @return true, if there are more position to process
	 */
	public boolean process()
	{
		if (toProcess.size() <= 0)
			return false;

		BlockPos pos = toProcess.removeFirst();
		process(pos);
		if (onProcess != null)
			onProcess.accept(world, pos);

		return true;
	}

	/**
	 * Checks whether the position should be processed.
	 *
	 * @param pos the pos
	 * @return true, if successful
	 */
	protected boolean shouldProcessed(BlockPos pos)
	{
		if (toProcess.contains(pos) || processed.contains(pos))
			return false;

		return shouldProcess == null || shouldProcess.test(world, pos);
	}

	/**
	 * Processes the position.
	 *
	 * @param pos the pos
	 */
	protected void process(BlockPos pos)
	{
		for (EnumFacing dir : searchDirs)
		{
			BlockPos newPos = pos.offset(dir);
			if (!processed.contains(newPos) && shouldProcessed(newPos))
				toProcess.add(newPos);
		}
		processed.add(pos);
		if (processed.size() >= countLimit)
			toProcess.clear();
	}

	/**
	 * Creates the {@link FloodFillBuilder} for a {@link FloodFill}.
	 *
	 * @param world the world
	 * @return the flood fill builder
	 */
	public static FloodFillBuilder builder(World world)
	{
		return new FloodFillBuilder(world);
	}

	/**
	 * Builder for {@link FloodFill}.
	 */
	public static class FloodFillBuilder
	{
		/** {@link World} to perform the flood fill. */
		protected World world;
		/** Starting position for the flood fill. */
		protected BlockPos origin;

		/** Predicate to check whether a position should be processed. */
		protected BiPredicate<World, BlockPos> shouldProcess = null;
		/** Function to execute for each position processed. */
		protected BiConsumer<World, BlockPos> onProcess = null;
		/** Directions to use when expending the positions to be processed. */
		protected EnumSet<EnumFacing> searchDirs = EnumSet.allOf(EnumFacing.class);
		/** Maximum number of position to process. */
		protected int countLimit = Integer.MAX_VALUE;

		private FloodFillBuilder(World world)
		{
			this.world = world;
		}

		private BiPredicate<World, BlockPos> compose(BiPredicate<World, BlockPos> predicate)
		{
			return shouldProcess == null ? predicate : shouldProcess.and(predicate);
		}

		/**
		 * Sets the starting position for the {@link FloodFill}.
		 *
		 * @param origin the origin
		 * @return the flood fill builder
		 */
		public FloodFillBuilder from(BlockPos origin)
		{
			this.origin = checkNotNull(origin);
			return this;
		}

		/**
		 * Tells the {@link FloodFill} to process only states matching the origin one.
		 *
		 * @return the flood fill builder
		 */
		public FloodFillBuilder matchesOriginState()
		{
			IBlockState originState = world.getBlockState(origin);
			shouldProcess = compose((w, p) -> w.getBlockState(p) == originState);
			return this;
		}

		/**
		 * Tells the {@link FloodFill} to process only blocks matching the origin one. (Regardless of the IBlockState).
		 *
		 * @return the flood fill builder
		 */
		public FloodFillBuilder matchesOriginBlock()
		{
			Block originBlock = world.getBlockState(origin).getBlock();
			shouldProcess = compose((w, p) -> w.getBlockState(p).getBlock() == originBlock);
			return this;
		}

		/**
		 * Limits distance from the origin of positions to process.
		 *
		 * @param distance the distance
		 * @return the flood fill builder
		 */
		public FloodFillBuilder limitDistance(float distance)
		{
			shouldProcess = compose((w, p) -> p.distanceSq(origin) < distance * distance);
			return this;
		}

		/**
		 * Limit the number of position to process.
		 *
		 * @param count the count
		 * @return the flood fill builder
		 */
		public FloodFillBuilder limitCount(int count)
		{
			this.countLimit = count;
			return this;
		}

		/**
		 * Sets a custom predicate to check if a position should be processed.
		 *
		 * @param predicate the predicate
		 * @return the flood fill builder
		 */
		public FloodFillBuilder processIf(BiPredicate<World, BlockPos> predicate)
		{
			this.shouldProcess = checkNotNull(predicate);
			return this;
		}

		/**
		 * Sets a callback to be used for each processed position.
		 *
		 * @param onParse the on parse
		 * @return the flood fill builder
		 */
		public FloodFillBuilder onProcess(BiConsumer<World, BlockPos> onParse)
		{
			this.onProcess = checkNotNull(onParse);
			return this;
		}

		public FloodFillBuilder forDirections(EnumFacing... directions)
		{
			searchDirs = EnumSet.noneOf(EnumFacing.class);
			for (EnumFacing dir : checkNotNull(directions))
				searchDirs.add(checkNotNull(dir));
			return this;
		}

		/**
		 * Creates the {@link FloodFill} object.
		 *
		 * @return the flood fill
		 */
		public FloodFill build()
		{
			return new FloodFill(world, origin, shouldProcess, onProcess, searchDirs, countLimit);
		}
	}
}
