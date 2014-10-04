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
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.container.UITabGroup;
import net.malisis.core.client.gui.component.container.UITabGroup.Position;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.element.TabShape;
import net.malisis.core.client.gui.event.ComponentEvent.ActiveStateChanged;
import net.malisis.core.client.gui.event.MouseEvent;
import net.malisis.core.client.gui.icon.GuiIcon;
import net.malisis.core.util.MouseButton;

import com.google.common.eventbus.Subscribe;

/**
 * @author Ordinastie
 *
 */
public class UITab extends UIComponent<UITab>
{
	private GuiIcon[] tabIcons = GuiIcon.XYResizable(200, 15, 15, 15, 3);

	protected String label;
	protected UIImage image;
	protected boolean autoWidth = false;
	protected boolean autoHeight = false;
	protected UIContainer container;
	protected boolean active = false;

	protected int baseContainerWidth;
	protected int baseContainerHeight;

	private GuiIcon[] icons;

	public UITab(String label)
	{
		setSize(0, 0);
		setLabel(label);
	}

	public UITab(UIImage image)
	{
		setSize(0, 0);
		setImage(image);
	}

	@Override
	public void setParent(UIContainer parent)
	{
		super.setParent(parent);
		this.shape = new TabShape(((UITabGroup) parent).getTabPosition(), 3);
		this.icons = ((TabShape) shape).getIcons(tabIcons);
	}

	@Override
	public UITab setSize(int width, int height)
	{
		this.autoWidth = width == 0;
		this.width = autoWidth ? calcAutoWidth() : width;

		this.autoHeight = height == 0;
		this.height = autoHeight ? calcAutoHeight() : height;

		if (shape != null)
			shape.setSize(width, height);

		return this;
	}

	/**
	 * Calculates the width of this <code>UITab</code> based on its contents
	 *
	 * @return
	 */
	private int calcAutoWidth()
	{
		if (label != null)
			return GuiRenderer.getStringWidth(label) + 8;
		else if (image != null)
			return image.getWidth() + 8;
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
	 * Calculates the height of this <code>UITab</code> base on its contents.
	 *
	 * @return
	 */
	private int calcAutoHeight()
	{
		if (label != null)
			return GuiRenderer.FONT_HEIGHT + 8;
		else if (image != null)
			return image.getHeight() + 8;
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

	@Override
	public UITab setPosition(int x, int y, int anchor)
	{
		switch (getTabPosition())
		{
			case TOP:
				y += 1;
				break;
			case BOTTOM:
				y -= 1;
				break;
			case LEFT:
				x += 1;
				break;
			case RIGHT:
				x -= 1;
				break;
			default:
				break;
		}
		return super.setPosition(x, y, anchor);
	}

	/**
	 * Sets the label for this <code>UITab</code>. Removes the image if was previously set.
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
	 * Sets the image <code>UITab</code>. Removes the label if was previously set.
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
	 * Set the <code>UIContainer</code> linked with this <code>UITab</code>.
	 *
	 * @param container
	 * @return
	 */
	public UITab setContainer(UIContainer container)
	{
		this.container = container;
		this.baseContainerWidth = container.getBaseWidth(); //we don't want the calculated width/height if INHERITED
		this.baseContainerHeight = container.getBaseHeight();
		return this;
	}

	/**
	 * @return the container original width.
	 */
	public int getContainerWidth()
	{
		return baseContainerWidth;
	}

	/**
	 * @return the container original height.
	 */
	public int getContainerHeight()
	{
		return baseContainerHeight;
	}

	/**
	 * @return this <code>UITab</code> position around the container.
	 */
	public Position getTabPosition()
	{
		return ((UITabGroup) parent).getTabPosition();
	}

	/**
	 * Sets this tab to be active. Enables and sets visiblity for its container.
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
		this.zIndex = active ? container.getZIndex() + 1 : 0;

		fireEvent(new ActiveStateChanged(this, active));
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
		rp.colorMultiplier.set(container.getBackgroundColor() != 0x404040 ? container.getBackgroundColor() : -1);
		renderer.drawShape(shape, rp, icons);
	}

	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		int w = label != null ? GuiRenderer.getStringWidth(label) : image.getWidth();
		int h = label != null ? GuiRenderer.FONT_HEIGHT : image.getHeight();
		int x = (width - w) / 2;
		int y = (height - h) / 2;

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
}
