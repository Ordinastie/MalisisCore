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

import static com.google.common.base.Preconditions.*;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import com.google.common.eventbus.EventBus;

import net.malisis.core.ExceptionHandler;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.content.IContent;
import net.malisis.core.client.gui.component.control.IControlComponent;
import net.malisis.core.client.gui.component.decoration.UITooltip;
import net.malisis.core.client.gui.element.IChild;
import net.malisis.core.client.gui.element.IClipable;
import net.malisis.core.client.gui.element.IClipable.ClipArea;
import net.malisis.core.client.gui.element.IKeyListener;
import net.malisis.core.client.gui.element.IOffset;
import net.malisis.core.client.gui.element.Padding;
import net.malisis.core.client.gui.element.Padding.IPadded;
import net.malisis.core.client.gui.element.Size;
import net.malisis.core.client.gui.element.Size.ISize;
import net.malisis.core.client.gui.element.Sizes;
import net.malisis.core.client.gui.element.position.Position;
import net.malisis.core.client.gui.element.position.Position.IPosition;
import net.malisis.core.client.gui.element.position.Position.ScreenPosition;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.malisis.core.client.gui.event.GuiEvent;
import net.malisis.core.client.gui.event.component.ContentUpdateEvent;
import net.malisis.core.client.gui.event.component.StateChangeEvent.DisabledStateChange;
import net.malisis.core.client.gui.event.component.StateChangeEvent.FocusStateChange;
import net.malisis.core.client.gui.event.component.StateChangeEvent.HoveredStateChange;
import net.malisis.core.client.gui.event.component.StateChangeEvent.VisibleStateChange;
import net.malisis.core.client.gui.render.GuiRenderer;
import net.malisis.core.client.gui.render.IGuiRenderer;
import net.malisis.core.util.MouseButton;

/**
 * {@link UIComponent} is the base of everything drawn onto a GUI.<br>
 * The drawing is separated between background and foreground.<br>
 * Most of the events are launched from UIComponent.
 *
 * @author Ordinastie, PaleoCrafter
 */
public abstract class UIComponent implements IContent, IGuiRenderer, IKeyListener, IChild<UIComponent>
{
	/** Reference to the {@link MalisisGui} this {@link UIComponent} was added to. Set when the component is added to screen. */
	protected MalisisGui gui;
	/** List of {@link UIComponent components} controlling this {@link UIContainer}. */
	protected final Set<IControlComponent> controlComponents;
	/** Position of this {@link UIComponent}. */
	protected IPosition position = Position.zero(this);
	/** Position of this {@link UIComponent} on screen. */
	private final IPosition screenPosition = new ScreenPosition(this, this instanceof IControlComponent);
	/** Position of the mouse inside this {@link UIComponent}. */
	private final IPosition mousePosition = MalisisGui.MOUSE_POSITION.minus(screenPosition);
	/** Size of this {@link UIComponent}. */
	protected ISize size = Size.inherited(this);
	/** Size available for content. */
	protected ISize innerSize = Size.of(Sizes.innerWidth(this), Sizes.innerHeight(this));
	/** Z index of the component. */
	protected int zIndex = 0;
	/** Color of the component. Effect dependent on renderers. */
	protected int color = 0xFFFFFF;
	/** Alpha transparency of this {@link UIComponent}. */
	protected int alpha = 255;
	/** Event bus on which event listeners are registered. */
	private EventBus bus;
	/** The parent {@link UIComponent} of this <code>UIComponent</code>. */
	protected UIComponent parent;
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
	protected Supplier<IGuiRenderer> backgroundRenderer = () -> null;
	/** Rendering for the foreground of this {@link UIComponent}. */
	protected Supplier<IGuiRenderer> foregroundRenderer = () -> null;

	private Object data;

