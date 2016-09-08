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

package net.malisis.core.util.chunkcollision;

import java.util.List;

import net.malisis.core.block.IComponent;
import net.malisis.core.block.component.DirectionalComponent;
import net.malisis.core.registry.AutoLoad;
import net.malisis.core.util.AABBUtils;
import net.malisis.core.util.BlockPosUtils;
import net.malisis.core.util.ItemUtils;
import net.malisis.core.util.MBlockState;
import net.malisis.core.util.Point;
import net.malisis.core.util.callback.CallbackResult;
import net.malisis.core.util.callback.ICallback.CallbackOption;
import net.malisis.core.util.chunkblock.ChunkBlockHandler;
import net.malisis.core.util.chunkblock.ChunkCallbackRegistry;
import net.malisis.core.util.chunkblock.ChunkCallbackRegistry.IChunkCallback;
import net.malisis.core.util.chunkblock.ChunkCallbackRegistry.IChunkCallbackPredicate;
import net.malisis.core.util.raytrace.Raytrace;
import net.malisis.core.util.raytrace.RaytraceBlock;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import org.apache.commons.lang3.ArrayUtils;

/**
 * This class is the entry point for all the chunk collision related calculation.<br>
 * The static methods are called via ASM which then call the process for the corresponding server or client instance.
 *
 * @author Ordinastie
 *
 */
@AutoLoad
public class ChunkCollision
{
	private static ChunkCollision instance = new ChunkCollision();

	private ChunkCallbackRegistry<IChunkCallback<Void>, IChunkCallbackPredicate, Void> collisionRegistry = new ChunkCallbackRegistry<>();
	private ChunkCallbackRegistry<IChunkCallback<RayTraceResult>, IChunkCallbackPredicate, RayTraceResult> rayTraceRegistry = new ChunkCallbackRegistry<>();
	private ChunkCallbackRegistry<IChunkCallback<Boolean>, IChunkCallbackPredicate, Boolean> placeAtRegistry = new ChunkCallbackRegistry<>();

	public ChunkCollision()
	{
		collisionRegistry.registerCallback(this::collisionBoxesCallback,
				CallbackOption.of((IChunkCallbackPredicate) this::isChunkCollidable));
		rayTraceRegistry.registerCallback(this::rayTraceCallback, CallbackOption.of((IChunkCallbackPredicate) this::isChunkCollidable));
		placeAtRegistry.registerCallback(this::placeAtCallback, CallbackOption.of((IChunkCallbackPredicate) this::isChunkCollidable));
	}

	public boolean isChunkCollidable(Chunk chunk, BlockPos listener, Object... params)
	{
		return IComponent.getComponent(IChunkCollidable.class, chunk.getWorld().getBlockState(listener).getBlock()) != null;
	}

	//#region getCollisionBoundinBoxes
	/**
	 * Gets the collision bounding boxes for the intersecting chunks.<br>
	 * Called via ASM from {@link World#getCollisionBoxes(Entity, AxisAlignedBB)}
	 *
	 * @param world the world
	 * @param mask the mask
	 * @param list the list
	 * @param entity the entity
	 */
	public void getCollisionBoxes(World world, AxisAlignedBB mask, List<AxisAlignedBB> list, Entity entity)
	{
		//no mask, no need to check for collision
		if (mask == null)
			return;

		for (Chunk chunk : ChunkBlockHandler.getAffectedChunks(world, mask))
			collisionRegistry.processCallbacks(chunk, mask, list);
	}

	private CallbackResult<Void> collisionBoxesCallback(Chunk chunk, BlockPos listener, Object... params)
	{

		IBlockState state = chunk.getBlockState(listener);
		IChunkCollidable cc = IComponent.getComponent(IChunkCollidable.class, state.getBlock());
		AxisAlignedBB mask = (AxisAlignedBB) params[0];
		@SuppressWarnings("unchecked")
		List<AxisAlignedBB> list = (List<AxisAlignedBB>) params[1];

		AxisAlignedBB[] aabbs = cc.getCollisionBoundingBoxes(chunk.getWorld(), listener, state);
		for (AxisAlignedBB aabb : aabbs)
		{
			if (aabb != null)
			{
				aabb = AABBUtils.offset(listener, aabb);
				if (mask.intersectsWith(aabb))
					list.add(aabb);
			}
		}

		return CallbackResult.noResult();
	}

	//#end getCollisionBoundinBoxes

	//#region getRayTraceResult

	private Point src;
	private Point dest;

	/**
	 * Sets the ray trace infos.<br>
	 * Called via ASM at the beginning of {@link World#rayTraceBlocks(Vec3d, Vec3d, boolean, boolean, boolean)}
	 *
	 * @param src the src
	 * @param dest the dest
	 */
	public void setRayTraceInfos(Vec3d src, Vec3d dest)
	{
		if (src == null || dest == null)
			return;
		setRayTraceInfos(new Point(src), new Point(dest));
	}

	/**
	 * Sets the ray trace infos.
	 *
	 * @param src the src
	 * @param dest the dest
	 */
	public void setRayTraceInfos(Point src, Point dest)
	{
		this.src = src;
		this.dest = dest;
	}

