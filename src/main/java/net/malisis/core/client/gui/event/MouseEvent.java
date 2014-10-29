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

package net.malisis.core.client.gui.event;

import net.malisis.core.util.MouseButton;

public abstract class MouseEvent extends GuiEvent
{
	protected int x;
	protected int y;

	protected MouseEvent(int x, int y)
	{
		this.x = x;
		this.y = y;
	}

	public int getX()
	{
		return x;
	}

	public int getY()
	{
		return y;
	}

	public abstract static class ButtonStateEvent extends MouseEvent
	{
		private final int button;

		public ButtonStateEvent(int x, int y, int button)
		{
			super(x, y);
			this.button = button;
		}

		public int getButtonCode()
		{
			return button;
		}

		public MouseButton getButton()
		{
			return MouseButton.getButton(button);
		}
	}

	public abstract static class MovementEvent extends MouseEvent
	{
		private final int lastPositionX;
		private final int lastPositionY;

		public MovementEvent(int lastPosX, int lastPosY, int newPosX, int newPosY)
		{
			super(newPosX, newPosY);
			this.lastPositionX = lastPosX;
			this.lastPositionY = lastPosY;
		}

		public int getLastPositionX()
		{
			return lastPositionX;
		}

		public int getLastPositionY()
		{
			return lastPositionY;
		}

		public int getDeltaX()
		{
			return x - lastPositionX;
		}

		public int getDeltaY()
		{
			return y - lastPositionY;
		}
	}

	public static class Press extends ButtonStateEvent
	{
		public Press(int x, int y, int button)
		{
			super(x, y, button);
		}
	}

	public static class Release extends ButtonStateEvent
	{
		public Release(int x, int y, int button)
		{
			super(x, y, button);
		}
	}

	public static class DoubleClick extends ButtonStateEvent
	{
		public DoubleClick(int x, int y, int button)
		{
			super(x, y, button);
		}
	}

	public static class Move extends MovementEvent
	{
		public Move(int lastPosX, int lastPosY, int newPosX, int newPosY)
		{
			super(lastPosX, lastPosY, newPosX, newPosY);
		}
	}

	public static class Drag extends MovementEvent
	{
		private final int button;

		public Drag(int lastPosX, int lastPosY, int newPosX, int newPosY, int button)
		{
			super(lastPosX, lastPosY, newPosX, newPosY);
			this.button = button;
		}

		public int getButtonCode()
		{
			return button;
		}

		public MouseButton getButton()
		{
			return MouseButton.getButton(button);
		}
	}

	public static class ScrollWheel extends MouseEvent
	{
		private final int delta;

		public ScrollWheel(int x, int y, int delta)
		{
			super(x, y);
			this.delta = delta;
		}

		public int getDelta()
		{
			return delta;
		}
	}

}
