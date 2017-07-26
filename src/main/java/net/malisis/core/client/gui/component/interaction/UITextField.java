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

import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.IGuiText;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.control.IScrollable;
import net.malisis.core.client.gui.component.control.UIScrollBar.Type;
import net.malisis.core.client.gui.component.control.UISlimScrollbar;
import net.malisis.core.client.gui.element.GuiShape;
import net.malisis.core.client.gui.element.SimpleGuiShape;
import net.malisis.core.client.gui.element.XYResizableGuiShape;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.malisis.core.client.gui.event.component.ContentUpdateEvent;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.renderer.font.Link;
import net.malisis.core.renderer.font.MalisisFont;
import net.malisis.core.renderer.icon.GuiIcon;
import net.malisis.core.renderer.icon.provider.GuiIconProvider;
import net.malisis.core.util.MouseButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatAllowedCharacters;

/**
 * UITextField.
 *
 * @author Ordinastie
 */
public class UITextField extends UIComponent<UITextField> implements IScrollable, IGuiText<UITextField>
{
	/** The {@link MalisisFont} to use for this {@link UITextField}. */
	protected MalisisFont font = MalisisFont.minecraftFont;
	/** The {@link FontOptions} to use for this {@link UITextField}. */
	protected FontOptions fontOptions = FontOptions.builder().color(0xFFFFFF).shadow().disableTranslation().build();
	/** The {@link FontOptions} to use for this {@link UITextField} when disabled. */
	protected FontOptions disabledFontOptions = FontOptions.builder().disableTranslation().build();
	/** Current text of this {@link UITextField}. */
	protected StringBuilder text = new StringBuilder();
	/** Different lines if {@link #multiLine} is <code>true</code>. */
	protected List<String> lines = new LinkedList<>();
	/** Whether this {@link UITextField} handles multiline text. */
	protected boolean multiLine = false;
	/** Validator **/
	protected Predicate<String> validator = Predicates.alwaysTrue();

	//text space
	/** Number of character offset out of this {@link UITextField} when drawn. */
	protected int charOffset = 0;
	/** Number of line offset out of this {@link UITextField} when drawn. Always 0 if {@link #multiLine} is false */
	protected int lineOffset = 0;
	/** Space used between each line. */
	protected int lineSpacing = 1;

	//cursors
	/** Whether currently selecting text. */
	protected boolean selectingText = false;
	/** Current cursor position. */
	protected CursorPosition cursorPosition;
	/** Current selection cursor position. */
	protected CursorPosition selectionPosition;
	/** Cursor blink timer. */
	protected long startTimer;

	//interaction
	/** Whether this {@link UITextField} should select the text when release left mouse button. */
	private boolean selectAllOnRelease = false;
	/** Whether this {@link UITextField} should auto select the text when gaining focus. */
	protected boolean autoSelectOnFocus = false;
	/** Whether this {@link UITextField} is editable. */
	protected boolean editable = true;

	/** Scrollbar of the textfield *. */
	protected UISlimScrollbar scrollBar;

	//options
	/** Background color of this {@link UITextField}. */
	protected int bgColor = 0xFFFFFF;
	/** Cursor color for this {@link UITextField}. */
	protected int cursorColor = 0xD0D0D0;
	/** Selection color for this {@link UITextField}. */
	protected int selectColor = 0x0000FF;

	//drawing
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

		//default size to prevent single line INHERITED height
		if (!multiLine)
			setSize(100, 12);

		shape = new XYResizableGuiShape(1);
		cursorShape = new SimpleGuiShape();
		selectShape = new SimpleGuiShape();

		iconProvider = new GuiIconProvider(	gui.getGuiTexture().getXYResizableIcon(200, 30, 9, 12, 1),
											null,
											gui.getGuiTexture().getXYResizableIcon(200, 42, 9, 12, 1));

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

	@Override
	public void setParent(UIComponent<?> parent)
	{
		if (parent != null)
			register(parent);
		else
			unregister(this.parent);
		super.setParent(parent);

	}

	@Override
	public void onAddedToScreen()
	{
		buildLines();
	}

	// #region Getters/Setters
	@Override
	public MalisisFont getFont()
	{
		return font;
	}

	@Override
	public UITextField setFont(MalisisFont font)
	{
		this.font = font;
		buildLines();
		return this;
	}

	@Override
	public FontOptions getFontOptions()
	{
		return fontOptions;
	}

