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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.malisis.core.renderer.animation.transformation.ITransformable;
import net.malisis.core.renderer.animation.transformation.Transformation;
import net.malisis.core.util.Timer;
import net.malisis.core.util.Utils;

/**
 * @author Ordinastie
 *
 */
public class AnimationRenderer
{
	private Timer timer = new Timer();
	private boolean clearFinished = false;
	private LinkedList<Animation<?>> animations = new LinkedList<>();
	private List<ITransformable> tranformables = new ArrayList<>();
	private List<Animation<?>> toClear = new ArrayList<>();

	public AnimationRenderer()
	{
		timer.start();
	}

	public void setStartTime(long start)
	{
		timer.setStart(start);
	}

	public void setStartTime()
	{
		timer.start();
	}

	public void setStartTick(long start)
	{
		setStartTime(System.currentTimeMillis() - (getWorldTime() - start) * 1000 / 20);
		//		MalisisCore.message("%s - %s = %s > %s / %s", getWorldTime(), start, getWorldTime() - start, getElapsedTime() / 1000000000,
		//				getElapsedTicks());
	}

	public long getWorldTime()
	{
		if (Utils.getClientWorld() != null)
			return Utils.getClientWorld().getTotalWorldTime();
		else
			return 0;
	}

	public long getElapsedTime()
	{
		return timer.elapsedTime();
	}

	public float getElapsedTicks()
	{
		return timer.elapsedTick();
	}

	public void addAnimation(Animation<?> animation)
	{
		animations.add(animation);
	}

	public void deleteAnimation(Animation<?> animation)
	{
		animations.remove(animation);
	}

	public void clearAnimations()
	{
		animations.clear();
	}

	public void autoClearAnimations()
	{
		clearFinished = true;
	}

	public List<ITransformable> animate(Animation<?>... animations)
	{
		return animate(timer, animations);
	}

	public List<ITransformable> animate(Timer timer, Animation<?>... animations)
	{
		tranformables.clear();
		toClear.clear();

		if (animations == null || animations.length == 0)
			return tranformables;

		ITransformable tr = null;

		for (Animation<?> animation : animations)
		{
			tr = animation.animate(timer);
			if (tr != null)
				tranformables.add(tr);

			if (animation.isFinished() && clearFinished)
				toClear.add(animation);
		}

		return tranformables;
	}

	public List<ITransformable> animate()
	{
		List<ITransformable> anims = animate(animations.toArray(new Animation[0]));

		for (Animation<?> animation : toClear)
			animations.remove(animation);

		return anims;
	}

	public <S extends ITransformable> void animate(S transformable, Transformation<?, S> animation)
	{
		if (transformable == null)
			return;

		animation.transform(transformable, getElapsedTime());
	}

}
