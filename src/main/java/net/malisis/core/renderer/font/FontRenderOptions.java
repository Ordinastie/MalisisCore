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

import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.EnumChatFormatting;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Ordinastie
 *
 */
public class FontRenderOptions
{
	/** Map of EnumChatFormatting **/
	private static Map<Character, EnumChatFormatting> charFormats = new HashMap<>();
	/** List of ECF colors **/
	private static int[] colors = new int[32];
	static
	{
		//could reflect to get EnumChatFormatting.formattingCodeMapping instead
		for (EnumChatFormatting ecf : EnumChatFormatting.values())
			charFormats.put(ecf.getFormattingCode(), ecf);

		//build colors for ECF
		for (int i = 0; i < 16; ++i)
		{
			int j = (i >> 3 & 1) * 85;
			int r = (i >> 2 & 1) * 170 + j;
			int g = (i >> 1 & 1) * 170 + j;
			int b = (i >> 0 & 1) * 170 + j;

			if (i == 6) //GOLD
				r += 85;

			colors[i] = (r & 255) << 16 | (g & 255) << 8 | b & 255;
		}
	}

	/** Scale for the font **/
	public float fontScale = 1;
	/** Color of the text **/
	public int color = 0x000000; //black
	/** Draw with shadow **/
	public boolean shadow = false;
	/** Use bold font **/
	public boolean bold;
	/** Use italic font **/
	public boolean italic;
	/** Underline the text **/
	public boolean underline;
	/** Striketrhough the text **/
	public boolean strikethrough;
	/** Disable ECF so char are actually drawn **/
	public boolean disableECF = false;
	/** Multilines (styles are only reset by ECF) **/
	public boolean multiLines = false;

	private FontRenderOptions defaultFro;
	private boolean defaultSaved = false;

	public FontRenderOptions()
	{
		defaultFro = new FontRenderOptions(false);
	}

	public FontRenderOptions(boolean b)
	{
		//constructor without a default.
		//'this' object should already be the default for another FRO
	}

	public FontRenderOptions(FontRenderOptions fro)
	{
		fontScale = fro.fontScale;
		color = fro.color;
		bold = fro.bold;
		italic = fro.italic;
		strikethrough = fro.strikethrough;
		underline = fro.underline;

		defaultFro = new FontRenderOptions(false);
		saveDefault();
		defaultSaved = false;
	}

	public FontRenderOptions(String ecfs)
	{
		defaultFro = new FontRenderOptions(false);
		processStyles(ecfs);
		saveDefault();
		defaultSaved = false;
	}

	public FontRenderOptions(String ecfs, int color)
	{
		this(ecfs);
		this.color = color;
		defaultFro.color = color;
	}

	/**
	 * Process styles applied to the beginning of the text with {@link EnumChatFormatting} values.<br>
	 * Applies the styles to this {@link FontRenderOptions} and returns the number of characters read.
	 *
	 * @param text the text
	 * @return the string with ECF
	 */
	public int processStyles(String text)
	{
		return processStyles(text, 0);
	}

	/**
	 * Process styles applied at the specified position in the text with {@link EnumChatFormatting} values.<br>
	 * Applies the styles to this {@link FontRenderOptions} and returns the number of characters read.
	 *
	 * @param text the text
	 * @param index the index
	 * @return the int
	 */
	public int processStyles(String text, int index)
	{
		if (!defaultSaved)
			saveDefault();
		if (disableECF)
			return 0;
		EnumChatFormatting ecf;
		int offset = 0;
		while ((ecf = getFormatting(text, index + offset)) != null)
		{
			offset += 2;
			apply(ecf);
		}

		return offset;
	}

	/**
	 * Applies the {@link EnumChatFormatting} style to this {@link FontRenderOptions}.
	 *
	 * @param ecf the ecf
	 */
	public void apply(EnumChatFormatting ecf)
	{
		if (ecf == EnumChatFormatting.RESET)
			resetStyles(true);
		else if (ecf.isColor())
		{
			color = colors[ecf.ordinal()];
		}
		else
		{
			switch (ecf)
			{
				case STRIKETHROUGH:
					strikethrough = true;
					break;
				case BOLD:
					bold = true;
					break;
				case ITALIC:
					italic = true;
					break;
				case UNDERLINE:
					underline = true;
					break;
				default:
					break;
			}
		}
	}

	public void saveDefault()
	{
		defaultSaved = true;
		defaultFro.color = color;
		defaultFro.strikethrough = strikethrough;
		defaultFro.bold = bold;
		defaultFro.italic = italic;
		defaultFro.underline = underline;
	}

	public void resetStyles()
	{
		resetStyles(true);
	}

	public void resetStyles(boolean force)
	{
		if (!defaultSaved)
			saveDefault();
		if (!force && multiLines)
			return;

		color = defaultFro.color;
		strikethrough = defaultFro.strikethrough;
		bold = defaultFro.bold;
		italic = defaultFro.italic;
		underline = defaultFro.underline;
	}

	/**
	 * Gets the shadow color corresponding to the current color.
	 *
	 * @return the shadow color
	 */
	public int getShadowColor()
	{
		if (color == 0) //black
			return 0x222222;
		if (color == 0xFFAA00) //gold
			return 0x2A2A00;

		int r = (color >> 16) & 255;
		int g = (color >> 8) & 255;
		int b = color & 255;

		r /= 4;
		g /= 4;
		b /= 4;

		return (r & 255) << 16 | (g & 255) << 8 | b & 255;
	}

	/**
	 * Gets the {@link EnumChatFormatting} at the specified position in the text.<br>
	 * Returns null if none is found.
	 *
	 * @param text the text
	 * @param index the index
	 * @return the formatting
	 */
	public static EnumChatFormatting getFormatting(String text, int index)
	{
		if (StringUtils.isEmpty(text) || index >= text.length() - 2)
			return null;

		char c = text.charAt(index);
		if (c != '\u00a7')
			return null;
		return charFormats.get(text.charAt(index + 1));
	}

	/**
	 * Checks if there is a {@link EnumChatFormatting} at the specified position in the text.
	 *
	 * @param text the text
	 * @param index the index
	 * @return true, if ECF
	 */
	public static boolean isFormatting(String text, int index)
	{
		return getFormatting(text, index) != null;
	}

}
