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

package net.malisis.core.client.gui.component.element;

import static com.google.common.base.Preconditions.*;

import javax.annotation.Nonnull;

import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.element.Size.HeightFunction;
import net.malisis.core.client.gui.component.element.Size.WidthFunction;

/**
 * @author Ordinastie
 *
 */
public class Sizes
{
	public static WidthFunction relativeWidth(float width)
	{
		return owner -> {
			UIComponent<?> parent = owner.getParent();
			if (parent == null)
				return 0;
			return (int) ((parent.size().width() - Padding.of(parent).horizontal()) * width);
		};
	}

	public static WidthFunction widthRelativeTo(float width, @Nonnull UIComponent<?> other)
	{
		checkNotNull(other);
		return owner -> {
			return (int) (other.size().width() * width);
		};
	}

	public static HeightFunction relativeHeight(float height)
	{
		return owner -> {
			UIComponent<?> parent = owner.getParent();
			if (parent == null)
				return 0;
			return (int) ((parent.size().height() - Padding.of(parent).vertical()) * height);
		};
	}

	public static HeightFunction heightRelativeTo(float height, @Nonnull UIComponent<?> other)
	{
		checkNotNull(other);
		return owner -> {
			return (int) (other.size().height() * height);
		};
	}
}
