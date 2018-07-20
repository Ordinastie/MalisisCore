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

import java.util.function.IntSupplier;
import java.util.function.Supplier;

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
public class UIProgressBar extends UIComponent
{
	protected float progress = 0;
	protected boolean reversed = false;
	protected boolean vertical = false;

	public UIProgressBar(ISize size, GuiIcon backgroundIcon, GuiIcon filledIcon)
	{
		setSize(size);

		IntSupplier xSupplier = null;
		IntSupplier ySupplier = null;
		ISize fillSize;
		Supplier<GuiIcon> fillIcon;
		if (vertical)
		{
			xSupplier = () -> reversed ? 0 : size().height() - getProgressLength();
			fillSize = Size.of(() -> size().width(), this::getProgressLength);
			fillIcon = () -> filledIcon.clip(0, reversed ? getIconProgress() : 0, 1, reversed ? 1 : getIconProgress());

		}
		else
		{
			ySupplier = () -> reversed ? size().width() - getProgressLength() : 0;
			fillSize = Size.of(this::getProgressLength, () -> size().height());
			fillIcon = () -> filledIcon.clip(reversed ? getIconProgress() : 0, 0, reversed ? 1 : getIconProgress(), 1);
		}

		//backgroundIcon = new GuiIcon(MalisisGui.BLOCK_TEXTURE, (float) 0, (float) 0, (float) 1, (float) 1);
		setBackground(GuiShape.builder(this).icon(backgroundIcon).build());
		setForeground(GuiShape.builder(this).position(xSupplier, ySupplier).size(fillSize).icon(fillIcon).build());
	}

	//by default, use furnace arrows
	public UIProgressBar(MalisisGui gui)
	{
		this(Size.of(22, 16), GuiIcon.ARROW_EMPTY, GuiIcon.ARROW_FILLED);
	}

	public UIProgressBar setReversed()
	{
		reversed = true;
		return this;
	}

	public UIProgressBar setVertical()
	{
		vertical = true;
		return this;
	}

	public float getProgress()
	{
		return progress;
	}

	public void setProgress(float progress)
	{
		if (progress < 0)
			progress = 0;
		if (progress > 1)
			progress = 1;
		this.progress = progress;
		//this.progress = .6F;
	}

	private int getProgressLength()
	{
		return Math.round((vertical ? size().height() : size().width()) * progress);
	}

	private float getIconProgress()
	{
		return (float) getProgressLength() / (vertical ? size().height() : size().width());
	}
}
