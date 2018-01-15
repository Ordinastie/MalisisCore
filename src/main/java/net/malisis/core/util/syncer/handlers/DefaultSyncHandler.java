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

import net.malisis.core.util.DoubleKeyMap;
import net.malisis.core.util.DoubleKeyMap.DoubleKeyEntry;
import net.malisis.core.util.syncer.ISyncHandler;
import net.malisis.core.util.syncer.ISyncableData;
import net.malisis.core.util.syncer.ObjectData;

/**
 * @author Ordinastie
 *
 */
public abstract class DefaultSyncHandler<T, S extends ISyncableData> implements ISyncHandler<T, S>
{
	private DoubleKeyMap<String, ObjectData> objectDatas = new DoubleKeyMap<>();

	@Override
	public void addObjectData(ObjectData objectData)
	{
		if (objectDatas.get(objectData.getName()) != null)
			throw new RuntimeException(objectData.getName() + " is already registered");

		objectData.setIndex(objectDatas.put(objectData.getName(), objectData));
	}

	@Override
	public ObjectData getObjectData(int index)
	{
		return objectDatas.get(index);
	}

	@Override
	public ObjectData getObjectData(String name)
	{
		return objectDatas.get(name);
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for (DoubleKeyEntry<String, ObjectData> entry : objectDatas)
			sb.append(entry.getIndex() + ":" + entry.getKey() + ",");
		sb.deleteCharAt(sb.length() - 1);

		return sb.toString();
	}
}
