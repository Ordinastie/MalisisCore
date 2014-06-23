/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 PaleoCrafter, Ordinastie
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

import net.malisis.core.client.gui.component.UIComponent;

/**
 * ComponentEvent
 * 
 * @author PaleoCrafter
 */
public abstract class ComponentEvent<T extends UIComponent> extends GuiEvent
{
	private T component;

	public ComponentEvent(T component)
	{
		this.component = component;
	}

	public T getComponent()
	{
		return component;
	}

	public abstract static class StateChanged<T extends UIComponent> extends ComponentEvent<T>
	{
		private boolean state;

		public StateChanged(T component, boolean state)
		{
			super(component);
			this.state = state;
		}

		public boolean getState()
		{
			return state;
		}
	}

	public static class HoveredStateChanged<T extends UIComponent> extends StateChanged<T>
	{
		public HoveredStateChanged(T component, boolean hovered)
		{
			super(component, hovered);
		}
	}

	public static class FocusStateChanged<T extends UIComponent> extends StateChanged<T>
	{
		public FocusStateChanged(T component, boolean focused)
		{
			super(component, focused);
		}
	}

	public static class ValueChanged<T extends UIComponent, S> extends ComponentEvent<T>
	{
		private S oldValue;
		private S newValue;

		public ValueChanged(T component, S oldValue, S newValue)
		{
			super(component);
			this.oldValue = oldValue;
			this.newValue = newValue;
		}

		public S getOldValue()
		{
			return oldValue;
		}

		public S getNewValue()
		{
			return newValue;
		}
	}
}
