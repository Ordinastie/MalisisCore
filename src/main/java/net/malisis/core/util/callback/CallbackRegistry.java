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

import net.malisis.core.util.callback.ICallback.ICallbackPredicate;
import net.malisis.core.util.callback.ICallback.Priority;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

/**
 * @author Ordinastie
 * @param <V>
 *
 */
public class CallbackRegistry<C extends ICallback<V>, P extends ICallbackPredicate, V>
{
	private List<Pair<C, P>> callbacks = Lists.newArrayList();

	public CallbackRegistry()
	{}

	@SuppressWarnings("unchecked")
	public void registerCallback(C callback)
	{
		registerCallback(callback, (P) (ICallbackPredicate) params -> true);
	}

	public void registerCallback(C callback, P predicate)
	{
		callbacks.add(Pair.of(callback, predicate));
		callbacks = Ordering.natural().onResultOf(Pair<C, P>::getLeft).sortedCopy(callbacks);
	}

	public CallbackResult<V> processCallbacks(Object... params)
	{
		CallbackResult<V> result = null;
		Priority currentPriority = Priority.HIGHEST;
		boolean isCancelled = false;
		for (Pair<C, P> pair : callbacks)
		{
			C callback = pair.getLeft();
			if (callback.getPriority() != currentPriority && isCancelled)
				break;

			if (pair.getRight().apply(params))
				callback.callback(params);
			isCancelled |= callback.isCancelled();
			currentPriority = callback.getPriority();
			if (result == null && callback.shouldReturn())
				result = CallbackResult.of(callback.shouldReturn(), callback.returnValue());
		}

		return Objects.firstNonNull(result, CallbackResult.<V> noReturn());
	}

	public static class CallbackResult<U>
	{
		private static CallbackResult<?> NO_RETURN = new CallbackResult<>(false, null);
		private static CallbackResult<?> NULL_RETURN = new CallbackResult<>(true, null);

		private boolean shouldReturn = false;
		private U returnValue;

		private CallbackResult(boolean shouldReturn, U returnValue)
		{
			this.shouldReturn = shouldReturn;
			this.returnValue = returnValue;
		}

		public boolean shouldReturn()
		{
			return shouldReturn;
		}

		public U getValue()
		{
			return returnValue;
		}

		public static <U> CallbackResult<U> of(boolean shouldReturn, U returnValue)
		{
			return new CallbackResult<>(shouldReturn, returnValue);
		}

		@SuppressWarnings("unchecked")
		public static <U> CallbackResult<U> noReturn()
		{
			return (CallbackResult<U>) NO_RETURN;
		}

		@SuppressWarnings("unchecked")
		public static <U> CallbackResult<U> nullReturn()
		{
			return (CallbackResult<U>) NULL_RETURN;
		}
	}

}
