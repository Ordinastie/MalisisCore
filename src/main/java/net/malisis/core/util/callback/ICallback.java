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

/**
 * {@link ICallback ICallbacks} is a flexible interface to be used for {@link CallbackRegistry CallbackRegistries}.<br>
 * Users are encouraged to extend the {@code ICallback} with default implementation for {@link #call(Object...)} that should redirect to a
 * more specialized {@code callback()} method with more specific arguments.<br>
 * It should go along with a custom {@code CallbackRegistry} implement dedicated for that callback extension.
 *
 * @author Ordinastie
 *
 */
public interface ICallback<T>
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
	 * Process this {@link ICallback}
	 *
	 * @param params the params
	 * @return the t
	 */
	public T call(Object... params);

	/**
	 * {@link ICallbackPredicate} are used for registering {@link ICallback} in {@link CallbackRegistry} to determine in which circumstances
	 * the {@code ICallback} should be called.
	 */
	public static interface ICallbackPredicate
	{
		/** Always true predicate. */
		static ICallbackPredicate ALWAYS_TRUE = params -> true;
		/** Always false predicate. */
		static ICallbackPredicate ALWAYS_FALSE = params -> false;

		/**
		 * Returns whether the associated {@link ICallback} should be called or not.
		 *
		 * @param params the params
		 * @return true, if successful
		 */
		public boolean apply(Object... params);

		/**
		 * Returns a predicates that is the logical AND composition of this {@link ICallbackPredicate} and the passed one.
		 *
		 * @param predicate the predicate
		 * @return the i callback predicate
		 */
		public default ICallbackPredicate and(ICallbackPredicate predicate)
		{
			checkNotNull(predicate);
			return (params) -> apply(params) && predicate.apply(params);
		}

		/**
		 * Returns a new {@link ICallback} that is the negation of the one passed.
		 *
		 * @return the i callback predicate
		 */
		public default ICallbackPredicate negate()
		{
			return (params) -> !apply(params);
		}

		/**
		 * Returns a predicates that is the logical OR composition of this {@link ICallbackPredicate} and the passed one.
		 *
		 * @param predicate the predicate
		 * @return the i callback predicate
		 */
		public default ICallbackPredicate or(ICallbackPredicate predicate)
		{
			checkNotNull(predicate);
			return (params) -> apply(params) || predicate.apply(params);
		}

		/**
		 * Return a predicate that always returns true.
		 *
		 * @return the i callback predicate
		 */
		public static ICallbackPredicate alwaysTrue()
		{
			return ALWAYS_TRUE;
		}

		/**
		 * Returns a predicate that always returns false.
		 *
		 * @return the i callback predicate
		 */
		public static ICallbackPredicate alwaysFalse()
		{
			return ALWAYS_FALSE;
		}
	}

	/**
	 * The {@link CallbackOption} holds the {@link ICallbackPredicate} and {@link Priority} necessary for the associated {@link ICallback}
	 * to be executed.
	 */
	public static class CallbackOption
	{
		private static final CallbackOption NONE = new CallbackOption(ICallbackPredicate.alwaysTrue(), Priority.NORMAL);
		private ICallbackPredicate predicate = (params) -> true;
		private Priority priority;

		private CallbackOption(ICallbackPredicate predicate, Priority priority)
		{
			this.predicate = predicate;
			this.priority = priority;
		}

		/**
		 * Gets the {@link Priority} of this {@link CallbackOption}
		 *
		 * @return the priority
		 */
		public Priority getPriority()
		{
			return priority;
		}

		/**
		 * Returns the result of the {@link ICallbackPredicate} held by this {@link CallbackOption}.
		 *
		 * @param params the params
		 * @return true, if successful
		 */
		public boolean apply(Object... params)
		{
			return predicate.apply(params);
		}

		/**
		 * Constructs an empty {@link CallbackOption}, with predicate {@link ICallbackPredicate#alwaysTrue()} and {@link Priority#NORMAL}
		 * priority.
		 *
		 * @param predicate the predicate
		 * @return the callback option
		 */
		public static CallbackOption of()
		{
			return NONE;
		}

		/**
		 * Constructs an empty {@link CallbackOption}, with {@link Priority#NORMAL} priority and the specified {@link ICallbackPredicate}.
		 *
		 * @param predicate the predicate
		 * @return the callback option
		 */
		public static CallbackOption of(ICallbackPredicate predicate)
		{
			return new CallbackOption(checkNotNull(predicate), Priority.NORMAL);
		}

		/**
		 * Constructs a {@link CallbackOption} with predicate {@link ICallbackPredicate#alwaysTrue()} and the specified {@link Priority}
		 *
		 * @param priority the priority
		 * @return the callback option
		 */
		public static CallbackOption of(Priority priority)
		{
			return new CallbackOption(ICallbackPredicate.alwaysTrue(), priority);
		}

		/**
		 * Constructs an empty {@link CallbackOption}, with specified {@link ICallbackPredicate} and {@link Priority}
		 *
		 * @param predicate the predicate
		 * @param priority the priority
		 * @return the callback option
		 */
		public static CallbackOption of(ICallbackPredicate predicate, Priority priority)
		{
			return new CallbackOption(checkNotNull(predicate), priority);
		}
	}
}
