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

package net.malisis.core.util.cacheddata;

import static com.google.common.base.Preconditions.*;

import java.util.function.IntSupplier;

/**
 * @author Ordinastie
 *
 */
public class IntCachedData
{
	/** Supplier to fetch the current data. */
	protected IntSupplier getter;
	/** Data at the last update. */
	protected int lastValue;
	/** Current data. */
	protected int currentValue;

	/**
	 * Instantiates a new {@link CachedData}.
	 *
	 * @param getter the getter
	 */
	public IntCachedData(IntSupplier getter)
	{
		this.getter = checkNotNull(getter);
		currentValue = getter.getAsInt();
		update();
	}

	/**
	 * Gets the current data.
	 *
	 * @return the t
	 */
	public int get()
	{
		return currentValue;
	}

	/**
	 * Updates the current data.
	 */
	public void update()
	{
		lastValue = currentValue;
		currentValue = getter.getAsInt();
	}

	/**
	 * Checks whether the data has changed since the last update.
	 *
	 * @return true, if data has changed
	 */
	public boolean hasChanged()
	{
		return lastValue != currentValue;
	}

	public static class IntFixedData extends IntCachedData
	{
		public IntFixedData(int value)
		{
			super(() -> 0);
			currentValue = value;
		}

		@Override
		public void update()
		{}
	}
}
