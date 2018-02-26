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

import java.util.List;
import java.util.function.BooleanSupplier;

import org.apache.commons.lang3.tuple.Pair;

import net.malisis.core.renderer.font.FontOptions;

/**
 * @author Ordinastie
 *
 */
public class PredicatedFontOptions extends FontOptions
{
	protected final FontOptions base;
	protected final List<Pair<BooleanSupplier, FontOptions>> suppliers;

	public PredicatedFontOptions(FontOptions base, List<Pair<BooleanSupplier, FontOptions>> suppliers)
	{
		super(0, 0, false, false, false, false, false, false, false);
		this.base = base;
		this.suppliers = suppliers;
	}

	private FontOptions get()
	{
		return suppliers.stream().filter(p -> p.getLeft().getAsBoolean()).map(Pair::getRight).findFirst().orElse(base);
	}

	/**
	 * Gets the font scale for this {@link FontOptions}.
	 *
	 * @return the font scale
	 */
	@Override
	public float getFontScale()
	{
		return get().getFontScale();
	}

	/**
	 * Checks if this {@link FontOptions} is bold.
	 *
	 * @return true, if is bold
	 */
	@Override
	public boolean isBold()
	{
		return get().isBold();
	}

	/**
	 * Checks if this {@link FontOptions} is italic.
	 *
	 * @return true, if is italic
	 */
	@Override
	public boolean isItalic()
	{
		return get().isItalic();
	}

	/**
	 * Checks if this {@link FontOptions} is underlined.
	 *
	 * @return true, if is underline
	 */
	@Override
	public boolean isUnderline()
	{
		return get().isBold();
	}

	/**
	 * Checks if this {@link FontOptions} is strikethrough.
	 *
	 * @return true, if is strikethrough
	 */
	@Override
	public boolean isStrikethrough()
	{
		return get().isStrikethrough();
	}

	/**
	 * Checks if this {@link FontOptions} is obfuscated.
	 *
	 * @return true, if is obfuscated
	 */
	@Override
	public boolean isObfuscated()
	{
		return get().isObfuscated();
	}

	/**
	 * Checks whether draw shadow is enabled for this {@link FontOptions}.
	 *
	 * @return true, if successful
	 */
	@Override
	public boolean hasShadow()
	{
		return get().hasShadow();
	}

	/**
	 * Gets the color for this {@link FontOptions}.
	 *
	 * @return the color
	 */
	@Override
	public int getColor()
	{
		return get().getColor();
	}

	/**
	 * Checks if formatting is disabled (formatting character are renderer literally).
	 *
	 * @return true, if is formatting disabled
	 */
	@Override
	public boolean isFormattingDisabled()
	{
		return get().isFormattingDisabled();
	}

	/**
	 * Checks if the text should be translated before rendering.
	 *
	 * @return true, if successful
	 */
	@Override
	public boolean shouldTranslate()
	{
		return get().shouldTranslate();
	}

}
