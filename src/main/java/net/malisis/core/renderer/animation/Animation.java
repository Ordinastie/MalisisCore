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

package net.malisis.core.renderer.animation;

import com.google.common.base.Preconditions;

import net.malisis.core.renderer.animation.transformation.ITransformable;
import net.malisis.core.renderer.animation.transformation.Transformation;
import net.malisis.core.util.Timer;

/**
 * The {@link Animation} class represent a element to animate ({@link ITransformable}) with its {@link Transformation}.
 *
 * @author Ordinastie
 * @param <S> the generic type
 */
public class Animation<S extends ITransformable>
{
	/** Element to animate. */
	private S transformable;
	/** {@link Transformation} to use for the animation. */
	private Transformation<?, S> transform;
	/** Delay before animated. */
	private int delay;
	/** Whether the animation has already start. */
	private boolean started = false;
	/** Whether the animation has already finished. */
	private boolean finished = false;

	/** Whether the {@link ITransformable} should be rendered before the animation has started. */
	private boolean renderBefore = true;
	/** Whether the {@link ITransformable} should be rendered before the animation has finished. */
	private boolean renderAfter = true;

	/**
	 * Instantiates a new {@link Animation}.
	 *
	 * @param transformable the transformable
	 * @param transform the transform
	 */
	public Animation(S transformable, Transformation<?, S> transform)
	{
		Preconditions.checkNotNull(transformable);
		Preconditions.checkNotNull(transform);
		this.transformable = transformable;
		this.transform = transform;
	}

	/**
	 * Gets the {@link ITransformable} for this {@link Animation}.
	 *
	 * @return the transformable
	 */
	public S getTransformable()
	{
		return transformable;
	}

	/**
	 * Gets the {@link Transformation} for this {@link Animation}.
	 *
	 * @return the transformation
	 */
	public Transformation<?, S> getTransformation()
	{
		return transform;
	}

	/**
	 * Sets whether to render the {@link ITransformable} before and after the animation.
	 *
	 * @param before the before
	 * @param after the after
	 */
	public void setRender(boolean before, boolean after)
	{
		renderBefore = before;
		renderAfter = after;
	}

	/**
	 * Checks if this {@link Animation} is started.
	 *
	 * @return true, if is started
	 */
	public boolean isStarted()
	{
		return started;
	}

	/**
	 * Checks if this {@link Animation} is finished.
	 *
	 * @return true, if is finished
	 */
	public boolean isFinished()
	{
		return finished;
	}

	/**
	 * Sets the delay for the animation.
	 *
	 * @param delay the new delay
	 */
	public void setDelay(int delay)
	{
		this.delay = delay;
	}

	/**
	 * Checks if the animation should persist once the animation is finished.
	 *
	 * @return true, if successful
	 */
	public boolean persistance()
	{
		return renderAfter;
	}

	/**
	 * Animates this {@link Animation}.<br>
	 * Sets the {@link #started} and {@link #finished} status.
	 *
	 * @param timer the timer
	 * @return the s
	 */
	public S animate(Timer timer)
	{
		long elapsed = timer.elapsedTime() - Timer.tickToTime(delay);
		started = elapsed > transform.getDelay();
		finished = elapsed > transform.totalDuration() && transform.getLoops() != -1;

		if (!started && !renderBefore)
			return null;
		if (finished && !renderAfter)
			return null;

		transform.transform(transformable, elapsed);
		return transformable;
	}
}
