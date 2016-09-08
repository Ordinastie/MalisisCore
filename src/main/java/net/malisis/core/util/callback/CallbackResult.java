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

/**
 * @author Ordinastie
 *
 */
public class CallbackResult<U>
{
	private static CallbackResult<?> NO_RESULT = CallbackResult.of(null);
	private static CallbackResult<?> RETURN_NULL = CallbackResult.builder().withReturn(true).result();
	private static CallbackResult<Boolean> RETURN_FALSE = CallbackResult.<Boolean> builder().value(false).withReturn(true).result();
	private static CallbackResult<Boolean> RETURN_TRUE = CallbackResult.<Boolean> builder().value(false).withReturn(true).result();

	private U value = null;
	private boolean shouldReturn = false;
	private boolean cancelCallbacks = false;
	private boolean forceCancel = false;

	private CallbackResult(U value, boolean shouldReturn, boolean cancelCallbacks, boolean forceCancel)
	{
		this.value = value;
		this.shouldReturn = shouldReturn;
		this.cancelCallbacks = cancelCallbacks;
		this.forceCancel = forceCancel;
	}

	public U getValue()
	{
		return value;
	}

	public boolean shouldReturn()
	{
		return shouldReturn;
	}

	public boolean isCancelled()
	{
		return cancelCallbacks;
	}

	public boolean isForcedCancelled()
	{
		return forceCancel;
	}

	public static <U> CallbackResult<U> of(U value)
	{
		return new CallbackResult<>(value, false, false, false);
	}

	@SuppressWarnings("unchecked")
	public static <U> CallbackResult<U> noResult()
	{
		return (CallbackResult<U>) NO_RESULT;
	}

	@SuppressWarnings("unchecked")
	public static <U> CallbackResult<U> returnNull()
	{
		return (CallbackResult<U>) RETURN_NULL;
	}

	public static CallbackResult<Boolean> returnTrue()
	{
		return RETURN_TRUE;
	}

	public static CallbackResult<Boolean> returnFalse()
	{
		return RETURN_FALSE;
	}

	public static <U> CallbackResultBuilder<U> builder()
	{
		return new CallbackResultBuilder<>();
	}

	public static class CallbackResultBuilder<U>
	{
		private U value = null;
		private boolean shouldReturn = false;
		private boolean cancelCallbacks = false;
		private boolean forceCancel = false;

		private CallbackResultBuilder()
		{

		}

		public CallbackResultBuilder<U> value(U value)
		{
			this.value = value;
			return this;
		}

		public CallbackResultBuilder<U> withReturn(boolean shouldReturn)
		{
			this.shouldReturn = shouldReturn;
			return this;
		}

		public CallbackResultBuilder<U> cancel(boolean cancel)
		{
			this.cancelCallbacks = cancel;
			return this;
		}

		public CallbackResultBuilder<U> forceCancel(boolean forceCancel)
		{
			this.forceCancel = forceCancel;
			return this;
		}

		public CallbackResult<U> result()
		{
			return new CallbackResult<>(value, shouldReturn, cancelCallbacks, forceCancel);
		}
	}

}
