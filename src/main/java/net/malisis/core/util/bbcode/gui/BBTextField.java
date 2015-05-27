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

package net.malisis.core.util.bbcode.gui;

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.malisis.core.util.bbcode.BBString;
import net.malisis.core.util.bbcode.render.IBBCodeRenderer;

/**
 * @author Ordinastie
 *
 */
public class BBTextField extends UITextField implements IBBCodeRenderer<BBTextField>
{
	protected BBCodeEditor editor;
	/** BBCode for this {@link UILabel} */
	protected BBString bbText;

	protected boolean isWysiwyg = true;

	public BBTextField(MalisisGui gui, BBCodeEditor editor, BBString bbText)
	{
		super(gui, true);
		this.editor = editor;
		setText(bbText);
	}

	public BBTextField(MalisisGui gui, BBCodeEditor editor, String text)
	{
		this(gui, editor, new BBString(text));
	}

	public BBTextField(MalisisGui gui, BBCodeEditor editor)
	{
		this(gui, editor, new BBString(""));
	}

	@Override
	public BBString getBBText()
	{
		return bbText;
	}

	@Override
	public BBTextField setText(BBString str)
	{
		bbText = str != null ? str : new BBString();
		bbText.parseText();
		setText(bbText.getRawText());

		return this;
	}

	@Override
	public float getFontScale()
	{
		return fro.fontScale;
	}

	public boolean isWysiwyg()
	{
		return isWysiwyg;
	}

	public void setWysiwyg(boolean isWysiwyg)
	{
		this.isWysiwyg = isWysiwyg;
		if (isWysiwyg)
			setText(new BBString(text.toString()));
		else
			setText(bbText.getBBString());
		getCursorPosition().jumpToEnd();
	}

	@Override
	public int getStartLine()
	{
		return lineOffset;
	}

	@Override
	protected void buildLines()
	{
		super.buildLines();
		if (isWysiwyg())
			bbText.buildRenderLines(lines);
	}

	@Override
	public void addText(String str)
	{
		if (!isWysiwyg())
		{
			super.addText(str);
			return;
		}

		if (selectingText)
			deleteSelectedText();

		int position = getCursorPosition().getPosition();

		String oldValue = bbText.getText();
		bbText.addText(str, position);
		String newValue = bbText.getText();

		//non cancellable
		fireEvent(new ComponentEvent.ValueChange(this, oldValue, newValue));

		text.setLength(0);
		text.append(bbText.getRawText());
		buildLines();

		getCursorPosition().jumpBy(str.length());

	}

	@Override
	public void deleteSelectedText()
	{
		if (!isWysiwyg())
		{
			super.deleteSelectedText();
			return;
		}

		if (!selectingText)
			return;

		int sp = getSelectionPosition().getPosition();
		int cp = getCursorPosition().getPosition();
		int start = Math.min(sp, cp);
		int end = Math.max(sp, cp);

		String oldValue = bbText.getText();
		bbText.deleteText(start, end);
		String newValue = bbText.getText();

		fireEvent(new ComponentEvent.ValueChange(this, oldValue, newValue));

		text.setLength(0);
		text.append(bbText.getRawText());
		buildLines();

		selectingText = false;
		getCursorPosition().jumpTo(start);
	}

	public void addTag(BBCodeEditor.Tag tag)
	{
		int p = getCursorPosition().getPosition();
		int sp = getSelectionPosition().getPosition();
		int cp = getCursorPosition().getPosition();
		int start = Math.min(sp, cp);
		int end = Math.max(sp, cp);

		if (!isWysiwyg())
		{
			String str = "";
			if (selectingText)
			{
				p = start;
				str = getSelectedText();
			}
			addText(tag.node.toBBString());
			getCursorPosition().jumpTo(p + tag.node.getTag().length() + 2);
			addText(str);
			return;
		}

		if (!selectingText)
			selectWord();

		bbText.insertNode(tag.node.copy(), start, end);
		buildLines();

		getCursorPosition().jumpTo(p);
		selectingText = false;
	}

	@Override
	public void drawText(GuiRenderer renderer)
	{
		if (!isWysiwyg())
			super.drawText(renderer);
		else
			bbText.render(renderer, 2, 2, 0, this);
	}
}
