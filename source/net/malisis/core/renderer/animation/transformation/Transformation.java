package net.malisis.core.renderer.animation.transformation;

import net.malisis.core.renderer.element.Shape;

public abstract class Transformation<T extends Transformation>
{
	public static final int LINEAR = 0, SINUSOIDAL = 1;

	public int movement = LINEAR;
	protected int duration, delay = 0;
	protected int loops = 1, loopStartDelay = 0, loopResetDelay = 0;
	protected float elapsedTimeCurrentLoop;

	public T movement(int movement)
	{
		this.movement = movement;
		return (T) this;
	}

	public T delay(int delay)
	{
		this.delay = delay;
		return (T) this;
	}

	public T forTicks(int duration)
	{
		return forTicks(duration, 0);
	}

	public T forTicks(int duration, int delay)
	{
		if (this.duration == 0)
		{
			this.duration = duration;
			this.delay = delay;
		}
		return (T) this;
	}

	public int getDuration()
	{
		return duration;
	}

	public int getDelay()
	{
		return delay;
	}

	public int getLoops()
	{
		return loops;
	}

	public int totalDuration()
	{
		if (loops == -1)
			return Integer.MAX_VALUE;

		return delay + loops * getLoopDuration();
	}

	public int getLoopDuration()
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

	public void transform(Shape s, float elapsedTime)
	{
		doTransform(s, completion(Math.max(0, elapsedTime)));
	}

	protected float completion(float elapsedTime)
	{
		if (duration == 0)
			return 0;

		float comp = 0;
		int loopDuration = getLoopDuration();
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

		comp = Math.min(elapsedTimeCurrentLoop / duration, 1);
		if (movement == SINUSOIDAL)
		{
			comp = (float) (1 - Math.cos(comp * Math.PI)) / 2;
		}

		comp = Math.max(0, Math.min(1, comp));

		return comp;
	}

	protected abstract void doTransform(Shape s, float comp);

	public abstract T reversed(boolean reversed);
}
