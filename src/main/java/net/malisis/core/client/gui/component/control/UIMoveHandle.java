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

package net.malisis.core.client.gui.component.control;

import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.element.Size;
import net.malisis.core.client.gui.element.position.Position;
import net.malisis.core.client.gui.render.GuiIcon;
import net.malisis.core.client.gui.render.shape.GuiShape;
import net.malisis.core.util.MouseButton;

/**
 * @author Ordinastie
 *
 */
public class UIMoveHandle extends UIComponent implements IControlComponent
{
	public enum Type
	{
		BOTH,
		HORIZONTAL,
		VERTICAL
	}

	private Type type;

	public UIMoveHandle(UIComponent parent, Type type)
	{
		this.type = type != null ? type : Type.BOTH;

		int x = 1;
		int y = 1;
		if (parent instanceof UIContainer)
		{
			x -= ((UIContainer) parent).padding().left();
			y -= ((UIContainer) parent).padding().top();
		}
		setPosition(Position.of(x, y));
		setSize(Size.of(5, 5));
		setZIndex(10);
		parent.addControlComponent(this);

		setForeground(GuiShape.builder(this).icon(GuiIcon.MOVE).build());
	}

	public UIMoveHandle(UIComponent parent)
	{
		this(parent, Type.BOTH);
	}

	@Override
	public boolean onDrag(MouseButton button)
	{
		if (button != MouseButton.LEFT)
			return super.onDrag(button);

		UIComponent parentCont = getParent().getParent();
		if (parentCont == null)
			return super.onDrag(button);

		int px = parent.position().x();
		if (type == Type.BOTH || type == Type.HORIZONTAL)
			px = parentCont.mousePosition().x();
		int py = parent.position().y();
		if (type == Type.BOTH || type == Type.VERTICAL)
			py = parentCont.mousePosition().y();
		if (px < 0)
			px = 0;
		if (py < 0)
			py = 0;
		//TODO: check x + w against screen size

		getParent().setPosition(Position.of(px, py));
		return true;
	}
}
