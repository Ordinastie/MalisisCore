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

import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.Lists;

import net.malisis.core.renderer.font.FontOptions.FontOptionsBuilder;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;

/**
 * @author Ordinastie
 *
 */
public class StringWalker
{
	private static FontOptions DEFAULT_OPTIONS = FontOptions.builder().build();
	protected MalisisFont font;
	protected String str;
	protected boolean litteral;
	protected boolean skipChars = true;
	protected boolean applyStyles;

	protected List<String> lines;
	protected int currentLine;
	protected boolean isEOL = true;

	protected int index;
	protected int endIndex;
	protected char c;
	protected TextFormatting format;
	protected float width;

	protected LinkedList<FontOptions> styles = Lists.newLinkedList();

	public StringWalker(String str, MalisisFont font, FontOptions options)
	{
		this.str = str;
		this.font = font;
		styles.add(options != null ? options : DEFAULT_OPTIONS);
		this.index = 0;
		this.endIndex = str.length();
		this.litteral = options != null && options.isFormattingDisabled();
	}

	public StringWalker(List<String> lines, MalisisFont font, FontOptions options)
	{
		this(lines.size() > 0 ? lines.get(0) : "", font, options);
		this.lines = lines;
	}

	//#region Getters/Setters

	public void setLitteral(boolean litteral)
	{
		this.litteral = litteral;
	}

	public void skipChars(boolean skip)
	{
		this.skipChars = skip;
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

	public float getWidth()
	{
		return width;
	}

	public void startIndex(int index)
	{
		this.index = MathHelper.clamp(index, 0, endIndex);
	}

	public void endIndex(int index)
	{
		if (index == 0)
			index = str.length();
		this.endIndex = MathHelper.clamp(index, index, str.length());
	}

	public int getCurrentLine()
	{
		return currentLine;
	}

	public String getCurrentText()
	{
		return lines.size() > getCurrentLine() ? lines.get(getCurrentLine()) : "";
	}

	public FontOptions getCurrentStyle()
	{
		return styles.getLast();
	}

	public boolean isEOL()
	{
		return index >= endIndex;
	}

	public boolean isEndOfText()
	{
		return isEOL() && (lines == null || currentLine >= lines.size() - 1);
	}

	//#end Getters/Setters

	protected void checkFormatting()
	{
		format = FontOptions.getFormatting(str, index);
		if (format == null)
			format = FontOptions.getFormatting(str, index - 1);

		if (format == null)
			return;

		if (applyStyles)
			applyStyle(format);

		if (skipChars && !litteral)
		{
			index += 2;
			checkFormatting();
		}
	}

	protected void applyStyle(TextFormatting format)
	{
		if (format == TextFormatting.RESET)
		{
			FontOptions fontOptions = styles.getFirst();
			styles.clear();
			styles.add(fontOptions);
			return;
		}

		FontOptionsBuilder builder = getCurrentStyle().toBuilder();
		builder.styles(format);
		styles.add(builder.build());
	}

	/**
	 * Walk to character index {@code c} and return the coordinate for that character.
	 *
	 * @param c the c
	 * @return the int
	 */
	public float walkToCharacter(int c)
	{
		endIndex(c);
		float w = 0;
		while (walk())
			w += getWidth();

		return w;
	}

	/**
	 * Walk to the specified {@code x} coordinate and returns the character index at that coordinate.
	 *
	 * @param x the x
	 * @return the int
	 */
	public int walkToCoord(float x)
	{
		if (x < 0)
			return getIndex();
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
		if (walkLine())
			return true;

		if (isEndOfText())
			return false;

		//Walk next line
		str = lines.get(++currentLine);
		index = 0;
		endIndex = str.length();
		width = 0;

		return walk();
	}

	private boolean walkLine()
	{
		if (isEOL())
			return false;

		checkFormatting();

		if (isEOL())
			return false;

		FontOptions options = styles.getLast();
		c = str.charAt(index);
		width = font.getCharWidth(c, options);
		if (options.isBold())
			width += options.getFontScale();

		if (!litteral && !skipChars && format != null)
			width = 0;

		index++;
		return true;
	}
}
