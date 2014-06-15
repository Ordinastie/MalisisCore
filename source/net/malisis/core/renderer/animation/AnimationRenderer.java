package net.malisis.core.renderer.animation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import net.malisis.core.renderer.BaseRenderer;
import net.malisis.core.renderer.RenderParameters;
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

	private Animation firstAnimation;
	private Animation lastAnimation;

	private HashMap<String, AnimElement> listAnimations = new HashMap<>();
	private ArrayList<String> excludeRenderList = new ArrayList<>();

	public AnimationRenderer(BaseRenderer renderer)
	{
		this.renderer = renderer;
	}

	public void setStartTime(long start)
	{
		this.startTime = start;
		this.worldTotalTime = Minecraft.getMinecraft().theWorld.getTotalWorldTime();
		this.partialTick = this.renderer.partialTick;
		this.elapsedTime = worldTotalTime - startTime + partialTick;
	}

	public long getStartTime()
	{
		return startTime;
	}

	public float getElapsedTime()
	{
		return elapsedTime;
	}

	public void add(Animation animation)
	{
		if (firstAnimation == null)
		{
			firstAnimation = animation;
			lastAnimation = animation;
		}
		else
		{
			lastAnimation.next = animation;
			animation.prev = lastAnimation;
			lastAnimation = animation;
		}
	}

	public void clearAnimation(String name)
	{
		listAnimations.remove(name);
	}

	public void clearAnimations()
	{
		listAnimations.clear();
		globalDelay = 0;
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
		add(new Translation(x, y, z));
		return this;
	}

	public AnimationRenderer rotate(float angle, float x, float y, float z, float offsetX, float offsetY, float offsetZ)
	{
		add(new Rotation(angle, x, y, z, offsetX, offsetY, offsetZ));
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
		add(new Scale(x, y, z));
		return this;
	}

	public AnimationRenderer scaleTo(float f)
	{
		return scaleTo(f, f, f);
	}

	public AnimationRenderer scaleTo(float x, float y, float z)
	{
		if (lastAnimation instanceof Scale)
			((Scale) lastAnimation).scaleTo(x, y, z);
		return this;
	}

	public AnimationRenderer globalDelay(int delay)
	{
		globalDelay = delay;
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
		if (lastAnimation != null)
			lastAnimation.forTicks(duration, delay);
		return this;
	}

	public AnimationRenderer loop(int loops)
	{
		return loop(loops, 0, 0);
	}

	public AnimationRenderer loop(int loops, int startDelay, int resetDelay)
	{
		if (lastAnimation != null)
			lastAnimation.loop(loops, startDelay, resetDelay);
		return this;
	}

	public AnimationRenderer linear()
	{
		if (lastAnimation != null)
			lastAnimation.movement(Animation.LINEAR);
		return this;
	}

	public AnimationRenderer sinusoidal()
	{
		if (lastAnimation != null)
			lastAnimation.movement(Animation.SINUSOIDAL);
		return this;
	}

	public AnimationRenderer animate(String name, Shape s)
	{
		return animate(name, new Shape[] { s }, null);
	}

	public AnimationRenderer animate(String name, Shape s, RenderParameters rp)
	{
		return animate(name, new Shape[] { s }, rp);
	}

	public AnimationRenderer animate(String name, Shape[] shapes)
	{
		return animate(name, shapes, null);
	}

	public AnimationRenderer animate(String name, Shape[] shapes, RenderParameters rp)
	{
		listAnimations.put(name, new AnimElement(firstAnimation, shapes, rp, globalDelay));
		firstAnimation = lastAnimation = null;
		return this;
	}

	public void renderAllBut(String... names)
	{
		excludeRenderList = new ArrayList(Arrays.asList(names));
		render();
		excludeRenderList.clear();
	}

	public void render(String... names)
	{
		if (names.length == 0)
			names = listAnimations.keySet().toArray(new String[0]);

		for (String name : names)
		{
			if (!excludeRenderList.contains(name))
			{
				AnimElement el = listAnimations.get(name);
				if (el != null)
				{
					for (Shape s : el.getTransformedShapes(elapsedTime))
					{
						renderer.drawShape(s, el.renderParameters);
					}
				}
			}
		}

	}

	private class AnimElement
	{
		public Animation animation;
		public Shape[] shapes;
		public RenderParameters renderParameters;
		public int delay;

		public AnimElement(Animation anim, Shape[] s, RenderParameters param, int d)
		{
			animation = anim;
			shapes = s;
			renderParameters = param;
			delay = d;
		}

		public Shape[] getTransformedShapes(float elapsed)
		{
			if (animation == null || elapsed < delay)
				return new Shape[0];

			Shape[] transformedShapes = new Shape[shapes.length];
			int i = 0;
			for (Shape s : shapes)
			{
				if (s != null)
				{
					Shape ts = new Shape(s);
					if (animation != null)
						animation.transformAll(ts, elapsed - delay);
					transformedShapes[i++] = ts;
				}
			}
			return transformedShapes;
		}

	}
}
