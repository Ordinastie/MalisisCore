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
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import com.google.common.collect.Maps;

import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.layout.RowLayout;
import net.malisis.core.client.gui.element.IClipable;
import net.malisis.core.client.gui.element.Size;
import net.malisis.core.client.gui.render.GuiRenderer;

/**
 * @author Ordinastie
 *
 */
public class UIListContainer<S> extends UIContainer
{
	protected int elementSpacing = 0;
	protected Collection<S> elements = Collections.emptyList();
	protected Map<S, UIComponent> componentElements = Maps.newHashMap();
	protected int lastSize = 0;
	protected Function<S, UIComponent> elementComponentFactory = e -> new UILabel(Objects.toString(e));
	protected int elementsSize;

	public UIListContainer()
	{
		setSize(Size.sizeOfContent(this));
	}

	protected void buildElementComponents()
	{
		removeAll();
		componentElements.clear();

		RowLayout layout = new RowLayout(this, elementSpacing);
		for (S element : elements)
		{
			UIComponent comp = elementComponentFactory.apply(element);
			comp.attachData(element);
			layout.add(comp);
			componentElements.put(element, comp);
		}
		elementsSize = elements.size();
	}

	public void setElements(Collection<S> elements)
	{
		this.elements = elements != null ? elements : Collections.emptyList();
		buildElementComponents();
	}

	public Collection<S> getElements()
	{
		return elements;
	}

	public UIComponent getElementComponent(S element)
	{
		return componentElements.get(element);
	}

	public UIListContainer<S> setComponentFactory(Function<S, UIComponent> factory)
	{
		this.elementComponentFactory = factory;
		return this;
	}

	public void setElementSpacing(int elementSpacing)
	{
		this.elementSpacing = elementSpacing;
	}

	//#region IClipable
	@Override
	public ClipArea getClipArea()
	{
		return IClipable.NOCLIP;
	}

	@Override
	public void render(GuiRenderer renderer)
	{
		if (elements.size() != elementsSize)
			buildElementComponents();

		super.render(renderer);
	}
}
