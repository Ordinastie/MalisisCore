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

package net.malisis.core.util.modmessage;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Collection;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.malisis.core.IMalisisMod;
import net.malisis.core.MalisisCore;
import net.minecraftforge.fml.common.Loader;

/**
 * @author Ordinastie
 *
 */
public class ModMessageManager
{
	/** List of messages method registered. */
	private static Multimap<String, Pair<Object, Method>> messages = HashMultimap.create();

	/**
	 * Registers a class to handle mod messages.<br>
	 * Only static methods will be registered to handle messages.
	 *
	 * @param mod the mod
	 * @param messageHandlerClass the message handler class
	 */
	public static void register(IMalisisMod mod, Class<?> messageHandlerClass)
	{
		register(mod, messageHandlerClass, null);
	}

	/**
	 * Registers an object to handle mod messages.
	 *
	 * @param mod the mod
	 * @param messageHandler the message handler
	 */
	public static void register(IMalisisMod mod, Object messageHandler)
	{
		register(mod, messageHandler.getClass(), messageHandler);
	}

	/**
	 * Registers a message handler.<br>
	 * If no instance for the message handler is passed, only static methods will be registered to handle messages.
	 *
	 * @param mod the mod
	 * @param messageHandlerClass the message handler class
	 * @param messageHandler the message handler
	 */
	private static void register(IMalisisMod mod, Class<?> messageHandlerClass, Object messageHandler)
	{
		Method[] methods = messageHandler.getClass().getMethods();
		for (Method method : methods)
		{
			ModMessage ann = method.getAnnotation(ModMessage.class);
			if (ann == null)
				continue;

			String name = ann.value().equals("") ? method.getName() : ann.value();
			boolean isStatic = Modifier.isStatic(method.getModifiers());
			//only register static methods if an instance is passed
			if (isStatic || (messageHandler != null))
				messages.put(mod.getModId() + ":" + name, Pair.of(isStatic ? null : messageHandler, method));
			//MalisisCore.log.info("Registered mod message " + mod.getModId() + ":" + name + " in "
			//		+ messageHandler.getClass().getSimpleName());
		}
	}

	/**
	 * Sends a message to the another mod.
	 *
	 * @param modid the modid
	 * @param messageName the message name
	 * @param data the data
	 */
	public static void message(String modid, String messageName, Object... data)
	{
		//do not print warnings if mod is not loaded
		if (!Loader.isModLoaded(modid))
			return;

		Collection<Pair<Object, Method>> messageList = messages.get(modid + ":" + messageName);
		if (messageList.size() == 0)
		{
			MalisisCore.log.warn("No message handler matching the parameters passed for {}", modid + ":" + messageName);
			return;
		}

		for (Pair<Object, Method> message : messageList)
		{
			if (checkParameters(message.getRight(), data))
			{
				try
				{
					message.getRight().invoke(message.getLeft(), data);
				}
				catch (ReflectiveOperationException e)
				{
					MalisisCore.log.warn("An error happened processing the message :", e);
				}
			}
		}

	}

	/**
	 * Checks if parameters passed match the parameters required for the {@link Method}.
	 *
	 * @param method the method
	 * @param data the data
	 * @return true, if successful
	 */
	private static boolean checkParameters(Method method, Object... data)
	{
		Parameter[] parameters = method.getParameters();
		if (data == null)
			return parameters.length == 0;

		if (parameters.length != data.length)
			return false;

		for (int i = 0; i < parameters.length; i++)
		{
			Class<?> paramClass = parameters[i].getType();
			if (data[i] != null)
			{
				Class<?> dataClass = data[i].getClass();
				if (!ClassUtils.isAssignable(dataClass, paramClass, true))
					return false;
			}
		}

		return true;
	}

}
