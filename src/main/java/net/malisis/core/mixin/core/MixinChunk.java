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

package net.malisis.core.mixin.core;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.malisis.core.registry.Registries;
import net.malisis.core.util.callback.CallbackResult;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

/**
 * @author Ordinastie
 *
 */
@Mixin(value = Chunk.class, priority = 1001)
public class MixinChunk
{
	private IBlockState oldState;

	@Inject(method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Lnet/minecraft/block/state/IBlockState;",
			at = @At("HEAD"),
			cancellable = true)
	private void preSetBlock(BlockPos pos, IBlockState newState, CallbackInfoReturnable<IBlockState> cir)
	{
		Chunk chunk = (Chunk) (Object) this;
		oldState = chunk.getBlockState(pos);
		CallbackResult<Void> cb = Registries.processPreSetBlock(chunk, pos, oldState, newState);
		if (cb.shouldReturn())
			cir.cancel();
	}

	@Inject(method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Lnet/minecraft/block/state/IBlockState;",
			at = @At("TAIL"))
	private void postSetBlock(BlockPos pos, IBlockState newState, CallbackInfoReturnable<IBlockState> cir)
	{
		Registries.processPostSetBlock((Chunk) (Object) this, pos, oldState, newState);
	}
}
