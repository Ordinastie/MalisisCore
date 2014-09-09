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

package net.malisis.core.client.gui.component;

import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.decoration.UITooltip;
import net.malisis.core.client.gui.component.interaction.IScrollable;
import net.malisis.core.client.gui.element.GuiShape;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.malisis.core.client.gui.event.ComponentEvent.FocusStateChanged;
import net.malisis.core.client.gui.event.ComponentEvent.HoveredStateChanged;
import net.malisis.core.client.gui.event.GuiEvent;
import net.malisis.core.client.gui.event.KeyboardEvent;
import net.malisis.core.client.gui.event.MouseEvent;
import net.malisis.core.renderer.RenderParameters;

import com.google.common.eventbus.EventBus;

/**
 * UIComponent
 * 
 * @author PaleoCrafter
 */
public abstract class UIComponent<T extends UIComponent>
{
	public final static int INHERITED = Integer.MIN_VALUE;
	/**
	 * Position of this <code>UIComponent</code>
	 */
	protected int x, y;
	/**
	 * Z index of the component
	 */
	protected int zIndex = INHERITED;
	/**
	 * Position anchor for this <code>UIComponent</code>. See {@link net.malisis.core.client.gui.Anchor Anchor}
	 */
	protected int anchor;
	/**
	 * Size of this <code>UIComponent</code>
	 */
	protected int width, height;
	/**
	 * Event bus on which event listeners are registered
	 */
	private EventBus bus;
	/**
	 * The parent <code>UIContainer</code> of this <code>UIComponent</code>. Can be used to pass through things or manipulate the parent's
	 * other children.
	 */
	protected UIContainer parent;
	/**
	 * The name of this <code>UIComponent</code>. Can be used to retrieve this back from a container.
	 */
	protected String name;
	/**
	 * The tooltip for this <code>UIComponent</code>. Automatically displayed when the <code>UIComponent</code> is hovered.
	 */
	protected UITooltip tooltip;
	/**
	 * Determines whether this <code>UIComponent</code> is visible. If set to false, {@link #size size} will be ignored by most layout
	 * managers.
	 */
	protected boolean visible = true;
	/**
	 * Determines whether this <code>UIComponent</code> is enabled. If set to false, will cancel any
	 * {@link net.malisis.core.client.gui.event.GuiEvent events} received.
	 */
	protected boolean disabled = false;
	/**
	 * Hover state of this <code>UIComponent</code>.
	 */
	protected boolean hovered;
	/**
	 * Focus state of this <code>UIComponent</code>.
	 */
	protected boolean focused;
	/**
	 * GuiShape used to draw this <code>UIComponent</code>.
	 */
	protected GuiShape shape;
	/**
	 * RenderParameters used to draw this <code>UIComponent</code>.
	 */
	protected RenderParameters rp;

	public UIComponent()
	{
		bus = new EventBus();
		bus.register(this);
		visible = true;
		rp = new RenderParameters();
	}

	/**
	 * Register an <code>object</code> to handle events received by this <code>UIComponent</code>.
	 * 
	 * @param object object whose handler methods should be registered
	 */
	public T register(Object object)
	{
		bus.register(object);
		return (T) this;
	}

	public T unregister(Object object)
	{
		bus.unregister(object);
		return (T) this;
	}

	public boolean fireEvent(ComponentEvent event)
	{
		bus.post(event);
		return !event.isCancelled();
	}

	/**
	 * Fire a {@link GuiEvent}
	 * 
	 * @param event
	 * @return
	 */
	public boolean fireMouseEvent(MouseEvent event)
	{
		if (isDisabled() || !isVisible())
			return false;

		bus.post(event);
		return !event.isCancelled();
	}

	public boolean fireKeyboardEvent(KeyboardEvent event)
	{
		if (isDisabled())
			return false;

		bus.post(event);
		return !event.isCancelled();
	}

	// #region getters/setters
	/**
	 * Set the position of this <code>UIComponent</code>.
	 * 
	 * @param x
	 * @param y
	 * @return this <code>UIComponent</code>
	 */
	public T setPosition(int x, int y)
	{
		return setPosition(x, y, Anchor.NONE);
	}

