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

package net.malisis.core.renderer.animation.transformation;

import java.util.List;

import net.malisis.core.util.Timer;

public abstract class Transformation<T extends Transformation<T, S>, S extends ITransformable>
{
	public static final int LINEAR = 0, SINUSOIDAL = 1;

	/** Type of movement. */
	protected int movement = LINEAR;
	/** Duration of this {@link Transformation} in ticks. */
	protected long duration = 0;
	/** Delay of this {@link Transformation} in ticks. */
	protected long delay = 0;
	/** Number of loops to do. -1 for infinite. */
	protected int loops = 1;
	/** Delay between loops. */
	protected long loopStartDelay = 0, loopResetDelay = 0;
	/** Amount of time passed wihtin current loop. */
	protected long elapsedTimeCurrentLoop;
	/** Whether this {@link Transformation} is in reversed.. */
	protected boolean reversed = false;

	/**
	 * Sets the movement for this {@link Transformation}.
	 *
	 * @param movement the movement
	 * @return the t
	 */
	public T movement(int movement)
	{
		this.movement = movement;
		return self();
	}

	/**
	 * Sets the delay for this {@link Transformation}.
	 *
	 * @param delay the delay
	 * @return the t
	 */
	public T delay(int delay)
	{
		this.delay = Timer.tickToTime(delay);
		return self();
	}

	/**
	 * Sets the duration for this {@link Transformation}.
	 *
	 * @param duration the duration
	 * @return the t
	 */
	public T forTicks(int duration)
	{
		this.duration = Timer.tickToTime(duration);
		return self();
	}

	/**
	 * Sets the duration and delay for this {@link Transformation}.
	 *
	 * @param duration the duration
	 * @param delay the delay
	 * @return the t
	 */
	public T forTicks(int duration, int delay)
	{
		this.duration = Timer.tickToTime(duration);
		this.delay = Timer.tickToTime(delay);

		return self();
	}

	/**
	 * Gets the duration of this {@link Transformation}
	 *
	 * @return the duration
	 */
	public long getDuration()
	{
		return duration;
	}

	/**
	 * Gets the delay of this {@link Transformation}
	 *
	 * @return the delay
	 */
	public long getDelay()
	{
		return delay;
	}

	/**
	 * Gets the number of loops of this {@link Transformation}
	 *
	 * @return the loops
	 */
	public int getLoops()
	{
		return loops;
	}

	/**
	 * Gets the total duration of this {@link Transformation}.
	 *
	 * @return the long
	 */
	public long totalDuration()
	{
		if (loops == -1)
			return Integer.MAX_VALUE;

		return delay + loops * getLoopDuration();
	}

	/**
	 * Gets the duration of one loop of this {@link Transformation}
	 *
	 * @return the loop duration
	 */
	public long getLoopDuration()
	{
		return duration + loopStartDelay + loopResetDelay;
	}

	/**
	 * Sets the number of loop for this {@link Transformation}.
	 *
	 * @param loops the loops
	 * @return the t
	 */
	public T loop(int loops)
	{
		return loop(loops, 0, 0);
	}

	/**
	 * Sets the number of loops, with the specified delays for this {@link Transformation}.
	 *
	 * @param loops the loops
	 * @param startDelay the start delay
	 * @param resetDelay the reset delay
	 * @return the t
	 */
	public T loop(int loops, int startDelay, int resetDelay)
	{
		if (loops == 0)
			return self();

		this.loops = loops;
		this.loopStartDelay = Timer.tickToTime(startDelay);
		this.loopResetDelay = Timer.tickToTime(resetDelay);
		return self();
	}

	/**
	 * Sets this {@link Transformation} to be reversed.
	 *
	 * @param reversed the reversed
	 * @return the t
	 */
	public T reversed(boolean reversed)
	{
		this.reversed = reversed;
		return self();
	}

	/**
	 * Applies this {@link Transformation} to the {@link ITransformable}.
	 *
	 * @param transformables the transformables
	 * @param elapsedTime the elapsed time
	 */
	public void transform(List<S> transformables, long elapsedTime)
	{
		for (S transformable : transformables)
			transform(transformable, elapsedTime);
	}

	/**
	 * Applies this {@link Transformation} to the {@link ITransformable}
	 *
	 * @param transformable the transformable
	 * @param elapsedTime the elapsed time
	 */
	public void transform(S transformable, long elapsedTime)
	{
		doTransform(transformable, completion(Math.max(0, elapsedTime)));
	}

	/**
	 * Calculates the completion of this {@link Transformation} based on the movement and the elapsed time.
	 *
	 * @param elapsedTime the elapsed time
	 * @return the float
	 */
	protected float completion(long elapsedTime)
	{
		if (duration == 0)
			return 0;

		float comp = 0;
		long loopDuration = getLoopDuration();
		elapsedTimeCurrentLoop = elapsedTime - delay;

		if (loops != -1 && elapsedTimeCurrentLoop > loops * loopDuration)
			return 1;

		if (loops != 1)
		{
			elapsedTimeCurrentLoop %= loopDuration;
			if (elapsedTimeCurrentLoop < loopStartDelay)
				return 0;
			if (elapsedTimeCurrentLoop + loopResetDelay > loopDuration)
				return 1;
			elapsedTimeCurrentLoop -= loopStartDelay;
		}

		comp = Math.min((float) elapsedTimeCurrentLoop / duration, 1);
		comp = Math.max(0, Math.min(1, comp));
		if (movement == SINUSOIDAL)
		{
			comp = (float) (1 - Math.cos(comp * Math.PI)) / 2;
		}

		//MalisisCore.message(comp);
		return comp;
	}

	/**
	 * Gets this {@link Transformation}.
	 *
	 * @return the t
	 */
	public abstract T self();

	/**
	 * Calculates the transformation.
	 *
	 * @param transformable the transformable
	 * @param comp the comp
	 */
	protected abstract void doTransform(S transformable, float comp);

}
