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

import java.util.function.Function;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.google.common.eventbus.Subscribe;

import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.content.IContentHolder;
import net.malisis.core.client.gui.element.IClipable;
import net.malisis.core.client.gui.element.IOffset;
import net.malisis.core.client.gui.element.Padding;
import net.malisis.core.client.gui.element.Padding.IPadded;
import net.malisis.core.client.gui.element.Size;
import net.malisis.core.client.gui.element.Size.ISize;
import net.malisis.core.client.gui.element.position.Position;
import net.malisis.core.client.gui.element.position.Position.IPosition;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.malisis.core.client.gui.event.component.SpaceChangeEvent.SizeChangeEvent;
import net.malisis.core.client.gui.render.GuiIcon;
import net.malisis.core.client.gui.render.GuiRenderer;
import net.malisis.core.client.gui.render.shape.GuiShape;
import net.malisis.core.client.gui.text.GuiText;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.renderer.font.StringWalker;
import net.malisis.core.util.MouseButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;

/**
 * UITextField.
 *
 * @author Ordinastie
 */
public class UITextField extends UIComponent implements IContentHolder, IClipable, IOffset, IPadded
{
	protected final GuiText guiText;
	/** The {@link FontOptions} to use for this {@link UITextField} when disabled. */
	/** Current text of this {@link UITextField}. */
	protected StringBuilder text = new StringBuilder();
	protected Function<String, String> filterFunction;

	/** The padding of this {@link UITextField}. */
	protected Padding padding = Padding.of(1);

	private int xOffset = 0;
	protected IPosition offset = Position.of(this).x(this::xOffset).y(0).build();

	//cursors
	/** Whether currently selecting text. */
	protected boolean selectingText = false;
	/** Current cursor position. */
	protected Cursor cursor = new Cursor();
	/** Current selection cursor position. */
	protected Cursor selectionCursor = new Cursor();
	/** Cursor blink timer. */
	protected long startTimer;

	//interaction
	/** Whether this {@link UITextField} should select the text when release left mouse button. */
	protected boolean selectAllOnRelease = false;
	/** Whether this {@link UITextField} should auto select the text when gaining focus. */
	protected boolean autoSelectOnFocus = false;
	/** Whether this {@link UITextField} is editable. */
	protected boolean editable = true;

	//options
	/** Cursor color for this {@link UITextField}. */
	protected int cursorColor = 0xD0D0D0;
	/** Selection color for this {@link UITextField}. */
	protected int selectColor = 0x0000FF;

	//drawing
	/** Shape used to draw the cursor of this {@link UITextField}. */
	protected GuiShape cursorShape = GuiShape	.builder(this)
												.position()
												.set(cursor)
												.back()
												.fixed(false)
												.size(Size.of(1, cursor::height))
												.color(this::getCursorColor)
												.icon(GuiIcon.NONE)
												.build();

	protected UITextField(boolean multiLine)
	{
		this.guiText = GuiText	.builder()
								.parent(this)
								.text(this::getText)
								.multiLine(multiLine)
								.translated(false)
								.literal(true)
								.position()
								.x(3)
								.y(3)
								.back()
								.fontOptions(FontOptions.builder().color(0xFFFFFF).shadow().build())
								.build();
		setSize(Size.of(100, 14));

		GuiShape background = GuiShape	.builder(this)
										.icon(GuiIcon.forComponent(this, GuiIcon.TEXTFIELD, null, GuiIcon.TEXTFIELD_DISABLED))
										.border(1)
										.build();
		setBackground(background);
		setForeground(guiText.and(this::drawCursor).and(this::drawSelectionBox));
	}

	/**
	 * Instantiates a new {@link UITextField}.
	 */
	public UITextField()
	{
		this(false);
	}

	public UITextField(String text)
	{
		this();
		setText(text);
	}

	@Override
	public void setParent(UIComponent parent)
	{
		if (parent != null)
			register(parent); //for size change
		else
			unregister(this.parent);
		super.setParent(parent);

	}

