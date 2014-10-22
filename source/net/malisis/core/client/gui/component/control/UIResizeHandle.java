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
public class UIResizeHandle extends UIComponent<UIResizeHandle> implements IControlComponent
{
	public UIResizeHandle(MalisisGui gui, UIContainer parent)
	{
		super(gui);
		parent.add(this);
		setPosition(parent.getHorizontalPadding() - 1, parent.getVerticalPadding() - 1, Anchor.BOTTOM | Anchor.RIGHT);
		setSize(5, 5);
		register(this);

		icon = gui.getGuiTexture().getIcon(268, 0, 15, 15);
	}

	@Subscribe
	public void onDrag(MouseEvent.Drag event)
	{
		if (event.getButton() != MouseButton.LEFT)
			return;

		if (getParent().getAnchor() != Anchor.NONE)
			getParent().setPosition(parent.screenX(), parent.screenY(), Anchor.NONE);

		int w = parent.getWidth() + event.getDeltaX();
		int h = parent.getHeight() + event.getDeltaY();
		if (w < 10)
			w = 10;
		if (h < 10)
			h = 10;

		getParent().setSize(w, h);
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
