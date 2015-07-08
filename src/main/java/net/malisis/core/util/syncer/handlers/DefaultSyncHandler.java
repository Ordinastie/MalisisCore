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

package net.malisis.core.util.syncer.handlers;

import java.lang.reflect.Field;

import net.malisis.core.util.DoubleKeyMap;
import net.malisis.core.util.DoubleKeyMap.DoubleKeyEntry;
import net.malisis.core.util.syncer.FieldData;
import net.malisis.core.util.syncer.ISyncHandler;
import net.malisis.core.util.syncer.ISyncableData;

/**
 * @author Ordinastie
 *
 */
public abstract class DefaultSyncHandler<T, S extends ISyncableData> implements ISyncHandler<T, S>
{
	private DoubleKeyMap<String, Field> fields = new DoubleKeyMap<>();

	@Override
	public void addFieldData(FieldData fieldData)
	{
		if (fields.get(fieldData.getName()) != null)
			throw new RuntimeException(fieldData.getName() + " is already registered");

		fields.put(fieldData.getName(), fieldData.getField());
	}

	private FieldData getFieldData(DoubleKeyEntry<String, Field> entry)
	{
		return entry != null ? new FieldData(entry.getIndex(), entry.getKey(), entry.getValue()) : null;
	}

	@Override
	public FieldData getFieldData(int index)
	{
		return getFieldData(fields.getEntry(index));
	}

	@Override
	public FieldData getFieldData(String name)
	{
		return getFieldData(fields.getEntry(name));
	}
}
