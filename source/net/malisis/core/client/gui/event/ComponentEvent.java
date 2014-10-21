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
 * Base for events fired by {@link UIComponent}.
 *
 * @author Ordinastie, PaleoCrafter
 */
public abstract class ComponentEvent<T extends UIComponent> extends GuiEvent
{
	protected T component;

	public ComponentEvent(T component)
	{
		this.component = component;
	}

	/**
	 * @return the {@link UIComponent} that fired this {@link ComponentEvent}
	 */
	public T getComponent()
	{
		return component;
	}

	/**
	 * Fired when a {@link UIComponent} changes state.
	 *
	 * @author Ordinastie
	 *
	 * @param <T> the type of <code>UIComponent</code> that fired this event.
	 */
	public abstract static class StateChanged<T extends UIComponent> extends ComponentEvent<T>
	{
		protected boolean state;

		public StateChanged(T component, boolean state)
		{
			super(component);
			this.state = state;
		}

		/**
		 * @return the new state for the {@link UIComponent} that fired this {@link StateChanged} event.
		 */
		public boolean getState()
		{
			return state;
		}
	}

	/**
	 * Fired when a {@link UIComponent} gets hovered.
	 *
	 * @author Ordinastie
	 *
	 * @param <T> the type of <code>UIComponent</code> that fired this event.
	 */
	public static class HoveredStateChanged<T extends UIComponent> extends StateChanged<T>
	{
		public HoveredStateChanged(T component, boolean hovered)
		{
			super(component, hovered);
		}
	}

	/**
	 * Fired when a {@link UIComponent} gets focused.
	 *
	 * @author Ordinastie
	 *
	 * @param <T> the type of <code>UIComponent</code> that fired this event.
	 */
	public static class FocusStateChanged<T extends UIComponent> extends StateChanged<T>
	{
		public FocusStateChanged(T component, boolean focused)
		{
			super(component, focused);
		}
	}

	/**
	 * Fired when a {@link UIComponent} gets activated.
	 *
	 * @author Ordinastie
	 *
	 * @param <T> the type of <code>UIComponent</code> that fired this event.
	 */
	public static class ActiveStateChanged<T extends UIComponent> extends StateChanged<T>
	{
		public ActiveStateChanged(T component, boolean active)
		{
			super(component, active);
		}
	}

	/**
	 * Fired when a {@link UIComponent} gets it's value changed.
	 *
	 * @author Ordinastie
	 *
	 * @param <T> the type of <code>UIComponent</code> that fired this event.
	 * @param <S> the type of the value being changed.
	 */
	public static class ValueChanged<T extends UIComponent, S> extends ComponentEvent<T>
	{
		protected S oldValue;
		protected S newValue;

		public ValueChanged(T component, S oldValue, S newValue)
		{
			super(component);
			this.oldValue = oldValue;
			this.newValue = newValue;
		}

		/**
		 * @return the value being changed for the {@link UIComponent}
		 */
		public S getOldValue()
		{
			return oldValue;
		}

		/**
		 * @return the being set for the {@link UIComponent}
		 */
		public S getNewValue()
		{
			return newValue;
		}
	}
}