	/**
	 * Set the position of this <code>UIComponent</code> relative to an anchor.
	 * 
	 * @param x
	 * @param y
	 * @param anchor
	 * @return this <code>UIComponent</code>
	 */
	public T setPosition(int x, int y, int anchor)
	{
		this.x = x;
		this.y = y;
		this.anchor = anchor;
		if (parent != null)
			parent.onContentUpdate();
		return (T) this;
	}

	/**
	 * @return the X coordinate of this <code>UIComponent</code>'s position
	 */
	public int getX()
	{
		return x;
	}

	/**
	 * @return the Y coordinate of this <code>UIComponent</code>'s position
	 */
	public int getY()
	{
		return y;
	}

	public T setZIndex(int zIndex)
	{
		this.zIndex = zIndex;
		return (T) this;
	}

	public int getZIndex()
	{
		return zIndex == INHERITED ? 0 : zIndex;
	}

	/**
	 * Set the anchor for this <code>UIComponent</code>'s position
	 * 
	 * @param anchor
	 * @return
	 */
	public T setAnchor(int anchor)
	{
		this.anchor = anchor;
		return (T) this;
	}

	/**
	 * @return the anchor of this <code>UIComponent</code>'s position
	 */
	public int getAnchor()
	{
		return anchor;
	}

	/**
	 * Set the size of this <code>UIComponent</code>.
	 * 
	 * @param width
	 * @param height
	 * @return this <code>UIComponent</code>
	 */
	public T setSize(int width, int height)
	{
		this.width = width;
		this.height = height;
		if (parent != null)
			parent.onContentUpdate();
		return (T) this;
	}

	/**
	 * @return the width of this <code>UIComponent</code>
	 */
	public int getWidth()
	{
		return width;
	}

	/**
	 * @return the height of this <code>UIComponent</code>
	 */
	public int getHeight()
	{
		return height;
	}

	/**
	 * Set the <code>hovered</code> state of this <code>UIComponent</code>.
	 * 
	 * @param hovered
	 */
	public void setHovered(boolean hovered)
	{
		boolean flag = this.hovered != hovered;
		flag |= MalisisGui.setHoveredComponent(this, hovered);
		if (!flag)
			return;

		this.hovered = hovered;
		fireEvent(new HoveredStateChanged(this, hovered));
	}

	/**
	 * Get the <code>hovered</code> state of this <code>UIComponent</code>.
	 * 
	 * @return hovered state
	 */
	public boolean isHovered()
	{
		return this.hovered;
	}

	/**
	 * Set the <code>focused</code> state of this <code>UIComponent</code>.
	 * 
	 * @param focused
	 */
	public void setFocused(boolean focused)
	{
		if (isDisabled())
			return;

		boolean flag = this.focused != focused;
		flag |= MalisisGui.setFocusedComponent(this, focused);
		if (!flag)
			return;

		this.focused = focused;
		fireEvent(new FocusStateChanged(this, focused));
	}

	/**
	 * Get the <code>focused</code> state of this <code>UIComponent</code>.
	 * 
	 * @return focused state
	 */
	public boolean isFocused()
	{
		return this.focused;
	}

	/**
	 * @return the parent of this <code>UIComponent</code>
	 * @see #parent
	 */
	public UIContainer getParent()
	{
		return parent;
	}

	/**
	 * Set the parent of this <code>UIComponent</code>.
	 * 
	 * @param parent the parent to be used
	 * @see #parent
	 */
	public void setParent(UIContainer parent)
	{
		this.parent = parent;
	}

	/**
	 * @return the visibility of this component
	 */
	public boolean isVisible()
	{
		return visible;
	}

	/**
	 * Set the visibility of this component.
	 * 
	 * @param visible the visibility for this component
	 */
	public T setVisible(boolean visible)
	{
		this.visible = visible;
		if (!visible)
		{
			this.setHovered(false);
			this.setFocused(false);
		}

		if (parent != null)
			parent.onContentUpdate();

		return (T) this;
	}

	/**
	 * @return the state of this component
	 */
	public boolean isDisabled()
	{
		return disabled || (parent != null && parent.isDisabled());
	}

	/**
	 * Set the state of this <code>UIComponent</code>.
	 * 
	 * @param enabled true for the component to be enabled
	 */
	public T setDisabled(boolean disabled)
	{
		this.disabled = disabled;
		if (disabled)
		{
			setHovered(false);
			setFocused(false);
		}
		return (T) this;
	}

