/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 Ordinastie
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

package net.malisis.core.client.gui.component.layout;

import static com.google.common.base.Preconditions.*;

import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.element.position.Position;
import net.malisis.core.client.gui.element.position.Position.IPosition;

/**
 * @author Ordinastie
 *
 */
public class RowLayout
{
	protected final UIContainer parent;
	protected final IPosition offset;
	protected final int spacing;
	protected UIComponent last;

	public RowLayout(UIContainer parent, int spacing, IPosition offset)
	{
		this.parent = checkNotNull(parent);
		this.offset = offset;
		this.spacing = spacing;
	}

	public RowLayout(UIContainer parent)
	{
		this(checkNotNull(parent), 0, null);
	}

	public RowLayout(UIContainer parent, int spacing)
	{
		this(checkNotNull(parent), spacing, null);
	}

	public void add(UIComponent component)
	{
		checkNotNull(component);
		parent.add(component);
		if (last == null)
			component.setPosition(offset != null ? offset : Position.zero(component));
		else
			component.setPosition(Position.below(component, last, spacing));
		last = component;
	}
}
