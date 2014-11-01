package net.malisis.core.client.gui.component.interaction;

import net.malisis.core.client.gui.MalisisGui;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

public class UIPasswordField extends UITextField
{
    private char passwordChar = '*';
    private StringBuilder password = new StringBuilder();

    public UIPasswordField(MalisisGui gui, int width)
    {
        super(gui, width);
    }

    public UIPasswordField(MalisisGui gui, int width, char passwordChar)
    {
        super(gui, width);
        this.passwordChar = passwordChar;
    }

    /**
     * Gets the masking character used when rendering text
     *
     * @return the masking character
     */
    public char getPasswordCharacter()
    {
        return passwordChar;
    }

    /**
     * Sets the masking character to use when rendering text
     *
     * @param passwordChar the masking character to use
     */
    public void setPasswordCharacter(char passwordChar)
    {
        this.passwordChar = passwordChar;
    }

    @Override
    public String getText()
    {
        return password.toString();
    }

    @Override
    public void addText(String text)
    {
        password = password.insert(getCursorPosition(), text);
        if (!validateText(password.toString()))
        {
            return;
        }
        super.addText(text.replaceAll("(?s).", String.valueOf(passwordChar)));
    }

    @Override
    public void setText(String text)
    {
        password.setLength(0);
        password.append(text);
        if (!validateText(password.toString()))
        {
            return;
        }
        super.setText(text.replaceAll("(?s).", String.valueOf(passwordChar)));
    }

    @Override
    public void deleteSelectedText()
    {
        final int start = Math.min(selectionPosition, getCursorPosition());
        final int end = Math.max(selectionPosition, getCursorPosition());
        password.delete(start, end);
        super.deleteSelectedText();
    }

    @Override
    protected boolean handleCtrlKeyDown(int keyCode)
    {
        return GuiScreen.isCtrlKeyDown() && !(keyCode == Keyboard.KEY_C || keyCode == Keyboard.KEY_X) && super.handleCtrlKeyDown(keyCode);
    }
}
