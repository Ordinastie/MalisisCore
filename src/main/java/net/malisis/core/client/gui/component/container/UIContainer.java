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

import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.content.IContent;
import net.malisis.core.client.gui.component.control.ICloseable;
import net.malisis.core.client.gui.component.control.IScrollable;
import net.malisis.core.client.gui.component.scrolling.UIScrollBar;
import net.malisis.core.client.gui.element.IClipable;
import net.malisis.core.client.gui.element.Padding;
import net.malisis.core.client.gui.element.Padding.IPadded;
import net.malisis.core.client.gui.element.Size.ISize;
import net.malisis.core.client.gui.element.position.Position;
import net.malisis.core.client.gui.element.position.Position.IPosition;
import net.malisis.core.client.gui.event.component.ContentUpdateEvent;
import net.malisis.core.client.gui.event.component.SpaceChangeEvent;
import net.malisis.core.client.gui.event.component.StateChangeEvent.VisibleStateChange;
import net.malisis.core.client.gui.render.GuiIcon;
import net.malisis.core.client.gui.render.GuiRenderer;
import net.malisis.core.client.gui.render.IGuiRenderer;
import net.malisis.core.client.gui.render.shape.GuiShape;

/**
 * {@link UIContainer} are the base for components holding other components.<br>
 * Child components are drawn in the foreground.<br>
 * Mouse events received are passed to the child concerned (getComponentAt()).<br>
 * Keyboard event are passed to all the children.
 *
 * @author Ordinastie, PaleoCrafter
 */
public class UIContainer extends UIComponent implements IClipable, IScrollable, ICloseable, IPadded
{
	protected ContainerContent content = new ContainerContent();

	/** Padding used by this {@link UIContainer}.? */
	protected Padding padding = Padding.NO_PADDING;

	//IClipable
	/** Determines whether this {@link UIContainer} should clip its contents to its drawn area. */
	protected boolean clipContent = true;

	protected final IPosition offset = UIScrollBar.scrollingOffset(this);

	/**
	 * Instantiates a new {@link UIContainer}.
	 */
	public UIContainer()
	{
		//titleLabel = new UILabel();
		setForeground(content);
	}

	@Override
	public ContainerContent content()
	{
		return content;
	}

	// #region getters/setters
	@Override
	public void setVisible(boolean visible)
	{
		super.setVisible(visible);
		content.setVisible(visible);
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
		content.setEnabled(enabled);
	}

	/**
	 * Set the padding for this {@link UIContainer}.
	 *
	 * @param padding the padding
	 */
	public void setPadding(Padding padding)
	{
		this.padding = padding;
	}

	@Override
	public Padding padding()
	{
		return padding;
	}

	@Override
	public IPosition contentPosition()
	{
		return Position.ZERO;
	}

	@Override
	public IPosition offset()
	{
		return offset;
	}

