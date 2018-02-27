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

import javax.annotation.Nonnull;

import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.element.Position.XFunction;
import net.malisis.core.client.gui.component.element.Position.YFunction;

/**
 * @author Ordinastie
 *
 */
public class Positions
{
	//absolute position
	public static XFunction x(int x)
	{
		return owner -> {
			return Padding.of(owner.getParent()).left() + x;
		};
	}

	public static YFunction y(int y)
	{
		return owner -> {
			return Padding.of(owner.getParent()).top() + y;
		};
	}

	//relative position
	public static XFunction leftOf(@Nonnull UIComponent<?> component, int spacing)
	{
		checkNotNull(component);
		return owner -> {
			return component.position().x() - owner.size().width() - spacing;
		};
	}

	public static XFunction rightOf(@Nonnull UIComponent<?> component, int spacing)
	{
		checkNotNull(component);
		return owner -> {
			return component.position().x() + component.size().width() + spacing;
		};
	}

	public static XFunction rightAligned(int spacing)
	{
		return owner -> {
			UIComponent<?> parent = owner.getParent();
			if (parent == null)
				return 0;
			return parent.size().width() - owner.size().width() - Padding.of(parent).right() - spacing;
		};
	}

	public static XFunction centered(int offset)
	{
		return owner -> {
			UIComponent<?> parent = owner.getParent();
			if (parent == null)
				return 0;
			return (parent.size().width() - Padding.of(parent).horizontal() - owner.size().width()) / 2 + offset;
		};
	}

	public static YFunction above(@Nonnull UIComponent<?> component, int spacing)
	{
		checkNotNull(component);
		return owner -> {
			return component.position().y() - owner.size().height() - spacing;
		};
	}

	public static YFunction below(@Nonnull UIComponent<?> component, int spacing)
	{
		checkNotNull(component);
		return owner -> {
			return component.position().y() + component.size().height() + spacing;
		};

	}

	public static YFunction bottomAligned(int spacing)
	{
		return owner -> {
			UIComponent<?> parent = owner.getParent();
			if (owner.getParent() == null)
				return 0;
			return parent.size().height() - owner.size().height() - Padding.of(parent).bottom() - spacing;
		};

	}

	public static YFunction middleAligned(int offset)
	{
		return owner -> {
			UIComponent<?> parent = owner.getParent();
			if (owner.getParent() == null)
				return 0;
			return (int) (Math.ceil(((float) parent.size().height() - Padding.of(parent).vertical() - owner.size().height()) / 2) + offset);
		};

	}

	//aligned relative to another component
	public static XFunction leftAlignedTo(@Nonnull UIComponent<?> other, int offset)
	{
		checkNotNull(other);
		return owner -> {
			return other.position().x() + offset;
		};
	}

	public static XFunction rightAlignedTo(@Nonnull UIComponent<?> other, int offset)
	{
		checkNotNull(other);
		return owner -> {
			return other.position().x() + other.size().width() - owner.size().width() + offset;
		};
	}

	public static XFunction centeredTo(@Nonnull UIComponent<?> other, int offset)
	{
		checkNotNull(other);
		return owner -> {
			return other.position().x() + (other.size().width() - owner.size().width()) / 2 + offset;
		};
	}

	public static YFunction topAlignedTo(@Nonnull UIComponent<?> other, int offset)
	{
		checkNotNull(other);
		return owner -> {
			return other.position().y() + offset;
		};

	}

	public static YFunction bottomAlignedTo(@Nonnull UIComponent<?> other, int offset)
	{
		checkNotNull(other);
		return owner -> {
			return other.position().y() + other.size().height() - owner.size().height() + offset;
		};

	}

	public static YFunction middleAlignedTo(@Nonnull UIComponent<?> other, int offset)
	{
		checkNotNull(other);
		return owner -> {
			return (int) (other.position().y() + Math.ceil(((float) other.size().height() - owner.size().height()) / 2) + offset);
		};

	}
}
