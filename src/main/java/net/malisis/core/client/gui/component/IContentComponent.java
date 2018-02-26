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

package net.malisis.core.client.gui.component;

import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.element.Size.ISize;
import net.malisis.core.client.gui.text.GuiText;
import net.malisis.core.client.gui.text.IGuiText;
import net.malisis.core.client.gui.text.IGuiTextProxy;

/**
 * @author Ordinastie
 *
 */
public interface IContentComponent extends IGuiTextProxy, IContentSize
{
	/**
	 * Sets the content component.<br>
	 *
	 * @param content the new content
	 */
	public void setContent(UIComponent<?> content);

	/**
	 * Gets the content of the component
	 *
	 * @return the content
	 */
	public UIComponent<?> getContent();

	@Override
	public default void setGuiText(GuiText text)
	{
		UIComponent<?> content = getContent();
		if (!(content instanceof IGuiText))
			throw new IllegalStateException("Component doesn't have GuiText capable content");
		((IGuiText) content).setGuiText(text);
	}

	@Override
	public default GuiText getGuiText()
	{
		UIComponent<?> content = getContent();
		if (!(content instanceof IGuiText))
			throw new IllegalStateException("Component doesn't have GuiText capable content");
		return ((IGuiText) content).getGuiText();
	}

	@Override
	public default ISize contentSize()
	{
		return getContent().size();
	}

	@Override
	public default GuiText getOrCreate()
	{
		if (getContent() == null)
			setContent(new UILabel(((UIComponent<?>) this).getGui()));
		return getGuiText();
	}
}
