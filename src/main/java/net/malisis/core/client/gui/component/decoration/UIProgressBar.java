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
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.element.GuiIcon;
import net.malisis.core.client.gui.element.GuiShape;
import net.malisis.core.client.gui.element.GuiShape.ShapePosition;
import net.malisis.core.client.gui.element.GuiShape.ShapeSize;
import net.minecraft.util.math.MathHelper;

/**
 * @author Ordinastie
 *
 */
public class UIProgressBar extends UIComponent<UIProgressBar>
{
	protected float progress = 0;
	protected boolean reversed = false;
	protected boolean vertical = false;

	protected GuiTexture texture;
	protected GuiIcon backgroundIcon;
	protected GuiIcon filledIcon;
	protected GuiShape background = GuiShape.builder().forComponent(this).icon(backgroundIcon).build();
	protected GuiShape filled = GuiShape.builder().position(this::getFillPosition).size(this::getFillSize).icon(this::getFillIcon).build();

	public UIProgressBar(int width, int height, GuiTexture texture, GuiIcon backgroundIcon, GuiIcon filledIcon)
	{
		setSize(width, height);
		this.texture = texture;
		this.backgroundIcon = backgroundIcon;
		this.filledIcon = filledIcon;
	}

	//by default, use furnace arrows
	public UIProgressBar()
	{
		this(22, 16, null, GuiIcon.ARROW_EMPTY, GuiIcon.ARROW_FILLED);
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
		this.progress = MathHelper.clamp(progress, 0, 1);
		//	this.progress = .4F;
	}

	private int getProgressLength()
	{
		return Math.round((vertical ? getHeight() : getWidth()) * progress);
	}

	public ShapePosition getFillPosition()
	{
		int length = getProgressLength();
		return vertical	? ShapePosition.of(0, reversed ? 0 : getHeight() - length)
						: ShapePosition.of(reversed ? getWidth() - length : 0, 0);
	}

	public ShapeSize getFillSize()
	{
		int length = getProgressLength();
		return vertical ? ShapeSize.of(getWidth(), length) : ShapeSize.of(length, getHeight());
	}

	public GuiIcon getFillIcon()
	{
		return vertical	? filledIcon.clip(0, reversed ? progress : 0, 1, reversed ? 1 : progress)
						: filledIcon.clip(reversed ? progress : 0, 0, reversed ? 1 : progress, 1);
	}

	@Override
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		renderer.bindTexture(texture);
		background.render();
	}

	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		filled.render();
	}
}
