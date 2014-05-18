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

import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.util.RenderHelper;

import java.util.Arrays;

/**
 * UILabel
 *
 * @author PaleoCrafter
 */
public class UILabel extends UIComponent
{

    private String[] lines;
    private int color;
    private boolean drawShadow;

    public UILabel()
    {
        this("");
    }

    public UILabel(String text)
    {
        this(text, 0x404040);
    }

    public UILabel(String text, int color)
    {
        this.color = color;
        this.lines = text.split("\\n");
        this.setSize(RenderHelper.getStringWidth(RenderHelper.getLongestString(lines)), lines.length * mc.fontRenderer.FONT_HEIGHT + (lines.length - 1));
    }

    public void setText(String text)
    {
        lines = text.split("\\n");
        this.setSize(RenderHelper.getStringWidth(RenderHelper.getLongestString(lines)), lines.length * (mc.fontRenderer.FONT_HEIGHT + 1));
    }

    public void setColor(int color)
    {
        this.color = color;
    }

    public void setDrawShadow(boolean drawShadow)
    {
        this.drawShadow = drawShadow;
    }

    @Override
    public void draw(int mouseX, int mouseY)
    {
        for (int i = 0; i < lines.length; i++)
        {
            String line = lines[i];
            RenderHelper.drawString(line, getScreenX(), getScreenY() + i * (mc.fontRenderer.FONT_HEIGHT + 1), zIndex, color, drawShadow);
        }
    }

    @Override
    public void update(int mouseX, int mouseY)
    {

    }

    @Override
    public String toString()
    {
        return this.getClass().getName() + "[ text=" + Arrays.toString(lines) + ", color=0x" + Integer.toHexString(this.color) + ", " + this.getPropertyString() + " ]";
    }
}
