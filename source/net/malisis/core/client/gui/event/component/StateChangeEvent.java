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
 * Fired when a {@link UIComponent} changes state.
 *
 * @author Ordinastie
 *
 * @param <T> the type of <code>UIComponent</code> that fired this event.
 */
public abstract class StateChangeEvent<T extends UIComponent> extends ComponentEvent<T>
{
	protected boolean state;

	public StateChangeEvent(T component, boolean state)
	{
		super(component);
		this.state = state;
	}

	/**
	 * @return the new state for the {@link UIComponent} that fired this {@link StateChangeEvent} event.
	 */
	public boolean getState()
	{
		return state;
	}

	/**
	 * Fired when a {@link UIComponent} gets hovered.
	 *
	 * @author Ordinastie
	 *
	 * @param <T> the type of <code>UIComponent</code> that fired this event.
	 */
	public static class HoveredStateChange<T extends UIComponent> extends StateChangeEvent<T>
	{
		public HoveredStateChange(T component, boolean hovered)
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
	public static class FocusStateChange<T extends UIComponent> extends StateChangeEvent<T>
	{
		public FocusStateChange(T component, boolean focused)
		{
			super(component, focused);
		}
	}

	/**
	 * Fired when a {@link UIComponent} gets activated or deactivated.
	 *
	 * @author Ordinastie
	 *
	 * @param <T> the type of <code>UIComponent</code> that fired this event.
	 */
	public static class ActiveStateChange<T extends UIComponent> extends StateChangeEvent<T>
	{
		public ActiveStateChange(T component, boolean active)
		{
			super(component, active);
		}
	}

	/**
	 * Fired when a {@link UIComponent} visibility changes.
	 *
	 * @author Ordinastie
	 *
	 * @param <T> the type of <code>UIComponent</code> that fired this event.
	 */
	public static class VisibleStateChange<T extends UIComponent> extends StateChangeEvent<T>
	{
		public VisibleStateChange(T component, boolean visible)
		{
			super(component, visible);
		}
	}

	/**
	 * Fired when a {@link UIComponent} visibility changes.
	 *
	 * @author Ordinastie
	 *
	 * @param <T> the type of <code>UIComponent</code> that fired this event.
	 */
	public static class DisabledStateChange<T extends UIComponent> extends StateChangeEvent<T>
	{
		public DisabledStateChange(T component, boolean disabled)
		{
			super(component, disabled);
		}
	}
}