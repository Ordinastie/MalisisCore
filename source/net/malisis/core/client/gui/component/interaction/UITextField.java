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

package net.malisis.core.client.gui.component.interaction;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.malisis.core.client.gui.GuiIcon;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.malisis.core.client.gui.event.KeyboardEvent;
import net.malisis.core.client.gui.event.MouseEvent;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.preset.ShapePreset;
import net.malisis.core.util.MouseButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatAllowedCharacters;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.google.common.eventbus.Subscribe;

/**
 * UITextField
 * 
 * @author Ordinastie
 */
public class UITextField extends UIComponent<UITextField>
{
	//@formatter:off
	public static GuiIcon[] iconTextfield = new GuiIcon[] { 		new GuiIcon(200,	30, 	3, 		12),
																	new GuiIcon(203,	30, 	3, 		12),
																	new GuiIcon(206, 	30, 	3, 		12)};
	public static GuiIcon[] iconTextfieldDisabled = new GuiIcon[] { iconTextfield[0].offset(0,  12),
																	iconTextfield[1].offset(0,  12),
																	iconTextfield[2].offset(0,  12) };
	//@formatter:on

	/**
	 * Current text of this <code>UITextField</code>
	 */
	private StringBuilder text = new StringBuilder();
	/**
	 * Current cursor position.
	 */
	private int cursorPosition;
	/**
	 * Current selection cursor position. If -1, no selection is active.
	 */
	private int selectionPosition = -1;
	/**
	 * Number of characters offset out of this <code>UITextField</code> when drawn.
	 */
	private int charOffset = 0;
	/**
	 * Cursor blink timer
	 */
	private long startTimer;

	private Pattern filter;

	private boolean selectAllOnRelease = false;

	protected boolean autoSelectOnFocus = false;

	/**
	 * Color of the text for this <code>UITextField</code>
	 */
	private int textColor = 0xFFFFFF;

	public UITextField(int width, String text)
	{
		super();
		this.width = width;
		this.height = 12;
		if (text != null)
			this.text.append(text);
		iconTextfield = new GuiIcon[] { new GuiIcon(200, 30, 3, 12), new GuiIcon(203, 30, 3, 12), new GuiIcon(206, 30, 3, 12) };
	}

	public UITextField(int width)
	{
		this(width, null);
	}

	/**
	 * Clamps the position <i>pos</i> between 0 and text length
	 * 
	 * @param pos
	 * @return position clamped
	 */
	private int clamp(int pos)
	{
		return Math.max(0, Math.min(pos, text.length()));
	}

	/**
	 * Determines the cursor position for a given x coordinate
	 * 
	 * @param x
	 * @return position
	 */
	private int cursorPositionFromX(int x)
	{
		int pos = 0;
		int width = 0;
		for (int i = charOffset; i < text.length(); i++)
		{
			int w = GuiRenderer.getCharWidth(text.charAt(i));
			if (width + ((float) w / 2) > x)
				return pos + charOffset;
			width += w;
			pos++;
		}
		return pos + charOffset;
	}

	/**
	 * Adds text at current cursor position. If some text is selected, it's deleted first.
	 */
	public void addText(String text)
	{
		if (selectionPosition != -1)
			deleteSelectedText();

		String oldValue = this.text.toString();
		StringBuilder temp = new StringBuilder(oldValue);
		temp.insert(cursorPosition, text);
		String newValue = temp.toString();

		if (!validateText(temp.toString()))
			return;

		if (!fireEvent(new ComponentEvent.ValueChanged(this, oldValue, newValue)))
			return;

		this.text.insert(cursorPosition, text);
		setCursorPosition(cursorPosition + text.length());

	}

	private int stringWidth(int start, int end)
	{
		if (end <= start)
			return 0;

		return GuiRenderer.getStringWidth(text.substring(clamp(start), clamp(end)));
	}

	private boolean validateText(String text)
	{
		if (filter == null)
			return true;

		Matcher matcher = filter.matcher(text);
		return matcher.matches();
	}

	// #region getters/setters
	/**
	 * @return the text of this <code>UITextField</code>.
	 */
	public String getText()
	{
		return text.toString();
	}

	/**
	 * Sets the text of this <code>UITextField</code> and place the cursor at the end.
	 */
	public void setText(String text)
	{
		if (!validateText(text))
			return;

		this.text.setLength(0);
		this.text.append(text);
		unselectText();
		if (focused)
			this.jumpToEnd();
		// fireEvent(new TextChanged(this));
	}

	@Override
	public void setFocused(boolean focused)
	{
		if (isDisabled() || !isVisible())
			return;

		if (!this.focused)
			selectAllOnRelease = true;
		else
			unselectText();
		super.setFocused(focused);
	}

