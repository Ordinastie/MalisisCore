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

import static com.google.common.base.Preconditions.*;

import java.util.function.IntSupplier;

import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.element.IChild;
import net.malisis.core.client.gui.element.Padding;
import net.malisis.core.client.gui.element.Size.ISized;
import net.malisis.core.client.gui.element.position.Position.IPosition;
import net.malisis.core.client.gui.element.position.Position.IPositioned;

/**
 * The Class PositionFunctions.
 *
 * @author Ordinastie
 */
public class Positions
{
	//position inside parent
	/**
	 * Positions the owner to the left inside its parent.<br>
	 * Respects the parent padding.
	 *
	 * @param spacing the spacing
	 * @return the int supplier
	 */
	public static IntSupplier leftAligned(IChild<?> owner, int spacing)
	{
		return () -> {
			return Padding.of(owner.getParent()).left() + spacing;
		};
	}

	/**
	 * Positions the owner to the right inside its parent.<br>
	 * Respects the parent padding.
	 *
	 * @param <T> the generic type
	 * @param <U> the generic type
	 * @param spacing the spacing
	 * @return the int supplier
	 */
	public static <T extends ISized & IChild<U>, U extends ISized> IntSupplier rightAligned(T owner, int spacing)
	{

		return () -> {
			U parent = owner.getParent();
			if (parent == null)
				return 0;
			return parent.size().width() - owner.size().width() - Padding.of(parent).right() - spacing;
		};
	}

	/**
	 * Positions the owner in the center of its parent.<br>
	 * Respects the parent padding.
	 *
	 * @param <T> the generic type
	 * @param <U> the generic type
	 * @param offset the offset
	 * @return the int supplier
	 */
	public static <T extends ISized & IChild<U>, U extends ISized> IntSupplier centered(T owner, int offset)
	{
		return () -> {
			U parent = owner.getParent();
			if (parent == null)
				return 0;
			return (parent.size().width() - Padding.of(parent).horizontal() - owner.size().width()) / 2 + offset;
		};
	}

	/**
	 * Positions the owner to the top inside its parent.<br>
	 * Respects the parent padding.
	 *
	 * @param spacing the spacing
	 * @return the int supplier
	 */
	public static IntSupplier topAligned(IChild<?> owner, int spacing)
	{
		return () -> {
			return Padding.of(owner.getParent()).top() + spacing;
		};
	}

	/**
	 * Positions the owner to the bottom inside its parent.<br>
	 * Respects the parent padding.
	 *
	 * @param <T> the generic type
	 * @param <U> the generic type
	 * @param spacing the spacing
	 * @return the int supplier
	 */
	public static <T extends ISized & IChild<U>, U extends ISized> IntSupplier bottomAligned(T owner, int spacing)
	{

		return () -> {
			U parent = owner.getParent();
			if (owner.getParent() == null)
				return 0;
			return parent.size().height() - owner.size().height() - Padding.of(parent).bottom() - spacing;
		};

	}

	/**
	 * Positions the owner to the bottom inside its parent.<br>
	 * Respects the parent padding.
	 *
	 * @param <T> the generic type
	 * @param <U> the generic type
	 * @param offset the offset
	 * @return the int supplier
	 */
	public static <T extends ISized & IChild<U>, U extends ISized> IntSupplier middleAligned(T owner, int offset)
	{
		return () -> {
			U parent = owner.getParent();
			if (owner.getParent() == null)
				return 0;
			return (int) (Math.ceil(((float) parent.size().height() - Padding.of(parent).vertical() - owner.size().height()) / 2) + offset);
		};
	}

	//relative position to other
	/**
	 * Positions the owner to the left of the other.
	 *
	 * @param other the other
	 * @param spacing the spacing
	 * @return the int supplier
	 */
	public static IntSupplier leftOf(ISized owner, IPositioned other, int spacing)
	{
		checkNotNull(other);
		return () -> {
			return other.position().x() - owner.size().width() - spacing;
		};
	}

