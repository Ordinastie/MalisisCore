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

import net.malisis.core.util.AABBUtils;
import net.malisis.core.util.BlockPosUtils;
import net.malisis.core.util.MBlockState;
import net.malisis.core.util.Point;
import net.malisis.core.util.RaytraceBlock;
import net.malisis.core.util.chunkblock.ChunkBlockHandler;
import net.malisis.core.util.chunkblock.ChunkBlockHandler.ChunkProcedure;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

/**
 * This class is the entry point for all the chunk collision related calculation.<br>
 * The static methods are called via ASM which then call the process for the corresponding server or client instance.
 *
 * @author Ordinastie
 *
 */
public class ChunkCollision
{
	private static ChunkCollision instance = new ChunkCollision();

	private Point src;
	private Point dest;

	//#region getCollisionBoundinBoxes
	/**
	 * Gets the collision bounding boxes for the intersecting chunks.<br>
	 * Called via ASM from {@link World#getCollidingBoundingBoxes(Entity, AxisAlignedBB)}
	 *
	 * @param world the world
	 * @param mask the mask
	 * @param list the list
	 * @param entity the entity
	 */
	public void getCollisionBoundingBoxes(World world, AxisAlignedBB mask, List<AxisAlignedBB> list, Entity entity)
	{
		CollisionProcedure procedure = new CollisionProcedure(mask, list);

		for (Chunk chunk : ChunkBlockHandler.getAffectedChunks(world, mask))
			ChunkBlockHandler.get().callProcedure(chunk, procedure);
	}

	//#end getCollisionBoundinBoxes

