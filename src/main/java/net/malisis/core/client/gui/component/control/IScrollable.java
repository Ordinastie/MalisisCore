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

package net.malisis.core.client.gui.component.control;

import net.malisis.core.client.gui.Padding.IPadding;
import net.malisis.core.client.gui.component.UIComponent;

/**
 * This interface allows scrollbars to be added to the {@link UIComponent} implementer.
 *
 * @author Ordinastie
 *
 */
public interface IScrollable extends IPadding
{
	/**
	 * Gets the width of the scrollable content. It should count the total content size to be displayed when not clipped.
	 *
	 * @return the content width
	 */
	public int getContentWidth();

	/**
	 * Gets the height of the scrollable content. It should count the total content size to be displayed when not clipped.
	 *
	 * @return the content height
	 */
	public int getContentHeight();

	/**
	 * Gets the offset from 0 to 1 of the scrollable content. Only used for {@link UIScrollBar.Type#HORIZONTAL} scrollbars.
	 *
	 * @return the offset x
	 */
	public float getOffsetX();

	/**
	 * Sets the offset from 0 to 1 of the scrollable content. Only used for {@link UIScrollBar.Type#HORIZONTAL} scrollbars.<br>
	 * Delta is the size taken for the {@link UIScrollBar.Type#VERTICAL} scrollbar if available.
	 *
	 * @param offsetX the offset x
	 * @param delta the delta
	 */
	public void setOffsetX(float offsetX, int delta);

	/**
	 * Gets the offset from 0 to 1 of the scrollable content. Only used for {@link UIScrollBar.Type#VERTICAL} scrollbars.
	 *
	 * @return the offset x
	 */
	public float getOffsetY();

	/**
	 * Sets the offset from 0 to 1 of the scrollable content. Only used for {@link UIScrollBar.Type#VERTICAL} scrollbars.<br>
	 * Delta is the size taken for the {@link UIScrollBar.Type#HORIZONTAL} scrollbar if available.
	 *
	 * @param offsetY the offset y
	 * @param delta the delta
	 */
	public void setOffsetY(float offsetY, int delta);

	/**
	 * Gets the amount of scrolling from 0 to 1, done by one step of the scroll wheel.
	 *
	 * @return the scroll step
	 */
	public float getScrollStep();
}
