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

package net.malisis.core.util.syncer;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @author Ordinastie
 *
 */
public class ObjectData
{
	private int index;
	private final String name;
	private final Class<?> type;
	private final Function<Object, Object> getter;
	private final BiConsumer<Object, Object> setter;

	public ObjectData(String name, Class<?> type, Function<Object, Object> getter, BiConsumer<Object, Object> setter)
	{
		this.name = name;
		this.type = type;
		this.getter = getter;
		this.setter = setter;
	}

	public void setIndex(int index)
	{
		this.index = index;
	}

	public int getIndex()
	{
		return index;
	}

	public String getName()
	{
		return name;
	}

	public Class<?> getType()
	{
		return type;
	}

	public void set(Object holder, Object value)
	{
		setter.accept(holder, value);
	}

	public Object get(Object holder)
	{
		return getter.apply(holder);
	}
}
