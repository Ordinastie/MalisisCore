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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.layout.Constraints;
import net.malisis.core.client.gui.layout.LayoutManager;
import net.malisis.core.client.gui.proxy.GuiScreenProxy;
import net.malisis.core.client.gui.renderer.Drawable;
import net.malisis.core.client.gui.renderer.GuiRenderer;
import net.malisis.core.client.gui.util.Size;
import net.malisis.core.client.gui.util.shape.Point;
import net.malisis.core.demo.test.GuiIcon;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.google.common.eventbus.EventBus;

/**
 * UIContainer
 * 
 * @author PaleoCrafter
 */
public class UIContainer extends UIComponent
{

	/**
	 * The list of {@link net.malisis.core.client.gui.component.UIComponent components}.
	 */
	protected final List<UIComponent> components;

	/**
	 * The {@link net.malisis.core.client.gui.layout.LayoutManager LayoutManager} to be used by this container. Is <code>null</code> by
	 * default, such that absolute positions are used.
	 */
	private LayoutManager<? extends Constraints> layoutManager;

	private Drawable background;

	private Point padding;

	/**
	 * Default constructor, creates the components list.
	 */
	public UIContainer()
	{
		this(0, 0);
	}

	public UIContainer(int width, int height)
	{
		super();
		this.setSize(width, height);
		components = new LinkedList<>();
		padding = new Point(0, 0);
	}

	public LayoutManager<? extends Constraints> getLayout()
	{
		return layoutManager;
	}

	public void setLayout(LayoutManager<? extends Constraints> layoutManager)
	{
		this.layoutManager = layoutManager;
	}

	public void add(UIComponent component)
	{
		components.add(component);
		if (layoutManager != null)
			layoutManager.setConstraints(component, layoutManager.createDefaultConstraints());
	}

	public void add(UIComponent component, Constraints constraints)
	{
		components.add(component);
		if (layoutManager != null)
			layoutManager.setConstraints(component, constraints);
	}

	@Override
	public void initComponent()
	{
		super.initComponent();
		for (UIComponent component : components)
		{
			component.setParent(this);
			component.initComponent();
		}
	}

	@Override
	public void drawBackground(int mouseX, int mouseY)
	{
		if (background != null)
			background.draw(this.getScreenX(), this.getScreenY());
		for (UIComponent component : components)
		{
			if (component.isVisible())
			{
				GL11.glColor4f(1F, 1F, 1F, 1F);
				if (layoutManager != null)
					component.setScreenPosition(Point.add(
							Point.add(screenPosition, layoutManager.getPositionForComponent(this, component)), padding));
				else
					component.setScreenPosition(Point.add(Point.add(screenPosition, component.getPosition()), padding));
				component.drawBackground(mouseX, mouseY);
			}
		}
	}

	@Override
	public void draw(int mouseX, int mouseY)
	{
		for (UIComponent component : components)
		{
			if (component.isVisible())
			{
				GL11.glColor4f(1F, 1F, 1F, 1F);
				component.draw(mouseX, mouseY);
			}
		}
	}

	@Override
	public void drawForeground(int mouseX, int mouseY)
	{
		for (UIComponent component : components)
		{
			if (component.isVisible())
			{
				GL11.glColor4f(1F, 1F, 1F, 1F);
				component.drawForeground(mouseX, mouseY);
			}
		}
	}

	public void drawTooltip(int mouseX, int mouseY)
	{
		for (UIComponent component : components)
		{
			if (component.getTooltip() != null && !component.getTooltip().isEmpty() && !(component instanceof UIContainer)
					&& component.isVisible() && component.isHovered(new Point(mouseX, mouseY)) && component.isEnabled())
			{
				getContext().getTooltip().setText(component.getTooltip());
				getContext().getTooltip().draw(mouseX, mouseY);
			}
			else if (component instanceof UIContainer && component.isVisible() && component.isEnabled())
			{
				((UIContainer) component).drawTooltip(mouseX, mouseY);
			}
		}
	}

	@Override
	public void update(int mouseX, int mouseY)
	{
		for (UIComponent component : components)
		{
			component.update(mouseX, mouseY);
		}
	}

	@Override
	public void setSize(Size size)
	{
		super.setSize(size);
		if (background != null)
			background.setSize(size);
	}

	@Override
	public void dispose()
	{
		super.dispose();
		for (UIComponent component : components)
			component.dispose();
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
		for (UIComponent component : components)
			component.setEnabled(enabled);
	}

	public void registerChildrenTo(EventBus bus)
	{
		for (UIComponent component : components)
		{
			bus.register(component);
			if (component instanceof UIContainer)
			{
				((UIContainer) component).registerChildrenTo(bus);
			}
		}
	}

	public void registerChildren()
	{
		for (UIComponent component : components)
		{
			if (component instanceof UIContainer)
				((UIContainer) component).registerChildren();
			else
				getContext().register(component);
		}
	}

	public void unregisterChildren()
	{
		for (UIComponent component : components)
		{
			if (component instanceof UIContainer)
				((UIContainer) component).unregisterChildren();
			else
				getContext().unregister(component);
		}
	}

	public void registerAll()
	{
		getContext().register(this);
		for (UIComponent component : components)
		{
			if (component instanceof UIContainer)
				((UIContainer) component).registerAll();
			else
				getContext().register(component);
		}
	}

	public void unregisterAll()
	{
		getContext().unregister(this);
		for (UIComponent component : components)
		{
			if (component instanceof UIContainer)
				((UIContainer) component).unregisterAll();
			else
				getContext().unregister(component);
		}
	}

	public int getContentWidth()
	{
		if (layoutManager != null)
			return layoutManager.calculateWidth(this);
		else
		{
			int width = 0;
			for (UIComponent component : components)
			{
				if (component.getX() + component.getWidth() > width)
				{
					width = component.getX() + component.getWidth();
				}
			}
			return width;
		}
	}

	public int getContentHeight()
	{
		if (layoutManager != null)
			return layoutManager.calculateHeight(this);
		else
		{
			int height = 0;
			for (UIComponent component : components)
			{
				if (component.getY() + component.getHeight() > height)
				{
					height = component.getY() + component.getHeight();
				}
			}
			return height;
		}
	}

	public Drawable getBackground()
	{
		return background;
	}

	public void setBackground(Drawable background)
	{
		this.background = background;
		background.setSize(this.getSize());
	}

	public void setPadding(Point padding)
	{
		this.padding = padding;
	}

	public void setPadding(int top, int left)
	{
		this.padding = new Point(left, top);
	}

	public Point getPadding()
	{
		return padding;
	}

	public Iterator<UIComponent> components()
	{
		return components.iterator();
	}

	public GuiScreenProxy createScreenProxy()
	{
		return new GuiScreenProxy(this);
	}

	/*****
	 * V2 Ordinastie
	 */
	public ResourceLocation getTexture(int mouseX, int mouseY)
	{
		return null;
	}

	@Override
	public GuiIcon getIcon(int face)
	{
		return null;
	}

	@Override
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{}

	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		for (UIComponent c : components)
			c.draw(renderer, mouseX, mouseY, partialTick);
	}

}
