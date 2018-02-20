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
import java.util.Collections;
import java.util.function.BiFunction;

import net.malisis.core.client.gui.ClipArea;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.control.UIScrollBar;
import net.malisis.core.client.gui.component.element.IPosition;
import net.malisis.core.client.gui.component.element.Size;
import net.malisis.core.client.gui.event.ComponentEvent.ValueChange;

/**
 * @author Ordinastie
 *
 */
public class UIListContainer<S> extends UIContainer<UIListContainer<S>>
{
	protected int elementSpacing = 0;
	protected boolean unselect = true;
	protected Collection<S> elements = Collections.emptyList();
	protected S selected;
	protected int lastSize = 0;
	protected BiFunction<MalisisGui, S, UIComponent<?>> elementComponentFactory = DefaultElementComponent::new;

	//IScrollable
	/** Vertical Scrollbar. */
	protected UIScrollBar scrollbar;
	/** Y Offset for the contents of this {@link UIListContainer}. */
	protected int yOffset;

	protected int elementsSize;

	public UIListContainer(MalisisGui gui)
	{
		super(gui);
		scrollbar = new UIScrollBar(gui, self(), UIScrollBar.Type.VERTICAL);
		scrollbar.setAutoHide(true);
	}

	public UIListContainer(MalisisGui gui, Size size)
	{
		this(gui);
		setSize(size);
	}

	protected void buildElementComponents()
	{
		removeAll();

		UIComponent<?> lastComp = null;
		for (S element : elements)
		{
			UIComponent<?> comp = elementComponentFactory.apply(getGui(), element);
			comp.attachData(element);
			comp.setPosition(lastComp != null ? IPosition.builder().below(lastComp, elementSpacing).build() : IPosition.zero());
			add(comp);
			lastComp = comp;
		}
		elementsSize = elements.size();
	}

	public void setElements(Collection<S> elements)
	{
		this.elements = elements != null ? elements : Collections.emptyList();
		buildElementComponents();
	}

	public Iterable<S> getElements()
	{
		return elements;
	}

	public UIListContainer<S> setComponentFactory(BiFunction<MalisisGui, S, UIComponent<?>> factory)
	{
		this.elementComponentFactory = factory;
		return self();
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

	@Override
	public void draw(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		if (elements.size() != elementsSize)
			buildElementComponents();

		super.draw(renderer, mouseX, mouseY, partialTick);
	}

	/**
	 * Event fired when a {@link UIListContainer} changes its selected element.<br>
	 * Canceling the event will prevent the element to be selected.
	 */
	public static class SelectEvent<S> extends ValueChange<UIListContainer<S>, S>
	{
		public SelectEvent(UIListContainer<S> component, S selected)
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

	public class DefaultElementComponent extends UIComponent<DefaultElementComponent>
	{
		private S element;

		public DefaultElementComponent(MalisisGui gui, S element)
		{
			super(gui);
			this.element = element;
		}

		@Override
		public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
		{}

		@Override
		public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
		{
			renderer.drawText(element.toString());
		}
	}

}
