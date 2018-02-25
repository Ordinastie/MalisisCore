/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 Ordinastie
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

package net.malisis.core.client.gui.text;

import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.renderer.font.MalisisFont;

/**
 * Interface for classes that use GuiText.<br>
 * Proxies GuiText setters and getters to the implementing class.
 *
 * @author Ordinastie
 *
 */
public interface IGuiTextProxy
{
	/**
	 * Sets the {@link GuiText}.
	 *
	 * @param text the new text
	 */
	public void setGuiText(GuiText text);

	/**
	 * Gets the {@link GuiText}.
	 *
	 * @return the text
	 */
	public GuiText getGuiText();

	/**
	 * Sets the text of this {@link UILabel}.<br>
	 *
	 * @param text the new text
	 */
	public default void setText(String text)
	{
		safeGetText(this).setText(text);
	}

	/**
	 * Gets processed text, translated and with resolved parameters.
	 *
	 * @return the text
	 */
	public default String getText()
	{
		return safeGetText(this).getText();
	}

	/**
	 * Sets whether the text should be translated.
	 *
	 * @param translate the translate
	 */
	public default void setTranslated(boolean translate)
	{
		safeGetText(this).setTranslated(translate);
	}

	/**
	 * Checks if the text should be translated.
	 *
	 * @return true, if is translated
	 */
	public default boolean isTranslated()
	{
		return safeGetText(this).isTranslated();
	}

	/**
	 * Sets the wrap size for the text.<br>
	 * Has no effect if this {@link GuiText} is not multilines.
	 *
	 * @param wrapSize the new wrap size
	 */
	public default void setWrapSize(int wrapSize)
	{
		safeGetText(this).setWrapSize(wrapSize);
	}

	/**
	 * Gets the wrap size for the text.
	 *
	 * @return the wrap size
	 */
	public default int getWrapSize()
	{
		return safeGetText(this).getWrapSize();
	}

	public default void setLineSpacing(int spacing)
	{
		safeGetText(this).setLineSpacing(spacing);
	}

	/**
	 * Gets the line spacing for the text.
	 *
	 * @return the line spacing
	 */
	public default int getLineSpacing()
	{
		return safeGetText(this).getLineSpacing();
	}

	/**
	 * Sets the font to use to render.
	 *
	 * @param font the new font
	 */
	public default void setFont(MalisisFont font)
	{
		safeGetText(this).setFont(font);
	}

	/**
	 * Gets the font used to render the text.
	 *
	 * @return the font
	 */
	public default MalisisFont getFont()
	{
		return safeGetText(this).getFont();
	}

	/**
	 * Sets the font options to use to render.
	 *
	 * @param fontOptions the new font options
	 */
	public default void setFontOptions(FontOptions fontOptions)
	{
		safeGetText(this).setFontOptions(fontOptions);
	}

	/**
	 * Gets the font options used to render.
	 *
	 * @return the font options
	 */
	public default FontOptions getFontOptions()
	{
		return safeGetText(this).getFontOptions();
	}

	/**
	 * Sets whether the text is multiline.
	 *
	 * @param multiLine the new multiline
	 */
	public default void setMultiline(boolean multiLine)
	{
		safeGetText(this).setMultiline(multiLine);
	}

	/**
	 * Checks if the text is multiline.
	 *
	 * @return true, if is multiline
	 */
	public default boolean isMultiLine()
	{
		return safeGetText(this).isMultiLine();
	}

	static GuiText safeGetText(IGuiTextProxy proxy)
	{
		GuiText gt = proxy.getGuiText();
		if (gt == null)
		{
			gt = new GuiText("");
			proxy.setGuiText(gt);
		}
		return gt;
	}

}
