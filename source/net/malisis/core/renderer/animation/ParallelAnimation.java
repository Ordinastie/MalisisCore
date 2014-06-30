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

import net.malisis.core.renderer.element.Shape;

/**
 * @author Ordinastie
 * 
 */
public class ParallelAnimation extends Animation<ParallelAnimation>
{
	protected LinkedList<Animation> listAnimations = new LinkedList<>();

	public ParallelAnimation(Animation... animations)
	{
		addAnimations(animations);

	}

	public ParallelAnimation addAnimations(Animation... animations)
	{
		for (Animation animation : animations)
		{
			duration = Math.max(duration, animation.duration + animation.delay);
			listAnimations.add(animation);
		}

		return this;
	}

	@Override
	protected void animate(Shape s, float comp)
	{
		if (listAnimations.size() == 0)
			return;

		for (Animation animation : listAnimations)
			animation.transform(s, elapsedTimeCurrentLoop);
	}

	@Override
	public ParallelAnimation reversed(boolean reversed)
	{
		if (!reversed)
			return this;

		//Collections.reverse(listAnimations);
		for (Animation animation : listAnimations)
			animation.reversed(true);

		return this;
	}
}