	/**
	 * @return the current position of the cursor.
	 */
	public int getCursorPosition()
	{
		return this.cursorPosition;
	}

	/**
	 * Sets the position of the cursor to the provided index.
	 * 
	 * @param position
	 */
	public void setCursorPosition(int position)
	{
		this.cursorPosition = clamp(position);
		while (stringWidth(charOffset, cursorPosition) > width - 3)
			charOffset++;
		if (charOffset > cursorPosition)
			charOffset = position;
		startTimer = System.currentTimeMillis();
	}

	/**
	 * Sets the text color for this <code>UITextField</code>.
	 * 
	 * @return this <code>UITextField</code>
	 */
	public UITextField setTextColor(int color)
	{
		this.textColor = color;
		return this;
	}

	/**
	 * @return the text color of this <code>UITextField</code>.
	 */
	public int getTextColor()
	{
		return textColor;
	}

	public UITextField setFilter(String regex)
	{
		if (regex == null || regex.length() == 0)
			filter = null;
		else
			filter = Pattern.compile(regex);
		return this;
	}

	public UITextField setAutoSelectOnFocus(boolean auto)
	{
		autoSelectOnFocus = auto;
		return this;
	}

	// #end getters/setters

	/**
	 * @return the text currently selected.
	 */
	public String getSelectedText()
	{
		if (selectionPosition == -1)
			return "";

		int start = Math.min(selectionPosition, cursorPosition);
		int end = Math.max(selectionPosition, cursorPosition);

		return this.text.substring(start, end);
	}

	/**
	 * Deletes the text currently selected
	 */
	public void deleteSelectedText()
	{
		int start = Math.min(selectionPosition, cursorPosition);
		int end = Math.max(selectionPosition, cursorPosition);

		this.text.delete(start, end);
		unselectText();
		setCursorPosition(start);
	}

	/**
	 * Clear the text selection
	 */
	public void unselectText()
	{
		selectionPosition = -1;
	}

	/**
	 * Deletes the selected text, otherwise deletes characters from either side of the cursor. params: delete num
	 */
	public void deleteFromCursor(int amount)
	{
		if (text.length() == 0)
			return;

		if (selectionPosition == -1)
			selectionPosition = cursorPosition + amount;

		deleteSelectedText();
	}

	/**
	 * Deletes the specified number of words starting at the cursor position. Negative numbers will delete words left of the cursor.
	 */
	public void deleteWords(int amount)
	{
		this.deleteFromCursor(nextSpacePosition(amount < 0));
	}

	public int nextSpacePosition(boolean backwards)
	{
		int pos = cursorPosition + (backwards ? -1 : 1);
		if (pos < 0 || pos > text.length())
			return 0;

		if (text.charAt(pos) == ' ')
			pos--;
		while (pos > 0 && pos < text.length())
		{
			if (text.charAt(pos) == ' ')
				return pos + 1 - cursorPosition;
			pos += backwards ? -1 : 1;
		}
		return pos - cursorPosition;
	}

	/**
	 * Moves the text cursor by a specified number of characters and clears the selection
	 */
	public void moveCursorBy(int amount)
	{
		if (GuiScreen.isShiftKeyDown())
			setSelectionPosition(cursorPosition);
		else
			unselectText();
		this.setCursorPosition(this.cursorPosition + amount);
	}

	/**
	 * sets the cursors position to the beginning
	 */
	public void jumpToBegining()
	{
		if (GuiScreen.isShiftKeyDown())
			setSelectionPosition(cursorPosition);
		else
			unselectText();
		this.setCursorPosition(0);
	}

	/**
	 * sets the cursors position to after the text
	 */
	public void jumpToEnd()
	{
		if (GuiScreen.isShiftKeyDown())
			setSelectionPosition(cursorPosition);
		else
			unselectText();
		this.setCursorPosition(this.text.length());
	}

	/**
	 * Starts text selection. The selection anchor is set to current cursor position, and the cursor is moved to the new position.
	 */
	public void setSelectionPosition(int pos)
	{
		if (selectionPosition == -1)
			selectionPosition = cursorPosition;
		setCursorPosition(pos);
	}

