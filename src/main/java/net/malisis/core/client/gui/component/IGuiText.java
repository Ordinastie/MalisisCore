/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Ordinastie
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

package net.malisis.core.client.gui.component;

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.renderer.font.FontRenderOptions;
import net.malisis.core.renderer.font.MalisisFont;

/**
 * Defines a {@link UIComponent} that uses text to be rendered on the {@link MalisisGui}.<br>
 * The {@code UIComponent} should allow null {@link MalisisFont} and {@link FontRenderOptions} values so that they default to
 * {@link GuiRenderer} default values when rendered.<br>
 * As a consequence of this, {@link GuiRenderer#getFont(IGuiText)} and {@link GuiRenderer#getFontRendererOptions(IGuiText)} should be used
 * when needed instead of direct access.
 *
 * @author Ordinastie
 *
 */
public interface IGuiText<T>
{
	/**
	 * Gets the {@link MalisisFont}.
	 *
	 * @return the font
	 */
	public MalisisFont getFont();

	/**
	 * Gets the {@link FontRenderOptions}.
	 *
	 * @return the font renderer options
	 */
	public FontRenderOptions getFontRendererOptions();

	/**
	 * Sets the {@link MalisisFont} and {@link FontRenderOptions}.
	 *
	 * @param font the new font
	 * @param fro the fro
	 */
	public T setFont(MalisisFont font, FontRenderOptions fro);
}
