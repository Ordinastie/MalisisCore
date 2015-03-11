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

import java.util.LinkedList;
import java.util.List;

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.control.IScrollable;
import net.malisis.core.client.gui.component.control.UISlimScrollbar;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.malisis.core.client.gui.event.component.ContentUpdateEvent;
import net.malisis.core.client.gui.event.component.SpaceChangeEvent.SizeChangeEvent;
import net.malisis.core.util.bbcode.BBString;
import net.malisis.core.util.bbcode.render.BBCodeRenderer;
import net.malisis.core.util.bbcode.render.IBBCodeRenderer;
import net.minecraft.client.gui.GuiScreen;

import org.apache.commons.lang3.StringUtils;

import com.google.common.eventbus.Subscribe;

/**
 * UILabel.
 *
 * @author Ordinastie
 */
public class UILabel extends UIComponent<UILabel> implements IScrollable, IBBCodeRenderer<UILabel>
{
	/** Text of this {@link UILabel}. */
	protected String text;
	/** BBCode for this {@link UILabel} */
	protected BBString bbText;
	/** BBCode renderer **/
	protected BBCodeRenderer bbRenderer;
	/** List of strings making the text of this {@link UILabel} */
	protected List<String> lines = new LinkedList<>();
	/** Whether this {@link UITextField} handles multiline text. */
	protected boolean multiLine = false;

	//text space
	/** Number of line offset out of this {@link UILabel} when drawn. Always 0 if {@link #multiLine} is false */
	protected int lineOffset = 0;
	/** Space used between each line. */
	protected int lineSpacing = 1;
	/** Font scale used to draw the text *. */
	protected float fontScale = 1;

	//interaction
	/** Scrollbar of the textfield **/
	protected UISlimScrollbar scrollBar;

	/** Color to use to draw the text of this {@link UILabel}. */
	protected int color = 0x404040;
	/** Determines if the text is drawn with drop shadow. */
	protected boolean drawShadow;
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
	public UILabel(MalisisGui gui, BBString text)
	{
		this(gui);
		this.setText(text);
		this.multiLine = true;
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
		this.text = text;
		this.bbText = null;
		if (multiLine)
			buildLines();
		else
			calculateSize();

		return this;
	}

	/**
	 * Sets the color of the text of this {@link UILabel}.
	 *
	 * @param color the color
	 * @return the UI label
	 */
	public UILabel setColor(int color)
	{
		this.color = color;
		return this;
	}

	/**
	 * Set the drop shadow for the text of this {@link UILabel}.
	 *
	 * @param drawShadow the draw shadow
	 * @return the UI label
	 */
	public UILabel setDrawShadow(boolean drawShadow)
	{
		this.drawShadow = drawShadow;
		return this;
	}

	/**
	 * Sets the scale of the font to use for this {@link UILabel}.
	 *
	 * @param scale the scale
	 * @return the UI label
	 */
	public UILabel setFontScale(float scale)
	{
		this.fontScale = scale;
		calculateSize();
		return this;
	}

	/**
	 * Gets the font scale for this {@link UILabel}.
	 *
	 * @return the font scale
	 */
	@Override
	public float getFontScale()
	{
		return fontScale;
	}

	// #end getters/setters

	//#region IScrollable
	@Override
	public int getContentWidth()
	{
		return getWidth();
	}

	@Override
	public int getContentHeight()
	{
		return lines.size() * (GuiRenderer.FONT_HEIGHT + 1);
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
		return (float) lineOffset / (lines.size() - getVisibleLines());
	}

	@Override
	public void setOffsetY(float offsetY, int delta)
	{
		lineOffset = Math.round(offsetY / getScrollStep());
		lineOffset = Math.max(0, Math.min(lines.size(), lineOffset));
	}

	@Override
	public float getScrollStep()
	{
		float step = (float) 1 / (lines.size() - getVisibleLines());
		return (GuiScreen.isCtrlKeyDown() ? 5 * step : step);
	}

	@Override
	public int getVerticalPadding()
	{
		return 0;
	}

	@Override
	public int getHorizontalPadding()
	{
		return 0;
	}

	//#end IScrollable

	//#region IBBStringRenderer

	/**
	 * Gets the BB text of this {@link UILabel}.
	 *
	 * @return the BB text
	 */
	@Override
	public BBString getBBText()
	{
		return bbText;
	}

	@Override
	public UILabel setText(BBString str)
	{
		if (!multiLine)
			throw new IllegalArgumentException("Can only set BBString for multi line labels.");

		setText(str.getRawText());
		bbText = str;
		bbText.buildRenderLines(lines);

		return this;
	}

	@Override
	public int getStartLine()
	{
		return lineOffset;
	}

	@Override
	public int getVisibleLines()
	{
		return getHeight() / getLineHeight();
	}

	@Override
	public int getLineHeight()
	{
		return GuiRenderer.getStringHeight(fontScale) + lineSpacing;
	}

	//#end IBBStringRenderer

	/**
	 * Gets the component at.
	 *
	 * @param x the x
	 * @param y the y
	 * @return the component at
	 */
	@Override
	public UIComponent getComponentAt(int x, int y)
	{
		//make single line label non interactible
		return multiLine ? super.getComponentAt(x, y) : null;
	}

	/**
	 * Builds the lines for this {@link UILabel}. Only used if {@link #multiLine} is true.
	 */
	protected void buildLines()
	{
		lines.clear();

		if (!StringUtils.isEmpty(text))
			lines = GuiRenderer.wrapText(text, getWidth(), fontScale);

		fireEvent(new ContentUpdateEvent<UILabel>(this));
	}

	/**
	 * Calculate the size of this {@link UILabel}.
	 */
	protected void calculateSize()
	{
		if (multiLine)
			return;
		this.textWidth = GuiRenderer.getStringWidth(text, fontScale);
		this.textHeight = GuiRenderer.getStringHeight(fontScale);
		setSize(textWidth, textHeight);
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
		if (bbText != null)
		{
			bbText.render(renderer, screenX(), screenY(), getZIndex(), this);
			return;
		}

		renderer.setFontScale(fontScale);
		if (multiLine)
		{
			for (int i = lineOffset; i < lineOffset + getVisibleLines() && i < lines.size(); i++)
			{
				int h = (i - lineOffset) * getLineHeight();
				renderer.drawText(lines.get(i), 0, h, color, drawShadow);
			}
		}
		else
			renderer.drawText(text, color, drawShadow);
		renderer.setFontScale(1);
	}

	@Subscribe
	public void onSizeChange(SizeChangeEvent<UILabel> event)
	{
		buildLines();
	}

	@Override
	public String getPropertyString()
	{
		return "text=" + text + " | color=0x" + Integer.toHexString(this.color) + " | " + super.getPropertyString();
	}

}
