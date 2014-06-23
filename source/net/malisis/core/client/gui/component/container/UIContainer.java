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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.ClipArea;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.event.KeyboardEvent;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.preset.ShapePreset;

import org.lwjgl.opengl.GL11;

/**
 * UIContainer
 * 
 * @author PaleoCrafter
 */
public class UIContainer extends UIComponent<UIContainer>
{
	/**
	 * The list of {@link net.malisis.core.client.gui.component.UIComponent components}.
	 */
	protected final List<UIComponent> components;
	/**
	 * Horizontal padding to apply to this <code>UIContainer</code>
	 */
	protected int horizontalPadding;
	/**
	 * Vertical padding to apply to this <code>UIContainer</code>
	 */
	protected int verticalPadding;
	/**
	 * Determines whether this <code>UIContainer</code> should clip its contents to its drawn area.
	 */
	public boolean clipContent = true;
	/**
	 * Background color multiplier
	 */
	private int backgroundColor = 0x404040;

	/**
	 * Default constructor, creates the components list.
	 */

	public UIContainer(String title, int width, int height)
	{
		super();
		setSize(width, height);
		components = new LinkedList<>();
		if (title != null && !title.equals(""))
			add(new UILabel(title));
	}

	public UIContainer(int width, int height)
	{
		this(null, width, height);
	}

	public UIContainer()
	{
		this(null, 16, 16);
	}

	// #region getters/setters
	@Override
	public UIContainer setVisible(boolean visible)
	{
		super.setVisible(visible);
		if (!visible)
		{
			for (UIComponent c : components)
			{
				c.setHovered(false);
				c.setFocused(false);
			}
		}
		return this;
	}

	@Override
	public UIContainer setDisabled(boolean disabled)
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
		return this;
	}

	/**
	 * Set the padding for this <code>UIContainer</code>.
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
	 * @return horizontal padding of this <code>UIContainer</code>.
	 */
	public int getHorizontalPadding()
	{
		return horizontalPadding;
	}

	/**
	 * @return horizontal padding of this <code>UIContainer</code>.
	 */
	public int getVerticalPadding()
	{
		return verticalPadding;
	}

	public int componentX(UIComponent component)
	{
		int x = component.getX();
		if (Anchor.horizontal(component.getAnchor()) == Anchor.CENTER)
			x += (width - component.getWidth()) / 2;
		else if (Anchor.horizontal(component.getAnchor()) == Anchor.RIGHT)
			x += width - component.getWidth() - getHorizontalPadding();
		else
			x += getHorizontalPadding();

		return x;
	}

	public int componentY(UIComponent component)
	{
		int y = component.getY();
		if (Anchor.vertical(component.getAnchor()) == Anchor.MIDDLE)
			y += (height - component.getHeight()) / 2;
		else if (Anchor.vertical(component.getAnchor()) == Anchor.BOTTOM)
			y += height - component.getHeight() - getVerticalPadding();
		else
			y += getVerticalPadding();

		return y;
	}

	public UIContainer setBackgroundColor(int color)
	{
		this.backgroundColor = color;
		return this;
	}

	public int getBackgroundColor()
	{
		return backgroundColor;
	}

	// #end getters/setters

	public void onContentUpdate()
	{}

	/**
	 * Get the clipping area delimited by this <code>UIContainer</code>
	 * 
	 * @return
	 */
	public ClipArea getClipArea()
	{
		return new ClipArea(this);
	}

	public void add(UIComponent component)
	{
		components.add(component);
		component.setParent(this);
		if (component.getZIndex() == INHERITED)
			component.setZIndex(zIndex);
	}

	@Override
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		if (backgroundColor == 0x404040)
			return;

		GL11.glDisable(GL11.GL_TEXTURE_2D);

		Shape s = ShapePreset.GuiXYResizable(width, height, 0, 0);
		RenderParameters rp = new RenderParameters();
		rp.colorMultiplier.set(backgroundColor);
		renderer.drawShape(s, rp);
		renderer.next();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		ClipArea area = getClipArea();
		renderer.startClipping(area);

		for (UIComponent c : components)
			c.draw(renderer, mouseX, mouseY, partialTick);

		renderer.endClipping(area);
	}

	// @Override
	// public boolean fireMouseEvent(MouseEvent event)
	// {
	// if (isDisabled() || !isVisible())
	// return false;
	//
	// boolean propagate = true;
	// UIComponent childHovered = getComponentAt(event.getX(), event.getY());
	// if (childHovered != null)
	// childHovered.setHovered(true);
	//
	// if (event instanceof MouseEvent.Press && childHovered != null)
	// childHovered.setFocused(true);
	//
	// if (childHovered != null && childHovered != this)
	// propagate = childHovered.fireMouseEvent(event);
	//
	// if (propagate)
	// propagate = super.fireMouseEvent(event);
	//
	// return propagate;
	// }

	@Override
	public boolean fireKeyboardEvent(KeyboardEvent event)
	{
		for (UIComponent c : components)
			c.fireKeyboardEvent(event);
		return true;
	}

	@Override
	public UIComponent getComponentAt(int x, int y)
	{
		ArrayList<UIComponent> list = new ArrayList<>();
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

}
