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

package net.malisis.core.client.gui;

import net.malisis.core.client.gui.component.IClipable;

public class ClipArea
{
	public boolean noClip = false;
	public int x;
	public int y;
	public int X;
	public int Y;

	public ClipArea(IClipable container)
	{
		this(container, 0, true);
	}

	public ClipArea(IClipable container, int clipPadding)
	{
		this(container, clipPadding, true);
	}

	public ClipArea(IClipable container, int clipPadding, boolean intersect)
	{
		this(container, container.screenX() + clipPadding, container.screenY() + clipPadding, container.screenX() + container.getWidth()
				- clipPadding, container.screenY() + container.getHeight() - clipPadding, intersect);
	}

	public ClipArea(IClipable container, int x, int y, int X, int Y, boolean intersect)
	{
		if (!container.shouldClipContent())
			this.noClip = true;
		else
		{
			this.x = x;
			this.y = y;
			this.X = X;
			this.Y = Y;
		}

		if (intersect && container.getParent() instanceof IClipable)
			this.intersect(((IClipable) container.getParent()).getClipArea());
	}

	public void intersect(ClipArea area)
	{
		if (this.noClip)
		{
			x = area.x;
			y = area.y;
			X = area.X;
			Y = area.Y;
		}
		else if (!area.noClip)
		{
			x = Math.max(x, area.x);
			y = Math.max(y, area.y);
			X = Math.min(X, area.X);
			Y = Math.min(Y, area.Y);
		}
	}

	public int width()
	{
		return X - x;
	}

	public int height()
	{
		return Y - y;
	}

	@Override
	public String toString()
	{
		return x + "->" + X + " , " + y + "->" + Y + " (" + width() + "," + height() + ")";
	}

}
