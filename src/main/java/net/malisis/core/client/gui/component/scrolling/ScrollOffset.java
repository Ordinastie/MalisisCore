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

package net.malisis.core.client.gui.component.scrolling;

import net.malisis.core.client.gui.element.position.Position.IPosition;

/**
 * @author Ordinastie
 *
 */
public class ScrollOffset implements IPosition
{
	public static final ScrollOffset ZERO = new ScrollOffset()
	{
		@Override
		public void xUpdate(int x)
		{}

		@Override
		public void yUpdate(int y)
		{}
	};

	private UIScrollBar vertical;
	private UIScrollBar horizontal;

	private int xOffset = 0;
	private int yOffset = 0;

	public void addScrollbar(UIScrollBar scrollbar)
	{
		if (scrollbar.isHorizontal())
			if (horizontal == null)
				horizontal = scrollbar;
			else
				throw new IllegalArgumentException("Horizontal scrollbar already set for this offset.");
		if (!scrollbar.isHorizontal())
			if (vertical == null)
				vertical = scrollbar;
			else
				throw new IllegalArgumentException("Horizontal scrollbar already set for this offset.");
	}

	public UIScrollBar getVerticalScrollbar()
	{
		return vertical;
	}

	public UIScrollBar getHorizontalScrollbar()
	{
		return horizontal;
	}

	public void xUpdate(int x)
	{
		xOffset = x;
	}

	public void yUpdate(int y)
	{
		yOffset = y;
	}

	@Override
	public int x()
	{
		return xOffset;
	}

	@Override
	public int y()
	{
		return yOffset;
	}

	@Override
	public String toString()
	{
		return x() + "," + y();
	}

}
