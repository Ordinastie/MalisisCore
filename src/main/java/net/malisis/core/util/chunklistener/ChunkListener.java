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

import net.malisis.core.block.IComponent;
import net.malisis.core.registry.AutoLoad;
import net.malisis.core.registry.MalisisRegistry;
import net.malisis.core.util.callback.CallbackResult;
import net.malisis.core.util.callback.ICallback;
import net.malisis.core.util.callback.ICallback.CallbackOption;
import net.malisis.core.util.chunkblock.ChunkCallbackRegistry;
import net.malisis.core.util.chunkblock.ChunkCallbackRegistry.IChunkCallback;
import net.malisis.core.util.chunkblock.ChunkCallbackRegistry.IChunkCallbackPredicate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

/**
 * This {@link ChunkListener} class handles the {@link IBlockListener} components for blocks.
 *
 * @author Ordinastie
 */
@AutoLoad(true)
public class ChunkListener
{
	private static final CallbackResult<Void> CANCELLED = CallbackResult.<Void> builder().forceCancel(true).withReturn(true).result();
	private static ChunkCallbackRegistry<IBlockListenerCallback, IBlockListenerPredicate, Void> preRegistry = new ChunkCallbackRegistry<>();
	private static ChunkCallbackRegistry<IBlockListenerCallback, IBlockListenerPredicate, Void> postRegistry = new ChunkCallbackRegistry<>();

	public ChunkListener()
	{
		MalisisRegistry.onPreSetBlock(preRegistry::processCallbacks, CallbackOption.of());
		MalisisRegistry.onPreSetBlock(postRegistry::processCallbacks, CallbackOption.of());
		preRegistry.registerCallback(this::callPreListener, CallbackOption.of(this::isValidPreListener));
		postRegistry.registerCallback(this::callPostListener, CallbackOption.of(this::isValidPostListener));
	}

	/**
	 * Calls {@link IBlockListener.Pre#onBlockSet(net.minecraft.world.World, BlockPos, BlockPos, IBlockState, IBlockState)} for the listener
	 * {@link BlockPos}.
	 *
	 * @param chunk the chunk
	 * @param listener the listener
	 * @param modified the modified
	 * @param oldState the old state
	 * @param newState the new state
	 * @return true, if successful
	 */
	public boolean callPreListener(Chunk chunk, BlockPos listener, BlockPos modified, IBlockState oldState, IBlockState newState)
	{
		IBlockListener.Pre bl = IComponent.getComponent(IBlockListener.Pre.class, chunk.getWorld().getBlockState(listener).getBlock());
		return bl.onBlockSet(chunk.getWorld(), listener, modified, oldState, newState);
	}

	/**
	 * Checks if the listener {@link BlockPos} has a {@link IBlockListener.Pre} component and if the modified {@code BlockPos} is in range.
	 *
	 * @param chunk the chunk
	 * @param listener the listener
	 * @param modified the modified
	 * @param oldState the old state
	 * @param newState the new state
	 * @return true, if is valid pre listener
	 */
	public boolean isValidPreListener(Chunk chunk, BlockPos listener, BlockPos modified, IBlockState oldState, IBlockState newState)
	{
		if (listener.equals(modified))
			return false;
		IBlockListener.Pre bl = IComponent.getComponent(IBlockListener.Pre.class, chunk.getWorld().getBlockState(listener).getBlock());
		if (bl != null && bl.isInRange(listener, modified))
			return true;

		return false;
	}

	/**
	 * Calls IBlockListener.Pre#onBlockSet(World, BlockPos, BlockPos, IBlockState, IBlockState) for the listener {@link BlockPos}.
	 *
	 * @param chunk the chunk
	 * @param listener the listener
	 * @param modified the modified
	 * @param oldState the old state
	 * @param newState the new state
	 * @return true, if successful
	 */
	public boolean callPostListener(Chunk chunk, BlockPos listener, BlockPos modified, IBlockState oldState, IBlockState newState)
	{
		IBlockListener.Post bl = IComponent.getComponent(IBlockListener.Post.class, chunk.getWorld().getBlockState(listener).getBlock());
		bl.onBlockSet(chunk.getWorld(), listener, modified, oldState, newState);
		return true;
	}

	/**
	 * Checks if the listener {@link BlockPos} has a {@link IBlockListener.Pre} component and if the modified {@code BlockPos} is in range.
	 *
	 * @param chunk the chunk
	 * @param listener the listener
	 * @param modified the modified
	 * @param oldState the old state
	 * @param newState the new state
	 * @return true, if is valid post listener
	 */
	public boolean isValidPostListener(Chunk chunk, BlockPos listener, BlockPos modified, IBlockState oldState, IBlockState newState)
	{
		if (listener.equals(modified))
			return false;
		IBlockListener.Post bl = IComponent.getComponent(IBlockListener.Post.class, chunk.getWorld().getBlockState(listener).getBlock());
		if (bl != null && bl.isInRange(listener, modified))
			return true;

		return false;
	}

	/**
	 * Specialized {@link ICallback} for {@link ChunkCallbackRegistry}.
	 */
	public interface IBlockListenerCallback extends IChunkCallback<Void>
	{
		@Override
		public default CallbackResult<Void> call(Chunk chunk, BlockPos listener, Object... params)
		{
			return call(chunk, listener, (BlockPos) params[0], (IBlockState) params[1], (IBlockState) params[2]) ? CallbackResult.noResult() : CANCELLED;
		}

		/**
		 * If this {@link IBlockListenerCallback} was registered with
		 * {@link MalisisRegistry#onPreSetBlock(net.malisis.core.registry.SetBlockCallbackRegistry.ISetBlockCallback, CallbackOption)},
		 * returns whether to cancel the block placement or not.
		 *
		 * @param chunk the chunk
		 * @param listener the listener
		 * @param modified the modified
		 * @param oldState the old state
		 * @param newState the new state
		 * @return true, if the block should be cancelled
		 */
		public boolean call(Chunk chunk, BlockPos listener, BlockPos modified, IBlockState oldState, IBlockState newState);
	}

	/**
	 * Specialized {@link IChunkCallbackPredicate} for {@link IBlockListenerCallback}.
	 */
	public interface IBlockListenerPredicate extends IChunkCallbackPredicate
	{
		@Override
		public default boolean apply(Chunk chunk, BlockPos listener, Object... params)
		{
			return apply(chunk, listener, (BlockPos) params[0], (IBlockState) params[1], (IBlockState) params[2]);
		}

		public boolean apply(Chunk chunk, BlockPos listener, BlockPos modified, IBlockState oldState, IBlockState newState);
	}
}
