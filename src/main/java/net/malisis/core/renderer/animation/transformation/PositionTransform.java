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

import net.malisis.core.renderer.animation.transformation.ITransformable.Position;

/**
 *
 * @author Ordinastie
 * @param <T> the generic type of the Transformable
 */
public class PositionTransform<T> extends Transformation<PositionTransform, ITransformable.Position<T>>
{
	/** Starting position. */
	protected int fromX, fromY;
	/** Target position. */
	protected int toX, toY;

	/**
	 * Instantiates a new {@link PositionTransform}.
	 *
	 * @param fromX the from x
	 * @param fromY the from y
	 * @param toX the to x
	 * @param toY the to y
	 */
	public PositionTransform(int fromX, int fromY, int toX, int toY)
	{
		from(fromX, fromY);
		to(toX, toY);
	}

	/**
	 * Sets the starting position.
	 *
	 * @param x the x
	 * @param y the y
	 * @return this {@link PositionTransform}
	 */
	public PositionTransform<T> from(int x, int y)
	{
		fromX = x;
		fromY = y;
		return this;
	}

	/**
	 * Sets the target position
	 *
	 * @param x the x
	 * @param y the y
	 * @return this {@link PositionTransform}
	 */
	public PositionTransform<T> to(int x, int y)
	{
		toX = x;
		toY = y;
		return this;
	}

	/**
	 * Calculates the transformation
	 *
	 * @param transformable the transformable
	 * @param comp the comp
	 */
	@Override
	protected void doTransform(Position<T> transformable, float comp)
	{
		int fromX = reversed ? this.toX : this.fromX;
		int toX = reversed ? this.fromX : this.toX;
		int fromY = reversed ? this.toY : this.fromY;
		int toY = reversed ? this.fromY : this.toY;

		int x = Math.round(fromX + (toX - fromX) * comp);
		int y = Math.round(fromY + (toY - fromY) * comp);

		transformable.setPosition(x, y);
	}
}
