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

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;

/**
 * UILabel.
 *
 * @author Ordinastie
 */
public class UILabel extends UIComponent<UILabel>
{
	/** Text of this {@link UILabel}. */
	protected String text;
	/** Color to use to draw the text of this {@link UILabel}. */
	protected int color = 0x404040;
	/** Determines if the text is drawn with drop shadow. */
	protected boolean drawShadow;
	/** Width of the text. */
	protected int textWidth;
	/** Height of the text. */
	protected int textHeight;
	/** Scale of the font. */
	protected float fontScale = 1;
	/** Whether this {@link UILabel} can be interacted with. */
	protected boolean canInteract = false;

	/**
	 * Instantiates a new {@link UILabel}.
	 *
	 * @param gui the gui
	 * @param text the text
	 */
	public UILabel(MalisisGui gui, String text)
	{
		this(gui);
		this.setText(text);
	}

	/**
	 * Instantiates a new {@link UILabel}.
	 *
	 * @param gui the gui
	 */
	public UILabel(MalisisGui gui)
	{
		super(gui);
	}

	// #region getters/setters
	/**
	 * Sets the text of this {@link UILabel}. If no width was previously set, it will be recalculated for this text.
	 *
	 * @param text the text
	 * @return this {@link UILabel}
	 */
	public UILabel setText(String text)
	{
		this.text = text;
		calculateSize();

		return this;
	}

	/**
	 * Gets the text.
	 *
	 * @return text of this {@link UILabel}
	 */
	public String getText()
	{
		return this.text;
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
	 * Gets the component at.
	 *
	 * @param x the x
	 * @param y the y
	 * @return the component at
	 */
	@Override
	public UIComponent getComponentAt(int x, int y)
	{
		return canInteract ? super.getComponentAt(x, y) : null;
	}

	/*	@Override
		public void setFocused(boolean focused)
		{
			if (canInteract)
				super.setFocused(focused);
			else if (parent != null)
				parent.setFocused(focused);
		}

		@Override
		public void setHovered(boolean hovered)
		{
			if (canInteract)
				super.setHovered(hovered);
			else if (parent != null)
				parent.setHovered(hovered);
		}
	*/
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
	 * Gets the font scale.
	 *
	 * @return the fontScale used by this {@link UILabel}
	 */
	public float getFontScale()
	{
		return fontScale;
	}

	// #end getters/setters

	/**
	 * Calculate the size of this {@link UILabel}.
	 */
	protected void calculateSize()
	{
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
		renderer.setFontScale(fontScale);
		renderer.drawText(text, color, drawShadow);
		renderer.setFontScale(1);
	}

	@Override
	public String getPropertyString()
	{
		return "text=" + text + " | color=0x" + Integer.toHexString(this.color) + " | " + super.getPropertyString();
	}

}
