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

public class Point
{
	public double x;
	public double y;
	public double z;

	public Point(double x, double y, double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Point(Point p)
	{
		x = p.x;
		y = p.y;
		z = p.z;
	}

	public Point(Vec3 v)
	{
		x = v.xCoord;
		y = v.yCoord;
		z = v.zCoord;
	}

	/**
	 * Set this point to x, y and z
	 * 
	 * @param x
	 * @param y
	 * @param z
	 */
	public void set(double x, double y, double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Move point according to vector
	 * 
	 * @param v
	 */
	public Point add(Vector v)
	{
		x += v.x;
		y += v.y;
		z += v.z;
		return this;
	}

	public Point add(double x, double y, double z)
	{
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}

	/**
	 * Test if this is equal to p
	 * 
	 * @param
	 */
	public boolean equals(Point p)
	{
		if (p == null)
			return false;
		return ((x == p.x) && (y == p.y) && (z == p.z));
	}

	/**
	 * Calculate the distance between two points squared
	 * 
	 * @param p1
	 * @param p2
	 */
	public static double distanceSquared(Point p1, Point p2)
	{
		double x = p2.x - p1.x;
		double y = p2.y - p1.y;
		double z = p2.z - p1.z;
		return x * x + y * y + z * z;
	}

	/**
	 * Calculate the distance between two points
	 */
	public static double distance(Point p1, Point p2)
	{
		double x = p2.x - p1.x;
		double y = p2.y - p1.y;
		double z = p2.z - p1.z;
		return Math.sqrt(x * x + y * y + z * z);
	}

	@Override
	public String toString()
	{
		return "[" + x + ", " + y + ", " + z + "]";
	}

	public Vec3 toVec3()
	{
		return Vec3.createVectorHelper(x, y, z);
	}

}
