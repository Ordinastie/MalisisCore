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

import java.util.LinkedList;
import java.util.List;

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.control.IScrollable;
import net.malisis.core.client.gui.component.control.UIScrollBar.Type;
import net.malisis.core.client.gui.component.control.UISlimScrollbar;
import net.malisis.core.client.gui.element.GuiShape;
import net.malisis.core.client.gui.element.SimpleGuiShape;
import net.malisis.core.client.gui.element.XYResizableGuiShape;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.malisis.core.client.gui.event.component.ContentUpdateEvent;
import net.malisis.core.client.gui.icon.GuiIcon;
import net.malisis.core.util.MouseButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.StatCollector;

import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

/**
 * UITextField.
 *
 * @author Ordinastie
 */
public class UITextField extends UIComponent<UITextField> implements IScrollable
{
	/** Current text of this {@link UITextField}. */
	protected StringBuilder text = new StringBuilder();
	/** Different lines if {@link #multiLine} is <code>true</code>. */
	protected List<String> lines = new LinkedList<>();
	/** Whether this {@link UITextField} handles multiline text. */
	protected boolean multiLine = false;
	/** Current cursor position. */
	protected CursorPosition cursorPosition;
	/** Current selection cursor position. */
	protected CursorPosition selectionPosition;

	/** Whether currently selecting text. */
	protected boolean selectingText = false;
	/** Number of character offset out of this {@link UITextField} when drawn. */
	protected int charOffset = 0;
	/** Number of line offset out of this {@link UITextField} when drawn. Always 0 if {@link #multiLine} is false */
	protected int lineOffset = 0;
	/** Space used between each line. */
	protected int lineSpacing = 1;
	/** Cursor blink timer. */
	protected long startTimer;
	/** Whether this {@link UITextField} should select the text when release left mouse button. */
	private boolean selectAllOnRelease = false;
	/** Whether this {@link UITextField} should auto select the text when gaining focus. */
	protected boolean autoSelectOnFocus = false;
	/** Whether this {@link UITextField} is editable. */
	protected boolean editable = true;
	/** Scrollbar of the textfield **/
	protected UISlimScrollbar scrollBar;

	/** Color of the text for this {@link UITextField}. */
	protected int textColor = 0xFFFFFF;
	/** Background color of this {@link UITextField. */
	protected int bgColor = 0xFFFFFF;
	/** Cursor color for this {@link UITextField}. */
	protected int cursorColor = 0xD0D0D0;
	/** Selection color for this {@link UITextField}. */
	protected int selectColor = 0x0000FF;
	/** Text shadow */
	protected boolean textShadow = true;

	/** Font scale used to draw the text *. */
	protected float fontScale = 1;
	/** Shape used to draw the cursor of this {@link UITextField}. */
	protected GuiShape cursorShape;
	/** Shape used to draw the selection box. */
	protected GuiShape selectShape;
	/** Icon used to draw this {@link UITextField}. */
	protected GuiIcon iconTextfield;
	/** Icon used to draw this {@link UITextField} when disabled. */
	protected GuiIcon iconTextfieldDisabled;

	/**
	 * Instantiates a new {@link UITextField}.
	 *
	 * @param gui the gui
	 * @param text the text
	 * @param multiLine whether the textfield handles multiple lines
	 */
	public UITextField(MalisisGui gui, String text, boolean multiLine)
	{
		super(gui);
		this.multiLine = multiLine;
		cursorPosition = new CursorPosition();
		selectionPosition = new CursorPosition();
		if (text != null)
			this.setText(text);

		createShape(gui);

		if (multiLine)
			scrollBar = new UISlimScrollbar(gui, this, Type.VERTICAL);
	}

	/**
	 * Instantiates a new single lined {@link UITextField}.
	 *
	 * @param gui the gui
	 * @param text the text
	 */
	public UITextField(MalisisGui gui, String text)
	{
		this(gui, text, false);
	}

	/**
	 * Instantiates a new empty {@link UITextField}.
	 *
	 * @param gui the gui
	 * @param multiLine the multi line
	 */
	public UITextField(MalisisGui gui, boolean multiLine)
	{
		this(gui, null, multiLine);
	}

