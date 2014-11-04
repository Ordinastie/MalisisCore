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

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.element.GuiShape;
import net.malisis.core.client.gui.element.SimpleGuiShape;
import net.malisis.core.client.gui.element.XResizableGuiShape;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.malisis.core.client.gui.event.KeyboardEvent;
import net.malisis.core.client.gui.event.MouseEvent;
import net.malisis.core.client.gui.icon.GuiIcon;
import net.malisis.core.util.MouseButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatAllowedCharacters;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.google.common.eventbus.Subscribe;

/**
 * UITextField.
 *
 * @author Ordinastie
 */
public class UITextField extends UIComponent<UITextField>
{
	/** Current text of this {@link UITextField}. */
	private StringBuilder text = new StringBuilder();
	/** Current cursor position. */
	private int cursorPosition;
	/** Current selection cursor position. If -1, no selection is active. */
	protected int selectionPosition = -1;
	/** Number of characters offset out of this {@link UITextField} when drawn. */
	private int charOffset = 0;
	/** Cursor blink timer. */
	private long startTimer;
	/** Filter for the inputs. */
	private Pattern filter;
	/** Whether this {@link UITextField} should select the text when release left mouse button. */
	private boolean selectAllOnRelease = false;
	/** Whether this {@link UITextField} should auto select the text when gaining focus. */
	protected boolean autoSelectOnFocus = false;
	/** Color of the text for this {@link UITextField}. */
	private int textColor = 0xFFFFFF;
	/** Shape used to draw the cursor of this {@link UITextField}. */
	private GuiShape cursorShape;
	/** Shape used to draw the selection box. */
	private GuiShape selectShape;
	/** Icon used to draw this {@link UITextField}. */
	protected GuiIcon iconTextfield;
	/** Icon used to draw this {@link UITextField} when disabled. */
	protected GuiIcon iconTextfieldDisabled;

	/**
	 * Instantiates a new {@link UITextField}.
	 *
	 * @param gui the gui
	 * @param width the width
	 * @param text the text
	 */
	public UITextField(MalisisGui gui, int width, String text)
	{
		super(gui);
		this.width = width;
		this.height = 12;
		if (text != null)
			this.text.append(text);

		shape = new XResizableGuiShape(3);
		cursorShape = new SimpleGuiShape();
		cursorShape.setSize(1, 10);
		cursorShape.storeState();
		selectShape = new SimpleGuiShape();

		iconTextfield = gui.getGuiTexture().getXResizableIcon(200, 30, 9, 12, 3);
		iconTextfieldDisabled = gui.getGuiTexture().getXResizableIcon(200, 42, 9, 12, 3);
	}

	/**
	 * Instantiates a new {@link UITextField}.
	 *
	 * @param gui the gui
	 * @param width the width
	 */
	public UITextField(MalisisGui gui, int width)
	{
		this(gui, width, null);
	}

	/**
	 * Clamps the position <i>pos</i> between 0 and text length.
	 *
	 * @param pos the pos
	 * @return position clamped
	 */
	private int clamp(int pos)
	{
		return Math.max(0, Math.min(pos, text.length()));
	}

	/**
	 * Determines the cursor position for a given x coordinate.
	 *
	 * @param x the x coordinate
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
	 *
	 * @param text the text
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

		if (!fireEvent(new ComponentEvent.ValueChange(this, oldValue, newValue)))
			return;

		this.text.insert(cursorPosition, text);
		setCursorPosition(cursorPosition + text.length());
	}

	/**
	 * Gets the width of the part of this {@link #text} delimited by <b>start</b> and <b>end</b>.
	 *
	 * @param start the start
	 * @param end the end
	 * @return the width
	 */
	private int stringWidth(int start, int end)
	{
		if (end <= start)
			return 0;

		return GuiRenderer.getStringWidth(text.substring(clamp(start), clamp(end)));
	}

