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

package net.malisis.core.client.gui.component.decoration;

import javax.annotation.Nonnull;

import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.control.IScrollable;
import net.malisis.core.client.gui.component.scrolling.UIScrollBar;
import net.malisis.core.client.gui.element.IClipable;
import net.malisis.core.client.gui.element.position.Position.IPosition;
import net.malisis.core.client.gui.element.size.Size;
import net.malisis.core.client.gui.element.size.Size.ISize;
import net.malisis.core.client.gui.text.GuiText;
import net.malisis.core.renderer.font.FontOptions;
import net.minecraft.util.text.TextFormatting;

/**
 * UILabel.
 *
 * @author Ordinastie
 */
public class UILabel extends UIComponent implements IScrollable, IClipable
{
	protected final GuiText text;
	protected boolean autoSize = false;
	protected final IPosition offset = UIScrollBar.scrollingOffset(this);

	/**
	 * Instantiates a new {@link UILabel}.
	 *
	 * @param text the text
	 * @param multiLine the multiLine
	 */
	public UILabel(String text, boolean multiLine)
	{
		this.text = GuiText	.builder()
							.parent(this)
							.multiLine(multiLine)
							.literal(false)
							.translated(true)
							.text(text)
							.fontOptions(FontOptions.builder().color(0x444444).build())
							.wrapSize(() -> autoSize ? 0 : innerSize().width())
							.build();
		setAutoSize();

		setForeground(this.text);
	}

	/**
	 * Instantiates a new {@link UILabel}.
	 *
	 * @param text the text
	 */
	public UILabel(String text)
	{
		this(text, text.contains("\r") || text.contains("\n"));
	}

	/**
	 * Instantiates a new {@link UILabel}.
	 *
	 * @param multiLine the multi line
	 */
	public UILabel(boolean multiLine)
	{
		this("", multiLine);
	}

	/**
	 * Instantiates a new {@link UILabel}.
	 */
	public UILabel()
	{
		this("", false);
	}

	// #region getters/setters

	@Override
	public GuiText content()
	{
		return text;
	}

	public void setText(String text)
	{
		this.text.setText(text);
	}

	public String getText()
	{
		return text.getRawText();
	}

	public void setFontOptions(FontOptions fontOptions)
	{
		text.setFontOptions(fontOptions);
	}

	@Override
	public IPosition contentPosition()
	{
		return text.position();
	}

	@Override
	public ISize contentSize()
	{
		return text.size();
	}

	public void setAutoSize()
	{
		setSize(Size.sizeOfContent(this, 0, 0));
		autoSize = true;
	}

	@Override
	public void setSize(@Nonnull ISize size)
	{
		super.setSize(size);
		autoSize = false;
	}

	@Override
	@Nonnull
	public ISize size()
	{
		return size;
	}

	@Override
	public IPosition offset()
	{
		return offset;
	}
	// #end getters/setters

	@Override
	public ClipArea getClipArea()
	{
		return ClipArea.from(this);
	}

	@Override
	public String getPropertyString()
	{
		return "[" + TextFormatting.DARK_AQUA + text + TextFormatting.RESET + "] " + super.getPropertyString();
	}

}
