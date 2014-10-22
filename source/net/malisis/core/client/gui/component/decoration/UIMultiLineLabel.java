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

import java.util.List;

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.minecraft.util.StatCollector;

/**
 * @author Ordinastie
 *
 */
public class UIMultiLineLabel extends UILabel
{
	protected String[] lines;
	protected boolean autoHeight;

	public UIMultiLineLabel(MalisisGui gui, int width, int height, String... text)
	{
		super(gui);
		this.setText(text);
		this.setSize(width, height);
	}

	public UIMultiLineLabel(MalisisGui gui)
	{
		this(gui, 0, 0);
	}

	public UIMultiLineLabel(MalisisGui gui, String... text)
	{
		this(gui, 0, 0, text);
	}

	@Override
	public UIMultiLineLabel setText(String text)
	{
		return this.setText(new String[] { text });
	}

	public UIMultiLineLabel setText(String... text)
	{
		this.lines = text;
		this.textWidth = GuiRenderer.getMaxStringWidth(text);
		this.textHeight = (GuiRenderer.FONT_HEIGHT + 1) * text.length;
		setSize(autoWidth ? 0 : width, autoHeight ? 0 : height);
		return this;
	}

	@Override
	public UILabel setSize(int width)
	{
		return setSize(width, textHeight);
	}

	@Override
	public UILabel setSize(int width, int height)
	{
		this.autoWidth = width <= 0;
		this.autoHeight = height <= 0;
		this.width = autoWidth ? textWidth : width;
		this.height = autoHeight ? textHeight : height;
		return this;
	}

	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		int i = 0;
		for (String line : lines)
		{
			List<String> lines = GuiRenderer.wrapText(StatCollector.translateToLocal(line), width);
			for (String str : lines)
				renderer.drawText(str, 0, (GuiRenderer.FONT_HEIGHT + 1) * i++, color, drawShadow);
		}
	}
}
