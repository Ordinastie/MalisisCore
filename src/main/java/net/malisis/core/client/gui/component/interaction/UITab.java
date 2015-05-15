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

import net.malisis.core.client.gui.ComponentPosition;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.IGuiText;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.container.UITabGroup;
import net.malisis.core.client.gui.component.container.UITabGroup.TabChangeEvent;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.decoration.UITooltip;
import net.malisis.core.client.gui.element.XYResizableGuiShape;
import net.malisis.core.client.gui.event.component.StateChangeEvent.ActiveStateChange;
import net.malisis.core.renderer.animation.transformation.ITransformable;
import net.malisis.core.renderer.font.FontRenderOptions;
import net.malisis.core.renderer.font.MalisisFont;
import net.minecraft.util.IIcon;

/**
 * @author Ordinastie
 *
 */
public class UITab extends UIComponent<UITab> implements IGuiText<UITab>
{
	/** The {@link MalisisFont} to use for this {@link UITooltip}. If null, uses {@link GuiRenderer#getDefaultFont()}. */
	protected MalisisFont font;
	/** The {@link FontRenderOptions} to use for this {@link UITooltip}. If null, uses {@link GuiRenderer#getDefaultFontRendererOptions()}. */
	protected FontRenderOptions fro;
	/** The {@link FontRenderOptions} to use for this {@link UITooltip} when active. */
	protected FontRenderOptions activeFro;
	/** The {@link FontRenderOptions} to use for this {@link UITooltip} when hovered. */
	protected FontRenderOptions hoveredFro;
	/** Label for this {@link UITab}. */
	protected String label;
	/** Image for this {@link UITab}. */
	protected UIImage image;
	/** Whether the width of this {@link UITab} is calculated based on the {@link #label} or {@link #image} . */
	protected boolean autoWidth = false;
	/** Whether the height of this {@link UITab} is calculated based on the {@link #label} or {@link #image} . */
	protected boolean autoHeight = false;
	/** The container this {@link UITab} is linked to. */
	protected UIContainer container;
	/** Whether this {@link UITab} is currently active. */
	protected boolean active = false;

	/** Background color for this {@link UITab}. */
	protected int bgColor = 0xFFFFFF;

	/**
	 * Instantiates a new {@link UITab}.
	 *
	 * @param gui the gui
	 * @param label the label
	 */
	public UITab(MalisisGui gui, String label)
	{
		super(gui);
		activeFro = new FontRenderOptions();
		activeFro.color = 0xFFFFFF;
		activeFro.shadow = true;
		hoveredFro = new FontRenderOptions();
		hoveredFro.color = 0xFFFFA0;

		setSize(0, 0);
		setLabel(label);

		shape = new XYResizableGuiShape();
	}

	/**
	 * Instantiates a new {@link UITab}.
	 *
	 * @param gui the gui
	 * @param image the image
	 */
	public UITab(MalisisGui gui, UIImage image)
	{
		super(gui);
		setSize(0, 0);
		setImage(image);

		shape = new XYResizableGuiShape();
	}

	//#region Getters/Setters
	/**
	 * Gets the {@link MalisisFont} used for this {@link UILabel}.
	 *
	 * @return the font
	 */
	@Override
	public MalisisFont getFont()
	{
		return font;
	}

	/**
	 * Gets the {@link FontRenderOptions} used for this {@link UILabel}.
	 *
	 * @return the font renderer options
	 */
	@Override
	public FontRenderOptions getFontRendererOptions()
	{
		return fro;
	}

	/**
	 * Sets the {@link MalisisFont} and {@link FontRenderOptions} to use for this {@link UILabel}.
	 *
	 * @param font the new font
	 * @param fro the fro
	 */
	@Override
	public UITab setFont(MalisisFont font, FontRenderOptions fro)
	{
		this.font = font;
		this.fro = fro;
		if (autoWidth)
			width = calcAutoWidth();
		if (autoHeight)
			height = calcAutoHeight();
		return this;
	}

	/**
	 * Gets the active {@link FontRenderOptions}.
	 *
	 * @return the activeFro
	 */
	public FontRenderOptions getActiveFontRendererOptions()
	{
		return activeFro;
	}

	/**
	 * Sets the active {@link FontRenderOptions}.
	 *
	 * @param fro the fro
	 * @return the UI tab
	 */
	public UITab setActiveFontRendererOptions(FontRenderOptions fro)
	{
		this.activeFro = fro;
		return this;
	}

	/**
	 * Gets the hovered {@link FontRenderOptions}.
	 *
	 * @return the hoveredFro
	 */
	public FontRenderOptions getHoveredFontRendererOptions()
	{
		return hoveredFro;
	}

	/**
	 * Sets the hovered {@link FontRenderOptions}.
	 *
	 * @param fro the fro
	 * @return the UI tab
	 */
	public UITab setHoveredFontRendererOptions(FontRenderOptions fro)
	{
		this.hoveredFro = fro;
		return this;
	}

