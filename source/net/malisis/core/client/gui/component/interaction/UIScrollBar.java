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

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.element.GuiShape;
import net.malisis.core.client.gui.element.SimpleGuiShape;
import net.malisis.core.client.gui.element.XYResizableGuiShape;
import net.malisis.core.client.gui.event.MouseEvent;
import net.malisis.core.client.gui.icon.GuiIcon;
import net.malisis.core.util.MouseButton;
import net.minecraft.client.gui.GuiScreen;

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

	protected GuiIcon disabledIcon;

	protected GuiIcon verticalIcon;
	protected GuiIcon verticalDisabledIcon;
	protected GuiIcon horizontalIcon;
	protected GuiIcon horizontalDisabledIcon;

	private int type;
	public IScrollable scrollable;
	public int length;
	public int scrollableLength;
	public float offset;

	private GuiShape scrollShape;

	public <T extends UIContainer & IScrollable> UIScrollBar(MalisisGui gui, T scrollable, int length, int type)
	{
		super(gui);
		this.setParent(scrollable);
		this.scrollable = scrollable;
		this.type = type;
		setLength(length);

		int w, h;
		if (type == HORIZONTAL)
		{
			w = SCROLLER_HEIGHT;
			h = SCROLL_THICKNESS - 2;
		}
		else
		{
			w = SCROLL_THICKNESS - 2;
			h = SCROLLER_HEIGHT;
		}

		shape = new XYResizableGuiShape(1);
		scrollShape = new SimpleGuiShape().setSize(w, h);
		scrollShape.storeState();

		icon = gui.getGuiTexture().getXYResizableIcon(215, 0, 15, 15, 1);
		disabledIcon = gui.getGuiTexture().getXYResizableIcon(215, 15, 15, 15, 1);

		verticalIcon = gui.getGuiTexture().getIcon(230, 0, 8, 15);
		verticalDisabledIcon = gui.getGuiTexture().getIcon(238, 0, 8, 15);
		horizontalIcon = gui.getGuiTexture().getIcon(230, 15, 15, 8);
		horizontalDisabledIcon = gui.getGuiTexture().getIcon(230, 23, 15, 8);
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
		{
			width = length;
			height = SCROLL_THICKNESS;
		}
		else
		{
			width = SCROLL_THICKNESS;
			height = length;
		}
		offset = Math.max(0, Math.min(offset, length - SCROLLER_HEIGHT - 2));
		setSize(width, height);
		return this;
	}

	public void setScrollableLength(int length)
	{
		this.scrollableLength = length;
		this.disabled = scrollableLength <= this.length;
	}

	public void scrollTo(float offset)
	{
		if (isDisabled())
			return;

		if (offset < 0)
			offset = 0;
		if (offset > 1)
			offset = 1;
		this.offset = offset;
		int amount = (int) ((scrollableLength - length) * offset);
		if (type == HORIZONTAL)
			scrollable.setOffsetX(amount);
		else
			scrollable.setOffsetY(amount);
	}

	public void scrollBy(float amount)
	{
		amount *= -1 / (float) (scrollableLength - length);
		scrollTo(offset + amount);
	}

	@Subscribe
	public void onClick(MouseEvent.Press event)
	{
		if (event.getButton() != MouseButton.LEFT)
			return;

		int l = length - SCROLLER_HEIGHT - 2;
		int pos = type == HORIZONTAL ? relativeX(event.getX()) : relativeY(event.getY());
		pos = Math.max(0, Math.min(pos - SCROLLER_HEIGHT / 2, l));
		scrollTo((float) pos / l);
	}

	@Subscribe
	public void onScrollWheel(MouseEvent.ScrollWheel event)
	{
		scrollBy(event.getDelta() * (GuiScreen.isCtrlKeyDown() ? 15 : 5));
	}

	@Subscribe
	public void onDrag(MouseEvent.Drag event)
	{
		if (event.getButton() != MouseButton.LEFT)
			return;

		int l = length - SCROLLER_HEIGHT - 2;
		int pos = type == HORIZONTAL ? relativeX(event.getX()) : relativeY(event.getY());
		pos = Math.max(0, Math.min(pos - SCROLLER_HEIGHT / 2, l));
		scrollTo((float) pos / l);
	}

	@Override
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		rp.icon.set(isDisabled() ? disabledIcon : icon);
		renderer.drawShape(shape, rp);
	}

	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		GuiIcon icon;
		int ox = 0, oy = 0;
		int l = length - SCROLLER_HEIGHT - 2;
		if (type == HORIZONTAL)
		{
			icon = isDisabled() ? horizontalDisabledIcon : horizontalIcon;
			ox = (int) (offset * l);
		}
		else
		{
			icon = isDisabled() ? verticalDisabledIcon : verticalIcon;
			oy = (int) (offset * l);
		}

		scrollShape.resetState();
		scrollShape.setPosition(ox + 1, oy + 1);

		rp.icon.set(icon);
		renderer.drawShape(scrollShape, rp);
	}
}
