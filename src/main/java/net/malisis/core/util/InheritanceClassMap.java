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

package net.malisis.core.util;

import java.util.Map;

import com.google.common.collect.ForwardingMap;
import com.google.common.collect.Maps;

/**
 * The InheritanceClassMap is a {@link Map} with {@link Class} keys where values can be queried with child classes of the key use for
 * insertion.
 *
 * @author Ordinastie
 * @param <V> the value type
 */
public class InheritanceClassMap<V> extends ForwardingMap<Class<?>, V>
{
	private final Map<Class<?>, V> internalMap;
	private Map<Class<?>, Class<?>> lookupMap = Maps.newHashMap();

	public InheritanceClassMap(Map<Class<?>, V> map)
	{
		this.internalMap = map;
	}

	@Override
	protected Map<Class<?>, V> delegate()
	{
		return internalMap;
	}

	/**
	 * Gets the matching inserted key in the map for the specified one.<br>
	 * If two or more keys match, the first one will always be used.
	 *
	 * @param key the key
	 * @return the key
	 */
	private Class<?> getKey(Class<?> key)
	{
		Class<?> newKey = lookupMap.get(key);
		if (newKey != null)
			return newKey;

		for (Class<?> c : internalMap.keySet())
		{
			if (c.isAssignableFrom(key))
			{
				lookupMap.put(key, c);
				return c;
			}
		}
		return null;
	}

	@Override
	public V get(Object key)
	{
		if (!(key instanceof Class<?>))
			return null;
		return containsKey(key) ? internalMap.get(key) : internalMap.get(getKey((Class<?>) key));
	}
}
