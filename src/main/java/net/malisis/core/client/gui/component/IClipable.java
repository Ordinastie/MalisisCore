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

package net.malisis.core.client.gui.component;

import net.malisis.core.client.gui.ClipArea;

/**
 * ICipable indicates an object (usually {@link UIComponent}) that they need to provide a ClipArea.<br>
 * That area will be used to clip content with glScissor.
 *
 * @author Ordinastie
 *
 */
public interface IClipable
{
	/**
	 * Gets {@link ClipArea} to be used for glScissor
	 *
	 * @return the clip area.
	 */
	public ClipArea getClipArea();

	/**
	 * Sets whether this {@link IClipable} should clip or not.
	 *
	 * @param clip the new clip content
	 */
	public void setClipContent(boolean clip);

	/**
	 * Checks whether this {@link IClipable} should clip or not.
	 *
	 * @return true, if should clip
	 */
	public boolean shouldClipContent();

	/**
	 * Gets the X position on the screen. See {@link UIComponent#screenX()}.
	 *
	 * @return the coordinate
	 */
	public int screenX();

	/**
	 * Gets the Y position on the screen. See {@link UIComponent#screenY()}.
	 *
	 * @return the coordinate
	 */
	public int screenY();

	/**
	 * Gets the with of this {@link IClipable}. See {@link UIComponent#getWidth()}.
	 *
	 * @return the width
	 */
	public int getWidth();

	/**
	 * Gets the with of this {@link IClipable}. See {@link UIComponent#getHeight()}.
	 *
	 * @return the height
	 */
	public int getHeight();

	/**
	 * Gets the parent {@link UIComponent} of this {@link IClipable}.
	 *
	 * @return the parent
	 */
	public UIComponent getParent();
}
