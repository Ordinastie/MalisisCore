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

import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.malisis.core.util.Point;
import net.malisis.core.util.chunkcollision.ChunkCollision;
import net.malisis.core.util.chunkcollision.IChunkCollidable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
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
		//When placing a block we need to check if it doesn't collide with a IChunkCollidable further away with a bigger bounding box
		@Inject(method = "onItemUse",
				at = @At(value = "INVOKE", target = "getMetadata"),
				locals = LocalCapture.CAPTURE_FAILSOFT,
				cancellable = true)
		private void onOnItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ, CallbackInfoReturnable<EnumActionResult> cir, IBlockState iblockstate, Block block, ItemStack itemstack)
		{
			if (!ChunkCollision.get().canPlaceBlockAt(player, world, block, pos, hand, side))
				cir.setReturnValue(EnumActionResult.FAIL);
		}
	}

	@Mixin(NetHandlerPlayServer.class)
	public static class MixinNetHandlerPlayServer
	{
		private World world;
		private BlockPos pos;

		@Shadow
		private MinecraftServer serverController;
		@Shadow
		public EntityPlayerMP player;

		@Inject(method = "processPlayerDigging", at = @At(value = "HEAD"))
		private void captureBlockPos(CPacketPlayerDigging packet, CallbackInfo ci)
		{
			pos = packet.getPosition();
			world = serverController.getWorld(this.player.dimension);
		}

		//Overrides reach distance check. Because the block currently being digged might be further away than expected
		//depending on the size of its bounding box if it's a IChunkCollidable
		@ModifyVariable(method = "processPlayerDigging",
						ordinal = 3, // double d3, using index = 10
						at = @At(value = "STORE", ordinal = 0), // first STORE of double d3
						require = 1)
		private double onPlayerDigging(double distance)
		{
			if (world.getBlockState(pos).getBlock() instanceof IChunkCollidable)
				return 0;
			return distance;
		}
	}

	@Mixin(World.class)
	public static class MixinWorld
	{
		private Pair<Point, Point> infos = null;

		//because rayTraceBlocks mutate the src of the raytrace while iterating, when need to cache the parameters first
		@Inject(method = "rayTraceBlocks(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;ZZZ)Lnet/minecraft/util/math/RayTraceResult;",
				at = @At(value = "HEAD"))
		private void setInfos(Vec3d src, Vec3d dest, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock, CallbackInfoReturnable<RayTraceResult> cir)
		{
			if (src != null && dest != null)
				infos = Pair.of(new Point(src), new Point(dest));
			else
				infos = null;
		}

		//before each return, we check if our rayTrace (against IChunkCollidable) yeild a result closer to src, if so, use that result
		@Inject(method = "rayTraceBlocks(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;ZZZ)Lnet/minecraft/util/math/RayTraceResult;",
				at = @At(value = "RETURN"),
				cancellable = true)
		private void onRayTraceBlocks(Vec3d src, Vec3d dest, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock, CallbackInfoReturnable<RayTraceResult> cir)
		{
			if (infos == null)
				return;

			RayTraceResult result = ChunkCollision.get().getRayTraceResult(	(World) (Object) this,
																			infos,
																			cir.getReturnValue(),
																			stopOnLiquid,
																			ignoreBlockWithoutBoundingBox,
																			returnLastUncollidableBlock);
			cir.setReturnValue(result);
		}
	}
}
