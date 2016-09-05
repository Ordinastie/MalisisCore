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

package net.malisis.core.registry;

import net.malisis.core.registry.ModEventRegistry.FMLEventPredicate;
import net.malisis.core.registry.ModEventRegistry.IFMLEventCallback;
import net.malisis.core.util.callback.CallbackRegistry;
import net.malisis.core.util.callback.ICallback;
import net.malisis.core.util.callback.ICallback.CallbackOption;
import net.malisis.core.util.callback.ICallback.ICallbackPredicate;
import net.minecraftforge.fml.common.event.FMLEvent;

/**
 * @author Ordinastie
 *
 */
@SuppressWarnings("unchecked")
public class ModEventRegistry extends CallbackRegistry<IFMLEventCallback<?>, FMLEventPredicate<?>, Void>
{
	/**
	 * Use {@link #registerCallback(Class, IFMLEventCallback)} instead.
	 *
	 * @param callback the callback
	 */
	@Override
	@Deprecated
	public void registerCallback(IFMLEventCallback<?> callback, CallbackOption option)
	{
		throw new IllegalAccessError("Do not use this method, use registerCallback(Class, IFMLEventCallback) instead.");
	}

	/**
	 * Registers a {@link IFMLEventCallback} that will be called when MalisisCore will reach the specified {@link FMLEvent}.<br>
	 * Note that the callbacks are propcessed during MalisisCore events and not the child mods.
	 *
	 * @param <T> the generic type
	 * @param clazz the clazz
	 * @param consumer the consumer
	 */
	public <T extends FMLEvent> void registerCallback(Class<T> clazz, IFMLEventCallback<T> consumer)
	{
		super.registerCallback(consumer, CallbackOption.of((FMLEventPredicate<T>) clazz::isInstance));
	}

	/**
	 * Process the {@link IFMLEventCallback} registered for the specified {@link FMLEvent}.
	 *
	 * @param event the event
	 */
	public void processCallbacks(FMLEvent event)
	{
		super.processCallbacks(event);
	}

	/**
	 * Specialized {@link ICallback} for {@link FMLEvent FMLEvents}.
	 *
	 * @param <T> the generic type
	 */
	public static interface IFMLEventCallback<T extends FMLEvent> extends ICallback<Void>
	{
		@Override
		public default Void call(Object... params)
		{
			call((T) params[0]);
			return null;
		}

		public void call(T event);
	}

	/**
	 * Specialized {@link ICallbackPredicate} for {@link FMLEvent FMLEvents}
	 *
	 * @param <T> the generic type
	 */
	public static interface FMLEventPredicate<T extends FMLEvent> extends ICallbackPredicate
	{
		@Override
		public default boolean apply(Object... params)
		{
			return apply((T) params[0]);
		}

		public boolean apply(T event);
	}
}
