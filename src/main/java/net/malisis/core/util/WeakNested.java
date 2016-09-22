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

import java.util.WeakHashMap;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * @author Ordinastie
 *
 */
public abstract class WeakNested<K, V>
{
	protected WeakHashMap<K, V> internalMap = new WeakHashMap<>();
	protected Supplier<V> factory = null;

	public WeakNested(Supplier<V> factory)
	{
		this.factory = factory;
	}

	protected V empty()
	{
		return factory.get();
	}

	public V get(K key)
	{
		return internalMap.get(key);
	}

	public V remove(K key)
	{
		return internalMap.remove(key);
	}

	protected V getOrCreate(K key, V defaultValue)
	{
		V v = internalMap.get(key);
		if (v != null)
			return v;

		internalMap.put(key, defaultValue);
		return defaultValue;
	}

	public static class Map<K, U, V> extends WeakNested<K, java.util.Map<U, V>>
	{
		public Map(Supplier<java.util.Map<U, V>> supplier)
		{
			super(supplier);
		}

		@Override
		public java.util.Map<U, V> get(K key)
		{
			java.util.Map<U, V> ret = internalMap.get(key);
			return ret != null ? ret : ImmutableMap.of();
		}

		public V get(K key, U key2)
		{
			return get(key).get(key2);
		}

		public void put(K key, U key2, V value)
		{
			getOrCreate(key, empty()).put(key2, value);
		}

		public void remove(K key, U key2)
		{
			java.util.Map<U, V> h = internalMap.get(key);
			if (h == null)
				return;
			h.remove(key2);
			if (h.size() == 0)
				remove(key);
		}
	}

	protected static abstract class Collection<K, U extends java.util.Collection<V>, V> extends WeakNested<K, U>
	{
		public Collection(Supplier<U> factory)
		{
			super(factory);
		}

		public void add(K key, V value)
		{
			getOrCreate(key, empty()).add(value);
		}

		public void addAll(K key, U values)
		{
			getOrCreate(key, values);
		}

		public void remove(K key, V value)
		{
			java.util.Collection<V> l = internalMap.get(key);
			if (l == null)
				return;

			l.remove(value);
			if (l.size() == 0)
				remove(key);
		}

	}

	public static class List<K, V> extends Collection<K, java.util.List<V>, V>
	{
		public List(Supplier<java.util.List<V>> supplier)
		{
			super(supplier);
		}

		@Override
		public java.util.List<V> get(K key)
		{
			java.util.List<V> ret = internalMap.get(key);
			return ret != null ? ret : ImmutableList.of();
		}
	}

	public static class Set<K, V> extends Collection<K, java.util.Set<V>, V>
	{
		public Set(Supplier<java.util.Set<V>> supplier)
		{
			super(supplier);
		}

		@Override
		public java.util.Set<V> get(K key)
		{
			java.util.Set<V> ret = internalMap.get(key);
			return ret != null ? ret : ImmutableSet.of();
		}
	}
}