	//#region getRayTraceResult
	/**
	 * Sets the ray trace infos.<br>
	 * Called via ASM at the beginning of {@link World#rayTraceBlocks(Vec3, Vec3, boolean, boolean, boolean)}
	 *
	 * @param src the src
	 * @param dest the dest
	 */
	public void setRayTraceInfos(Vec3 src, Vec3 dest)
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
	 * Called via ASM from {@link World#rayTraceBlocks(Vec3, Vec3, boolean, boolean, boolean)} before each return.
	 *
	 * @param world the world
	 * @param mop the mop
	 * @return the ray trace result
	 */
	public MovingObjectPosition getRayTraceResult(World world, MovingObjectPosition mop)
	{
		if (src == null || dest == null)
			return mop;

		AxisAlignedBB aabb = new AxisAlignedBB(src.x, src.y, src.z, dest.x, dest.y, dest.z);

		RayTraceProcedure procedure = new RayTraceProcedure(src, dest, mop);
		for (Chunk chunk : ChunkBlockHandler.getAffectedChunks(world, aabb))
			ChunkBlockHandler.get().callProcedure(chunk, procedure);

		return procedure.mop;
	}

	/**
	 * Gets the closest {@link MovingObjectPosition} to the source.
	 *
	 * @param src the src
	 * @param mop1 the mop1
	 * @param mop2 the mop2
	 * @return the closest
	 */
	private MovingObjectPosition getClosest(Point src, MovingObjectPosition mop1, MovingObjectPosition mop2)
	{
		if (mop1 == null)
			return mop2;
		if (mop2 == null)
			return mop1;

		if (mop1.typeOfHit == MovingObjectType.MISS && mop2.typeOfHit != MovingObjectType.MISS)
			return mop2;
		if (mop1.typeOfHit != MovingObjectType.MISS && mop2.typeOfHit == MovingObjectType.MISS)
			return mop1;

		if (Point.distanceSquared(src, new Point(mop1.hitVec)) > Point.distanceSquared(src, new Point(mop2.hitVec)))
			return mop2;
		return mop1;
	}

	//#end getRayTraceResult

	//#region canPlaceBlockAt
	/**
	 * Checks whether the block can be placed at the position.<br>
	 * Called via ASM from {@link World#canPlaceEntityOnSide(Block, int, int, int, boolean, int, Entity, net.minecraft.item.ItemStack)} at
	 * the begining.<br>
	 * Tests the block bounding box (boxes if {@link IChunkCollidable}) against the occupied blocks position, then against all the bounding
	 * boxes of the {@link IChunkCollidable} available for those chunks.
	 *
	 * @param world the world
	 * @param block the block
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @return true, if can be placed
	 */
	public boolean canPlaceBlockAt(ItemStack itemStack, EntityPlayer player, World world, Block block, BlockPos pos, EnumFacing side)
	{
		AxisAlignedBB[] aabbs;
		if (block instanceof IChunkCollidable)
			aabbs = ((IChunkCollidable) block).getPlacedBoundingBox(world, pos, side, player, itemStack);
		else
			aabbs = AABBUtils.getCollisionBoundingBoxes(world, block, pos);

		if (aabbs == null)
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

		CheckCollisionProcedure procedure = new CheckCollisionProcedure(aabbs);
		for (Chunk chunk : ChunkBlockHandler.getAffectedChunks(world, aabbs))
			ChunkBlockHandler.get().callProcedure(chunk, procedure);

		return !procedure.collide;
	}

	//#end canPlaceBlockAt

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

	public static ChunkCollision get()
	{
		return instance;
	}

	/**
	 * The procedure used to check the collision for a {@link IChunkCollidable} coordinate.<br>
	 */
	private static class CollisionProcedure extends ChunkProcedure
	{
		private AxisAlignedBB mask;
		private List<AxisAlignedBB> list;

		public CollisionProcedure(AxisAlignedBB mask, List<AxisAlignedBB> list)
		{
			this.mask = mask;
			this.list = list;
		}

		@Override
		public boolean execute(long coord)
		{
			if (!check(coord))
				return true;

			if (state.getBlock() instanceof IChunkCollidable)
			{
				AxisAlignedBB[] aabbs = ((IChunkCollidable) state.getBlock()).getCollisionBoundingBoxes(world, state.getPos());
				for (AxisAlignedBB aabb : aabbs)
				{
					if (mask != null && aabb != null)
					{
						aabb = AABBUtils.offset(state.getPos(), aabb);
						if (mask.intersectsWith(aabb))
							list.add(aabb);
					}

				}
			}
			return true;
		}

		@Override
		protected void clean()
		{
			super.clean();
			mask = null;
			list = null;
		}
	}

	/**
	 * The procedure used to check ray tracing for a {@link IChunkCollidable} coordinate.
	 */
	private static class RayTraceProcedure extends ChunkProcedure
	{
		private Point src;
		private Point dest;
		private MovingObjectPosition mop;

		public RayTraceProcedure(Point src, Point dest, MovingObjectPosition mop)
		{
			this.src = src;
			this.dest = dest;
			this.mop = mop;
		}

		@Override
		public boolean execute(long coord)
		{
			if (!check(coord))
				return true;

			RaytraceBlock rt = new RaytraceBlock(world, src, dest, state.getPos());
			mop = get().getClosest(src, rt.trace(), mop);

			return true;
		}

		@Override
		protected void clean()
		{
			super.clean();
			src = null;
			dest = null;
			mop = null;
		}
	}

	/**
	 * The procedure used to check intersection from {@link IChunkCollidable} coordinates with {@link AxisAlignedBB} array.
	 */
	private static class CheckCollisionProcedure extends ChunkProcedure
	{
		private AxisAlignedBB[] aabbs;
		private boolean collide = false;

		public CheckCollisionProcedure(AxisAlignedBB[] aabbs)
		{
			this.aabbs = aabbs;
		}

		@Override
		public boolean execute(long coord)
		{
			if (!check(coord))
				return true;

			AxisAlignedBB[] blockBounds = AABBUtils.getCollisionBoundingBoxes(world, state);
			AABBUtils.offset(state.getX(), state.getY(), state.getZ(), blockBounds);

			collide = AABBUtils.isColliding(aabbs, blockBounds);
			if (collide)
				return false;
			return true;
		}

		@Override
		protected void clean()
		{
			super.clean();
			aabbs = null;
			collide = false;
		}

	}
}
