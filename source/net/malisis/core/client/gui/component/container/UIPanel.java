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
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.interaction.IScrollable;
import net.malisis.core.client.gui.component.interaction.UIScrollBar;
import net.malisis.core.client.gui.element.XYResizableGuiShape;
import net.malisis.core.client.gui.event.MouseEvent;
import net.minecraft.client.gui.GuiScreen;

import com.google.common.eventbus.Subscribe;

public class UIPanel extends UIContainer<UIPanel> implements IScrollable
{
	protected boolean allowVerticalScroll = false;
	protected boolean allowHorizontalScroll = false;

	protected UIScrollBar horizontalScroll;
	protected UIScrollBar verticalScroll;

	protected int contentWidth;
	protected int contentHeight;

	protected int xOffset;
	protected int yOffset;

	public UIPanel(MalisisGui gui, int width, int height)
	{
		super(gui, width, height);
		setPadding(3, 3);

		horizontalScroll = new UIScrollBar(gui, this, width, HORIZONTAL);
		verticalScroll = new UIScrollBar(gui, this, height, VERTICAL);
		setScrollBarsPosition();
		calculateContentSize();

		shape = new XYResizableGuiShape(5);
		icon = gui.getGuiTexture().getXYResizableIcon(200, 15, 15, 15, 5);
	}

	// #region getters/setters
	@Override
	public void setParent(UIContainer parent)
	{
		super.setParent(parent);
		if (width == INHERITED || height == INHERITED)
			calculateContentSize();
	}

	@Override
	public UIPanel setSize(int width, int height)
	{
		super.setSize(width, height);
		if (verticalScroll != null)
		{
			verticalScroll.setLength(height);
			//setVerticalScroll(allowVerticalScroll);

			horizontalScroll.setLength(width);
			//setVerticalScroll(allowHorizontalScroll);

			setScrollBarsPosition();
		}

		calculateContentSize();
		return this;
	}

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
		if (verticalScroll != null)
		{
			verticalScroll.setPosition(horizontalPadding - xOffset, -verticalPadding - yOffset, Anchor.RIGHT);
			horizontalScroll.setPosition(-verticalPadding - xOffset, horizontalPadding - yOffset, Anchor.BOTTOM);
		}
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
		int w = getWidth() - (allowVerticalScroll ? UIScrollBar.SCROLL_THICKNESS + 1 : 0);
		int h = getHeight() - (allowHorizontalScroll ? UIScrollBar.SCROLL_THICKNESS + 1 : 0);
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

	@Override
	public UIComponent getComponentAt(int x, int y)
	{
		if (allowVerticalScroll && verticalScroll.isInsideBounds(x, y))
			return verticalScroll;
		if (allowHorizontalScroll && horizontalScroll.isInsideBounds(x, y))
			return horizontalScroll;

		return super.getComponentAt(x, y);
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

	@Override
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		rp.colorMultiplier.set(getBackgroundColor() != 0x404040 ? getBackgroundColor() : -1);
		rp.icon.set(icon);
		renderer.drawShape(shape, rp);
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

	@Subscribe
	public void onScrollWheel(MouseEvent.ScrollWheel event)
	{
		UIScrollBar sb = GuiScreen.isShiftKeyDown() ? horizontalScroll : verticalScroll;
		if (GuiScreen.isShiftKeyDown() ? allowHorizontalScroll : allowVerticalScroll)
			sb.scrollBy(event.getDelta() * (GuiScreen.isCtrlKeyDown() ? 15 : 5));
	}
}
