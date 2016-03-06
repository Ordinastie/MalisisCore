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

package net.malisis.core.renderer.animation.transformation;

public class Rotation extends Transformation<Rotation, ITransformable.Rotate>
{
	/** Starting angle. */
	protected float fromAngle;
	/** Target angle. */
	protected float toAngle;
	/** Axis for the rotation. */
	protected float axisX, axisY, axisZ;
	/** Axis position. */
	protected float offsetX, offsetY, offsetZ;

	/**
	 * Instantiates a new {@link Rotation}.
	 *
	 * @param angle the angle
	 */
	public Rotation(float angle)
	{
		to(angle);
	}

	/**
	 * Instantiates a new {@link Rotation}.
	 *
	 * @param fromAngle the from angle
	 * @param toAngle the to angle
	 */
	public Rotation(float fromAngle, float toAngle)
	{
		from(fromAngle);
		to(toAngle);

	}

	/**
	 * Instantiates a new {@link Rotation}.
	 *
	 * @param angle the angle
	 * @param axisX the axis x
	 * @param axisY the axis y
	 * @param axisZ the axis z
	 */
	public Rotation(float angle, float axisX, float axisY, float axisZ)
	{
		to(angle);
		aroundAxis(axisX, axisY, axisZ);
	}

	/**
	 * Instantiates a new {@link Rotation}.
	 *
	 * @param angle the angle
	 * @param axisX the axis x
	 * @param axisY the axis y
	 * @param axisZ the axis z
	 * @param offsetX the offset x
	 * @param offsetY the offset y
	 * @param offsetZ the offset z
	 */
	public Rotation(float angle, float axisX, float axisY, float axisZ, float offsetX, float offsetY, float offsetZ)
	{
		to(angle);
		aroundAxis(axisX, axisY, axisZ);
		offset(offsetX, offsetY, offsetZ);
	}

	/**
	 * Gets this {@link Rotation}.
	 *
	 * @return the rotation
	 */
	@Override
	public Rotation self()
	{
		return this;
	}

	/**
	 * Sets the starting angle for this {@link Rotation}.
	 *
	 * @param angle the angle
	 * @return the rotation
	 */
	public Rotation from(float angle)
	{
		fromAngle = angle;
		return this;
	}

	/**
	 * Sets the target angle for this {@link Rotation}.
	 *
	 * @param angle the angle
	 * @return the rotation
	 */
	public Rotation to(float angle)
	{
		toAngle = angle;
		return this;
	}

	/**
	 * Sets the axis for this {@link Rotation}.
	 *
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @return the rotation
	 */
	public Rotation aroundAxis(float x, float y, float z)
	{
		axisX = x;
		axisY = y;
		axisZ = z;
		return this;
	}

	/**
	 * Sets the axis position for this {@link Rotation}.
	 *
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @return the rotation
	 */
	public Rotation offset(float x, float y, float z)
	{
		offsetX = x;
		offsetY = y;
		offsetZ = z;
		return this;
	}

	/**
	 * Calculates the transformation.
	 *
	 * @param transformable the transformable
	 * @param comp the comp
	 */
	@Override
	protected void doTransform(ITransformable.Rotate transformable, float comp)
	{
		float from = reversed ? toAngle : fromAngle;
		float to = reversed ? fromAngle : toAngle;
		transformable.rotate(from + (to - from) * comp, axisX, axisY, axisZ, offsetX, offsetY, offsetZ);
	}
}