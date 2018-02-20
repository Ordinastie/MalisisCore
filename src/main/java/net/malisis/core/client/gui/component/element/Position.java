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

/**
 * @author Ordinastie
 *
 */
public interface Position extends IComponentElement
{
	public static final Position ZERO = Position.of(0, 0);

	public int x();

	public int y();

	public static class DynamicPosition implements Position
	{
		private final ToIntFunction<UIComponent<?>> x;
		private final ToIntFunction<UIComponent<?>> y;
		private UIComponent<?> owner;

		public DynamicPosition(ToIntFunction<UIComponent<?>> x, ToIntFunction<UIComponent<?>> y)
		{
			this.x = x;
			this.y = y;
		}

		//call from component.setPosition()
		@Override
		public void setOwner(UIComponent<?> component)
		{
			this.owner = component;
		}

		@Override
		public int x()
		{
			return x.applyAsInt(owner);
		}

		@Override
		public int y()
		{
			return y.applyAsInt(owner);
		}
	}

	public static Position zero()
	{
		return Position.of(0, 0);
	}

	public static Position of(int x, int y)
	{
		return new PositionFactory().x(x).y(y).build();
	}

	public static PositionFactory builder()
	{
		return new PositionFactory();
	}
	/*
		public static class AbsolutePosition implements Position
		{
			protected final int x, y;
	
			protected AbsolutePosition(int x, int y, int anchor)
			{
				Position.of(x, y);
				Position.x().y().get();
				Position.this.x = x;
				this.y = y;
			}
	
			@Override
			public int x()
			{
				return x;
			}
	
			@Override
			public int y()
			{
				return y;
			}
		}
	
		public static class AlignedPosition implements Position
		{
			protected final UIComponent<?> owner;
			protected final int anchor;
			protected final int xOffset, yOffset;
	
			protected AlignedPosition(UIComponent<?> component, int anchor, int xOffset, int yOffset)
			{
				this.owner = component;
				this.anchor = anchor;
				this.xOffset = xOffset;
				this.yOffset = yOffset;
			}
	
			@Override
			public int x()
			{
				int x = xOffset;
				int w = owner.getParent().size().width() - owner.size().width();
				int a = Anchor.horizontal(anchor);
				if (a == Anchor.CENTER)
					x += w / 2;
				else if (a == Anchor.RIGHT)
					x += w;
				return x;
			}
	
			@Override
			public int y()
			{
				int y = yOffset;
				int h = owner.getParent().size().height() - owner.size().height();
				int a = Anchor.vertical(anchor);
				if (a == Anchor.MIDDLE)
					y += h / 2;
				else if (a == Anchor.BOTTOM)
					y += h;
				return y;
			}
	
			public int anchor()
			{
				return anchor;
			}
	
		}
	
		public static class RelativePosition implements Position
		{
			static enum Direction
			{
				ABOVE,
				BELOW,
				LEFT,
				RIGHT
			}
	
			private final UIComponent<?> owner;
			private final UIComponent<?> other;
			private final Direction direction;
			private final int spacing;
	
			public RelativePosition(UIComponent<?> owner, UIComponent<?> other, Direction direction, int spacing)
			{
				this.owner = owner;
				this.other = other;
				this.direction = direction;
				this.spacing = spacing;
			}
	
			@Override
			public int x()
			{
				switch (direction)
				{
					case LEFT:
						return other.position().x() - owner.size().width() - spacing;
					case RIGHT:
						return other.position().x() + other.size().width() + spacing;
					default:
						return other.position().x();
				}
			}
	
			@Override
			public int y()
			{
				switch (direction)
				{
					case ABOVE:
						return other.position().y() - owner.size().height() - spacing;
					case BELOW:
						return other.position().y() + owner.size().height() + spacing;
					default:
						return other.position().y();
				}
			}
		}
	
		public static Position of(int x, int y)
		{
			return new AbsolutePosition(x, y, 0);
		}
	
		public static PositionFactory of(UIComponent<?> owner)
		{
			return new PositionFactory(owner);
		}
	
		public static class PositionFactory
		{
			private UIComponent<?> owner;
	
			public PositionFactory(UIComponent<?> owner)
			{
				this.owner = owner;
			}
	
			public Position align(int anchor, int xOffset, int yOffset)
			{
				return new AlignedPosition(owner, anchor, xOffset, yOffset);
			}
	
			public Position above(UIComponent<?> component, int ySpacing)
			{
				return new RelativePosition(owner, component, Direction.ABOVE, ySpacing);
			}
	
			public Position below(UIComponent<?> component, int ySpacing)
			{
				return new RelativePosition(owner, component, Direction.BELOW, ySpacing);
			}
	
			public Position leftOf(UIComponent<?> component, int xSpacing)
			{
				return new RelativePosition(owner, component, Direction.LEFT, xSpacing);
			}
	
			public Position rightOf(UIComponent<?> component, int xSpacing)
			{
				return new RelativePosition(owner, component, Direction.RIGHT, xSpacing);
			}
		}
	
		*/
}
