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

import java.util.List;

import net.malisis.core.util.callback.CallbackRegistry;
import net.malisis.core.util.callback.CallbackResult;
import net.malisis.core.util.callback.ICallback;
import net.malisis.core.util.callback.ICallback.ICallbackPredicate;
import net.malisis.core.util.chunkblock.ChunkCallbackRegistry.IChunkCallback;
import net.malisis.core.util.chunkblock.ChunkCallbackRegistry.IChunkCallbackPredicate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

/**
 * The {@link ChunkCallbackRegistry} handles {@link IChunkCallback IChunkCallbacks}.<br>
 * {@code IChunkCallbacks} are called for every {@link IChunkBlock} stored in the chunk data.
 *
 * @author Ordinastie
 */
public class ChunkCallbackRegistry<C extends IChunkCallback<V>, P extends IChunkCallbackPredicate, V> extends CallbackRegistry<C, P, V>
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
	public CallbackResult<V> processCallbacks(Chunk chunk, Object... params)
	{
		//true = cancel => return
		return ChunkBlockHandler.get()
								.getCoords(chunk)
								.map(list -> processListeners(list, chunk, params))
								.orElse(CallbackResult.noResult());

	}

	/**
	 * Processes the {@link IChunkCallback IChunkCallbacks} registered for the list of {@link BlockPos}.
	 *
	 * @param list the list
	 * @param chunk the chunk
	 * @param params the params
	 * @return the callback result
	 */
	private CallbackResult<V> processListeners(List<BlockPos> list, Chunk chunk, Object... params)
	{
		CallbackResult<V> result = CallbackResult.noResult();
		for (BlockPos listener : list)
		{
			CallbackResult<V> tmp = super.processCallbacks(chunk, listener, params);
			result = reduce.apply(result, tmp);
			if (result.isForcedCancelled())
				return result;
		}

		return result;

	}

	/**
	 * Specialized {@link ICallback} for {@link ChunkCallbackRegistry}.<br>
	 * If the {@link CallbackResult#isForcedCancelled()} is {@code true}, the next listener positions won't be processed.
	 */
	public interface IChunkCallback<V> extends ICallback<V>
	{
		@Override
		public default CallbackResult<V> call(Object... params)
		{
			return call((Chunk) params[0], (BlockPos) params[1], (Object[]) params[2]);
		}

		public CallbackResult<V> call(Chunk chunk, BlockPos listener, Object... params);
	}

	/**
	 * Specialized {@link IChunkCallbackPredicate} for {@link IChunkCallback}.
	 */
	@FunctionalInterface
	public interface IChunkCallbackPredicate extends ICallbackPredicate
	{
		@Override
		public default boolean apply(Object... params)
		{
			return apply((Chunk) params[0], (BlockPos) params[1], (Object[]) params[2]);
		}

		public boolean apply(Chunk chunk, BlockPos listener, Object... params);
	}
}
