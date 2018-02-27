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
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
M,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.malisis.core.client.gui.component.interaction;

import net.malisis.core.client.gui.ComponentPosition;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.IContentComponent;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.container.UITabGroup;
import net.malisis.core.client.gui.component.container.UITabGroup.TabChangeEvent;
import net.malisis.core.client.gui.component.decoration.UITooltip;
import net.malisis.core.client.gui.component.element.Position;
import net.malisis.core.client.gui.component.element.Size;
import net.malisis.core.client.gui.component.element.Size.ISize;
import net.malisis.core.client.gui.element.XYResizableGuiShape;
import net.malisis.core.client.gui.event.component.StateChangeEvent.ActiveStateChange;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.renderer.icon.Icon;
import net.malisis.core.renderer.icon.provider.GuiIconProvider;
import net.minecraft.util.text.TextFormatting;

/**
 * @author Ordinastie
 *
 */
public class UITab extends UIComponent<UITab> implements IContentComponent
{
	protected final ISize AUTO_SIZE = Size	.width(o -> isHorizontal() ? contentSize().width() : parent.size().width())
											.height(o -> isHorizontal() ? parent.size().height() : contentSize().height());

	/** The {@link FontOptions} to use for this {@link UITooltip}. */
	protected FontOptions fontOptions = FontOptions	.builder()
													.color(0x444444)
													.when(this::isActive)
													.color(0xFFFFFF)
													.shadow()
													.when(this::isHovered)
													.color(0xFFFFA0)
													.build();
	/** Content to use for this {@link UITab}. */
	protected UIComponent<?> content;

	protected ISize contentSize = Size.width(o -> content.size().width() + 6).height(o -> content.size().height() + 6);
	/** Whether the width of this {@link UITab} is calculated based on the {@link #label} or {@link #image} . */
	protected boolean autoWidth = false;
	/** Whether the height of this {@link UITab} is calculated based on the {@link #label} or {@link #image} . */
	protected boolean autoHeight = false;
	/** The container this {@link UITab} is linked to. */
	protected UIContainer<?> container;
	/** Whether this {@link UITab} is currently active. */
	protected boolean active = false;

	/** Background color for this {@link UITab}. */
	protected int bgColor = 0xFFFFFF;

	/**
	 * Instantiates a new {@link UITab}.
	 *
	 * @param gui the gui
	 * @param text the label
	 */
	public UITab(MalisisGui gui, String text)
	{
		super(gui);
		contentSize.setOwner(this);
		setAutoSize();
		setText(text);

		shape = new XYResizableGuiShape();
		iconProvider = new GuiIconProvider(null);

		fontOptions = FontOptions	.builder()
									.color(0x444444)
									.when(this::isActive)
									.color(0xFFFFFF)
									.shadow()
									.when(this::isHovered)
									.color(0xFFFFA0)
									.build();
	}

	/**
	 * Instantiates a new {@link UITab}.
	 *
	 * @param gui the gui
	 * @param content the content
	 */
	public UITab(MalisisGui gui, UIComponent<?> content)
	{
		super(gui);
		contentSize.setOwner(this);
		setAutoSize();
		setContent(content);

		shape = new XYResizableGuiShape();
		iconProvider = new GuiIconProvider(null);
	}

	//#region Getters/Setters
	@Override
	public void createGuiText()
	{
		IContentComponent.super.createGuiText();
		//apply default fontOptions when GuiText is automatically created by setText()
		setFontOptions(fontOptions);
		content.setPosition(Position.centered().middleAligned());
	}

	@Override
	public UIComponent<?> getContent()
	{
		return content;
	}

	@Override
	public void setContent(UIComponent<?> content)
	{
		this.content = content;
		content.setParent(this);
		content.setPosition(Position.centered().middleAligned());
	}

	public void setAutoSize()
	{
		setSize(AUTO_SIZE);
	}

	@Override
	public ISize contentSize()
	{
		return contentSize;
	}

	public UITabGroup tabGroup()
	{
		return (UITabGroup) getParent();
	}

	/**
	 * Sets the parent for this {@link UITab}.<br>
	 *
	 * @param parent the new parent
	 * @exception IllegalArgumentException if the parent is not a {@link UITabGroup}
	 */
	@Override
	public void setParent(UIComponent<?> parent)
	{
		if (!(parent instanceof UITabGroup))
			throw new IllegalArgumentException("UITabs can only be added to UITabGroup");

		super.setParent(parent);
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
	public UITab setContainer(UIContainer<?> container)
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
		return tabGroup().getTabPosition();
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
	 * Sets the background color for this {@link UITab}.<br>
	 * Also sets the bacground color for its {@link #container}.
	 *
	 * @param color the color
	 * @return this {@link UITab}
	 */
	public UITab setBgColor(int color)
	{
		this.bgColor = color;
		if (tabGroup() != null && tabGroup().getAttachedContainer() != null)
			tabGroup().getAttachedContainer().setColor(color);
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

		//		if (this.active != active)
		//		{
		//			switch (getTabPosition())
		//			{
		//				case TOP:
		//				case BOTTOM:
		//					this.y += active ? -1 : 1;
		//					this.height += active ? 2 : -2;
		//					break;
		//				case LEFT:
		//				case RIGHT:
		//					this.x += active ? -1 : 1;
		//					this.width += active ? 2 : -2;
		//					break;
		//			}
		//		}

		this.active = active;
		this.container.setVisible(active);
		this.container.setEnabled(active);
		this.zIndex = container.getZIndex() + (active ? 1 : 0);

		//applies current color to attached container
		setBgColor(bgColor);

		fireEvent(new ActiveStateChange<>(this, active));
		return this;
	}

	/**
	 * Gets the {@link Icon} to use for this {@link UITab}.
	 *
	 * @return the icons to render.
	 */
	private Icon getIcon()
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
		((GuiIconProvider) iconProvider).setIcon(getIcon());
		renderer.drawShape(shape, rp);
	}

	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		if (content == null)
			return;
		content.draw(renderer, mouseX, mouseY, partialTick);
	}

	@Override
	public String getPropertyString()
	{
		return "[" + TextFormatting.GREEN + content + TextFormatting.RESET + "] " + super.getPropertyString();
	}
}
