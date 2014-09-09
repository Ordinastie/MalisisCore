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

package net.malisis.core.client.gui.component.interaction;

import net.malisis.core.client.gui.GuiIcon;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.container.UITabGroup;
import net.malisis.core.client.gui.element.XResizableGuiShape;
import net.malisis.core.client.gui.event.MouseEvent;
import net.malisis.core.util.MouseButton;

import com.google.common.eventbus.Subscribe;

/**
 * @author Ordinastie
 * 
 */
public class UITab extends UIComponent<UITab>
{
	//@formatter:off
	public static GuiIcon[] icons = new GuiIcon[] { new GuiIcon(200, 	15, 	5, 	12),
													new GuiIcon(205, 	15, 	5, 	12),
													new GuiIcon(210, 	15, 	5, 	12)};
	//@formatter:on

	protected String label;
	protected boolean autoWidth = false;
	protected UIContainer container;
	protected boolean active = false;

	public UITab(int width, String label)
	{
		setSize(width, 0);
		setLabel(label);

		shape = new XResizableGuiShape();
	}

	public UITab(String label)
	{
		this(0, label);
	}

	@Override
	public UITab setSize(int width, int height)
	{
		this.height = 12;
		this.width = width;
		if (width == 0)
			autoWidth = true;
		if (shape != null)
			shape.setSize(width, height);
		return this;
	}

	@Override
	public UITab setPosition(int x, int y, int anchor)
	{
		return super.setPosition(x, y + 1, anchor);
	}

	public UITab setLabel(String label)
	{
		this.label = label;
		if (autoWidth)
			width = GuiRenderer.getStringWidth(label) + 8;
		return this;
	}

	public UITab setContainer(UIContainer container)
	{
		this.container = container;
		return this;
	}

	public void setActive(boolean active)
	{
		if (this.active != active)
		{
			this.y += active ? -1 : 1;
			this.height += active ? 1 : -1;
		}

		this.active = active;
		this.container.setVisible(active);
		this.container.setDisabled(!active);
		this.zIndex = active ? container.getZIndex() + 1 : 0;
	}

	@Subscribe
	public void onClick(MouseEvent.Release event)
	{
		if (event.getButton() != MouseButton.LEFT)
			return;

		if (!(parent instanceof UITabGroup))
			return;

		((UITabGroup) parent).setActiveTab(this);
	}

	@Override
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		renderer.drawShape(shape, icons);
	}

	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		int x = (width - GuiRenderer.getStringWidth(label)) / 2;
		int y = 3;
		int color = isHovered() ? 0xFFFFA0 : (active ? 0xFFFFFF : 0x404040);
		renderer.drawText(label, x, y, zIndex, color, active);
	}
}
