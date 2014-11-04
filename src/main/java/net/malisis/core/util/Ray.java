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
	 * Instantiates a new ray.
	 *
	 * @param p the p
	 * @param v the v
	 */
	public Ray(Point p, Vector v)
	{
		origin = p;
		direction = v;
	}

	/**
	 * Instantiates a new ray.
	 *
	 * @param r the r
	 */
	public Ray(Ray r)
	{
		origin = new Point(r.origin);
		direction = new Vector(r.direction);
	}

	/**
	 * Gets the {@link Point} at the specified distance.
	 *
	 * @param t the distance
	 * @return the point at the distance t
	 */
	public Point getPointAt(double t)
	{
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

}
