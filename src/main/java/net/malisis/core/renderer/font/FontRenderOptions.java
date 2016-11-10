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

import net.minecraft.util.text.TextFormatting;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

/**
 * @author Ordinastie
 *
 */
public class FontRenderOptions
{
	/** Map of TextFormatting **/
	private static Map<Character, TextFormatting> charFormats = new HashMap<>();
	/** List of ECF colors **/
	private static int[] colors = new int[32];
	static
	{
		//could reflect to get TextFormatting.formattingCodeMapping instead
		for (TextFormatting ecf : TextFormatting.values())
			charFormats.put(ecf.toString().charAt(1), ecf);

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

	private FontRenderOptions defaultFro;
	private FontRenderOptions lineFro;
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
		defaultFro = new FontRenderOptions(false);

		from(fro);

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
	 * Process styles applied to the beginning of the text with {@link TextFormatting} values.<br>
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
	 * Process styles applied at the specified position in the text with {@link TextFormatting} values.<br>
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
		TextFormatting ecf;
		int offset = 0;
		while ((ecf = getFormatting(text, index + offset)) != null)
		{
			offset += 2;
			apply(ecf);
		}

		return offset;
	}

	/**
	 * Applies the {@link TextFormatting} style to this {@link FontRenderOptions}.
	 *
	 * @param ecf the ecf
	 */
	public void apply(TextFormatting ecf)
	{
		if (!defaultSaved)
			saveDefault();
		if (ecf == TextFormatting.RESET)
			resetStyles();
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
		defaultFro.fontScale = fontScale;
	}

	public void resetStyles()
	{
		if (!defaultSaved)
		{
			saveDefault();
			return;
		}

		from(defaultFro);
	}

	public void setLineFro(FontRenderOptions fro)
	{
		if (lineFro == null)
			lineFro = new FontRenderOptions();
		lineFro.from(fro);
	}

	public void resetStylesLine()
	{
		if (lineFro == null)
		{
			resetStyles();
			return;
		}

		from(lineFro);
	}

	public void from(FontRenderOptions fro)
	{
		fontScale = fro.fontScale;
		color = fro.color;
		bold = fro.bold;
		italic = fro.italic;
		strikethrough = fro.strikethrough;
		underline = fro.underline;
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
	 * Gets the {@link TextFormatting} at the specified position in the text.<br>
	 * Returns null if none is found.
	 *
	 * @param text the text
	 * @param index the index
	 * @return the formatting
	 */
	public static TextFormatting getFormatting(String text, int index)
	{
		if (StringUtils.isEmpty(text) || index < 0 || index > text.length() - 2)
			return null;

		char c = text.charAt(index);
		if (c != '\u00a7')
			return null;
		return charFormats.get(text.charAt(index + 1));
	}

	/**
	 * Checks if there is a {@link TextFormatting} at the specified position in the text.
	 *
	 * @param text the text
	 * @param index the index
	 * @return true, if ECF
	 */
	public static boolean isFormatting(String text, int index)
	{
		return getFormatting(text, index) != null;
	}

	/**
	 * Gets a {@link Pair} separating the {@link TextFormatting} tags at the beginning of a <code>text</code>.
	 *
	 * @param text the text
	 * @return a Pair with the formatting in the left part and the text in the right part.
	 */
	public static Pair<String, String> getStartFormat(String text)
	{
		int offset = 0;
		while (getFormatting(text, offset) != null)
			offset += 2;

		return Pair.of(text.substring(0, offset), text.substring(offset, text.length()));
	}

	public static Link getLink(String text, int index)
	{
		if (StringUtils.isEmpty(text) || index < 0 || index > text.length() - 2)
			return null;

		if (text.charAt(index) != '[')
			return null;
		int i = text.indexOf(']');
		if (i < 2)
			return null;

		Link link = new Link(index, text.substring(index + 1, i));
		return link.isValid() ? link : null;
	}
}
