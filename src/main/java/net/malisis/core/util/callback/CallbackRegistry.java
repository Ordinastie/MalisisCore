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

import static com.google.common.base.Preconditions.*;

import java.util.List;
import java.util.function.BiFunction;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

import net.malisis.core.util.callback.ICallback.CallbackOption;
import net.malisis.core.util.callback.ICallback.ICallbackPredicate;
import net.malisis.core.util.callback.ICallback.Priority;

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
	protected List<Pair<C, CallbackOption<P>>> callbacks = Lists.newArrayList();

	protected BiFunction<CallbackResult<V>, CallbackResult<V>, CallbackResult<V>> reduce = this::doReduce;

	private CallbackResult<V> doReduce(CallbackResult<V> r1, CallbackResult<V> r2)
	{
		if (r1 == CallbackResult.noResult())
			return r2 != null ? r2 : CallbackResult.noResult();
		return r1 != null ? r1 : CallbackResult.noResult();
	}

	public void reduce(BiFunction<CallbackResult<V>, CallbackResult<V>, CallbackResult<V>> reduce)
	{
		this.reduce = checkNotNull(reduce);
	}

	/**
	 * Registers a {@link ICallback} to be call when the {@link ICallbackPredicate} returns true.
	 *
	 * @param callback the callback
	 * @param option the option
	 */
	public void registerCallback(C callback, CallbackOption<P> option)
	{
		callbacks.add(Pair.of(callback, option));
		callbacks = Ordering.natural()
							.reverse()
							.onResultOf(Priority::ordinal)
							.onResultOf(CallbackOption<P>::getPriority)
							.onResultOf(Pair<C, CallbackOption<P>>::getRight)
							.sortedCopy(callbacks);
	}

	/**
	 * Processes the registered {@link ICallback ICallbacks} according to their priority.
	 *
	 * @param params the params
	 * @return the v
	 */
	public CallbackResult<V> processCallbacks(Object... params)
	{
		if (callbacks.size() == 0)
			return CallbackResult.noResult();

		CallbackResult<V> result = CallbackResult.noResult();
		Priority lastPriority = Priority.HIGHEST;
		for (Pair<C, CallbackOption<P>> pair : callbacks)
		{
			if (result.isCancelled() && pair.getRight().getPriority() != lastPriority)
				return result;

			if (pair.getRight().apply(params))
			{
				CallbackResult<V> tmp = pair.getLeft().call(params);
				result = reduce.apply(result, tmp);
				if (result.isForcedCancelled())
					return result;

				lastPriority = pair.getRight().getPriority();
			}
		}

		return result;
	}
}
