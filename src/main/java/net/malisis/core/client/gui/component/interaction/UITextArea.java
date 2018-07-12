/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 Ordinastie
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

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.google.common.eventbus.Subscribe;

import net.malisis.core.client.gui.component.control.IScrollable;
import net.malisis.core.client.gui.component.scrolling.UIScrollBar;
import net.malisis.core.client.gui.component.scrolling.UISlimScrollbar;
import net.malisis.core.client.gui.event.component.SpaceChangeEvent.SizeChangeEvent;
import net.malisis.core.client.gui.render.GuiRenderer;
import net.malisis.core.client.gui.render.shape.GuiShape;
import net.malisis.core.client.gui.render.shape.GuiShape.Builder;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatAllowedCharacters;

/**
 * @author Ordinastie
 *
 */

public class UITextArea extends UITextField implements IScrollable
{
	private final UIScrollBar scrollbar;

	/**
	 * Instantiates a new {@link UITextField}.
	 */
	public UITextArea()
	{
		super(true);
		offset = UIScrollBar.scrollingOffset(this);
		scrollbar = new UISlimScrollbar(this, UIScrollBar.Type.VERTICAL);
		guiText.setWrapSize(() -> size().width() - padding().horizontal() - UIScrollBar.scrollbarWidth(this) - 4);
	}

	/**
	 * Instantiates a new {@link UITextArea}.
	 *
	 * @param text the text
	 */
	public UITextArea(String text)
	{
		this();
		setText(text);
	}

	/**
	 * Called when a cursor is updated.<br>
	 * Offsets the content to make sure the cursor is still visible.
	 */
	@Override
	protected void onCursorUpdated()
	{
		if (getParent() == null)
			return;

		startTimer = System.currentTimeMillis();

		Integer yOffset = null;
		if (text.length() == 0)
			yOffset = 0;
		else if (cursor.y < -offset().y())
			yOffset = -cursor.y + 2;
		else if (cursor.y + cursor.height > innerSize().height() - offset().y())
			yOffset = Math.min(innerSize().height() - cursor.y - cursor.height - 2, 0);
		if (yOffset != null)
			scrollbar.setOffset(yOffset);
	}

	@Override
	public boolean onKeyTyped(char keyChar, int keyCode)
	{
		if (super.onKeyTyped(keyChar, keyCode))
			return true;

		switch (keyCode)
		{
			case Keyboard.KEY_UP:
				startSelecting();
				cursor.jumpLine(true);
				break;
			case Keyboard.KEY_DOWN:
				startSelecting();
				cursor.jumpLine(false);
				break;
			case Keyboard.KEY_RETURN:
				if (guiText.isMultiLine() && isEditable())
					this.addText("\n");
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
	@Override
	protected boolean handleCtrlKeyDown(int keyCode)
	{
		if (!GuiScreen.isCtrlKeyDown())
			return false;

		if (super.handleCtrlKeyDown(keyCode))
			return true;

		switch (keyCode)
		{
			case Keyboard.KEY_HOME:
				startSelecting();
				cursor.jumpToStart();
				return true;
			case Keyboard.KEY_END:
				startSelecting();
				cursor.jumpToEnd();
				return true;
			default:
				return false;
		}
	}

	//#end Input
	/**
	 * Draw the selection box of this {@link UITextField}.
	 *
	 * @param renderer the renderer
	 */
	@Override
	public void drawSelectionBox(GuiRenderer renderer)
	{
		if (!selectingText || selectionCursor.index == cursor.index)
			return;

		if (selectionCursor.lineIndex == cursor.lineIndex)
		{
			super.drawSelectionBox(renderer);
			return;
		}

		renderer.next();
		GL11.glEnable(GL11.GL_COLOR_LOGIC_OP);
		GL11.glLogicOp(GL11.GL_OR_REVERSE);

		Cursor first = cursor.index < selectionCursor.index ? cursor : selectionCursor;
		Cursor last = cursor == first ? selectionCursor : cursor;

		Builder builder = GuiShape	.builder(this)
									.position(first)
									.fixed(false)
									.size(innerSize().width() - first.x(), cursor.height())
									.color(selectColor);
		//drawFirst line :
		GuiShape s = builder.build();
		s.render(renderer);

		//draw intermediate selection
		s = builder	.position(padding().left(), first.y() + first.height())
					.size(innerSize().width(), last.y() - first.y() - first.height())
					.build();
		s.render(renderer);

		//drawLastLine :
		s = builder.position(padding().left(), last.y()).size(last.x(), last.height).build();
		s.render(renderer);

		renderer.next();

		GL11.glDisable(GL11.GL_COLOR_LOGIC_OP);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	@Override
	@Subscribe
	public void onResize(SizeChangeEvent<UITextField> event)
	{
		onCursorUpdated();
	}
}
