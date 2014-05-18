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

import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.event.MouseClickedEvent;
import net.malisis.core.client.gui.event.MouseScrolledEvent;
import net.malisis.core.client.gui.renderer.Drawable;
import net.malisis.core.client.gui.renderer.DynamicTexture;
import net.malisis.core.client.gui.util.MouseButton;
import net.malisis.core.client.gui.util.shape.Rectangle;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;

/**
 * UIScrollBar
 *
 * @author PaleoCrafter
 */
public class UIScrollBar extends UIComponent
{

    private Drawable bar;
    private int barY;
    private int barHeight;
    private int step;
    private int initialClickY;

    public UIScrollBar(int width, int height)
    {
        this.setSize(width, height);
        this.barHeight = height / 4;
        this.step = 10;
        this.bar = new DynamicTexture(new ResourceLocation("malisiscore", "textures/gui/widgets/scrollbar.png"), 4, 4, width, barHeight, new Rectangle(0, 0, 1, 1), new Rectangle(1, 0, 2, 1), new Rectangle(1, 1, 2, 2));
    }

    public void clamp()
    {
        this.barY = MathHelper.clamp_int(this.barY, 0, this.getHeight() - this.barHeight);
    }

    @Override
    public void draw(int mouseX, int mouseY)
    {
        bar.draw(getScreenX(), getScreenY() + barY);
    }

    @Override
    public void update(int mouseX, int mouseY)
    {
        if (Mouse.isButtonDown(MouseButton.LEFT.ordinal()))
        {
            if (initialClickY >= 0)
            {
                barY = ((mouseY - getScreenY()) - initialClickY);
            }
        }
        else
        {
            initialClickY = -1;
        }
        clamp();
    }

    public int getBarHeight()
    {
        return barHeight;
    }

    public void setBarHeight(int barHeight)
    {
        bar.setSize(getWidth(), barHeight);
        this.barHeight = barHeight;
    }

    public int getOffset()
    {
        return barY;
    }

    @Subscribe
    public void mouseClick(MouseClickedEvent event)
    {
        if (this.isHovered(event.getPosition()) && event.getButton().isLeft())
        {
            initialClickY = event.getPosition().y - (getScreenY() + barY);
            barY = ((event.getPosition().y - getScreenY()) - initialClickY);
        }
    }

    @Subscribe
    public void mouseScroll(MouseScrolledEvent event)
    {
        barY -= event.getDir() * step;
        clamp();
    }
}
