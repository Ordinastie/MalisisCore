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

package net.malisis.core.client.gui.element;

import static com.google.common.base.Preconditions.*;

import java.util.function.IntSupplier;

import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.content.IContentHolder;

/**
 * @author Ordinastie
 *
 */
public class Size
{
	public static final ISize ZERO = Size.of(0, 0);

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

	public static class FixedSize implements ISize
	{
		private int width;
		private int height;

		public FixedSize(int width, int height)
		{
			this.width = width;
			this.height = height;
		}

		@Override
		public int width()
		{
			return width;
		}

		@Override
		public int height()
		{
			return height;
		}

		@Override
		public String toString()
		{
			return width() + "x" + height();
		}
	}

	public static class FixedWidthSize implements ISize
	{
		private int width;
		private IntSupplier heightSupplier;

		public FixedWidthSize(int width, IntSupplier heightSupplier)
		{
			this.width = width;
			this.heightSupplier = heightSupplier;
		}

		@Override
		public int width()
		{
			return width;
		}

		@Override
		public int height()
		{
			return heightSupplier.getAsInt();
		}

		@Override
		public String toString()
		{
			return width() + "," + height();
		}
	}

	public static class FixedHeightSize implements ISize
	{
		private IntSupplier widthSupplier;
		private int height;

		public FixedHeightSize(IntSupplier widthSupplier, int height)
		{
			this.widthSupplier = widthSupplier;
			this.height = height;
		}

		@Override
		public int width()
		{
			return widthSupplier.getAsInt();
		}

		@Override
		public int height()
		{
			return height;
		}

		@Override
		public String toString()
		{
			return width() + "," + height();
		}
	}

	public static class DynamicSize implements ISize
	{
		private final IntSupplier width;
		private final IntSupplier height;

		public DynamicSize(IntSupplier width, IntSupplier height)
		{
			this.width = checkNotNull(width);
			this.height = checkNotNull(height);
		}

		@Override
		public int width()
		{
			return width.getAsInt();
		}

		@Override
		public int height()
		{
			return height.getAsInt();
		}

		@Override
		public String toString()
		{
			return width() + "x" + height();
		}
	}

	//Builder
	public static SizeBuilder builder()
	{
		return new SizeBuilder();
	}

	public static SizeBuilder of(UIComponent owner)
	{
		return new SizeBuilder(owner);
	}

	//Size shortcuts
	public static ISize of(int x, int y)
	{
		return new FixedSize(x, y);
	}

	public static ISize of(int x, IntSupplier ySupplier)
	{
		return new FixedWidthSize(x, ySupplier);
	}

	public static ISize of(IntSupplier xSupplier, int y)
	{
		return new FixedHeightSize(xSupplier, y);
	}

	public static ISize of(IntSupplier xSupplier, IntSupplier ySupplier)
	{
		return new DynamicSize(xSupplier, ySupplier);
	}

	public static <T extends ISized & IChild<UIComponent>> ISize relativeTo(T other)
	{
		return new DynamicSize(Sizes.widthRelativeTo(other, 1.0F, 0), Sizes.heightRelativeTo(other, 1.0F, 0));
	}

	public static <T extends ISized & IChild<UIComponent>> ISize inherited(T owner)
	{
		return new DynamicSize(Sizes.parentWidth(owner, 1.0F, 0), Sizes.parentHeight(owner, 1.0F, 0));
	}

	public static ISize sizeOfContent(IContentHolder owner)
	{
		return sizeOfContent(owner, 0, 0);
	}

	public static ISize sizeOfContent(IContentHolder owner, int widthOffset, int heightOffset)
	{
		return new DynamicSize(Sizes.widthOfContent(owner, widthOffset), Sizes.heightOfContent(owner, heightOffset));
	}

}