	/**
	 * Instantiates a new {@link UIComponent}.
	 */
	public UIComponent()
	{
		bus = new EventBus(ExceptionHandler.instance);
		bus.register(this);
		controlComponents = new LinkedHashSet<>();
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
	 * @param position the new position
	 */
	@Override
	public void setPosition(@Nonnull IPosition position)
	{
		//if(fireEvent(this, this.position, position);
		this.position = position;
	}

	/**
	 * Gets the position of this {@link UIComponent}.
	 *
	 * @return the position
	 */
	@Override
	@Nonnull
	public IPosition position()
	{
		return position;
	}

	public IPosition screenPosition()
	{
		return screenPosition;
	}

	public IPosition mousePosition()
	{
		if (this instanceof IOffset)
			return mousePosition.minus(((IOffset) this).offset());
		return mousePosition;
	}

	/**
	 * Sets the size of this {@link UIComponent}.
	 *
	 * @param size the new size
	 */
	public void setSize(@Nonnull ISize size)
	{
		//if(fireEvent(this, this.size, size)
		this.size = size;
	}

	/**
	 * Gets the size of this {@link UIComponent}.
	 *
	 * @return the size
	 */
	@Override
	@Nonnull
	public ISize size()
	{
		return size;
	}

	public ISize innerSize()
	{
		return innerSize;
	}

	/**
	 * Sets the zIndex for this {@link UIComponent}.
	 *
	 * @param zIndex the z index
	 */
	public void setZIndex(int zIndex)
	{
		this.zIndex = zIndex;
	}

	/**
	 * Gets the zIndex of this {@link UIComponent}.
	 *
	 * @return the zIndex
	 */
	public int getZIndex()
	{
		return zIndex == 0 ? (parent != null ? parent.getZIndex() : 0) : zIndex;
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
		fireEvent(new HoveredStateChange<>(this, hovered));

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
		fireEvent(new FocusStateChange<>(this, focused));
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
	@Override
	public UIComponent getParent()
	{
		return parent;
	}

	/**
	 * Sets the parent of this {@link UIComponent}.
	 *
	 * @param parent the parent
	 */
	@Override
	public void setParent(UIComponent parent)
	{
		this.parent = parent;
		fireEvent(new ContentUpdateEvent<>(this));
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
	 */
	public void setVisible(boolean visible)
	{
		if (isVisible() == visible)
			return;

		if (!fireEvent(new VisibleStateChange<>(this, visible)))
			return;

		this.visible = visible;
		if (!visible)
		{
			this.setHovered(false);
			this.setFocused(false);
		}

		return;
	}

	/**
	 * Checks if this {@link UIComponent} is enabled.
	 *
	 * @return true if enabled
	 */
	public boolean isEnabled()
	{
		return enabled && (parent == null || parent.isEnabled());
	}

	/**
	 * Checks if this {@link UIComponent} is disabled.
	 *
	 * @return true, if is disabled
	 */
	public boolean isDisabled()
	{
		return !isEnabled();
	}

	/**
	 * Set the state of this {@link UIComponent}.
	 *
	 * @param enabled the new state
	 */
	public void setEnabled(boolean enabled)
	{
		if (isEnabled() == enabled)
			return;

		if (!fireEvent(new DisabledStateChange<>(this, enabled)))
			return;

		this.enabled = enabled;
		if (enabled)
		{
			setHovered(false);
			setFocused(false);
		}
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
	 */
	public void setTooltip(UITooltip tooltip)
	{
		this.tooltip = tooltip;
	}

	/**
	 * Sets the {@link UITooltip} of this {@link UIComponent}.
	 *
	 * @param text the text of the tooltip
	 */
	public void setTooltip(String text)
	{
		setTooltip(new UITooltip(text));
	}

	//@Override
	public void setColor(int color)
	{
		this.color = color;
	}

	public int getColor()
	{
		return color;
	}

	/**
	 * Sets the alpha transparency for this {@link UIComponent}.
	 *
	 * @param alpha the new alpha
	 */
	//@Override
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
	 * Sets a supplier for background for this {@link UIComponent}
	 *
	 * @param supplier the new background
	 */
	public void setBackground(Supplier<IGuiRenderer> supplier)
	{
		this.backgroundRenderer = checkNotNull(supplier);
	}

	/**
	 * Sets the background for this {@link UIComponent}.
	 *
	 * @param render the new background
	 */
	public void setBackground(IGuiRenderer render)
	{
		setBackground(() -> render);
	}

	/**
	 * Sets a supplier for foreground for this {@link UIComponent}
	 *
	 * @param supplier the new foreground
	 */
	public void setForeground(Supplier<IGuiRenderer> supplier)
	{
		this.foregroundRenderer = checkNotNull(supplier);
	}

	/**
	 * Sets the foreground for this {@link UIComponent}.
	 *
	 * @param render the new foreground
	 */
	public void setForeground(IGuiRenderer render)
	{
		setForeground(() -> render);
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
	/**
	 * Registers an <code>object</code> to handle events received by this {@link UIComponent}.
	 *
	 * @param object object whose handler methods should be registered
	 */
	public void register(Object object)
	{
		bus.register(object);
	}

	/**
	 * Unregister an <code>object</code> to stop receiving events for this {@link UIComponent}.
	 *
	 * @param object the object
	 */
	public void unregister(Object object)
	{
		bus.unregister(object);
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
	 * @return true, if successful
	 */
	public boolean onMouseMove()
	{
		if (!isEnabled())
			return false;

		return parent != null ? parent.onMouseMove() : false;
	}

	/**
	 * On button press.
	 *
	 * @param button the button
	 * @return true, if successful
	 */
	public boolean onButtonPress(MouseButton button)
	{
		if (!isEnabled())
			return false;

		return parent != null ? parent.onButtonPress(button) : false;
	}

	/**
	 * On button release.
	 *
	 * @param button the button
	 * @return true, if successful
	 */
	public boolean onButtonRelease(MouseButton button)
	{
		if (!isEnabled())
			return false;

		return parent != null ? parent.onButtonRelease(button) : false;
	}

	/**
	 * On click.
	 *
	 * @return true, if successful
	 */
	public boolean onClick()
	{
		if (!isEnabled())
			return false;

		return parent != null ? parent.onClick() : false;
	}

	/**
	 * On right click.
	 *
	 * @return true, if successful
	 */
	public boolean onRightClick()
	{
		if (!isEnabled())
			return false;

		return parent != null ? parent.onRightClick() : false;
	}

	/**
	 * On double click.
	 *
	 * @param button the button
	 * @return true, if successful
	 */
	public boolean onDoubleClick(MouseButton button)
	{
		if (!isEnabled())
			return false;

		return parent != null ? parent.onDoubleClick(button) : false;
	}

	/**
	 * On drag.
	 *
	 * @param button the button
	 * @return true, if successful
	 */
	public boolean onDrag(MouseButton button)
	{
		if (!isEnabled())
			return false;

		return parent != null ? parent.onDrag(button) : false;
	}

	/**
	 * On scroll wheel.
	 *
	 * @param delta the delta
	 * @return true, if successful
	 */
	public boolean onScrollWheel(int delta)
	{
		if (!isEnabled())
			return false;

		for (IControlComponent c : controlComponents)
			if (c.onScrollWheel(delta))
				return true;

		return parent != null && !(this instanceof IControlComponent) ? parent.onScrollWheel(delta) : false;
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
		int sx = screenPosition().x();
		int sy = screenPosition().y();
		return x >= sx && x <= sx + size().width() && y >= sy && y <= sy + size().height();
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
	 * Called when this {@link UIComponent} is added to screen.
	 */
	public void onAddedToScreen(MalisisGui gui)
	{
		this.gui = gui;
	}

	/**
	 * Draws this {@link UIComponent}. Rendering is surrounded by glPushAttrib(GL_ALL_ATTRIB_BITS) so no state should bleed between
	 * components. Also, a draw() is triggered between background and foreground.
	 *
	 * @param renderer the renderer
	 */
	@Override
	public void render(GuiRenderer renderer)
	{
		if (!isVisible())
			return;

		//		if (getGui().isOverlay())
		//		{
		//			GlStateManager.blendFunc(GL11.GL_CONSTANT_ALPHA, GL11.GL_ONE_MINUS_CONSTANT_ALPHA);
		//			GL14.glBlendColor(1, 1, 1, (float) getAlpha() / 255);
		//		}

		//store last drawn component so that it can be set back after drawing.
		//makes sure components overriding rendering and calling super still have correct
		//relative position in case super renders other components.
		UIComponent oldComponent = renderer.currentComponent;

		//draw background
		IGuiRenderer gr = backgroundRenderer.get();
		if (gr != null)
		{
			renderer.currentComponent = this;
			gr.render(renderer);
			renderer.next();
		}

		//draw foreground
		gr = foregroundRenderer.get();
		if (gr != null)
		{
			ClipArea area = IClipable.intersected(this);
			if (!area.fullClip())
			{
				if (!(this instanceof IControlComponent))
					renderer.startClipping(area);
				renderer.currentComponent = this;
				gr.render(renderer);
				if (!(this instanceof IControlComponent))
					renderer.endClipping(area);
			}
		}

		for (IControlComponent c : controlComponents)
			c.render(renderer);

		renderer.currentComponent = oldComponent;
	}

	/**
	 * Gets the property string.
	 *
	 * @return the property string
	 */
	public String getPropertyString()
	{
		String str = position() + "@" + size() + " | Screen=" + screenPosition();
		if (this instanceof IPadded)
		{
			Padding p = Padding.of(this);
			str += " | Padding " + p.left() + "." + p.right() + "." + p.right() + "." + p.right();
		}
		return str;
	}

	/**
	 * To string.
	 *
	 * @return the string
	 */
	@Override
	public String toString()
	{
		return (this.name == null ? getClass().getSimpleName() : this.name) + " " + getPropertyString();
	}
}
