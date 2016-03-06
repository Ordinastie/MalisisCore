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

package net.malisis.core.renderer.element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A class that generates {@link Vertex vertexes} for a Bezier curve based on provided control points
 *
 * @author Ordinastie
 */
public class Bezier
{
	/** Generated vertexes. */
	private List<Vertex> vertexes = new ArrayList<>();
	/** Control points. */
	private List<Vertex> controlPoints = new ArrayList<>();
	/** Precision (amount of vertexes for the path). */
	private int precision;
	/** True if control points or precision have changed. Vertexes will be recalculated. */
	private boolean dirty = false;

	/**
	 * Instantiates a new {@link Bezier}
	 *
	 * @param controlPoints the control points
	 * @param precision the precision
	 */
	public Bezier(List<Vertex> controlPoints, int precision)
	{
		setControlPoints(controlPoints);
		setPrecision(precision);
	}

	/**
	 * Instantiates a new {@link Bezier}
	 *
	 * @param controlPoints the control points
	 * @param precision the precision
	 */
	public Bezier(Vertex[] controlPoints, int precision)
	{
		this(Arrays.asList(controlPoints), precision);
	}

	//#region Getters/Setters
	/**
	 * Sets the control points for this {@link Bezier}.
	 *
	 * @param controlPoints vertexes for the control points
	 */
	public void setControlPoints(List<Vertex> controlPoints)
	{
		this.controlPoints = controlPoints != null ? controlPoints : new ArrayList<>();
		dirty = true;
	}

	/**
	 * Adds a control point.
	 *
	 * @param vertex the control points
	 */
	public void addControlPoint(Vertex vertex)
	{
		controlPoints.add(vertex);
		dirty = true;
	}

	/**
	 * Removes the specified control point.
	 *
	 * @param vertex the control point
	 */
	public void removeControlPoint(Vertex vertex)
	{
		controlPoints.remove(vertex);
		dirty = true;
	}

	/**
	 * Gets the control points of this {@link Bezier}.
	 *
	 * @return the control points
	 */
	public List<Vertex> getControlPoints()
	{
		return controlPoints;
	}

	/**
	 * Sets the amount of {@link Vertex vertexes} for this {@link Bezier}.
	 *
	 * @param precision the new precision
	 */
	public void setPrecision(int precision)
	{
		this.precision = precision;
		dirty = true;
	}

	/**
	 * Gets the amount of {@link Vertex vertexes} for this {@link Bezier}.
	 *
	 * @return the precision
	 */
	public int getPrecision()
	{
		return precision;
	}

	//#end Getters/Setters

	/**
	 * Notify this {@link Bezier} that the {@link Vertex vertexes} will need to be recalculated.
	 */
	public void markDirty()
	{
		dirty = true;
	}

	/**
	 * Builds the vertexes.
	 */
	private void buildVertexes()
	{
		vertexes.clear();
		if (controlPoints.size() == 0)
			return;

		double step = 1D / (precision + 1);
		double t = 0;
		vertexes.add(controlPoints.get(0));
		for (int i = 1; i <= precision; i++)
		{
			t = i * step;
			vertexes.add(i, interpolateAll(controlPoints, controlPoints.size() - 1, 0, t));
		}
		vertexes.add(controlPoints.get(controlPoints.size() - 1));

		dirty = false;
	}

	/**
	 * Interpolate the vertexes places.
	 *
	 * @param vertexes the vertexes
	 * @param r the degree
	 * @param index the index of the vertex
	 * @param t the completion among the path
	 * @return the vertex
	 */
	public Vertex interpolateAll(List<Vertex> vertexes, int r, int index, double t)
	{
		if (vertexes.size() == 1)
			return vertexes.get(0);
		if (r == 0)
			return vertexes.get(index);

		Vertex v1 = interpolateAll(vertexes, r - 1, index, t);
		Vertex v2 = interpolateAll(vertexes, r - 1, index + 1, t);

		return interpolate(v1, v2, t);
	}

	/**
	 * Interpolate the place of a {@link Vertex} between two.
	 *
	 * @param start the start
	 * @param end the end
	 * @param t the t
	 * @return the vertex
	 */
	private Vertex interpolate(Vertex start, Vertex end, double t)
	{
		double x = interpolate(start.getX(), end.getX(), t);
		double y = interpolate(start.getY(), end.getY(), t);
		double z = interpolate(start.getZ(), end.getZ(), t);

		return new Vertex(x, y, z);
	}

	/**
	 * Interpolate a value between two.
	 *
	 * @param start the start
	 * @param end the end
	 * @param t the t
	 * @return the double
	 */
	private double interpolate(double start, double end, double t)
	{
		return (1 - t) * start + t * end;
	}

	/**
	 * Gets the vertexes of this {@link Bezier}.<br>
	 *
	 * @return the vertexes
	 */
	public List<Vertex> getVertexes()
	{
		if (dirty)
			buildVertexes();

		return vertexes;
	}
}