	/**
	 * Gets the ray trace result.<br>
	 * Called via ASM from {@link World#rayTraceBlocks(Vec3d, Vec3d, boolean, boolean, boolean)} before each return.
	 *
	 * @param world the world
	 * @param result the mop
	 * @return the ray trace result
	 */
	public RayTraceResult getRayTraceResult(World world, RayTraceResult result)
	{
		if (src == null || dest == null)
			return result;

		//TODO: use chunks actually traversed by the  raytrace vector
		AxisAlignedBB aabb = new AxisAlignedBB(src.x, src.y, src.z, dest.x, dest.y, dest.z);
		for (Chunk chunk : ChunkBlockHandler.getAffectedChunks(world, aabb))
		{
			CallbackResult<RayTraceResult> tmp = rayTraceRegistry.processCallbacks(chunk);
			result = Raytrace.getClosestHit(Type.BLOCK, src, result, tmp.getValue());
		}

		src = null;
		dest = null;
		return result;
	}

	private CallbackResult<RayTraceResult> rayTraceCallback(Chunk chunk, BlockPos listener, Object... params)
	{
		RaytraceBlock rt = new RaytraceBlock(chunk.getWorld(), src, dest, listener);
		return CallbackResult.of(rt.trace());
	}

	//#end getRayTraceResult

	//#region canPlaceBlockAt
	/**
	 * Checks whether the block can be placed at the position.<br>
	 * Called via ASM from {@link ItemBlock#onItemUse(ItemStack, EntityPlayer, World, BlockPos, EnumHand, EnumFacing, float, float, float)}
	 * at the beginning.<br>
	 * Tests the block bounding box (boxes if {@link IChunkCollidable}) against the occupied blocks position, then against all the bounding
	 * boxes of the {@link IChunkCollidable} available for those chunks.
	 *
	 * @param itemStack the item stack
	 * @param player the player
	 * @param world the world
	 * @param block the block
	 * @param pos the pos
	 * @param side the side
	 * @return true, if can be placed
	 */
	public boolean canPlaceBlockAt(ItemStack itemStack, EntityPlayer player, World world, Block block, BlockPos pos, EnumHand hand, EnumFacing side)
	{
		AxisAlignedBB[] aabbs;
		if (block instanceof IChunkCollidable)
		{
			IBlockState state = DirectionalComponent.getPlacedState(ItemUtils.getStateFromItemStack(itemStack), side, player);
			aabbs = ((IChunkCollidable) block).getPlacedBoundingBox(world, pos, state, hand, side, player, itemStack);
		}
		else
			aabbs = AABBUtils.getCollisionBoundingBoxes(world, block, pos);

		if (ArrayUtils.isEmpty(aabbs))
			return true;

		AABBUtils.offset(pos, aabbs);

		//check against each block position occupied by the AABBs
		if (block instanceof IChunkCollidable)
		{
			for (AxisAlignedBB aabb : aabbs)
			{
				if (aabb == null)
					continue;
				for (BlockPos p : BlockPosUtils.getAllInBox(aabb))
				{
					boolean b = false;
					b |= !world.getBlockState(p).getBlock().isReplaceable(world, p);
					b &= AABBUtils.isColliding(aabb, AABBUtils.getCollisionBoundingBoxes(world, new MBlockState(world, p), true));

					if (b)
						return false;
				}
			}
		}

		for (Chunk chunk : ChunkBlockHandler.getAffectedChunks(world, aabbs))
		{
			CallbackResult<Boolean> result = placeAtRegistry.processCallbacks(chunk, (Object[]) aabbs);
			if (result.getValue() != null && !result.getValue())
				return false;
		}
		return true;
	}

	private CallbackResult<Boolean> placeAtCallback(Chunk chunk, BlockPos listener, Object... params)
	{
		MBlockState state = new MBlockState(chunk.getWorld(), listener);
		AxisAlignedBB[] blockBounds = AABBUtils.getCollisionBoundingBoxes(chunk.getWorld(), state, true);

		return CallbackResult.of(!AABBUtils.isColliding((AxisAlignedBB[]) params, blockBounds));
	}

	//#end canPlaceBlockAt

	/**
	 * Replaces to air all the blocks colliding with the {@link AxisAlignedBB} of the {@link MBlockState}.
	 *
	 * @param world the world
	 * @param state the state
	 */
	public void replaceBlocks(World world, MBlockState state)
	{
		AxisAlignedBB[] aabbs = AABBUtils.getCollisionBoundingBoxes(world, state, true);
		for (AxisAlignedBB aabb : aabbs)
		{
			if (aabb == null)
				continue;

			for (BlockPos pos : BlockPosUtils.getAllInBox(aabb))
			{
				if (world.getBlockState(pos).getBlock().isReplaceable(world, pos))
					world.setBlockToAir(pos);
			}
		}
	}

	/**
	 * Notifies all the blocks colliding with the {@link AxisAlignedBB} of the {@link MBlockState}.
	 *
	 * @param world the world
	 * @param state the state
	 */
	public void updateBlocks(World world, MBlockState state)
	{
		AxisAlignedBB[] aabbs = AABBUtils.getCollisionBoundingBoxes(world, state, true);
		for (AxisAlignedBB aabb : aabbs)
		{
			if (aabb == null)
				continue;

			for (BlockPos pos : BlockPosUtils.getAllInBox(aabb))
				world.notifyBlockOfStateChange(pos, state.getBlock());
		}
	}

	/**
	 * Gets the {@link ChunkCollision} instance.
	 *
	 * @return the chunk collision
	 */
	public static ChunkCollision get()
	{
		return instance;
	}
}
