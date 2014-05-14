package net.malisis.core.renderer;

import net.malisis.core.renderer.element.Shape;
import net.minecraft.client.Minecraft;

public class AnimationRenderer
{
	private BaseRenderer renderer;
	private long startTime;
	private long worldTotalTime;
	private int globalDelay;
	private float partialTick;
	private float elapsedTime;

	private Animation firstTransform;
	private Animation lastTransform;
	private boolean allowReset = true;

	public AnimationRenderer(BaseRenderer renderer)
	{
		this(renderer, 0);
	}

	public AnimationRenderer(BaseRenderer renderer, long start)
	{
		this.renderer = renderer;
		this.startTime = start;
		this.worldTotalTime = Minecraft.getMinecraft().theWorld.getTotalWorldTime();
		this.partialTick = this.renderer.partialTick;
		this.elapsedTime = worldTotalTime - startTime + partialTick;
	}

	private void reset()
	{
		if (!allowReset)
			return;

		firstTransform = null;
		lastTransform = null;
		globalDelay = 0;
	}

	public long getStartTime()
	{
		return startTime;
	}

	public float getElapsedTime()
	{
		return elapsedTime;
	}

	private void addTransformation(Animation transform)
	{
		if (firstTransform == null)
		{
			firstTransform = transform;
			lastTransform = transform;
		}
		else
		{
			lastTransform.next = transform;
			transform.prev = lastTransform;
			lastTransform = transform;
		}
	}

	public AnimationRenderer nextAnimation(int delay)
	{
		startTime += delay;
		elapsedTime = worldTotalTime - startTime + partialTick;
		return this;
	}

	public boolean animationReady()
	{
		return elapsedTime >= 0;
	}

	public AnimationRenderer translate(float x, float y, float z)
	{
		addTransformation(new Translation(x, y, z));
		return this;
	}

	public AnimationRenderer rotate(float angle, float x, float y, float z, float offsetX, float offsetY, float offsetZ)
	{
		addTransformation(new Rotation(angle, x, y, z, offsetX, offsetY, offsetZ));
		return this;
	}

	public AnimationRenderer rotate(float angle, float x, float y, float z)
	{
		return rotate(angle, x, y, z, 0, 0, 0);
	}

	public AnimationRenderer scaleFrom(float f)
	{
		return scaleFrom(f, f, f);
	}

	public AnimationRenderer scaleFrom(float x, float y, float z)
	{
		addTransformation(new Scale(x, y, z));
		return this;
	}

	public AnimationRenderer scaleTo(float f)
	{
		return scaleTo(f, f, f);
	}

	public AnimationRenderer scaleTo(float x, float y, float z)
	{
		if (lastTransform instanceof Scale)
			((Scale) lastTransform).scaleTo(x, y, z);
		return this;
	}

	public AnimationRenderer globalDelay(int delay)
	{
		globalDelay += delay;
		return this;
	}

	public AnimationRenderer clearDelay()
	{
		globalDelay = 0;
		return this;
	}

	public AnimationRenderer forTicks(int duration)
	{
		return forTicks(duration, 0);
	}

	public AnimationRenderer forTicks(int duration, int delay)
	{
		if (lastTransform != null)
			lastTransform.forTicks(duration, delay + globalDelay);
		return this;
	}

	public AnimationRenderer loop(int loops)
	{
		return loop(loops, 0, 0);
	}
	public AnimationRenderer loop(int loops, int startDelay, int resetDelay)
	{
		if (lastTransform != null)
		{
			lastTransform.delay(globalDelay);
			lastTransform.loop(loops, startDelay, resetDelay);
		}
		return this;
	}

	public AnimationRenderer linear()
	{
		if (lastTransform != null)
			lastTransform.movement(Animation.LINEAR);
		return this;
	}

	public AnimationRenderer sinusoidal()
	{
		if (lastTransform != null)
			lastTransform.movement(Animation.SINUSOIDAL);
		return this;
	}

	public void animate(Shape[] shapes)
	{
		allowReset = false;
		for (Shape s : shapes)
			animate(s);

		allowReset = true;
		reset();
	}

	public void animate(Shape s)
	{
		if (firstTransform != null && s != null)
			firstTransform.transformAll(s, elapsedTime);
		reset();
	}

	/***
	 * Transformation classes
	 * 
	 * @author -
	 * 
	 */

	private class Scale extends Animation
	{
		float toX, toY, toZ;

		public Scale(float x, float y, float z)
		{
			super(x, y, z);
		}

		protected void scaleTo(float x, float y, float z)
		{
			toX = x;
			toY = y;
			toZ = z;
		}

		@Override
		protected void transform(Shape s, float comp)
		{
			comp = Math.max(comp, 0);
			s.scale(x + (toX - x) * comp, y + (toY - y) * comp, z + (toZ - z) * comp);
		}
	}

	private class Translation extends Animation
	{
		protected Translation(float x, float y, float z)
		{
			super(x, y, z);
		}

		@Override
		protected void transform(Shape s, float comp)
		{
			if (comp >= 0)
				s.translate(x * comp, y * comp, z * comp);
		}

	}

	private class Rotation extends Animation
	{
		float angle;
		float offsetX, offsetY, offsetZ;

		protected Rotation(float angle, float x, float y, float z, float offsetX, float offsetY, float offsetZ)
		{
			super(x, y, z);
			this.angle = angle;
			this.offsetX = offsetX;
			this.offsetY = offsetY;
			this.offsetZ = offsetZ;
		}

		@Override
		protected void transform(Shape s, float comp)
		{
			if (comp >= 0)
				s.rotate(angle * comp, x, y, z, offsetX, offsetY, offsetZ);
		}
	}

	private abstract class Animation
	{
		static final int LINEAR = 0, SINUSOIDAL = 1;
		int movement = LINEAR;
		float x, y, z;
		int duration, delay = 0;
		int loops = 1, loopStartDelay = 0, loopResetDelay = 0;
		Animation prev, next;

		protected Animation(float x, float y, float z)
		{
			this.x = x;
			this.y = y;
			this.z = z;
		}

		protected void delay(int delay)
		{
			this.delay = delay;
		}

		protected void movement(int movement)
		{
			this.movement = movement;
			if (prev != null)
				prev.movement(movement);
		}

		protected void forTicks(int duration, int delay)
		{
			if (this.duration == 0)
			{
				this.duration = duration;
				this.delay = delay;
			}
		}

		protected void loop(int loops, int startDelay, int resetDelay)
		{
			this.loops = loops;
			this.loopStartDelay = startDelay;
			this.loopResetDelay = resetDelay;
		}

		protected void transformAll(Shape s, float elapsedTime)
		{
			transform(s, completion());
			if (next != null)
				next.transformAll(s, elapsedTime);
		}

		protected float completion()
		{
			if (duration == 0)
				return 0;

			float comp = 0;
			int loopDuration = duration + loopStartDelay + loopResetDelay; 
			float elapsed = (elapsedTime - delay) % loopDuration;
			if(elapsed < loopStartDelay)
				return 0;
			if(elapsed + loopResetDelay > loopDuration)
				return 1;

			duration = loops * duration;
			
			if (movement == LINEAR)
				comp = Math.min(elapsed / loopDuration, 1);
			else if (movement == SINUSOIDAL)
			{
				comp = (float) Math.sin(elapsed / loopDuration) + 0.5F;
				// System.out.println(comp);
			}

			
			return comp;
		}

		protected abstract void transform(Shape s, float comp);
	}

}
