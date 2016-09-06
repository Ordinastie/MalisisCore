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

package net.malisis.core.util.chunkblock;

import java.util.Optional;

import net.malisis.core.util.callback.ASMCallbackRegistry.CallbackResult;
import net.malisis.core.util.callback.CallbackRegistry;
import net.malisis.core.util.callback.ICallback;
import net.malisis.core.util.callback.ICallback.CallbackOption;
import net.malisis.core.util.callback.ICallback.ICallbackPredicate;
import net.malisis.core.util.callback.ICallback.Priority;
import net.malisis.core.util.chunkblock.ChunkCallbackRegistry.IChunkCallback;
import net.malisis.core.util.chunkblock.ChunkCallbackRegistry.IChunkCallbackPredicate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

import org.apache.commons.lang3.tuple.Pair;

/**
 * The {@link ChunkCallbackRegistry} handles {@link IChunkCallback IChunkCallbacks}.<br>
 * {@code IChunkCallbacks} are called for every {@link IChunkBlock} stored in the chunk data.
 *
 * @author Ordinastie
 */
public class ChunkCallbackRegistry<C extends IChunkCallback, P extends IChunkCallbackPredicate> extends CallbackRegistry<C, P, Boolean>
{
	/**
	 * Processes the {@link IChunkCallback IChunkCallbacks} registered.
	 *
	 * @param chunk the chunk
	 * @param pos the pos
	 * @param oldState the old state
	 * @param newState the new state
	 * @return the callback result
	 */
	public CallbackResult<Boolean> processCallbacks(Chunk chunk, Object... params)
	{
		Optional<Boolean> ret = ChunkBlockHandler.get().getCoords(chunk).map(list -> {
			for (BlockPos listener : list)
				if (processCallbacksForPosition(chunk, listener, params)) //should we stop process positions if one cancels ?
					return true;
			return false;
		});

		return CallbackResult.of(ret.orElse(true), false);
	}

	/**
	 * Process the {@link IChunkCallback} registered the specified position.
	 *
	 * @param params the params
	 * @return true, if successful
	 */
	private boolean processCallbacksForPosition(Chunk chunk, BlockPos listner, Object... params)
	{
		if (callbacks.size() == 0)
			return false;

		boolean cancel = false;
		Priority currentPriority = Priority.HIGHEST;
		for (Pair<C, CallbackOption<P>> pair : callbacks)
		{
			//don't process lower priority if cancelled
			if (cancel && pair.getRight().getPriority() != currentPriority)
				break;
			currentPriority = pair.getRight().getPriority();
			if (pair.getRight().apply(chunk, listner, params))
				cancel |= pair.getLeft().call(chunk, listner, params);
		}

		return cancel;
	}

	/**
	 * Specialized {@link ICallback} for {@link ChunkCallbackRegistry}.
	 */
	public interface IChunkCallback extends ICallback<Boolean>
	{
		@Override
		public default Boolean call(Object... params)
		{
			//should never be called
			throw new IllegalStateException();
			//return call((Chunk) params[0], (BlockPos) params[1], (BlockPos) params[2], (IBlockState) params[3], (IBlockState) params[4]);
		}

		public boolean call(Chunk chunk, BlockPos listener, Object... params);
	}

	/**
	 * Specialized {@link IChunkCallbackPredicate} for {@link IChunkCallback}.
	 */
	public interface IChunkCallbackPredicate extends ICallbackPredicate
	{
		@Override
		public default boolean apply(Object... params)
		{
			//should never be called
			throw new IllegalStateException();
			//return apply((Chunk) params[0], (BlockPos) params[1], (BlockPos) params[2], (IBlockState) params[3], (IBlockState) params[4]);
		}

		public boolean apply(Chunk chunk, BlockPos listener, Object... params);
	}
}
