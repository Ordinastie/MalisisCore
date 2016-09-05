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

package net.malisis.core.util.callback;

import java.util.List;

import net.malisis.core.util.callback.ICallback.CallbackOption;
import net.malisis.core.util.callback.ICallback.ICallbackPredicate;
import net.malisis.core.util.callback.ICallback.Priority;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

/**
 * A {@link CallbackRegistry} allows the registration and processing of {@link ICallback ICallbacks}.<br>
 * Users are encouraged to have a custom implementation that will expose a more specialized registration and process for the
 * {@code ICallback}.
 *
 * @author Ordinastie
 * @param <C> the type of {@link ICallback}
 * @param <P> the type of {@link ICallbackPredicate}
 * @param <V> the type of the return value for the {@code ICallback}
 */
public class CallbackRegistry<C extends ICallback<V>, P extends ICallbackPredicate, V>
{
	/** List of registered {@link ICallback}. */
	protected List<Pair<C, CallbackOption>> callbacks = Lists.newArrayList();

	/**
	 * Registers a {@link ICallback} to be call when the {@link ICallbackPredicate} returns true.
	 *
	 * @param callback the callback
	 * @param predicate the predicate
	 */
	public void registerCallback(C callback, CallbackOption option)
	{
		callbacks.add(Pair.of(callback, option));
		callbacks = Ordering.natural()
							.reverse()
							.onResultOf(Priority::ordinal)
							.onResultOf(CallbackOption::getPriority)
							.onResultOf(Pair<C, CallbackOption>::getRight)
							.sortedCopy(callbacks);
	}

	/**
	 * Processes the registered {@link ICallback ICallbacks} according to their priority, and returns the first non null return value
	 * encountered.<br>
	 * Classes extending {@link CallbackRegistry} should expose a more specialized version of this method, with specific arguments.
	 *
	 * @param params the params
	 * @return the v
	 */
	public V processCallbacks(Object... params)
	{
		if (callbacks.size() == 0)
			return null;

		V result = null;
		for (Pair<C, CallbackOption> pair : callbacks)
		{
			V tempRes = null;
			if (pair.getRight().apply(params))
				tempRes = pair.getLeft().call(params);
			if (result == null && tempRes != null)
				result = tempRes;
		}

		return result;
	}
}
