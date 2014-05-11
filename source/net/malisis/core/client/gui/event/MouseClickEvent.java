package net.malisis.core.client.gui.event;

import net.malisis.core.client.gui.util.MouseButton;

/**
 * MouseClickEvent
 *
 * @author PaleoCrafter
 */
public class MouseClickEvent extends GuiEvent
{

    private int x, y;
    private MouseButton button;

    public MouseClickEvent(int x, int y, MouseButton button) {
        this.x = x;
        this.y = y;
        this.button = button;
    }

    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }

    public MouseButton getButton()
    {
        return button;
    }
}
