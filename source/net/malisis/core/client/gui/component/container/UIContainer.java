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

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.ClipArea;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.control.IControlComponent;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.element.SimpleGuiShape;
import net.malisis.core.client.gui.event.KeyboardEvent;

import org.lwjgl.opengl.GL11;

/**
 * {@link UIContainer} are the base for components holding other components.<br />
 * Child components are drawn in the foreground.<br />
 * Mouse events received are passed to the child concerned (getComponentAt()).<br />
 * Keyboard event are passed to all the children.
 *
 * @author Ordinastie, PaleoCrafter
 */
public class UIContainer<T extends UIContainer> extends UIComponent<T>
{
	/**
	 * List of {@link net.malisis.core.client.gui.component.UIComponent components}.
	 */
	protected final Set<UIComponent> components;
	/**
	 * List of {@link net.malisis.core.client.gui.component.UIComponent components} controling this {@link UIContainer}.
	 */
	protected final Set<UIComponent> controlComponents;
	/**
	 * Horizontal padding to apply to this {@link UIContainer}
	 */
	protected int horizontalPadding;
	/**
	 * Vertical padding to apply to this {@link UIContainer}
	 */
	protected int verticalPadding;
	/**
	 * Determines whether this {@link UIContainer} should clip its contents to its drawn area.
	 */
	public boolean clipContent = true;
	/**
	 * Background color multiplier.
	 */
	protected int backgroundColor = -1;
	/**
	 * Label for the title of this {@link UIContainer}.
	 */
	protected UILabel titleLabel;

	/**
	 * Default constructor, creates the components list.
	 */
	public UIContainer(MalisisGui gui)
	{
		super(gui);
		components = new LinkedHashSet<>();
		controlComponents = new LinkedHashSet<>();

		shape = new SimpleGuiShape();
		titleLabel = new UILabel(gui);
	}

	public UIContainer(MalisisGui gui, String title)
	{
		this(gui);
		setTitle(title);
	}

	public UIContainer(MalisisGui gui, int width, int height)
	{
		this(gui);
		setSize(width, height);
	}

	public UIContainer(MalisisGui gui, String title, int width, int height)
	{
		this(gui);
		setTitle(title);
		setSize(width, height);
	}

	// #region getters/setters
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
	 * @param horizontal
	 * @param vertical
	 */
	public void setPadding(int horizontal, int vertical)
	{
		this.horizontalPadding = horizontal;
		this.verticalPadding = vertical;
	}

	/**
	 * @return horizontal padding of this {@link UIContainer}.
	 */
	public int getHorizontalPadding()
	{
		return horizontalPadding;
	}

	/**
	 * @return horizontal padding of this {@link UIContainer}.
	 */
	public int getVerticalPadding()
	{
		return verticalPadding;
	}

	/**
	 * Sets the background color for {@link UIContainer}.
	 *
	 * @param color
	 * @return
	 */
	public UIContainer setBackgroundColor(int color)
	{
		this.backgroundColor = color;
		return this;
	}

	/**
	 * @return the background color for {@link UIContainer}.
	 */
	public int getBackgroundColor()
	{
		return backgroundColor;
	}

	/**
	 * Sets the title for {@link UIContainer}.<br />
	 * Creates a {@link UILabel} and adds it inside {@link UIContainer}.
	 *
	 * @param title
	 * @return
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
	 * @param component
	 * @return
	 */
	public int componentX(UIComponent component)
	{
		int x = component.getX();
		if (Anchor.horizontal(component.getAnchor()) == Anchor.CENTER)
			x += (getWidth() - component.getWidth()) / 2;
		else if (Anchor.horizontal(component.getAnchor()) == Anchor.RIGHT)
			x += getWidth() - component.getWidth() - getHorizontalPadding();
		else
			x += getHorizontalPadding();

		return x;
	}

