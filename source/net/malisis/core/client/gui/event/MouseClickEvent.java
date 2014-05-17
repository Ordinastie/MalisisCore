package net.malisis.core.client.gui.event;

import net.malisis.core.client.gui.util.MouseButton;
import net.malisis.core.client.gui.util.shape.Point;

/**
 * MouseClickEvent
 *
 * @author PaleoCrafter
 */
public class MouseClickEvent extends GuiEvent
{

    private Point position;
    private int button;

    public MouseClickEvent(int x, int y, int button)
    {
        this.position = new Point(x, y);
        this.button = button;
    }

    public Point getPosition()
    {
        return position;
    }

    public int getButtonCode()
    {
        return button;
    }

    public MouseButton getButton()
    {
        return MouseButton.getButton(button);
    }
}
