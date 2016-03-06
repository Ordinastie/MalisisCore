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

package net.malisis.core.client.gui.event.component;

import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.event.ComponentEvent;

/**
 * Fired when a {@link UIComponent} has changed its size or position.
 *
 * @author Ordinastie
 *
 * @param <T> the type of <code>UIComponent</code> that fired this event.
 */
public abstract class SpaceChangeEvent<T extends UIComponent<T>> extends ComponentEvent<T>
{
	public SpaceChangeEvent(T component)
	{
		super(component);
	}

	/**
	 * Fired when a {@link UIComponent} changes it's position.
	 *
	 * @author Ordinastie
	 *
	 * @param <T> the type of <code>UIComponent</code> that fired this event.
	 */
	public static class PositionChangeEvent<T extends UIComponent<T>> extends SpaceChangeEvent<T>
	{
		protected int newX;
		protected int newY;
		protected int newAnchor;

		public PositionChangeEvent(T component, int newX, int newY, int newAnchor)
		{
			super(component);
			this.newX = newX;
			this.newY = newY;
			this.newAnchor = newAnchor;
		}

		/**
		 * @return the new X position for the {@link UIComponent}.
		 */
		public int getNewX()
		{
			return newX;
		}

		/**
		 * @return the new Y position for the {@link UIComponent}.
		 */
		public int getNewY()
		{
			return newY;
		}

		/**
		 * @return the new anchor position for the {@link UIComponent}.
		 */
		public int getNewAnchor()
		{
			return newAnchor;
		}
	}

	/**
	 * Fired when a {@link UIComponent} changes its size.
	 *
	 * @author Ordinastie
	 *
	 * @param <T> the type of <code>UIComponent</code> that fired this event.
	 */
	public static class SizeChangeEvent<T extends UIComponent<T>> extends SpaceChangeEvent<T>
	{
		protected int newWidth;
		protected int newHeight;

		public SizeChangeEvent(T component, int newWidth, int newHeight)
		{
			super(component);
		}

		/**
		 * @return the new width for the {@link UIComponent}.
		 */
		public int getNewWidth()
		{
			return newWidth;
		}

		/**
		 * @return the new height for the {@link UIComponent}.
		 */
		public int getNewHeight()
		{
			return newHeight;
		}
	}

}