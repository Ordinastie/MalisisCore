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
		public void setOwner(UIComponent<?> component);

		public int x();

		public int y();
	}

	public static class DynamicPosition implements IPosition
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
		return new PositionFactory(owner -> {
			return Padding.of(owner.getParent()).left() + x;
		});
	}

	//relative position
	public static PositionFactory leftOf(UIComponent<?> component)
	{
		return leftOf(component, 0);
	}

	public static PositionFactory leftOf(UIComponent<?> component, int spacing)
	{
		return new PositionFactory(owner -> {
			return component.position().x() - owner.size().width() - spacing;
		});
	}

	public static PositionFactory rightOf(UIComponent<?> component)
	{
		return rightOf(component, 0);
	}

	public static PositionFactory rightOf(UIComponent<?> component, int spacing)
	{
		return new PositionFactory(owner -> {
			return component.position().x() + component.size().width() + spacing;
		});
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
		return new PositionFactory(owner -> {
			UIComponent<?> parent = owner.getParent();
			if (parent == null)
				return 0;
			return parent.size().width() - owner.size().width() - Padding.of(parent).right() - spacing;
		});
	}

	public static PositionFactory centered()
	{
		return centered(0);
	}

	public static PositionFactory centered(int offset)
	{
		return new PositionFactory(owner -> {
			UIComponent<?> parent = owner.getParent();
			if (parent == null)
				return 0;
			return (parent.size().width() - Padding.of(parent).horizontal() - owner.size().width()) / 2 + offset;
		});
	}

	//aligned relative to another component
	public PositionFactory leftAlignedTo(UIComponent<?> other)
	{
		return leftAlignedTo(other, 0);
	}

	public PositionFactory leftAlignedTo(UIComponent<?> other, int offset)
	{
		return new PositionFactory(owner -> {
			return other.position().x() + offset;
		});
	}

	public PositionFactory rightAlignedTo(UIComponent<?> other)
	{
		return rightAlignedTo(other, 0);
	}

	public PositionFactory rightAlignedTo(UIComponent<?> other, int offset)
	{
		return new PositionFactory(owner -> {
			return other.position().x() + other.size().width() - owner.size().width() + offset;
		});
	}

	public PositionFactory centeredTo(UIComponent<?> other)
	{
		return centeredTo(other, 0);
	}

	public PositionFactory centeredTo(UIComponent<?> other, int offset)
	{
		return new PositionFactory(owner -> {
			return other.position().x() + (other.size().width() - owner.size().width()) / 2 + offset;
		});
	}
}
