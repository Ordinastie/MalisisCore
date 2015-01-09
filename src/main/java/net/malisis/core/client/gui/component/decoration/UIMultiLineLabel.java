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

import java.util.LinkedList;
import java.util.List;

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.control.IScrollable;
import net.malisis.core.client.gui.event.component.SpaceChangeEvent.SizeChangeEvent;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.StatCollector;

import com.google.common.eventbus.Subscribe;

/**
 * @author Ordinastie
 *
 */
public class UIMultiLineLabel extends UILabel implements IScrollable
{
	protected List<String> lines = new LinkedList<>();
	protected boolean autoHeight;
	protected int lineOffset;
	protected int lineSpacing;

	public UIMultiLineLabel(MalisisGui gui)
	{
		super(gui);
		canInteract = true;
	}

	public UIMultiLineLabel(MalisisGui gui, String text)
	{
		this(gui);
		this.setText(text);
	}

	@Override
	public UIMultiLineLabel setText(String text)
	{
		this.text = text;
		buildLines();

		return this;
	}

	public UILabel setSize(int width)
	{
		return setSize(width, height);
	}

	public int getLineHeight()
	{
		return GuiRenderer.FONT_HEIGHT + lineSpacing;
	}

	@Override
	public int getContentWidth()
	{
		return getWidth();
	}

	@Override
	public int getContentHeight()
	{
		return lines.size() * (GuiRenderer.FONT_HEIGHT + 1);
	}

	@Override
	public float getOffsetX()
	{
		return 0;
	}

	@Override
	public void setOffsetX(float offsetX, int delta)
	{}

	@Override
	public float getOffsetY()
	{
		return (float) lineOffset / (lines.size() - visibleLines());
	}

	@Override
	public void setOffsetY(float offsetY, int delta)
	{
		lineOffset = Math.round(offsetY / getScrollStep());
		lineOffset = Math.max(0, Math.min(lines.size(), lineOffset));
	}

	@Override
	public float getScrollStep()
	{
		float step = (float) 1 / (lines.size() - visibleLines());
		return (GuiScreen.isCtrlKeyDown() ? 5 * step : step);
	}

	@Override
	public int getVerticalPadding()
	{
		return 0;
	}

	@Override
	public int getHorizontalPadding()
	{
		return 0;
	}

	private int visibleLines()
	{
		return getHeight() / getLineHeight();
	}

	private void buildLines()
	{
		lines.clear();
		if (text == null)
			return;

		String[] texts = text.split("(?<=\r)\n?");
		int width = getWidth();
		for (String str : texts)
			lines.addAll(GuiRenderer.wrapText(StatCollector.translateToLocal(str), width));
	}

	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		for (int i = lineOffset; i < lineOffset + visibleLines() && i < lines.size(); i++)
		{
			int h = (i - lineOffset) * getLineHeight();
			renderer.drawText(lines.get(i), 0, h, color, drawShadow);
		}
	}

	@Subscribe
	public void onSizeChange(SizeChangeEvent<UIMultiLineLabel> event)
	{
		buildLines();
	}
}
