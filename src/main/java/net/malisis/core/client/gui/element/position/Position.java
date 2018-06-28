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

package net.malisis.core.client.gui.element.position;

import java.util.function.IntSupplier;

import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.control.IScrollable;
import net.malisis.core.client.gui.element.IChild;
import net.malisis.core.client.gui.element.IOffset;
import net.malisis.core.client.gui.element.Size.ISized;

/**
 * @author Ordinastie
 *
 */

public class Position
{
	public static final IPosition ZERO = Position.of(0, 0);

	public interface IPositioned
	{
		public default IPosition position()
		{
			return Position.ZERO;
		}
	}

	public interface IPosition
	{
		public int x();

		public int y();

		public default IPosition offset(int x, int y)
		{
			return plus(Position.of(x, y));
		}

		public default IPosition plus(IPosition other)
		{
			if (other == null || other == ZERO)
				return this;
			if (this == ZERO)
				return other;

			return new DynamicPosition(0, 0, () -> x() + other.x(), () -> y() + other.y());
		}

		public default IPosition minus(IPosition other)
		{
			if (other == null || other == ZERO)
				return this;
			if (this == ZERO)
				return other;

			return new DynamicPosition(0, 0, () -> x() - other.x(), () -> y() - other.y());
		}
	}

	public static class DynamicPosition implements IPosition
	{
		private int x;
		private int y;
		private IntSupplier xFunction;
		private IntSupplier yFunction;

		DynamicPosition(int x, int y, IntSupplier xFunction, IntSupplier yFunction)
		{
			this.x = x;
			this.y = y;
			this.xFunction = xFunction;
			this.yFunction = yFunction;
		}

		@Override
		public int x()
		{
			return xFunction == null ? x : xFunction.getAsInt();
		}

		@Override
		public int y()
		{
			return yFunction == null ? y : yFunction.getAsInt();
		}

		@Override
		public String toString()
		{
			return x() + "," + y();
		}
	}

	public static class ScreenPosition implements IPosition
	{
		private final IPositioned owner;
		private final boolean fixed;

		public ScreenPosition(IPositioned owner)
		{
			this(owner, false);
		}

		public ScreenPosition(IPositioned owner, boolean fixed)
		{
			this.owner = owner;
			this.fixed = fixed;
		}

		@Override
		public int x()
		{
			int x = owner.position().x();
			if (owner instanceof IChild<?>)
			{
				Object parent = ((IChild<?>) owner).getParent();
				if (parent instanceof UIComponent)
					x += ((UIComponent) parent).screenPosition().x();
				if (fixed)
					return x;
				if (parent instanceof IOffset)
					x += ((IOffset) parent).offset().x();
			}
			return x;
		}

		@Override
		public int y()
		{
			int y = owner.position().y();
			if (owner instanceof IChild<?>)
			{
				Object parent = ((IChild<?>) owner).getParent();
				if (parent instanceof UIComponent)
					y += ((UIComponent) parent).screenPosition().y();
				if (fixed)
					return y;
				if (parent instanceof IScrollable)
					y += ((IScrollable) parent).offset().y();
			}
			return y;
		}

		@Override
		public String toString()
		{
			return x() + "," + y();
		}
	}

	//Builder
	public static <T> PositionBuilder<T, ?> of(T owner)
	{
		return new PositionBuilder<>(owner);
	}

	//Position shortcuts
	public static IPosition of(int x, int y)
	{
		return new DynamicPosition(x, y, null, null);
	}

	public static IPosition of(IntSupplier x, IntSupplier y)
	{
		return new DynamicPosition(0, 0, x, y);
	}

	//position relative to parent
	public static IPosition of(IChild<?> owner, int x, int y)
	{
		return new DynamicPosition(0, 0, Positions.leftAligned(owner, x), Positions.topAligned(owner, y));
	}

	/**
	 * Positions the <code>owner</code> as the top left of its parent.<br>
	 * Does respect parent padding if any.
	 *
	 * @param owner the owner
	 * @return the i position
	 */
	public static IPosition zero(IChild<?> owner)
	{
		return of(owner, 0, 0);
	}

	public static IPosition leftAligned(Object owner)
	{
		return leftAligned(owner, 0);
	}

	public static IPosition leftAligned(Object owner, int offset)
	{
		return new PositionBuilder<>(owner).leftAligned(offset).build();
	}

	public static IPosition centered(Object owner)
	{
		return centered(owner, 0);
	}

	public static IPosition centered(Object owner, int offset)
	{
		return new PositionBuilder<>(owner).centered(offset).middleAligned().build();
	}

	public static IPosition rightAligned(Object owner)
	{
		return rightAligned(owner, 0);
	}

	public static IPosition rightAligned(Object owner, int offset)
	{
		return new PositionBuilder<>(owner).rightAligned(offset).build();
	}

	//position relative to other
	public static <T extends IPositioned & ISized> IPosition leftOf(Object owner, T other)
	{
		return leftOf(owner, other, 0);
	}

	public static <T extends IPositioned & ISized> IPosition leftOf(Object owner, T other, int spacing)
	{
		return new PositionBuilder<>(owner).leftOf(other, spacing).middleAlignedTo(other).build();
	}

	public static <T extends IPositioned & ISized> IPosition rightOf(Object owner, T other)
	{
		return rightOf(owner, other, 0);
	}

	public static <T extends IPositioned & ISized> IPosition rightOf(Object owner, T other, int spacing)
	{
		return new PositionBuilder<>(owner).rightOf(other, spacing).middleAlignedTo(other).build();
	}

	public static <T extends IPositioned & ISized> IPosition above(Object owner, T other)
	{
		return above(owner, other, 0);
	}

	public static <T extends IPositioned & ISized> IPosition above(Object owner, T other, int spacing)
	{
		return new PositionBuilder<>(owner).above(other, spacing).leftAlignedTo(other).build();
	}

	public static <T extends IPositioned & ISized> IPosition below(Object owner, T other)
	{
		return below(owner, other, 0);
	}

	public static <T extends IPositioned & ISized> IPosition below(Object owner, T other, int spacing)
	{
		return new PositionBuilder<>(owner).below(other, spacing).leftAlignedTo(other).build();
	}
}
