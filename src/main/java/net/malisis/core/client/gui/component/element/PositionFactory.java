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
import net.malisis.core.client.gui.component.element.Position.DynamicPosition;

/**
 * @author Ordinastie
 *
 */
public class PositionFactory
{
	private ToIntFunction<UIComponent<?>> xFunction;
	private ToIntFunction<UIComponent<?>> yFunction;

	public PositionFactory x(int x)
	{
		xFunction = owner -> {
			return Padding.of(owner.getParent()).left() + x;
		};
		return this;
	}

	public PositionFactory y(int y)
	{
		yFunction = owner -> {
			return Padding.of(owner.getParent()).top() + y;
		};;
		return this;
	}

	public PositionFactory leftOf(UIComponent<?> component)
	{
		return leftOf(component, 0);
	}

	public PositionFactory leftOf(UIComponent<?> component, int spacing)
	{
		xFunction = owner -> {
			return component.position().x() - owner.size().width() - spacing;
		};
		return this;
	}

	public PositionFactory rightOf(UIComponent<?> component)
	{
		return rightOf(component, 0);
	}

	public PositionFactory rightOf(UIComponent<?> component, int spacing)
	{
		xFunction = owner -> {
			return component.position().x() + component.size().width() + spacing;
		};
		return this;
	}

	public PositionFactory above(UIComponent<?> component)
	{
		return above(component, 0);
	}

	public PositionFactory above(UIComponent<?> component, int spacing)
	{
		yFunction = owner -> {
			return component.position().y() - owner.size().height() - spacing;
		};
		return this;
	}

	public PositionFactory below(UIComponent<?> component)
	{
		return below(component, 0);
	}

	public PositionFactory below(UIComponent<?> component, int spacing)
	{
		yFunction = owner -> {
			return component.position().y() + component.size().height() + spacing;
		};
		return this;
	}

	//aligned inside parent container
	public PositionFactory leftAligned()
	{
		return leftAligned(0);
	}

	public PositionFactory leftAligned(int spacing)
	{
		return x(0);
	}

	public PositionFactory rightAligned()
	{
		return rightAligned(0);
	}

	public PositionFactory rightAligned(int spacing)
	{
		xFunction = owner -> {
			UIComponent<?> parent = owner.getParent();
			if (parent == null)
				return 0;
			return parent.size().width() - owner.size().width() - Padding.of(parent).right() - spacing;
		};

		return this;
	}

	public PositionFactory centered()
	{
		return centered(0);
	}

	public PositionFactory centered(int offset)
	{
		xFunction = owner -> {
			UIComponent<?> parent = owner.getParent();
			if (parent == null)
				return 0;
			return (parent.size().width() - Padding.of(parent).horizontal() - owner.size().width()) / 2 + offset;
		};

		return this;
	}

	public PositionFactory topAligned()
	{
		return topAligned(0);
	}

	public PositionFactory topAligned(int spacing)
	{
		return y(0);
	}

	public PositionFactory bottomAligned()
	{
		return bottomAligned(0);
	}

	public PositionFactory bottomAligned(int spacing)
	{
		yFunction = owner -> {
			UIComponent<?> parent = owner.getParent();
			if (owner.getParent() == null)
				return 0;
			return parent.size().height() - owner.size().height() - Padding.of(parent).bottom() - spacing;
		};
		return this;
	}

	public PositionFactory middleAligned()
	{
		return middleAligned(0);
	}

	public PositionFactory middleAligned(int offset)
	{
		yFunction = owner -> {
			UIComponent<?> parent = owner.getParent();
			if (owner.getParent() == null)
				return 0;
			return (parent.size().height() - Padding.of(parent).vertical() - owner.size().height()) / 2 + offset;
		};
		return this;
	}

	//aligned relative to another component
	public PositionFactory leftAlignedTo(UIComponent<?> other)
	{
		return leftAlignedTo(other, 0);
	}

	public PositionFactory leftAlignedTo(UIComponent<?> other, int offset)
	{
		xFunction = owner -> {
			return other.position().x() + offset;
		};
		return this;
	}

	public PositionFactory rightAlignedTo(UIComponent<?> other)
	{
		return rightAlignedTo(other, 0);
	}

	public PositionFactory rightAlignedTo(UIComponent<?> other, int offset)
	{
		xFunction = owner -> {
			return other.position().x() + other.size().width() - owner.size().width() + offset;
		};

		return this;
	}

	public PositionFactory centeredTo(UIComponent<?> other)
	{
		return centeredTo(other, 0);
	}

	public PositionFactory centeredTo(UIComponent<?> other, int offset)
	{
		xFunction = owner -> {
			return other.position().x() + (other.size().width() - owner.size().width()) / 2 + offset;
		};

		return this;
	}

	public PositionFactory topAlignedTo(UIComponent<?> other)
	{
		return topAlignedTo(other, 0);
	}

	public PositionFactory topAlignedTo(UIComponent<?> other, int offset)
	{
		yFunction = owner -> {
			return other.position().y() + offset;
		};
		return this;
	}

	public PositionFactory bottomAlignedTo(UIComponent<?> other)
	{
		return bottomAlignedTo(other, 0);
	}

	public PositionFactory bottomAlignedTo(UIComponent<?> other, int offset)
	{
		yFunction = owner -> {
			return other.position().y() + other.size().height() - owner.size().height() + offset;
		};
		return this;
	}

	public PositionFactory middleAlignedTo(UIComponent<?> other)
	{
		return middleAlignedTo(other, 0);
	}

	public PositionFactory middleAlignedTo(UIComponent<?> other, int offset)
	{
		yFunction = owner -> {
			return other.position().y() + (other.size().height() - owner.size().height()) / 2 + offset;
		};
		return this;
	}

	public Position build()
	{
		return new DynamicPosition(xFunction, yFunction);
	}

}
