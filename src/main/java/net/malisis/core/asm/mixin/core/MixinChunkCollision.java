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
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.malisis.core.util.chunkcollision.ChunkCollision;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * @author Ordinastie
 *
 */
public class MixinChunkCollision
{
	@Mixin(ItemBlock.class)
	public static class MixinItemBlock
	{
		@Inject(method = "onItemUse", at = @At(value = "INVOKE", target = "getMetadata"), locals = LocalCapture.CAPTURE_FAILSOFT)
		private void onOnItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ, CallbackInfoReturnable<EnumActionResult> cir, IBlockState iblockstate, Block block, ItemStack itemstack)
		{
			if (!ChunkCollision.get().canPlaceBlockAt(player, world, block, pos, hand, side))
				cir.setReturnValue(EnumActionResult.FAIL);
		}
	}

	@Mixin(NetHandlerPlayServer.class)
	public static class MixinNetHandlerPlayServer
	{
		//private World world;
		//private BlockPos pos;

		//		@Inject(method = "processPlayerDigging", at = @At(value = "STORE", ordinal = 4))
		//		private void captureBlockPos(CPacketPlayerDigging packet, CallbackInfo ci)
		//		{
		//			pos = packet.ge
		//		}

		//		@ModifyVariable(method = "processPlayerDigging", at = @At(value = "STORE", ordinal = 4), require = 1, print = true)
		//		private double onPlayerDigging(double distance)
		//		{
		//			//BlockPos pos = packet.getPosition();
		//			//World world = ((NetHandlerPlayServer) (Object) this).player.world;
		//
		//			//if (world.getBlockState(pos).getBlock() instanceof IChunkCollidable)
		//			//	return 0;
		//
		//			return distance;
		//		}
	}
}
