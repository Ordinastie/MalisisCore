/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 PaleoCrafter, Ordinastie
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
import net.malisis.core.client.gui.event.MouseEvent;
import net.malisis.core.renderer.element.RenderParameters;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.preset.ShapePreset;
import net.malisis.core.util.MouseButton;
import net.minecraft.util.IIcon;

import com.google.common.eventbus.Subscribe;

/**
 * UIScrollBar
 * 
 * @author Ordinastie
 */
public class UIScrollBar extends UIComponent
{
	public static final int VERTICAL = 0;
	public static final int HORIZONTAL = 1;

	public static final int SCROLL_THICKNESS = 10;
	public static final int SCROLLER_HEIGHT = 15;

	private int type;
	public IScrollable scrollable;
	public int length;
	public int scrollableLength;
	public float offset;

	//@formatter:off
	public GuiIcon[] icons = new GuiIcon[] { 	new GuiIcon(200, 	0, 	1, 	1),
												new GuiIcon(201, 	0, 	1, 	1),
												new GuiIcon(209, 	0, 	1, 	1),
												new GuiIcon(200, 	1, 	1, 	1),
												new GuiIcon(201, 	1, 	1, 	1),
												new GuiIcon(209, 	1, 	1, 	1),
												new GuiIcon(200, 	9, 	1, 	1),
												new GuiIcon(201, 	9, 	1, 	1),
												new GuiIcon(209, 	9, 	1, 	1)};
	//@formatter:on
	public GuiIcon verticalIcon = new GuiIcon(227, 0, 8, 15);
	public GuiIcon verticalDisabledIcon = verticalIcon.offset(8, 0);
	public GuiIcon horizontalIcon = new GuiIcon(227, 15, 15, 8);
	public GuiIcon horizontalDisabledIcon = horizontalIcon.offset(0, 8);

	public <T extends UIContainer & IScrollable> UIScrollBar(T scrollable, int length, int type)
	{
		super();
		this.setParent(scrollable);
		this.scrollable = scrollable;
		this.type = type;
		this.length = length;
		if (type == HORIZONTAL)
		{
			width = length;
			height = SCROLL_THICKNESS;
		}
		else
		{
			width = SCROLL_THICKNESS;
			height = length;
		}
		horizontalDisabledIcon = horizontalIcon.offset(0, 8);
	}

	@Override
	public UIComponent setPosition(int x, int y, int anchor)
	{
		this.x = x;
		this.y = y;
		this.anchor = anchor;
		return this;
	}

	public int getLength()
	{
		return length;
	}

	public UIScrollBar setLength(int length)
	{
		this.length = length;
		if (type == HORIZONTAL)
			width = length;
		else
			height = length;
		offset = Math.max(0, Math.min(offset, length - SCROLLER_HEIGHT - 2));
		return this;
	}

	public void setScrollableLength(int length)
	{
		this.scrollableLength = length;
		this.disabled = scrollableLength <= this.length;
	}

	public void scrollTo(float offset)
	{
		this.offset = offset;
		int l = length - SCROLLER_HEIGHT - 2;
		int amount = offset == 0 ? 0 : (int) ((scrollableLength - l) * offset);
		if (type == HORIZONTAL)
			scrollable.setOffsetX(amount);
		else
			scrollable.setOffsetY(amount);
	}

	@Subscribe
	public void onClick(MouseEvent.Press event)
	{
		int l = length - SCROLLER_HEIGHT - 2;
		int pos = type == HORIZONTAL ? componentX(event.getX()) : componentY(event.getY());
		pos = Math.max(0, Math.min(pos - SCROLLER_HEIGHT / 2, l));
		scrollTo((float) pos / l);
	}
	
	@Subscribe
	public void onDrag(MouseEvent.Drag event)
	{
		if(event.getButton() != MouseButton.LEFT)
			return;
	
		int l = length - SCROLLER_HEIGHT - 2;
		int pos = type == HORIZONTAL ? componentX(event.getX()) : componentY(event.getY());
		pos = Math.max(0, Math.min(pos - SCROLLER_HEIGHT / 2, l));
		scrollTo((float) pos / l);
	}

	@Override
	public IIcon getIcon(int face)
	{
		if (face < 0 || face > icons.length)
			return null;

		return icons[face];
	}

	@Override
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		Shape shape = ShapePreset.GuiXYResizable(width, height, 1, 1);
		renderer.drawShape(shape);
	}

	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		RenderParameters rp = new RenderParameters();
		int w, h, ox = 0, oy = 0;
		int l = length - SCROLLER_HEIGHT - 2;
		if (type == HORIZONTAL)
		{
			rp.icon = disabled ? horizontalDisabledIcon : horizontalIcon;
			w = SCROLLER_HEIGHT;
			h = SCROLL_THICKNESS - 2;
			ox = (int) (offset * l);
		}
		else
		{
			rp.icon = disabled ? verticalDisabledIcon : verticalIcon;
			w = SCROLL_THICKNESS - 2;
			h = SCROLLER_HEIGHT;
			oy = (int) (offset * l);
		}
		Shape shape = ShapePreset.GuiElement(w, h);
		shape.translate(1, 1, 0).translate(ox, oy, 0);

		renderer.drawShape(shape, rp);
	}
}
