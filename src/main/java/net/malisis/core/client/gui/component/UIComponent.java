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

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import com.google.common.eventbus.EventBus;

import net.malisis.core.ExceptionHandler;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.ClipArea;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.Padding.IPadding;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.control.IControlComponent;
import net.malisis.core.client.gui.component.decoration.UITooltip;
import net.malisis.core.client.gui.element.GuiShape;
import net.malisis.core.client.gui.element.SimpleGuiShape;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.malisis.core.client.gui.event.GuiEvent;
import net.malisis.core.client.gui.event.component.ContentUpdateEvent;
import net.malisis.core.client.gui.event.component.SpaceChangeEvent.PositionChangeEvent;
import net.malisis.core.client.gui.event.component.SpaceChangeEvent.SizeChangeEvent;
import net.malisis.core.client.gui.event.component.StateChangeEvent.DisabledStateChange;
import net.malisis.core.client.gui.event.component.StateChangeEvent.FocusStateChange;
import net.malisis.core.client.gui.event.component.StateChangeEvent.HoveredStateChange;
import net.malisis.core.client.gui.event.component.StateChangeEvent.VisibleStateChange;
import net.malisis.core.client.gui.render.IGuiRenderer;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.animation.transformation.ITransformable;
import net.malisis.core.renderer.icon.GuiIcon;
import net.malisis.core.renderer.icon.provider.IGuiIconProvider;
import net.malisis.core.renderer.icon.provider.IIconProvider;
import net.malisis.core.util.MouseButton;
import net.minecraft.client.renderer.GlStateManager;

/**
 * {@link UIComponent} is the base of everything drawn onto a GUI.<br>
 * The drawing is separated between background and foreground.<br>
 * Most of the events are launched from UIComponent.
 *
 * @author Ordinastie, PaleoCrafter
 * @param <T> the type of <code>UIComponent</code>
 */
