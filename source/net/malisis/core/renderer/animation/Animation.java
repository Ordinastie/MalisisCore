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

import net.malisis.core.renderer.BaseRenderer;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.animation.transformation.Transformation;
import net.malisis.core.renderer.element.Shape;

/**
 * @author Ordinastie
 * 
 */
public class Animation
{

	private Shape shape;
	private Shape transformedShape;
	private Transformation tranformation;
	private RenderParameters parameters;
	private int delay;
	private boolean started = false;
	private boolean finished = false;
	private boolean renderBefore = false;
	private boolean renderAfter = false;

	public Animation(Shape shape, Transformation transformation, RenderParameters parameters, int delay)
	{
		this.shape = shape;
		this.tranformation = transformation;
		this.parameters = parameters;
		this.delay = delay;
	}

	public Animation(Shape shape, Transformation transformation)
	{
		this(shape, transformation, null, 0);
	}

	public void setRender(boolean before, boolean after)
	{
		renderBefore = before;
		renderAfter = after;
	}

	public void setDelay(int delay)
	{
		this.delay = delay;
	}

	public boolean isStarted()
	{
		return started;
	}

	public boolean isFinished()
	{
		return finished;
	}

	public Shape animate(float elapsedTime)
	{
		float elapsed = elapsedTime - delay;
		started = elapsed > tranformation.getDelay();
		finished = elapsed > tranformation.totalDuration() && tranformation.getLoops() != -1;

		transformedShape = new Shape(shape);
		tranformation.transform(transformedShape, elapsed);
		return transformedShape;
	}

	public void render(BaseRenderer renderer)
	{
		if (!started && !renderBefore)
			return;
		if (finished && !renderAfter)
			return;

		renderer.drawShape(transformedShape, parameters);
	}

}
