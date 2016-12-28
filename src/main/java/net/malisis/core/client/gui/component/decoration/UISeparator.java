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
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.element.GuiIcon;
import net.malisis.core.client.gui.element.GuiShape;

/**
 * @author Ordinastie
 *
 */
public class UISeparator extends UIComponent<UISeparator>
{
	protected GuiShape shape = new GuiShape(GuiIcon.SEPARATOR);
	/** Color multiplier. */
	protected int color = 0xFFFFFF;
	protected boolean vertical;

	public UISeparator(boolean vertical)
	{
		this.vertical = vertical;
		setSize(INHERITED, INHERITED);
	}

	public UISeparator()
	{
		this(false);
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
	 * @return this {@link UISeparator}
	 */
	public UISeparator setColor(int color)
	{
		this.color = color;
		return this;
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
		shape.setSize(getWidth(), getHeight());
		shape.setColor(getColor());
		renderer.drawShape(shape);
	}

	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{}
}
