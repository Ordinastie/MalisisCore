package net.malisis.core.client.gui.util;

/**
 * MouseButton
 *
 * @author PaleoCrafter
 */
public enum MouseButton
{

    LEFT, RIGHT, MIDDLE, UNKNOWN;

    public static MouseButton[] DEFAULT = {
            LEFT, RIGHT, MIDDLE
    };

    public static MouseButton getButton(int id)
    {
        if (id < DEFAULT.length && id >= 0)
            return DEFAULT[id];
        return UNKNOWN;
    }

    public boolean isLeft()
    {
        return this == LEFT;
    }

    public boolean isRight()
    {
        return this == RIGHT;
    }

    public boolean isMiddle()
    {
        return this == MIDDLE;
    }

}
