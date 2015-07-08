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

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.malisis.core.MalisisCore;
import net.malisis.core.util.DoubleKeyMap;
import net.malisis.core.util.syncer.handlers.TileEntitySyncHandler;
import net.malisis.core.util.syncer.message.SyncerMessage;
import net.malisis.core.util.syncer.message.SyncerMessage.Packet;
import cpw.mods.fml.common.discovery.ASMDataTable;
import cpw.mods.fml.common.discovery.ASMDataTable.ASMData;

/**
 * This class handles the synchronization between server and client objects. Objects to be synchronized need to have the {@link Syncable}
 * annotation on their classes with a value matching a registered {@link ISyncHandler}.<br>
 * Fields that need to be synchronized need {@link Sync} annotation.<br>
 * To manually synchronize one or more fields, call {@link Syncer#sync(Object, String...)} with the {@link Sync} values for those fields.
 *
 * @author Ordinastie
 */
public class Syncer
{
	/** Map of the {@link ISyncHandler} registered. */
	private DoubleKeyMap<String, ISyncHandler<?, ? extends ISyncableData>> handlers = new DoubleKeyMap<>();
	/** Map of {@link ISyncHandler} registered, accessible by the classes annotated by {@link Syncable} */
	private Map<Class<?>, ISyncHandler<?, ? extends ISyncableData>> classToHandler = new HashMap<>();

	private Map<Object, HashMap<String, Object>> syncCache = new HashMap<>();

	/** Syncer instance **/
	private static Syncer instance;

	private Syncer()
	{
		registerSyncHandler(new TileEntitySyncHandler());
	}

	/**
	 * Registers a {@link ISyncHandler}.
	 *
	 * @param handler the handler
	 */
	private void registerSyncHandler(ISyncHandler<?, ? extends ISyncableData> handler)
	{
		handlers.put(handler.getName(), handler);
	}

	/**
	 * Gets the handler id for the specified {@link ISyncHandler}.
	 *
	 * @param handler the handler
	 * @return the handler id
	 */
	public int getHandlerId(ISyncHandler<?, ? extends ISyncableData> handler)
	{
		return handlers.getIndex(handler.getName());
	}

	/**
	 * Gets a {@link ISyncHandler} from it's ID
	 *
	 * @param id the id
	 * @return the handler from id
	 */
	public ISyncHandler<?, ? extends ISyncableData> getHandlerFromId(int id)
	{
		return handlers.get(id);
	}

	/**
	 * Gets the {@link ISyncHandler} for the specified object.
	 *
	 * @param <T> the generic type
	 * @param caller the caller
	 * @return the handler
	 */
	public <T> ISyncHandler<? super T, ? extends ISyncableData> getHandler(T caller)
	{
		ISyncHandler handler = classToHandler.get(caller.getClass());
		if (handler == null)
		{
			MalisisCore.log.error("No ISyncHandler registered for type '{}'", caller.getClass());
			return null;
		}

		return handler;
	}

	/**
	 * Discovers all the classes with {@link Syncable} annotation and fields with {@link Sync} annotation.<br>
	 * Fields are added the the corresponding {@link ISyncHandler}.
	 *
	 * @param asmDataTable the asm data table
	 */
	public void discover(ASMDataTable asmDataTable)
	{
		for (ASMData data : asmDataTable.getAll(Syncable.class.getName()))
		{
			try
			{
				Class clazz = Class.forName(data.getClassName());
				Syncable anno = (Syncable) clazz.getDeclaredAnnotation(Syncable.class);
				ISyncHandler<?, ? extends ISyncableData> handler = handlers.get(anno.value());
				classToHandler.put(clazz, handler);

				for (Field f : clazz.getFields())
				{
					Sync syncAnno = f.getDeclaredAnnotation(Sync.class);
					if (syncAnno != null)
						handler.addFieldData(new FieldData(0, syncAnno.value(), f));
				}
			}
			catch (Exception e)
			{
				MalisisCore.log.error("Could not process {} syncable.", data.getClassName(), e);
			}
		}
	}

