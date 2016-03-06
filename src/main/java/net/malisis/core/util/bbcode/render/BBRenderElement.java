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

package net.malisis.core.util.bbcode.render;

import java.util.EnumSet;

import net.malisis.core.client.gui.GuiRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

/**
 * @author Ordinastie
 *
 */
public class BBRenderElement
{
	public EnumSet<EnumChatFormatting> styles = EnumSet.noneOf(EnumChatFormatting.class);
	public boolean shadow;
	public int color = 0;
	public int bgColor = 0;
	public String text;
	public ItemStack itemStack;
	public int line;
	public boolean newLine = false;

	public BBRenderElement(BBRenderElement element)
	{
		if (element == null)
			return;

		this.color = element.color;
		this.bgColor = element.bgColor;
		this.styles.addAll(element.styles);
		this.line = element.line;
	}

	public BBRenderElement split(int position)
	{
		if (text == null)
			return null;

		if (position < 0 || position > text.length())
			return null;

		if (position == text.length())
		{
			newLine = true;
			return null;
		}

		BBRenderElement split = new BBRenderElement(this);
		split.text = text.substring(position);
		split.line++;

		text = text.substring(0, position);
		newLine = true;

		return split;
	}

	public String getFormattedText()
	{
		String str = "";
		for (Object ecf : styles)
			str += ecf;
		return str + text;
	}

	public int width()
	{
		if (itemStack != null)
			return 16;
		return 0;//GuiRenderer.getStringWidth(getFormattedText());
	}

	public void render(GuiRenderer renderer, int x, int y, int z)
	{
		if (itemStack != null)
			renderer.drawItemStack(itemStack, x, y);
		else
			renderer.drawText(null, getFormattedText(), x, y, z, null);
	}

	@Override
	public String toString()
	{
		return styles.toString() + Integer.toHexString(color) + "/" + Integer.toHexString(bgColor) + " : " + text + (newLine ? "~" : "");
	}
}