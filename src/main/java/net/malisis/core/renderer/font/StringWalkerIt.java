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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;

import net.malisis.core.client.gui.element.position.Position.IPosition;
import net.malisis.core.client.gui.text.GuiText.LineInfo;
import net.malisis.core.renderer.font.FontOptions.FontOptionsBuilder;
import net.malisis.core.renderer.font.StringWalkerIt.CharInfo;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;

/**
 * @author Ordinastie
 *
 */
public class StringWalkerIt implements Iterable<CharInfo>
{
	protected String str;
	protected boolean litteral;
	protected boolean skipChars = true;
	protected boolean applyStyles;

	protected List<LineInfo> lines;

	//global index
	protected int index;
	//current line
	protected int lineIndex;
	//char in line
	protected int charIndex;
	//line end index
	protected int lineEndIndex;
	//global end index
	protected int endIndex;
	//current char
	protected char c;

	protected boolean isEOL = true;

	protected TextFormatting format;
	protected float width;
	protected float height;
	protected float lineHeight;
	protected int lineSpacing;

	protected LinkedList<FontOptions> styles = Lists.newLinkedList();

	public StringWalkerIt(String str, FontOptions options)
	{
		this.str = str;
		styles.add(options != null ? options : FontOptions.EMPTY);
		this.charIndex = 0;
		this.endIndex = str.length();
		this.litteral = options != null && options.isFormattingDisabled();
		this.lineSpacing = (options != null ? options.lineSpacing() : 3);
	}

	public StringWalkerIt(List<LineInfo> lines, FontOptions options)
	{
		this(lines.size() > 0 ? lines.get(0).text() : "", options);
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

	public int getLineIndex()
	{
		return lineIndex;
	}

	public int getIndex()
	{
		return charIndex;
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

	public float getHeight()
	{
		return height;
	}

	public float getLineHeight()
	{
		return lineHeight + lineSpacing;
	}

	public void startIndex(int index)
	{
		this.charIndex = MathHelper.clamp(index, 0, endIndex);
	}

	public void endIndex(int index)
	{
		if (index == 0)
			index = str.length();
		this.endIndex = MathHelper.clamp(index, index, str.length());
	}

	public LineInfo currentLine()
	{
		return lines.get(lineIndex);
	}

	public String getCurrentText()
	{
		return lines.size() > lineIndex ? currentLine().text() : "";
	}

	public FontOptions getCurrentStyle()
	{
		return styles.getLast();
	}

	public boolean isEOL()
	{
		return charIndex >= endIndex;
	}

	public boolean isEndOfText()
	{
		return isEOL() && (lines == null || lineIndex >= lines.size() - 1);
	}

	//#end Getters/Setters

	protected void checkFormatting()
	{
		format = FontOptions.getFormatting(str, charIndex);
		if (format == null)
			format = FontOptions.getFormatting(str, charIndex - 1);

		if (format == null)
			return;

		if (applyStyles)
			applyStyle(format);

		if (skipChars && !litteral)
		{
			charIndex += 2;
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

	public void setLineIndex(int lineIndex)
	{
		this.lineIndex = MathHelper.clamp(charIndex, 0, lines.size() - 1);
		str = lines.get(++lineIndex).text();
		charIndex = 0;
		endIndex = str.length();
		width = 0;
		height = 0;
		lineHeight = 0;
	}

	/**
	 * Walks to character index {@code c} and return the coordinate for that character.
	 *
	 * @param c the c
	 * @return the int
	 */
	public float walkToCharacter(int c)
	{
		endIndex(c - 1);
		return walkToEnd();
	}

	/**
	 * Walks to the set endIndex character and return the width of characters walked.
	 *
	 * @return the float
	 */
	public float walkToEnd()
	{
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
	public Pair<Integer, Integer> walkToCoord(IPosition position)
	{
		walkToY(position.y());
		walkToX(position.x());

		return Pair.of(lineIndex, charIndex);
	}

	public int walkToX(int x)
	{
		float width = 0;
		while (walk() && width + getWidth() <= x)
			width += getWidth();
		return charIndex;
	}

	public int walkToY(int y)
	{
		int h = 0;
		while (!isEndOfText() && h + currentLine().height() <= y)
		{
			h += currentLine().height();
			setLineIndex(lineIndex++);
		}

		return lineIndex;
	}

	public boolean walk()
	{
		if (walkLine())
			return true;

		if (isEndOfText())
			return false;

		//Walk next line
		setLineIndex(lineIndex++);

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
		c = str.charAt(charIndex);
		width = options.getFont().getCharWidth(c, options);
		//		if (lines != null && c == ' '/*options.isJustified()*/)
		//			width += lines.get(currentLine).getSpaceWidth();
		height = options.getFont().getCharHeight(c, options);
		lineHeight = Math.max(lineHeight, height);

		if (options.isBold())
			width += options.getFontScale();

		if (!litteral && !skipChars && format != null)
			width = 0;

		charIndex++;
		return true;
	}

	@Override
	public Iterator<CharInfo> iterator()
	{
		return new CharIterator(endIndex);
	}

	public class CharIterator implements Iterator<CharInfo>
	{
		int end = 0;

		public CharIterator(int end)
		{
			this.end = end;
		}

		@Override
		public boolean hasNext()
		{
			return index < endIndex;
		}

		@Override
		public CharInfo next()
		{
			index++;
			charIndex++;

			FontOptions options = styles.getLast();
			c = str.charAt(charIndex);
			width = options.getFont().getCharWidth(c, options);
			//		if (lines != null && c == ' '/*options.isJustified()*/)
			//			width += lines.get(currentLine).getSpaceWidth();
			height = options.getFont().getCharHeight(c, options);
			lineHeight = Math.max(lineHeight, height);

			if (options.isBold())
				width += options.getFontScale();

			if (!litteral && !skipChars && format != null)
				width = 0;

			return new CharInfo(StringWalkerIt.this);
		}
	}

	public static class CharInfo
	{
		public final char c;
		public final int globalIndex;
		public final int lineIndex;
		public final int charIndex;
		//public final float x;
		//public final float y;
		public final float width;
		public final float height;

		public CharInfo(StringWalkerIt walker)
		{
			this.c = walker.c;
			this.globalIndex = walker.index;
			this.lineIndex = walker.lineIndex;
			this.charIndex = walker.charIndex;

			//this.x = walker.x;
			//this.y = walker.y;
			this.width = walker.width;
			this.height = walker.height;
		}
	}
}
