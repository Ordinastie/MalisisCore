/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Ordinastie
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

package net.malisis.core.block;

import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * {@link IComponent} are elements handled by {@link IComponentProvider}.
 *
 * @author Ordinastie
 */
public interface IComponent
{
	/**
	 * Checks if this component should only be used client side.
	 *
	 * @return true, if is client component
	 */
	public default boolean isClientComponent()
	{
		return false;
	}

	/**
	 * Gets the additional components that this {@link IComponent} depends on.
	 *
	 * @return the dependencies
	 */
	public default List<IComponent> getDependencies()
	{
		return ImmutableList.of();
	}

	/**
	 * Called when this {@link IComponent} is added to the {@link IComponentProvider}.
	 *
	 * @param provider the provider
	 */
	public default void onComponentAdded(IComponentProvider provider)
	{

	}

	/**
	 * Gets the component of the specify <code>type</code> for the {@link Object}.<br>
	 * The returned object may <b>not</b> be a component but the block itself if it implements an interface used for a {@link IComponent}.
	 *
	 * @param <T> the generic type
	 * @param type the type
	 * @param object the block
	 * @return the component
	 */
	public static <T> T getComponent(Class<T> type, Object object)
	{
		if (object == null)
			return null;

		if (type.isAssignableFrom(object.getClass()))
			return type.cast(object);

		if (!(object instanceof IComponentProvider))
			return null;

		return ((IComponentProvider) object).getComponent(type);
	}
}
