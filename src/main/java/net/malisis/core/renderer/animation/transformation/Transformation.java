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

public abstract class Transformation<T extends Transformation, S extends ITransformable>
{
	public static final int LINEAR = 0, SINUSOIDAL = 1;

	protected int movement = LINEAR;
	protected long duration, delay = 0;
	protected int loops = 1;
	protected long loopStartDelay = 0, loopResetDelay = 0;
	protected long elapsedTimeCurrentLoop;
	protected boolean reversed = false;

	public T movement(int movement)
	{
		this.movement = movement;
		return (T) this;
	}

	public T delay(int delay)
	{
		this.delay = Timer.tickToTime(delay);
		return (T) this;
	}

	public T forTicks(int duration)
	{
		this.duration = Timer.tickToTime(duration);
		return (T) this;
	}

	public T forTicks(int duration, int delay)
	{
		this.duration = Timer.tickToTime(duration);
		this.delay = Timer.tickToTime(delay);

		return (T) this;
	}

	public long getDuration()
	{
		return duration;
	}

	public long getDelay()
	{
		return delay;
	}

	public int getLoops()
	{
		return loops;
	}

	public long totalDuration()
	{
		if (loops == -1)
			return Integer.MAX_VALUE;

		return delay + loops * getLoopDuration();
	}

	public long getLoopDuration()
	{
		return duration + loopStartDelay + loopResetDelay;
	}

	public T loop(int loops)
	{
		return loop(loops, 0, 0);
	}

	public T loop(int loops, int startDelay, int resetDelay)
	{
		if (loops == 0)
			return (T) this;

		this.loops = loops;
		this.loopStartDelay = startDelay;
		this.loopResetDelay = resetDelay;
		return (T) this;
	}

	public void transform(List<S> transformables, long elapsedTime)
	{
		for (S transformable : transformables)
			transform(transformable, elapsedTime);
	}

	public void transform(S transformable, long elapsedTime)
	{
		doTransform(transformable, completion(Math.max(0, elapsedTime)));
	}

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
			if (elapsedTimeCurrentLoop - loopResetDelay > loopDuration)
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

	public T reversed(boolean reversed)
	{
		this.reversed = reversed;
		return (T) this;
	}

	protected abstract void doTransform(S transformable, float comp);

}
