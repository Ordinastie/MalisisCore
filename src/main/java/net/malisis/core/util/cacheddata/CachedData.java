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

package net.malisis.core.util.cacheddata;

import static com.google.common.base.Preconditions.*;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

/**
 * This class allows custom handling for data that needs to be cached and checked for changes.<br>
 * {@link #update()} must be called to refresh the data before calling {@link #hasChanged()}.
 *
 * @author Ordinastie
 * @param <T> the generic type
 */
public class CachedData<T> implements ICachedData<T>
{
	/** Supplier to fetch the current data. */
	protected Supplier<T> getter;
	/** Predicate to test if data has changed. */
	protected BiPredicate<T, T> predicate;
	/** Data at the last update. */
	protected T lastData;
	/** Current data. */
	protected T currentData;

	/**
	 * Instantiates a new {@link CachedData}.
	 *
	 * @param getter the getter
	 * @param predicate the predicate
	 */
	public CachedData(Supplier<T> getter, BiPredicate<T, T> predicate)
	{
		this.getter = checkNotNull(getter);
		this.predicate = checkNotNull(predicate);
		currentData = getter.get();
		update();
	}

	/**
	 * Instantiates a new {@link CachedData}.
	 *
	 * @param getter the getter
	 */
	public CachedData(Supplier<T> getter)
	{
		this(getter, (o1, o2) -> !Objects.equals(o1, o2));
	}

	/**
	 * Gets the current data.
	 *
	 * @return the t
	 */
	@Override
	public T get()
	{
		return currentData;
	}

	/**
	 * Updates the current data.
	 */
	@Override
	public void update()
	{
		lastData = currentData;
		currentData = getter.get();
	}

	/**
	 * Checks whether the data has changed since the last update.
	 *
	 * @return true, if data has changed
	 */
	@Override
	public boolean hasChanged()
	{
		return predicate.test(lastData, currentData);
	}
}
