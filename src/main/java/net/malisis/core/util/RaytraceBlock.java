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

import java.util.ArrayList;
import java.util.List;

import net.malisis.core.block.BoundingBoxType;
import net.malisis.core.block.MalisisBlock;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * RayTrace class that offers more control to handle raytracing.
 *
 * @author Ordinastie
 *
 */
public class RaytraceBlock
{
	/** Unique instance of {@link RaytraceBlock}. */
	private static RaytraceBlock instance = new RaytraceBlock();

	/** World object (needed for ray tracing inside each block). */
	private World world;
	/** X coordinate of the block being ray traced. */
	private int x;
	/** Y coordinate of the block being ray traced. */
	private int y;
	/** Z coordinate of the block being ray traced. */
	private int z;
	/** Block being ray traced. */
	private Block block;
	/** Source of the ray trace. */
	private Point src;
	/** Destination of the ray trace. */
	private Point dest;
	/** Ray describing the ray trace. */
	private Ray ray;
	/** List of points intersecting the bouding boxes of the block. **/
	List<Point> points = new ArrayList<>();

	/**
	 * Instantiates a new {@link RaytraceBlock}.
	 */
	private RaytraceBlock()
	{
		this.world = Minecraft.getMinecraft().theWorld;
	}

	/**
	 * Sets the parameters for this {@link RaytraceBlock}
	 *
	 * @param ray the ray
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 */
	private void _set(Ray ray, int x, int y, int z)
	{
		this.src = ray.origin;
		this.ray = ray;
		this.x = x;
		this.y = y;
		this.z = z;
		this.block = world.getBlock(x, y, z);
	}

	/**
	 * Sets the parameters for the ray trace and returns the {@link RaytraceBlock} instance.
	 *
	 * @param ray the ray
	 * @param x the x coordinate of the block
	 * @param y the y coordinate of the block
	 * @param z the z coordinate of the block
	 * @return the {@link RaytraceBlock} instance
	 */
	public static RaytraceBlock set(Ray ray, int x, int y, int z)
	{
		instance._set(ray, x, y, z);
		return instance;
	}

	/**
	 * Sets the parameters for the ray trace and returns the {@link RaytraceBlock} instance.
	 *
	 * @param src the src
	 * @param v the v
	 * @param x the x coordinate of the block
	 * @param y the y coordinate of the block
	 * @param z the z coordinate of the block
	 * @return the {@link RaytraceBlock} instance
	 */
	public static RaytraceBlock set(Point src, Vector v, int x, int y, int z)
	{
		instance._set(new Ray(src, v), x, y, z);
		return instance;
	}

	/**
	 * Sets the parameters for the ray trace and returns the {@link RaytraceBlock} instance.
	 *
	 * @param src the src
	 * @param dest the dest
	 * @param x the x coordinate of the block
	 * @param y the y coordinate of the block
	 * @param z the z coordinate of the block
	 * @return the {@link RaytraceBlock} instance
	 */
	public static RaytraceBlock set(Point src, Point dest, int x, int y, int z)
	{
		instance.dest = dest;
		instance._set(new Ray(src, new Vector(src, dest)), x, y, z);
		return instance;
	}

	/**
	 * Sets the parameters for the ray trace and returns the {@link RaytraceBlock} instance.
	 *
	 * @param src the src
	 * @param dest the dest
	 * @param x the x coordinate of the block
	 * @param y the y coordinate of the block
	 * @param z the z coordinate of the block
	 * @return the {@link RaytraceBlock} instance
	 */
	public static RaytraceBlock set(Vec3 src, Vec3 dest, int x, int y, int z)
	{
		instance.dest = new Point(dest);
		instance._set(new Ray(src, dest), x, y, z);
		return instance;
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
		points.clear();

		if (!(block instanceof MalisisBlock))
			return block.collisionRayTrace(world, x, y, z, ray.origin.toVec3(), dest.toVec3());

		MalisisBlock block = (MalisisBlock) this.block;
		AxisAlignedBB[] aabbs = block.getBoundingBox(world, x, y, z, BoundingBoxType.RAYTRACE);

		for (AxisAlignedBB aabb : aabbs)
		{
			aabb.offset(x, y, z);
			points.addAll(ray.intersect(aabb));
		}

		if (points.size() == 0)
			return null;

		Point closest = getClosest(points);
		ForgeDirection side = getSide(aabbs, closest);

		return new MovingObjectPosition(x, y, z, side.ordinal(), closest.toVec3());
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

	private ForgeDirection getSide(AxisAlignedBB[] aabbs, Point point)
	{
		for (AxisAlignedBB aabb : aabbs)
		{
			if (point.x == aabb.minX)
				return ForgeDirection.WEST;
			if (point.x == aabb.maxX)
				return ForgeDirection.EAST;
			if (point.y == aabb.minY)
				return ForgeDirection.DOWN;
			if (point.y == aabb.maxY)
				return ForgeDirection.UP;
			if (point.z == aabb.minZ)
				return ForgeDirection.NORTH;
			if (point.z == aabb.maxZ)
				return ForgeDirection.SOUTH;
		}
		return ForgeDirection.UNKNOWN;
	}
}
