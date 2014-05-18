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
import net.malisis.core.client.gui.event.GuiEvent;
import net.malisis.core.client.gui.event.MouseClickedEvent;
import net.malisis.core.client.gui.renderer.Drawable;
import net.malisis.core.client.gui.renderer.DrawableIcon;
import net.malisis.core.client.gui.renderer.DynamicTexture;
import net.malisis.core.client.gui.util.Orientation;
import net.malisis.core.client.gui.util.shape.Point;
import net.malisis.core.client.gui.util.shape.Rectangle;
import net.malisis.core.util.RenderHelper;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Color;

import java.util.EnumSet;

/**
 * UIInfoTab
 *
 * @author PaleoCrafter
 */
public class UIInfoTab extends UIContainer
{

    private static final DynamicTexture TEXTURE_LEFT = new DynamicTexture(new ResourceLocation("malisiscore", "textures/gui/widgets/infoTabLeft.png"), 15, 15, 0, 0, new Rectangle(0, 0, 5, 5), new Rectangle(5, 0, 5, 5), new Rectangle(5, 5, 5, 5));

    private static final DynamicTexture TEXTURE_RIGHT = new DynamicTexture(new ResourceLocation("malisiscore", "textures/gui/widgets/infoTabRight.png"), 15, 15, 0, 0, new Rectangle(0, 0, 5, 5), new Rectangle(5, 0, 5, 5), new Rectangle(5, 5, 5, 5));

    private static final int speed = 16;
    private int minWidth = 24;
    private int minHeight = 26;
    private int maxWidth;
    private int maxHeight;

    private String title;
    private Drawable icon;
    private Orientation orientation;

    private int color;
    private boolean open;
    private int growth;

    private EventBus limitedBus;

    private Point realScreenPosition;

    public UIInfoTab()
    {
        this("", new DrawableIcon(Items.apple.getIconFromDamage(0), DrawableIcon.ITEM_SHEET), Orientation.HORIZONTAL_RIGHT, 0x424242);
    }

    public UIInfoTab(String title, Drawable icon, Orientation orientation, int color)
    {
        this.title = title;
        this.orientation = orientation;
        this.icon = icon;
        if (!EnumSet.of(Orientation.HORIZONTAL_LEFT, Orientation.HORIZONTAL_RIGHT).contains(orientation))
            this.orientation = Orientation.HORIZONTAL_RIGHT;
        this.color = color;
        this.growth = 1;
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
        this.maxWidth = RenderHelper.getStringWidth(title) + 16 + 2 + 5;
        int contentWidth = getContentWidth();
        if (contentWidth > maxWidth)
        {
            maxWidth = contentWidth + 10;
        }
        this.maxHeight = 16 + 2 + getContentHeight() + 10;
        this.setSize(minWidth, minHeight);
        getContext().register(this);
    }

    @Subscribe
    public void onAnyEvent(GuiEvent event)
    {
        if (open && getWidth() > minWidth)
            if (event instanceof MouseClickedEvent)
            {
                MouseClickedEvent mouseEvent = ((MouseClickedEvent) event);
                for (UIComponent component : components)
                {
                    if (component.isHovered(mouseEvent.getPosition()))
                    {
                        limitedBus.post(event);
                        return;
                    }
                }
                if (mouseEvent.getButton().isLeft() && new Rectangle(realScreenPosition, getWidth(), getHeight()).contains(mouseEvent.getPosition()))
                    this.close();
            }
            else
            {
                limitedBus.post(event);
            }
    }

