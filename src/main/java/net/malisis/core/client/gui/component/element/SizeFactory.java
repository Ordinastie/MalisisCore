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

import java.util.function.ToIntFunction;

import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.element.Size.DynamicSize;
import net.malisis.core.client.gui.component.element.Size.ISize;

/**
 * @author Ordinastie
 *
 */
public class SizeFactory
{
	private ToIntFunction<UIComponent<?>> widthFunction;
	private ToIntFunction<UIComponent<?>> heightFunction;

	public SizeFactory(ToIntFunction<UIComponent<?>> widthFunction)
	{
		this.widthFunction = widthFunction;
	}

	public ISize height(int height)
	{
		heightFunction = owner -> height;
		return build();
	}

	public ISize relativeHeight(float height)
	{
		heightFunction = owner -> {
			UIComponent<?> parent = owner.getParent();
			if (parent == null)
				return 0;
			return (int) ((parent.size().height() - Padding.of(parent).vertical()) * height);
		};
		return build();
	}

	public ISize heightRelativeTo(float height, UIComponent<?> other)
	{
		heightFunction = owner -> {
			if (other == null)
				return 0;
			return (int) (other.size().height() * height);
		};

		return build();
	}

	private ISize build()
	{
		return new DynamicSize(widthFunction, heightFunction);
	}

}
