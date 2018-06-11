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

import org.lwjgl.input.Mouse;

import net.malisis.core.client.gui.element.position.Position;
import net.malisis.core.client.gui.element.position.Position.IPosition;

/**
 * @author Ordinastie
 *
 */
public class MousePosition implements IPosition
{
	private int x = 0;
	private int y = 0;
	/** Last known position of the mouse. */
	protected int xPrevious, yPrevious;

	public void udpate(MalisisGui gui)
	{
		//if we ignore scaling, use real mouse position on screen
		if (gui.renderer.isIgnoreScale())
		{
			x = Mouse.getX();
			y = gui.height - Mouse.getY() - 1;
		}
		else
		{
			x = Mouse.getX() * gui.width / gui.mc.displayWidth;
			y = gui.height - Mouse.getY() * gui.height / gui.mc.displayHeight - 1;
		}
	}

	@Override
	public int y()
	{
		return y;
	}

	@Override
	public int x()
	{
		return x;
	}

	public boolean hasChanged()
	{
		return x != xPrevious || y != yPrevious;
	}

	public IPosition previous()
	{
		return Position.of(x, y);
	}

	public IPosition dragged()
	{
		return Position.of(x - xPrevious, y - yPrevious);
	}

	@Override
	public String toString()
	{
		return x() + "," + y();
	}
}
