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

import net.minecraft.util.math.Vec3d;

/**
 *
 * @author Ordinastie
 *
 */
public class Vector
{
	/** x coordinate of this {@link Vector}. */
	public double x;

	/** y coordinate of this {@link Vector}. */
	public double y;

	/** z coordinate of this {@link Vector}. */
	public double z;

	/**
	 * Instantiates a new vector.
	 *
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 */
	public Vector(double x, double y, double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Instantiates a new vector.
	 *
	 * @param v the v
	 */
	public Vector(Vector v)
	{
		x = v.x;
		y = v.y;
		z = v.z;
	}

	/**
	 * Instantiates a new vector.
	 *
	 * @param p the p
	 */
	public Vector(Point p)
	{
		x = p.x;
		y = p.y;
		z = p.z;
	}

	/**
	 * Instantiates a new vector.
	 *
	 * @param p1 the p1
	 * @param p2 the p2
	 */
	public Vector(Point p1, Point p2)
	{
		x = p2.x - p1.x;
		y = p2.y - p1.y;
		z = p2.z - p1.z;
	}

	/**
	 * Instantiates a new vector.
	 *
	 * @param vec the vec
	 */
	public Vector(Vec3d vec)
	{
		x = vec.xCoord;
		y = vec.yCoord;
		z = vec.zCoord;
	}

	/**
	 * Sets the x, y and z coordinates for this {@link Vector}.
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
	 * Gets the squared length of this {@link Vector}.
	 *
	 * @return the length squared
	 */
	public double lengthSquared()
	{
		return x * x + y * y + z * z;
	}

	/**
	 * Gets the squared length of this {@link Vector}.
	 *
	 * @return the length squared
	 */
	public double length()
	{
		return Math.sqrt(x * x + y * y + z * z);
	}

	/**
	 * Normalizes this {@link Vector}.
	 */
	public void normalize()
	{
		double d = length();
		x /= d;
		y /= d;
		z /= d;
	}

	/**
	 * Subtracts the given {@link Vector} from this <code>Vector</code>.
	 *
	 * @param v the vector to subtract
	 */
	public void subtract(Vector v)
	{
		x = x - v.x;
		y = y - v.y;
		z = z - v.z;
	}

	/**
	 * Adds the given {@link Vector} from this <code>Vector</code>.
	 *
	 * @param v the vector to add
	 */
	public void add(Vector v)
	{
		x += v.x;
		y += v.y;
		z += v.z;
	}

	/**
	 * Calculates the cross product of this {@link Vector} with a given <code>Vector</code>.
	 *
	 * @param v the vector
	 */
	public void cross(Vector v)
	{
		x = (y * v.z) - (z * v.y);
		y = (z * v.x) - (x * v.z);
		z = (x * v.y) - (y * v.x);
	}

	/**
	 * Calculates the dot product of this {@link Vector} with a given <code>Vector</code>.
	 *
	 * @param v the vector
	 * @return the dot product
	 */
	public double dot(Vector v)
	{
		return (x * v.x) + (y * v.y) + (z * v.z);
	}

	/**
	 * Calculates the dot product of this {@link Vector} with a given {@link Point}.
	 *
	 * @param p the point
	 * @return the dot product
	 */
	public double dot(Point p)
	{
		return (x * p.x) + (y * p.y) + (z * p.z);
	}

	/**
	 * Scales this {@link Vector} by a factor.
	 *
	 * @param factor the factor to scale
	 */
	public void scale(double factor)
	{
		x *= factor;
		y *= factor;
		z *= factor;
	}

	/**
	 * Inverses the vector.
	 */
	public void negate()
	{
		x = -x;
		y = -y;
		z = -z;
	}

	@Override
	public String toString()
	{
		return "[" + x + ", " + y + ", " + z + "]";
	}
}
