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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.animation.transformation.ITransformable;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;

import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class Shape implements ITransformable.Translate, ITransformable.Rotate, ITransformable.Scale
{
	protected Face[] faces;
	protected Matrix4f transformMatrix;
	protected Map<String, MergedVertex> mergedVertexes;

	public Shape()
	{
		this.faces = new Face[0];
	}

	public Shape(Face... faces)
	{
		this.faces = faces;
	}

	public Shape(List<Face> faces)
	{
		this(faces.toArray(new Face[0]));
	}

	public Shape(Shape s)
	{
		this(s.faces);
		copyMatrix(s);
	}

	//#region FACES
	/**
	 * Adds {@link Face faces} to this {@link Shape}.
	 *
	 * @param faces
	 * @return
	 */
	public Shape addFaces(Face[] faces)
	{
		return addFaces(faces, null);
	}

	/**
	 * Adds {@link Face faces} to this {@link Shape} with the specified <b>groupName</b>.
	 *
	 * @param faces
	 * @param groupName
	 * @return
	 */
	public Shape addFaces(Face[] faces, String groupName)
	{
		if (groupName != null)
		{
			for (Face f : faces)
				f.setBaseName(groupName);
		}

		this.faces = ArrayUtils.addAll(this.faces, faces);

		return this;
	}

	/**
	 * Gets the {@link Face faces} that make up this {@link Shape}.
	 *
	 * @return
	 */
	public Face[] getFaces()
	{
		return faces;
	}

	/**
	 * Gets the {@link Face faces} that make up this {@link Shape} which match the specified <b>name</b>.
	 *
	 * @param name
	 * @return
	 */
	public List<Face> getFaces(String name)
	{
		List<Face> list = new ArrayList<>();
		for (Face f : faces)
			if (f.baseName().toLowerCase().equals(name.toLowerCase()))
				list.add(f);
		return list;
	}

	/**
	 * Gets a face from its name
	 *
	 * @param name
	 * @return
	 */
	public Face getFace(String name)
	{
		List<Face> list = getFaces(name);
		return list.size() > 0 ? list.get(0) : null;
	}

	/**
	 * Removes a {@link Face} from this {@link Shape}. Has no effect if the <code>Face</code> doesn't belong to this <code>Shape</code>.
	 *
	 * @param face
	 * @return
	 */
	public Shape removeFace(Face face)
	{
		faces = ArrayUtils.removeElement(faces, face);
		return this;
	}

	//#end FACES

	//#region VERTEXES
	/**
	 * Enables the {@link MergedVertex} for this {@link Shape}. Will transfer the current transformation matrix to the {@link MergedVertex}
	 */
	public void enableMergedVertexes()
	{
		if (mergedVertexes != null)
			return;

		this.mergedVertexes = MergedVertex.getMergedVertexes(this);
		//transfer current transforms into the mergedVertexes if any
		if (transformMatrix != null)
		{
			for (MergedVertex mv : mergedVertexes.values())
				mv.copyMatrix(transformMatrix);
		}
	}

	/**
	 * Gets a list of {@link Vertex} matching <b>name</b>.
	 *
	 * @param name
	 * @return
	 */
	public List<Vertex> getVertexes(String name)
	{
		List<Vertex> vertexes = new ArrayList<>();
		for (Face f : faces)
		{
			for (Vertex v : f.getVertexes())
			{
				if (v.baseName().toLowerCase().contains(name.toLowerCase()))
					vertexes.add(v);
			}
		}
		return vertexes;
	}

	public List<Vertex> getVertexes(Face face)
	{
		List<Vertex> vertexes = new ArrayList<>();
		if (face == null)
			return vertexes;

		Set<String> names = new HashSet<>();
		for (Vertex v : face.getVertexes())
			names.add(v.baseName().toLowerCase());

		for (Face f : faces)
		{
			for (Vertex v : f.getVertexes())
			{
				if (names.contains(v.baseName().toLowerCase()))
					vertexes.add(v);
			}
		}
		return vertexes;
	}

	public List<Vertex> getVertexes(ForgeDirection direction)
	{
		return getVertexes(getFace(Face.name(direction)));
	}

	public MergedVertex getMergedVertex(Vertex vertex)
	{
		if (mergedVertexes == null)
			return null;
		return mergedVertexes.get(vertex.baseName());
	}

	public List<MergedVertex> getMergedVertexes(Face face)
	{
		List<MergedVertex> vertexes = new ArrayList<>();
		if (mergedVertexes == null)
			return vertexes;

		for (Vertex v : face.getVertexes())
		{
			MergedVertex mv = getMergedVertex(v);
			if (mv != null)
				vertexes.add(mv);
		}

		return vertexes;
	}

	public List<MergedVertex> getMergedVertexs(Face face)
	{
		List<MergedVertex> vertexes = new ArrayList<>();
		for (Vertex v : face.getVertexes())
		{
			MergedVertex mv = getMergedVertex(v);
			if (mv != null)
				vertexes.add(mv);
		}

		return vertexes;
	}

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
	public Shape copyMatrix(Shape shape)
	{
		if (shape.transformMatrix != null)
			this.transformMatrix = new Matrix4f(shape.transformMatrix);
		return this;
	}

	/**
	 * Applies the transformations matrices to this {@link Shape}. This modifies to position of the vertexes making up the faces of this
	 * <code>Shape</code>.
	 *
	 * @return
	 */
	public Shape applyMatrix()
	{
		if (mergedVertexes != null)
		{
			for (MergedVertex mv : mergedVertexes.values())
				mv.applyMatrix();

			return this;
		}

		if (transformMatrix == null)
			return this;

		//transform back to original place
		transformMatrix.translate(new Vector3f(-0.5F, -0.5F, -0.5F));

		for (Face f : faces)
		{
			for (Vertex v : f.getVertexes())
				v.applyMatrix(transformMatrix);
		}

		transformMatrix = null;
		return this;
	}

	/**
	 * Set {@link RenderParameters} for {@link Face faces} matching the specified <b>name</b>. If <b>merge</b> is true, the parameters will
	 * be merge with the <code>face</code> parameters instead of completely overriding them.
	 *
	 * @param face
	 * @param params
	 * @param merge
	 * @return
	 */
	public Shape setParameters(String name, RenderParameters params, boolean merge)
	{
		List<Face> faces = getFaces(name);
		for (Face f : faces)
		{
			if (merge)
				f.getParameters().merge(params);
			else
				f.setParameters(params);
		}
		return this;
	}

	/**
	 * Sets the size of this {@link Shape}. <b>width</b> represents East-West axis, <b>height</b> represents Bottom-Top axis and
	 * <b>Depth</b> represents North-South axis. The calculations are based on {@link Vertex#baseName()}.
	 *
	 * @param width
	 * @param height
	 * @param depth
	 * @return
	 */
	public Shape setSize(float width, float height, float depth)
	{
		float x = 0, y = 0, z = 0;
		for (Face f : faces)
		{
			for (Vertex v : f.getVertexes())
			{
				String name = v.baseName();
				if (name.contains("West"))
					x = (float) v.getX();
				if (name.contains("Bottom"))
					y = (float) v.getY();
				if (name.contains("North"))
					z = (float) v.getZ();
			}
		}

		return setBounds(x, y, z, x + width, y + height, z + depth);
	}

	/**
	 * Sets the bounds for this {@link Shape}. Calculations are based on {@link Vertex#baseName()}.
	 *
	 * @param x
	 * @param y
	 * @param z
	 * @param X
	 * @param Y
	 * @param Z
	 * @return
	 */
	public Shape setBounds(float x, float y, float z, float X, float Y, float Z)
	{
		for (Face f : faces)
		{
			for (Vertex v : f.getVertexes())
			{
				String name = v.name();
				if (name.contains("West"))
					v.setX(x);
				if (name.contains("East"))
					v.setX(X);
				if (name.contains("Bottom"))
					v.setY(y);
				if (name.contains("Top"))
					v.setY(Y);
				if (name.contains("North"))
					v.setZ(z);
				if (name.contains("South"))
					v.setZ(Z);
			}
		}
		return this;
	}

	/**
	 * Limits this {@link Shape} to the bounding box passed.
	 *
	 * @param aabb
	 * @return
	 */
	public Shape limit(AxisAlignedBB aabb)
	{
		return limit(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
	}

	/**
	 * Limits this {@link Shape} to the bounding box passed.
	 *
	 * @param x
	 * @param y
	 * @param z
	 * @param X
	 * @param Y
	 * @param Z
	 * @return
	 */
	public Shape limit(double x, double y, double z, double X, double Y, double Z)
	{
		for (Face f : faces)
		{
			for (Vertex v : f.getVertexes())
			{
				v.setX(Vertex.clamp(v.getX(), x, X));
				v.setY(Vertex.clamp(v.getY(), y, Y));
				v.setZ(Vertex.clamp(v.getZ(), z, Z));
			}
		}
		return this;
	}

	/**
	 * Translates this {@link Shape}.
	 *
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	@Override
	public void translate(float x, float y, float z)
	{
		if (mergedVertexes != null)
		{
			for (MergedVertex mv : mergedVertexes.values())
				mv.translate(x, y, z);
		}
		else
			matrix().translate(new Vector3f(x, y, z));
	}

	/**
	 * Scales this {@link Shape} on all axis.
	 *
	 * @param f
	 * @return
	 */
	public void scale(float f)
	{
		scale(f, f, f);
	}

	/**
	 * Scales this {@link Shape}.
	 *
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	@Override
	public void scale(float x, float y, float z)
	{
		if (mergedVertexes != null)
		{
			for (MergedVertex mv : mergedVertexes.values())
				mv.scale(x, y, z);
		}
		else
			matrix().scale(new Vector3f(x, y, z));
	}

	/**
	 * Rotates this {@link Shape} around the given axis the specified angle.
	 *
	 * @param angle
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public void rotate(float angle, float x, float y, float z)
	{
		rotate(angle, x, y, z, 0, 0, 0);
	}

	/**
	 * Rotates this {@link Shape} around the given axis the specified angle. Offsets the origin for the rotation.
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
		if (mergedVertexes != null)
		{
			for (MergedVertex mv : mergedVertexes.values())
				mv.rotate(angle, x, y, z, offsetX, offsetY, offsetZ);
		}
		else
		{
			translate(offsetX, offsetY, offsetZ);
			matrix().rotate((float) Math.toRadians(angle), new Vector3f(x, y, z));
			translate(-offsetX, -offsetY, -offsetZ);
		}
	}

	/**
	 * Stores the current state of each vertex making up this {@link Shape}.
	 *
	 * @return
	 */
	public Shape storeState()
	{
		applyMatrix();
		for (Face f : faces)
		{
			for (Vertex v : f.getVertexes())
				v.setInitialState();
		}
		return this;
	}

	/**
	 * Resets the state of each vertex making up this {@link Shape} to a previously stored one.
	 *
	 * @return
	 */
	public Shape resetState()
	{
		transformMatrix = null;
		for (Face f : faces)
		{
			for (Vertex v : f.getVertexes())
				v.resetState();
		}
		return this;
	}

	/**
	 * Interpolates the UVs of each vertex making up this {@link Shape} base on their position and the {@link Face} orientation.
	 *
	 * @return
	 */
	public Shape interpolateUV()
	{
		for (Face f : faces)
			f.interpolateUV();

		return this;
	}

	/**
	 * Shrinks the face matching <b>face</b> name by a certain <b>factor</b>. The vertexes of connected faces are moved too.
	 *
	 * @param face
	 * @param factor
	 * @return
	 */
	public Shape shrink(ForgeDirection dir, float factor)
	{
		Face face = getFace(Face.name(dir));
		if (face == null || mergedVertexes == null)
			return this;

		HashMap<String, Vertex> vertexNames = new HashMap<String, Vertex>();
		double x = 0, y = 0, z = 0;
		for (Vertex v : face.getVertexes())
		{
			vertexNames.put(v.name(), v);
			x += v.getX() / 4;
			y += v.getY() / 4;
			z += v.getZ() / 4;
		}
		face.scale(factor, x, y, z);

		for (Vertex v : face.getVertexes())
		{
			for (Vertex sv : mergedVertexes.get(v.baseName()))
			{
				if (sv != v)
					sv.set(v.getX(), v.getY(), v.getZ());
			}
		}

		return this;
	}

	/**
	 * Builds a Shape from multiple ones.
	 *
	 * @param shapes
	 * @return
	 */
	public static Shape fromShapes(Shape... shapes)
	{
		Face[] faces = new Face[0];
		for (Shape s : shapes)
		{
			s.applyMatrix();
			faces = ArrayUtils.addAll(faces, s.getFaces());
		}

		return new Shape(faces);
	}
}