    @Override
    public void drawBackground(int mouseX, int mouseY)
    {
        realScreenPosition = getScreenPosition();
        GL11.glColor4f(getColorRGB().getRed() / 255F, getColorRGB().getGreen() / 255F, getColorRGB().getBlue() / 255F, 1F);
        if (orientation == Orientation.HORIZONTAL_RIGHT)
        {
            TEXTURE_RIGHT.setSize(getWidth(), getHeight());
            TEXTURE_RIGHT.draw(getScreenX(), getScreenY());
            GL11.glColor4f(1, 1, 1, 1);
            icon.draw(getScreenX() + 2, getScreenY() + 5);
            if (open)
            {
                if (getWidth() == maxWidth)
                {
                    RenderHelper.drawString(title, getScreenX() + 2 + 16,
                            getScreenY() + 7 + ((16 - mc.fontRenderer.FONT_HEIGHT) / 2), zIndex,
                            0xE1C92F, true);
                }
            }
        }
        else
        {
            TEXTURE_LEFT.setSize(getWidth(), getHeight());
            TEXTURE_LEFT.draw(getScreenX(), getScreenY());
            GL11.glColor4f(1, 1, 1, 1);
            icon.draw(getScreenX() + 6, getScreenY() + 5);
            if (open)
            {
                if (getWidth() == maxWidth)
                {
                    RenderHelper.drawString(title, getScreenX() + 5 + 16,
                            getScreenY() + 7 + ((16 - mc.fontRenderer.FONT_HEIGHT) / 2), zIndex,
                            0xE1C92F, true);
                }
            }
        }
        if (open)
        {
            if (getHeight() == maxHeight && getWidth() == maxWidth)
            {
                this.setScreenPosition(new Point(getScreenX() + 5, getScreenY() + 16 + 7));
                super.drawBackground(mouseX, mouseY);
            }
        }
    }

    @Override
    public void draw(int mouseX, int mouseY)
    {
        if (open && getHeight() == maxHeight && getWidth() == maxWidth)
        {
            this.setScreenPosition(new Point(getScreenX() + 5, getScreenY() + 16 + 7));
            super.draw(mouseX, mouseY);
        }
    }

    @Override
    public void drawForeground(int mouseX, int mouseY)
    {
        if (open && getHeight() == maxHeight && getWidth() == maxWidth)
        {
            this.setScreenPosition(new Point(getScreenX() + 5, getScreenY() + 16 + 7));
            super.drawForeground(mouseX, mouseY);
        }
    }

    @Override
    public void update(int mouseX, int mouseY)
    {
        int width = getWidth();
        int height = getHeight();
        if (open)
        {
            if (width != maxWidth)
            {
                width += speed * growth;
                if (width >= maxWidth)
                    width = maxWidth;

            }

            if (height != maxHeight)
            {
                height += speed * growth;
                if (height >= maxHeight)
                    height = maxHeight;
            }
        }

        if (growth < 0)
        {
            if (height != minHeight)
            {
                height += speed * growth;
                if (height <= minHeight)
                    height = minHeight;
            }

            if (width != minWidth)
            {
                width += speed * growth;
                if (width <= minWidth)
                {
                    width = minWidth;
                }
            }

            if (height == minHeight && width == minWidth)
            {
                growth = 1;
            }
        }

        setSize(width, height);
    }

    /**
     * Gets the RGB equivalent of the tab's
     * text color.
     *
     * @return a {@link org.lwjgl.util.Color} object with the RGB values of this tab's color
     */

    public Color getColorRGB()
    {
        return RenderHelper.getRGBFromColor(color);
    }

    public String getTitle()
    {
        return title;
    }

    @Subscribe
    public void onMouseClick(MouseClickedEvent event)
    {
        if (event.getButton().isLeft() && new Rectangle(realScreenPosition, getWidth(), getHeight()).contains(event.getPosition()))
            if (!open)
                this.open();
    }

    /**
     * Open this tab.
     */
    public void open()
    {
        UIWindow window = (UIWindow) getParent();
        window.closeAllInfoTabs(this.orientation);
        this.open = true;
    }

    /**
     * Close this tab.
     */
    public void close()
    {
        growth = -1;
        this.open = false;
    }

    /**
     * Check whether the tab is closed and hovered.
     * Used for tooltip rendering.
     *
     * @param mousePos the position of the mouse
     * @return true if the tab is closed and hovered, otherwise false
     */
    public boolean isClosedAndHovered(Point mousePos)
    {
        return !open && isHovered(mousePos);
    }

    public Orientation getOrientation()
    {
        return orientation;
    }

    public boolean isOpen()
    {
        return open;
    }

}
