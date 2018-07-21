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
import java.util.function.Predicate;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;

import net.malisis.core.client.gui.element.position.Position.IPosition;
import net.malisis.core.client.gui.text.GuiText;
import net.malisis.core.client.gui.text.GuiText.LineInfo;
import net.malisis.core.renderer.font.FontOptions.FontOptionsBuilder;
import net.minecraft.util.text.TextFormatting;

/**
 * @author Ordinastie
 *
 */
public class StringWalker
{
	/** List of lines of the walker is used for {@link GuiText}. */
	protected List<LineInfo> lines = Lists.newArrayList();
	/** Current text/line being walked through. */
	protected String currentText;
	/** Whether format character should be considered as regular characters. */
	protected boolean litteral;
	/**
	 * Whether walking through the text automatically advances formatting. Should be set to true when rendering (don't show format
	 * characters), and false when building lines (keeps formatting in lines).
	 */
	protected boolean skipChars = true;
	/** Whether to apply styles for format characters. */
	protected boolean applyStyles;
	/** Space between each line. */
	protected int lineSpacing;

	protected boolean rightAligned = false;

	/** Current global character index. */
	protected int globalIndex = -1;
	/** Current line index. */
	protected int lineIndex = 0;
	/** Current character index in the current line. **/
	protected int charIndex;
	/** Index of end of current line. (size of line - 1). */
	protected int endLineIndex;
	/** Index of the end of text. (size of text - 1). */
	protected int endIndex;
	/** Current character. */
	protected char c;
	/** Current {@link TextFormatting}. */
	protected TextFormatting format;
	/** Position of current character. */
	protected float x;
	/** Position of current character. */
	protected float y;
	/** Width of current character. */
	protected float width;
	/** Height of current character. */
	protected float height;

	protected float lineWidth;
	protected float lineHeight;

	protected LinkedList<FontOptions> styles = Lists.newLinkedList();

	public StringWalker(String text, FontOptions options)
	{
		initLine(text);
		this.lineSpacing = (options != null ? options.lineSpacing() : 2);
		styles.add(options != null ? options : FontOptions.EMPTY);
	}

	public StringWalker(GuiText text, FontOptions options)
	{
		this.lines = text.lines();
		this.litteral = text.isLitteral();
		this.lineSpacing = (options != null ? options.lineSpacing() : 2);
		styles.add(options != null ? options : FontOptions.EMPTY);
		this.rightAligned = options != null && options.isRightAligned();

		if (lines.size() > 0)
		{
			LineInfo lineInfo = lines.get(lineIndex);
			lineHeight = lineInfo.height();
			initLine(lineInfo.text());
		}
	}

	//#region Getters/Setters
	//options
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

	public TextFormatting getFormatting()
	{
		return format;
	}

	public boolean isFormatted()
	{
		return format != null;
	}

	//indexes
	public int globalIndex()
	{
		return globalIndex;
	}

	public int charIndex()
	{
		return charIndex;
	}

	public int lineIndex()
	{
		return lineIndex;
	}

	public char getChar()
	{
		return c;
	}

	public float x()
	{
		return x;
	}

	public float y()
	{
		return y;
	}

	public float width()
	{
		return width;
	}

	public float height()
	{
		return height;
	}

	public float lineHeight()
	{
		return lineHeight + lineSpacing;
	}

	public float lineWidth()
	{
		return x + width;
	}

	public FontOptions currentStyle()
	{
		return styles.getLast();
	}

	public boolean isEOL()
	{
		return charIndex >= endLineIndex;
	}

	public boolean isEOT()
	{
		return lineIndex >= lines.size() - 1 && isEOL();
	}

	//#end Getters/Setters

	/**
	 * Checks if there is {@link TextFormatting} at the current index and applies the corresponding style.<br>
	 * Advances the index by 2.
	 */
	protected void checkFormatting()
	{
		//previous character was formatting, keep format field set
		if (FontOptions.getFormatting(currentText, charIndex - 1) != null)
			return;
		format = FontOptions.getFormatting(currentText, charIndex);
		if (format == null)
			return;

		if (applyStyles)
			applyStyle(format);

		if (skipChars && !litteral)
		{
			globalIndex += 2;
			charIndex += 2;
			checkFormatting();
		}
	}

