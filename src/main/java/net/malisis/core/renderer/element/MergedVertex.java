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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.malisis.core.renderer.animation.transformation.ITransformable;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

/**
 * MergedVertex are holders of vertex that share the same position inside a shape. The position is determined by the vertexes base name when
 * the MergedVertex are enable for a shape.<br>
 * When enabling MergedVertex for a Shape, all the transformations are passed to the MergedVertex and further transformations for the Shape
 * will also be passed to the MergedVertex.
 *
 * @author Ordinastie
 *
 */
public class MergedVertex implements ITransformable.Translate, ITransformable.Rotate, ITransformable.Scale, ITransformable.Alpha,
		ITransformable.Color, Iterable<Vertex>
{
	protected String name;
	protected Vertex base;
	protected Matrix4f transformMatrix;
	private Set<Vertex> vertexes = new HashSet<>();

	public MergedVertex(Vertex vertex)
	{
		this.name = vertex.baseName();
		this.base = vertex;
		addVertex(vertex);
	}

	/**
	 * @return the name of this {@link MergedVertex}.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @return the X coordinate of this {@link MergedVertex}.
	 */
	public double getX()
	{
		return base.getX();
	}

	/**
	 * @return the Y coordinate of this {@link MergedVertex}.
	 */
	public double getY()
	{
		return base.getY();
	}

	/**
	 * @return the Z coordinate of this {@link MergedVertex}.
	 */
	public double getZ()
	{
		return base.getZ();
	}

	/**
	 * Checks whether this {@link MergedVertex} matches one of the <b>names</b> specified.
	 *
	 * @param names
	 * @return
	 */
	public boolean is(String... names)
	{
		boolean b = true;
		for (String n : names)
		{
			b &= name.toLowerCase().contains(n.toLowerCase());
		}
		return b;
	}

	/**
	 * Adds a {@link Vertex} to this {@link MergedVertex}. The <code>Vertex</code> base name must match this <code>MergedVertex</code> name.
	 *
	 * @param vertex
	 */
	public void addVertex(Vertex vertex)
	{
		if (!name.equals(vertex.baseName()))
			return;

		vertexes.add(vertex);
	}

	/**
	 * Removes a {@link Vertex} from this {@link MergedVertex}.
	 *
	 * @param vertex
	 */
	public void removeVertex(Vertex vertex)
	{
		vertexes.remove(vertex);
	}

	/**
	 * Gets the transform matrix of this {@link MergedVertex}. Creates it if it doesn't exist already.<br>
	 * The matrix is translated by 0.5F, 0.5F, 0.5F upon creation.
	 *
	 * @return
	 */
	private Matrix4f matrix()
	{
		if (transformMatrix == null)
		{
			transformMatrix = new Matrix4f();
			transformMatrix.translate(new Vector3f(0.5F, 0.5F, 0.5F));
		}
		return transformMatrix;
	}

	/**
	 * Copies the transformation from a {@link Shape shape} to this <code>Shape</code>.
	 *
	 * @param shape
	 * @return
	 */
	public void copyMatrix(Matrix4f matrix)
	{
		transformMatrix = new Matrix4f(matrix);
	}

	/**
	 * Applies the transformations matrices to this {@link MergedVertex}. This modifies the position of the vertexes.
	 *
	 * @return
	 */
	public void applyMatrix()
	{
		if (transformMatrix == null)
			return;

		transformMatrix.translate(new Vector3f(-0.5F, -0.5F, -0.5F));

		for (Vertex v : vertexes)
			v.applyMatrix(transformMatrix);

		transformMatrix = null;
		return;
	}

	/**
	 * Translates this {@link MergedVertex}.
	 *
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	@Override
	public void translate(float x, float y, float z)
	{
		matrix().translate(new Vector3f(x, y, z));
	}

	/**
	 * Rotates this {@link MergedVertex} around the given axis the specified angle. Offsets the origin for the rotation.
	 *
	 * @param angle
	 * @param x
	 * @param y
	 * @param z
	 * @param offsetX
	 * @param offsetY
	 * @param offsetZ
	 * @return
	 */
	@Override
	public void rotate(float angle, float x, float y, float z, float offsetX, float offsetY, float offsetZ)
	{
		translate(offsetX, offsetY, offsetZ);
		matrix().rotate((float) Math.toRadians(angle), new Vector3f(x, y, z));
		translate(-offsetX, -offsetY, -offsetZ);
	}

	/**
	 * Scales this {@link MergedVertex}.
	 *
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	@Override
	public void scale(float x, float y, float z)
	{
		matrix().scale(new Vector3f(x, y, z));
	}

	/**
	 * @return the color of this {@link MergedVertex}.
	 */
	@Override
	public int getColor()
	{
		return base.getColor();
	}

	/**
	 * Sets the color for this {@link MergedVertex}.
	 *
	 * @param color
	 */
	@Override
	public void setColor(int color)
	{
		for (Vertex v : this)
			v.setColor(color);
	}

	/**
	 * @return the alpha value for this {@link MergedVertex}.
	 */
	@Override
	public int getAlpha()
	{
		return 0;
	}

	/**
	 * Sets the alpha value for this {@link MergedVertex}.
	 *
	 * @param alpha
	 */
	@Override
	public void setAlpha(int alpha)
	{
		for (Vertex v : this)
			v.setAlpha(alpha);
	}

	/**
	 * Gets the {@link Iterator} for this {@link MergedVertex}.
	 */
	@Override
	public Iterator<Vertex> iterator()
	{
		return vertexes.iterator();
	}

	/**
	 * Gets the list of {@link MergedVertex} for a {@link Shape}.
	 *
	 * @param shape
	 * @return a {@link HashMap} where the key is the {@link Vertex#baseName()} of the vertexes merged.
	 */
	public static Map<String, MergedVertex> getMergedVertexes(Shape shape)
	{
		Map<String, MergedVertex> mergedVertexes = new HashMap<>();

		for (Face f : shape.getFaces())
		{
			for (Vertex v : f.getVertexes())
			{
				MergedVertex mv = mergedVertexes.get(v.baseName());
				if (mv == null)
				{
					mv = new MergedVertex(v);
					mergedVertexes.put(v.baseName(), mv);
				}
				else
					mv.addVertex(v);
			}

		}

		return mergedVertexes;

	}
}
