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

import javax.annotation.Nonnull;

import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.element.size.Size;
import net.malisis.core.client.gui.element.size.Size.ISize;
import net.malisis.core.client.gui.render.GuiIcon;
import net.malisis.core.client.gui.render.shape.GuiShape;

/**
 * @author Ordinastie
 *
 */
public class UISeparator extends UIComponent
{
	/** Color multiplier. */
	protected int color = 0xFFFFFF;
	/** Whether the separator is vertical or horizontal. */
	protected final boolean vertical;
	/** Separator size. */
	private final ISize separatorSize = Size.of(() -> (isVertical() ? size.width() : 1), () -> (isVertical() ? 1 : size.height()));

	public UISeparator(boolean vertical)
	{
		this.vertical = vertical;
		setForeground(GuiShape.builder(this).icon(GuiIcon.SEPARATOR).color(this::getColor).build());
	}

	public UISeparator(MalisisGui gui)
	{
		this(false);
	}

	/**
	 * Checks if this {@link UISeparator} is vertical.
	 *
	 * @return true, if is vertical
	 */
	public boolean isVertical()
	{
		return vertical;
	}

	@Override
	@Nonnull
	public ISize size()
	{
		return separatorSize;
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
	@Override
	public int getColor()
	{
		return color;
	}
}
