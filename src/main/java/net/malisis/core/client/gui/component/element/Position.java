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

public class Position
{
	public interface IPosition
	{
		public default void setOwner(UIComponent<?> component)
		{}

		public int x();

		public int y();
	}

	public interface XFunction extends ToIntFunction<UIComponent<?>>
	{
	}

	public interface YFunction extends ToIntFunction<UIComponent<?>>
	{
	}

	public static class DynamicPosition implements IPosition
	{
		private final XFunction x;
		private final YFunction y;
		private UIComponent<?> owner;

		public DynamicPosition(XFunction x, YFunction y)
		{
			this.x = x;
			this.y = y;
		}

		//call from component.setPosition()
		@Override
		public void setOwner(@Nonnull UIComponent<?> component)
		{
			this.owner = checkNotNull(component);
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

	public static IPosition zero()
	{
		return Position.of(0, 0);
	}

	//absolute position
	public static IPosition of(int x, int y)
	{
		return x(x).y(y);
	}

	public static PositionFactory x(int x)
	{
		return new PositionFactory(Positions.x(x));
	}

	//relative position
	public static PositionFactory leftOf(@Nonnull UIComponent<?> component)
	{
		return leftOf(component, 0);
	}

	public static PositionFactory leftOf(@Nonnull UIComponent<?> component, int spacing)
	{
		return new PositionFactory(Positions.leftOf(component, spacing));
	}

	public static PositionFactory rightOf(@Nonnull UIComponent<?> component)
	{
		return rightOf(component, 0);
	}

	public static PositionFactory rightOf(@Nonnull UIComponent<?> component, int spacing)
	{
		return new PositionFactory(Positions.rightOf(component, spacing));
	}

	//aligned inside parent container
	public static PositionFactory leftAligned()
	{
		return leftAligned(0);
	}

	public static PositionFactory leftAligned(int spacing)
	{
		return x(0);
	}

	public static PositionFactory rightAligned()
	{
		return rightAligned(0);
	}

	public static PositionFactory rightAligned(int spacing)
	{
		return new PositionFactory(Positions.rightAligned(spacing));
	}

	public static PositionFactory centered()
	{
		return centered(0);
	}

	public static PositionFactory centered(int offset)
	{
		return new PositionFactory(Positions.centered(offset));
	}

	//aligned relative to another component
	public static PositionFactory leftAlignedTo(@Nonnull UIComponent<?> other)
	{
		return leftAlignedTo(other, 0);
	}

	public static PositionFactory leftAlignedTo(@Nonnull UIComponent<?> other, int offset)
	{
		checkNotNull(other);
		return new PositionFactory(Positions.leftAlignedTo(other, offset));
	}

	public static PositionFactory rightAlignedTo(@Nonnull UIComponent<?> other)
	{
		return rightAlignedTo(other, 0);
	}

	public static PositionFactory rightAlignedTo(@Nonnull UIComponent<?> other, int offset)
	{
		checkNotNull(other);
		return new PositionFactory(Positions.rightAlignedTo(other, offset));
	}

	public static PositionFactory centeredTo(@Nonnull UIComponent<?> other)
	{
		return centeredTo(other, 0);
	}

	public static PositionFactory centeredTo(@Nonnull UIComponent<?> other, int offset)
	{
		checkNotNull(other);
		return new PositionFactory(Positions.centeredTo(other, offset));
	}

	public static class PositionFactory
	{
		private XFunction xFunction;
		private YFunction yFunction;

		public PositionFactory(XFunction xFunction)
		{
			this.xFunction = xFunction;
		}

		//absolute position
		public IPosition y(int y)
		{
			yFunction = Positions.y(y);
			return build();
		}

		//relative postion
		public IPosition above(@Nonnull UIComponent<?> component)
		{
			return above(component, 0);
		}

		public IPosition above(@Nonnull UIComponent<?> component, int spacing)
		{
			yFunction = Positions.above(component, spacing);
			return build();
		}

		public IPosition below(@Nonnull UIComponent<?> component)
		{
			return below(component, 0);
		}

		public IPosition below(@Nonnull UIComponent<?> component, int spacing)
		{
			yFunction = Positions.below(component, spacing);
			return build();
		}

		public IPosition topAligned()
		{
			return topAligned(0);
		}

		public IPosition topAligned(int spacing)
		{
			return y(0);
		}

		public IPosition bottomAligned()
		{
			return bottomAligned(0);
		}

		public IPosition bottomAligned(int spacing)
		{
			yFunction = Positions.bottomAligned(spacing);
			return build();
		}

		public IPosition middleAligned()
		{
			return middleAligned(0);
		}

		public IPosition middleAligned(int offset)
		{
			yFunction = Positions.middleAligned(offset);
			return build();
		}

		//aligned relative to another component
		public IPosition topAlignedTo(@Nonnull UIComponent<?> other)
		{
			return topAlignedTo(other, 0);
		}

		public IPosition topAlignedTo(@Nonnull UIComponent<?> other, int offset)
		{
			yFunction = Positions.topAlignedTo(other, offset);
			return build();
		}

		public IPosition bottomAlignedTo(@Nonnull UIComponent<?> other)
		{
			return bottomAlignedTo(other, 0);
		}

		public IPosition bottomAlignedTo(@Nonnull UIComponent<?> other, int offset)
		{
			yFunction = Positions.bottomAlignedTo(other, offset);
			return build();
		}

		public IPosition middleAlignedTo(@Nonnull UIComponent<?> other)
		{
			return middleAlignedTo(other, 0);
		}

		public IPosition middleAlignedTo(@Nonnull UIComponent<?> other, int offset)
		{
			yFunction = Positions.middleAlignedTo(other, offset);
			return build();
		}

		public IPosition build()
		{
			return new DynamicPosition(xFunction, yFunction);
		}
	}
}