	/**
	 * Gets the relative position of the specified {@link UIComponent} inside this {@link UIContainer}.
	 *
	 * @param component
	 * @return
	 */
	public int componentY(UIComponent component)
	{
		int y = component.getY();
		if (Anchor.vertical(component.getAnchor()) == Anchor.MIDDLE)
			y += (getHeight() - component.getHeight()) / 2;
		else if (Anchor.vertical(component.getAnchor()) == Anchor.BOTTOM)
			y += getHeight() - component.getHeight() - getVerticalPadding();
		else
			y += getVerticalPadding();

		return y;
	}

	/**
	 * Gets the component at the specified coordinates.<br />
	 * Selects the component with the highest z-index from the components overlapping the coordinates.
	 *
	 * @param x
	 * @param y
	 * @return the child component in this {@link UIContainer}, this {@link UIContainer} if none, or null if outside its bounds.
	 */
	@Override
	public UIComponent getComponentAt(int x, int y)
	{
		//control components take precedence over regular components
		for (UIComponent c : controlComponents)
		{
			UIComponent component = c.getComponentAt(x, y);
			if (component != null)
				return component;
		}

		Set<UIComponent> list = new HashSet<>();
		for (UIComponent c : components)
		{
			UIComponent component = c.getComponentAt(x, y);
			if (component != null)
				list.add(component);
		}

		if (list.size() == 0)
			return super.getComponentAt(x, y);

		UIComponent component = null;
		for (UIComponent c : list)
		{
			if (component == null || component.getZIndex() <= c.getZIndex())
				component = c;
		}

		return component;
	}

	public void onContentUpdate()
	{}

	/**
	 * Gets the clipping area delimited by this {@link UIContainer}.
	 *
	 * @return
	 */
	public ClipArea getClipArea()
	{
		return new ClipArea(this);
	}

	//#region Child components
	/**
	 * Adds a component to this {@link UIContainer}.
	 *
	 * @param component
	 */
	public void add(UIComponent component)
	{
		if (component instanceof IControlComponent)
			controlComponents.add(component);
		else
		{
			components.add(component);
			onContentUpdate();
		}
		component.setParent(this);

	}

	/**
	 * Removes the component from this {@link UIContainer}.
	 *
	 * @param component
	 */
	public void remove(UIComponent component)
	{
		if (component.getParent() != this)
			return;
		if (component instanceof IControlComponent)
			controlComponents.remove(component);
		else
		{
			components.remove(component);
			onContentUpdate();
		}
		component.setParent(null);

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

	/**
	 * Adds a control component to this {@link UIContainer}.
	 *
	 * @param component
	 */
	public <S extends UIComponent & IControlComponent> void addControlComponent(S component)
	{
		controlComponents.add(component);
		component.setParent(this);
	}

	/**
	 * Removes the component from this {@link UIContainer}.
	 *
	 * @param component
	 */
	public <S extends UIComponent & IControlComponent> void removeControlComponent(S component)
	{
		if (component.getParent() != this)
			return;
		component.setParent(null);
		controlComponents.remove(component);
	}

	/**
	 * Removes all the control components from this {@link UIContainer}. Does not remove regular components
	 */
	public void removeAllControlComponents()
	{
		for (UIComponent component : controlComponents)
			component.setParent(null);
		controlComponents.clear();
	}

	//#end Child components

	@Override
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		if (backgroundColor == -1)
			return;

		rp.colorMultiplier.set(backgroundColor);

		GL11.glDisable(GL11.GL_TEXTURE_2D);
		renderer.drawShape(shape, rp);
		renderer.next();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		for (UIComponent c : controlComponents)
			c.draw(renderer, mouseX, mouseY, partialTick);

		ClipArea area = getClipArea();
		renderer.startClipping(area);

		for (UIComponent c : components)
			c.draw(renderer, mouseX, mouseY, partialTick);

		renderer.endClipping(area);
	}

	@Override
	public boolean fireKeyboardEvent(KeyboardEvent event)
	{
		for (UIComponent c : controlComponents)
			c.fireKeyboardEvent(event);
		for (UIComponent c : components)
			c.fireKeyboardEvent(event);
		return true;
	}

}
