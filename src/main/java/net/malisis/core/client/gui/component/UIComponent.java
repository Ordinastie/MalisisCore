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

import java.util.LinkedHashSet;
import java.util.Set;

import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.ClipArea;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.control.IControlComponent;
import net.malisis.core.client.gui.component.decoration.UITooltip;
import net.malisis.core.client.gui.element.GuiShape;
import net.malisis.core.client.gui.element.SimpleGuiShape;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.malisis.core.client.gui.event.KeyboardEvent;
import net.malisis.core.client.gui.event.MouseEvent;
import net.malisis.core.client.gui.event.component.ContentUpdateEvent;
import net.malisis.core.client.gui.event.component.SpaceChangeEvent.PositionChangeEvent;
import net.malisis.core.client.gui.event.component.SpaceChangeEvent.SizeChangeEvent;
import net.malisis.core.client.gui.event.component.StateChangeEvent.DisabledStateChange;
import net.malisis.core.client.gui.event.component.StateChangeEvent.FocusStateChange;
import net.malisis.core.client.gui.event.component.StateChangeEvent.HoveredStateChange;
import net.malisis.core.client.gui.event.component.StateChangeEvent.VisibleStateChange;
import net.malisis.core.client.gui.icon.GuiIcon;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.animation.transformation.ITransformable;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import com.google.common.eventbus.EventBus;

/**
 * {@link UIComponent} is the base of everything drawn onto a GUI.<br />
 * The drawing is separated between background and foreground.<br />
 * Most of the events are launched from UIComponent.
 *
 * @author Ordinastie, PaleoCrafter
 */
