package net.malisis.core.renderer.animation;

import net.malisis.core.renderer.element.Shape;

public abstract class Animation
{
	public static final int LINEAR = 0, SINUSOIDAL = 1;
	
	public int movement = LINEAR;
	protected float x, y, z;
	protected int duration, delay = 0;
	protected int loops = 1, loopStartDelay = 0, loopResetDelay = 0;
	protected Animation prev, next;
	
	protected Animation(float x, float y, float z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Animation movement(int movement)
	{
		this.movement = movement;
		return this; 
	}
	
	public Animation delay(int delay)
	{
		this.delay = delay;
		return this;
	}

	public Animation forTicks(int duration, int delay)
	{
		if (this.duration == 0)
		{
			this.duration = duration;
			this.delay = delay;
		}
		return this;
	}

	public Animation loop(int loops, int startDelay, int resetDelay)
	{
		if(loops == 0)
			return this;
		
		this.loops = loops;
		this.loopStartDelay = startDelay;
		this.loopResetDelay = resetDelay;
		return this;
	}

	public void transformAll(Shape s, float elapsedTime)
	{
		animate(s, completion(elapsedTime));
		if (next != null)
			next.transformAll(s, elapsedTime);
	}

	protected float completion(float elapsedTime)
	{
		if (duration == 0)
			return 0;

		float comp = 0;
		int loopDuration = duration + loopStartDelay + loopResetDelay;
		float elapsed = elapsedTime - delay;
		
		if(loops != -1 && elapsed > loops * loopDuration)
			return 1;
				
		if(loops != 1)
		{
			elapsed %= loopDuration;
			if (elapsed < loopStartDelay)
				return 0;
			if (elapsed - loopResetDelay > loopDuration)
				return 1;
			elapsed -= loopStartDelay;
		}

		comp = Math.min(elapsed / duration, 1);
		if (movement == SINUSOIDAL)
		{
			comp = (float) (1 - Math.cos(comp * Math.PI)) / 2;
		}

		return comp;
	}

	protected abstract void animate(Shape s, float comp);
}
