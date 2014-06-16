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

import net.malisis.core.client.gui.GuiIcon;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.preset.ShapePreset;
import net.minecraft.util.IIcon;

/**
 * @author Ordinastie
 * 
 */
public class UIProgressBar extends UIComponent<UIProgressBar>
{
	private GuiIcon barIcon = new GuiIcon(246, 0, 22, 16);
	private GuiIcon barFilledIcon = barIcon.offset(0, 16);

	protected float progress = 0;
	protected boolean reversed = false;

	public UIProgressBar()
	{
		width = 22;
		height = 16;
	}

	public float getProgress()
	{
		return progress;
	}

	public UIProgressBar setReversed()
	{
		barIcon = barIcon.getIconFlipped(true, false);
		barFilledIcon = barFilledIcon.getIconFlipped(true, false);
		reversed = true;
		return this;
	}

	public void setProgress(float progress)
	{
		if (progress < 0)
			progress = 0;
		if (progress > 1)
			progress = 1;
		this.progress = progress;
	}

	@Override
	public IIcon getIcon(int face)
	{
		return null;
	}

	@Override
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		Shape shape = ShapePreset.GuiElement(22, 16);
		RenderParameters rp = new RenderParameters();
		rp.icon.set(barIcon);
		renderer.drawShape(shape, rp);
	}

	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		int width = (int) (this.width * progress);
		int xOffset = 0;
		GuiIcon icon = barFilledIcon.clip(0, 0, width, 16);

		if (reversed)
		{
			xOffset = this.width - width;
			icon = barFilledIcon.clip(width - 22, 0, -width, 16);
		}

		Shape shape = ShapePreset.GuiElement(width, 16);
		shape.translate(xOffset, 0, 0);

		RenderParameters rp = new RenderParameters();
		rp.icon.set(icon);
		// rp.uvFactor.set(uvFactors);

		renderer.drawShape(shape, rp);
	}
}
