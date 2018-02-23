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

import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.IGuiText;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.control.IScrollable;
import net.malisis.core.client.gui.component.control.UIScrollBar;
import net.malisis.core.client.gui.component.control.UIScrollBar.Type;
import net.malisis.core.client.gui.component.control.UISlimScrollbar;
import net.malisis.core.client.gui.component.element.Padding;
import net.malisis.core.client.gui.component.element.Size;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.malisis.core.client.gui.event.component.ContentUpdateEvent;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.renderer.font.MalisisFont;
import net.minecraft.client.gui.GuiScreen;

/**
 * UILabel.
 *
 * @author Ordinastie
 */
public class UILabel extends UIComponent<UILabel> implements IScrollable, IGuiText<UILabel>
{
	/** The {@link MalisisFont} to use for this {@link UILabel}. */
	protected MalisisFont font = MalisisFont.minecraftFont;
	/** The {@link FontOptions} to use for this {@link UILabel}. */
	protected FontOptions fontOptions = FontOptions.builder().color(0x444444).build();
	/** Text of this {@link UILabel}. */
	protected String text;
	/** List of strings making the text of this {@link UILabel}. */
	protected List<String> lines = Lists.newArrayList();
	/** Whether this {@link UITextField} handles multiline text. */
	protected boolean multiLine = false;

	//text space
	/** Number of line offset out of this {@link UILabel} when drawn. Always 0 if {@link #multiLine} is false. */
	protected int lineOffset = 0;
	/** Space used between each line. */
	protected int lineSpacing = 1;

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
		this.setText(text);
		this.multiLine = multiLine;
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
		this(gui, (String) null, multiLine);
	}

	/**
	 * Instantiates a new {@link UILabel}.
	 *
	 * @param gui the gui
	 */
	public UILabel(MalisisGui gui)
	{
		this(gui, (String) null, false);
	}

	@Override
	public void onAddedToScreen()
	{
		buildLines();
	}

	// #region getters/setters
	/**
	 * Gets the text of this {@link UILabel}.
	 *
	 * @return the text
	 */
	public String getText()
	{
		return text;
	}

	/**
	 * Sets the text of this {@link UILabel}.<br>
	 * If {@link #multiLine} is false, the width is recalculated.<br>
	 * If {@link #multiLine} is true, the {@link #lines} will be recreated.
	 *
	 * @param text the text
	 * @return this {@link UILabel}
	 */
	public UILabel setText(String text)
	{
		//text didn't change, skip
		if (Objects.equals(this.text, text))
			return this;

		this.text = text;
		if (multiLine)
			buildLines();
		else
			calculateSize();

		return this;
	}

	/**
	 * Gets the {@link MalisisFont} used for this {@link UILabel}.
	 *
	 * @return the font
	 */
	@Override
	public MalisisFont getFont()
	{
		return font;
	}

	@Override
	public UILabel setFont(MalisisFont font)
	{
		this.font = font;
		calculateSize();
		return this;
	}

	/**
	 * Gets the {@link FontOptions} used for this {@link UILabel}.
	 *
	 * @return the font renderer options
	 */
	@Override
	public FontOptions getFontOptions()
	{
		return fontOptions;
	}

	/**
	 * Sets the {@link MalisisFont} and {@link FontOptions} to use for this {@link UILabel}.
	 *
	 * @param fro the fro
	 * @return this {@link UILabel}
	 */
	@Override
	public UILabel setFontOptions(FontOptions fro)
	{
		this.fontOptions = fro;
		calculateSize();
		return this;
	}

	public int getVisibleLines()
	{
		return size().height() / getLineHeight();
	}

	public int getLineHeight()
	{
		return (int) (font.getStringHeight(fontOptions) + lineSpacing);
	}

	// #end getters/setters

	//#region IScrollable
	@Override
	public int getContentWidth()
	{
		return size.width();
	}

	@Override
	public int getContentHeight()
	{
		return lines.size() * getLineHeight();
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
		if (lines.size() < getVisibleLines())
			return 0;

		return (float) lineOffset / (lines.size() - getVisibleLines());
	}

	@Override
	public void setOffsetY(float offsetY, int delta)
	{
		lineOffset = Math.round(offsetY * (lines.size() - getVisibleLines()));
		lineOffset = Math.max(0, Math.min(lines.size(), lineOffset));
	}

	@Override
	public float getScrollStep()
	{
		float step = (float) 1 / (lines.size() - getVisibleLines());
		return (GuiScreen.isCtrlKeyDown() ? 5 * step : step);
	}

	@Override
	public Padding getPadding()
	{
		return Padding.NO_PADDING;
	}

	//#end IScrollable
	/**
	 * Builds the lines for this {@link UILabel}. Only used if {@link #multiLine} is true.
	 */
	protected void buildLines()
	{
		lines.clear();

		if (!StringUtils.isEmpty(text))
		{
			UIScrollBar sc = UIScrollBar.getScrollbar(this, Type.VERTICAL);
			int width = size().width();
			if (sc != null && sc.isVisible())
				width -= sc.size().width();
			lines = font.wrapText(text, width, fontOptions);
		}

		fireEvent(new ContentUpdateEvent<>(this));
	}

	/**
	 * Calculate the size of this {@link UILabel}.
	 */
	protected void calculateSize()
	{
		if (multiLine)
			return;

		this.textWidth = (int) font.getStringWidth(text, fontOptions);
		this.textHeight = (int) font.getStringHeight(fontOptions);
		//TODO: custom Size ?
		setSize(Size.of(textWidth, textHeight));
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
	{}

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
		if (multiLine)
		{
			font.render(renderer,
						lines,
						lineOffset,
						lineOffset + getVisibleLines(),
						screenX(),
						screenY(),
						getZIndex(),
						lineSpacing,
						fontOptions);
		}
		else
			renderer.drawText(font, text, fontOptions);
	}

	@Override
	public String getPropertyString()
	{
		return "text=" + text + " | " + super.getPropertyString();
	}
}
