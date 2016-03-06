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

package net.malisis.core.block;

import java.util.List;

import net.malisis.core.block.component.DirectionalComponent;
import net.malisis.core.util.AABBUtils;
import net.malisis.core.util.raytrace.RaytraceBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import org.apache.commons.lang3.ArrayUtils;

/**
 * {@link IBoundingBox} defines an implementers that have bounding box.<br>
 * Currently used in {@link RaytraceBlock#trace()}.
 *
 * @author Ordinastie
 *
 */
public interface IBoundingBox
{
	public AxisAlignedBB getBoundingBox(IBlockAccess world, BlockPos pos, BoundingBoxType type);

	/**
	 * Gets the {@link AxisAlignedBB} for this {@link IBoundingBox}.
	 *
	 * @param world the world
	 * @param pos the pos
	 * @param type the type
	 * @return the bounding box
	 */
	public default AxisAlignedBB[] getBoundingBoxes(IBlockAccess world, BlockPos pos, BoundingBoxType type)
	{
		return new AxisAlignedBB[] { getBoundingBox(world, pos, type) };
	}

	public default AxisAlignedBB[] getCollisionBoundingBoxes(World world, BlockPos pos, IBlockState state)
	{
		AxisAlignedBB[] aabbs = getBoundingBoxes(world, pos, BoundingBoxType.COLLISION);
		aabbs = AABBUtils.rotate(aabbs, DirectionalComponent.getDirection(state));
		return aabbs;
	}

	public default void addCollisionBoxesToList(World world, BlockPos pos, IBlockState state, AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity)
	{
		AxisAlignedBB[] aabbs = getBoundingBoxes(world, pos, BoundingBoxType.COLLISION);
		aabbs = AABBUtils.rotate(aabbs, DirectionalComponent.getDirection(state));

		for (AxisAlignedBB aabb : AABBUtils.offset(pos, aabbs))
		{
			if (aabb != null && mask.intersectsWith(aabb))
				list.add(aabb);
		}
	}

	public default AxisAlignedBB getSelectedBoundingBox(World world, BlockPos pos)
	{
		AxisAlignedBB[] aabbs = getBoundingBoxes(world, pos, BoundingBoxType.SELECTION);
		if (ArrayUtils.isEmpty(aabbs) || aabbs[0] == null)
			return AABBUtils.empty(pos);

		aabbs = AABBUtils.rotate(aabbs, DirectionalComponent.getDirection(world, pos));

		return AABBUtils.offset(pos, AABBUtils.combine(aabbs));
	}

	public default AxisAlignedBB[] getRenderBoundingBox(IBlockAccess world, BlockPos pos, IBlockState state)
	{
		AxisAlignedBB[] aabbs = getBoundingBoxes(world, pos, BoundingBoxType.RENDER);
		aabbs = AABBUtils.rotate(aabbs, DirectionalComponent.getDirection(state));

		return aabbs;
	}

	public default AxisAlignedBB[] getRayTraceBoundingBox(IBlockAccess world, BlockPos pos, IBlockState state)
	{
		AxisAlignedBB[] aabbs = getBoundingBoxes(world, pos, BoundingBoxType.RAYTRACE);
		aabbs = AABBUtils.rotate(aabbs, DirectionalComponent.getDirection(state));

		return aabbs;
	}

	public default MovingObjectPosition collisionRayTrace(World world, BlockPos pos, Vec3 src, Vec3 dest)
	{
		return new RaytraceBlock(world, src, dest, pos).trace();
	}
}