	// #end getters/setters
	/**
	 * Gets the {@link UIComponent} matching the specified name.
	 *
	 * @param name the name
	 * @return the component
	 */
	public UIComponent getComponent(String name)
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
	public UIComponent getComponent(String name, boolean recursive)
	{
		if (StringUtils.isEmpty(name))
			return null;

		for (UIComponent c : content.components)
		{
			if (name.equals(c.getName()))
				return c;
		}

		if (!recursive)
			return null;

		for (UIComponent c : content.components)
		{
			if (c instanceof UIContainer)
			{
				UIComponent found = getComponent(name, true);
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
	public UIComponent getComponentAt(int x, int y)
	{
		if (!isEnabled() || !isVisible())
			return null;

		//super impl will return control components or itself
		//control components take precedence over child components
		UIComponent superComp = super.getComponentAt(x, y);
		if (superComp != null && superComp != this)
			return superComp;

		if (shouldClipContent() && !getClipArea().isInside(x, y))
			return superComp;

		Set<UIComponent> list = new LinkedHashSet<>();
		for (UIComponent c : content.components)
		{
			UIComponent component = c.getComponentAt(x, y);
			if (component != null)
				list.add(component);
		}

		if (list.size() == 0)
			return superComp;

		UIComponent component = superComp;
		for (UIComponent c : list)
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
		content.updateSize();
		fireEvent(new ContentUpdateEvent<>(this));
	}

	//#region IClipable
	@Override
	public ClipArea getClipArea()
	{
		return shouldClipContent() ? ClipArea.from(this) : IClipable.NOCLIP;
	}

	/**
	 * Sets whether this {@link UIContainer} should clip its contents
	 *
	 * @param clipContent if true, clip contents
	 */
	public void setClipContent(boolean clipContent)
	{
		this.clipContent = clipContent;
	}

	/**
	 * Checks whether this {@link UIContainer} should clip its contents
	 *
	 * @return true, if should clip contents
	 */
	public boolean shouldClipContent()
	{
		return clipContent;
	}

	//#end IClipable

	/**
	 * Adds components to this {@link UIContainer}.
	 *
	 * @param components the components
	 */
	public void add(UIComponent... components)
	{
		for (UIComponent component : components)
		{
			if (component != null && component != this)
			{
				content.components.add(component);
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

		content.components.remove(component);
		component.setParent(null);
		component.unregister(this);
		onContentUpdate();
	}

	/**
	 * Removes all the components from this {@link UIContainer}. Does not remove control components
	 */
	public void removeAll()
	{
		for (UIComponent component : content.components)
			component.setParent(null);
		content.components.clear();
		onContentUpdate();
	}

	@Override
	public void onAddedToScreen(MalisisGui gui)
	{
		this.gui = gui;
		for (UIComponent component : content.components)
			component.onAddedToScreen(gui);
	}

	@Override
	public void onClose()
	{
		if (getParent() instanceof UIContainer)
			((UIContainer) getParent()).remove(this);
	}

	/**
	 * Called when a child {@link UIComponent} gets its visibility changed
	 *
	 * @param event the event
	 */
	@Subscribe
	public void onComponentStateChange(VisibleStateChange<UIContainer> event)
	{
		onContentUpdate();
	}

	/**
	 * Called when a child {@link UIComponent} gets its size or position changed
	 *
	 * @param event the event
	 */
	@Subscribe
	public void onComponentSpaceChange(SpaceChangeEvent<UIContainer> event)
	{
		onContentUpdate();
	}

	@Override
	public String getPropertyString()
	{
		return super.getPropertyString() + " | O : " + offset;
	}

	/**
	 * Creates a centered {@link UIContainer} with a window background and a padding of 5.
	 *
	 * @return the UI container
	 */
	public static UIContainer window()
	{
		UIContainer container = new UIContainer();
		container.setName("Window");
		container.setBackground(GuiShape.builder(container).icon(GuiIcon.WINDOW).border(5).build());
		container.setPosition(Position.middleCenter(container));
		container.setPadding(Padding.of(5));
		return container;
	}

	/**
	 * Creates a {@link UIContainer} with a Panel background and a padding of 3.
	 *
	 * @return the UI container
	 */
	public static UIContainer panel()
	{
		UIContainer container = new UIContainer();
		container.setName("Panel");
		container.setBackground(GuiShape.builder(container).icon(GuiIcon.PANEL).border(3).build());
		container.setPadding(Padding.of(3));
		return container;
	}

	/**
	 * Creates a {@link UIContainer} with a Box background and a padding of 1.
	 *
	 * @return the UI container
	 */
	public static UIContainer box()
	{
		UIContainer container = new UIContainer();
		container.setName("box");
		container.setBackground(GuiShape.builder(container).icon(GuiIcon.BOX).border(1).build());
		container.setPadding(Padding.of(1));
		return container;
	}

	public class ContainerContent implements IContent, IGuiRenderer, ISize
	{
		/** List of {@link UIComponent} inside this {@link UIContainer}. */
		protected final Set<UIComponent> components = new LinkedHashSet<>();
		protected int width;
		protected int height;

		@Override
		public void setParent(UIComponent parent)
		{}

		@Override
		public UIContainer getParent()
		{
			return UIContainer.this;
		}

		@Override
		public void setPosition(IPosition position)
		{}

		@Override
		public IPosition position()
		{
			return Position.ZERO;
		}

		@Override
		public ISize size()
		{
			return this;
		}

		private void updateSize()
		{
			width = components.stream().filter(UIComponent::isVisible).mapToInt(c -> c.position().x() + c.size().width()).max().orElse(0)
					- padding().left();
			height = components.stream().filter(UIComponent::isVisible).mapToInt(c -> c.position().y() + c.size().height()).max().orElse(0)
					- padding().top();
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

		public void setVisible(boolean visible)
		{
			if (isVisible() == visible)
				return;

			if (!visible)
			{
				for (UIComponent c : components)
				{
					c.setHovered(false);
					c.setFocused(false);
				}
			}
		}

		public void setEnabled(boolean enabled)
		{
			if (!enabled)
			{
				for (UIComponent c : components)
				{
					c.setHovered(false);
					c.setFocused(false);
				}
			}
		}

		@Override
		public void render(GuiRenderer renderer)
		{
			components.forEach(c -> c.render(renderer));
		}
	}
}
