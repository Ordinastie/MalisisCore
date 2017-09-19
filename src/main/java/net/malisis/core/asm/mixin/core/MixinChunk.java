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

package net.malisis.core.asm.mixin.core;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.malisis.core.registry.Registries;
import net.malisis.core.util.callback.CallbackResult;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

/**
 * @author Ordinastie
 *
 */
@Mixin(Chunk.class)
public class MixinChunk
{
	@Inject(method = "setBlockState",
			at = @At(	value = "INVOKE",
						target = "Lnet/minecraft/world/chunk/storage/ExtendedBlockStorage;set(IIILnet/minecraft/block/state/IBlockState;)V"),
			locals = LocalCapture.CAPTURE_FAILSOFT,
			cancellable = true)
	private void preSetBlock(BlockPos pos, IBlockState state, CallbackInfoReturnable<IBlockState> cir, int i, int j, int k, int l, int i1, IBlockState iblockstate, Block block, Block block1, int k1, ExtendedBlockStorage extendedblockstorage, boolean flag)
	{
		CallbackResult<Void> cb = Registries.processPreSetBlock((Chunk) (Object) this, pos, iblockstate, state);
		if (cb.shouldReturn())
			cir.cancel();
	}

	@Inject(method = "setBlockState",
			at = @At(	value = "INVOKE",
						target = "Lnet/minecraft/world/chunk/storage/ExtendedBlockStorage;set(IIILnet/minecraft/block/state/IBlockState;)V",
						shift = Shift.AFTER),
			locals = LocalCapture.CAPTURE_FAILSOFT)
	private void postSetBlock(BlockPos pos, IBlockState oldState, CallbackInfoReturnable<IBlockState> cir, int i, int j, int k, int l, int i1, IBlockState newState, Block block, Block block1, int k1, ExtendedBlockStorage extendedblockstorage, boolean flag)
	{
		Registries.processPostSetBlock((Chunk) (Object) this, pos, oldState, newState);
	}
}
