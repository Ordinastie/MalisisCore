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

import net.malisis.core.util.chunkblock.IChunkBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Defines a block that will listen to block placement and breaking around it.
 *
 * @author Ordinastie
 *
 */
public final class IBlockListener
{
	private IBlockListener()
	{}

	public static interface Pre extends IChunkBlock
	{
		/**
		 * Called when a block is placed around this {@link IBlockListener} block.<br>
		 * The return value defines whether the block placement should be canceled.
		 *
		 * @param world the world
		 * @param listener the listener
		 * @param modified the modified
		 * @param oldState the old state
		 * @param newState the new state
		 * @return true, if the block is allowed to be placed, false to cancel block placement
		 */
		public boolean onBlockSet(World world, BlockPos listener, BlockPos modified, IBlockState oldState, IBlockState newState);
	}

	public static interface Post extends IChunkBlock
	{
		/**
		 * Called after a block is placed around this {@link IBlockListener} block.
		 *
		 * @param world the world
		 * @param listener the listener
		 * @param modified the modified
		 * @param oldState the old state
		 * @param newState the new state
		 */
		public void onBlockSet(World world, BlockPos listener, BlockPos modified, IBlockState oldState, IBlockState newState);
	}
}
