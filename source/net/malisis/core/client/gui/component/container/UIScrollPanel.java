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

package net.malisis.core.client.gui.component.container;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.interaction.UIScrollBar;
import net.malisis.core.client.gui.event.GuiEvent;
import net.malisis.core.client.gui.event.PositionedEvent;
import net.malisis.core.client.gui.util.shape.Point;
import net.malisis.core.client.gui.util.shape.Rectangle;
import net.malisis.core.util.RenderHelper;
import org.lwjgl.opengl.GL11;

/**
 * UIScrollPanel
 *
 * @author PaleoCrafter
 */
public class UIScrollPanel extends UIContainer
{

    private Point realScreenPosition;
    private UIScrollBar bar;
    private EventBus limitedBus;

    public UIScrollPanel(int width, int height, int barWidth)
    {
        this.bar = new UIScrollBar(barWidth, height);
        this.setSize(width, height);
        limitedBus = new EventBus();
        realScreenPosition = new Point(0, 0);
    }

    @Override
    public void initComponent()
    {
        getContext().register(this);
        for (UIComponent component : components)
        {
            component.initComponent();
            if (component instanceof UIContainer)
            {
                ((UIContainer) component).unregisterAll();
                ((UIContainer) component).registerChildrenTo(limitedBus);
            }
            else
            {
                getContext().unregister(component);
            }
            limitedBus.register(component);
        }
        bar.initComponent();
    }

    @Subscribe
    public void onAnyEvent(GuiEvent event)
    {
        if (event instanceof PositionedEvent)
        {
            if (new Rectangle(realScreenPosition, getWidth(), getHeight()).contains(((PositionedEvent) event).getPosition()))
                limitedBus.post(event);
        }
        else limitedBus.post(event);
    }

    public UIScrollBar getBar()
    {
        return bar;
    }

    @Override
    public void drawBackground(int mouseX, int mouseY)
    {
        realScreenPosition = getScreenPosition();
        int contentHeight = getContentHeight();
        if (contentHeight > getHeight())
        {
            float div = ((float) getHeight() / (float) (contentHeight - getHeight()));
            if (div < 1F)
                div += 1F;
            this.setScreenPosition(Point.add(realScreenPosition, new Point(0, (int) ((float) -(bar.getOffset() * contentHeight / (getHeight() - bar.getBarHeight()))
                    / div))));
        }
        GL11.glPushAttrib(GL11.GL_SCISSOR_BIT);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        int scale = RenderHelper.computeGuiScale();
        GL11.glScissor(realScreenPosition.x * scale, mc.displayHeight - (realScreenPosition.y + getHeight()) * scale, getWidth() * scale, getHeight() * scale);

        super.drawBackground(mouseX, mouseY);

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glPopAttrib();
        if (contentHeight > getHeight())
        {
            bar.setScreenPosition(Point.add(realScreenPosition, new Point(getWidth() - bar.getWidth(), 0)));
            bar.draw(mouseX, mouseY);
        }
    }

    @Override
    public void draw(int mouseX, int mouseY)
    {
        GL11.glPushAttrib(GL11.GL_SCISSOR_BIT);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        int scale = RenderHelper.computeGuiScale();
        GL11.glScissor(realScreenPosition.x * scale, mc.displayHeight - (realScreenPosition.y + getHeight()) * scale, getWidth() * scale, getHeight() * scale);

        super.draw(mouseX, mouseY);

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glPopAttrib();
    }

    @Override
    public void drawForeground(int mouseX, int mouseY)
    {
        GL11.glPushAttrib(GL11.GL_SCISSOR_BIT);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        int scale = RenderHelper.computeGuiScale();
        GL11.glScissor(realScreenPosition.x * scale, mc.displayHeight - (realScreenPosition.y + getHeight()) * scale, getWidth() * scale, getHeight() * scale);

        for (UIComponent component : components)
        {
            if (component.isVisible())
            {
                GL11.glColor4f(1F, 1F, 1F, 1F);
                component.drawForeground(mouseX, mouseY);
            }
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glPopAttrib();
    }

    @Override
    public void drawTooltip(int mouseX, int mouseY)
    {
        for (UIComponent component : components)
        {
            if (component.getScreenY() < getScreenY() + getHeight() && component.getScreenY() > getScreenY() && isHovered(realScreenPosition))
            {
                if (component.getTooltip() != null && !component.getTooltip().isEmpty() && !(component instanceof UIContainer) && component.isVisible() && component.isHovered(new Point(mouseX, mouseY)) && component.isEnabled())
                {
                    getContext().getTooltip().setText(component.getTooltip());
                    getContext().getTooltip().draw(mouseX, mouseY);
                }
                else if (component instanceof UIContainer && component.isVisible() && component.isEnabled())
                {
                    ((UIContainer) component).drawTooltip(mouseX, mouseY);
                }
            }
        }
    }

    @Override
    public void update(int mouseX, int mouseY)
    {
        super.update(mouseX, mouseY);
        bar.update(mouseX, mouseY);
    }

}
