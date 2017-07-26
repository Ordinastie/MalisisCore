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
import java.util.List;
import java.util.Map;

import net.minecraft.util.text.TextFormatting;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;

/**
 * @author Ordinastie
 *
 */
public class FontOptions
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
	private float fontScale = 1;
	/** Color of the text **/
	private int color = 0x000000; //black
	/** Draw with shadow **/
	private boolean shadow = false;
	/** Use bold font **/
	private boolean bold;
	/** Use italic font **/
	private boolean italic;
	/** Underline the text **/
	private boolean underline;
	/** Strike through the text **/
	private boolean strikethrough;
	/** Disable ECF so char are actually drawn **/
	private boolean formattingDisabled = false;
	/** Translate the text before display */
	private boolean translate = true;

	private FontOptions defaultFro;
	private FontOptions lineOptions;
	private boolean defaultSaved = false;

	private FontOptions(float fontScale, int color, boolean shadow, boolean bold, boolean italic, boolean underline, boolean strikethrough, boolean translate)
	{
		this.fontScale = fontScale;
		this.color = color;
		this.shadow = shadow;
		this.italic = italic;
		this.underline = underline;
		this.strikethrough = strikethrough;
		this.translate = translate;

		defaultFro = new FontOptions();
		lineOptions = new FontOptions();

		saveDefault();
		lineOptions.from(this);
	}

	private FontOptions()
	{
		//constructor without a default.
		//'this' object should already be the default for another FRO
	}

	/**
	 * Gets the font scale for this {@link FontOptions}.
	 *
	 * @return the font scale
	 */
	public float getFontScale()
	{
		return fontScale;
	}

	/**
	 * Checks if this {@link FontOptions} is bold.
	 *
	 * @return true, if is bold
	 */
	public boolean isBold()
	{
		return bold;
	}

	/**
	 * Checks if this {@link FontOptions} is italic.
	 *
	 * @return true, if is italic
	 */
	public boolean isItalic()
	{
		return italic;
	}

	/**
	 * Checks if this {@link FontOptions} is underlined.
	 *
	 * @return true, if is underline
	 */
	public boolean isUnderline()
	{
		return underline;
	}

	/**
	 * Checks if this {@link FontOptions} is strikethrough.
	 *
	 * @return true, if is strikethrough
	 */
	public boolean isStrikethrough()
	{
		return strikethrough;
	}

	/**
	 * Checks whether draw shadow is enabled for this {@link FontOptions}.
	 *
	 * @return true, if successful
	 */
	public boolean hasShadow()
	{
		return shadow;
	}

	/**
	 * Gets the color for this {@link FontOptions}.
	 *
	 * @return the color
	 */
	public int getColor()
	{
		return color;
	}

	/**
	 * Checks if formatting is disabled (formatting character are renderer literally).
	 *
	 * @return true, if is formatting disabled
	 */
	public boolean isFormattingDisabled()
	{
		return formattingDisabled;
	}

	/**
	 * Checks if the text should be translated before rendering.
	 *
	 * @return true, if successful
	 */
	public boolean shouldTranslate()
	{
		return translate;
	}

	/**
	 * Process styles applied to the beginning of the text with {@link TextFormatting} values.<br>
	 * Applies the styles to this {@link FontOptions} and returns the number of characters read.
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
	 * Applies the styles to this {@link FontOptions} and returns the number of characters read.
	 *
	 * @param text the text
	 * @param index the index
	 * @return the int
	 */
	public int processStyles(String text, int index)
	{
		if (!defaultSaved)
			saveDefault();
		if (formattingDisabled)
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
	 * Applies the {@link TextFormatting} style to this {@link FontOptions}.
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

	/**
	 * Saves the current styles as default.<br>
	 * Default styles are restored when {@link #resetStyles()} is called.
	 */
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

	/**
	 * Resets styles to the default values.
	 */
	public void resetStyles()
	{
		if (!defaultSaved)
		{
			saveDefault();
			return;
		}

		from(defaultFro);
	}

	/**
	 * Sets the line options.
	 *
	 * @param options the new line options
	 */
	public void setLineOptions(FontOptions options)
	{
		lineOptions.from(options);
	}

	public void resetLineOptions()
	{
		from(lineOptions);
	}

	/**
	 * Sets the styles for this {@link FontOptions} based on the passed one.
	 *
	 * @param options the fro
	 */
	private void from(FontOptions options)
	{
		fontScale = options.fontScale;
		color = options.color;
		bold = options.bold;
		italic = options.italic;
		strikethrough = options.strikethrough;
		underline = options.underline;
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
	 * Gets the {@link TextFormatting TextFormattings} at the specified position in the text.
	 *
	 * @param text the text
	 * @param index the index
	 * @return the formattings
	 */
	public static List<TextFormatting> getFormattings(String text, int index)
	{
		List<TextFormatting> list = Lists.newArrayList();
		TextFormatting format;
		int offset = 0;
		while ((format = getFormatting(text, index + offset)) != null)
		{
			offset += 2;
			list.add(format);
		}
		return list;
	}

	/**
	 * Gets the string corresponding to the passed {@link TextFormatting TextFormattings}.
	 *
	 * @param list the list
	 * @return the string
	 */
	public static String formattingAsText(List<TextFormatting> list)
	{
		StringBuilder builder = new StringBuilder();
		list.forEach(f -> builder.append(f));
		return builder.toString();
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

	/**
	 * Get a non translation version of this {@link FontOptions}
	 *
	 * @return the font options
	 */
	public FontOptions notTranslated()
	{
		return builder().scale(fontScale)
						.color(color)
						.shadow(shadow)
						.bold(bold)
						.italic(italic)
						.underline(underline)
						.strikethrough(strikethrough)
						.disableTranslation()
						.build();
	}

	/**
	 * Create the {@link FontOptionsBuilder} for a new {@link FontOptions}.
	 *
	 * @return the font options builder
	 */
	public static FontOptionsBuilder builder()
	{
		return new FontOptionsBuilder();
	}

	public static class FontOptionsBuilder
	{
		private float fontScale = 1;
		private int color = 0x000000; //black
		private boolean shadow = false;
		private boolean bold = false;
		private boolean italic = false;
		private boolean underline = false;
		private boolean strikethrough = false;
		private boolean translate = true;

		public FontOptionsBuilder()
		{}

		public FontOptionsBuilder scale(float scale)
		{
			this.fontScale = scale;
			return this;
		}

		public FontOptionsBuilder color(int color)
		{
			this.color = color;
			return this;
		}

		public FontOptionsBuilder bold()
		{
			return bold(true);
		}

		public FontOptionsBuilder bold(boolean bold)
		{
			this.bold = bold;
			return this;
		}

		public FontOptionsBuilder italic()
		{
			return italic(true);
		}

		public FontOptionsBuilder italic(boolean italic)
		{
			this.italic = italic;
			return this;
		}

		public FontOptionsBuilder underline()
		{
			return underline(true);
		}

		public FontOptionsBuilder underline(boolean underline)
		{
			this.underline = underline;
			return this;
		}

		public FontOptionsBuilder strikethrough()
		{
			return strikethrough(true);
		}

		public FontOptionsBuilder strikethrough(boolean strikethrough)
		{
			this.strikethrough = strikethrough;
			return this;
		}

		public FontOptionsBuilder shadow()
		{
			return shadow(true);
		}

		public FontOptionsBuilder shadow(boolean shadow)
		{
			this.shadow = shadow;
			return this;
		}

		public FontOptionsBuilder disableTranslation()
		{
			this.translate = false;
			return this;
		}

		public FontOptionsBuilder styles(String styles)
		{
			for (TextFormatting format : getFormattings(styles, 0))
				styles(format);
			return this;
		}

		public FontOptionsBuilder styles(TextFormatting... formats)
		{
			for (TextFormatting f : formats)
			{
				if (f.isColor())
					color(colors[f.ordinal()]);
				else
					switch (f)
					{
						case BOLD:
							bold();
							break;
						case UNDERLINE:
							underline();
							break;
						case ITALIC:
							italic();
							break;
						case STRIKETHROUGH:
							strikethrough();
							break;
						default:
					}
			}
			return this;
		}

		public FontOptions build()
		{
			return new FontOptions(fontScale, color, shadow, bold, italic, underline, strikethrough, translate);
		}

	}
}
