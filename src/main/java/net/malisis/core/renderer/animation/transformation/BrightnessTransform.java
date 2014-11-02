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
public class BrightnessTransform extends Transformation<BrightnessTransform, ITransformable.Brightness>
{
	private boolean reversed;
	protected int fromBrightness;
	protected int toBrightness;

	public BrightnessTransform(int fromBrightness, int toBrightness)
	{
		this.fromBrightness = fromBrightness;
		this.toBrightness = toBrightness;
	}

	@Override
	protected void doTransform(ITransformable.Brightness transformable, float comp)
	{
		if (comp <= 0)
			return;

		float from = reversed ? toBrightness : fromBrightness;
		float to = reversed ? fromBrightness : toBrightness;
		transformable.setBrightness((int) (from + (to - from) * comp));
	}

	@Override
	public BrightnessTransform reversed(boolean reversed)
	{
		this.reversed = reversed;
		return this;
	}

}
