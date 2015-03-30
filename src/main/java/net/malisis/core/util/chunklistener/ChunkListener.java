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

package net.malisis.core.util.chunklistener;

import net.malisis.core.util.BlockPos;
import net.malisis.core.util.BlockState;
import net.malisis.core.util.chunkblock.ChunkBlockHandler;
import net.malisis.core.util.chunkblock.ChunkBlockHandler.ChunkProcedure;
import net.malisis.core.util.chunkblock.IChunkBlockHandler;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.chunk.Chunk;

/**
 * @author Ordinastie
 *
 */
public class ChunkListener implements IChunkBlockHandler
{
	@Override
	public boolean updateCoordinates(Chunk chunk, BlockPos pos, Block old, Block block)
	{
		BlockNotifierProcedure procedure = new BlockNotifierProcedure(new BlockState(pos, block));

		ChunkBlockHandler.get().callProcedure(chunk, procedure);

		if (procedure.isCanceled())
			return true;
		return false;
	}

	private static class BlockNotifierProcedure extends ChunkProcedure
	{
		private boolean cancel = false;
		private BlockState newState;

		public BlockNotifierProcedure(BlockState newState)
		{
			this.newState = newState;
		}

		public boolean isCanceled()
		{
			return cancel;
		}

		@Override
		public boolean execute(long coord)
		{
			if (!check(coord))
				return true;

			if (!(state.getBlock() instanceof IBlockListener))
				return true;

			IBlockListener block = (IBlockListener) state.getBlock();
			if (!state.getPos().isInRange(newState.getPos(), block.blockRange()))
				return true;

			if (!state.getPos().equals(newState.getPos()))
			{
				if (newState.getBlock() == Blocks.air)
					cancel |= !block.onBlockRemoved(world, state.getPos(), newState.getPos());
				else
					cancel |= !block.onBlockSet(world, state.getPos(), newState);
			}

			return !cancel;
		}

		@Override
		protected void clean()
		{
			super.clean();
			cancel = false;
			newState = null;
		}
	}
}
