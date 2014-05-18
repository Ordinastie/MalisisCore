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

import net.malisis.core.client.gui.renderer.DynamicTexture;
import net.malisis.core.client.gui.util.Orientation;
import net.malisis.core.client.gui.util.shape.Point;
import net.malisis.core.client.gui.util.shape.Rectangle;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.LinkedList;
import java.util.List;

/**
 * UIWindow
 *
 * @author PaleoCrafter
 */
public class UIWindow extends UIContainer
{

    private List<UIInfoTab> infoTabs;

    private static final ResourceLocation TEXTURE = new ResourceLocation("malisiscore", "textures/gui/widgets/window.png");

    public UIWindow(int width, int height)
    {
        super(width, height);
        this.setBackground(new DynamicTexture(TEXTURE, 15, 15, this.getWidth(), this.getHeight(), new Rectangle(0, 0, 5, 5), new Rectangle(5, 0, 5, 5), new Rectangle(5, 5, 5, 5)));
        this.setPadding(5, 5);
        infoTabs = new LinkedList<>();
    }

    @Override
    public void initComponent()
    {
        super.initComponent();
        for (UIInfoTab tab : infoTabs)
        {
            tab.initComponent();
        }
    }

    @Override
    public void drawBackground(int mouseX, int mouseY)
    {
        super.drawBackground(mouseX, mouseY);
        int left = 0;
        int right = 0;
        for (UIInfoTab tab : infoTabs)
        {
            if (tab.getOrientation() == Orientation.HORIZONTAL_RIGHT)
            {
                tab.setScreenPosition(new Point(getScreenX() + getWidth(), getScreenY() + 5 + right));
                right += tab.getHeight() + 2;
            }
            else
            {
                tab.setScreenPosition(new Point(getScreenX() - tab.getWidth(), getScreenY() + 5 + left));
                left += tab.getHeight() + 2;
            }
            GL11.glColor4f(1, 1, 1, 1);
            tab.drawBackground(mouseX, mouseY);
        }
    }

    @Override
    public void draw(int mouseX, int mouseY)
    {
        super.draw(mouseX, mouseY);
        for (UIInfoTab tab : infoTabs)
        {
            GL11.glColor4f(1, 1, 1, 1);
            tab.draw(mouseX, mouseY);
        }
    }

    @Override
    public void drawForeground(int mouseX, int mouseY)
    {
        super.drawForeground(mouseX, mouseY);
        for (UIInfoTab tab : infoTabs)
        {
            GL11.glColor4f(1, 1, 1, 1);
            tab.drawForeground(mouseX, mouseY);
        }
    }

    @Override
    public void drawTooltip(int mouseX, int mouseY)
    {
        super.drawTooltip(mouseX, mouseY);
        for (UIInfoTab tab : infoTabs)
        {
            if (tab.isClosedAndHovered(new Point(mouseX, mouseY)))
            {
                getContext().getTooltip().setText(tab.getTitle());
                getContext().getTooltip().draw(mouseX, mouseY);
            }
        }
    }

    @Override
    public void update(int mouseX, int mouseY)
    {
        super.update(mouseX, mouseY);
        for (UIInfoTab tab : infoTabs)
        {
            tab.update(mouseX, mouseY);
        }
    }

    /**
     * Adds a new info tab to this window. The side
     * will be determined by the tab's orientation.
     *
     * @param tab an {@link net.malisis.core.client.gui.component.container.UIInfoTab UIInfoTab} object
     *            to be added to this window as info tab
     */
    public void addInfoTab(UIInfoTab tab)
    {
        this.infoTabs.add(tab);
        tab.setParent(this);
    }

    /**
     * Closes all info tabs on the specified side.
     *
     * @param orientation the orientation of the tabs to close
     */
    public void closeAllInfoTabs(Orientation orientation)
    {
        for (UIInfoTab tab : infoTabs)
        {
            if (tab.getOrientation() == orientation && tab.isOpen())
            {
                tab.close();
            }
        }
    }

}
