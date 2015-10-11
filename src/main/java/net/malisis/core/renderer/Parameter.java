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

package net.malisis.core.renderer;


/**
 *
 * @author Ordinastie
 * @param <T> type of value held by the {@link Parameter}
 */
public class Parameter<T>
{
	/** Default value. */
	private T defaultValue;

	/** Current alue. */
	private T value;

	/**
	 * Instantiates a new parameter.
	 *
	 * @param defaultValue the default value
	 */
	public Parameter(T defaultValue)
	{
		this.defaultValue = defaultValue;
	}

	/**
	 * Gets the default value.
	 *
	 * @return the default
	 */
	public T getDefault()
	{
		return defaultValue;
	}

	/**
	 * Gets the current value.
	 *
	 * @return the value
	 */
	public T getValue()
	{
		return value;
	}

	/**
	 * Resets the value to its default.
	 */
	public void reset()
	{
		value = null;
	}

	/**
	 * Gets the value of this {@link Parameter}. If not value was set, default value is returned.
	 *
	 * @return the value
	 */
	public T get()
	{
		return value != null ? value : defaultValue;
	}

	/**
	 * Sets the value for this {@link Parameter}.
	 *
	 * @param value the value
	 */
	public void set(T value)
	{
		this.value = value;
	}

	/**
	 * Gets the value of the specified index in the array held by this {@link Parameter}.
	 *
	 * @param index the index
	 * @return the object
	 */
	public Object get(int index)
	{
		if (value == null)
			return value;
		if (value != null && !(value instanceof Object[]))
			throw new IllegalStateException("Trying to access indexed element of non-array Parameter");

		Object[] v = (Object[]) value;
		if (index < 0 || index >= v.length)
			return null;

		return v[index];
	}

	/**
	 * Merge this {@link Parameter} with the specified one. The value will only be overridden if it is default.
	 *
	 * @param parameter the parameter
	 */
	public void merge(Parameter<T> parameter)
	{
		if (parameter.getValue() != null)
			value = parameter.getValue();
	}

	@Override
	public String toString()
	{
		return value.toString() + " [" + defaultValue.toString() + "]";
	}
}
