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

package net.malisis.core.client.gui.proxy;

import com.google.common.eventbus.EventBus;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.decoration.UITooltip;
import net.malisis.core.client.gui.event.GuiEvent;
import net.malisis.core.client.gui.event.KeyTypedEvent;
import net.malisis.core.client.gui.event.MouseClickedEvent;
import net.malisis.core.client.gui.event.MouseScrolledEvent;
import net.malisis.core.client.gui.util.shape.Point;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;

/**
 * GuiScreenProxy
 *
 * @author PaleoCrafter
 */
public class GuiScreenProxy extends GuiScreen implements Context
{

    private EventBus bus;

    private UIContainer container;

    private UITooltip tooltip;

    public GuiScreenProxy()
    {
        this(null);
    }

    public GuiScreenProxy(UIContainer container)
    {
        this.container = container;
        this.bus = new EventBus();
    }

    @Override
    public void initGui()
    {
        super.initGui();
        GuiManager.setActiveContext(this);
        if (container != null)
        {
            container.initComponent();
        }
    }

    @Override
    public void updateScreen()
    {
        if (container != null)
        {
            ScaledResolution scaledresolution = new ScaledResolution(this.mc.gameSettings, this.mc.displayWidth, this.mc.displayHeight);
            int scaledWidth = scaledresolution.getScaledWidth();
            int scaledHeight = scaledresolution.getScaledHeight();
            int x = Mouse.getX() * scaledWidth / this.mc.displayWidth;
            int y = scaledHeight - Mouse.getY() * scaledHeight / this.mc.displayHeight - 1;
            int dWheel = Mouse.getDWheel() / 120;

            if (dWheel != 0)
            {
                publish(new MouseScrolledEvent(dWheel, x, y));
            }
            container.update(x, y);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawWorldBackground(1);
        if (container != null)
        {
            int xStart = (width - container.getWidth()) / 2;
            int yStart = (height - container.getHeight()) / 2;
            container.setScreenPosition(new Point(xStart, yStart));
            container.drawBackground(mouseX, mouseY);
            container.draw(mouseX, mouseY);
            container.drawForeground(mouseX, mouseY);
            container.drawTooltip(mouseX, mouseY);
        }
    }

    @Override
    protected void mouseClicked(int x, int y, int button)
    {
        super.mouseClicked(x, y, button);
        publish(new MouseClickedEvent(x, y, button));
    }

    @Override
    protected void keyTyped(char keyChar, int keyCode)
    {
        super.keyTyped(keyChar, keyCode);
        publish(new KeyTypedEvent(keyChar, keyCode));
    }

    @Override
    public void onGuiClosed()
    {
        super.onGuiClosed();
        if (container != null)
            container.dispose();
        GuiManager.setActiveContext(null);
    }

    @Override
    public UITooltip getTooltip()
    {
        if (tooltip == null)
            tooltip = new UITooltip();
        return tooltip;
    }

    @Override
    public void publish(GuiEvent event)
    {
        bus.post(event);
    }

    @Override
    public void register(Object listener)
    {
        bus.register(listener);
    }

    @Override
    public void unregister(Object object)
    {
        try
        {
            bus.unregister(object);
        }
        catch (Throwable t)
        {

        }
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }
}
