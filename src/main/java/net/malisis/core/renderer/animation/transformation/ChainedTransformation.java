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

import java.util.ArrayList;

/**
 * @author -
 *
 */
public class ChainedTransformation<S extends ITransformable> extends Transformation<ChainedTransformation<S>, S>
{
	/** List of transformations. */
	protected ArrayList<Transformation<?, S>> listTransformations = new ArrayList<>();

	/**
	 * Instantiates a new {@link ChainedTransformation}.
	 *
	 * @param transformations the transformations
	 */
	public ChainedTransformation(@SuppressWarnings("unchecked") Transformation<?, S>... transformations)
	{
		addTransformations(transformations);
	}

	/**
	 * Gets this {@link ChainedTransformation}.
	 *
	 * @return the chained transformation
	 */
	@Override
	public ChainedTransformation<S> self()
	{
		return null;
	}

	/**
	 * Adds the {@link Transformation} this to {@link ParallelTransformation}
	 *
	 * @param transformations the transformations
	 * @return the chained transformation
	 */
	public ChainedTransformation<S> addTransformations(@SuppressWarnings("unchecked") Transformation<?, S>... transformations)
	{
		duration = 0;
		for (Transformation<?, S> transformation : transformations)
		{
			duration += transformation.totalDuration();
			listTransformations.add(transformation);
		}

		return this;
	}

	/**
	 * Calculates the transformation.
	 *
	 * @param transformable the transformable
	 * @param comp the comp
	 */
	@Override
	protected void doTransform(S transformable, float comp)
	{
		if (listTransformations.size() == 0)
			return;

		if (reversed)
			elapsedTimeCurrentLoop = Math.max(0, duration - elapsedTimeCurrentLoop);
		for (Transformation<?, S> transformation : listTransformations)
		{
			transformation.transform(transformable, elapsedTimeCurrentLoop);
			elapsedTimeCurrentLoop -= transformation.totalDuration();
		}
	}
}
