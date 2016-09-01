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
 * {@link ICallback ICallbacks} is a flexible interface to be used for {@link CallbackRegistry CallbackRegistries}.<br>
 * Users are encouraged to extend the {@code ICallback} with default implementation for {@link #call(Object...)} that should redirect to a
 * more specialized {@code callback()} method with more specific arguments.<br>
 * It should go along with a custom {@code CallbackRegistry} implement dedicated for that callback extension.
 *
 * @author Ordinastie
 *
 */
public interface ICallback<T> extends Comparable<ICallback<T>>
{
	public static enum Priority
	{
		LOWEST,
		LOW,
		NORMAL,
		HIGH,
		HIGHEST;
	}

	/**
	 * Gets the priority for this {@link ICallback}
	 *
	 * @return the priority
	 */
	public default Priority getPriority()
	{
		return Priority.NORMAL;
	}

	/**
	 * Process this {@link ICallback}
	 *
	 * @param params the params
	 * @return the t
	 */
	public T call(Object... params);

	/**
	 * Sets the {@link ICallback} ordering based on its priority.
	 *
	 * @param callback the callback
	 * @return the int
	 */
	@Override
	public default int compareTo(ICallback<T> callback)
	{
		return callback.getPriority().ordinal() - getPriority().ordinal();
	}

	/**
	 * {@link ICallbackPredicate} are used for registering {@link ICallback} in {@link CallbackRegistry} to determine in which circumstances
	 * the {@code ICallback} should be called.
	 */
	public static interface ICallbackPredicate
	{

		/**
		 * Returns whether the associated {@link ICallback} should be called or not.
		 *
		 * @param params the params
		 * @return true, if successful
		 */
		public boolean apply(Object... params);
	}
}
