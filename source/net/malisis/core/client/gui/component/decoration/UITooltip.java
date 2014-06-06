/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 PaleoCrafter, Ordinastie
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

import java.util.Arrays;
import java.util.List;

import net.malisis.core.client.gui.GuiIcon;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.renderer.element.RenderParameters;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.preset.ShapePreset;
import net.minecraft.util.IIcon;

/**
 * UITooltip
 * 
 * @author PaleoCrafter
 */
public class UITooltip extends UIComponent
{
	//@formatter:off
	public static GuiIcon[] icons = new GuiIcon[] { new GuiIcon(227, 	31, 	5, 	5),
													new GuiIcon(232, 	31, 	5, 	5),
													new GuiIcon(237, 	31, 	5, 	5),
													new GuiIcon(227, 	36, 	5, 	5),
													new GuiIcon(232, 	36, 	5, 	5),
													new GuiIcon(237, 	36,  	5, 	5),
													new GuiIcon(227, 	41, 	5, 	5),
													new GuiIcon(232, 	41, 	5, 	5),
													new GuiIcon(237, 	41, 	5, 	5)};
	//@formatter:on

	protected List<String> lines;
	protected int padding = 4;

	public UITooltip()
	{
		width = 16;
		height = 16;
	}

	public UITooltip(String text)
	{
		setText(text);
	}

	public UITooltip setText(String text)
	{
		lines = Arrays.asList(text.split("\\n"));
		calcSize();
		return this;

	}

	public UITooltip setText(List<String> lines)
	{
		this.lines = lines;
		calcSize();
		return this;
	}

	protected void calcSize()
	{
		width = 16;
		height = lines.size() > 1 ? (GuiRenderer.FONT_HEIGHT + 1) * (lines.size()) : 8;
		height += padding * 2;
		for (String s : lines)
		{
			width = Math.max(width, GuiRenderer.getStringWidth(s));
		}
		width += padding * 2;
	}

	@Override
	public IIcon getIcon(int face)
	{
		if (face < 0 || face > icons.length)
			return null;

		return icons[face];
	}

	protected int getOffsetX()
	{
		return 8;
	}

	protected int getOffsetY()
	{
		return -16;
	}

	@Override
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		Shape shape = ShapePreset.GuiXYResizable(width, height);
		shape.translate(mouseX + getOffsetX(), mouseY + getOffsetY(), 300);
		RenderParameters rp = new RenderParameters();
		rp.alpha = 255;
		renderer.drawShape(shape, rp);
	}

	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		int x = mouseX + getOffsetX() + padding;
		int y = mouseY + getOffsetY() + padding;
		int i = 0;
		for (String s : lines)
		{
			int sy = y;
			if (i > 0)
				sy += 2;
			renderer.drawString(s, x, sy + (GuiRenderer.FONT_HEIGHT + 1) * i, 0xFFFFFF, true);
			i++;
		}
	}
}
