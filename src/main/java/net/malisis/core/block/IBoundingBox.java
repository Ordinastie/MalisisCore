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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
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
	public AxisAlignedBB getBoundingBox(IBlockAccess world, BlockPos pos, IBlockState state, BoundingBoxType type);

	/**
	 * Gets the {@link AxisAlignedBB} for this {@link IBoundingBox}.
	 *
	 * @param world the world
	 * @param pos the pos
	 * @param type the type
	 * @return the bounding box
	 */
	public default AxisAlignedBB[] getBoundingBoxes(IBlockAccess world, BlockPos pos, IBlockState state, BoundingBoxType type)
	{
		return new AxisAlignedBB[] { getBoundingBox(world, pos, state, type) };
	}

	public default AxisAlignedBB[] getCollisionBoundingBoxes(World world, BlockPos pos, IBlockState state)
	{
		AxisAlignedBB[] aabbs = getBoundingBoxes(world, pos, state, BoundingBoxType.COLLISION);
		aabbs = AABBUtils.rotate(aabbs, DirectionalComponent.getDirection(state));
		return aabbs;
	}

	public default void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity)
	{
		AxisAlignedBB[] aabbs = getBoundingBoxes(world, pos, state, BoundingBoxType.COLLISION);
		aabbs = AABBUtils.rotate(aabbs, DirectionalComponent.getDirection(state));

		for (AxisAlignedBB aabb : AABBUtils.offset(pos, aabbs))
		{
			if (aabb != null && mask.intersectsWith(aabb))
				list.add(aabb);
		}
	}

	//TODO : implement multi AABB selection box
	public default AxisAlignedBB getSelectedBoundingBox(IBlockState state, World world, BlockPos pos)
	{
		AxisAlignedBB[] aabbs = getBoundingBoxes(world, pos, state, BoundingBoxType.SELECTION);
		if (ArrayUtils.isEmpty(aabbs) || aabbs[0] == null)
			return AABBUtils.empty(pos);

		aabbs = AABBUtils.rotate(aabbs, DirectionalComponent.getDirection(world, pos));

		return AABBUtils.offset(pos, AABBUtils.combine(aabbs));
	}

	public default AxisAlignedBB[] getRenderBoundingBox(IBlockAccess world, BlockPos pos, IBlockState state)
	{
		AxisAlignedBB[] aabbs = getBoundingBoxes(world, pos, state, BoundingBoxType.RENDER);
		aabbs = AABBUtils.rotate(aabbs, DirectionalComponent.getDirection(state));

		return aabbs;
	}

	public default AxisAlignedBB[] getRayTraceBoundingBox(IBlockAccess world, BlockPos pos, IBlockState state)
	{
		AxisAlignedBB[] aabbs = getBoundingBoxes(world, pos, state, BoundingBoxType.RAYTRACE);
		aabbs = AABBUtils.rotate(aabbs, DirectionalComponent.getDirection(state));

		return aabbs;
	}

	public default RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d src, Vec3d dest)
	{
		//TODO: remove RayTraceBlock entirely and call regular RayTrace on getRayTraceBoundingBox()
		return new RaytraceBlock(world, src, dest, pos).trace();
	}

	/**
	 * Gets the rendering bounds for the Block at the specified {@link BlockPos}.
	 *
	 * @param world the world
	 * @param pos the pos
	 * @return the rendering bounds
	 */
	public static AxisAlignedBB getRenderingBounds(IBlockAccess world, BlockPos pos)
	{
		IBlockState state = world.getBlockState(pos);
		IBoundingBox ibb = IComponent.getComponent(IBoundingBox.class, state.getBlock());
		if (ibb == null)
			return AABBUtils.identity(pos);

		return AABBUtils.offset(pos, AABBUtils.combine(ibb.getRenderBoundingBox(world, pos, state)));
	}
}