	@Override
	public void onAddedToScreen(MalisisGui gui)
	{
		this.gui = gui;
	}

	// #region Getters/Setters
	/**
	 * Sets the text of this {@link UITextField} and place the cursor at the end.
	 *
	 * @param text the new text
	 */
	public void setText(String text)
	{
		if (this.filterFunction != null)
			text = this.filterFunction.apply(text);

		if (text == null)
			text = "";
		this.text.setLength(0);
		this.text.append(text);
		guiText.setText(text);

		selectingText = false;
		xOffset = 0;
		if (focused)
			cursor.jumpToEnd();
		// fireEvent(new TextChanged(this));
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

	@Override
	public GuiText content()
	{
		return guiText;
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

		int start = Math.min(selectionCursor.index, cursor.index);
		int end = Math.max(selectionCursor.index, cursor.index);

		return this.text.substring(start, end);
	}

	@Override
	public Padding padding()
	{
		return padding;
	}

	private int xOffset()
	{
		return xOffset;
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
	public UITextField setColors(int bgColor, int cursorColor, int selectColor)
	{
		setColor(bgColor);
		this.cursorColor = cursorColor;
		this.selectColor = selectColor;

		return this;
	}

	/**
	 * Sets the size of this {@link UITextField}.<br>
	 *
	 * @param size the new size
	 */
	@Override
	public void setSize(ISize size)
	{
		super.setSize(size);
	}

	/**
	 * Sets the focused.
	 *
	 * @param focused the new focused
	 */
	@Override
	public void setFocused(boolean focused)
	{
		if (!isEnabled() || !isVisible())
			return;

		if (!this.focused)
			selectAllOnRelease = true;

		super.setFocused(focused);
	}

	/**
	 * Gets the current cursor position.
	 *
	 * @return the position of the cursor.
	 */
	public Cursor getCursorPosition()
	{
		return cursor;
	}

	/**
	 * Gets the selection position.
	 *
	 * @return the selection position
	 */
	public Cursor getSelectionPosition()
	{
		return selectionCursor;
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
	 * Sets the function that applies to all incoming text. Immediately applies filter to current text.
	 *
	 * @param filterFunction the function
	 */
	public void setFilter(Function<String, String> filterFunction)
	{
		this.filterFunction = filterFunction;
		this.text = new StringBuilder(this.filterFunction.apply(this.text.toString()));
	}

	/**
	 * Gets the function applied to all incoming text
	 *
	 * @return the filter function
	 */
	public Function<String, String> getFilter()
	{
		return this.filterFunction;
	}

	// #end Getters/Setters
	@Override
	public IPosition offset()
	{
		return offset;
	}

	@Override
	public ClipArea getClipArea()
	{
		return ClipArea.from(this);
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

		final StringBuilder oldText = this.text;
		final String oldValue = this.text.toString();
		String newValue = oldText.insert(this.cursor.index, str).toString();

		if (this.filterFunction != null)
			newValue = this.filterFunction.apply(newValue);

		if (!fireEvent(new ComponentEvent.ValueChange<>(this, oldValue, newValue)))
			return;

		this.text = new StringBuilder(newValue);
		guiText.setText(newValue);

		cursor.jumpBy(str.length());
	}

	/**
	 * Deletes the text currently selected.
	 */
	public void deleteSelectedText()
	{
		if (!selectingText)
			return;

		int start = Math.min(selectionCursor.index, cursor.index);
		int end = Math.max(selectionCursor.index, cursor.index);

		String oldValue = this.text.toString();
		String newValue = new StringBuilder(oldValue).delete(start, end).toString();

		if (!fireEvent(new ComponentEvent.ValueChange<>(this, oldValue, newValue)))
			return;

		this.text = new StringBuilder(newValue);
		guiText.setText(newValue);
		selectingText = false;
		cursor.jumpTo(start);
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
			selectionCursor.from(cursor);
			selectionCursor.jumpBy(amount);
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
			selectionCursor.from(cursor);
			cursor.jumpToNextSpace(backwards);
		}
		deleteSelectedText();
	}

	/**
	 * Select word.
	 */
	public void selectWord()
	{
		selectWord(cursor.index);
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

		selectionCursor.jumpTo(position);
		selectionCursor.jumpToNextSpace(true);
		if (Character.isWhitespace(text.charAt(selectionCursor.index)))
			selectionCursor.shiftRight();

		cursor.jumpTo(position);
		cursor.jumpToNextSpace(false);
	}

	/**
	 * Called when a cursor is updated.<br>
	 * Offsets the content to make sure the cursor is still visible.
	 */
	protected void onCursorUpdated()
	{
		if (getParent() == null)
			return;

		startTimer = System.currentTimeMillis();
		//- 4 because Padding.of(2) for clipping
		if (text.length() == 0)
			xOffset = 0;
		else if (cursor.x <= -xOffset)
			xOffset = -cursor.x;
		else if (cursor.x >= innerSize().width() - xOffset)
			xOffset = Math.min(innerSize().width() - cursor.x - 4, 0);
		else if (guiText.size().width() <= innerSize().width() - xOffset - 5)
			xOffset = Math.min(innerSize().width() - guiText.size().width() - 4, 0);
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
				selectionCursor.from(cursor);
			selectingText = true;
		}
		else
			selectingText = false;
	}

	//#region Input

	@Override
	public boolean onButtonPress(MouseButton button)
	{
		if (button != MouseButton.LEFT)
			return super.onButtonRelease(button);
		if (GuiScreen.isShiftKeyDown())
		{
			if (!selectingText)
			{
				selectingText = true;
				selectionCursor.from(cursor);
			}
		}
		else
			selectingText = false;

		cursor.fromMouse();

		return true;
	}

	@Override
	public boolean onButtonRelease(MouseButton button)
	{
		if (!autoSelectOnFocus || !selectAllOnRelease || button != MouseButton.LEFT)
			return super.onButtonRelease(button);

		selectingText = true;
		selectionCursor.jumpTo(0);
		cursor.jumpTo(text.length());

		selectAllOnRelease = false;
		return true;
	}

	@Override
	public boolean onDrag(MouseButton button)
	{
		if (!isFocused() || button != MouseButton.LEFT)
			return super.onDrag(button);

		if (!selectingText)
		{
			selectingText = true;
			selectionCursor.from(cursor);
		}

		cursor.fromMouse();

		selectAllOnRelease = false;
		return true;
	}

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
				cursor.shiftLeft();
				return true;
			case Keyboard.KEY_RIGHT:
				startSelecting();
				cursor.shiftRight();
				return true;
			case Keyboard.KEY_HOME:
				startSelecting();
				cursor.jumpToLineStart();
				return true;
			case Keyboard.KEY_END:
				startSelecting();
				cursor.jumpToLineEnd();
				return true;
			case Keyboard.KEY_BACK:
				if (isEditable())
					this.deleteFromCursor(-1);
				return true;
			case Keyboard.KEY_DELETE:
				if (isEditable())
					this.deleteFromCursor(1);
				return true;
			case Keyboard.KEY_TAB:
				if (isEditable())
					addText("\t");
				return true;
			default:
				if ((ChatAllowedCharacters.isAllowedCharacter(keyChar) || keyChar == '\u00a7') && isEditable())
				{
					this.addText(Character.toString(keyChar));
					return true;
				}
				else
					return super.onKeyTyped(keyChar, keyCode);
		}
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
				cursor.jumpToNextSpace(true);
				return true;
			case Keyboard.KEY_RIGHT:
				startSelecting();
				cursor.jumpToNextSpace(false);
				return true;
			case Keyboard.KEY_BACK:
				if (isEditable())
					this.deleteWord(true);
				return true;
			case Keyboard.KEY_END:
				startSelecting();
				cursor.jumpToEnd();
				return true;
			case Keyboard.KEY_A:
				selectingText = true;
				selectionCursor.jumpToStart();
				cursor.jumpToEnd();
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