	/**
	 * Checks against {@link #filter} if text is valid
	 *
	 * @param text the text
	 * @return true, if input is valid
	 */
	protected boolean validateText(String text)
	{
		if (filter == null)
			return true;

		Matcher matcher = filter.matcher(text);
		return matcher.matches();
	}

	// #region getters/setters
	/**
	 * Gets the text of this {@link UITextField}.
	 *
	 * @return the text
	 */
	public String getText()
	{
		return text.toString();
	}

	/**
	 * Sets the text of this {@link UITextField} and place the cursor at the end.
	 *
	 * @param text the new text
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
	 * Gets the current cursor position.
	 *
	 * @return the position of the cursor.
	 */
	public int getCursorPosition()
	{
		return this.cursorPosition;
	}

	/**
	 * Sets the position of the cursor at the specified <b>position</b>
	 *
	 * @param position the new cursor position
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
	 * Sets the text color for this {@link UITextField}.
	 *
	 * @param color the color
	 * @return this {@link UITextField}
	 */
	public UITextField setTextColor(int color)
	{
		this.textColor = color;
		return this;
	}

	/**
	 * Gets the text color.
	 *
	 * @return the text color of this {@link UITextField}.
	 */
	public int getTextColor()
	{
		return textColor;
	}

	/**
	 * Sets the filter for this {@link UITextField}.
	 *
	 * @param regex the regex
	 * @return this {@link UITextField}
	 */
	public UITextField setFilter(String regex)
	{
		if (regex == null || regex.length() == 0)
			filter = null;
		else
			filter = Pattern.compile(regex);
		return this;
	}

	/**
	 * Sets whether this {@link UIComponent} should automatically select its {@link #text} when focused
	 *
	 * @param auto the auto
	 * @return this {@link UITextField}
	 */
	public UITextField setAutoSelectOnFocus(boolean auto)
	{
		autoSelectOnFocus = auto;
		return this;
	}

	// #end getters/setters

	/**
	 * Gets the currently selected text.
	 *
	 * @return the text selected.
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
	 * Deletes the text currently selected.
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
	 * Clears the text selection.
	 */
	public void unselectText()
	{
		selectionPosition = -1;
	}

	/**
	 * Deletes the specified <b>amount</b> of characters. Negative numbers will delete characters left of the cursor.<br>
	 * If text is already selected, delete that text instead.
	 *
	 * @param amount the amount of characters to delete
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
	 *
	 * @param amount the amount
	 */
	public void deleteWords(int amount)
	{
		this.deleteFromCursor(nextSpacePosition(amount < 0));
	}

	/**
	 * Gets the next position with a space character
	 *
	 * @param backwards whether to look left of the cursor
	 * @return the space position
	 */
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
	 * Moves the text cursor by a specified number of characters and clears the selection.
	 *
	 * @param amount the amount
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
	 * Sets the cursor position to the beginning.
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
	 * Sets the cursor position to after the text.
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
	 *
	 * @param pos the new selection position
	 */
	public void setSelectionPosition(int pos)
	{
		if (selectionPosition == -1)
			selectionPosition = cursorPosition;
		setCursorPosition(pos);
	}

