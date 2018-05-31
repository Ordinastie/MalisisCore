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

import org.apache.commons.lang3.StringUtils;

import com.google.common.eventbus.Subscribe;

import net.malisis.core.client.gui.ClipArea;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.IClipable;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.control.ICloseable;
import net.malisis.core.client.gui.component.control.IScrollable;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.element.Padding;
import net.malisis.core.client.gui.component.element.Position;
import net.malisis.core.client.gui.component.element.Size.ISize;
import net.malisis.core.client.gui.event.component.ContentUpdateEvent;
import net.malisis.core.client.gui.event.component.SpaceChangeEvent;
import net.malisis.core.client.gui.event.component.StateChangeEvent.VisibleStateChange;
import net.malisis.core.client.gui.render.TexturedBackground.WindowBackground;
import net.minecraft.client.gui.GuiScreen;

/**
 * {@link UIContainer} are the base for components holding other components.<br>
 * Child components are drawn in the foreground.<br>
 * Mouse events received are passed to the child concerned (getComponentAt()).<br>
 * Keyboard event are passed to all the children.
 *
 * @author Ordinastie, PaleoCrafter
 * @param <T> type of UIContainer
 */
public class UIContainer<T extends UIContainer<T>> extends UIComponent<T> implements IClipable, IScrollable, ICloseable
{
	/** List of {@link UIComponent} inside this {@link UIContainer}. */
	protected final Set<UIComponent<?>> components;

	/** Padding used by this {@link UIContainer}.? */
	protected Padding padding;

	/** Label for the title of this {@link UIContainer}. */
	protected UILabel titleLabel;
	//IClipable
	/** Determines whether this {@link UIContainer} should clip its contents to its drawn area. */
	protected boolean clipContent = true;
	//IScrollable
	public final ContentSize contentSize = new ContentSize();
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
	 * @param size the size
	 */
	public UIContainer(MalisisGui gui, ISize size)
	{
		this(gui);
		setSize(size);
	}

	/**
	 * Instantiates a new {@link UIContainer}.
	 *
	 * @param gui the gui
	 * @param title the title
	 * @param size the size
	 */
	public UIContainer(MalisisGui gui, String title, ISize size)
	{
		this(gui);
		setTitle(title);
		setSize(size);
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
			return self();

		super.setVisible(visible);
		if (!visible)
		{
			for (UIComponent<?> c : components)
			{
				c.setHovered(false);
				c.setFocused(false);
			}
		}
		return self();
	}

