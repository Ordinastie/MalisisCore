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

import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.renderer.icon.provider.GuiIconProvider;
import net.malisis.core.util.MouseButton;

/**
 * @author Ordinastie
 *
 */
public class UIResizeHandle extends UIComponent<UIResizeHandle> implements IControlComponent
{
	public enum Type
	{
		BOTH, HORIZONTAL, VERTICAL
	}

	private Type type;

	public UIResizeHandle(MalisisGui gui, UIComponent<?> parent, Type type)
	{
		super(gui);
		this.type = type != null ? type : Type.BOTH;

		int x = -1;
		int y = -1;
		if (parent instanceof UIContainer)
		{
			x += ((UIContainer<?>) parent).getHorizontalPadding();
			y += ((UIContainer<?>) parent).getVerticalPadding();
		}

		setPosition(x, y, Anchor.BOTTOM | Anchor.RIGHT);
		setSize(5, 5);
		register(this);

		parent.addControlComponent(this);

		iconProvider = new GuiIconProvider(gui.getGuiTexture().getIcon(268, 0, 15, 15));
	}

	public UIResizeHandle(MalisisGui gui, UIComponent<?> parent)
	{
		this(gui, parent, Type.BOTH);
	}

	@Override
	public boolean onDrag(int lastX, int lastY, int x, int y, MouseButton button)
	{
		if (button != MouseButton.LEFT)
			return super.onDrag(lastX, lastY, x, y, button);

		UIComponent<?> p = getParent();
		if (p.getAnchor() != Anchor.NONE)
			p.setPosition(p.parentX(), p.parentY(), Anchor.NONE);

		int w = parent.getWidth();
		int h = parent.getHeight();
		if (type == Type.BOTH || type == Type.HORIZONTAL)
			w += x - lastX;
		if (type == Type.BOTH || type == Type.VERTICAL)
			h += y - lastY;
		if (w < 10)
			w = 10;
		if (h < 10)
			h = 10;

		getParent().setSize(w, h);

		return true;
	}

	@Override
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{}

	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		renderer.drawShape(shape, rp);
	}
}
