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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.BiMap;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashBiMap;

/**
 * @author Ordinastie
 *
 */
public class DoubleKeyMap<K, V>
{
	public static class DoubleKeyEntry<K, V>
	{
		private int index;
		private K key;
		private V value;

		public DoubleKeyEntry(int index, K key, V value)
		{
			this.index = index;
			this.key = key;
			this.value = value;
		}

		public int getIndex()
		{
			return index;
		}

		public K getKey()
		{
			return key;
		}

		public V getValue()
		{
			return value;
		}
	}

	private List<DoubleKeyEntry<K, V>> data = new ArrayList<>();
	private BiMap<K, Integer> keys = HashBiMap.create();

	public int put(K key, V value)
	{
		if (keys.get(key) != null)
			throw new IllegalArgumentException("Key already in map : " + key);

		int i = data.size();
		keys.put(key, i);
		data.add(new DoubleKeyEntry<>(i, key, value));
		return i;
	}

	public DoubleKeyEntry<K, V> getEntry(int index)
	{
		return data.get(index);
	}

	public DoubleKeyEntry<K, V> getEntry(K key)
	{
		if (keys.get(key) == null)
			return null;
		return data.get(keys.get(key));
	}

	public K getKey(int index)
	{
		DoubleKeyEntry<K, V> entry = getEntry(index);
		return entry != null ? entry.getKey() : null;
	}

	public int getIndex(K key)
	{
		return keys.get(key);
	}

	public V get(int index)
	{
		DoubleKeyEntry<K, V> entry = getEntry(index);
		return entry != null ? entry.getValue() : null;
	}

	public V get(K key)
	{
		DoubleKeyEntry<K, V> entry = getEntry(key);
		return entry != null ? entry.getValue() : null;
	}

	public Collection<V> values()
	{
		return Collections2.transform(data, new Function<DoubleKeyEntry<K, V>, V>()
		{
			@Override
			public V apply(DoubleKeyEntry<K, V> entry)
			{
				return entry.getValue();
			}
		});
	}
}
