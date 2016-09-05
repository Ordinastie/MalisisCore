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

package net.malisis.core.registry;

import net.malisis.core.registry.RenderBlockRegistry.IRenderBlockCallback;
import net.malisis.core.registry.RenderBlockRegistry.IRenderBlockCallbackPredicate;
import net.malisis.core.util.callback.ASMCallbackRegistry;
import net.malisis.core.util.callback.ASMCallbackRegistry.IASMCallback;
import net.malisis.core.util.callback.ICallback;
import net.malisis.core.util.callback.ICallback.ICallbackPredicate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * @author Ordinastie
 *
 */
public class RenderBlockRegistry extends ASMCallbackRegistry<IRenderBlockCallback, IRenderBlockCallbackPredicate, Boolean>
{

	/**
	 * Specialized {@link ICallback} called when a block is rendered.
	 */
	public interface IRenderBlockCallback extends IASMCallback<Boolean>
	{
		@Override
		public default CallbackResult<Boolean> call(Object... params)
		{
			return callback((VertexBuffer) params[0], (IBlockAccess) params[1], (BlockPos) params[2], (IBlockState) params[3]);
		}

		public CallbackResult<Boolean> callback(VertexBuffer buffer, IBlockAccess world, BlockPos pos, IBlockState state);
	}

	/**
	 * Specialized {@link ICallbackPredicate} for {@link IRenderBlockCallback IRenderBlockCallbacks}
	 */
	public interface IRenderBlockCallbackPredicate extends ICallbackPredicate
	{
		@Override
		public default boolean apply(Object... params)
		{
			return apply((VertexBuffer) params[0], (IBlockAccess) params[1], (BlockPos) params[2], (IBlockState) params[3]);
		}

		public boolean apply(VertexBuffer buffer, IBlockAccess world, BlockPos pos, IBlockState state);
	}
}
