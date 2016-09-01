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

import net.malisis.core.asm.AsmHook;
import net.malisis.core.util.callback.ASMCallbackRegistry.CallbackResult;
import net.malisis.core.util.callback.ASMCallbackRegistry.IASMCallback;
import net.malisis.core.util.callback.ICallback.ICallbackPredicate;

/**
 * {@link ASMCallbackRegistry} is a registry specialized for {@link AsmHook AsmHooks}.<br>
 *
 * @author Ordinastie
 * @param <V> the value type
 */
public class ASMCallbackRegistry<V> extends CallbackRegistry<IASMCallback<V>, ICallbackPredicate, CallbackResult<V>>
{
	public ASMCallbackRegistry()
	{}

	public static interface IASMCallback<T> extends ICallback<CallbackResult<T>>
	{
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
