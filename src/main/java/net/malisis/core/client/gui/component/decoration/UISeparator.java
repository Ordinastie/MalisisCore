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

package net.malisis.core.client.gui.component.decoration;

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.element.XYResizableGuiShape;
import net.malisis.core.renderer.icon.provider.GuiIconProvider;

/**
 * @author Ordinastie
 *
 */
public class UISeparator extends UIComponent<UISeparator>
{
	/** Color multiplier. */
	protected int color = -1;
	protected boolean vertical;

	public UISeparator(MalisisGui gui, boolean vertical)
	{
		super(gui);
		this.vertical = vertical;

		shape = new XYResizableGuiShape(1);
		iconProvider = new GuiIconProvider(gui.getGuiTexture().getXYResizableIcon(200, 15, 15, 15, 3));

		setSize(0, 0);
	}

	public UISeparator(MalisisGui gui)
	{
		this(gui, false);
	}

	@Override
	public UISeparator setSize(int width, int height)
	{
		return super.setSize(vertical ? 1 : width, vertical ? height : 1);
	}

	/**
	 * Sets the color for this {@link UISeparator}.
	 *
	 * @param color the color
	 */
	@Override
	public void setColor(int color)
	{
		this.color = color;
	}

	/**
	 * Gets the color.
	 *
	 * @return the color for this {@link UISeparator}.
	 */
	public int getColor()
	{
		return color;
	}

	@Override
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		rp.useTexture.set(true);
		rp.alpha.set(255);
		rp.colorMultiplier.set(getColor());
		renderer.drawShape(shape, rp);
	}

	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{}
}