	/**
	 * Creates the shapes used by this {@link UITextField}.
	 *
	 * @param gui the gui
	 */
	protected void createShape(MalisisGui gui)
	{
		shape = new XYResizableGuiShape(1);
		cursorShape = new SimpleGuiShape();
		selectShape = new SimpleGuiShape();

		iconTextfield = gui.getGuiTexture().getXYResizableIcon(200, 30, 9, 12, 1);
		iconTextfieldDisabled = gui.getGuiTexture().getXYResizableIcon(200, 42, 9, 12, 1);
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

	public int getTextColor()
	{
		return textColor;
	}

	public UITextField setTextColor(int color)
	{
		this.textColor = color;
		return this;
	}

	public int getCursorColor()
	{
		return cursorColor;
	}

	public UITextField setCursorColor(int cursorColor)
	{
		this.cursorColor = cursorColor;
		return this;
	}

	public int getSelectColor()
	{
		return selectColor;
	}

	public UITextField setSelectColor(int selectColor)
	{
		this.selectColor = selectColor;
		return this;
	}

	public boolean isTextShadow()
	{
		return textShadow;
	}

	public UITextField setTextShadow(boolean textShadow)
	{
		this.textShadow = textShadow;
		return this;
	}

	public UITextField setOptions(int textColor, int bgColor, int cursorColor, int selectColor, boolean textShadow)
	{
		this.textColor = textColor;
		this.bgColor = bgColor;
		this.cursorColor = cursorColor;
		this.selectColor = selectColor;
		this.textShadow = textShadow;

		return this;
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
		buildLines();
		selectingText = false;
		charOffset = 0;
		lineOffset = 0;
		if (focused)
			cursorPosition.jumpToEnd();
		// fireEvent(new TextChanged(this));
	}

	/**
	 * Sets the size of this {@link UITextField}.<br>
	 * If {@link #multiLine} is <code>false</code>, <b>height</b> is forced to 12.
	 *
	 * @param width the width
	 * @param height the height
	 * @return the UI text field
	 */
	@Override
	public UITextField setSize(int width, int height)
	{
		return super.setSize(width, multiLine ? height : 12);
	}

	/**
	 * Sets the focused.
	 *
	 * @param focused the new focused
	 */
	@Override
	public void setFocused(boolean focused)
	{
		if (isDisabled() || !isVisible())
			return;

		if (!this.focused)
			selectAllOnRelease = true;

		super.setFocused(focused);
	}

	/**
	 * Sets the font scale for this {@link UITextField}.
	 *
	 * @param fontScale the font scale
	 * @return this {@link UITextField}
	 */
	public UITextField setFontScale(float fontScale)
	{
		this.fontScale = fontScale;
		buildLines();
		return this;
	}

	/**
	 * Gets the font scale used by this {@link UITextField}.
	 *
	 * @return the font scale
	 */
	public float getFontScale()
	{
		return fontScale;
	}

	/**
	 * Gets the line spacing used when drawing.
	 *
	 * @return the lineSpacing
	 */
	public int getLineSpacing()
	{
		return lineSpacing;
	}

	/**
	 * Sets the line offset.
	 *
	 * @param line the new line offset
	 */
	public void setLineOffset(int line)
	{
		this.lineOffset = line;
	}

	/**
	 * Sets the line spacing for this {@link UITextField}.
	 *
	 * @param lineSpacing the lineSpacing to set
	 * @return this {@link UITextField}
	 */
	public UITextField setLineSpacing(int lineSpacing)
	{
		this.lineSpacing = lineSpacing;
		return this;
	}

	/**
	 * Gets the line height of this {@link UITextField}.
	 *
	 * @return the line height
	 */
	public int getLineHeight()
	{
		return (int) Math.ceil((GuiRenderer.FONT_HEIGHT + lineSpacing) * fontScale);
	}

	/**
	 * Gets the current cursor position.
	 *
	 * @return the position of the cursor.
	 */
	public CursorPosition getCursorPosition()
	{
		return cursorPosition;
	}

	/**
	 * Gets the selection position.
	 *
	 * @return the selection position
	 */
	public CursorPosition getSelectionPosition()
	{
		return selectionPosition;
	}

	/**
	 * Sets the position of the cursor at the specified cooridnates.
	 *
	 * @param x the x coordinate
	 * @param y the y coordinate
	 */
	public void setCursorPosition(int x, int y)
	{
		cursorPosition.setPosition(x, y);

		startTimer = System.currentTimeMillis();
	}

	/**
	 * Sets whether this {@link UITextField} should automatically select its {@link #text} when focused.
	 *
	 * @param auto the auto
	 * @return this {@link UITextField}
	 */
	public UITextField setAutoSelectOnFocus(boolean auto)
	{
		autoSelectOnFocus = auto;
		return this;
	}

	public boolean isEditable()
	{
		return editable;
	}

	public UITextField setEditable(boolean editable)
	{
		this.editable = editable;
		return this;
	}

	/**
	 * Gets the {@link UISlimScrollbar} of this {@link UITextField}.
	 *
	 * @return the scrollbar
	 */
	public UISlimScrollbar getScrollbar()
	{
		return scrollBar;
	}

	// #end getters/setters

	/**
	 * Builds the lines for this {@link UITextField}. Does nothing if {@link #multiLine} is <code>false</code>.
	 */
	protected void buildLines()
	{
		lines.clear();

		if (text == null || text.length() == 0)
		{
			fireEvent(new ContentUpdateEvent<UITextField>(this));
			return;
		}

		if (!multiLine)
		{
			lines.add(text.toString());
			return;
		}

		String[] texts = text.toString().split("\r?(?<=\n)");
		int width = getWidth() - 4;
		for (String str : texts)
			lines.addAll(GuiRenderer.wrapText(StatCollector.translateToLocal(str), width, fontScale));

		if (text.charAt(text.length() - 1) == '\n')
			lines.add("");

		fireEvent(new ContentUpdateEvent<UITextField>(this));
	}

	/**
	 * Adds text at current cursor position. If some text is selected, it's deleted first.
	 *
	 * @param text the text
	 */
	public void addText(String text)
	{
		if (selectingText)
			deleteSelectedText();

		int position = cursorPosition.textPosition;
		String oldValue = this.text.toString();
		String newValue = new StringBuilder(oldValue).insert(position, text).toString();

		if (!validateText(newValue))
			return;

		if (!fireEvent(new ComponentEvent.ValueChange(this, oldValue, newValue)))
			return;

		this.text.insert(position, text);
		buildLines();
		cursorPosition.jumpBy(text.length());
	}

	/**
	 * Checks against if text is valid.
	 *
	 * @param text the text
	 * @return true, if input is valid
	 */
	protected boolean validateText(String text)
	{
		//TODO : handle text validator
		return true;
	}

	/**
	 * Gets the currently selected text.
	 *
	 * @return the text selected.
	 */
	public String getSelectedText()
	{
		if (!selectingText)
			return "";

		int start = Math.min(selectionPosition.textPosition, cursorPosition.textPosition);
		int end = Math.max(selectionPosition.textPosition, cursorPosition.textPosition);

		return this.text.substring(start, end);
	}

	/**
	 * Deletes the text currently selected.
	 */
	public void deleteSelectedText()
	{
		if (!selectingText)
			return;

		int start = Math.min(selectionPosition.textPosition, cursorPosition.textPosition);
		int end = Math.max(selectionPosition.textPosition, cursorPosition.textPosition);

		String oldValue = this.text.toString();
		String newValue = new StringBuilder(oldValue).substring(start, end).toString();

		if (!fireEvent(new ComponentEvent.ValueChange(this, oldValue, newValue)))
			return;

		text.delete(start, end);
		selectingText = false;
		buildLines();
		cursorPosition.jumpTo(start);
	}

	/**
	 * Deletes the specified <b>amount</b> of characters. Negative numbers will delete characters left of the cursor.<br>
	 * If text is already selected, delete that text instead.
	 *
	 * @param amount the amount of characters to delete
	 */
	public void deleteFromCursor(int amount)
	{
		if (!selectingText)
		{
			selectingText = true;
			selectionPosition.set(cursorPosition);
			selectionPosition.jumpBy(amount);
		}
		deleteSelectedText();
	}

	/**
	 * Deletes the text from current cursor position to the next space.
	 *
	 * @param backwards whether to look left for the next space
	 */
	public void deleteWord(boolean backwards)
	{
		if (!selectingText)
		{
			selectingText = true;
			selectionPosition.set(cursorPosition);
			cursorPosition.jumpToNextSpace(backwards);
		}
		deleteSelectedText();
	}

	/**
	 * Gets the content width.
	 *
	 * @return the content width
	 */
	@Override
	public int getContentWidth()
	{
		return getWidth();
	}

	/**
	 * Gets the content height.
	 *
	 * @return the content height
	 */
	@Override
	public int getContentHeight()
	{
		return multiLine ? lines.size() * getLineHeight() + 4 : 12;
	}

	@Override
	public float getOffsetX()
	{
		return 0;
	}

	/**
	 * Sets the offset x.
	 *
	 * @param offsetX the offset x
	 * @param delta the delta
	 */
	@Override
	public void setOffsetX(float offsetX, int delta)
	{}

	@Override
	public float getOffsetY()
	{
		return (float) lineOffset / (lines.size() - visibleLines());
	}

	/**
	 * Sets the offset y.
	 *
	 * @param offsetY the offset y
	 * @param delta the delta
	 */
	@Override
	public void setOffsetY(float offsetY, int delta)
	{
		lineOffset = Math.round(offsetY / getScrollStep());
		lineOffset = Math.max(0, Math.min(lines.size(), lineOffset));
	}

	@Override
	public float getScrollStep()
	{
		float step = (float) 1 / (lines.size() - visibleLines());
		return (GuiScreen.isCtrlKeyDown() ? 5 * step : step);
	}

	/**
	 * Gets the vertical padding.
	 *
	 * @return the vertical padding
	 */
	@Override
	public int getVerticalPadding()
	{
		return 1;
	}

	/**
	 * Gets the horizontal padding.
	 *
	 * @return the horizontal padding
	 */
	@Override
	public int getHorizontalPadding()
	{
		return 1;
	}

	/**
	 * Gets the number of visible lines inside this {@link UITextField}.
	 *
	 * @return the int
	 */
	protected int visibleLines()
	{
		return multiLine ? (getHeight() - 4) / getLineHeight() : 1;
	}

	/**
	 * Called when a cursor is updated.<br>
	 * Offsets the content to make sure the cursor is still visible.
	 */
	public void onCursorUpdated()
	{
		if (!multiLine)
		{
			if (cursorPosition.character < charOffset)
				charOffset = cursorPosition.character;
			else if (text.length() != 0)
			{
				//charOffset = 0;
				while (GuiRenderer.getStringWidth(text.substring(charOffset, cursorPosition.textPosition), fontScale) >= getWidth() - 4)
					charOffset++;
			}
		}
		else
		{
			if (cursorPosition.line < lineOffset)
				setLineOffset(cursorPosition.line);
			else if (cursorPosition.line > lineOffset + visibleLines() - 1)
				setLineOffset(cursorPosition.line - visibleLines() + 1);
		}

		startTimer = System.currentTimeMillis();
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
		rp.colorMultiplier.set(bgColor);
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
		if (selectingText && selectionPosition.textPosition != cursorPosition.textPosition)
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
		renderer.setFontScale(fontScale);
		if (!multiLine)
		{
			int end = text.length();
			int w = GuiRenderer.getStringWidth(text.substring(charOffset, end), fontScale);
			while (w > getWidth())
				w = GuiRenderer.getStringWidth(text.substring(charOffset, end--), fontScale);
			renderer.drawText(text.substring(charOffset, end), 2, 2, isDisabled() ? 0xAAAAAA : textColor, textShadow);
		}
		else
		{
			for (int i = lineOffset; i < lineOffset + visibleLines() && i < lines.size(); i++)
			{
				int h = (i - lineOffset) * getLineHeight();
				renderer.drawText(lines.get(i), 2, h + 2, isDisabled() ? 0xAAAAAA : textColor, textShadow);
			}
		}
		renderer.setFontScale(1);
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

		if (cursorPosition.line < lineOffset || cursorPosition.line >= lineOffset + visibleLines())
			return;

		GL11.glDisable(GL11.GL_TEXTURE_2D);

		cursorShape.resetState();
		cursorShape.setSize(1, getLineHeight());
		cursorShape.setPosition(cursorPosition.getXOffset() + 1, cursorPosition.getYOffset() + 1);

		rp.useTexture.set(false);
		rp.colorMultiplier.set(cursorColor);

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

		CursorPosition first = cursorPosition.textPosition < selectionPosition.textPosition ? cursorPosition : selectionPosition;
		CursorPosition last = cursorPosition == first ? selectionPosition : cursorPosition;

		for (int i = first.line; i <= last.line; i++)
		{
			if (i >= lineOffset && i < lineOffset + visibleLines() && i < lines.size())
			{
				int x = 0;
				int y = (i - lineOffset) * getLineHeight();
				int X = GuiRenderer.getStringWidth(lines.get(i), fontScale);

				if (i == first.line)
					x = first.getXOffset();
				if (i == last.line)
					X = last.getXOffset();

				selectShape.resetState();
				selectShape.setSize(Math.min(getWidth() - 2, X) - x, getLineHeight());
				selectShape.setPosition(x + 2, y + 1);

				rp.useTexture.set(false);
				rp.colorMultiplier.set(selectColor);

				renderer.drawShape(selectShape, rp);
			}
		}

		renderer.next();

		GL11.glDisable(GL11.GL_COLOR_LOGIC_OP);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	@Override
	public boolean onButtonPress(int x, int y, MouseButton button)
	{
		if (button != MouseButton.LEFT)
			return super.onButtonRelease(x, y, button);
		x = relativeX(x);
		y = relativeY(y);
		if (GuiScreen.isShiftKeyDown())
		{
			if (!selectingText)
			{
				selectingText = true;
				selectionPosition.set(cursorPosition);
			}
		}
		else
			selectingText = false;

		cursorPosition.setPosition(x, y);

		return true;
	}

	@Override
	public boolean onButtonRelease(int x, int y, MouseButton button)
	{
		if (!autoSelectOnFocus || !selectAllOnRelease || button != MouseButton.LEFT)
			return super.onButtonRelease(x, y, button);

		selectingText = true;
		selectionPosition.jumpTo(0);
		cursorPosition.jumpTo(text.length());

		selectAllOnRelease = false;
		return true;
	}

	@Override
	public boolean onDrag(int lastX, int lastY, int x, int y, MouseButton button)
	{
		if (!isFocused() || button != MouseButton.LEFT)
			return super.onDrag(lastX, lastY, x, y, button);

		if (!selectingText)
		{
			selectingText = true;
			selectionPosition.set(cursorPosition);
		}

		x = relativeX(x);
		y = relativeY(y);
		cursorPosition.setPosition(x, y);

		selectAllOnRelease = false;
		return true;
	}

	@Override
	public boolean onDoubleClick(int x, int y, MouseButton button)
	{
		return super.onDoubleClick(x, y, button);
		//TODO:
		//		int pos = cursorPositionFromX(relativeX(event.getX()));
		//		if (pos > 0 && text.charAt(pos - 1) == ' ')
		//			selectionPosition = pos;
		//		else
		//			selectionPosition = pos + nextSpacePosition(true);
		//
		//		if (text.charAt(pos) == ' ')
		//			setCursorPosition(pos);
		//		else
		//			setCursorPosition(pos + nextSpacePosition(false) - 1);
	}

	/**
	 * Starts selecting text if Shift key is pressed.<br>
	 * Places selection cursor at the current cursor position.
	 */
	protected void startSelecting()
	{
		if (GuiScreen.isShiftKeyDown())
		{
			if (!selectingText)
				selectionPosition.set(cursorPosition);
			selectingText = true;
		}
		else
			selectingText = false;
	}

	@Override
	public boolean onKeyTyped(char keyChar, int keyCode)
	{
		if (!isFocused())
			return false;

		if (keyCode == Keyboard.KEY_ESCAPE)
			return true; //we don't want to close the GUI

		if (handleCtrlKeyDown(keyCode))
			return true;

		switch (keyCode)
		{
			case Keyboard.KEY_LEFT:
				startSelecting();
				cursorPosition.shiftLeft();
				break;
			case Keyboard.KEY_RIGHT:
				startSelecting();
				cursorPosition.shiftRight();
				break;
			case Keyboard.KEY_UP:
				if (multiLine)
				{
					startSelecting();
					cursorPosition.jumpLine(true);
				}
				break;
			case Keyboard.KEY_DOWN:
				if (multiLine)
				{
					startSelecting();
					cursorPosition.jumpLine(false);
				}
				break;
			case Keyboard.KEY_HOME:
				startSelecting();
				cursorPosition.jumpToLineStart();
				break;
			case Keyboard.KEY_END:
				startSelecting();
				cursorPosition.jumpToLineEnd();
				break;
			case Keyboard.KEY_BACK:
				if (editable)
					this.deleteFromCursor(-1);
				break;
			case Keyboard.KEY_DELETE:
				if (editable)
					this.deleteFromCursor(1);
				break;
			case Keyboard.KEY_RETURN:
				if (multiLine && editable)
					this.addText("\n");
				break;
			default:
				if (ChatAllowedCharacters.isAllowedCharacter(keyChar) && editable)
					this.addText(Character.toString(keyChar));
				break;
		}
		return true;
	}

	/**
	 * Handles the key typed while a control key is pressed.
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
				startSelecting();
				cursorPosition.jumpToNextSpace(true);
				return true;
			case Keyboard.KEY_RIGHT:
				startSelecting();
				cursorPosition.jumpToNextSpace(false);
				return true;
			case Keyboard.KEY_BACK:
				if (editable)
					this.deleteWord(true);
				return true;
			case Keyboard.KEY_DELETE:
				if (editable)
					this.deleteWord(false);
				return true;
			case Keyboard.KEY_HOME:
				startSelecting();
				cursorPosition.jumpToBeginning();
				return true;
			case Keyboard.KEY_END:
				startSelecting();
				cursorPosition.jumpToEnd();
				return true;
			case Keyboard.KEY_A:
				selectingText = true;
				selectionPosition.jumpToBeginning();
				cursorPosition.jumpToEnd();
				return true;
			case Keyboard.KEY_C:
				GuiScreen.setClipboardString(getSelectedText());
				return true;
			case Keyboard.KEY_V:
				if (editable)
					addText(GuiScreen.getClipboardString());
				return true;
			case Keyboard.KEY_X:
				GuiScreen.setClipboardString(getSelectedText());
				if (editable)
					addText("");
				return true;
			default:
				return false;
		}
	}

	/**
	 * Gets the property string.
	 *
	 * @return the property string
	 */
	@Override
	public String getPropertyString()
	{
		return text + " | " + super.getPropertyString();
	}

	/**
	 * The Class CursorPosition.
	 */
	protected class CursorPosition
	{
		/** The text position. */
		protected int textPosition;
		/** The character. */
		protected int character;
		/** The line. */
		protected int line;

		/**
		 * Sets this {@link CursorPosition} at the same position than <b>position</b>.
		 *
		 * @param position the new position
		 */
		public void set(CursorPosition position)
		{
			this.textPosition = position.textPosition;
			this.character = position.character;
			this.line = position.line;
		}

		/**
		 * Sets this {@link CursorPosition} at the specified position in the text.
		 *
		 * @param pos the new cursor position
		 */
		public void jumpTo(int pos)
		{
			if (text.length() == 0)
			{
				textPosition = 0;
				line = 0;
				character = 0;
				return;
			}

			if (pos < 0)
				pos = 0;
			if (pos >= text.length())
			{
				textPosition = text.length();
				line = lines.size() - 1;
				character = currentLineText().length();
				onCursorUpdated();
				return;
			}

			textPosition = pos;
			line = 0;
			if (!multiLine)
			{
				character = pos;
				onCursorUpdated();
				return;
			}

			while (line < lines.size() && pos >= currentLineText().length())
			{
				pos -= currentLineText().length();
				line++;
			}
			character = pos;

			onCursorUpdated();
		}

		/**
		 * Moves this {@link CursorPosition} by a specified amount.
		 *
		 * @param amount the amount
		 */
		public void jumpBy(int amount)
		{
			jumpTo(textPosition + amount);
		}

		/**
		 * Moves this {@link CursorPosition} to the beginning of the text.
		 */
		public void jumpToBeginning()
		{
			jumpTo(0);
		}

		/**
		 * Moves this {@link CursorPosition} to the end of the text.
		 */
		public void jumpToEnd()
		{
			jumpTo(text.length());
		}

		/**
		 * Moves this {@link CursorPosition} to the beginning of the current line.
		 */
		public void jumpToLineStart()
		{
			jumpBy(-character);
		}

		/**
		 * Moves this {@link CursorPosition} to the end of the current line.
		 */
		public void jumpToLineEnd()
		{
			textPosition -= character;
			character = currentLineText().length() - (currentLineText().endsWith("\n") ? 1 : 0);
			textPosition += character;
			onCursorUpdated();
		}

		/**
		 * Moves this {@link CursorPosition} one step to the left. If the cursor is at the beginning of the line, it is moved to the end of
		 * the previous line without changing the {@link #textPosition}.
		 */
		public void shiftLeft()
		{
			if (textPosition == 0)
				return;
			if (character == 0 && line > 0)
			{
				line--;
				character = currentLineText().length();
				if (currentLineText().endsWith("\n"))
				{
					character--;
					textPosition--;
				}
			}
			else
				jumpBy(-1);

			onCursorUpdated();
		}

		/**
		 * Moves this {@link CursorPosition} one step to the right. If the cursor is at the end of the line, it is moved to the start of the
		 * next line whithout changing the {@link #textPosition}.
		 */
		public void shiftRight()
		{
			if (textPosition == text.length())
				return;
			if (currentLineText().endsWith("\n"))
				jumpBy(1);
			else
			{
				if (character++ >= currentLineText().length() && line < lines.size() - 1)
				{
					line++;
					character = 0;
				}
				else
					textPosition++;
			}

			onCursorUpdated();
		}

		/**
		 * Moves this {@link CursorPosition} to the next space position.
		 *
		 * @param backwards backwards whether to look left of the cursor
		 */
		public void jumpToNextSpace(boolean backwards)
		{
			int pos = textPosition;
			int step = backwards ? -1 : 1;

			pos += step;
			while (pos > 0 && pos < text.length() && !Character.isWhitespace(text.charAt(pos)))
				pos += step;

			jumpTo(pos);
		}

		/**
		 * Moves this {@link CursorPosition} to the previous or next line.
		 *
		 * @param backwards if true, jump to the previous line, jump to the next otherwise
		 */
		public void jumpLine(boolean backwards)
		{
			if (backwards)
				line = Math.max(0, line - 1);
			else
				line = Math.min(line + 1, lines.size() - 1);
			character = Math.min(character, currentLineText().length());
			updateTextPosition();

		}

		/**
		 * Gets the text at the current line.
		 *
		 * @return the text
		 */
		private String currentLineText()
		{
			if (line < 0 || line >= lines.size())
				return "";

			return lines.get(line);
		}

		/**
		 * Sets this {@link CursorPosition} line and character position based on coordinates inside this {@link UITextField}.
		 *
		 * @param x the X coordinate
		 * @param y the Y coordinate
		 */
		public void setPosition(int x, int y)
		{
			line = lineFromY(y);
			character = characterFromX(x);
			updateTextPosition();
		}

		/**
		 * Update the text position based on {@link #character} and {@link #line}.
		 */
		private void updateTextPosition()
		{
			textPosition = character;
			if (!multiLine)
				return;

			for (int i = 0; i < line && i < lines.size(); i++)
				textPosition += lines.get(i).length();

			onCursorUpdated();
		}

		/**
		 * Determines the line for given Y coordinate.
		 *
		 * @param y the y coordinate
		 * @return the line number
		 */
		private int lineFromY(int y)
		{
			return multiLine ? Math.max(0, Math.min(y / getLineHeight() + lineOffset, lines.size() - 1)) : 0;
		}

		/**
		 * Determines the character for a given X coordinate.
		 *
		 * @param x the x coordinate
		 * @return position
		 */
		private int characterFromX(int x)
		{
			if (StringUtils.isEmpty(currentLineText()))
				return 0;

			int pos = 0;
			int width = 0;
			for (int i = charOffset; i < currentLineText().length(); i++)
			{
				float w = GuiRenderer.getCharWidth(currentLineText().charAt(i), fontScale);
				if (width + (w / 2) > x)
					return pos + charOffset;
				width += w;
				pos++;
			}
			return pos + charOffset;
		}

		/**
		 * Gets the x offset from the left of this {@link CursorPosition} inside this {@link UITextField}.
		 *
		 * @return the x offset
		 */
		public int getXOffset()
		{
			if (textPosition == text.length() && multiLine)
				return GuiRenderer.getStringWidth(currentLineText(), fontScale);
			if (currentLineText().length() == 0)
				return 0;
			if (charOffset >= character)
				return 0;
			return GuiRenderer.getStringWidth(currentLineText().substring(charOffset, character), fontScale);
		}

		/**
		 * Gets the x offset from the left of this {@link CursorPosition} inside this {@link UITextField}.
		 *
		 * @return the y offset
		 */
		public int getYOffset()
		{
			return (line - lineOffset) * getLineHeight();
		}

	}

}