	/**
	 * Sets the label for this {@link UITab}.<br>
	 * Removes the image if previously set.<br>
	 * Recalculates the width if {@link #autoWidth} is true, the height if {@link #autoHeight} is true.
	 *
	 * @param label the label
	 * @return this {@link UITab}
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
	 * Sets the image {@link UITab}.<br>
	 * Removes the label if previously set.<br>
	 * Recalculates the width if {@link #autoWidth} is true, the height if {@link #autoHeight} is true.
	 *
	 * @param image the image
	 * @return this {@link UITab}
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
	 * Sets the parent for this {@link UITab}.<br>
	 *
	 * @param parent the new parent
	 * @exception IllegalArgumentException if the parent is not a {@link UITabGroup}
	 */
	@Override
	public void setParent(UIComponent parent)
	{
		if (!(parent instanceof UITabGroup))
			throw new IllegalArgumentException("UITabs can only be added to UITabGroup");

		super.setParent(parent);
	}

	/**
	 * Sets the size of this {@link UITab}.<br>
	 * If width or height is 0, it will be automatically calculated base on {@link #label} or {@link #image}.
	 *
	 * @param width the width
	 * @param height the height
	 * @return this {@link UITab}
	 */
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
	 * Checks if the width is calculated automatically.
	 *
	 * @return true if the width is calculated automatically.
	 */
	public boolean isAutoWidth()
	{
		return autoWidth;
	}

	/**
	 * Checks if height is calculated automatically.
	 *
	 * @return true if the height is calculated automatically.
	 */
	public boolean isAutoHeight()
	{
		return autoHeight;
	}

	/**
	 * Set the {@link UIContainer} linked with this {@link UITab}.
	 *
	 * @param container the container
	 * @return this {@link UITab}
	 */
	public UITab setContainer(UIContainer container)
	{
		this.container = container;
		return this;
	}

	/**
	 * Gets the {@link ComponentPosition} of this {@link UITab}.
	 *
	 * @return the tab position
	 */
	public ComponentPosition getTabPosition()
	{
		return ((UITabGroup) parent).getTabPosition();
	}

	/**
	 * Gets the background color for this {@link UITab}.
	 *
	 * @return the color of this {@link UITab}.
	 */
	public int getBgColor()
	{
		return bgColor;
	}

	/**
	 * Sets the baground color for this {@link UITab}.<br>
	 * Also sets the bacground color for its {@link #container}.
	 *
	 * @param color the color
	 * @return this {@link UITab}
	 */
	public UITab setBgColor(int color)
	{
		this.bgColor = color;
		if (parent != null)
		{
			UIContainer cont = ((UITabGroup) parent).getAttachedContainer();
			if (cont instanceof ITransformable.Color)
				((Color) cont).setColor(color);
		}
		return this;
	}

	//#end Getters/Setters

	public boolean isActive()
	{
		return active;
	}

	/**
	 * Sets this tab to be active. Enables and sets visibility for its container.
	 *
	 * @param active true if active
	 */
	public UITab setActive(boolean active)
	{
		if (container == null)
		{
			this.active = active;
			return this;
		}

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
		return this;
	}

	/**
	 * Gets the {@link IIcon} to use for this {@link UITab}.
	 *
	 * @return the icons to render.
	 */
	private IIcon getIcon()
	{
		if (parent == null)
			return null;

		return ((UITabGroup) parent).getIcons();
	}

	/**
	 * Checks whether this {@link UITab} is horizontally positioned.
	 *
	 * @return true, if is horizontal
	 */
	protected boolean isHorizontal()
	{
		if (parent == null)
			return true;
		ComponentPosition pos = ((UITabGroup) parent).getTabPosition();
		return pos == ComponentPosition.TOP || pos == ComponentPosition.BOTTOM;
	}

	/**
	 * Calculates the width of this {@link UITab} based on its contents.
	 *
	 * @return the width
	 */
	private int calcAutoWidth()
	{
		if (label != null)
			return getRenderer().getStringWidth(this, label) + (isHorizontal() ? 10 : 8);
		else if (image != null)
			return image.getWidth() + 10;
		else
			return 8;
	}

	/**
	 * Calculates the height of this {@link UITab} base on its contents.
	 *
	 * @return the height
	 */
	private int calcAutoHeight()
	{
		if (label != null)
			return getRenderer().getStringHeight(this) + (isHorizontal() ? 8 : 10);
		else if (image != null)
			return image.getHeight() + 10;
		else
			return 8;
	}

	@Override
	public boolean onClick(int x, int y)
	{
		if (!(parent instanceof UITabGroup))
			return super.onClick(x, y);

		if (!fireEvent(new TabChangeEvent((UITabGroup) parent, this)))
			return super.onClick(x, y);

		((UITabGroup) parent).setActiveTab(this);
		return true;
	}

	@Override
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		rp.colorMultiplier.set(bgColor);
		rp.icon.set(getIcon());
		renderer.drawShape(shape, rp);
	}

	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		int w = label != null ? getRenderer().getStringWidth(this, label) : image.getWidth();
		int h = label != null ? getRenderer().getStringHeight(this) : image.getHeight();
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
			FontRenderOptions fro = isHovered() ? hoveredFro : (active ? activeFro : this.fro);
			renderer.drawText(font, label, x, y, 1, fro);
		}
		else if (image != null)
		{
			image.setPosition(screenX() + x, screenY() + y);
			image.setZIndex(zIndex);
			image.draw(renderer, mouseX, mouseY, partialTick);
		}
	}

	@Override
	public String getPropertyString()
	{
		return "label : " + label + " | " + super.getPropertyString();
	}
}
