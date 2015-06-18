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

package net.malisis.core.renderer.font;

import net.minecraft.util.EnumChatFormatting;

/**
 * @author Ordinastie
 *
 */
public class StringWalker
{
	private static StringWalker instance = new StringWalker();

	private MalisisFont font;
	private FontRenderOptions fro;
	private String str;
	private boolean applyStyles;
	private boolean ignoreScale;

	private int index;
	private char c;
	private EnumChatFormatting ecf;
	private float width;

	public StringWalker()
	{}

	//#region Getters/Setters
	public int getIndex()
	{
		return index;
	}

	public char getChar()
	{
		return c;
	}

	public EnumChatFormatting getFormatting()
	{
		return ecf;
	}

	public boolean isFormatting()
	{
		return ecf != null;
	}

	public float getWidth()
	{
		return width;
	}

	public void ignoreFontScale(boolean ignore)
	{
		ignoreScale = ignore;
	}

	public void startIndex(int index)
	{
		this.index = index;
	}

	//#end Getters/Setters

	public boolean walk()
	{
		if (index >= str.length())
			return false;

		ecf = fro.disableECF ? null : FontRenderOptions.getFormatting(str, index);
		c = str.charAt(index);
		width = font.getCharWidth(c);
		if (isFormatting())
		{
			width = 0;
			index++;
			if (applyStyles)
				fro.apply(ecf);
		}
		if (!ignoreScale)
			width *= fro.fontScale;

		index++;
		return true;
	}

	private void set(String str, MalisisFont font, FontRenderOptions fro, boolean applyStyles)
	{
		this.str = str;
		this.font = font;
		this.fro = fro;
		this.applyStyles = applyStyles;
		this.index = 0;
	}

	public static StringWalker get(String str, MalisisFont font, FontRenderOptions fro, boolean applyStyles)
	{
		instance.set(str, font, fro, applyStyles);
		return instance;
	}
}