public abstract class UIComponent<T extends UIComponent> implements ITransformable.Position<T>, ITransformable.Size<T>,
		ITransformable.Alpha
{
	public final static int INHERITED = 0;
	/**
	 * Reference to the {@link MalisisGui} this {@link UIComponent} was added to
	 */
	private final MalisisGui gui;
	/**
	 * List of {@link UIComponent components} controling this {@link UIContainer}.
	 */
	private final Set<IControlComponent> controlComponents;
	/**
	 * Position of this {@link UIComponent}
	 */
	protected int x, y;
	/**
	 * Z index of the component
	 */
	protected int zIndex = INHERITED;
	/**
	 * Position anchor for this {@link UIComponent}. See {@link Anchor}
	 */
	protected int anchor = Anchor.NONE;
	/**
	 * Size of this {@link UIComponent}
	 */
	protected int width = INHERITED, height = INHERITED;
	/**
	 * Event bus on which event listeners are registered
	 */
	private EventBus bus;
	/**
	 * The parent {@link UIContainer} of this {@link UIComponent} Can be used to pass through things or manipulate the parent's other
	 * children.
	 */
	protected UIComponent parent;
	/**
	 * The name of this {@link UIComponent} Can be used to retrieve this back from a container.
	 */
	protected String name;
	/**
	 * The tooltip for this {@link UIComponent} Automatically displayed when the {@link UIComponent} is hovered.
	 */
	protected UITooltip tooltip;
	/**
	 * Determines whether this {@link UIComponent} is visible. If set to false, {@link #size size} will be ignored by most layout managers.
	 */
	protected boolean visible = true;
	/**
	 * Determines whether this {@link UIComponent} is enabled. If set to false, will cancel any
	 * {@link net.malisis.core.client.gui.event.GuiEvent events} received.
	 */
	protected boolean disabled = false;
	/**
	 * Hover state of this {@link UIComponent}
	 */
	protected boolean hovered = false;
	/**
	 * Focus state of this {@link UIComponent}
	 */
	protected boolean focused = false;
	/**
	 * GuiShape used to draw this {@link UIComponent}
	 */
	protected GuiShape shape;
	/**
	 * {@link RenderParameters} used to draw this {@link UIComponent}
	 */
	protected RenderParameters rp;
	/**
	 * {@link GuiIcon} used to draw this {@link UIComponent}
	 */
	protected GuiIcon icon;

	protected int alpha = 255;

	public UIComponent(MalisisGui gui)
	{
		this.gui = gui;
		bus = new EventBus();
		bus.register(this);
		controlComponents = new LinkedHashSet<>();
		rp = new RenderParameters();
		shape = new SimpleGuiShape();
	}

	// #region getters/setters
	/**
	 * @return the {@link MalisisGui} this {@link UIComponent} was added to.
	 */
	public MalisisGui getGui()
	{
		return gui;
	}

	/**
	 * Set the position of this {@link UIComponent}
	 *
	 * @param x
	 * @param y
	 * @return this {@link UIComponent}
	 */
	@Override
	public T setPosition(int x, int y)
	{
		return setPosition(x, y, anchor);
	}

	/**
	 * Set the position of this {@link UIComponent} relative to an anchor.
	 *
	 * @param x
	 * @param y
	 * @param anchor
	 * @return this {@link UIComponent}
	 */
	public T setPosition(int x, int y, int anchor)
	{
		//backup values
		int oldX = this.x;
		int oldY = this.y;
		int oldAnchor = this.anchor;

		this.x = x;
		this.y = y;
		this.anchor = anchor;

		if (!fireEvent(new PositionChangeEvent(this, x, y, anchor)))
		{
			//event is cancelled, restore old values
			this.x = oldX;
			this.y = oldY;
			this.anchor = oldAnchor;
			return (T) this;
		}

		return (T) this;
	}

	/**
	 * @return the X coordinate of this {@link UIComponent}'s position
	 */
	public int getX()
	{
		return x;
	}

	/**
	 * @return the Y coordinate of this {@link UIComponent}'s position
	 */
	public int getY()
	{
		return y;
	}

	/**
	 * Sets the zIndex for this {@link UIComponent}.
	 *
	 * @param zIndex
	 * @return
	 */
	public T setZIndex(int zIndex)
	{
		this.zIndex = zIndex;
		return (T) this;
	}

	/**
	 * @return the zIndex of this {@link UIComponent}.
	 */
	public int getZIndex()
	{
		return zIndex == INHERITED ? (parent != null ? parent.getZIndex() : 0) : zIndex;
	}

	/**
	 * Sets the anchor for this {@link UIComponent}'s position
	 *
	 * @param anchor
	 * @return
	 */
	public T setAnchor(int anchor)
	{
		int oldAnchor = this.anchor;
		this.anchor = anchor;

		if (!fireEvent(new PositionChangeEvent(this, x, y, anchor)))
		{
			//event is cancelled, restore old values
			this.anchor = oldAnchor;
			return (T) this;
		}
		return (T) this;
	}

	/**
	 * @return the anchor of this {@link UIComponent}'s position
	 */
	public int getAnchor()
	{
		return anchor;
	}

	/**
	 * Sets the size of this {@link UIComponent}
	 *
	 * @param width
	 * @param height
	 * @return this {@link UIComponent}
	 */
	@Override
	public T setSize(int width, int height)
	{
		int oldWidth = this.width;
		int oldHeight = this.height;

		this.width = width;
		this.height = height;

		if (!fireEvent(new SizeChangeEvent(this, width, height)))
		{
			//event is cancelled, restore old values
			this.width = oldWidth;
			this.height = oldHeight;
			return (T) this;
		}

		return (T) this;
	}

	/**
	 * @return the raw width of this {@link UIComponent}
	 */
	public int getRawWidth()
	{
		return width;
	}

	/**
	 * @return the width of this {@link UIComponent}
	 */
	public int getWidth()
	{
		if (width > 0)
			return width;

		if (parent == null)
			return 0;

		//if width < 0 consider it relative to parent container
		int w = parent.getWidth() + width;
		if (parent instanceof UIContainer)
			w -= 2 * ((UIContainer) parent).getHorizontalPadding();

		return w;
	}

	/**
	 * @return true if the width of this {@link UIComponent} is relative to its parent <code>UIComponent</code>.
	 */
	public boolean isRelativeWidth()
	{
		return width <= 0;
	}

	/**
	 * @return the raw width of this {@link UIComponent}
	 */
	public int getRawHeight()
	{
		return height;
	}

	/**
	 * @return the height of this {@link UIComponent}
	 */
	public int getHeight()
	{
		if (height > 0)
			return height;

		if (parent == null)
			return 0;

		//if height < 0 consider it relative to parent container
		int h = parent.getHeight() + height;
		if (parent instanceof UIContainer)
			h -= 2 * ((UIContainer) parent).getVerticalPadding();

		return h;
	}

	/**
	 * @return true if the height of this {@link UIComponent} is relative to its parent <code>UIComponent</code>.
	 */
	public boolean isRelativeHeight()
	{
		return height <= 0;
	}

	/**
	 * Set the <code>hovered</code> state of this {@link UIComponent}
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
		fireEvent(new HoveredStateChange(this, hovered));
	}

	/**
	 * Get the <code>hovered</code> state of this {@link UIComponent}
	 *
	 * @return hovered state
	 */
	public boolean isHovered()
	{
		return this.hovered;
	}

	/**
	 * Set the <code>focused</code> state of this {@link UIComponent}
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
		fireEvent(new FocusStateChange(this, focused));
	}

	/**
	 * Get the <code>focused</code> state of this {@link UIComponent}
	 *
	 * @return focused state
	 */
	public boolean isFocused()
	{
		return this.focused;
	}

	/**
	 * @return the parent of this {@link UIComponent}
	 * @see #parent
	 */
	public UIComponent getParent()
	{
		return parent;
	}

	/**
	 * Set the parent of this {@link UIComponent}
	 *
	 * @param parent the parent to be used
	 * @see #parent
	 */
	public void setParent(UIComponent parent)
	{
		this.parent = parent;
		fireEvent(new ContentUpdateEvent(this));
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
		if (isVisible() == visible)
			return (T) this;

		if (!fireEvent(new VisibleStateChange(this, visible)))
			return (T) this;

		this.visible = visible;
		if (!visible)
		{
			this.setHovered(false);
			this.setFocused(false);
		}

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
	 * Set the state of this {@link UIComponent}
	 *
	 * @param enabled true for the component to be enabled
	 */
	public T setDisabled(boolean disabled)
	{
		if (isDisabled() == disabled)
			return (T) this;

		if (!fireEvent(new DisabledStateChange(this, disabled)))
			return (T) this;

		this.disabled = disabled;
		if (disabled)
		{
			setHovered(false);
			setFocused(false);
		}
		return (T) this;
	}

	/**
	 * @return the name of this {@link UIComponent}
	 * @see #name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Set the name of this {@link UIComponent}
	 *
	 * @param name the name to be used
	 * @see #name
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return the tooltip of this {@link UIComponent}
	 * @see #tooltip
	 */
	public UITooltip getTooltip()
	{
		return tooltip;
	}

	/**
	 * Set the tooltip of this {@link UIComponent}
	 *
	 * @param tooltip the tooltip for this {@link UIComponent}
	 * @see #tooltip
	 */
	public T setTooltip(UITooltip tooltip)
	{
		this.tooltip = tooltip;
		return (T) this;
	}

	@Override
	public void setAlpha(int alpha)
	{
		this.alpha = alpha;
	}

	public int getAlpha()
	{
		if (getParent() == null)
			return alpha;

		return Math.min(alpha, parent.getAlpha());
	}

	// #end getters/setters

	/**
	 * Registers an <code>object</code> to handle events received by this {@link UIComponent}
	 *
	 * @param object object whose handler methods should be registered
	 */
	public T register(Object object)
	{
		bus.register(object);
		return (T) this;
	}

	/**
	 * Unregister an <code>object</code> to stop receiving events for this {@link UIComponent}
	 *
	 * @param object
	 * @return
	 */
	public T unregister(Object object)
	{
		bus.unregister(object);
		return (T) this;
	}

	/**
	 * Fires a {@link ComponentEvent}
	 *
	 * @param event
	 * @return
	 */
	public boolean fireEvent(ComponentEvent event)
	{
		bus.post(event);
		return !event.isCancelled();
	}

	/**
	 * Fires a {@link MouseEvent}
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

	/**
	 * Fires a {@link KeyboardEvent}
	 *
	 * @param event
	 * @return
	 */
	public boolean fireKeyboardEvent(KeyboardEvent event)
	{
		if (isDisabled())
			return false;

		for (IControlComponent c : controlComponents)
			c.fireKeyboardEvent(event);

		bus.post(event);
		return !event.isCancelled();
	}

	/**
	 * Check if supplied coordinates are inside this {@link UIComponent} bounds.
	 *
	 * @param x
	 * @param y
	 * @return true if coordinates are inside bounds
	 */
	public boolean isInsideBounds(int x, int y)
	{
		if (!isVisible())
			return false;
		return x >= screenX() && x <= screenX() + getWidth() && y >= screenY() && y <= screenY() + getHeight();
	}

	/**
	 * Gets the component at the specified coordinates.<br />
	 * Checks if inside bounds, visible and not disabled.
	 *
	 * @param x
	 * @param y
	 * @return this {@link UIComponent} or null if outside its bounds.
	 */
	public UIComponent getComponentAt(int x, int y)
	{
		//control components take precedence over regular components
		for (IControlComponent c : controlComponents)
		{
			UIComponent component = c.getComponentAt(x, y);
			if (component != null)
				return component;
		}

		return isInsideBounds(x, y) ? this : null;
	}

	/**
	 * Get the X coordinate relative to this {@link UIComponent}
	 *
	 * @param x
	 * @return
	 */
	public int relativeX(int x)
	{
		return x - screenX();
	}

	/**
	 * Get the Y coordinate relative to this {@link UIComponent}
	 *
	 * @param y
	 * @return
	 */
	public int relativeY(int y)
	{
		return y - screenY();
	}

	/**
	 * Gets the X coordinate of a {@link UIComponent} inside this <code>UIComponent</code>.
	 *
	 * @param component
	 * @return
	 */
	public int componentX(UIComponent component)
	{
		int x = component.getX();
		int w = getWidth() - component.getWidth();
		int a = Anchor.horizontal(component.getAnchor());
		if (a == Anchor.CENTER)
			x += w / 2;
		else if (a == Anchor.RIGHT)
			x += w;
		return x;
	}

	/**
	 * Gets the Y coordinate of a {@link UIComponent} inside this <code>UIComponent</code>.
	 *
	 * @param component
	 * @return
	 */
	public int componentY(UIComponent component)
	{
		int y = component.getY();
		int h = getHeight() - component.getHeight();
		int a = Anchor.vertical(component.getAnchor());
		if (a == Anchor.MIDDLE)
			y += h / 2;
		else if (a == Anchor.BOTTOM)
			y += h;
		return y;
	}

	/**
	 * Get the X coordinate of this {@link UIComponent} relative to its parent.
	 *
	 * @return
	 */
	public int parentX()
	{
		return getParent() != null ? getParent().componentX(this) : getX();
	}

	/**
	 * Get the Y coordinate of this {@link UIComponent} relative to its parent.
	 *
	 * @return
	 */
	public int parentY()
	{
		return getParent() != null ? getParent().componentY(this) : getY();
	}

	/**
	 * Get the X coordinate of this {@link UIComponent} relative to the screen.
	 *
	 * @return
	 */
	public int screenX()
	{
		int x = parentX();
		if (getParent() != null)
			x += getParent().screenX();
		return x;
	}

	/**
	 * Get the Y coordinate of this {@link UIComponent} relative to the screen.
	 *
	 * @return
	 */
	public int screenY()
	{
		int y = parentY();
		if (getParent() != null)
			y += getParent().screenY();
		return y;
	}

	/**
	 * Adds a control component to this {@link UIContainer}.
	 *
	 * @param component
	 */
	public void addControlComponent(IControlComponent component)
	{
		controlComponents.add(component);
		component.setParent(this);
	}

	/**
	 * Removes the component from this {@link UIContainer}.
	 *
	 * @param component
	 */
	public void removeControlComponent(IControlComponent component)
	{
		if (component.getParent() != this)
			return;

		controlComponents.remove(component);
		component.setParent(null);
	}

	/**
	 * Removes all the control components from this {@link UIContainer}. Does not remove regular components
	 */
	public void removeAllControlComponents()
	{
		for (IControlComponent component : controlComponents)
			component.setParent(null);
		controlComponents.clear();
	}

	public void onAddedToScreen()
	{
		if (width <= 0)
			fireEvent(new SizeChangeEvent<UIComponent>(this, getWidth(), getHeight()));
	}

	/**
	 * Draw this {@link UIComponent} Called by {@link #parent} container.<br />
	 * Will set the size of <i>shape</i> according to the size of this {@link UIComponent} <br />
	 * Rendering is surrounded by glPushAttrib(GL_ALL_ATTRIB_BITS) so no state should bleed between components. Also, a draw() is triggered
	 * between background and foreground.
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
			shape.setSize(getWidth(), getHeight());
		}
		if (rp != null)
			rp.reset();

		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		GL11.glBlendFunc(GL11.GL_CONSTANT_ALPHA, GL11.GL_ONE_MINUS_CONSTANT_ALPHA);
		GL14.glBlendColor(1, 1, 1, (float) getAlpha() / 255);

		//draw background
		renderer.currentComponent = this;
		drawBackground(renderer, mouseX, mouseY, partialTick);
		renderer.next();

		//draw forgeground
		renderer.currentComponent = this;

		ClipArea area = this instanceof IClipable ? ((IClipable) this).getClipArea() : null;
		if (area != null)
			renderer.startClipping(area);

		//GL11.glColor4f(1, 1, 1, 0.5F);

		drawForeground(renderer, mouseX, mouseY, partialTick);

		if (area != null)
			renderer.endClipping(area);

		renderer.next();

		for (IControlComponent c : controlComponents)
			c.draw(renderer, mouseX, mouseY, partialTick);

		GL11.glPopAttrib();
	}

	@Override
	public String toString()
	{
		return (this.name == null ? getClass().getSimpleName() : this.name) + " : [" + getPropertyString() + "]";
	}

	public String getPropertyString()
	{
		return "parent=" + (parent != null ? parent.getClass().getSimpleName() : "null") + ", size=" + width + "," + height
				+ " | position=" + x + "," + y + " | container=" + parentX() + "," + parentY() + " | screen=" + screenX() + "," + screenY();
	}

	/**
	 * Called first when drawing this {@link UIComponent}
	 *
	 * @param renderer
	 * @param mouseX
	 * @param mouseY
	 * @param partialTick
	 */
	public abstract void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick);

	/**
	 * Called last when drawing this {@link UIComponent}
	 *
	 * @param renderer
	 * @param mouseX
	 * @param mouseY
	 * @param partialTick
	 */
	public abstract void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick);

}