	/**
	 * Draws the cursor for this {@link UITextField}.
	 *
	 * @param renderer the renderer
	 */
	//#end Input
	public void drawCursor(GuiRenderer renderer)
	{
		if (!isFocused())
			return;

		long elaspedTime = startTimer - System.currentTimeMillis();
		if ((elaspedTime / 500) % 2 != 0)
			return;
		cursorShape.render(renderer);
	}

	/**
	 * Draws the selection box of this {@link UITextField}.
	 *
	 * @param renderer the renderer
	 */
	public void drawSelectionBox(GuiRenderer renderer)
	{
		if (!selectingText || selectionCursor.index == cursor.index)
			return;

		renderer.next();
		GL11.glEnable(GL11.GL_COLOR_LOGIC_OP);
		GL11.glLogicOp(GL11.GL_OR_REVERSE);

		Cursor first = cursor.index < selectionCursor.index ? cursor : selectionCursor;
		Cursor last = cursor == first ? selectionCursor : cursor;

		GuiShape s = GuiShape	.builder(this)
								.position()
								.set(first)
								.back()
								.fixed(false)
								.size(last.x() - first.x(), cursor.height())
								.color(selectColor)
								.build();
		s.render(renderer);

		renderer.next();

		GL11.glDisable(GL11.GL_COLOR_LOGIC_OP);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	@Subscribe
	public void onResize(SizeChangeEvent<UITextField> event)
	{
		onCursorUpdated();
	}

	/**
	 * Gets the property string.
	 *
	 * @return the property string
	 */
	@Override
	public String getPropertyString()
	{
		return "[" + TextFormatting.DARK_AQUA + guiText + TextFormatting.RESET + "] " + TextFormatting.DARK_PURPLE + cursor
				+ TextFormatting.RESET + " | " + super.getPropertyString();
	}

	//#region CursorPosition

	/**
	 * This class determines a position inside the text.
	 */
	public class Cursor implements IPosition
	{
		/** The text position. */
		protected int index;
		/** The char position in line. */
		protected int charIndex;
		/** The line number. */
		protected int lineIndex;

		/** The last x position. */
		protected int lastX; //used when going up and down lines, if the new line is shorter than the previous one, we want to keep the correct offset for the other ones.
		/** The x position in the text. */
		protected int x;
		/** The y position in the text. */
		protected int y;
		/** The height of the line. */
		protected int height = 9;

		/**
		 * Gets the x offset from the left of this {@link Cursor} inside this {@link UITextField}.
		 *
		 * @return the x offset
		 */
		@Override
		public int x()
		{
			return x + 2;
		}

		/**
		 * Gets the x offset from the left of this {@link Cursor} inside this {@link UITextField}.
		 *
		 * @return the y offset
		 */
		@Override
		public int y()
		{
			return y + 2;
		}

		/**
		 * Get the height of the cursor, should match the height of the current line.
		 *
		 * @return the int
		 */
		public int height()
		{
			return height;
		}

		private StringWalker back(StringWalker walker)
		{
			//			if (walker.getChar() == '\n')
			//			{
			//				int i = walker.globalIndex();
			//				walker = guiText.walker();
			//				walker.walkToIndex(i - 1);
			//			}
			return walker;
		}

		/**
		 * Sets this {@link Cursor} data based on walker current state.
		 *
		 * @param walker the walker
		 */
		private void set(StringWalker walker)
		{
			index = MathHelper.clamp(walker.globalIndex() + 1, 0, text.length()); //== globalIndex
			charIndex = walker.charIndex();
			lineIndex = walker.lineIndex();
			x = (int) walker.lineWidth();
			y = (int) walker.y(); //== 0
			height = (int) Math.ceil(walker.lineHeight());
			if (height < 9)
				height = 9;

			//if cursor after a \n, virtually set its position at the beginning of the next line.
			if (guiText.isMultiLine() && walker.getChar() == '\n')
			{
				x = 0;
				y += height;
			}

			onCursorUpdated();
		}

		public void from(Cursor cursor)
		{
			index = cursor.index;
			charIndex = cursor.charIndex;
			lineIndex = cursor.lineIndex;
			height = cursor.height;
			x = cursor.x;
			y = cursor.y;
		}

		/**
		 * Updates this cursor based on mouse position.
		 */
		public void fromMouse()
		{
			StringWalker walker = guiText.walker();
			if (!walker.walkToY(mousePosition().y()))
			{
				jumpToEnd();
				lastX = x;
				return;
			}

			int x = mousePosition().x();
			walker.walkToX(x);
			walker = back(walker);
			int i = walker.globalIndex();
			if (x > walker.x() + walker.width() / 2)
				i++;
			jumpTo(i);
			lastX = x;
		}

		/**
		 * Sets this {@link Cursor} at the specified position in the text.
		 *
		 * @param index the new cursor position
		 */
		public void jumpTo(int index)
		{
			index = MathHelper.clamp(index, 0, text.length());
			StringWalker walker = guiText.walker();
			walker.walkToIndex(index - 1);
			set(walker);
			lastX = x;
			onCursorUpdated();
		}

		/**
		 * Moves this {@link Cursor} by a specified amount.
		 *
		 * @param amount the amount
		 */
		public void jumpBy(int amount)
		{
			jumpTo(index + amount);
		}

		/**
		 * Moves this {@link Cursor} to the beginning of the text.
		 */
		public void jumpToStart()
		{
			jumpTo(0);
		}

		/**
		 * Moves this {@link Cursor} to the end of the text.
		 */
		public void jumpToEnd()
		{
			jumpTo(text.length());
		}

		public void jumpToLineStart()
		{
			jumpTo(index - charIndex - 1);
		}

		public void jumpToLineEnd()
		{
			StringWalker walker = guiText.walker();
			walker.walkToIndex(index);
			walker.walkToEOL();
			walker = back(walker);
			set(walker);
		}

		/**
		 * Moves this {@link Cursor} one step to the left. If the cursor is at the beginning of the line, it is moved to the end of the
		 * previous line without changing the {@link #index}.
		 */
		public void shiftLeft()
		{
			//if (index == 0)
			//	return;

			if (FontOptions.isFormatting(text.toString(), index - 2))
				jumpBy(-2);
			else
				jumpBy(-1);
		}

		/**
		 * Moves this {@link Cursor} one step to the right. If the cursor is at the end of the line, it is moved to the start of the next
		 * line whithout changing the {@link #index}.
		 */
		public void shiftRight()
		{
			if (index == text.length())
				return;

			if (FontOptions.isFormatting(text.toString(), index))
				jumpBy(2);
			else
				jumpBy(1);
		}

		public void jumpLine(boolean backwards)
		{
			if (lineIndex == 0 && backwards)
				return;
			if (lineIndex == guiText.lineCount() - 1 && !backwards)
				return;

			StringWalker walker = guiText.walker();
			walker.walkUntil(w -> w.lineIndex() == lineIndex + (backwards ? -1 : 1));
			walker.walkToX(lastX);
			//if (guiText.lines().get(walker.lineIndex()).text().length() == 1)
			//	walker = back(walker);
			set(walker);
		}

		/**
		 * Moves this {@link Cursor} to the next space position.
		 *
		 * @param backwards backwards whether to look left of the cursor
		 */
		public void jumpToNextSpace(boolean backwards)
		{
			int pos = index;
			int step = backwards ? -1 : 1;

			pos += step;
			while (pos > 0 && pos < text.length() && Character.isLetterOrDigit(text.charAt(pos)))
				pos += step;

			jumpTo(pos);
		}

		@Override
		public String toString()
		{
			return "Index : " + index + " (L" + lineIndex + " C" + charIndex + ") at " + x() + "," + y() + " (offset: " + offset + ")";
		}
	}
	//#end CursorPosition
}
