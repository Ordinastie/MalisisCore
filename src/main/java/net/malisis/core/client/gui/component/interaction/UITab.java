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
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.container.UITabGroup;
import net.malisis.core.client.gui.component.container.UITabGroup.TabChangeEvent;
import net.malisis.core.client.gui.component.content.IContent;
import net.malisis.core.client.gui.component.content.IContentHolder;
import net.malisis.core.client.gui.element.Size;
import net.malisis.core.client.gui.element.Size.ISize;
import net.malisis.core.client.gui.element.position.Position;
import net.malisis.core.client.gui.event.component.StateChangeEvent.ActiveStateChange;
import net.malisis.core.client.gui.render.GuiIcon;
import net.malisis.core.client.gui.render.shape.GuiShape;
import net.malisis.core.client.gui.text.GuiText;
import net.malisis.core.renderer.font.FontOptions;
import net.minecraft.util.text.TextFormatting;

/**
 * @author Ordinastie
 *
 */
public class UITab extends UIComponent implements IContentHolder
{
	protected final ISize AUTO_SIZE = Size.of(	() -> isHorizontal() ? contentSize().width() : parent.size().width(),
												() -> isHorizontal() ? parent.size().height() : contentSize().height());

	/** The default {@link FontOptions} to use for this {@link UITab}. */
	protected FontOptions fontOptions = FontOptions	.builder()
													.color(0x444444)
													.when(this::isActive)
													.color(0xFFFFFF)
													.shadow()
													.when(this::isHovered)
													.color(0xFFFFA0)
													.build();
	/** Content to use for this {@link UITab}. */
	protected IContent content;
	/** Size calculated based on content. */
	protected ISize contentSize = Size.ZERO;
	/** The container this {@link UITab} is linked to. */
	protected UIContainer container;
	/** Whether this {@link UITab} is currently active. */
	protected boolean active = false;

	/** Background color for this {@link UITab}. */
	protected int bgColor = 0xFFFFFF;

	public UITab()
	{
		setAutoSize();

		setBackground(GuiShape.builder(this).icon(this::getIcon).color(this::getColor).border(3).build());
		setForeground(this::content);
	}

	/**
	 * Instantiates a new {@link UITab}.
	 *
	 * @param text the label
	 */
	public UITab(String text)
	{
		this();
		setText(text);
	}

	/**
	 * Instantiates a new {@link UITab}.
	 *
	 * @param content the content
	 */
	public UITab(IContent content)
	{
		this();
		setContent(content);
	}

	//#region Getters/Setters
	/**
	 * Sets the content for this {@link UICheckBox}.
	 *
	 * @param content the content
	 */
	public void setContent(IContent content)
	{
		this.content = content;
		content.setParent(this);
		content.setPosition(Position.of(content).centered().middleAligned().build());
		contentSize = content.size().plus(Size.of(6, 6));
	}

	public void setText(String text)
	{
		GuiText gt = GuiText.of(text, fontOptions);
		setContent(gt);
	}

	/**
	 * Gets the {@link UIComponent} used as content for this {@link UICheckBox}.
	 *
	 * @return the content component
	 */
	@Override
	public IContent content()
	{
		return content;
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
	public void setParent(UIComponent parent)
	{
		if (!(parent instanceof UITabGroup))
			throw new IllegalArgumentException("UITabs can only be added to UITabGroup");

		super.setParent(parent);
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
		return tabGroup().getTabPosition();
	}

	public UIContainer attachedContainer()
	{
		return tabGroup() != null ? tabGroup().getAttachedContainer() : null;
	}

	/**
	 * Sets the background color for this {@link UITab}.<br>
	 * Also sets the color for its {@link #container}.
	 *
	 * @param color the color
	 */
	@Override
	public void setColor(int color)
	{
		this.color = color;
		UIContainer attachedContainer = attachedContainer();
		if (attachedContainer != null)
			attachedContainer.setColor(color);
	}

	@Override
	public int getColor()
	{
		UIContainer attachedContainer = attachedContainer();
		return attachedContainer != null && active ? attachedContainer.getColor() : color;
	}

	public GuiIcon getIcon()
	{
		return tabGroup() != null ? tabGroup().getIcon() : GuiIcon.FULL;
	}

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
		setColor(this.color);

		fireEvent(new ActiveStateChange<>(this, active));
		return this;
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
	//#end Getters/Setters

	@Override
	public boolean onClick()
	{
		if (!(parent instanceof UITabGroup))
			return super.onClick();

		if (!fireEvent(new TabChangeEvent((UITabGroup) parent, this)))
			return super.onClick();

		tabGroup().setActiveTab(this);
		return true;
	}

	@Override
	public String getPropertyString()
	{
		return "[" + TextFormatting.GREEN + content + TextFormatting.RESET + "] " + super.getPropertyString();
	}
}