	/**
	 * Positions the owner to the right of the other.
	 *
	 * @param <T> the generic type
	 * @param other the other
	 * @param spacing the spacing
	 * @return the int supplier
	 */
	public static <T extends IPositioned & ISized> IntSupplier rightOf(T other, int spacing)
	{
		checkNotNull(other);
		return () -> {
			return other.position().x() + other.size().width() + spacing;
		};
	}

	/**
	 * Positions the owner above the other.
	 *
	 * @param other the other
	 * @param spacing the spacing
	 * @return the int supplier
	 */
	public static IntSupplier above(ISized owner, IPositioned other, int spacing)
	{
		checkNotNull(other);
		return () -> {
			return other.position().y() - owner.size().height() - spacing;
		};
	}

	/**
	 * Positions the owner below the other.
	 *
	 * @param <T> the generic type
	 * @param other the other
	 * @param spacing the spacing
	 * @return the int supplier
	 */
	public static <T extends IPositioned & ISized> IntSupplier below(T other, int spacing)
	{
		checkNotNull(other);
		return () -> {
			return other.position().y() + other.size().height() + spacing;
		};

	}

	//alignment relative to another component
	/**
	 * Left aligns the owner to the other.
	 *
	 * @param other the other
	 * @param offset the offset
	 * @return the int supplier
	 */
	public static IntSupplier leftAlignedTo(IPositioned other, int offset)
	{
		checkNotNull(other);
		return () -> {
			return other.position().x() + offset;
		};
	}

	/**
	 * Right aligns the owner to the other.
	 *
	 * @param <T> the generic type
	 * @param other the other
	 * @param offset the offset
	 * @return the int supplier
	 */
	public static <T extends IPositioned & ISized> IntSupplier rightAlignedTo(ISized owner, T other, int offset)
	{
		checkNotNull(other);
		return () -> {
			return other.position().x() + other.size().width() - owner.size().width() + offset;
		};
	}

	/**
	 * Centers the owner to the other.
	 *
	 * @param <T> the generic type
	 * @param other the other
	 * @param offset the offset
	 * @return the int supplier
	 */
	public static <T extends IPositioned & ISized> IntSupplier centeredTo(ISized owner, T other, int offset)
	{
		checkNotNull(other);
		return () -> {
			return other.position().x() + (other.size().width() - owner.size().width()) / 2 + offset;
		};
	}

	/**
	 * Top aligns the owner to the other.
	 *
	 * @param other the other
	 * @param offset the offset
	 * @return the int supplier
	 */
	public static IntSupplier topAlignedTo(IPositioned other, int offset)
	{
		checkNotNull(other);
		return () -> {
			return other.position().y() + offset;
		};

	}

	/**
	 * Bottom aligns the owner to the other.
	 *
	 * @param <T> the generic type
	 * @param other the other
	 * @param offset the offset
	 * @return the int supplier
	 */
	public static <T extends IPositioned & ISized> IntSupplier bottomAlignedTo(ISized owner, T other, int offset)
	{

		checkNotNull(other);
		return () -> {
			return other.position().y() + other.size().height() - owner.size().height() + offset;
		};

	}

	/**
	 * Middle aligns the owner to the other.
	 *
	 * @param <T> the generic type
	 * @param other the other
	 * @param offset the offset
	 * @return the int supplier
	 */
	public static <T extends IPositioned & ISized> IntSupplier middleAlignedTo(ISized owner, T other, int offset)
	{
		checkNotNull(other);
		return () -> {
			return (int) (other.position().y() + Math.ceil(((float) other.size().height() - owner.size().height()) / 2) + offset);
		};
	}

	/**
	 * Of.
	 *
	 * @param component the component
	 * @return the i position
	 */
	public static IPosition of(UIComponent component)
	{
		return of(component, 0, 0);
	}

	/**
	 * Of.
	 *
	 * @param component the component
	 * @param xOffset the x offset
	 * @param yOffset the y offset
	 * @return the i position
	 */
	public static IPosition of(UIComponent component, int xOffset, int yOffset)
	{
		return component.screenPosition().offset(xOffset, yOffset);
	}
}
