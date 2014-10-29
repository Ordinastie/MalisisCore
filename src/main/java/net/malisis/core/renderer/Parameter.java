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

import java.lang.reflect.Array;

/**
 * @author Ordinastie
 * 
 */
public class Parameter<T>
{
	private T defaultValue;
	private T value;

	public Parameter(T defaultValue)
	{
		this.defaultValue = defaultValue;
	}

	public T getDefault()
	{
		return defaultValue;
	}

	public T getValue()
	{
		return value;
	}

	public void reset()
	{
		value = null;
	}

	public T get()
	{
		return value != null ? value : defaultValue;
	}

	public Object get(int index)
	{
		if (value == null)
			return value;
		if (value != null && !(value instanceof Object[]))
			throw new IllegalStateException("Trying to access indexed element of non-array Parameter");

		Object[] v = (Object[]) value;
		if (index < 0 || index >= v.length)
			return null;

		return Array.get(value, index);
	}

	public void set(T value)
	{
		this.value = value;
	}

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
