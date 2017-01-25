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

import static com.google.common.base.Preconditions.*;

import net.minecraft.util.text.TextFormatting;

/**
 * @author Ordinastie
 *
 */
public class StringWalker
{
	private MalisisFont font;
	private FontOptions fontOptions;
	private String text;
	private boolean litteral;
	private boolean skipFormattingChars = true;
	private boolean applyStyles;
	private boolean isText;

	//	private int prevColor;
	//	private boolean prevUnderline;

	private int index;
	private int endIndex;
	private char c;
	private TextFormatting format;
	private Link link;
	private float width;

	private boolean bold;
	private boolean italic;
	private boolean underline;
	private boolean strikethrough;
	private int color;

	public StringWalker(MalisisFont font, FontOptions options)
	{
		this.font = font;
		this.fontOptions = options;

		applyFontOptions();
	}

	public StringWalker(String text, MalisisFont font, FontOptions options)
	{
		this(font, options);
		setText(text);
	}

	private void applyFontOptions()
	{
		this.bold = fontOptions.isBold();
		this.italic = fontOptions.isItalic();
		this.underline = fontOptions.isUnderline();
		this.strikethrough = fontOptions.isStrikethrough();
		this.color = fontOptions.getColor();
	}

	//#region Getters/Setters

	public void setText(String text, int start, int end)
	{
		this.text = checkNotNull(text);
		this.index = Math.max(0, start);
		this.endIndex = Math.min(end, text.length());
	}

	public void setText(String text)
	{
		setText(text, 0, text.length());
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
	 * Gets the color for this {@link FontOptions}.
	 *
	 * @return the color
	 */
	public int getColor()
	{
		return color;
	}

	public void setLitteral(boolean litteral)
	{
		this.litteral = litteral;
	}

	public void skipFormattingChars(boolean skip)
	{
		this.skipFormattingChars = skip;
	}

	public void applyStyles(boolean apply)
	{
		this.applyStyles = apply;
	}

	public boolean isApplyStyles()
	{
		return applyStyles;
	}

	public int getIndex()
	{
		return index;
	}

	public char getChar()
	{
		return c;
	}

	public TextFormatting getFormatting()
	{
		return format;
	}

	public boolean isFormatted()
	{
		return format != null;
	}

	public Link getLink()
	{
		return link;
	}

	public boolean isLink()
	{
		return link != null;
	}

	public float getWidth()
	{
		return width;
	}

	private void setLinkStyle(FontOptions options)
	{
		if (options == null || litteral || !applyStyles)
			return;

		//prevColor = fro.color;
		//prevUnderline = fro.underline;
		//		fro.saveDefault();
		//fro.color = 0x6666FF;
		//fro.underline = true;
	}

	private void resetLinkStyle(FontOptions options)
	{
		if (options == null || litteral || !applyStyles)
			return;

		//fro.color = prevColor;
		//fro.underline = prevUnderline;
	}

	//#end Getters/Setters

	private void checkFormatting()
	{
		format = FontOptions.getFormatting(text, index);
		if (format == null)
			format = FontOptions.getFormatting(text, index - 1);

		if (format == null)
			return;

		if (applyStyles && fontOptions != null && !isLink())
			applyFormatting(format);

		if (skipFormattingChars && !litteral)
		{
			index += 2;
			checkFormatting();
		}
	}

	public void reset()
	{
		index = 0;
		applyFontOptions();
	}

	/**
	 * Applies the {@link TextFormatting} style to this {@link StringWalker}.
	 *
	 * @param format the ecf
	 */
	private void applyFormatting(TextFormatting format)
	{
		if (format.isColor())
		{
			color = FontOptions.getColor(format);
			return;
		}

		switch (format)
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
			case RESET:
				applyFontOptions();
				break;
			default:
				break;
		}
	}

	public void checkLink()
	{
		if (link != null)
		{
			isText = link.isText(getIndex());

			if (text.charAt(index) == ']')
			{
				resetLinkStyle(fontOptions);
				if (skipFormattingChars && !litteral)
					index++;
			}
		}
		else
		{
			link = FontOptions.getLink(text, index);
			if (isLink())
			{
				if (skipFormattingChars && !litteral)
					index += link.indexAdvance();
				setLinkStyle(fontOptions);
			}
		}

	}

	public int walkTo(float x)
	{
		float width = 0;
		while (walk())
		{
			width += getWidth();
			if (width > x)
				return getIndex() - 1;
		}

		return getIndex();
	}

	public boolean walk()
	{
		if (index >= endIndex)
			return false;

		checkFormatting();
		//checkLink();

		if (index >= endIndex)
			return false;

		c = text.charAt(index);
		width = font.getCharWidth(c) * fontOptions.getFontScale();
		//width += fontOptions.getFontScale();
		if (isBold())
			width += fontOptions.getFontScale();

		if (!litteral && !skipFormattingChars && (format != null || (link != null && !isText)))
			width = 0;

		index++;
		return true;
	}
}
