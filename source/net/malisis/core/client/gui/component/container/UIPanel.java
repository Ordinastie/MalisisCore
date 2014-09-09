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

import static net.malisis.core.client.gui.component.interaction.UIScrollBar.*;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.ClipArea;
import net.malisis.core.client.gui.GuiIcon;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.interaction.IScrollable;
import net.malisis.core.client.gui.component.interaction.UIScrollBar;
import net.malisis.core.client.gui.element.XYResizableGuiShape;
import net.malisis.core.client.gui.event.MouseEvent;
import net.minecraft.client.gui.GuiScreen;

import com.google.common.eventbus.Subscribe;

public class UIPanel extends UIContainer<UIPanel> implements IScrollable
{
	//@formatter:off
	public static GuiIcon[] icons = new GuiIcon[] { new GuiIcon(200, 	15, 	5, 	5),
													new GuiIcon(205, 	15, 	5, 	5),
													new GuiIcon(210, 	15, 	5, 	5),
													new GuiIcon(200, 	20, 	5, 	5),
													new GuiIcon(205, 	20, 	5, 	5),
													new GuiIcon(210, 	20, 	5, 	5),
													new GuiIcon(200, 	25, 	5, 	5),
													new GuiIcon(205, 	25, 	5, 	5),
													new GuiIcon(210, 	25, 	5, 	5)};
	//@formatter:on

	protected boolean allowVerticalScroll = false;
	protected boolean allowHorizontalScroll = false;

	protected UIScrollBar horizontalScroll;
	protected UIScrollBar verticalScroll;

	protected int contentWidth;
	protected int contentHeight;

	protected int xOffset;
	protected int yOffset;

	protected int color = -1;

	public UIPanel(int width, int height)
	{
		super(width, height);
		setPadding(3, 3);

		horizontalScroll = new UIScrollBar(this, width, HORIZONTAL);
		verticalScroll = new UIScrollBar(this, height, VERTICAL);
		setScrollBarsPosition();
		calculateContentSize();

		shape = new XYResizableGuiShape(5);
	}

	@Override
	public boolean fireMouseEvent(MouseEvent event)
	{
		if (allowVerticalScroll
				&& (verticalScroll.isInsideBounds(event.getX(), event.getY()) || (verticalScroll.isFocused() && event instanceof MouseEvent.Drag)))
			return verticalScroll.fireMouseEvent(event);
		if (allowHorizontalScroll
				&& (horizontalScroll.isInsideBounds(event.getX(), event.getY()) || (horizontalScroll.isFocused() && event instanceof MouseEvent.Drag)))
			return horizontalScroll.fireMouseEvent(event);

		if (isInsideBounds(event.getX(), event.getY()))
			return super.fireMouseEvent(event);

		return false;
	}

	public boolean isInsideBounds(int x, int y, boolean scrolls)
	{
		if (scrolls && allowVerticalScroll && verticalScroll.isInsideBounds(x, y))
			return true;
		if (scrolls && allowHorizontalScroll && horizontalScroll.isInsideBounds(x, y))
			return true;
		if (super.isInsideBounds(x, y))
			return true;

		return false;
	}

	// #region getters/setters
	public UIPanel setHorizontalScroll(boolean allow)
	{
		int shift = allow ? -SCROLL_THICKNESS : SCROLL_THICKNESS;
		verticalScroll.setLength(verticalScroll.getLength() + shift);
		allowHorizontalScroll = allow;
		return this;
	}

	public boolean getHorizontalScroll()
	{
		return allowHorizontalScroll;
	}

	public UIPanel setVerticalScroll(boolean allow)
	{
		int shift = allow ? -SCROLL_THICKNESS : SCROLL_THICKNESS;
		horizontalScroll.setLength(horizontalScroll.getLength() + shift);
		allowVerticalScroll = allow;
		return this;
	}

	public boolean getVerticalScroll()
	{
		return allowVerticalScroll;
	}

	public void setScrollBarsPosition()
	{
		verticalScroll.setPosition(horizontalPadding - xOffset, -verticalPadding - yOffset, Anchor.RIGHT);
		horizontalScroll.setPosition(-verticalPadding - xOffset, horizontalPadding - yOffset, Anchor.BOTTOM);
	}

	@Override
	public void setOffsetX(int offset)
	{
		this.xOffset = -offset;
		setScrollBarsPosition();
	}

	@Override
	public void setOffsetY(int offset)
	{
		this.yOffset = -offset;
		setScrollBarsPosition();
	}

	@Override
	public int getOffsetX()
	{
		return xOffset;
	}

	@Override
	public int getOffsetY()
	{
		return yOffset;
	}

	public void setColor(int color)
	{
		this.color = color;
	}

	public int getColor()
	{
		return color;
	}

	// #end getters/setters

	@Override
	public void add(UIComponent component)
	{
		super.add(component);
		calculateContentSize();
	}

	@Override
	public void onContentUpdate()
	{
		calculateContentSize();
	}

	public void calculateContentSize()
	{
		int w = width - (allowVerticalScroll ? UIScrollBar.SCROLL_THICKNESS + 1 : 0);
		int h = height - (allowHorizontalScroll ? UIScrollBar.SCROLL_THICKNESS + 1 : 0);
		int contentWidth = w - horizontalPadding;
		int contentHeight = h - verticalPadding;

		for (UIComponent c : components)
		{
			if (c.isVisible())
			{
				contentWidth = Math.max(contentWidth, c.containerX() + c.getWidth());
				contentHeight = Math.max(contentHeight, c.containerY() + c.getHeight());
			}
		}

		this.contentHeight = contentHeight + verticalPadding;
		if (verticalScroll != null)
		{
			verticalScroll.setScrollableLength(this.contentHeight);
			if (this.contentHeight == h)
				verticalScroll.scrollTo(0);
		}

		this.contentWidth = contentWidth + horizontalPadding;
		if (horizontalScroll != null)
		{
			horizontalScroll.setScrollableLength(this.contentWidth);
			if (this.contentWidth == w)
				horizontalScroll.scrollTo(0);
		}
	}

	@Subscribe
	public void onScrollWheel(MouseEvent.ScrollWheel event)
	{
		UIScrollBar sb = GuiScreen.isShiftKeyDown() ? horizontalScroll : verticalScroll;
		if (GuiScreen.isShiftKeyDown() ? allowHorizontalScroll : allowVerticalScroll)
			sb.scrollBy(event.getDelta() * (GuiScreen.isCtrlKeyDown() ? 15 : 5));
	}

	@Override
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		rp.colorMultiplier.set(color);
		renderer.drawShape(shape, rp, icons);
	}

	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		// GL11.glTranslatef(xOffset, yOffset, 0);
		super.drawForeground(renderer, mouseX, mouseY, partialTick);
		// GL11.glTranslatef(-xOffset, -yOffset, 0);

		if (allowVerticalScroll)
			verticalScroll.draw(renderer, mouseX, mouseY, partialTick);
		if (allowHorizontalScroll)
			horizontalScroll.draw(renderer, mouseX, mouseY, partialTick);

	}

	@Override
	public ClipArea getClipArea()
	{
		ClipArea area = new ClipArea(this, 1);
		if (allowVerticalScroll)
			area.X -= UIScrollBar.SCROLL_THICKNESS - 1;
		if (allowHorizontalScroll)
			area.Y -= UIScrollBar.SCROLL_THICKNESS - 1;
		return area;
	}
}
