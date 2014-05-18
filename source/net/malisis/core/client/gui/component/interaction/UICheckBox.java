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
import net.malisis.core.client.gui.event.ValueChangedEvent;
import net.malisis.core.client.gui.util.shape.Point;
import net.malisis.core.util.RenderHelper;
import net.minecraft.util.ResourceLocation;

/**
 * UICheckBox
 *
 * @author PaleoCrafter
 */
public class UICheckBox extends UIComponent
{

    private static final ResourceLocation DEFAULT_TEXTURE = new ResourceLocation("malisiscore", "textures/gui/widgets/checkbox.png");
    private static final ResourceLocation CHECKED_TEXTURE = new ResourceLocation("malisiscore", "textures/gui/widgets/checkbox_checked.png");
    private static final ResourceLocation HOVERED_TEXTURE = new ResourceLocation("malisiscore", "textures/gui/widgets/checkbox_hovered.png");
    private String label;
    private boolean checked;

    public UICheckBox()
    {
        this("");
    }

    public UICheckBox(String label)
    {
        this.label = label;
        this.setSize(16 + RenderHelper.getStringWidth(label), 14);
    }

    @Override
    public boolean isHovered(Point mousePosition)
    {
        return getScreenBounds().resize(14, 14).contains(mousePosition);
    }

    @Subscribe
    public void onMouseClick(MouseClickedEvent event)
    {
        if (this.isHovered(event.getPosition()) && event.getButton().isLeft())
        {
            this.checked = !checked;
            this.getContext().publish(new ValueChangedEvent(this));
        }
    }

    public void setLabel(String label)
    {
        this.label = label;
        this.setSize(16 + RenderHelper.getStringWidth(label), 14);
    }

    @Override
    public void draw(int mouseX, int mouseY)
    {
        RenderHelper.drawRectangle(checked ? CHECKED_TEXTURE : isHovered(new Point(mouseX, mouseY)) ? HOVERED_TEXTURE : DEFAULT_TEXTURE, getScreenX(), getScreenY(), zIndex, 14, 14, 0, 0, 14, 14);
        RenderHelper.drawString(label, getScreenX() + 16, getScreenY() + (getHeight() - mc.fontRenderer.FONT_HEIGHT + 1) / 2, zIndex, 0x404040, false);
    }

    @Override
    public void update(int mouseX, int mouseY)
    {

    }

    @Override
    public String toString()
    {
        return this.getClass().getName() + "[ text=" + label + ", checked=" + this.checked + ", " + this.getPropertyString() + " ]";
    }
}
