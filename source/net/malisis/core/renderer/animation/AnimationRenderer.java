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

import java.util.LinkedList;

import net.malisis.core.renderer.BaseRenderer;
import net.malisis.core.renderer.animation.transformation.Transformation;
import net.malisis.core.renderer.element.Shape;
import net.minecraft.client.Minecraft;

/**
 * @author Ordinastie
 * 
 */
public class AnimationRenderer
{
	private BaseRenderer renderer;
	private long startTime;
	private long worldTotalTime;
	private float partialTick;
	private float elapsedTime;
	private LinkedList<Animation> animations = new LinkedList<>();

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

	public void addAnimation(Animation animation)
	{
		animations.add(animation);
	}

	public void clearAnimations()
	{
		animations.clear();
	}

	public void animate()
	{
		for (Animation animation : animations)
		{
			animation.animate(elapsedTime);
		}
	}

	public void render(BaseRenderer renderer)
	{
		for (Animation animation : animations)
		{
			animation.render(renderer);
		}
	}

	public void animate(Shape shape, Transformation animation)
	{
		animation.transform(shape, elapsedTime);
	}

}
