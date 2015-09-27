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

import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

/**
 *
 * @author Ordinastie
 *
 */
public class Ray
{
	/** Origin {@link Point} of this {@link Ray}. */
	public Point origin;
	/** Direction of this {@link Ray}. */
	public Vector direction;

	/**
	 * Instantiates a new {@link Ray}.
	 *
	 * @param p the origin {@link Point}
	 * @param v the direction {@link Vector}
	 */
	public Ray(Point p, Vector v)
	{
		origin = p;
		direction = v;
	}

	/**
	 * Instantiates a new {@link Ray} from a specified one.
	 *
	 * @param r the ray to copy
	 */
	public Ray(Ray r)
	{
		origin = new Point(r.origin);
		direction = new Vector(r.direction);
	}

	/**
	 * Instantiates a new {@link Ray} from two {@link Vec3}.
	 *
	 * @param src the src
	 * @param dest the dest
	 */
	public Ray(Vec3 src, Vec3 dest)
	{
		origin = new Point(src);
		direction = new Vector(origin, new Point(dest));
	}

	/**
	 * Gets the {@link Point} at the specified distance.
	 *
	 * @param t the distance
	 * @return the point at the distance t
	 */
	public Point getPointAt(double t)
	{
		if (Double.isNaN(t))
			return null;
		return new Point(origin.x + t * direction.x, origin.y + t * direction.y, origin.z + t * direction.z);
	}

	/**
	 * Gets the distance to the plane at x.
	 *
	 * @param x the x plane
	 * @return the distance
	 */
	public double intersectX(double x)
	{
		if (direction.x == 0)
			return Double.NaN;
		return (x - origin.x) / direction.x;
	}

	/**
	 * Gets the distance to the plane at y.
	 *
	 * @param y the y plane
	 * @return the distance
	 */
	public double intersectY(double y)
	{
		if (direction.y == 0)
			return Double.NaN;
		return (y - origin.y) / direction.y;
	}

	/**
	 * Gets the distance to the plane at z.
	 *
	 * @param z the z plane
	 * @return the distance
	 */
	public double intersectZ(double z)
	{
		if (direction.z == 0)
			return Double.NaN;
		return (z - origin.z) / direction.z;
	}

	/**
	 * Finds the points intersecting the {@link AxisAlignedBB}
	 *
	 * @param aabb the aabb
	 * @return the list
	 */
	public List<Point> intersect(AxisAlignedBB aabb)
	{
		double ix = intersectX(aabb.minX);
		double iX = intersectX(aabb.maxX);
		double iy = intersectY(aabb.minY);
		double iY = intersectY(aabb.maxY);
		double iz = intersectZ(aabb.minZ);
		double iZ = intersectZ(aabb.maxZ);
		Point interx = ix >= 0 ? getPointAt(ix) : null;
		Point interX = iX >= 0 ? getPointAt(iX) : null;
		Point intery = iy >= 0 ? getPointAt(iy) : null;
		Point interY = iY >= 0 ? getPointAt(iY) : null;
		Point interz = iz >= 0 ? getPointAt(iz) : null;
		Point interZ = iZ >= 0 ? getPointAt(iZ) : null;

		List<Point> list = new ArrayList<>();
		if (interx != null && interx.isInside(aabb))
			list.add(interx);
		if (interX != null && interX.isInside(aabb))
			list.add(interX);

		if (intery != null && intery.isInside(aabb))
			list.add(intery);
		if (interY != null && interY.isInside(aabb))
			list.add(interY);

		if (interz != null && interz.isInside(aabb))
			list.add(interz);
		if (interZ != null && interZ.isInside(aabb))
			list.add(interZ);

		return list;
	}

}
