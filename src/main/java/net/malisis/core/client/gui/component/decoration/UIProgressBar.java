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
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.element.SimpleGuiShape;
import net.malisis.core.renderer.icon.Icon;
import net.malisis.core.renderer.icon.VanillaIcon;

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
	protected Icon backgroundIcon;
	protected Icon filledIcon;

	//by default, use furnace arrows
	public UIProgressBar(MalisisGui gui)
	{
		super(gui);
		setSize(22, 16);

		shape = new SimpleGuiShape();
		texture = getGui().getGuiTexture();
		backgroundIcon = texture.createIcon(246, 0, 22, 16);
		filledIcon = texture.createIcon(246, 16, 22, 16);
	}

	public UIProgressBar(MalisisGui gui, int width, int height, GuiTexture texture, Icon backgroundIcon, Icon filledIcon)
	{
		super(gui);
		setSize(width, height);

		shape = new SimpleGuiShape();
		this.texture = texture;
		this.backgroundIcon = backgroundIcon;
		this.filledIcon = filledIcon;
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
		//	this.progress = .4F;
	}

	@Override
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		renderer.bindTexture(texture);
		rp.icon.set(backgroundIcon.flip(!vertical && reversed, vertical && reversed));
		shape.resetState();
		shape.setSize(getWidth(), getHeight());
		renderer.drawShape(shape, rp);
	}

	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		renderer.bindTexture(texture);
		int width = getWidth();
		int height = getHeight();
		int length = (int) ((vertical ? width : height) * progress);

		Icon icon = filledIcon;
		if (icon instanceof VanillaIcon)
			icon = new Icon(icon);
		if (vertical)
			icon = icon.clip(0, icon.getIconHeight() - length, icon.getIconWidth(), length);
		else
			icon = icon.clip(0, 0, length, icon.getIconHeight());
		icon.flip(!vertical && reversed, vertical && reversed);
		rp.icon.set(icon);

		shape.resetState();
		shape.setSize(vertical ? width : length, vertical ? length : height);
		if (vertical)
			shape.translate(0, reversed ? 0 : height - length);
		else
			shape.translate(reversed ? width - length : 0, 0);
		renderer.drawShape(shape, rp);
	}
}