	@Override
	public UITextField setFontOptions(FontOptions options)
	{
		this.fontOptions = options.notTranslated();
		buildLines();
		return this;
	}

	/**
	 * Gets the {@link FontOptions} used when disabled.
	 *
	 * @return the disabled font renderer options
	 */
	public FontOptions getDisabledFontOptions()
	{
		return disabledFontOptions;
	}

	/**
	 * Sets the {@link FontOptions} to use when disabled.
	 *
	 * @param options the options
	 * @return this {@link UITextField}
	 */
	public UITextField setDisabledFontOptions(FontOptions options)
	{
		this.disabledFontOptions = options.notTranslated();
		return this;
	}

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

		buildLines();

		selectingText = false;
		charOffset = 0;
		lineOffset = 0;
		if (focused)
			cursorPosition.jumpToEnd();
		// fireEvent(new TextChanged(this));
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
	 * Gets the cursor color.
	 *
	 * @return the cursor color
	 */
	public int getCursorColor()
	{
		return cursorColor;
	}

	/**
	 * Sets the cursor color.
	 *
	 * @param cursorColor the cursor color
	 * @return the UI text field
	 */
	public UITextField setCursorColor(int cursorColor)
	{
		this.cursorColor = cursorColor;
		return this;
	}

	/**
	 * Gets the select color.
	 *
	 * @return the select color
	 */
	public int getSelectColor()
	{
		return selectColor;
	}

	/**
	 * Sets the select color.
	 *
	 * @param selectColor the select color
	 * @return the UI text field
	 */
	public UITextField setSelectColor(int selectColor)
	{
		this.selectColor = selectColor;
		return this;
	}