	/**
	 * Gets the indexes of the sync fields into a single integer.
	 *
	 * @param handler the handler
	 * @param syncNames the sync names
	 * @return the field indexes
	 */
	private int getFieldIndexes(ISyncHandler<?, ? extends ISyncableData> handler, String... syncNames)
	{
		int indexes = 0;
		for (String str : syncNames)
		{
			FieldData fd = handler.getFieldData(str);
			if (fd != null)
				indexes |= 1 << fd.getIndex();
		}
		return indexes;
	}

	/**
	 * Gets the field values for the specified names.
	 *
	 * @param caller the caller
	 * @param handler the handler
	 * @param syncNames the sync names
	 * @return the field values
	 */
	private Map<String, Object> getFieldValues(Object caller, ISyncHandler<?, ? extends ISyncableData> handler, String... syncNames)
	{
		Map<String, Object> values = new LinkedHashMap<>();
		try
		{
			for (String str : syncNames)
			{
				FieldData fd = handler.getFieldData(str);
				if (fd != null)
					values.put(str, fd.getField().get(caller));
			}
		}
		catch (IllegalArgumentException | IllegalAccessException e)
		{
			e.printStackTrace();
		}

		return values;
	}

	/**
	 * Synchronizes the specified fields names and sends the corresponding packet.
	 *
	 * @param <T> the type of the caller
	 * @param caller the caller
	 * @param syncNames the sync names
	 */
	private <T> void doSync(T caller, String... syncNames)
	{
		ISyncHandler<? super T, ? extends ISyncableData> handler = getHandler(caller);
		if (handler == null)
			return;

		ISyncableData data = handler.getSyncData(caller);
		int indexes = getFieldIndexes(handler, syncNames);
		Map<String, Object> values = getFieldValues(caller, handler, syncNames);

		SyncerMessage.Packet<T> packet = new Packet<>(handler, data, indexes, values);

		handler.send(caller, packet);
	}

	private void registerAutoSync(Object caller)
	{
		if (syncCache.get(caller) != null)
			return;

		//	HashMap<String, Object> values = getFieldValues(caller, handler, syncNames)
	}

	/**
	 * Update the fields values for the receiver object.
	 *
	 * @param receiver the caller
	 * @param handler the handler
	 * @param values the values
	 */
	public void updateValues(Object receiver, ISyncHandler<?, ? extends ISyncableData> handler, Map<String, Object> values)
	{
		if (receiver == null || handler == null)
			return;

		for (Entry<String, Object> entry : values.entrySet())
		{
			try
			{
				FieldData fd = handler.getFieldData(entry.getKey());
				if (fd != null)
					fd.getField().set(receiver, entry.getValue());
			}
			catch (IllegalArgumentException | IllegalAccessException e)
			{
				MalisisCore.log.error("Failed to update {} field for {}.", entry.getKey(), receiver.getClass().getSimpleName(), e);
			}
		}
	}

	/**
	 * Gets the {@link Syncer} instance.
	 *
	 * @return the syncer
	 */
	public static Syncer get()
	{
		if (instance == null)
			instance = new Syncer();
		return instance;
	}

	/**
	 * Registers a {@link ISyncHandler} to the {@link Syncer}.
	 *
	 * @param handler the handler
	 */
	public static void registerHandler(ISyncHandler<?, ? extends ISyncableData> handler)
	{
		get().registerSyncHandler(handler);
	}

	/**
	 * Synchronizes the specified fields names and sends the corresponding packet.
	 *
	 * @param caller the caller
	 * @param syncNames the sync names
	 */
	public static void sync(Object caller, String... syncNames)
	{
		get().doSync(caller, syncNames);
	}

	public static void autoSync(Object caller)
	{
		get().registerAutoSync(caller);
	}

}
