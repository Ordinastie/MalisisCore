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
import net.malisis.core.client.gui.event.ComponentExceptionHandler;
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
 * {@link UIComponent} is the base of everything drawn onto a GUI.<br>
 * The drawing is separated between background and foreground.<br>
 * Most of the events are launched from UIComponent.
 *
 * @author Ordinastie, PaleoCrafter
 * @param <T> the type of <code>UIComponent</code>
 */
public abstract class UIComponent<T extends UIComponent> implements ITransformable.Position<T>, ITransformable.Size<T>,
		ITransformable.Alpha
{
	/** The Exception handler for all Compoenent events. */
	private static final ComponentExceptionHandler exceptionHandler = new ComponentExceptionHandler();
	public final static int INHERITED = 0;

	/** Reference to the {@link MalisisGui} this {@link UIComponent} was added to. */
	private final MalisisGui gui;
	/** List of {@link UIComponent components} controling this {@link UIContainer}. */
	private final Set<IControlComponent> controlComponents;
	/** Position of this {@link UIComponent}. */
	protected int x, y;
	/** Z index of the component. */
	protected int zIndex = INHERITED;
	/** Position anchor for this {@link UIComponent}. See {@link Anchor} */
	protected int anchor = Anchor.NONE;
	/** Size of this {@link UIComponent}. */
	protected int width = INHERITED, height = INHERITED;
	/** Event bus on which event listeners are registered. */
	private EventBus bus;
	/** The parent {@link UIComponent} of this <code>UIComponent</code>. */
	protected UIComponent parent;
	/** The name of this {@link UIComponent} Can be used to retrieve this back from a container. */
	protected String name;
	/** The tooltip for this {@link UIComponent} Automatically displayed when the {@link UIComponent} is hovered. */
	protected UITooltip tooltip;
	/** Determines whether this {@link UIComponent} is visible. */
	protected boolean visible = true;
	/**
	 * Determines whether this {@link UIComponent} is enabled. If set to false, will cancel any
	 * {@link net.malisis.core.client.gui.event.GuiEvent events} received.
	 */
	protected boolean disabled = false;
	/** Hover state of this {@link UIComponent}. */
	protected boolean hovered = false;
	/** Focus state of this {@link UIComponent}. */
	protected boolean focused = false;
	/** GuiShape used to draw this {@link UIComponent}. */
	protected GuiShape shape;
	/** {@link RenderParameters} used to draw this {@link UIComponent}. */
	protected RenderParameters rp;
	/** {@link GuiIcon} used to draw this {@link UIComponent}. */
	protected GuiIcon icon;
	/** Alpha transparency of this {@link UIComponent}. */
	protected int alpha = 255;

	/**
	 * Instantiates a new {@link UIComponent}.
	 *
	 * @param gui the gui
	 */
	public UIComponent(MalisisGui gui)
	{
		this.gui = gui;
		bus = new EventBus(exceptionHandler);
		bus.register(this);
		controlComponents = new LinkedHashSet<>();
		rp = new RenderParameters();
		shape = new SimpleGuiShape();
	}

	// #region getters/setters
	/**
	 * Gets the {@link MalisisGui} this {@link UIComponent} was added to.
	 *
	 * @return the gui
	 */
	public MalisisGui getGui()
	{
		return gui;
	}

	/**
	 * Sets the position of this {@link UIComponent}.
	 *
	 * @param x the x
	 * @param y the y
	 * @return this {@link UIComponent}
	 */
	@Override
	public T setPosition(int x, int y)
	{
		return setPosition(x, y, anchor);
	}

	/**
	 * Sets the position of this {@link UIComponent} relative to an anchor.
	 *
	 * @param x the x
	 * @param y the y
	 * @param anchor the anchor
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
	 * Gets the X coordinate of this {@link UIComponent}'s position.
	 *
	 * @return the coordinate
	 */
	public int getX()
	{
		return x;
	}

	/**
	 * Gets the Y coordinate of this {@link UIComponent}'s position.
	 *
	 * @return the coordinate
	 */
	public int getY()
	{
		return y;
	}

	/**
	 * Sets the zIndex for this {@link UIComponent}.
	 *
	 * @param zIndex the z index
	 * @return this {@link UIComponent}
	 */
	public T setZIndex(int zIndex)
	{
		this.zIndex = zIndex;
		return (T) this;
	}

	/**
	 * Gets the zIndex of this {@link UIComponent}.
	 *
	 * @return the zIndex
	 */
	public int getZIndex()
	{
		return zIndex == INHERITED ? (parent != null ? parent.getZIndex() : 0) : zIndex;
	}

	/**
	 * Sets the anchor for this {@link UIComponent}'s position.
	 *
	 * @param anchor the anchor
	 * @return this {@link UIComponent}
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
	 * Gets the anchor.
	 *
	 * @return the anchor of this {@link UIComponent}'s position
	 */
	public int getAnchor()
	{
		return anchor;
	}

	/**
	 * Sets the size of this {@link UIComponent}.
	 *
	 * @param width the width
	 * @param height the height
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
	 * Gets the raw width of this {@link UIComponent}
	 *
	 * @return the width
	 */
	public int getRawWidth()
	{
		return width;
	}

	/**
	 * Gets the width of this {@link UIComponent}.
	 *
	 * @return the width, or 0 for relative width without a parent
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
	 * Checks if the width of this {@link UIComponent} is relative to its parent <code>UIComponent</code>.
	 *
	 * @return true, if the width is relative
	 */
	public boolean isRelativeWidth()
	{
		return width <= 0;
	}

	/**
	 * Gets the raw height of this {@link UIComponent}.
	 *
	 * @return the height
	 */
	public int getRawHeight()
	{
		return height;
	}

	/**
	 * Gets the height of this {@link UIComponent}.
	 *
	 * @return the height, or 0 for relative width without a parent
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
	 * Checks if the height of this {@link UIComponent} is relative to its parent <code>UIComponent</code>.
	 *
	 * @return true, if the height is relative
	 */
	public boolean isRelativeHeight()
	{
		return height <= 0;
	}

	/**
	 * Sets the <code>hovered</code> state of this {@link UIComponent}.
	 *
	 * @param hovered the new state
	 */
	public void setHovered(boolean hovered)
	{
		boolean flag = this.hovered != hovered;
		flag |= MalisisGui.setHoveredComponent(this, hovered);
		if (!flag)
			return;

		this.hovered = hovered;
		fireEvent(new HoveredStateChange(this, hovered));

		if (tooltip != null && hovered)
			tooltip.animate();
	}

	/**
	 * Gets the <code>hovered</code> state of this {@link UIComponent}.
	 *
	 * @return true, this component is hovered
	 */
	public boolean isHovered()
	{
		return this.hovered;
	}

	/**
	 * Sets the <code>focused</code> state of this {@link UIComponent}.
	 *
	 * @param focused the state
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
	 * Gets the <code>focused</code> state of this {@link UIComponent}.
	 *
	 * @return true, if this component if focused
	 */
	public boolean isFocused()
	{
		return this.focused;
	}

	/**
	 * Gets the parent of this {@link UIComponent}.
	 *
	 * @return the parent
	 */
	public UIComponent getParent()
	{
		return parent;
	}

	/**
	 * Sets the parent of this {@link UIComponent}.
	 *
	 * @param parent the parent
	 */
	public void setParent(UIComponent parent)
	{
		this.parent = parent;
		fireEvent(new ContentUpdateEvent(this));
	}

	/**
	 * Checks if this {@link UIComponent} is visible.
	 *
	 * @return true, if visible
	 */
	public boolean isVisible()
	{
		return visible;
	}

	/**
	 * Sets the visibility of this {@link UIComponent}.
	 *
	 * @param visible the visibility for this component
	 * @return this {@link UIComponent}
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
	 * Checks if this {@link UIComponent} is disabled
	 *
	 * @return true if disabled
	 */
	public boolean isDisabled()
	{
		return disabled || (parent != null && parent.isDisabled());
	}

	/**
	 * Set the state of this {@link UIComponent}.
	 *
	 * @param disabled the new state
	 * @return this {@link UIComponent}
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
	 * Gets the name of this {@link UIComponent}.
	 *
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Sets the name of this {@link UIComponent}.
	 *
	 * @param name the name to be used
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Gets the {@link UITooltip} for this {@link UIComponent}.
	 *
	 * @return the tooltip
	 */
	public UITooltip getTooltip()
	{
		return tooltip;
	}

	/**
	 * Sets the {@link UITooltip} of this {@link UIComponent}.
	 *
	 * @param tooltip the tooltip
	 * @return this {@link UIComponent}
	 */
	public T setTooltip(UITooltip tooltip)
	{
		this.tooltip = tooltip;
		return (T) this;
	}

	/**
	 * Sets the {@link UITooltip} of this {@link UIComponent}.
	 *
	 * @param text the text of the tooltip
	 * @return the t
	 */
	public T setTooltip(String text)
	{
		setTooltip(new UITooltip(getGui(), text));
		return (T) this;
	}

	/**
	 * Sets the alpha transparency for this {@link UIComponent}.
	 *
	 * @param alpha the new alpha
	 */
	@Override
	public void setAlpha(int alpha)
	{
		this.alpha = alpha;
	}

	/**
	 * Gets the alpha transparency for this {@link UIComponent}.
	 *
	 * @return the alpha
	 */
	public int getAlpha()
	{
		if (getParent() == null)
			return alpha;

		return Math.min(alpha, parent.getAlpha());
	}

	// #end getters/setters

	/**
	 * Registers an <code>object</code> to handle events received by this {@link UIComponent}.
	 *
	 * @param object object whose handler methods should be registered
	 * @return this {@link UIComponent}
	 */
	public T register(Object object)
	{
		bus.register(object);
		return (T) this;
	}

	/**
	 * Unregister an <code>object</code> to stop receiving events for this {@link UIComponent}.
	 *
	 * @param object the object
	 * @return this {@link UIComponent}
	 */
	public T unregister(Object object)
	{
		bus.unregister(object);
		return (T) this;
	}

	/**
	 * Fires a {@link ComponentEvent}.
	 *
	 * @param event the event
	 * @return true, if the even can propagate, false if cancelled
	 */
	public boolean fireEvent(ComponentEvent event)
	{
		bus.post(event);
		return !event.isCancelled();
	}

	/**
	 * Fires a {@link MouseEvent}.
	 *
	 * @param event the event
	 * @return true, if the even can propagate, false if cancelled
	 */
	public boolean fireMouseEvent(MouseEvent event)
	{
		if (isDisabled() || !isVisible())
			return false;

		bus.post(event);
		return !event.isCancelled();
	}

	/**
	 * Fires a {@link KeyboardEvent}.
	 *
	 * @param event the event
	 * @return true, if the even can propagate, false if cancelled
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
	 * Checks if supplied coordinates are inside this {@link UIComponent} bounds.
	 *
	 * @param x the x
	 * @param y the y
	 * @return true, if coordinates are inside bounds
	 */
	public boolean isInsideBounds(int x, int y)
	{
		if (!isVisible())
			return false;
		return x >= screenX() && x <= screenX() + getWidth() && y >= screenY() && y <= screenY() + getHeight();
	}

	/**
	 * Gets the {@link UIComponent} at the specified coordinates.<br>
	 * Will return a {@link IControlComponent} if any. Checks if inside bounds, visible and not disabled.
	 *
	 * @param x the x
	 * @param y the y
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
	 * Gets the X coordinate relative to this {@link UIComponent}.
	 *
	 * @param x the x
	 * @return the coordinate
	 */
	public int relativeX(int x)
	{
		return x - screenX();
	}

	/**
	 * Gets the Y coordinate relative to this {@link UIComponent}.
	 *
	 * @param y the y
	 * @return the coordinate
	 */
	public int relativeY(int y)
	{
		return y - screenY();
	}

	/**
	 * Gets the X coordinate of a {@link UIComponent} inside this <code>UIComponent</code>.
	 *
	 * @param component the component
	 * @return the coordinate
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
	 * @param component the component
	 * @return the coordinate
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
	 * Gets the X coordinate of this {@link UIComponent} relative to its parent.
	 *
	 * @return the coordinate
	 */
	public int parentX()
	{
		return getParent() != null ? getParent().componentX(this) : getX();
	}

	/**
	 * Get the Y coordinate of this {@link UIComponent} relative to its parent.
	 *
	 * @return the coordinate
	 */
	public int parentY()
	{
		return getParent() != null ? getParent().componentY(this) : getY();
	}

	/**
	 * Gets the X coordinate of this {@link UIComponent} relative to the screen.
	 *
	 * @return the the coordinate
	 */
	public int screenX()
	{
		int x = parentX();
		if (getParent() != null)
			x += getParent().screenX();
		return x;
	}

	/**
	 * Gets the Y coordinate of this {@link UIComponent} relative to the screen.
	 *
	 * @return the coordinate
	 */
	public int screenY()
	{
		int y = parentY();
		if (getParent() != null)
			y += getParent().screenY();
		return y;
	}

	/**
	 * Adds a {@link IControlComponent} component to this {@link UIComponent}.
	 *
	 * @param component the component
	 */
	public void addControlComponent(IControlComponent component)
	{
		controlComponents.add(component);
		component.setParent(this);
	}

	/**
	 * Removes the {@link IControlComponent} from this {@link UIComponent}.
	 *
	 * @param component the component
	 */
	public void removeControlComponent(IControlComponent component)
	{
		if (component.getParent() != this)
			return;

		controlComponents.remove(component);
		component.setParent(null);
	}

	/**
	 * Removes all the {@link IControlComponent} from this {@link UIContainer}.
	 */
	public void removeAllControlComponents()
	{
		for (IControlComponent component : controlComponents)
			component.setParent(null);
		controlComponents.clear();
	}

	/**
	 * Called when this {@link UIComponent} is added to screen.<br>
	 * Triggers a {@link SizeChangeEvent} is this component size is relative.
	 */
	public void onAddedToScreen()
	{
		if (width <= 0 || height <= 0)
			fireEvent(new SizeChangeEvent<UIComponent>(this, getWidth(), getHeight()));
	}

	/**
	 * Draws this {@link UIComponent} Called by {@link #parent} component.<br>
	 * Will set the size of {@link #shape} according to the size of this <code>UIComponent</code><br>
	 * Rendering is surrounded by glPushAttrib(GL_ALL_ATTRIB_BITS) so no state should bleed between components. Also, a draw() is triggered
	 * between background and foreground.
	 *
	 * @param renderer the renderer
	 * @param mouseX the mouse x
	 * @param mouseY the mouse y
	 * @param partialTick the partial tick
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
		if (getAlpha() < 255)
		{
			GL11.glBlendFunc(GL11.GL_CONSTANT_ALPHA, GL11.GL_ONE_MINUS_CONSTANT_ALPHA);
			GL14.glBlendColor(1, 1, 1, (float) getAlpha() / 255);
		}

		//draw background
		renderer.currentComponent = this;
		drawBackground(renderer, mouseX, mouseY, partialTick);
		renderer.next();

		//draw foreground
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

	/**
	 * Gets the property string.
	 *
	 * @return the property string
	 */
	public String getPropertyString()
	{
		return "parent=" + (parent != null ? parent.getClass().getSimpleName() : "null") + ", size=" + width + "," + height
				+ " | position=" + x + "," + y + " | container=" + parentX() + "," + parentY() + " | screen=" + screenX() + "," + screenY();
	}

	@Override
	public String toString()
	{
		return (this.name == null ? getClass().getSimpleName() : this.name) + " [" + getPropertyString() + "]";
	}

	/**
	 * Called first when drawing this {@link UIComponent}.
	 *
	 * @param renderer the renderer
	 * @param mouseX the mouse x
	 * @param mouseY the mouse y
	 * @param partialTick the partial tick
	 */
	public abstract void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick);

	/**
	 * Called last when drawing this {@link UIComponent}.
	 *
	 * @param renderer the renderer
	 * @param mouseX the mouse x
	 * @param mouseY the mouse y
	 * @param partialTick the partial tick
	 */
	public abstract void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick);

}
