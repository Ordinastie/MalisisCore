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
	protected float fromAngle;
	protected float toAngle;
	protected float axisX, axisY, axisZ;
	protected float offsetX, offsetY, offsetZ;

	public Rotation(float angle)
	{
		to(angle);
	}

	public Rotation(float fromAngle, float toAngle)
	{
		from(fromAngle);
		to(toAngle);

	}

	public Rotation(float angle, float axisX, float axisY, float axisZ)
	{
		to(angle);
		aroundAxis(axisX, axisY, axisZ);
	}

	public Rotation(float angle, float axisX, float axisY, float axisZ, float offsetX, float offsetY, float offsetZ)
	{
		to(angle);
		aroundAxis(axisX, axisY, axisZ);
		offset(offsetX, offsetY, offsetZ);
	}

	public Rotation from(float angle)
	{
		fromAngle = angle;
		return this;
	}

	public Rotation to(float angle)
	{
		toAngle = angle;
		return this;
	}

	public Rotation aroundAxis(float x, float y, float z)
	{
		axisX = x;
		axisY = y;
		axisZ = z;
		return this;
	}

	public Rotation offset(float x, float y, float z)
	{
		offsetX = x;
		offsetY = y;
		offsetZ = z;
		return this;
	}

	@Override
	protected void doTransform(ITransformable.Rotate transformable, float comp)
	{
		if (comp <= 0)
			return;

		transformable.rotate(fromAngle + (toAngle - fromAngle) * comp, axisX, axisY, axisZ, offsetX, offsetY, offsetZ);
		//MalisisCore.message(fromAngle + (toAngle - fromAngle) * comp);
	}

	@Override
	public Rotation reversed(boolean reversed)
	{
		if (!reversed)
			return this;

		float tmpAngle = fromAngle;
		fromAngle = toAngle;
		toAngle = tmpAngle;
		return this;
	}
}