	/**
	 * @return the name of this <code>UIComponent</code>
	 * @see #name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Set the name of this <code>UIComponent</code>.
	 * 
	 * @param name the name to be used
	 * @see #name
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return the tooltip of this <code>UIComponent</code>.
	 * @see #tooltip
	 */
	public UITooltip getTooltip()
	{
		return tooltip;
	}

	/**
	 * Set the tooltip of this <code>UIComponent</code>.
	 * 
	 * @param tooltip the tooltip for this <code>UIComponent</code>
	 * @see #tooltip
	 */
	public T setTooltip(UITooltip tooltip)
	{
		this.tooltip = tooltip;
		return (T) this;
	}

	/**
	 * Set the text for the tooltip
	 * 
	 * @param text
	 */
	public T setTooltip(String text)
	{
		this.tooltip = new UITooltip(text);
		return (T) this;
	}

	// #end getters/setters

	/**
	 * Check if supplied coordinates are inside this <code>UIComponent</code> bounds.
	 * 
	 * @param x
	 * @param y
	 * @return true if coordinates are inside bounds
	 */
	public boolean isInsideBounds(int x, int y)
	{
		if (!isVisible())
			return false;
		return x >= screenX() && x <= screenX() + width && y >= screenY() && y <= screenY() + height;
	}

	/**
	 * 
	 */
	public UIComponent getComponentAt(int x, int y)
	{
		return isInsideBounds(x, y) && !isDisabled() && isVisible() ? this : null;
	}

	/**
	 * Get the X coordinate relative to this <code>UIComponent</code>
	 * 
	 * @param x
	 * @return
	 */
	public int componentX(int x)
	{
		return x - screenX();
	}

	/**
	 * Get the Y coordinate relative to this <code>UIComponent</code>
	 * 
	 * @param y
	 * @return
	 */
	public int componentY(int y)
	{
		return y - screenY();
	}

	/**
	 * Get the X coordinate of this <code>UIComponent</code> relative to its parent container
	 * 
	 * @return
	 */
	public int containerX()
	{
		return parent == null ? this.x : parent.componentX(this);

	}

	/**
	 * Get the Y coordinate of this <code>UIComponent</code> relative to its parent container
	 * 
	 * @return
	 */
	public int containerY()
	{
		return parent == null ? this.y : parent.componentY(this);
	}

	/**
	 * Get the X coordinate of this <code>UIComponent</code> relative to the screen.
	 * 
	 * @return
	 */
	public int screenX()
	{
		int x = containerX();
		if (parent != null)
		{
			x += parent.screenX();
			if (parent instanceof IScrollable)
				x += ((IScrollable) parent).getOffsetX();
		}

		return x;
	}

	/**
	 * Get the Y coordinate of this <code>UIComponent</code> relative to the screen.
	 * 
	 * @return
	 */
	public int screenY()
	{
		int y = containerY();
		if (parent != null)
		{
			y += parent.screenY();
			if (parent instanceof IScrollable)
				y += ((IScrollable) parent).getOffsetY();
		}
		return y;
	}

	/**
	 * Draw this <code>UIComponent</code>. Called by {@link #parent} container.
	 * 
	 * @param renderer
	 * @param mouseX
	 * @param mouseY
	 * @param partialTick
	 */
	public void draw(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		if (!isVisible())
			return;

		if (shape != null)
		{
			shape.resetState();
			shape.setSize(width, height);
		}
		renderer.currentComponent = this;
		drawBackground(renderer, mouseX, mouseY, partialTick);
		renderer.next();
		renderer.currentComponent = this;
		drawForeground(renderer, mouseX, mouseY, partialTick);
		renderer.next();
	}

	@Override
	public String toString()
	{
		return (this.name == null ? getClass().getSimpleName() : this.name) + " : [" + getPropertyString() + "]";
	}

	public String getPropertyString()
	{
		return "size=" + width + "," + height + " | position=" + x + "," + y + " | container=" + containerX() + "," + containerY()
				+ " | screen=" + screenX() + "," + screenY();
	}

	/**
	 * Called first when drawing this <code>UIComponent</code>.
	 * 
	 * @param renderer
	 * @param mouseX
	 * @param mouseY
	 * @param partialTick
	 */
	public abstract void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick);

	/**
	 * Called last when drawing this <code>UIComponent</code>.
	 * 
	 * @param renderer
	 * @param mouseX
	 * @param mouseY
	 * @param partialTick
	 */
	public abstract void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick);

}
