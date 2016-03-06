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
	/** Starting brightness. */
	protected int fromBrightness;
	/** Target brightness. */
	protected int toBrightness;

	/**
	 * Instantiates a new {@link BrightnessTransform}.
	 *
	 * @param fromBrightness the from brightness
	 * @param toBrightness the to brightness
	 */
	public BrightnessTransform(int fromBrightness, int toBrightness)
	{
		this.fromBrightness = fromBrightness;
		this.toBrightness = toBrightness;
	}

	/**
	 * Gets this {@link BrightnessTransform}.
	 *
	 * @return the brightness transform
	 */
	@Override
	public BrightnessTransform self()
	{
		return this;
	}

	/**
	 * Sets the starting brightness for this {@link BrightnessTransform}.
	 *
	 * @param brightness the brightness
	 * @return the brightness transform
	 */
	public BrightnessTransform from(int brightness)
	{
		this.fromBrightness = brightness;
		return this;
	}

	/**
	 * Sets the target brightness for this {@link BrightnessTransform}.
	 *
	 * @param brightness the brightness
	 * @return the brightness transform
	 */
	public BrightnessTransform to(int brightness)
	{
		this.toBrightness = brightness;
		return this;
	}

	/**
	 * Calculates the transformation.
	 *
	 * @param transformable the transformable
	 * @param comp the comp
	 */
	@Override
	protected void doTransform(ITransformable.Brightness transformable, float comp)
	{
		if (comp <= 0)
			return;

		float from = reversed ? toBrightness : fromBrightness;
		float to = reversed ? fromBrightness : toBrightness;

		transformable.setBrightness((int) (from + (to - from) * comp));
	}
}
