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

package net.malisis.core.client.gui;

import javax.annotation.Nonnull;

/**
 * @author Ordinastie
 *
 */
public class Padding
{
	public static final Padding NO_PADDING = Padding.of(0);

	public final int top;
	public final int bottom;
	public final int left;
	public final int right;

	public final int horizontal;
	public final int vertical;

	private Padding(int top, int bottom, int left, int right)
	{
		this.top = top;
		this.bottom = bottom;
		this.left = left;
		this.right = right;

		horizontal = left + right;
		vertical = top + bottom;
	}

	public static Padding of(int padding)
	{
		return new Padding(padding, padding, padding, padding);
	}

	public static Padding of(int horizontal, int vertical)
	{
		return new Padding(vertical, vertical, horizontal, horizontal);
	}

	public static Padding of(int top, int bottom, int left, int right)
	{
		return new Padding(top, bottom, left, right);
	}

	public interface IPadding
	{
		@Nonnull
		public default Padding getPadding()
		{
			return NO_PADDING;
		}
	}
}
