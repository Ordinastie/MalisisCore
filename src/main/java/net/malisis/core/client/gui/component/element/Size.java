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

import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.element.Padding.IPadded;

/**
 * @author Ordinastie
 *
 */
public interface Size
{
	public static final Size ZERO = Size.of(0, 0);

	public int width();

	public int height();

	public static class FixedSize implements Size
	{
		private final int width;
		private final int height;

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
	}

	public static class RelativeSize implements Size
	{
		private final UIComponent<?> owner;
		private int width;
		private int height;
		private UIComponent<?> relCompWidth;
		private UIComponent<?> relCompHeight;
		private float relativeWidth;
		private float relativeHeight;

		public RelativeSize(UIComponent<?> owner, int width, int height, UIComponent<?> relCompWidth, float relativeWidth, UIComponent<?> relCompHeight, float relativeHeight)
		{
			this.owner = owner;
			this.width = width;
			this.height = height;
			this.relCompWidth = relCompWidth;
			this.relativeWidth = relativeWidth;
			this.relCompHeight = relCompHeight;
			this.relativeHeight = relativeHeight;
		}

		@Override
		public int width()
		{
			if (relativeWidth == 0)
				return width;

			UIComponent<?> c = relCompWidth;
			int p = 0;

			if (c == null) //use parent if relative comp is null
			{
				c = owner.getParent();
				if (c instanceof IPadded) //assume size without padding if available
					p = ((IPadded) c).getPadding().horizontal();
			}
			if (c == null)
				return 0;
			return (int) ((c.size().width() - p) * relativeWidth);
		}

		@Override
		public int height()
		{
			if (relativeHeight == 0)
				return height;

			UIComponent<?> c = relCompHeight;
			int p = 0;

			if (c == null) //use parent if relative comp is null
			{
				c = owner.getParent();
				if (c instanceof IPadded) //assume size without padding if available
					p = ((IPadded) c).getPadding().vertical();
			}
			if (c == null)
				return 0;
			return (int) ((c.size().height() - p) * relativeHeight);
		}
	}

	public static Size of(int width, int height)
	{
		return new FixedSize(width, height);
	}

	public static SizeBuilder of(UIComponent<?> owner)
	{
		return new SizeBuilder(owner);
	}

	public static class SizeBuilder
	{
		private final UIComponent<?> owner;
		private int width;
		private int height;
		private UIComponent<?> relCompWidth;
		private UIComponent<?> relCompHeight;
		private float relativeWidth;
		private float relativeHeight;

		public SizeBuilder(UIComponent<?> owner)
		{
			this.owner = owner;
		}

		public SizeBuilder width(int width)
		{
			this.width = width;
			return this;
		}

		public SizeBuilder height(int height)
		{
			this.height = height;
			return this;
		}

		public SizeBuilder relativeWidth(float width)
		{
			return relativeWidth(width, null);
		}

		public SizeBuilder relativeWidth(float width, UIComponent<?> relativeTo)
		{
			this.relativeWidth = width;
			this.relCompWidth = relativeTo;
			return this;
		}

		public SizeBuilder relativeHeight(float height)
		{
			return relativeHeight(height, null);
		}

		public SizeBuilder relativeHeight(float height, UIComponent<?> relativeTo)
		{
			this.relativeHeight = height;
			this.relCompHeight = relativeTo;
			return this;
		}

		public Size build()
		{
			if (relativeWidth == 0 && relativeHeight == 0)
				return new FixedSize(width, height);

			return new RelativeSize(owner, width, height, relCompWidth, relativeWidth, relCompHeight, relativeHeight);
		}
	}

	public interface ISized
	{
		public Size getSize();
	}
}
