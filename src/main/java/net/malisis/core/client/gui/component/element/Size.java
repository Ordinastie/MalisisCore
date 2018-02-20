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

import java.util.function.ToIntFunction;

import javax.annotation.Nonnull;

import net.malisis.core.client.gui.component.UIComponent;

/**
 * @author Ordinastie
 *
 */
public interface Size
{
	public interface ISize
	{
		public default void setOwner(UIComponent<?> owner)
		{}

		public int width();

		public int height();
	}

	public static class DynamicSize implements ISize
	{
		private final ToIntFunction<UIComponent<?>> width;
		private final ToIntFunction<UIComponent<?>> height;
		private UIComponent<?> owner;

		public DynamicSize(ToIntFunction<UIComponent<?>> width, ToIntFunction<UIComponent<?>> height)
		{
			this.width = width;
			this.height = height;
		}

		@Override
		public void setOwner(@Nonnull UIComponent<?> owner)
		{
			this.owner = checkNotNull(owner);
		}

		@Override
		public int width()
		{
			return width.applyAsInt(owner);
		}

		@Override
		public int height()
		{
			return height.applyAsInt(owner);
		}
	}

	public static ISize of(int width, int height)
	{
		return width(width).height(height);
	}

	public static ISize inherited()
	{
		return relativeWidth(1.0F).relativeHeight(1.0F);
	}

	public static SizeFactory width(int width)
	{
		return new SizeFactory(owner -> width);
	}

	public static SizeFactory relativeWidth(float width)
	{
		return new SizeFactory(owner -> {
			UIComponent<?> parent = owner.getParent();
			if (parent == null)
				return 0;
			return (int) ((parent.size().width() - Padding.of(parent).horizontal()) * width);
		});
	}

	public static SizeFactory widthRelativeTo(float width, @Nonnull UIComponent<?> other)
	{
		checkNotNull(other);
		return new SizeFactory(owner -> {
			return (int) (other.size().width() * width);
		});
	}
}
