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

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import net.minecraftforge.common.model.TRSRTransformation;

/**
 * @author Ordinastie
 *
 */
public class TransformBuilder
{
	private Vector3f translation;
	private Quat4f leftRot;
	private Vector3f scale;
	private Quat4f rightRot;

	public TransformBuilder translate(float x, float y, float z)
	{
		translation = new Vector3f(x, y, z);
		return this;
	}

	public TransformBuilder scale(float x, float y, float z)
	{
		scale = new Vector3f(x, y, z);
		return this;
	}

	public TransformBuilder scale(float s)
	{
		scale = new Vector3f(s, s, s);
		return this;
	}

	public TransformBuilder rotate(float x, float y, float z)
	{
		leftRot = TRSRTransformation.quatFromXYZ((float) Math.toRadians(x), (float) Math.toRadians(y), (float) Math.toRadians(z));
		return this;
	}

	public TransformBuilder rotateAfter(float x, float y, float z)
	{
		rightRot = TRSRTransformation.quatFromXYZ((float) Math.toRadians(x), (float) Math.toRadians(y), (float) Math.toRadians(z));
		return this;
	}

	public Matrix4f get()
	{
		return new TRSRTransformation(translation, leftRot, scale, rightRot).getMatrix();
	}
}
