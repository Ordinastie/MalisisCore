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

import net.minecraft.util.Vec3;

/**
 *
 * @author Ordinastie
 *
 */
public class Point
{

	/** x coordinate of this {@link Point}. */
	public double x;

	/** y coordinate of this {@link Point}. */
	public double y;

	/** z coordinate of this {@link Point}. */
	public double z;

	/**
	 * Instantiates a new point.
	 *
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 */
	public Point(double x, double y, double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Instantiates a new point.
	 *
	 * @param p the p
	 */
	public Point(Point p)
	{
		x = p.x;
		y = p.y;
		z = p.z;
	}

	/**
	 * Instantiates a new point.
	 *
	 * @param v the v
	 */
	public Point(Vec3 v)
	{
		x = v.xCoord;
		y = v.yCoord;
		z = v.zCoord;
	}

	/**
	 * Sets this point to x, y and z coordinates.
	 *
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 */
	public void set(double x, double y, double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Moves this {@link Point} according to vector.
	 *
	 * @param v the v
	 * @return the point
	 */
	public Point add(Vector v)
	{
		x += v.x;
		y += v.y;
		z += v.z;
		return this;
	}

	/**
	 * Adds the x, y and z coordinates to this {@link Point}.
	 *
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @return the point
	 */
	public Point add(double x, double y, double z)
	{
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}

	/**
	 * Creates a {@link Vec3} from this {@link Point} coordinates.
	 *
	 * @return the vec3
	 */
	public Vec3 toVec3()
	{
		return Vec3.createVectorHelper(x, y, z);
	}

	/**
	 * Checks if this {@link Point} is equal to the specified one.
	 *
	 * @param p the point to check
	 * @return true, if equal, false otherwise
	 */
	public boolean equals(Point p)
	{
		if (p == null)
			return false;
		return ((x == p.x) && (y == p.y) && (z == p.z));
	}

	@Override
	public String toString()
	{
		return "[" + x + ", " + y + ", " + z + "]";
	}

	/**
	 * Calculates the squared distance between two {@link Point points}.
	 *
	 * @param p1 fist point
	 * @param p2 second point
	 * @return the distance squared
	 */
	public static double distanceSquared(Point p1, Point p2)
	{
		double x = p2.x - p1.x;
		double y = p2.y - p1.y;
		double z = p2.z - p1.z;
		return x * x + y * y + z * z;
	}

	/**
	 * Calculates the distance between two {@link Point points}.
	 *
	 * @param p1 fist point
	 * @param p2 second point
	 * @return the distance
	 */
	public static double distance(Point p1, Point p2)
	{
		double x = p2.x - p1.x;
		double y = p2.y - p1.y;
		double z = p2.z - p1.z;
		return Math.sqrt(x * x + y * y + z * z);
	}
}