	/**
	 * Sets the disabled.
	 *
	 * @param enabled the disabled
	 * @return the t
	 */
	@Override
	public T setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
		if (enabled)
		{
			for (UIComponent<?> c : components)
			{
				c.setHovered(false);
				c.setFocused(false);
			}
		}
		return self();
	}

	/**
	 * Set the padding for this {@link UIContainer}.
	 *
	 * @param padding the padding
	 * @return the t
	 */
	public T setPadding(Padding padding)
	{
		this.padding = padding;
		return self();
	}

	@Override
	public Padding getPadding()
	{
		if (padding != null)
			return padding;

		if (backgroundRenderer != null)
			return backgroundRenderer.getPadding();

		return Padding.NO_PADDING;
	}

	/**
	 * Sets the title for {@link UIContainer}.<br>
	 * Creates a {@link UILabel} and adds it inside {@link UIContainer}.
	 *
	 * @param title the title
	 * @return the UI container
	 */
	public T setTitle(String title)
	{
		if (title == null || title == "")
		{
			remove(titleLabel);
			return self();
		}

		titleLabel.setText(title);
		add(titleLabel);
		return self();
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

	@Override
	public ISize contentSize()
	{
		return contentSize;
	}

	// #end getters/setters
	/**
	 * Gets the {@link UIComponent} matching the specified name.
	 *
	 * @param name the name
	 * @return the component
	 */
	public UIComponent<?> getComponent(String name)
	{
		return getComponent(name, false);
	}

	/**
	 * Gets the {@link UIComponent} matching the specified name. If recursive is true, looks for the {@code UIComponent} inside it child
	 * {@link UIContainer} too.
	 *
	 * @param name the name
	 * @param recursive if true, look inside child {@code UIContainer}
	 * @return the component
	 */
	public UIComponent<?> getComponent(String name, boolean recursive)
	{
		if (StringUtils.isEmpty(name))
			return null;

		for (UIComponent<?> c : components)
		{
			if (name.equals(c.getName()))
				return c;
		}

		if (!recursive)
			return null;

		for (UIComponent<?> c : components)
		{
			if (c instanceof UIContainer)
			{
				UIComponent<?> found = getComponent(name, true);
				if (found != null)
					return found;
			}
		}

		return null;
	}

	/**
	 * Gets the {@link UIComponent} at the specified coordinates.<br>
	 * Selects the component with the highest z-index from the components overlapping the coordinates.
	 *
	 * @param x the x
	 * @param y the y
	 * @return the child component in this {@link UIContainer}, this {@link UIContainer} if none, or null if outside its bounds.
	 */
	@Override
	public UIComponent<?> getComponentAt(int x, int y)
	{
		if (!isEnabled() || !isVisible())
			return null;

		//super impl will return control components or itself
		//control components take precedence over child components
		UIComponent<?> superComp = super.getComponentAt(x, y);
		if (superComp != null && superComp != this)
			return superComp;

		if (shouldClipContent() && !getClipArea().isInside(x, y))
			return superComp;

		Set<UIComponent<?>> list = new LinkedHashSet<>();
		for (UIComponent<?> c : components)
		{
			UIComponent<?> component = c.getComponentAt(x, y);
			if (component != null)
				list.add(component);
		}

		if (list.size() == 0)
			return this;

		UIComponent<?> component = superComp;
		for (UIComponent<?> c : list)
		{
			if (component != null && (component.getZIndex() <= c.getZIndex()))
				component = c;
		}

		return component != null && component.isEnabled() ? component : superComp;
	}

	/**
	 * Called when this {@link UIComponent} gets its content updated.
	 */
	public void onContentUpdate()
	{
		contentSize.update();
		fireEvent(new ContentUpdateEvent<>(self()));
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
	public float getOffsetX()
	{
		if (contentSize().width() <= size().width())
			return 0;
		return (float) xOffset / (contentSize.width() - size().width());
	}

	@Override
	public void setOffsetX(float offsetX, int delta)
	{
		this.xOffset = Math.round((contentSize().width() - size().width() + delta) * offsetX);
	}

	@Override
	public float getOffsetY()
	{
		if (contentSize().height() <= size().height())
			return 0;
		return (float) yOffset / (contentSize().height() - size().height());
	}

	@Override
	public void setOffsetY(float offsetY, int delta)
	{
		this.yOffset = Math.round((contentSize().height() - size().height() + delta) * offsetY);
	}

	@Override
	public float getScrollStep()
	{
		return (GuiScreen.isCtrlKeyDown() ? 0.125F : 0.25F);
	}

	//#end IScrollable

	@Override
	public int parentX()
	{
		return screenX() - xOffset;
	}

	@Override
	public int parentY()
	{
		return screenY() - yOffset;
	}

	/**
	 * Adds components to this {@link UIContainer}.
	 *
	 * @param components the components
	 */
	public void add(UIComponent<?>... components)
	{
		for (UIComponent<?> component : components)
		{
			if (component != null && component != this)
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
	public void remove(UIComponent<?> component)
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
		for (UIComponent<?> component : components)
			component.setParent(null);
		components.clear();
		onContentUpdate();
	}

	@Override
	public void onAddedToScreen()
	{
		super.onAddedToScreen();
		for (UIComponent<?> component : components)
			component.onAddedToScreen();
	}

	@Override
	public void onClose()
	{
		if (getParent() instanceof UIContainer)
			((UIContainer<?>) getParent()).remove(this);
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
		for (UIComponent<?> c : components)
			c.draw(renderer, mouseX, mouseY, partialTick);
	}

	/**
	 * Called when a child {@link UIComponent} gets its visibility changed
	 *
	 * @param event the event
	 */
	@Subscribe
	public void onComponentStateChange(VisibleStateChange<T> event)
	{
		onContentUpdate();
	}

	/**
	 * Called when a child {@link UIComponent} gets its size or position changed
	 *
	 * @param event the event
	 */
	@Subscribe
	public void onComponentSpaceChange(SpaceChangeEvent<T> event)
	{
		onContentUpdate();
	}

	/**
	 * Creates a centered {@link UIContainer} with a {@link WindowBackground} background.
	 *
	 * @param gui the gui
	 * @param size the size
	 * @return the UI container
	 */
	public static UIContainer<?> window(MalisisGui gui, ISize size)
	{
		UIContainer<?> container = new UIContainer<>(gui, size);
		container.setBackground(new WindowBackground(gui));
		container.setPosition(Position.centered().middleAligned());
		return container;
	}

	public class ContentSize implements ISize
	{
		private int width;
		private int height;

		private void update()
		{
			width = components.stream().filter(UIComponent::isVisible).mapToInt(c -> c.position().x() + c.size().width()).max().orElse(0);
			height = components.stream().filter(UIComponent::isVisible).mapToInt(c -> c.position().y() + c.size().height()).max().orElse(0);
		}

		@Override
		public int width()
		{
			return width;
		}

		@Override
		public int height()
		{
			return height;
		}

	}
}
