/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 Ordinastie
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

package net.malisis.core.client.gui.component.element;

import javax.annotation.Nonnull;

import net.malisis.core.client.gui.component.UIComponent;

/**
 * @author Ordinastie
 *
 */
public interface Padding
{
	public static final Padding NO_PADDING = new FixedPadding(0, 0, 0, 0);

	/**
	 * Left padding.
	 *
	 * @return the left padding
	 */
	public int left();

	/**
	 * Right padding.
	 *
	 * @return the right padding
	 */
	public int right();

	/**
	 * Top padding.
	 *
	 * @return the top padding.
	 */
	public int top();

	/**
	 * Bottom padding.
	 *
	 * @return the bottom padding.
	 */
	public int bottom();

	/**
	 * Horizontal padding.
	 *
	 * @return the horizontal padding
	 */
	public default int horizontal()
	{
		return left() + right();
	}

	/**
	 * Vertical padding
	 *
	 * @return the vertical padding
	 */
	public default int vertical()
	{
		return top() + bottom();
	}

	public static class FixedPadding implements Padding
	{
		protected final int top, bottom, left, right;

		private FixedPadding(int top, int bottom, int left, int right)
		{
			this.top = top;
			this.bottom = bottom;
			this.left = left;
			this.right = right;
		}

		@Override
		public int left()
		{
			return left;
		}

		@Override
		public int right()
		{
			return right;
		}

		@Override
		public int bottom()
		{
			return bottom;
		}

		@Override
		public int top()
		{
			return top;
		}
	}

	public static Padding of(int padding)
	{
		return Padding.of(padding, padding, padding, padding);
	}

	public static Padding of(int horizontal, int vertical)
	{
		return Padding.of(vertical, vertical, horizontal, horizontal);
	}

	public static Padding of(int top, int bottom, int left, int right)
	{
		return top == 0 && bottom == 0 && left == 0 && right == 0 ? NO_PADDING : new FixedPadding(top, bottom, left, right);
	}

	public static Padding of(UIComponent<?> component)
	{
		return component instanceof IPadded ? ((IPadded) component).getPadding() : Padding.NO_PADDING;
	}

	/**
	 * IPadded defines an object that provides a Padding element.
	 */
	public interface IPadded
	{
		@Nonnull
		public default Padding getPadding()
		{
			return NO_PADDING;
		}
	}
}
