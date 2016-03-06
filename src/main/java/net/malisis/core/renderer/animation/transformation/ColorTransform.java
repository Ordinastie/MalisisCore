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

/**
 * @author Ordinastie
 *
 */
public class ColorTransform extends Transformation<ColorTransform, ITransformable.Color>
{
	/** Starting color. */
	protected int fromColor;
	/** Target color. */
	protected int toColor;

	/**
	 * Instantiates a new {@link ColorTransform}.
	 *
	 * @param fromColor the from color
	 * @param toColor the to color
	 */
	public ColorTransform(int fromColor, int toColor)
	{
		this.fromColor = fromColor;
		this.toColor = toColor;
	}

	/**
	 * Gets this {@link ColorTransform}.
	 *
	 * @return the color transform
	 */
	@Override
	public ColorTransform self()
	{
		return this;
	}

	/**
	 * Sets the starting color for this {@link ColorTransform}.
	 *
	 * @param color the color
	 * @return the color transform
	 */
	public ColorTransform from(int color)
	{
		this.fromColor = color;
		return this;
	}

	/**
	 * Sets the target color for this {@link ColorTransform}.
	 *
	 * @param color the color
	 * @return the color transform
	 */
	public ColorTransform to(int color)
	{
		this.toColor = color;
		return this;
	}

	/**
	 * Gets the red component of the color.
	 *
	 * @param color the color
	 * @return the int
	 */
	private int red(int color)
	{
		return (color >> 16) & 0xFF;
	}

	/**
	 * Gets the green component of the color.
	 *
	 * @param color the color
	 * @return the int
	 */
	private int green(int color)
	{
		return (color >> 8) & 0xFF;
	}

	/**
	 * Gets the blue component of the color.
	 *
	 * @param color the color
	 * @return the int
	 */
	private int blue(int color)
	{
		return color & 0xFF;
	}

	/**
	 * Calculates the transformation.
	 *
	 * @param transformable the transformable
	 * @param comp the comp
	 */
	@Override
	protected void doTransform(ITransformable.Color transformable, float comp)
	{
		if (comp <= 0)
			return;

		int from = reversed ? toColor : fromColor;
		int to = reversed ? fromColor : toColor;

		int r = (int) (red(from) + (red(to) - red(from)) * comp);
		int g = (int) (green(from) + (green(to) - green(from)) * comp);
		int b = (int) (blue(from) + (blue(to) - blue(from)) * comp);

		transformable.setColor((r & 0xFF) << 16 | (g & 0xFF) << 8 | b & 0xFF);
	}

}
