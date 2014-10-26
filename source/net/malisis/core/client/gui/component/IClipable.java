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
 * ICipable indicate an object (usually {@link UIComponent}) that they should provide a ClipArea.<br>
 * That area will be use to clip content with glScissor
 *
 * @author Ordinastie
 *
 */
public interface IClipable
{
	/**
	 * @return the {@link ClipArea} to be used for glScissor.
	 */
	public ClipArea getClipArea();

	/**
	 * Sets whether this {@link IClipable} should clip or not.
	 */
	public void setClipContent(boolean clip);

	/**
	 * @return whether this {@link IClipable} should clip or not.
	 */
	public boolean shouldClipContent();

	/**
	 * Gets the X position on the screen. See {@link UIComponent#screenX()}.
	 *
	 * @return
	 */
	public int screenX();

	/**
	 * Gets the Y position on the screen. See {@link UIComponent#screenY()}.
	 *
	 * @return
	 */
	public int screenY();

	/**
	 * Gets the with of this {@link IClipable}. See {@link UIComponent#getWidth()}.
	 *
	 * @return
	 */
	public int getWidth();

	/**
	 * Gets the with of this {@link IClipable}. See {@link UIComponent#getHeight()}.
	 *
	 * @return
	 */
	public int getHeight();

	/**
	 * @return the parent {@link UIComponent} of this {@link IClipable}.
	 */
	public UIComponent getParent();
}
