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

import net.malisis.core.client.gui.ClipArea;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.component.IClipable;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.control.IScrollable;
import net.malisis.core.client.gui.component.control.UIScrollBar;
import net.malisis.core.client.gui.component.control.UIScrollBar.Type;
import net.malisis.core.client.gui.component.control.UIWindowScrollbar;
import net.malisis.core.client.gui.event.ComponentEvent.ValueChange;
import net.malisis.core.client.gui.event.component.ContentUpdateEvent;
import net.minecraft.client.gui.GuiScreen;

/**
 * @author Ordinastie
 *
 */
public abstract class UIListContainer<T extends UIListContainer<T, S>, S> extends UIComponent<T> implements IScrollable, IClipable
{
	protected int elementSpacing = 0;
	protected boolean unselect = true;
	protected Collection<S> elements;
	protected S selected;
	protected int lastSize = 0;

	//IScrollable
	/** Vertical Scrollbar. */
	protected UIWindowScrollbar scrollbar;
	/** Y Offset for the contents of this {@link UIListContainer}. */
	protected int yOffset;

	public UIListContainer()
	{
		scrollbar = new UIWindowScrollbar(self(), UIScrollBar.Type.VERTICAL);
		scrollbar.setAutoHide(true);
	}

	public UIListContainer(int width, int height)
	{
		setSize(width, height);
	}

	@Override
	public T setSize(int width, int height)
	{
		super.setSize(width, height);
		scrollbar.updateScrollbar();
		return self();
	}

	public void setElements(Collection<S> elements)
	{
		this.elements = elements;
		fireEvent(new ContentUpdateEvent<>(self()));
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
		if (!fireEvent(new SelectEvent<>(self(), element)))
			return getSelected();

		setSelected(element);
		return element;
	}

	//#region IClipable
	/**
	 * Gets the {@link ClipArea}.
	 *
	 * @return the clip area
	 */
	@Override
	public ClipArea getClipArea()
	{
		return new ClipArea(this);
	}

	/**
	 * Sets whether this {@link UIContainer} should clip its contents
	 *
	 * @param clipContent if true, clip contents
	 */
	@Override
	public void setClipContent(boolean clipContent)
	{}

	/**
	 * Checks whether this {@link UIContainer} should clip its contents
	 *
	 * @return true, if should clip contents
	 */
	@Override
	public boolean shouldClipContent()
	{
		return true;
	}

	//#end IClipable

	//#region IScrollable
	@Override
	public int getContentWidth()
	{
		UIScrollBar<?> sb = UIScrollBar.getScrollbar(this, Type.VERTICAL);
		if (sb != null && sb.isVisible())
			return getWidth() - sb.getWidth() - 2 - getHorizontalPadding() * 2;

		return getWidth();
	}

	@Override
	public int getContentHeight()
	{
		if (elements == null || elements.size() == 0)
			return 0;

		int height = 0;
		for (S element : elements)
			height += getElementHeight(element) + elementSpacing;

		UIScrollBar<?> sb = UIScrollBar.getScrollbar(this, Type.HORIZONTAL);
		if (sb != null && sb.isVisible())
			return getWidth() - sb.getWidth() - 2 - getHorizontalPadding() * 2;

		return height;
	}

	@Override
	public float getOffsetX()
	{
		return 0;
	}

	@Override
	public void setOffsetX(float offsetX, int delta)
	{}

	@Override
	public float getOffsetY()
	{
		return (float) yOffset / (getContentHeight() - getHeight());
	}

	@Override
	public void setOffsetY(float offsetY, int delta)
	{
		yOffset = (int) ((getContentHeight() - getHeight() + delta) * offsetY);
	}

	@Override
	public float getScrollStep()
	{
		return (GuiScreen.isCtrlKeyDown() ? 0.125F : 0.025F);
	}

	/**
	 * Gets the horizontal padding.
	 *
	 * @return horizontal padding of this {@link UIContainer}.
	 */
	@Override
	public int getHorizontalPadding()
	{
		return 0;
	}

	/**
	 * Gets the vertical padding.
	 *
	 * @return horizontal padding of this {@link UIContainer}.
	 */
	@Override
	public int getVerticalPadding()
	{
		return 0;
	}

	//#end IScrollable

	public S getElementAt(int x, int y)
	{
		if (!isHovered())
			return null;
		int ey = 0;
		int cy = relativeY(y) + yOffset;
		for (S element : elements)
		{
			int h = getElementHeight(element) + elementSpacing;
			if (ey + h > cy)
				return element;
			ey += h;
		}

		return null;
	}

	@Override
	public boolean onClick(int x, int y)
	{
		S element = getElementAt(x, y);
		if (!canUnselect() && (element == null || isSelected(element)))
			return super.onClick(x, y);

		select(isSelected(element) ? null : element);
		return true;
	}

	@Override
	public void draw(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		if (lastSize != elements.size())
		{
			scrollbar.updateScrollbar();
			lastSize = elements.size();
		}

		super.draw(renderer, mouseX, mouseY, partialTick);
	}

	@Override
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{}

	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		drawElements(renderer, mouseX, mouseY, partialTick);
	}

	public void drawElements(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		if (elements == null || elements.size() == 0)
		{
			drawEmpty(renderer, mouseX, mouseY, partialTick);
			return;
		}

		S hoveredElement = getElementAt(mouseX, mouseY);

		int bk = y;
		y -= yOffset;
		for (S element : elements)
		{
			drawElementBackground(renderer, mouseX, mouseY, partialTick, element, hoveredElement == element);
			drawElementForeground(renderer, mouseX, mouseY, partialTick, element, hoveredElement == element);
			y += getElementHeight(element) + elementSpacing;
		}

		y = bk;

	}

	public void drawEmpty(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		renderer.drawText("No element");
	}

	public abstract int getElementHeight(S element);

	public abstract void drawElementBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick, S element, boolean isHovered);

	public abstract void drawElementForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick, S element, boolean isHovered);

	/**
	 * Event fired when a {@link UIListContainer} changes its selected element.<br>
	 * Cancelling the event will prevent the element to be selected.
	 */
	public static class SelectEvent<T extends UIListContainer<T, S>, S> extends ValueChange<T, S>
	{
		public SelectEvent(T component, S selected)
		{
			super(component, component.getSelected(), selected);
		}

		/**
		 * Gets the new element to be set.
		 *
		 * @return the new option
		 */
		public S getSelected()
		{
			return newValue;
		}
	}

}
