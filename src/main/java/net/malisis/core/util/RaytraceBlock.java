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

package net.malisis.core.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import net.malisis.core.block.BoundingBoxType;
import net.malisis.core.block.IBlockDirectional;
import net.malisis.core.block.IBoundingBox;
import net.minecraft.block.Block;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

/**
 * RayTrace class that offers more control to handle raytracing.
 *
 * @author Ordinastie
 *
 */
public class RaytraceBlock
{
	/** World reference **/
	private WeakReference<World> world;
	/** Position of the block being ray traced **/
	private BlockPos pos;
	/** Block being ray traced. */
	private Block block;
	/** Source of the ray trace. */
	private Point src;
	/** Destination of the ray trace. */
	private Point dest;
	/** Ray describing the ray trace. */
	private Ray ray;

	/**
	 * Sets the parameters for this {@link RaytraceBlock}.
	 *
	 * @param world the world
	 * @param ray the ray
	 * @param pos the pos
	 */
	public RaytraceBlock(World world, Ray ray, BlockPos pos)
	{
		this.world = new WeakReference<World>(world);
		this.src = ray.origin;
		this.ray = ray;
		this.pos = pos;
		this.block = world().getBlockState(pos).getBlock();
	}

	/**
	 * Sets the parameters for this {@link RaytraceBlock}.
	 *
	 * @param world the world
	 * @param src the src
	 * @param v the v
	 * @param pos the pos
	 */
	public RaytraceBlock(World world, Point src, Vector v, BlockPos pos)
	{
		this(world, new Ray(src, v), pos);
	}

	/**
	 * Sets the parameters for this {@link RaytraceBlock}.
	 *
	 * @param world the world
	 * @param src the src
	 * @param dest the dest
	 * @param pos the pos
	 */
	public RaytraceBlock(World world, Point src, Point dest, BlockPos pos)
	{
		this(world, new Ray(src, new Vector(src, dest)), pos);
		this.dest = dest;
	}

	/**
	 * Sets the parameters for this {@link RaytraceBlock}.
	 *
	 * @param world the world
	 * @param src the src
	 * @param dest the dest
	 * @param pos the pos
	 */
	public RaytraceBlock(World world, Vec3 src, Vec3 dest, BlockPos pos)
	{
		this(world, new Ray(src, dest), pos);
		this.dest = new Point(dest);
	}

	/**
	 * Get the world used by this {@link RaytraceBlock}.
	 *
	 * @return the world
	 */
	public World world()
	{
		return world.get();
	}

	/**
	 * Gets the direction vector of the ray.
	 *
	 * @return the direction
	 */
	public Vector direction()
	{
		return ray.direction;
	}

	/**
	 * Gets the length of the ray.
	 *
	 * @return the distance
	 */
	public double distance()
	{
		return ray.direction.length();
	}

	/**
	 * Does the raytracing.
	 *
	 * @return {@link MovingObjectPosition} with <code>typeOfHit</code> <b>BLOCK</b> if a ray hits a block in the way, or <b>MISS</b> if it
	 *         reaches <code>dest</code> without any hit
	 */
	public MovingObjectPosition trace()
	{
		if (!(block instanceof IBoundingBox))
			return block.collisionRayTrace(world(), pos, ray.origin.toVec3(), dest.toVec3());

		//
		IBoundingBox block = (IBoundingBox) this.block;
		AxisAlignedBB[] aabbs = block.getBoundingBox(world(), pos, BoundingBoxType.RAYTRACE);
		if (aabbs == null || aabbs.length == 0)
			return null;
		if (block instanceof IBlockDirectional)
			aabbs = AABBUtils.rotate(aabbs, ((IBlockDirectional) block).getDirection(world.get(), pos));

		List<Point> points = new ArrayList<>();
		double maxDist = Point.distanceSquared(src, dest);
		for (AxisAlignedBB aabb : AABBUtils.offset(pos, aabbs))
		{
			if (aabb == null)
				continue;

			for (Point p : ray.intersect(aabb))
			{
				if (Point.distanceSquared(src, p) < maxDist)
					points.add(p);
			}
		}

		if (points.size() == 0)
			return null;

		Point closest = getClosest(points);
		if (closest == null)
			return null;

		EnumFacing side = getSide(aabbs, closest);

		return new MovingObjectPosition(closest.toVec3(), side, pos);
	}

	/**
	 * Gets the closest {@link Point} of the origin.
	 *
	 * @param points the points
	 * @return the closest point
	 */
	private Point getClosest(List<Point> points)
	{
		double distance = Double.MAX_VALUE;
		Point ret = null;
		for (Point p : points)
		{
			double d = Point.distanceSquared(src, p);
			if (distance > d)
			{
				distance = d;
				ret = p;
			}
		}

		return ret;
	}

	private EnumFacing getSide(AxisAlignedBB[] aabbs, Point point)
	{
		for (AxisAlignedBB aabb : aabbs)
		{
			if (aabb == null)
				continue;

			if (point.x == aabb.minX)
				return EnumFacing.WEST;
			if (point.x == aabb.maxX)
				return EnumFacing.EAST;
			if (point.y == aabb.minY)
				return EnumFacing.DOWN;
			if (point.y == aabb.maxY)
				return EnumFacing.UP;
			if (point.z == aabb.minZ)
				return EnumFacing.NORTH;
			if (point.z == aabb.maxZ)
				return EnumFacing.SOUTH;
		}
		return null;
	}
}
