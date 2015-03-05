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

package net.malisis.core.client.gui.component.container;

import java.util.LinkedHashSet;
import java.util.Set;

import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.ClipArea;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.IClipable;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.control.ICloseable;
import net.malisis.core.client.gui.component.control.IControlComponent;
import net.malisis.core.client.gui.component.control.IScrollable;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.event.component.ContentUpdateEvent;
import net.malisis.core.client.gui.event.component.SpaceChangeEvent;
import net.malisis.core.client.gui.event.component.StateChangeEvent.VisibleStateChange;
import net.minecraft.client.gui.GuiScreen;

import com.google.common.eventbus.Subscribe;

/**
 * {@link UIContainer} are the base for components holding other components.<br>
 * Child components are drawn in the foreground.<br>
 * Mouse events received are passed to the child concerned (getComponentAt()).<br>
 * Keyboard event are passed to all the children.
 *
 * @author Ordinastie, PaleoCrafter
 * @param <T> type of UIContainer
 */
public class UIContainer<T extends UIContainer> extends UIComponent<T> implements IClipable, IScrollable, ICloseable
{
	/** List of {@link UIComponent} inside this {@link UIContainer}. */
	protected final Set<UIComponent> components;
	/** Horizontal padding to apply to this {@link UIContainer}. */
	protected int horizontalPadding;
	/** Vertical padding to apply to this {@link UIContainer}. */
	protected int verticalPadding;
	/** Label for the title of this {@link UIContainer}. */
	protected UILabel titleLabel;
	//IClipable
	/** Determines whether this {@link UIContainer} should clip its contents to its drawn area. */
	protected boolean clipContent = true;
	//IScrollable
	/** Width of the contents of this {@link UIContainer}. */
	protected int contentWidth;
	/** Height of the contents of this {@link UIContainer}. */
	protected int contentHeight;
	/** X Offset for the contents of this {@link UIContainer} from 0 to 1. */
	protected int xOffset;
	/** Y Offset for the contents of this {@link UIContainer} from 0 to 1. */
	protected int yOffset;

	/**
	 * Default constructor, creates the components list.
	 *
	 * @param gui the gui
	 */
	public UIContainer(MalisisGui gui)
	{
		super(gui);
		components = new LinkedHashSet<>();
		titleLabel = new UILabel(gui);
	}

	/**
	 * Instantiates a new {@link UIContainer}.
	 *
	 * @param gui the gui
	 * @param title the title
	 */
	public UIContainer(MalisisGui gui, String title)
	{
		this(gui);
		setTitle(title);
	}

	/**
	 * Instantiates a new {@link UIContainer}.
	 *
	 * @param gui the gui
	 * @param width the width
	 * @param height the height
	 */
	public UIContainer(MalisisGui gui, int width, int height)
	{
		this(gui);
		setSize(width, height);
	}

	/**
	 * Instantiates a new {@link UIContainer}.
	 *
	 * @param gui the gui
	 * @param title the title
	 * @param width the width
	 * @param height the height
	 */
	public UIContainer(MalisisGui gui, String title, int width, int height)
	{
		this(gui);
		setTitle(title);
		setSize(width, height);
	}

	// #region getters/setters
	/**
	 * Sets the visible.
	 *
	 * @param visible the visible
	 * @return the t
	 */
	@Override
	public T setVisible(boolean visible)
	{
		if (isVisible() == visible)
			return (T) this;

		super.setVisible(visible);
		if (!visible)
		{
			for (UIComponent c : components)
			{
				c.setHovered(false);
				c.setFocused(false);
			}
		}
		return (T) this;
	}

	/**
	 * Sets the disabled.
	 *
	 * @param disabled the disabled
	 * @return the t
	 */
	@Override
	public T setDisabled(boolean disabled)
	{
		super.setDisabled(disabled);
		if (disabled)
		{
			for (UIComponent c : components)
			{
				c.setHovered(false);
				c.setFocused(false);
			}
		}
		return (T) this;
	}

