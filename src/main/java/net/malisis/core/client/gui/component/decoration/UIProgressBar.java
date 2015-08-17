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
import net.malisis.core.client.gui.element.SimpleGuiShape;
import net.malisis.core.renderer.icon.provider.GuiIconProvider;

/**
 * @author Ordinastie
 *
 */
public class UIProgressBar extends UIComponent<UIProgressBar>
{
	protected float progress = 0;
	protected boolean reversed = false;

	protected GuiIconProvider filledIconProvider;

	public UIProgressBar(MalisisGui gui)
	{
		super(gui);
		setSize(22, 16);

		shape = new SimpleGuiShape();
		iconProvider = new GuiIconProvider(gui.getGuiTexture().getIcon(246, 0, 22, 16));
		filledIconProvider = new GuiIconProvider(gui.getGuiTexture().getIcon(246, 16, 22, 16));
	}

	public float getProgress()
	{
		return progress;
	}

	public UIProgressBar setReversed()
	{
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
		//	this.progress = .4F;
	}

	@Override
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		shape.resetState();
		shape.setSize(width, height);
		//TODO:
		//		barIcon.flip(reversed, false);
		//		rp.icon.set(barIcon);
		renderer.drawShape(shape, rp);
	}

	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		int width = (int) (this.width * progress);
		//		barFilledIcon.clip(0, 0, width, 16);
		//		barFilledIcon.flip(reversed, false);
		shape.resetState();
		shape.setSize(width, 16);
		shape.translate(reversed ? this.width - width : 0, 0);
		//		rp.icon.set(barFilledIcon);
		renderer.drawShape(shape, rp);
	}
}