	/**
	 * Draws the background.
	 *
	 * @param renderer the renderer
	 * @param mouseX the mouse x
	 * @param mouseY the mouse y
	 * @param partialTick the partial tick
	 */
	@Override
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		rp.useTexture.reset();
		rp.colorMultiplier.reset();
		rp.icon.set(isDisabled() ? iconTextfieldDisabled : iconTextfield);
		renderer.drawShape(shape, rp);
	}

	/**
	 * Draws the foreground.
	 *
	 * @param renderer the renderer
	 * @param mouseX the mouse x
	 * @param mouseY the mouse y
	 * @param partialTick the partial tick
	 */
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

	/**
	 * Draws the text of this {@link UITextField}.
	 *
	 * @param renderer the renderer
	 */
	public void drawText(GuiRenderer renderer)
	{
		int end = text.length();
		while (stringWidth(charOffset, end) > width - 2)
			end--;
		renderer.drawText(text.substring(charOffset, end), 2, 2, isDisabled() ? 0xAAAAAA : textColor, true);
	}

	/**
	 * Draws the cursor of this {@link UITextField}.
	 *
	 * @param renderer the renderer
	 */
	public void drawCursor(GuiRenderer renderer)
	{
		long elaspedTime = startTimer - System.currentTimeMillis();
		if ((elaspedTime / 500) % 2 != 0)
			return;

		GL11.glDisable(GL11.GL_TEXTURE_2D);
		int offset = stringWidth(charOffset, cursorPosition);

		cursorShape.resetState();
		cursorShape.setPosition(offset + 1, 1);

		rp.useTexture.set(false);
		rp.colorMultiplier.set(0xD0D0D0);

		renderer.drawShape(cursorShape, rp);
		renderer.next();

		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	/**
	 * Draw the selection box of this {@link UITextField}.
	 *
	 * @param renderer the renderer
	 */
	public void drawSelectionBox(GuiRenderer renderer)
	{
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_COLOR_LOGIC_OP);
		GL11.glLogicOp(GL11.GL_OR_REVERSE);

		int start = Math.max(Math.min(cursorPosition, selectionPosition), charOffset);
		int width = stringWidth(start, Math.max(cursorPosition, selectionPosition));
		start = stringWidth(charOffset, start);
		width = Math.min(this.width - start - 2, width);

		selectShape.resetState();
		selectShape.setSize(width, 10);
		shape.setPosition(start + 1, 1);

		rp.useTexture.set(false);
		rp.colorMultiplier.set(0x0000FF);

		renderer.drawShape(selectShape, rp);
		renderer.next();

		GL11.glDisable(GL11.GL_COLOR_LOGIC_OP);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	/**
	 * Called when a mouse button is pressed over this {@link UITextField}.
	 *
	 * @param event the event
	 */
	@Subscribe
	public void onClick(MouseEvent.Press event)
	{
		int pos = cursorPositionFromX(relativeX(event.getX()));
		if (GuiScreen.isShiftKeyDown())
			setSelectionPosition(pos);
		else
		{
			unselectText();
			setCursorPosition(pos);
		}

		// selectAllOnRelease = false;
	}

	/**
	 * Called when a mouse button is released over this {@link UITextField}.
	 *
	 * @param event the event
	 */
	@Subscribe
	public void onClick(MouseEvent.Release event)
	{
		if (!autoSelectOnFocus || !selectAllOnRelease)
			return;

		setCursorPosition(0);
		setSelectionPosition(text.length());

		selectAllOnRelease = false;
	}

	/**
	 * Called when a mouse button is dragged over this {@link UITextField}.
	 *
	 * @param event the event
	 */
	@Subscribe
	public void onDrag(MouseEvent.Drag event)
	{
		if (!this.focused || event.getButton() != MouseButton.LEFT)
			return;
		int pos = cursorPositionFromX(relativeX(event.getX()));
		setSelectionPosition(pos);
		selectAllOnRelease = false;
	}

	/**
	 * Called when a mouse button is double clicked over this {@link UITextField}.
	 *
	 * @param event the event
	 */
	@Subscribe
	public void onDoubleClick(MouseEvent.DoubleClick event)
	{
		int pos = cursorPositionFromX(relativeX(event.getX()));
		if (pos > 0 && text.charAt(pos - 1) == ' ')
			selectionPosition = pos;
		else
			selectionPosition = pos + nextSpacePosition(true);

		if (text.charAt(pos) == ' ')
			setCursorPosition(pos);
		else
			setCursorPosition(pos + nextSpacePosition(false) - 1);

	}

	/**
	 * Called when a key is pressed.
	 *
	 * @param event the event
	 */
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

	/**
	 * Handles the key typed while a control key is pressed
	 *
	 * @param keyCode the key code
	 * @return true, if successful
	 */
	protected boolean handleCtrlKeyDown(int keyCode)
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

	@Override
	public String getPropertyString()
	{
		return text + " | " + super.getPropertyString();
	}
}