	@Override
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		Shape shape = ShapePreset.GuiXResizable(width, height, 3);
		renderer.drawShape(shape, isDisabled() ? iconTextfieldDisabled : iconTextfield);
	}

	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		if (text.length() != 0)
			drawText(renderer);
		if (selectionPosition != -1 && selectionPosition != cursorPosition)
			drawSelectionBox(renderer);
		if (focused)
			drawCursor(renderer);
	}

	public void drawText(GuiRenderer renderer)
	{
		int end = text.length();
		while (stringWidth(charOffset, end) > width - 2)
			end--;
		renderer.drawText(text.substring(charOffset, end), 2, 2, isDisabled() ? 0xAAAAAA : textColor, true);
	}

	public void drawCursor(GuiRenderer renderer)
	{
		long elaspedTime = startTimer - System.currentTimeMillis();
		if ((elaspedTime / 500) % 2 != 0)
			return;

		GL11.glDisable(GL11.GL_TEXTURE_2D);
		int offset = stringWidth(charOffset, cursorPosition);

		Shape shape = ShapePreset.GuiElement(1, 10);
		shape.translate(offset + 1, 1, 0);

		RenderParameters rp = new RenderParameters();
		rp.useTexture.set(false);
		rp.colorMultiplier.set(0xD0D0D0);

		renderer.drawShape(shape, rp);
		renderer.next();

		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	public void drawSelectionBox(GuiRenderer renderer)
	{
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_COLOR_LOGIC_OP);
		GL11.glLogicOp(GL11.GL_OR_REVERSE);

		int start = Math.max(Math.min(cursorPosition, selectionPosition), charOffset);
		int width = stringWidth(start, Math.max(cursorPosition, selectionPosition));
		start = stringWidth(charOffset, start);
		width = Math.min(this.width - start - 2, width);

		Shape shape = ShapePreset.GuiElement(width, 10);
		shape.translate(start + 1, 1, 0);

		RenderParameters rp = new RenderParameters();
		rp.useTexture.set(false);
		rp.colorMultiplier.set(0x0000FF);

		renderer.drawShape(shape, rp);
		renderer.next();

		GL11.glDisable(GL11.GL_COLOR_LOGIC_OP);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	@Subscribe
	public void onClick(MouseEvent.Press event)
	{
		int pos = cursorPositionFromX(componentX(event.getX()));
		if (GuiScreen.isShiftKeyDown())
			setSelectionPosition(pos);
		else
		{
			unselectText();
			setCursorPosition(pos);
		}

		// selectAllOnRelease = false;
	}

	@Subscribe
	public void onClick(MouseEvent.Release event)
	{
		if (!autoSelectOnFocus || !selectAllOnRelease)
			return;

		setCursorPosition(0);
		setSelectionPosition(text.length());

		selectAllOnRelease = false;
	}

	@Subscribe
	public void onDrag(MouseEvent.Drag event)
	{
		if (!this.focused || event.getButton() != MouseButton.LEFT)
			return;
		int pos = cursorPositionFromX(componentX(event.getX()));
		setSelectionPosition(pos);
		selectAllOnRelease = false;
	}

	@Subscribe
	public void keyTyped(KeyboardEvent event)
	{
		if (!focused)
			return;

		char keyChar = event.getKeyChar();
		int keyCode = event.getKeyCode();

		if (keyCode == Keyboard.KEY_ESCAPE)
			return;

		event.cancel();

		if (handleCtrlKeyDown(keyCode))
			return;

		switch (keyCode)
		{
			case Keyboard.KEY_LEFT:
				this.moveCursorBy(-1);
				return;
			case Keyboard.KEY_RIGHT:
				this.moveCursorBy(1);
				return;
			case Keyboard.KEY_HOME:
				this.jumpToBegining();
				return;
			case Keyboard.KEY_END:
				this.jumpToEnd();
				return;
			case Keyboard.KEY_BACK:
				this.deleteFromCursor(-1);
				return;
			case Keyboard.KEY_DELETE:
				this.deleteFromCursor(1);
				return;
			default:
				if (ChatAllowedCharacters.isAllowedCharacter(keyChar))
					this.addText(Character.toString(keyChar));
				return;
		}
	}

	private boolean handleCtrlKeyDown(int keyCode)
	{
		if (!GuiScreen.isCtrlKeyDown())
			return false;

		switch (keyCode)
		{
			case Keyboard.KEY_LEFT:
				this.moveCursorBy(nextSpacePosition(true));
				return true;
			case Keyboard.KEY_RIGHT:
				this.moveCursorBy(nextSpacePosition(false));
				return true;
			case Keyboard.KEY_BACK:
				this.deleteWords(-1);
				return true;
			case Keyboard.KEY_DELETE:
				this.deleteWords(1);
				return true;
			case Keyboard.KEY_A:
				setCursorPosition(0);
				setSelectionPosition(text.length());
				return true;
			case Keyboard.KEY_C:
				GuiScreen.setClipboardString(this.getSelectedText());
				return true;
			case Keyboard.KEY_V:
				this.addText(GuiScreen.getClipboardString());
				return true;
			case Keyboard.KEY_X:
				GuiScreen.setClipboardString(this.getSelectedText());
				this.addText("");
				return true;
			default:
				return false;
		}
	}
}
