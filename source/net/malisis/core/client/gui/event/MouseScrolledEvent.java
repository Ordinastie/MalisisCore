package net.malisis.core.client.gui.event;

/**
 * MouseScrolledEvent
 *
 * @author PaleoCrafter
 */
public class MouseScrolledEvent extends GuiEvent
{

    private final int dir;
    private final int mouseX;
    private final int mouseY;

    public MouseScrolledEvent(int dir, int mouseX, int mouseY)
    {
        this.dir = dir;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    public int getDir()
    {
        return dir;
    }

    public int getMouseX()
    {
        return mouseX;
    }

    public int getMouseY()
    {
        return mouseY;
    }
}
