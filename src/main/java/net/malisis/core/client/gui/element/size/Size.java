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

package net.malisis.core.client.gui.element.size;

import java.util.function.IntSupplier;

import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.content.IContentHolder;
import net.malisis.core.client.gui.element.IChild;

/**
 * @author Ordinastie
 *
 */
public class Size
{
	public static boolean CACHE_SIZE = true;
	public static final ISize ZERO = Size.of(0, 0);
	public static final ISize DEFAULT = Size.of(16, 16);

	public interface ISized
	{
		public default ISize size()
		{
			return Size.ZERO;
		}
	}

	public interface ISize
	{
		public int width();

		public int height();

		public default ISize offset(int x, int y)
		{
			return plus(Size.of(x, y));
		}

		public default ISize plus(ISize other)
		{
			if (other == null || other == ZERO)
				return this;
			if (this == ZERO)
				return other;

			return Size.of(() -> width() + other.width(), () -> height() + other.height());
		}

		public default ISize minus(ISize other)
		{
			if (other == null || other == ZERO)
				return this;
			if (this == ZERO)
				return other;

			return Size.of(() -> width() - other.width(), () -> height() - other.height());
		}
	}

	public static class DynamicSize implements ISize
	{
		private int cachedWidth;
		private int cachedHeight;

		private int lastFrameWidth = -1;
		private int lastFrameHeight = -1;

		private final int width;
		private final int height;
		private final IntSupplier widthFunction;
		private final IntSupplier heightFunction;

		DynamicSize(int width, int height, IntSupplier widthFunction, IntSupplier heightFunction)
		{
			this.width = width;
			this.height = height;
			this.widthFunction = widthFunction;
			this.heightFunction = heightFunction;
		}

		private void updateWidth()
		{
			if (lastFrameWidth == MalisisGui.counter && CACHE_SIZE)
				return;
			cachedWidth = widthFunction.getAsInt();
			lastFrameWidth = MalisisGui.counter;
		}

		private void updateHeight()
		{
			if (lastFrameHeight == MalisisGui.counter && CACHE_SIZE)
				return;
			cachedHeight = heightFunction.getAsInt();
			lastFrameHeight = MalisisGui.counter;
		}

		@Override
		public int width()
		{
			if (widthFunction == null)
				return width;
			updateWidth();
			return cachedWidth;
		}

		@Override
		public int height()
		{
			if (heightFunction == null)
				return height;
			updateHeight();
			return cachedHeight;
		}

		@Override
		public String toString()
		{
			return width() + "x" + height();
		}
	}

	//Size shortcuts
	public static ISize of(int width, int height)
	{
		return new DynamicSize(width, height, null, null);
	}

	public static ISize of(int width, IntSupplier heightSupplier)
	{
		return new DynamicSize(width, 0, null, heightSupplier);
	}

	public static ISize of(IntSupplier widthSupplier, int height)
	{
		return new DynamicSize(0, height, widthSupplier, null);
	}

	public static ISize of(IntSupplier widthSupplier, IntSupplier heightSupplier)
	{
		return new DynamicSize(0, 0, widthSupplier, heightSupplier);
	}

	public static <T extends ISized & IChild<UIComponent>> ISize relativeTo(T other)
	{
		return new DynamicSize(0, 0, Sizes.widthRelativeTo(other, 1.0F, 0), Sizes.heightRelativeTo(other, 1.0F, 0));
	}

	public static <T extends ISized & IChild<UIComponent>> ISize inherited(T owner)
	{
		return new DynamicSize(0, 0, Sizes.parentWidth(owner, 1.0F, 0), Sizes.parentHeight(owner, 1.0F, 0));
	}

	public static ISize sizeOfContent(IContentHolder owner)
	{
		return sizeOfContent(owner, 0, 0);
	}

	public static ISize sizeOfContent(IContentHolder owner, int widthOffset, int heightOffset)
	{
		return new DynamicSize(0, 0, Sizes.widthOfContent(owner, widthOffset), Sizes.heightOfContent(owner, heightOffset));
	}

}
