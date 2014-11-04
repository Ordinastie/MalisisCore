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
		ITransformable.Color, ITransformable.Brightness, Iterable<Vertex>
{
	/** Name of this {@link MergedVertex}. */
	protected String name;

	/** Base {@link Vertex}. */
	protected Vertex base;

	/** Matrix holding the tranformations applied to this {@link MergedVertex}. */
	protected Matrix4f transformMatrix;

	/** List of {@link Vertex vertexes} that share the same position. */
	private Set<Vertex> vertexes = new HashSet<>();

	/**
	 * Instantiates a new {@link MergedVertex}.
	 *
	 * @param vertex the vertex
	 */
	public MergedVertex(Vertex vertex)
	{
		this.name = vertex.baseName();
		this.base = vertex;
		addVertex(vertex);
	}

	/**
	 * Gets the name of this {@link MergedVertex}.
	 *
	 * @return the name of this {@link MergedVertex}.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Gets the X coordinate of this {@link MergedVertex}.
	 *
	 * @return the X coordinate of this {@link MergedVertex}.
	 */
	public double getX()
	{
		return base.getX();
	}

	/**
	 * Gets the Y coordinate of this {@link MergedVertex}
	 *
	 * @return the Y coordinate of this {@link MergedVertex}.
	 */
	public double getY()
	{
		return base.getY();
	}

	/**
	 * Gets the Z coordinate of this {@link MergedVertex}
	 *
	 * @return the Z coordinate of this {@link MergedVertex}.
	 */
	public double getZ()
	{
		return base.getZ();
	}

	/**
	 * Checks whether this {@link MergedVertex} matches one of the <b>names</b> specified.
	 *
	 * @param names the names
	 * @return true, if successful
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
	 * @param vertex the vertex
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
	 * @param vertex the vertex
	 */
	public void removeVertex(Vertex vertex)
	{
		vertexes.remove(vertex);
	}

	/**
	 * Gets the transform matrix of this {@link MergedVertex}. Creates it if it doesn't exist already.<br>
	 * The matrix is translated by 0.5F, 0.5F, 0.5F upon creation.
	 *
	 * @return the matrix4f
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
	 * @param matrix the matrix
	 */
	public void copyMatrix(Matrix4f matrix)
	{
		transformMatrix = new Matrix4f(matrix);
	}

	/**
	 * Applies the transformations matrices to this {@link MergedVertex}. This modifies the position of the vertexes.
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
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 */
	@Override
	public void translate(float x, float y, float z)
	{
		matrix().translate(new Vector3f(x, y, z));
	}

	/**
	 * Rotates this {@link MergedVertex} around the given axis the specified angle. Offsets the origin for the rotation.
	 *
	 * @param angle the angle
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @param offsetX the offset x
	 * @param offsetY the offset y
	 * @param offsetZ the offset z
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
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @param offsetX the offset x
	 * @param offsetY the offset y
	 * @param offsetZ the offset z
	 */
	@Override
	public void scale(float x, float y, float z, float offsetX, float offsetY, float offsetZ)
	{
		translate(offsetX, offsetY, offsetZ);
		matrix().scale(new Vector3f(x, y, z));
		translate(-offsetX, -offsetY, -offsetZ);
	}

	/**
	 * Sets the color for this {@link MergedVertex}.
	 *
	 * @param color the new color
	 */
	@Override
	public void setColor(int color)
	{
		for (Vertex v : this)
			v.setColor(color);
	}

	/**
	 * Sets the alpha value for this {@link MergedVertex}.
	 *
	 * @param alpha the new alpha
	 */
	@Override
	public void setAlpha(int alpha)
	{
		for (Vertex v : this)
			v.setAlpha(alpha);
	}

	/**
	 * Sets the brightness for this {@link MergedVertex}.
	 *
	 * @param brightness the new brightness
	 */
	@Override
	public void setBrightness(int brightness)
	{
		for (Vertex v : this)
			v.setBrightness(brightness);
	}

	/**
	 * Gets the {@link Iterator} for this {@link MergedVertex}.
	 *
	 * @return the iterator
	 */
	@Override
	public Iterator<Vertex> iterator()
	{
		return vertexes.iterator();
	}

	/**
	 * Gets the list of {@link MergedVertex} for a {@link Shape}.
	 *
	 * @param shape the shape
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
