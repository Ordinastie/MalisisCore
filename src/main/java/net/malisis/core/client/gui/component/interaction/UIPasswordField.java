package net.malisis.core.client.gui.component.interaction;

import java.util.function.Function;

import org.lwjgl.input.Keyboard;

import net.malisis.core.client.gui.event.ComponentEvent;
import net.minecraft.client.gui.GuiScreen;

/**
 * The Class UIPasswordField.
 */
public class UIPasswordField extends UITextField
{

	/** Character to draw instead of real text. */
	private char passwordChar = '*';

	/** Actual stored password *. */
	private StringBuilder password = new StringBuilder();

	/**
	 * Instantiates a new {@link UIPasswordField}.
	 */
	public UIPasswordField()
	{
		super();
	}

	/**
	 * Instantiates a new {@link UIPasswordField}
	 *
	 * @param passwordChar the password char
	 */
	public UIPasswordField(char passwordChar)
	{
		this();
		this.passwordChar = passwordChar;
	}

	/**
	 * Gets the masking character used when rendering text.
	 *
	 * @return the masking character
	 */
	public char getPasswordCharacter()
	{
		return passwordChar;
	}

	/**
	 * Sets the masking character to use when rendering text.
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
		if (password == null)
			return "";
		return password.toString();
	}

	/**
	 * Updates the text from the password value
	 */
	protected void updateText()
	{
		this.text.setLength(0);
		this.text.append(password.toString().replaceAll("(?s).", String.valueOf(passwordChar)));
		guiText.setText(text.toString());
	}

	/**
	 * Adds the text.
	 *
	 * @param text the text
	 */
	@Override
	public void addText(String text)
	{
		if (selectingText)
			deleteSelectedText();

		final StringBuilder oldText = this.password;
		final String oldValue = oldText.toString();
		String newValue = oldText.insert(this.cursor.index, text).toString();

		if (this.filterFunction != null)
			newValue = this.filterFunction.apply(newValue);

		if (!fireEvent(new ComponentEvent.ValueChange<>(this, oldValue, newValue)))
			return;

		this.password = new StringBuilder(newValue);
		this.updateText();
		this.cursor.jumpBy(text.length());
	}

	/**
	 * Sets the text.
	 *
	 * @param text the new text
	 */
	@Override
	public void setText(String text)
	{
		if (password == null) //called from parent ctor
			return;
		if (filterFunction != null)
			text = filterFunction.apply(text);

		password.setLength(0);
		password.append(text);
		updateText();
		selectingText = false;
		if (focused)
			cursor.jumpToEnd();

	}

	@Override
	public void setFilter(Function<String, String> filterFunction)
	{
		this.filterFunction = filterFunction;
		this.password = new StringBuilder(this.filterFunction.apply(this.password.toString()));
	}

	/**
	 * Delete selected text.
	 */
	@Override
	public void deleteSelectedText()
	{
		if (!selectingText)
			return;

		int start = Math.min(selectionCursor.index, cursor.index);
		int end = Math.max(selectionCursor.index, cursor.index);

		password.delete(start, end);
		updateText();
		selectingText = false;
		cursor.jumpTo(start);
	}

	/**
	 * Handle ctrl key down.
	 *
	 * @param keyCode the key code
	 * @return true, if successful
	 */
	@Override
	protected boolean handleCtrlKeyDown(int keyCode)
	{
		return GuiScreen.isCtrlKeyDown() && !(keyCode == Keyboard.KEY_C || keyCode == Keyboard.KEY_X) && super.handleCtrlKeyDown(keyCode);
	}

	@Override
	public String getPropertyString()
	{
		return password + " > " + super.getPropertyString();
	}
}
