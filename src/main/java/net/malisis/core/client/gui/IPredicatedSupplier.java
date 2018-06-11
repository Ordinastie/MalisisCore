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

package net.malisis.core.client.gui;

import static com.google.common.base.Preconditions.*;

import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * @author Ordinastie
 *
 */

public interface IPredicatedSupplier<T> extends Supplier<T>
{
	public default IPredicatedSupplier<T> or(IPredicatedSupplier<T> other)
	{
		return () -> Optional.ofNullable(get()).orElseGet(other);
	}

	public static class PredicatedSupplier<T> implements IPredicatedSupplier<T>
	{
		private BooleanSupplier predicate;
		private T value;

		public PredicatedSupplier(BooleanSupplier predicate, T value)
		{
			this.predicate = checkNotNull(predicate);
			this.value = checkNotNull(value);
		}

		@Override
		public T get()
		{
			return predicate.getAsBoolean() ? value : null;
		}
	}

	public static class PredicatedIntSupplier implements IntSupplier
	{
		private BooleanSupplier predicate;
		private int value;

		public PredicatedIntSupplier(BooleanSupplier predicate, int value)
		{
			this.predicate = checkNotNull(predicate);
			this.value = value;
		}

		@Override
		public int getAsInt()
		{
			return predicate.getAsBoolean() ? value : 0;
		}
	}
}
