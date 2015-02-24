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

package net.malisis.core.client.gui.component.container;

import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.event.ComponentEvent.ValueChange;
import net.malisis.core.client.gui.event.MouseEvent;

import com.google.common.eventbus.Subscribe;

/**
 * @author Ordinastie
 *
 */
public class UIListContainer extends UIContainer<UIListContainer>
{
	private int elementHeight = 0;
	private int elementSpacing = 0;
	private int elementY = 0;
	private UIComponent selected;
	private boolean unselect = true;

	public UIListContainer(MalisisGui gui)
	{
		super(gui);
	}

	public UIListContainer(MalisisGui gui, int width, int height)
	{
		super(gui, width, height);
	}

	public void setFixedHeight(int height)
	{
		elementHeight = height;
	}

	public void setVariableHeight()
	{
		elementHeight = 0;
	}

	public void setElementSpacing(int elementSpacing)
	{
		this.elementSpacing = elementSpacing;
	}

	public boolean canUnselect()
	{
		return unselect;
	}

	public void setUnselect(boolean unselect)
	{
		this.unselect = unselect;
	}

	private void calculateElementY()
	{
		elementY = 0;
		for (UIComponent c : components)
			elementY += (elementHeight == 0 ? c.getHeight() : elementHeight) + elementSpacing;
	}

	public void setSelected(UIComponent comp)
	{
		if (comp != null && comp.getParent() != this)
			return;

		selected = comp;
	}

	public UIComponent getSelected()
	{
		return selected;
	}

	public boolean isSelected(UIComponent comp)
	{
		return comp == selected;
	}

	public UIComponent select(UIComponent comp)
	{
		if (!fireEvent(new SelectEvent(this, comp)))
			return getSelected();

		setSelected(comp);
		return comp;
	}

	@Override
	public void remove(UIComponent component)
	{
		super.remove(component);
		calculateElementY();
	}

	@Override
	public void removeAll()
	{
		super.removeAll();
		calculateElementY();
	}

	@Override
	public void add(UIComponent... components)
	{
		for (UIComponent c : components)
		{
			c.setPosition(c.getX(), (elementY) * this.components.size(), c.getAnchor());
			super.add(c);
			elementY = (elementHeight == 0 ? c.getHeight() : elementHeight) + elementSpacing;
		}
	}

	@Subscribe
	public void onClick(MouseEvent.Press event)
	{
		UIComponent component = getComponentAt(event.getX(), event.getY());
		if (!canUnselect())
		{
			if (component == null || component == getSelected())
				return;
		}

		if (component == getSelected() || component == this)
			component = null;
		select(component);
	}

	/**
	 * Event fired when a {@link UIListContainer} changes its selected {@link IListElement}.<br>
	 * When catching the event, the state is not applied to the {@code UISelect} yet.<br>
	 * Cancelling the event will prevent the {@code Option} to be set for the {@code UISelect} .
	 */
	public static class SelectEvent extends ValueChange<UIListContainer, UIComponent>
	{
		public SelectEvent(UIListContainer component, UIComponent selected)
		{
			super(component, component.getSelected(), selected);
		}

		/**
		 * Gets the new {@link IListElement} to be set.
		 *
		 * @return the new option
		 */
		public UIComponent getSelected()
		{
			return newValue;
		}
	}

}