public abstract class UIComponent<T extends UIComponent<T>>
		implements ITransformable.Position<T>, ITransformable.Size<T>, ITransformable.Color, ITransformable.Alpha, IKeyListener
{
	/** The Constant INHERITED. */
	public final static int INHERITED = 0;

	/** Reference to the {@link MalisisGui} this {@link UIComponent} was added to. */
	private final MalisisGui gui;
	/** Reference to the {@link GuiRenderer} that will draw this {@link UIComponent}. */
	protected final GuiRenderer renderer;
	/** List of {@link UIComponent components} controlling this {@link UIContainer}. */
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
	protected UIComponent<?> parent;
	/** The name of this {@link UIComponent}. Can be used to retrieve this back from a container. */
	protected String name;
	/** The tooltip for this {@link UIComponent} Automatically displayed when the {@link UIComponent} is hovered. */
	protected UITooltip tooltip;
	/** Determines whether this {@link UIComponent} is visible. */
	protected boolean visible = true;
	/** Determines whether this {@link UIComponent} is enabled. If set to false, will cancel any {@link GuiEvent events} received. */
	protected boolean enabled = true;
	/** Hover state of this {@link UIComponent}. */
	protected boolean hovered = false;
	/** Focus state of this {@link UIComponent}. */
	protected boolean focused = false;

	/** Rendering for the background of this {@link UIComponent}. */
	protected IGuiRenderer backgroundRenderer;
	/** GuiShape used to draw this {@link UIComponent}. */
	protected GuiShape shape;
	/** {@link RenderParameters} used to draw this {@link UIComponent}. */
	protected RenderParameters rp;
	/** {@link GuiIcon} used to draw this {@link UIComponent}. */
	protected IGuiIconProvider iconProvider;
	/** Alpha transparency of this {@link UIComponent}. */
	protected int alpha = 255;

	private Object data;

	/**
	 * Instantiates a new {@link UIComponent}.
	 *
	 * @param gui the gui
	 */
	public UIComponent(MalisisGui gui)
	{
		this.gui = gui;
		this.renderer = gui.getRenderer();
		bus = new EventBus(ExceptionHandler.instance);
		bus.register(this);
		controlComponents = new LinkedHashSet<>();
		rp = new RenderParameters();
		shape = new SimpleGuiShape();
	}

	// #region getters/setters
	@SuppressWarnings("unchecked")
	public T self()
	{
		return (T) this;
	}

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
	 * Gets the {@link GuiRenderer} that will draw this {@link UIComponent}.
	 *
	 * @return the renderer
	 */
	public GuiRenderer getRenderer()
	{
		return renderer;
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

		if (!fireEvent(new PositionChangeEvent<>(self(), x, y, anchor)))
		{
			//event is cancelled, restore old values
			this.x = oldX;
			this.y = oldY;
			this.anchor = oldAnchor;
			return self();
		}

		return self();
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
		return self();
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

		if (!fireEvent(new PositionChangeEvent<>(self(), x, y, anchor)))
		{
			//event is cancelled, restore old values
			this.anchor = oldAnchor;
			return self();
		}
		return self();
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

		if (!fireEvent(new SizeChangeEvent<>(self(), width, height)))
		{
			//event is cancelled, restore old values
			this.width = oldWidth;
			this.height = oldHeight;
			return self();
		}

		return self();
	}

	/**
	 * Gets the raw width of this {@link UIComponent}.
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
		if (parent instanceof IPadding)
			w -= ((IPadding) parent).getPadding().horizontal;

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
		if (parent instanceof IPadding)
			h -= ((IPadding) parent).getPadding().vertical;
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
		fireEvent(new HoveredStateChange<>(self(), hovered));

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
		if (!isEnabled())
			return;

		boolean flag = this.focused != focused;
		flag |= MalisisGui.setFocusedComponent(this, focused);
		if (!flag)
			return;

		this.focused = focused;
		fireEvent(new FocusStateChange<>(self(), focused));
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
	public UIComponent<?> getParent()
	{
		return parent;
	}

	/**
	 * Sets the parent of this {@link UIComponent}.
	 *
	 * @param parent the parent
	 */
	public void setParent(UIComponent<?> parent)
	{
		this.parent = parent;
		fireEvent(new ContentUpdateEvent<>(self()));
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
			return self();

		if (!fireEvent(new VisibleStateChange<>(self(), visible)))
			return self();

		this.visible = visible;
		if (!visible)
		{
			this.setHovered(false);
			this.setFocused(false);
		}

		return self();
	}

	/**
	 * Checks if this {@link UIComponent} is disabled.
	 *
	 * @return true if disabled
	 */
	public boolean isEnabled()
	{
		return enabled && (parent == null || parent.isEnabled());
	}

	/**
	 * Set the state of this {@link UIComponent}.
	 *
	 * @param enabled the new state
	 * @return this {@link UIComponent}
	 */
	public T setEnabled(boolean enabled)
	{
		if (isEnabled() == enabled)
			return self();

		if (!fireEvent(new DisabledStateChange<>(self(), enabled)))
			return self();

		this.enabled = enabled;
		if (enabled)
		{
			setHovered(false);
			setFocused(false);
		}
		return self();
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
	public T setName(String name)
	{
		this.name = name;
		return self();
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
		return self();
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
		return self();
	}

	@Override
	public void setColor(int color)
	{
		if (backgroundRenderer instanceof ITransformable.Color)
			((ITransformable.Color) backgroundRenderer).setColor(color);
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

	/**
	 * Sets the background for this {@link UIComponent}.
	 *
	 * @param render the new background
	 */
	public void setBackground(IGuiRenderer render)
	{
		this.backgroundRenderer = render;
	}

	public void attachData(Object data)
	{
		this.data = data;
	}

	public Object getData()
	{
		return this.data;
	}

	// #end getters/setters

	public IIconProvider getIconProvider()
	{
		return iconProvider;
	}

	/**
	 * Registers an <code>object</code> to handle events received by this {@link UIComponent}.
	 *
	 * @param object object whose handler methods should be registered
	 * @return this {@link UIComponent}
	 */
	public T register(Object object)
	{
		bus.register(object);
		return self();
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
		return self();
	}

	/**
	 * Fires a {@link ComponentEvent}.
	 *
	 * @param event the event
	 * @return true, if the even can propagate, false if cancelled
	 */
	public boolean fireEvent(ComponentEvent<?> event)
	{
		bus.post(event);
		return !event.isCancelled();
	}

	//#region Inputs

	/**
	 * On mouse move.
	 *
	 * @param lastX the last x
	 * @param lastY the last y
	 * @param x the x
	 * @param y the y
	 * @return true, if successful
	 */
	public boolean onMouseMove(int lastX, int lastY, int x, int y)
	{
		if (!isEnabled())
			return false;

		return parent != null ? parent.onMouseMove(lastX, lastY, x, y) : false;
	}

	/**
	 * On button press.
	 *
	 * @param x the x
	 * @param y the y
	 * @param button the button
	 * @return true, if successful
	 */
	public boolean onButtonPress(int x, int y, MouseButton button)
	{
		if (!isEnabled())
			return false;

		return parent != null ? parent.onButtonPress(x, y, button) : false;
	}

	/**
	 * On button release.
	 *
	 * @param x the x
	 * @param y the y
	 * @param button the button
	 * @return true, if successful
	 */
	public boolean onButtonRelease(int x, int y, MouseButton button)
	{
		if (!isEnabled())
			return false;

		return parent != null ? parent.onButtonRelease(x, y, button) : false;
	}

	/**
	 * On click.
	 *
	 * @param x the x
	 * @param y the y
	 * @return true, if successful
	 */
	public boolean onClick(int x, int y)
	{
		if (!isEnabled())
			return false;

		return parent != null ? parent.onClick(x, y) : false;
	}

	/**
	 * On right click.
	 *
	 * @param x the x
	 * @param y the y
	 * @return true, if successful
	 */
	public boolean onRightClick(int x, int y)
	{
		if (!isEnabled())
			return false;

		return parent != null ? parent.onRightClick(x, y) : false;
	}

	/**
	 * On double click.
	 *
	 * @param x the x
	 * @param y the y
	 * @param button the button
	 * @return true, if successful
	 */
	public boolean onDoubleClick(int x, int y, MouseButton button)
	{
		if (!isEnabled())
			return false;

		return parent != null ? parent.onDoubleClick(x, y, button) : false;
	}

	/**
	 * On drag.
	 *
	 * @param lastX the last x
	 * @param lastY the last y
	 * @param x the x
	 * @param y the y
	 * @param button the button
	 * @return true, if successful
	 */
	public boolean onDrag(int lastX, int lastY, int x, int y, MouseButton button)
	{
		if (!isEnabled())
			return false;

		return parent != null ? parent.onDrag(lastX, lastY, x, y, button) : false;
	}

	/**
	 * On scroll wheel.
	 *
	 * @param x the x
	 * @param y the y
	 * @param delta the delta
	 * @return true, if successful
	 */
	public boolean onScrollWheel(int x, int y, int delta)
	{
		if (!isEnabled())
			return false;

		for (IControlComponent c : controlComponents)
			if (c.onScrollWheel(x, y, delta))
				return true;

		return parent != null && !(this instanceof IControlComponent) ? parent.onScrollWheel(x, y, delta) : false;
	}

	@Override
	public boolean onKeyTyped(char keyChar, int keyCode)
	{
		if (!isEnabled())
			return false;

		for (IControlComponent c : controlComponents)
			if (c.onKeyTyped(keyChar, keyCode))
				return true;

		return parent != null && !(this instanceof IControlComponent) ? parent.onKeyTyped(keyChar, keyCode) : false;
	}

	//#end Inputs

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
	public UIComponent<?> getComponentAt(int x, int y)
	{
		//control components take precedence over regular components
		for (IControlComponent c : controlComponents)
		{
			UIComponent<?> component = c.getComponentAt(x, y);
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
	public int componentX(UIComponent<?> component)
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
	public int componentY(UIComponent<?> component)
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
	 * Triggers a {@link SizeChangeEvent} if this component size is relative.
	 */
	public void onAddedToScreen()
	{
		if (isRelativeWidth() || isRelativeHeight())
			fireEvent(new SizeChangeEvent<>(self(), getWidth(), getHeight()));
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
		if (getGui().isOverlay())
		{
			GlStateManager.blendFunc(GL11.GL_CONSTANT_ALPHA, GL11.GL_ONE_MINUS_CONSTANT_ALPHA);
			GL14.glBlendColor(1, 1, 1, (float) getAlpha() / 255);
		}

		//store last drawn component so that it can be set back after drawing.
		//makes sure components overriding rendering and calling super still have correct
		//relative position in case super renders other components.
		UIComponent<?> oldComponent = renderer.currentComponent;

		//draw background
		renderer.currentComponent = this;
		drawBackground(renderer, mouseX, mouseY, partialTick);
		renderer.next();

		//draw foreground
		renderer.currentComponent = this;

		ClipArea area = this instanceof IClipable ? ((IClipable) this).getClipArea() : null;
		boolean render = true;
		if (area != null)
		{
			renderer.startClipping(area);
			if (!area.noClip && (area.width() <= 0 || area.height() <= 0))
				render = false;
		}

		if (render)
		{
			drawForeground(renderer, mouseX, mouseY, partialTick);
			renderer.next();
		}

		if (area != null)
			renderer.endClipping(area);

		for (IControlComponent c : controlComponents)
			c.draw(renderer, mouseX, mouseY, partialTick);

		renderer.currentComponent = oldComponent;

		GL11.glPopAttrib();
	}

	/**
	 * Gets the property string.
	 *
	 * @return the property string
	 */
	public String getPropertyString()
	{
		return "P=" + (parent != null ? parent.getClass().getSimpleName() : "null") + " | " + width + "x" + height + "@" + x + "," + y
				+ " | C=" + parentX() + "," + parentY() + " | S=" + screenX() + "," + screenY();
	}

	/**
	 * To string.
	 *
	 * @return the string
	 */
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
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		if (backgroundRenderer != null)
			backgroundRenderer.render(this, renderer, mouseX, mouseY, partialTick);
	}

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
