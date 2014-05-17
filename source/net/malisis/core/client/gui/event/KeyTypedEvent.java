package net.malisis.core.client.gui.event;

/**
 * KeyTypedEvent
 *
 * @author PaleoCrafter
 */
public class KeyTypedEvent extends GuiEvent
{

    private final char keyChar;
    private final int keyCode;

    public KeyTypedEvent(char keyChar, int keyCode)
    {
        this.keyChar = keyChar;
        this.keyCode = keyCode;
    }

    public char getKeyChar()
    {
        return keyChar;
    }

    public int getKeyCode()
    {
        return keyCode;
    }
}