	/**
	 * Set the padding for this {@link UIContainer}.
	 *
	 * @param horizontal the horizontal
	 * @param vertical the vertical
	 */
	public void setPadding(int horizontal, int vertical)
	{
		this.horizontalPadding = horizontal;
		this.verticalPadding = vertical;
	}

	/**
	 * Gets the horizontal padding.
	 *
	 * @return horizontal padding of this {@link UIContainer}.
	 */
	@Override
	public int getHorizontalPadding()
	{
		return horizontalPadding;
	}

	/**
	 * Gets the vertical padding.
	 *
	 * @return horizontal padding of this {@link UIContainer}.
	 */
	@Override
	public int getVerticalPadding()
	{
		return verticalPadding;
	}

	/**
	 * Sets the title for {@link UIContainer}.<br>
	 * Creates a {@link UILabel} and adds it inside {@link UIContainer}.
	 *
	 * @param title the title
	 * @return the UI container
	 */
	public UIContainer setTitle(String title)
	{
		if (title == null || title == "")
		{
			remove(titleLabel);
			return this;
		}

		titleLabel.setText(title);
		add(titleLabel);
		return this;
	}

	/**
	 * Gets the title.
	 *
	 * @return the title for this {@link UIContainer}.
	 */
	public String getTitle()
	{
		return titleLabel != null ? titleLabel.getText() : null;
	}

	// #end getters/setters

	/**
	 * Gets the relative position of the specified {@link UIComponent} inside this {@link UIContainer}.
	 *
	 * @param component the component
	 * @return the coordinate
	 */
	@Override
	public int componentX(UIComponent component)
	{
		int x = super.componentX(component);
		int a = Anchor.horizontal(component.getAnchor());
		if (a == Anchor.LEFT || a == Anchor.NONE)
			x += getHorizontalPadding();
		else if (a == Anchor.RIGHT)
			x -= getHorizontalPadding();

		if (!(component instanceof IControlComponent))
			x -= xOffset;

		return x;
	}

	/**
	 * Gets the relative position of the specified {@link UIComponent} inside this {@link UIContainer}.
	 *
	 * @param component the component
	 * @return the coordinate
	 */
	@Override
	public int componentY(UIComponent component)
	{
		int y = super.componentY(component);
		int a = Anchor.vertical(component.getAnchor());
		if (a == Anchor.TOP || a == Anchor.NONE)
			y += getVerticalPadding();
		else if (a == Anchor.BOTTOM)
			y -= getVerticalPadding();

		if (!(component instanceof IControlComponent))
			y -= yOffset;
		return y;
	}

	/**
	 * Gets the component at the specified coordinates.<br>
	 * Selects the component with the highest z-index from the components overlapping the coordinates.
	 *
	 * @param x the x
	 * @param y the y
	 * @return the child component in this {@link UIContainer}, this {@link UIContainer} if none, or null if outside its bounds.
	 */
	@Override
	public UIComponent getComponentAt(int x, int y)
	{
		UIComponent superComp = super.getComponentAt(x, y);
		if (superComp != null && superComp != this)
			return superComp;

		if (isDisabled() || !isVisible())
			return null;

		Set<UIComponent> list = new LinkedHashSet<>();
		for (UIComponent c : components)
		{
			UIComponent component = c.getComponentAt(x, y);
			if (component != null)
				list.add(component);
		}

		if (list.size() == 0)
			return superComp;

		UIComponent component = null;
		for (UIComponent c : list)
		{
			if (component == null || component.getZIndex() <= c.getZIndex())
				component = c;
		}

		return component;
	}

	/**
	 * Called when this {@link UIComponent} gets its content updated.
	 */
	public void onContentUpdate()
	{
		calculateContentSize();
		fireEvent(new ContentUpdateEvent(this));
	}

	/**
	 * Calculates content size.
	 */
	public void calculateContentSize()
	{
		int contentWidth = 0;
		int contentHeight = 0;

		for (UIComponent c : components)
		{
			if (c.isVisible())
			{
				contentWidth = Math.max(contentWidth, c.parentX() + c.getWidth() + xOffset);
				contentHeight = Math.max(contentHeight, c.parentY() + c.getHeight() + yOffset);
			}
		}

		this.contentHeight = contentHeight + 2 * getVerticalPadding();
		this.contentWidth = contentWidth + 2 * getHorizontalPadding();
	}

