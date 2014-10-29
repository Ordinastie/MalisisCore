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

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.container.UITabGroup;
import net.malisis.core.client.gui.component.container.UITabGroup.Position;
import net.malisis.core.client.gui.component.container.UITabGroup.TabChangeEvent;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.element.XYResizableGuiShape;
import net.malisis.core.client.gui.event.MouseEvent;
import net.malisis.core.client.gui.event.component.StateChangeEvent.ActiveStateChange;
import net.malisis.core.util.MouseButton;
import net.minecraft.util.IIcon;

import com.google.common.eventbus.Subscribe;

/**
 * @author Ordinastie
 *
 */
public class UITab extends UIComponent<UITab>
{
	protected String label;
	protected UIImage image;
	protected boolean autoWidth = false;
	protected boolean autoHeight = false;
	protected UIContainer container;
	protected boolean active = false;
	protected int color = 0xFFFFFF;

	public UITab(MalisisGui gui, String label)
	{
		super(gui);
		setSize(0, 0);
		setLabel(label);

		shape = new XYResizableGuiShape();
	}

	public UITab(MalisisGui gui, UIImage image)
	{
		super(gui);
		setSize(0, 0);
		setImage(image);

		shape = new XYResizableGuiShape();
	}

	/**
	 * @return whether this {@link UITab} is horizontally positioned.
	 */
	protected boolean isHorizontal()
	{
		if (parent == null)
			return true;
		Position pos = ((UITabGroup) parent).getTabPosition();
		return pos == Position.TOP || pos == Position.BOTTOM;
	}

	/**
	 * Sets the color for this {@link UITab}. Also sets the color for its {@link UIContainer}.
	 *
	 * @param color
	 * @return
	 */
	public UITab setColor(int color)
	{
		this.color = color;
		if (parent != null && ((UITabGroup) parent).getAttachedContainer() != null)
			((UITabGroup) parent).getAttachedContainer().setBackgroundColor(color);
		return this;
	}

	/**
	 * @return the color of this {@link UITab}.
	 */
	public int getColor()
	{
		return color;
	}

	@Override
	public void setParent(UIComponent parent)
	{
		if (!(parent instanceof UITabGroup))
			throw new IllegalArgumentException("UITabs can only be added to UITabGroup");

		super.setParent(parent);
	}

	@Override
	public UITab setSize(int width, int height)
	{
		this.autoWidth = width == 0;
		this.width = autoWidth ? calcAutoWidth() : width;

		this.autoHeight = height == 0;
		this.height = autoHeight ? calcAutoHeight() : height;

		if (shape != null)
			shape.setSize(this.width, this.height);

		return this;
	}

	/**
	 * Calculates the width of this {@link UITab} based on its contents.
	 *
	 * @return
	 */
	private int calcAutoWidth()
	{
		if (label != null)
			return GuiRenderer.getStringWidth(label) + (isHorizontal() ? 10 : 8);
		else if (image != null)
			return image.getWidth() + 10;
		else
			return 8;
	}

	/**
	 * @return whether the width should be calculated automatically.
	 */
	public boolean isAutoWidth()
	{
		return autoWidth;
	}

	/**
	 * Calculates the height of this {@link UITab} base on its contents.
	 *
	 * @return
	 */
	private int calcAutoHeight()
	{
		if (label != null)
			return GuiRenderer.FONT_HEIGHT + (isHorizontal() ? 8 : 10);
		else if (image != null)
			return image.getHeight() + 10;
		else
			return 8;
	}

	/**
	 * @return whether the height should be calculated automatically.
	 */
	public boolean isAutoHeight()
	{
		return autoHeight;
	}

	/**
	 * Sets the label for this {@link UITab}. Removes the image if was previously set.
	 *
	 * @param label
	 * @return
	 */
	public UITab setLabel(String label)
	{
		this.image = null;
		this.label = label;
		if (autoWidth)
			width = calcAutoWidth();
		if (autoHeight)
			height = calcAutoHeight();
		return this;
	}

	/**
	 * Sets the image {@link UITab}. Removes the label if was previously set.
	 *
	 * @param image
	 * @return
	 */
	public UITab setImage(UIImage image)
	{
		this.label = null;
		this.image = image;
		if (autoWidth)
			width = calcAutoWidth();
		if (autoHeight)
			height = calcAutoHeight();

		return this;
	}

	/**
	 * Set the {@link UIContainer} linked with this {@link UITab}.
	 *
	 * @param container
	 * @return
	 */
	public UITab setContainer(UIContainer container)
	{
		this.container = container;
		return this;
	}

	/**
	 * @return this {@link UITab} position around the container.
	 */
	public Position getTabPosition()
	{
		return ((UITabGroup) parent).getTabPosition();
	}

	/**
	 * Sets this tab to be active. Enables and sets visibility for its container.
	 *
	 * @param active
	 */
	public void setActive(boolean active)
	{
		if (container == null)
			return;

		if (this.active != active)
		{
			switch (getTabPosition())
			{
				case TOP:
				case BOTTOM:
					this.y += active ? -1 : 1;
					this.height += active ? 2 : -2;
					break;
				case LEFT:
				case RIGHT:
					this.x += active ? -1 : 1;
					this.width += active ? 2 : -2;
					break;
			}
		}

		this.active = active;
		this.container.setVisible(active);
		this.container.setDisabled(!active);
		this.zIndex = container.getZIndex() + (active ? 1 : 0);

		fireEvent(new ActiveStateChange(this, active));
	}

	/**
	 * @return the icons to render.
	 */
	private IIcon getIcon()
	{
		if (parent == null)
			return null;

		return ((UITabGroup) parent).getIcons();
	}

	@Override
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		rp.colorMultiplier.set(color);
		rp.icon.set(getIcon());
		renderer.drawShape(shape, rp);
	}

	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		int w = label != null ? GuiRenderer.getStringWidth(label) : image.getWidth();
		int h = label != null ? GuiRenderer.FONT_HEIGHT : image.getHeight();
		int x = (getWidth() - w) / 2;
		int y = (getHeight() - h) / 2 + 1;

		if (active)
		{
			switch (getTabPosition())
			{
				case TOP:
					y -= 1;
					break;
				case BOTTOM:
					y += 1;
					break;
				case LEFT:
					x -= 1;
					break;
				case RIGHT:
					x += 1;
					break;
			}
		}

		if (label != null)
		{
			int color = isHovered() ? 0xFFFFA0 : (active ? 0xFFFFFF : 0x404040);
			renderer.drawText(label, x, y, zIndex, color, active);
		}
		else if (image != null)
		{
			image.setPosition(screenX() + x, screenY() + y);
			image.setZIndex(zIndex);
			image.draw(renderer, mouseX, mouseY, partialTick);
		}
	}

	@Subscribe
	public void onClick(MouseEvent.Release event)
	{
		if (event.getButton() != MouseButton.LEFT)
			return;

		if (!(parent instanceof UITabGroup))
			return;

		if (!fireEvent(new TabChangeEvent((UITabGroup) parent, this)))
			return;

		((UITabGroup) parent).setActiveTab(this);

	}
}
