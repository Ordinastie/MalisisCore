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
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Function;

import net.malisis.core.MalisisCore;
import net.malisis.core.util.DoubleKeyMap;
import net.malisis.core.util.Silenced;
import net.malisis.core.util.syncer.Sync.Type;
import net.malisis.core.util.syncer.handlers.TileEntitySyncHandler;
import net.malisis.core.util.syncer.message.SyncerMessage;
import net.malisis.core.util.syncer.message.SyncerMessage.Packet;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.discovery.ASMDataTable.ASMData;

import com.google.common.base.Supplier;
import com.google.common.collect.Maps;

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
	private DoubleKeyMap<Class<?>, ISyncHandler<?, ? extends ISyncableData>> handlers = new DoubleKeyMap<>();
	/** Map of {@link ISyncHandler} registered, accessible by the classes annotated by {@link Syncable} */
	//private Map<Class<?>, ISyncHandler<?, ? extends ISyncableData>> classToHandler = new HashMap<>();

	private Map<String, Supplier<ISyncHandler<?, ? extends ISyncableData>>> factories = new HashMap<>();

	private Map<Object, HashMap<String, Object>> syncCache = new HashMap<>();

	/** Syncer instance **/
	private static Syncer instance;

	private Syncer()
	{
		registerFacotory("TileEntity", TileEntitySyncHandler::new);
	}

	private void registerFacotory(String name, Supplier<ISyncHandler<?, ? extends ISyncableData>> supplier)
	{
		factories.put(name, supplier);
	}

	//	/**
	//	 * Registers a {@link ISyncHandler}.
	//	 *
	//	 * @param handler the handler
	//	 */
	//	private void registerSyncHandler(ISyncHandler<?, ? extends ISyncableData> handler)
	//	{
	//		handlers.put(handler.getName(), handler);
	//	}
	//
	/**
	 * Gets the handler id for the specified {@link ISyncHandler}.
	 *
	 * @param clazz the clazz
	 * @return the handler id
	 */
	public int getHandlerId(Class<?> clazz)
	{
		return handlers.getIndex(clazz);
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
		@SuppressWarnings("unchecked")
		ISyncHandler<? super T, ? extends ISyncableData> handler = (ISyncHandler<? super T, ? extends ISyncableData>) handlers.get(caller.getClass());
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
				Class<?> clazz = Class.forName(data.getClassName());
				Syncable anno = clazz.getAnnotation(Syncable.class);
				ISyncHandler<?, ? extends ISyncableData> handler = factories.get(anno.value()).get();
				handlers.put(clazz, handler);

				for (Field f : clazz.getFields())
				{
					Sync syncAnno = f.getAnnotation(Sync.class);
					if (syncAnno != null)
						handler.addObjectData(getObjectData(syncAnno.value(), f));
				}

				Map<String, Method> gets = Maps.newHashMap();
				Map<String, Method> sets = Maps.newHashMap();
				for (Method m : clazz.getMethods())
				{
					Sync syncAnno = m.getAnnotation(Sync.class);
					if (syncAnno != null)
					{
						Type type = getMethodType(syncAnno, m);
						if (type == null)
						{
							MalisisCore.log.error("Could not determine the type of the method {} (GETTER or SETTER).", m.getName());
							break;
						}

						if (type == Type.GETTER)
							gets.put(syncAnno.value(), m);
						else if (type == Type.SETTER)
							sets.put(syncAnno.value(), m);

						Method setter = sets.get(syncAnno.value());
						Method getter = gets.get(syncAnno.value());
						if (getter != null && setter != null)
						{
							ObjectData od = getObjectData(syncAnno.value(), getter, setter);
							if (od != null)
								handler.addObjectData(od);
						}
					}

				}
			}
			catch (Exception e)
			{
				MalisisCore.log.error("Could not process {} syncable.", data.getClassName(), e);
			}
		}
	}

	private Type getMethodType(Sync syncAnno, Method m)
	{
		if (syncAnno.type() != Type.AUTO)
			return syncAnno.type();
		int c = m.getParameterCount();
		return c == 1 ? Type.SETTER : (c == 0 ? Type.GETTER : null);
	}

	private ObjectData getObjectData(String name, Field field)
	{
		Function<Object, Object> getter = (holder) -> Silenced.apply(field::get, holder);
		BiConsumer<Object, Object> setter = (holder, value) -> Silenced.accept(field::set, holder, value);

		return new ObjectData(name, field.getType(), getter, setter);
	}

	private ObjectData getObjectData(String name, Method get, Method set)
	{
		Function<Object, Object> getter = (holder) -> Silenced.apply(get::invoke, holder);
		BiConsumer<Object, Object> setter = (holder, value) -> Silenced.accept(set::invoke, holder, value);

		if (set.getParameterTypes()[0] != get.getReturnType())
			return null;

		return new ObjectData(name, get.getReturnType(), getter, setter);
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
			ObjectData od = handler.getObjectData(str);
			if (od != null)
				indexes |= 1 << od.getIndex();
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
		for (String str : syncNames)
		{
			ObjectData od = handler.getObjectData(str);
			if (od != null)
				values.put(str, od.get(caller));
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
	private <T, S extends ISyncableData> void doSync(T caller, String... syncNames)
	{
		@SuppressWarnings("unchecked")
		ISyncHandler<T, S> handler = (ISyncHandler<T, S>) getHandler(caller);
		if (handler == null)
			return;

		S data = handler.getSyncData(caller);
		int indexes = getFieldIndexes(handler, syncNames);
		Map<String, Object> values = getFieldValues(caller, handler, syncNames);

		SyncerMessage.Packet<T, S> packet = new Packet<>(getHandlerId(caller.getClass()), data, indexes, values);

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
	public <T> void updateValues(T receiver, ISyncHandler<T, ? extends ISyncableData> handler, Map<String, Object> values)
	{
		if (receiver == null || handler == null)
			return;

		for (Entry<String, Object> entry : values.entrySet())
		{
			ObjectData od = handler.getObjectData(entry.getKey());
			if (od != null)
				od.set(receiver, entry.getValue());
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
	 * @param name the name
	 * @param supplier the supplier
	 */
	public static void registerHandlerFactory(String name, Supplier<ISyncHandler<?, ? extends ISyncableData>> supplier)
	{
		get().registerFacotory(name, supplier);
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