	//#region IClipable
	/**
	 * Gets the {@link ClipArea}.
	 *
	 * @return the clip area
	 */
	@Override
	public ClipArea getClipArea()
	{
		return new ClipArea(this);
	}

	/**
	 * Sets whether this {@link UIContainer} should clip its contents
	 *
	 * @param clipContent if true, clip contents
	 */
	@Override
	public void setClipContent(boolean clipContent)
	{
		this.clipContent = clipContent;
	}

	/**
	 * Checks whether this {@link UIContainer} should clip its contents
	 *
	 * @return true, if should clip contents
	 */
	@Override
	public boolean shouldClipContent()
	{
		return clipContent;
	}

	//#end IClipable

	//#region IScrollable
	@Override
	public int getContentWidth()
	{
		return contentWidth;
	}

	@Override
	public int getContentHeight()
	{
		return contentHeight;
	}

	@Override
	public float getOffsetX()
	{
		return xOffset;
	}

	@Override
	public void setOffsetX(float offsetX, int delta)
	{
		this.xOffset = (int) ((getContentWidth() - getWidth() + delta) * offsetX);
	}

	@Override
	public float getOffsetY()
	{
		return yOffset;
	}

	@Override
	public void setOffsetY(float offsetY, int delta)
	{
		this.yOffset = (int) ((getContentHeight() - getHeight() + delta) * offsetY);
	}

	@Override
	public float getScrollStep()
	{
		return (GuiScreen.isCtrlKeyDown() ? 0.125F : 0.025F);
	}

	//#end IScrollable

	/**
	 * Adds components to this {@link UIContainer}.
	 *
	 * @param components the components
	 */
	public void add(UIComponent... components)
	{
		for (UIComponent component : components)
		{
			if (component != null)
			{
				this.components.add(component);
				component.setParent(this);
				component.register(this);
			}
		}
		onContentUpdate();
	}

	/**
	 * Removes the component from this {@link UIContainer}.
	 *
	 * @param component the component
	 */
	public void remove(UIComponent component)
	{
		if (component.getParent() != this)
			return;

		components.remove(component);
		component.setParent(null);
		component.unregister(this);
		onContentUpdate();
	}

	/**
	 * Removes all the components from this {@link UIContainer}. Does not remove control components
	 */
	public void removeAll()
	{
		for (UIComponent component : components)
			component.setParent(null);
		components.clear();
		onContentUpdate();
	}

	@Override
	public void onAddedToScreen()
	{
		super.onAddedToScreen();
		for (UIComponent component : components)
			component.onAddedToScreen();
	}

	@Override
	public void onClose()
	{
		if (getParent() instanceof UIContainer)
			((UIContainer) getParent()).remove(this);
	}

	/**
	 * Draws the background.
	 *
	 * @param renderer the renderer
	 * @param mouseX the mouse x
	 * @param mouseY the mouse y
	 * @param partialTick the partial tick
	 */
	@Override
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{

	}

	/**
	 * Draws the foreground.
	 *
	 * @param renderer the renderer
	 * @param mouseX the mouse x
	 * @param mouseY the mouse y
	 * @param partialTick the partial tick
	 */
	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		for (UIComponent c : components)
			c.draw(renderer, mouseX, mouseY, partialTick);
	}

	/**
	 * Called when a child {@link UIComponent} gets its visibility changed
	 *
	 * @param event the event
	 */
	@Subscribe
	public void onComponentStateChange(VisibleStateChange event)
	{
		onContentUpdate();
	}

	/**
	 * Called when a child {@link UIComponent} gets its size or position changed
	 *
	 * @param event the event
	 */
	@Subscribe
	public void onComponentSpaceChange(SpaceChangeEvent event)
	{
		onContentUpdate();
	}
}
