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

import net.malisis.core.renderer.RenderParameters;

/**
 * @author Ordinastie
 *
 */
public class ColorTransform extends Transformation<ColorTransform, RenderParameters>
{
	protected int fromColor;
	protected int toColor;
	protected boolean relative = false;

	public ColorTransform(int toColor)
	{
		this.toColor = toColor;
		this.relative = true;
	}

	public ColorTransform(int fromColor, int toColor)
	{
		this.fromColor = fromColor;
		this.toColor = toColor;
	}

	private int red(int color)
	{
		return (color >> 16) & 0xFF;
	}

	private int green(int color)
	{
		return (color >> 8) & 0xFF;
	}

	private int blue(int color)
	{
		return color & 0xFF;
	}

	@Override
	protected void doTransform(RenderParameters rp, float comp)
	{
		if (comp <= 0)
			return;

		int fromColor = relative ? rp.colorMultiplier.get() : this.fromColor;
		int r = (int) (red(fromColor) + (red(toColor) - red(fromColor)) * comp);
		int g = (int) (green(fromColor) + (green(toColor) - green(fromColor)) * comp);
		int b = (int) (blue(fromColor) + (blue(toColor) - blue(fromColor)) * comp);

		rp.colorMultiplier.set((r & 0xFF) << 16 | (g & 0xFF) << 8 | b & 0xFF);
	}

	@Override
	public ColorTransform reversed(boolean reversed)
	{
		if (!reversed)
			return this;

		int tmpColor = fromColor;
		fromColor = toColor;
		toColor = tmpColor;

		return this;

	}

}