	/**
	 * Applies the {@link TextFormatting} as a new style.
	 *
	 * @param format the format
	 */
	protected void applyStyle(TextFormatting format)
	{
		if (format == TextFormatting.RESET)
		{
			FontOptions fontOptions = styles.getFirst();
			styles.clear();
			styles.add(fontOptions);
			return;
		}

		FontOptionsBuilder builder = currentStyle().toBuilder();
		builder.styles(format);
		styles.add(builder.build());
	}

	private void initLine(String text)
	{
		currentText = text;
		charIndex = -1;
		endLineIndex = currentText.length() - 1;

		//position
		x = 0;
		if (rightAligned && lines.size() >= lineIndex)
			x += lines.get(lineIndex).spaceWidth() - lines.get(lineIndex).width();
		//add last line height
		height = 0;
		width = 0;
		lineWidth = 0;
	}

	private boolean nextLine()
	{
		if (lineIndex >= lines.size() - 1)
			return false;

		globalIndex += endLineIndex - charIndex; //add line size
		lineIndex++;
		LineInfo lineInfo = lines.get(lineIndex);
		y += lineHeight + lineSpacing;
		lineHeight = lineInfo.height();
		initLine(lineInfo.text());

		return true;//nextCharacter();
	}

	private boolean nextCharacter()
	{
		if (isEOT())
			return false;
		if (isEOL())
		{
			nextLine();
			return nextCharacter();
		}

		globalIndex++;
		charIndex++;

		checkFormatting();
		FontOptions options = currentStyle();

		c = currentText.charAt(charIndex);
		x += width; // add last width
		width = options.getFont().getCharWidth(c, options);
		//		if (lines.size() > 0 && c == ' '/*options.isJustified()*/)
		//			width += lines.get(lineIndex).spaceWidth();
		height = options.getFont().getCharHeight(c, options);
		if (options.isBold())
			width += options.getFontScale();

		lineWidth += width;

		if (!litteral && !skipChars && format != null)
			width = 0;

		return true;
	}

	/**
	 * Advances this {@link StringWalker} until the predicate condition is met.
	 *
	 * @param predicate the predicate
	 * @return true, if successful
	 */
	public boolean walkUntil(Predicate<StringWalker> predicate)
	{
		if (predicate.test(this))
			return true;
		while (nextCharacter())
			if (predicate.test(this))
				return true;
		return false;
	}

	/**
	 * Advances this {@link StringWalker} to the {@code index}.
	 *
	 * @param index the index
	 * @return true if index is in range, false otherwise.
	 */
	public boolean walkToIndex(int index)
	{
		return walkUntil(w -> index == globalIndex);
	}

	/**
	 * Advances this {@link StringWalker} to the next {@code character}.
	 *
	 * @param character the character
	 * @return true if the character was found, false otherwise
	 */
	public boolean walkToChar(char character)
	{
		return walkUntil(w -> character == c);
	}

	/**
	 * Advances this {@link StringWalker} to the end of current line.
	 *
	 * @return true, if successful
	 */
	public boolean walkToEOL()
	{
		return walkUntil(StringWalker::isEOL);
	}

	public boolean walkToEnd()
	{
		return walkUntil(StringWalker::isEOT);
	}

	/**
	 * Advances this {@link StringWalker} to the {@code x} coordinate in the current line.
	 *
	 * @param x the x
	 * @return true, if successful
	 */
	public boolean walkToX(int x)
	{
		walkUntil(w -> lineWidth >= x || isEOL());
		return !isEOL();
	}

	/**
	 * Advances this {@link StringWalker} to the {@code y} coordinate.<br>
	 * Sets the character to the
	 *
	 * @param y the y
	 * @return true, if successful
	 */
	public boolean walkToY(int y)
	{
		if (this.y + lineHeight > y)
			return true;
		while (nextLine())
			if (this.y + lineHeight > y)
				return true;
		return false;
	}

	/**
	 * Advances this {@link StringWalker} to the {@code position}.
	 *
	 * @param position the position
	 * @return the int
	 */
	public Pair<Integer, Integer> walkToCoord(IPosition position)
	{
		walkToY(position.y());
		walkToX(position.x());

		return Pair.of(lineIndex, globalIndex);
	}

	public boolean walk()
	{
		return nextCharacter();
	}
}
