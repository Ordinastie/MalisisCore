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

package net.malisis.core.util.raytrace;

import java.util.ArrayList;
import java.util.List;

import net.malisis.core.util.Point;
import net.malisis.core.util.Ray;
import net.malisis.core.util.Vector;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

/**
 * @author Ordinastie
 *
 */
public class Raytrace
{
	/** Source of the ray trace. */
	protected Point src;
	/** Destination of the ray trace. */
	protected Point dest;
	/** Ray describing the ray trace. */
	protected Ray ray;

	/**
	 * Instanciate a new {@link Raytrace}.
	 *
	 * @param ray the ray
	 */
	public Raytrace(Ray ray)
	{
		this.src = ray.origin;
		this.ray = ray;
	}

	/**
	 * Instanciate a new {@link Raytrace}.
	 *
	 * @param src the src
	 * @param dest the dest
	 */
	public Raytrace(Point src, Point dest)
	{
		this(new Ray(src, new Vector(src, dest)));
		this.dest = dest;
	}

	/**
	 * Instanciate a new {@link Raytrace}.
	 *
	 * @param src the src
	 * @param dest the dest
	 */
	public Raytrace(Vec3d src, Vec3d dest)
	{
		this(new Ray(src, dest));
		this.dest = new Point(dest);
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

	public Pair<EnumFacing, Point> trace(AxisAlignedBB... aabbs)
	{
		if (ArrayUtils.isEmpty(aabbs))
			return null;

		List<Pair<EnumFacing, Point>> points = new ArrayList<>();
		double maxDist = dest != null ? Point.distanceSquared(src, dest) : Double.MAX_VALUE;
		for (AxisAlignedBB aabb : aabbs)
		{
			if (aabb == null)
				continue;

			for (Pair<EnumFacing, Point> pair : ray.intersect(aabb))
			{
				if (Point.distanceSquared(src, pair.getRight()) < maxDist)
					points.add(pair);
			}
		}

		if (points.size() == 0)
			return null;

		return getClosest(points);
	}

	/**
	 * Gets the closest {@link Point} of the origin.
	 *
	 * @param points the points
	 * @return the closest point
	 */
	private Pair<EnumFacing, Point> getClosest(List<Pair<EnumFacing, Point>> points)
	{
		double distance = Double.MAX_VALUE;
		Pair<EnumFacing, Point> ret = null;
		for (Pair<EnumFacing, Point> pair : points)
		{
			double d = Point.distanceSquared(src, pair.getRight());
			if (distance > d)
			{
				distance = d;
				ret = pair;
			}
		}

		return ret;
	}
}
