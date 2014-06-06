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

import net.malisis.core.client.gui.GuiIcon;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.component.UIComponent;

/**
 * UILabel
 * 
 * @author PaleoCrafter
 */
public class UILabel extends UIComponent
{
	/**
	 * Text of this <code>UILabel</code>.
	 */
	private String text;
	/**
	 * Color to use to draw the text of this <code>UILabel</code>.
	 */
	private int color;
	/**
	 * Determines if the text is drawn with drop shadow.
	 */
	private boolean drawShadow;
	/**
	 * Determines if the width of this <code>UILabel</code> is dependent of the text
	 */
	private boolean autoWidth = true;
	/**
	 * Width of the text
	 */
	private int textWidth;
	/**
	 * Height of the text
	 */
	private int textHeight;

	public UILabel()
	{
		this("", 0, 0x404040);
	}

	public UILabel(String text)
	{
		this(text, 0, 0x404040);
	}

	public UILabel(String text, int width, int color)
	{
		this.color = color;
		this.setText(text);
		this.setSize(0);
	}

	// #region getters/setters
	/**
	 * Sets the text of this <code>UILabel</code>. If no width was previously set, it will be recalculated for this text.
	 * 
	 * @param text
	 * @return this <code>UILabel</code>
	 */
	public UILabel setText(String text)
	{
		this.text = text;
		this.textWidth = GuiRenderer.getStringWidth(text);
		this.textHeight = GuiRenderer.FONT_HEIGHT;
		if (autoWidth)
			setSize(0);
		return this;
	}

	/**
	 * @return text of this <code>UILabel</code>
	 */
	public String getText()
	{
		return this.text;
	}

	/**
	 * Sets the width of this <code>UILabel</code>. Height is defined by text height.
	 * 
	 * @param width
	 * @return this <code>UILabel</code>
	 */
	public UIComponent setSize(int width)
	{
		this.autoWidth = width == 0;
		this.width = autoWidth ? textWidth : width;
		this.height = textHeight;
		return this;
	}

	/**
	 * Sets the width of this <code>UILabel</code>. Height parameter is ignored and defined by text height.
	 * 
	 * @param width
	 * @param height ignored
	 * @return this <code>UILabel</code>
	 */
	@Override
	public UIComponent setSize(int width, int height)
	{
		return setSize(width);
	}

	/**
	 * Set the color of the text of this <code>UILabel</code>.
	 * 
	 * @param color
	 */
	public void setColor(int color)
	{
		this.color = color;
	}

	/**
	 * Set the drop shadow for the text of this <code>UILabel</code>.
	 * 
	 * @param drawShadow
	 */
	public void setDrawShadow(boolean drawShadow)
	{
		this.drawShadow = drawShadow;
	}

	// #end getters/setters

	@Override
	public GuiIcon getIcon(int face)
	{
		return null;
	}

	@Override
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{}

	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		renderer.drawString(text, screenX(), screenY(), color, drawShadow);
	}

	@Override
	public String toString()
	{
		return this.getClass().getName() + "[ text=" + text + ", color=0x" + Integer.toHexString(this.color) + ", "
				+ this.getPropertyString() + " ]";
	}

}