	/**
	 * Sets the options.
	 *
	 * @param bgColor the bg color
	 * @param cursorColor the cursor color
	 * @param selectColor the select color
	 * @return the UI text field
	 */
	public UITextField setOptions(int bgColor, int cursorColor, int selectColor)
	{
		this.bgColor = bgColor;
		this.cursorColor = cursorColor;
		this.selectColor = selectColor;

		return this;
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
		super.setSize(width, multiLine ? height : 12);
		buildLines();
		return this;
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
	 * Gets the line spacing used when drawing.
	 *
	 * @return the lineSpacing
	 */
	public int getLineSpacing()
	{
		return lineSpacing;
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
	 * Sets the line offset.
	 *
	 * @param line the new line offset
	 */
	public void setLineOffset(int line)
	{
		if (line < 0)
			line = 0;
		this.lineOffset = line;
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

	/**
	 * Checks if is editable.
	 *
	 * @return true, if is editable
	 */
	public boolean isEditable()
	{
		return editable;
	}

	/**
	 * Sets the editable.
	 *
	 * @param editable the editable
	 * @return the UI text field
	 */
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

	/**
	 * Gets the predicate used to validate input text.
	 *
	 * @return the predicate
	 */
	public Predicate<String> getValidator()
	{
		return validator;
	}

	/**
	 * Sets the predicate used to validate input text.
	 *
	 * @param validator the validator
	 * @return the UI text field
	 */
	public UITextField setValidator(Predicate<String> validator)
	{
		this.validator = validator;
		return this;
	}

	// #end Getters/Setters

	//#region IScrollable

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

	/**
	 * Gets the offset x.
	 *
	 * @return the offset x
	 */
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

	/**
	 * Gets the offset y.
	 *
	 * @return the offset y
	 */
	@Override
	public float getOffsetY()
	{
		if (lines.size() < getVisibleLines())
			return 0;
		return (float) lineOffset / (lines.size() - getVisibleLines());
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

	/**
	 * Gets the scroll step.
	 *
	 * @return the scroll step
	 */
	@Override
	public float getScrollStep()
	{
		float step = (float) 1 / (lines.size() - getVisibleLines());
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

	//#end IScrollable

	/**
	 * Gets the number of visible lines inside this {@link UITextField}.
	 *
	 * @return the int
	 */
	public int getVisibleLines()
	{
		return multiLine ? (getHeight() - 4) / getLineHeight() : 1;
	}

	/**
	 * Gets the line height of this {@link UITextField}.
	 *
	 * @return the line height
	 */
	public int getLineHeight()
	{
		return (int) (font.getStringHeight(fontOptions) + lineSpacing);
	}

	//#end IBBSRenderer

	/**
	 * Builds the lines for this {@link UITextField}.
	 */
	public void buildLines()
	{
		lines.clear();
		if (!StringUtils.isEmpty(text))
		{
			if (!multiLine)
				lines.add(text.toString());
			else
			{
				lines = font.wrapText(text.toString(), getWidth() - 4, fontOptions);

				if (text.charAt(text.length() - 1) == '\n')
					lines.add("");
			}
		}

		fireEvent(new ContentUpdateEvent<>(this));
	}

	/**
	 * Checks against if text is valid.
	 *
	 * @param text the text
	 * @return true, if input is valid
	 */
	protected boolean validateText(String text)
	{
		return validator.apply(text);
	}

	/**
	 * Adds text at current cursor position. If some text is selected, it's deleted first.
	 *
	 * @param str the text
	 */
	public void addText(String str)
	{
		if (selectingText)
			deleteSelectedText();

		int position = cursorPosition.textPosition;

		String oldValue = this.text.toString();
		String newValue = new StringBuilder(oldValue).insert(position, str).toString();

		if (!validateText(newValue))
			return;

		if (!fireEvent(new ComponentEvent.ValueChange<>(this, oldValue, newValue)))
			return;

		text.insert(position, str);
		buildLines();
		cursorPosition.jumpBy(str.length());
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
		String newValue = new StringBuilder(oldValue).delete(start, end).toString();

		if (!fireEvent(new ComponentEvent.ValueChange<>(this, oldValue, newValue)))
			return;

		text.delete(start, end);
		buildLines();
		selectingText = false;
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
	 * Select word.
	 */
	public void selectWord()
	{
		selectWord(cursorPosition.textPosition);
	}

	/**
	 * Select word.
	 *
	 * @param position the position
	 */
	public void selectWord(int position)
	{
		if (text.length() == 0)
			return;

		selectingText = true;

		selectionPosition.jumpTo(position);
		selectionPosition.jumpToNextSpace(true);
		if (Character.isWhitespace(text.charAt(selectionPosition.textPosition)))
			selectionPosition.shiftRight();

		cursorPosition.jumpTo(position);
		cursorPosition.jumpToNextSpace(false);
	}

	/**
	 * Called when a cursor is updated.<br>
	 * Offsets the content to make sure the cursor is still visible.
	 */
	protected void onCursorUpdated()
	{
		if (!multiLine)
		{
			if (cursorPosition.character < charOffset)
				charOffset = cursorPosition.character;
			else if (text.length() != 0)
			{
				//charOffset = 0;
				while (font.getStringWidth(text.substring(charOffset, cursorPosition.textPosition), fontOptions) >= getWidth() - 4)
					charOffset++;
			}
		}
		else
		{
			if (cursorPosition.line < lineOffset)
				setLineOffset(cursorPosition.line);
			else if (cursorPosition.line > lineOffset + getVisibleLines() - 1)
				setLineOffset(cursorPosition.line - getVisibleLines() + 1);
		}

		startTimer = System.currentTimeMillis();
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

	//#region Input

	/**
	 * On button press.
	 *
	 * @param x the x
	 * @param y the y
	 * @param button the button
	 * @return true, if successful
	 */
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

	/**
	 * On button release.
	 *
	 * @param x the x
	 * @param y the y
	 * @param button the button
	 * @return true, if successful
	 */
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

	/**
	 * On drag.
	 *
	 * @param lastX the last x
	 * @param lastY the last y
	 * @param x the x
	 * @param y the y
	 * @param button the button
	 * @return true, if successful
	 */
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

	/**
	 * On double click.
	 *
	 * @param x the x
	 * @param y the y
	 * @param button the button
	 * @return true, if successful
	 */
	@Override
	public boolean onDoubleClick(int x, int y, MouseButton button)
	{
		if (button != MouseButton.LEFT)
			return super.onDoubleClick(x, y, button);

		Link link = cursorPosition.getLink();
		if (link != null)
		{
			link.click();
			return true;
		}

		selectWord();
		return true;
	}

	/**
	 * On key typed.
	 *
	 * @param keyChar the key char
	 * @param keyCode the key code
	 * @return true, if successful
	 */
	@Override
	public boolean onKeyTyped(char keyChar, int keyCode)
	{
		if (!isFocused())
			return false;

		if (keyCode == Keyboard.KEY_ESCAPE)
		{
			setFocused(false);
			return true; //we don't want to close the GUI
		}

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
				if (isEditable())
					this.deleteFromCursor(-1);
				break;
			case Keyboard.KEY_DELETE:
				if (isEditable())
					this.deleteFromCursor(1);
				break;
			case Keyboard.KEY_RETURN:
				if (multiLine && isEditable())
					this.addText("\n");
				break;
			case Keyboard.KEY_TAB:
				if (isEditable())
					addText("\t");
				break;
			default:
				if ((ChatAllowedCharacters.isAllowedCharacter(keyChar) || keyChar == '\u00a7') && isEditable())
				{
					this.addText(Character.toString(keyChar));
					break;
				}
				else
					return super.onKeyTyped(keyChar, keyCode);

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
				if (isEditable())
					this.deleteWord(true);
				return true;
			case Keyboard.KEY_DELETE:
				if (isEditable())
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
				if (isEditable())
					addText(GuiScreen.getClipboardString());
				return true;
			case Keyboard.KEY_X:
				GuiScreen.setClipboardString(getSelectedText());
				if (isEditable())
					addText("");
				return true;
			default:
				return false;
		}
	}

	//#end Input

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
		FontOptions options = isDisabled() ? disabledFontOptions : this.fontOptions;
		if (!multiLine)
		{
			String t = font.clipString(text.substring(charOffset, text.length()), getWidth() - 4, options);
			renderer.drawText(font, t, 2, 2, 0, options);
		}
		else
		{
			options.resetStyles();
			for (int i = lineOffset; i < lineOffset + getVisibleLines() && i < lines.size(); i++)
			{
				options.setLineOptions(options);
				int h = (i - lineOffset) * getLineHeight();
				renderer.drawText(font, lines.get(i), 2, h + 2, 0, options);

			}
		}
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

		if (cursorPosition.line < lineOffset || cursorPosition.line >= lineOffset + getVisibleLines())
			return;

		renderer.drawRectangle(cursorPosition.getXOffset()
				+ 1, cursorPosition.getYOffset() + 1, getZIndex(), 1, getLineHeight(), cursorColor, 255, true);
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
			if (i >= lineOffset && i < lineOffset + getVisibleLines() && i < lines.size())
			{
				int x = 0;
				int y = (i - lineOffset) * getLineHeight();
				int X = (int) font.getStringWidth(lines.get(i), fontOptions);

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

	//#region CursorPosition
	/**
	 * This class determines a position inside the text divides in lines.
	 */
	public class CursorPosition
	{
		/** The text position. */
		protected int textPosition;
		/** The character. */
		protected int character;
		/** The line. */
		protected int line;
		/** Last x offset **/
		protected int lastOffset;

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
		 * Gets the position of this {@link CursorPosition} in the text.
		 *
		 * @return the position
		 */
		public int getPosition()
		{
			return textPosition;
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
				onCursorUpdated();
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
			lastOffset = getXOffset();
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

			if (FontOptions.isFormatting(text.toString(), textPosition - 2))
				jumpBy(-2);
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

			if (FontOptions.isFormatting(text.toString(), textPosition))
				jumpBy(2);
			else
				jumpBy(1);

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
			while (pos > 0 && pos < text.length() && Character.isLetterOrDigit(text.charAt(pos)))
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
			if ((backwards && line == 0) || (!backwards && line == lines.size() - 1))
				return;

			if (backwards)
				line = Math.max(0, line - 1);
			else
				line = Math.min(line + 1, lines.size() - 1);
			//character = Math.min(character, currentLineText().length());
			character = Math.round(font.getCharPosition(currentLineText(), fontOptions, lastOffset, charOffset));
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
			lastOffset = getXOffset();
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
			return (int) font.getCharPosition(currentLineText(), fontOptions, x, charOffset);
		}

		/**
		 * Gets the x offset from the left of this {@link CursorPosition} inside this {@link UITextField}.
		 *
		 * @return the x offset
		 */
		public int getXOffset()
		{
			if (textPosition == text.length() && multiLine)
				return (int) font.getStringWidth(currentLineText(), fontOptions);

			if (currentLineText().length() == 0)
				return 0;
			if (charOffset >= character || charOffset >= currentLineText().length())
				return 0;

			return (int) font.getStringWidth(currentLineText(), fontOptions, charOffset, character);
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

		public Link getLink()
		{
			return Link.getLink(currentLineText(), character);
		}

		@Override
		public String toString()
		{
			return "Pos : " + textPosition + " (l" + line + " / c" + character + ") at " + getXOffset() + "," + getYOffset();
		}

	}

	//#end CursorPosition

}
