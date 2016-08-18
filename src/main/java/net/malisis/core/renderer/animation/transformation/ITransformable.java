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

import net.malisis.core.util.EnumFacingUtils;
import net.minecraft.util.EnumFacing;

/**
 * @author Ordinastie
 *
 */
public interface ITransformable
{
	public static interface Translate extends ITransformable
	{
		public void translate(float x, float y, float z);
	}

	public static interface Rotate extends ITransformable
	{
		public void rotate(float angle, float x, float y, float z, float offsetX, float offsetY, float offsetZ);

		public default void rotate(EnumFacing direction)
		{
			if (direction == EnumFacing.SOUTH)
				return;
			rotate(90 * EnumFacingUtils.getRotationCount(direction), 0, 1, 0, 0, 0, 0);
		}
	}

	public static interface Scale extends ITransformable
	{
		public void scale(float x, float y, float z, float offsetX, float offsetY, float offsetZ);
	}

	public static interface Color extends ITransformable
	{
		public void setColor(int color);
	}

	public static interface Alpha extends ITransformable
	{
		public void setAlpha(int alpha);
	}

	public static interface Brightness extends ITransformable
	{
		public void setBrightness(int brightness);
	}

	public interface Position<T> extends ITransformable
	{
		public T setPosition(int x, int y);
	}

	public interface Size<T> extends ITransformable
	{
		public T setSize(int width, int height);
	}
}
