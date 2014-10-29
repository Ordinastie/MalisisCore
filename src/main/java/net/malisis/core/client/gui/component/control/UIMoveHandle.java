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
import net.malisis.core.client.gui.event.MouseEvent;
import net.malisis.core.util.MouseButton;

import com.google.common.eventbus.Subscribe;

/**
 * @author Ordinastie
 *
 */
public class UIMoveHandle extends UIComponent<UIMoveHandle> implements IControlComponent
{
	public enum Type
	{
		BOTH, HORIZONTAL, VERTICAL
	}

	private Type type;

	public UIMoveHandle(MalisisGui gui, UIComponent parent, Type type)
	{
		super(gui);
		this.type = type != null ? type : Type.BOTH;

		int x = 1;
		int y = 1;
		if (parent instanceof UIContainer)
		{
			x -= ((UIContainer) parent).getHorizontalPadding();
			y -= ((UIContainer) parent).getVerticalPadding();
		}
		setPosition(x, y);
		setSize(5, 5);
		setZIndex(10);
		register(this);

		parent.addControlComponent(this);

		icon = gui.getGuiTexture().getIcon(268, 15, 15, 15);
	}

	public UIMoveHandle(MalisisGui gui, UIComponent parent)
	{
		this(gui, parent, Type.BOTH);
	}

	@Subscribe
	public void onDrag(MouseEvent.Drag event)
	{
		if (event.getButton() != MouseButton.LEFT)
			return;

		UIComponent parentCont = getParent().getParent();
		if (parentCont == null)
			return;

		int x = parent.getX();
		if (type == Type.BOTH || type == Type.HORIZONTAL)
			x = parentCont.relativeX(event.getX() /*- parentCont.getHorizontalPadding()*/);
		int y = parent.getY();
		if (type == Type.BOTH || type == Type.VERTICAL)
			y = parentCont.relativeY(event.getY() /*- parentCont.getVerticalPadding()*/);
		if (x < 0)
			x = 0;
		if (y < 0)
			y = 0;

		getParent().setPosition(x, y, Anchor.NONE);
	}

	@Override
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{}

	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		rp.icon.set(icon);
		renderer.drawShape(shape, rp);
	}

}
