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

import java.util.Collection;

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.event.ComponentEvent.ValueChange;

/**
 * @author Ordinastie
 *
 */
public abstract class UIListContainer<T extends UIListContainer, S> extends UIComponent<T>
{
	protected int elementSpacing = 0;
	protected boolean unselect = true;
	protected Collection<S> elements;
	protected S hovered;
	protected S selected;
	protected S current;

	public UIListContainer(MalisisGui gui)
	{
		super(gui);
	}

	public UIListContainer(MalisisGui gui, int width, int height)
	{
		super(gui);
		setSize(width, height);
	}

	public void setElements(Collection<S> elements)
	{
		this.elements = elements;
	}

	public Iterable<S> getElements()
	{
		return elements;
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

	public void setSelected(S comp)
	{
		selected = comp;
	}

	public S getSelected()
	{
		return selected;
	}

	public boolean isSelected(S element)
	{
		return element == selected;
	}

	public S select(S element)
	{
		if (!fireEvent(new SelectEvent<S>(this, element)))
			return getSelected();

		setSelected(element);
		return element;
	}

	@Override
	public UIComponent getComponentAt(int x, int y)
	{
		hovered = null;
		UIComponent c = super.getComponentAt(x, y);
		if (c != this)
			return c;

		int ey = 0;
		int cy = relativeY(y);
		for (S element : elements)
		{
			int h = getElementHeight(element);
			if (ey + h > cy)
			{
				hovered = element;
				return this;
			}
			ey += h;
		}

		return this;
	}

	@Override
	public boolean onClick(int x, int y)
	{
		if (!canUnselect())
		{
			if (hovered == null || hovered == getSelected())
				return super.onClick(x, y);
		}

		if (hovered == getSelected())
			hovered = null;
		select(hovered);

		return true;
	}

	@Override
	public void draw(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		if (elements.size() == 0)
		{
			drawEmtpy(renderer, mouseX, mouseY, partialTick);
			return;
		}

		int bk = y;
		for (S element : elements)
		{
			current = element;
			super.draw(renderer, mouseX, mouseY, partialTick);
			y += getElementHeight(element) + elementSpacing;
		}

		y = bk;
	}

	public abstract int getElementHeight(S element);

	public abstract void drawEmtpy(GuiRenderer renderer, int mouseX, int mouseY, float partialTick);

	/**
	 * Event fired when a {@link UIListContainer} changes its selected {@link IListElement}.<br>
	 * When catching the event, the state is not applied to the {@code UISelect} yet.<br>
	 * Cancelling the event will prevent the {@code Option} to be set for the {@code UISelect} .
	 */
	public static class SelectEvent<T> extends ValueChange<UIListContainer, T>
	{
		public SelectEvent(UIListContainer component, T selected)
		{
			super(component, (T) component.getSelected(), selected);
		}

		/**
		 * Gets the new {@link IListElement} to be set.
		 *
		 * @return the new option
		 */
		public T getSelected()
		{
			return newValue;
		}
	}

}
