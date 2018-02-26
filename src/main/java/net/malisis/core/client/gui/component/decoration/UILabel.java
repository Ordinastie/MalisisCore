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

import static com.google.common.base.Preconditions.*;

import javax.annotation.Nonnull;

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.control.IScrollable;
import net.malisis.core.client.gui.component.control.UIScrollBar;
import net.malisis.core.client.gui.component.control.UISlimScrollbar;
import net.malisis.core.client.gui.component.element.Padding;
import net.malisis.core.client.gui.component.element.Size.ISize;
import net.malisis.core.client.gui.text.GuiText;
import net.malisis.core.client.gui.text.IGuiTextProxy;
import net.malisis.core.renderer.font.FontOptions;
import net.minecraft.client.gui.GuiScreen;

/**
 * UILabel.
 *
 * @author Ordinastie
 */
public class UILabel extends UIComponent<UILabel> implements IScrollable, IGuiTextProxy
{
	protected GuiText text = null;

	//text space
	/** Number of line offset out of this {@link UILabel} when drawn. Always 0 if {@link #multiLine} is false. */
	protected int lineOffset = 0;

	//interaction
	/** Scrollbar of the textfield **/
	protected UISlimScrollbar scrollBar;

	/** Width of the text. */
	protected int textWidth;
	/** Height of the text. */
	protected int textHeight;

	/**
	 * Instantiates a new {@link UILabel}.
	 *
	 * @param gui the gui
	 * @param text the text
	 * @param multiLine the multi line
	 */
	public UILabel(MalisisGui gui, String text, boolean multiLine)
	{
		super(gui);
		setText(text);
		setMultiline(multiLine);
		setFontOptions(FontOptions.builder().color(0x444444).build());
	}

	/**
	 * Instantiates a new {@link UILabel}.
	 *
	 * @param gui the gui
	 * @param text the text
	 */
	public UILabel(MalisisGui gui, String text)
	{
		this(gui, text, false);
	}

	/**
	 * Instantiates a new {@link UILabel}.
	 *
	 * @param gui the gui
	 * @param multiLine the multi line
	 */
	public UILabel(MalisisGui gui, boolean multiLine)
	{
		this(gui, "", multiLine);
	}

	/**
	 * Instantiates a new {@link UILabel}.
	 *
	 * @param gui the gui
	 */
	public UILabel(MalisisGui gui)
	{
		this(gui, "", false);
	}

	@Override
	public void onAddedToScreen()
	{
		text.setWrapSize(size.width() - UIScrollBar.xOffset(this));
	}

	// #region getters/setters
	/**
	 * Sets the {@link GuiText} used by this {@link UILabel}.
	 *
	 * @param text the new text
	 */
	@Override
	public void setGuiText(GuiText text)
	{
		this.text = checkNotNull(text);
	}

	/**
	 * Gets the {@link GuiText} used by this {@link UILabel}.
	 *
	 * @return the text
	 */
	@Override
	public GuiText getGuiText()
	{
		return text;
	}

	public int getVisibleLines()
	{
		return size().height() / getLineHeight();
	}

	public int getLineHeight()
	{
		return text.getLineHeight();
	}

	@Override
	public void setSize(ISize size)
	{
		super.setSize(size);
		text.setWrapSize(size.width() - UIScrollBar.xOffset(this));
	}

	@Override
	@Nonnull
	public ISize size()
	{
		//a single line label size should match the text
		return text.isMultiLine() ? super.size() : text.size();
	}
	// #end getters/setters

	//#region IScrollable
	@Override
	public ISize contentSize()
	{
		return text.size();
	}

	@Override
	public float getOffsetX()
	{
		return 0;
	}

	@Override
	public void setOffsetX(float offsetX, int delta)
	{}

	@Override
	public float getOffsetY()
	{
		if (text.lines().size() < getVisibleLines())
			return 0;

		return (float) lineOffset / (text.lines().size() - getVisibleLines());
	}

	@Override
	public void setOffsetY(float offsetY, int delta)
	{
		lineOffset = Math.round(offsetY * (text.lines().size() - getVisibleLines()));
		lineOffset = Math.max(0, Math.min(text.lines().size(), lineOffset));
	}

	@Override
	public float getScrollStep()
	{
		float step = (float) 1 / (text.lines().size() - getVisibleLines());
		return (GuiScreen.isCtrlKeyDown() ? 5 * step : step);
	}

	@Override
	public Padding getPadding()
	{
		return Padding.NO_PADDING;
	}

	//#end IScrollable
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
		text.render(renderer, lineOffset, lineOffset + getVisibleLines(), screenX(), screenY(), getZIndex());

		//debug
		//renderer.drawRectangle(0, 0, 0, size().width(), size().height(), 0x3399FF, 100);
	}

	@Override
	public String getPropertyString()
	{
		return "text=" + text + " | " + super.getPropertyString();
	}
}
