/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Ordinastie
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

import net.malisis.core.client.gui.ClipArea;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.element.XYResizableGuiShape;
import net.malisis.core.renderer.animation.transformation.ITransformable;
import net.malisis.core.renderer.icon.provider.GuiIconProvider;

public class UIPanel extends UIContainer<UIPanel> implements ITransformable.Color
{
	/** Background color multiplier. */
	protected int backgroundColor = -1;

	public UIPanel(MalisisGui gui)
	{
		super(gui);
		setPadding(3, 3);

		shape = new XYResizableGuiShape(5);
		iconProvider = new GuiIconProvider(gui.getGuiTexture().getXYResizableIcon(200, 15, 15, 15, 5));
	}

	public UIPanel(MalisisGui gui, int width, int height)
	{
		this(gui);
		setSize(width, height);
	}

	public UIPanel(MalisisGui gui, String title)
	{
		this(gui);
		setTitle(title);
	}

	public UIPanel(MalisisGui gui, String title, int width, int height)
	{
		this(gui);
		setTitle(title);
		setSize(width, height);
	}

	/**
	 * Sets the background color for {@link UIContainer}.
	 *
	 * @param color the color
	 * @return the UI container
	 */
	public UIPanel setBackgroundColor(int color)
	{
		this.backgroundColor = color;
		return this;
	}

	/**
	 * Gets the background color.
	 *
	 * @return the background color for {@link UIContainer}.
	 */
	public int getBackgroundColor()
	{
		return backgroundColor;
	}

	/**
	 * Sets the background color of this {@link UIContainer}.
	 *
	 * @param color the new color
	 */
	@Override
	public void setColor(int color)
	{
		setBackgroundColor(color);
	}

	@Override
	public ClipArea getClipArea()
	{
		return new ClipArea(this, 1);
	}

	@Override
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		rp.useTexture.set(true);
		rp.alpha.set(255);
		rp.colorMultiplier.set(getBackgroundColor() != 0x404040 ? getBackgroundColor() : -1);
		renderer.drawShape(shape, rp);
		renderer.next();
	}
}
