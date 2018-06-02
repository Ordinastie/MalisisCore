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

package net.malisis.core.mixin.core.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.malisis.core.block.IComponent;
import net.malisis.core.registry.Registries;
import net.malisis.core.renderer.icon.provider.IIconProvider;
import net.malisis.core.util.callback.CallbackResult;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * @author Ordinastie
 *
 */
public class MixinMalisisRenderer
{

	@Mixin(BlockRendererDispatcher.class)
	public static abstract class MixinBlockRendererDispatcher
	{
		@Inject(method = "renderBlock",
				at = @At(	value = "INVOKE",
							target = "Lnet/minecraft/block/state/IBlockState;getRenderType()Lnet/minecraft/util/EnumBlockRenderType;"),
				cancellable = true,
				locals = LocalCapture.CAPTURE_FAILSOFT)
		private void onRenderBlock(IBlockState state, BlockPos pos, IBlockAccess world, BufferBuilder buffer, CallbackInfoReturnable<Boolean> cir)
		{
			CallbackResult<Boolean> cb = Registries.processRenderBlockCallbacks(buffer, world, pos, state);
			if (cb.shouldReturn())
				cir.setReturnValue(cb.getValue());
		}
	}

	@Mixin(RenderItem.class)
	public static abstract class MixinRenderItem
	{
		@Inject(method = "renderModel(Lnet/minecraft/client/renderer/block/model/IBakedModel;ILnet/minecraft/item/ItemStack;)V",
				at = @At("HEAD"),
				cancellable = true)
		private void onRenderModel(IBakedModel model, int color, ItemStack itemStack, CallbackInfo ci)
		{
			if (Registries.renderItem(itemStack))
				ci.cancel();
		}

	}

	@Mixin(BlockModelShapes.class)
	public static abstract class MixinBlockModelShapes
	{
		@Inject(method = "getTexture", at = @At("HEAD"), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
		private void onGetTexture(IBlockState state, CallbackInfoReturnable<TextureAtlasSprite> cir)
		{
			if (IComponent.getComponent(IIconProvider.class, state.getBlock()) != null)
				cir.setReturnValue(Registries.getParticleIcon(state));
		}
	}
}
