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
import java.util.List;

import net.malisis.core.renderer.RenderParameters;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;

import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class Shape
{
	protected Face[] faces;
	protected Matrix4f selfRotationMatrix;
	protected Matrix4f transformMatrix;

	public Shape(Face[] faces)
	{
		// we need a copy of the faces else the modification for one shape would
		// impact the others ones
		this.faces = new Face[faces.length];
		for (int i = 0; i < faces.length; i++)
			this.faces[i] = new Face(faces[i]);
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

	/**
	 * Gets the faces that make up this <code>Shape</code>
	 * 
	 * @return
	 */
	public Face[] getFaces()
	{
		return faces;
	}

	/**
	 * Gets a face from its name
	 * 
	 * @param name
	 * @return
	 */
	public Face getFace(String name)
	{
		for (Face f : faces)
			if (f.baseName().toLowerCase().equals(name.toLowerCase()))
				return f;
		return null;
	}

	/**
	 * Gets a face from a ForgeDirection
	 * 
	 * @param dir
	 * @return
	 */
	public Face getFace(ForgeDirection dir)
	{
		return getFace(nameFromDirection(dir));
	}

	/**
	 * Gets a list of vertexes matching name
	 * 
	 * @param name
	 * @return
	 */
	public List<Vertex> getVertexes(String name)
	{
		List<Vertex> vertexes = new ArrayList<>();
		for (Face f : faces)
			for (Vertex v : f.getVertexes())
				if (v.name().toLowerCase().contains(name.toLowerCase()))
					vertexes.add(v);
		return vertexes;
	}

	/**
	 * Gets a list of vertexes from a ForgeDirection
	 * 
	 * @param dir
	 * @return
	 */
	public List<Vertex> getVertexes(ForgeDirection dir)
	{
		return getVertexes(nameFromDirection(dir));
	}

	private String nameFromDirection(ForgeDirection dir)
	{
		if (dir == ForgeDirection.UP)
			return "top";
		else if (dir == ForgeDirection.DOWN)
			return "bottom";
		else
			return dir.toString();
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

	private Matrix4f rotationMatrix()
	{
		if (selfRotationMatrix == null)
		{
			selfRotationMatrix = new Matrix4f();
			selfRotationMatrix.translate(new Vector3f(0.5F, 0.5F, 0.5F));
		}
		return selfRotationMatrix;
	}

	/**
	 * Set parameters for a face. The face is determined by <b>face</b>.<i>name()</i> in order to avoid having to keep a reference to the
	 * actual shape face. If <b>merge</b> is true, the parameters will be merge with the shape face parameters instead of completely
	 * overriding them
	 * 
	 * @param face
	 * @param params
	 * @param merge
	 * @return
	 */
	public Shape setParameters(String name, RenderParameters params, boolean merge)
	{
		Face face = getFace(name);
		if (face != null)
		{
			if (merge)
				face.getParameters().merge(params);
			else
				face.setParameters(params);
		}
		return this;
	}

	/**
	 * Sets the color for this <code>Shape</code>. RenderParameters.usePerVertexColor should be set to true for it to have an effect.
	 * 
	 * @param color
	 */
	public void setColor(int color)
	{
		for (Face f : faces)
		{
			f.setColor(color);
		}
	}

	/**
	 * Sets the size of this <code>Shape</code>. <b>width</b> represents East-West axis, <b>height</b> represents Bottom-Top axis and
	 * <b>Depth</b> represents North-South axis. The calculations are based on vertexes names.
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
				String name = v.name();
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
	 * Sets the bounds for this <code>Shape</code>. Calculations are based on vertexes names
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
	 * Limits this <code>Shape</code> to the bounding box passed.
	 * 
	 * @param aabb
	 * @return
	 */
	public Shape limit(AxisAlignedBB aabb)
	{
		return limit(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
	}

	/**
	 * Limits this <code>Shape</code> to the bounding box passed.
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
	 * Translates this <code>Shape</code>.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public Shape translate(float x, float y, float z)
	{
		matrix().translate(new Vector3f(x, y, z));
		return this;
	}

	/**
	 * Scales this <code>Shape</code> on all axis.
	 * 
	 * @param f
	 * @return
	 */
	public Shape scale(float f)
	{
		return scale(f, f, f);
	}

	/**
	 * Scales this <code>Shape</code>.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public Shape scale(float x, float y, float z)
	{
		matrix().scale(new Vector3f(x, y, z));
		return this;
	}

	/**
	 * Rotates this <code>Shape</code> around the given axis the specified angle.
	 * 
	 * @param angle
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public Shape rotate(float angle, float x, float y, float z)
	{
		return rotate(angle, x, y, z, 0, 0, 0);
	}

	/**
	 * Rotates this <code>Shape</code> around the given axis the specified angle. Offsets the origin for the rotation.
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
	public Shape rotate(float angle, float x, float y, float z, float offsetX, float offsetY, float offsetZ)
	{
		translate(offsetX, offsetY, offsetZ);
		matrix().rotate((float) Math.toRadians(angle), new Vector3f(x, y, z));
		translate(-offsetX, -offsetY, -offsetZ);
		return this;
	}

	/**
	 * Rotates this <code>Shape</code> around the X axis the specified angle.
	 * 
	 * @param angle
	 * @return
	 */
	public Shape rotateAroundX(float angle)
	{
		return rotate(angle, 1, 0, 0, 0, 0, 0);
	}

	/**
	 * Rotates this <code>Shape</code> around the X axis the specified angle. Offsets the origin for the rotation.
	 * 
	 * @param angle
	 * @param y
	 * @param z
	 * @return
	 */
	public Shape rotateAroundX(float angle, float y, float z)
	{
		return rotate(angle, 1, 0, 0, 0, y, z);
	}

	/**
	 * Rotates this <code>Shape</code> around the Y axis the specified angle.
	 * 
	 * @param angle
	 * @return
	 */
	public Shape rotateAroundY(float angle)
	{
		return rotate(angle, 0, 1, 0, 0, 0, 0);
	}

	/**
	 * Rotates this <code>Shape</code> around the given Y the specified angle. Offsets the origin for the rotation.
	 * 
	 * @param angle
	 * @param x
	 * @param z
	 * @return
	 */
	public Shape rotateAroundY(float angle, float x, float z)
	{
		return rotate(angle, 0, 1, 0, x, 0, z);
	}

	/**
	 * Rotates this <code>Shape</code> around the Z axis the specified angle.
	 * 
	 * @param angle
	 * @return
	 */
	public Shape rotateAroundZ(float angle)
	{
		return rotate(angle, 0, 0, 1, 0, 0, 0);
	}

	/**
	 * Rotates this <code>Shape</code> around the Z axis the specified angle. Offsets the origin for the rotation.
	 * 
	 * @param angle
	 * @param x
	 * @param y
	 * @return
	 */
	public Shape rotateAroundZ(float angle, float x, float y)
	{
		return rotate(angle, 0, 0, 1, x, y, 0);
	}

	/**
	 * Rotates this <code>Shape</code> around itself on the given axis the specified angle.
	 * 
	 * @param angle
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public Shape pivot(float angle, float x, float y, float z)
	{
		rotationMatrix().rotate((float) Math.toRadians(angle), new Vector3f(x, y, z));
		return this;
	}

	/**
	 * Rotates this <code>Shape</code> around itself on the X axis the specified angle.
	 * 
	 * @param angle
	 * @return
	 */
	public Shape pivotX(float angle)
	{
		return pivot(angle, 1, 0, 0);
	}

	/**
	 * Rotates this <code>Shape</code> around itself on the Y axis the specified angle.
	 * 
	 * @param angle
	 * @return
	 */
	public Shape pivotY(float angle)
	{
		return pivot(angle, 0, 1, 0);
	}

	/**
	 * Rotates this <code>Shape</code> around itself on the Z axis the specified angle.
	 * 
	 * @param angle
	 * @return
	 */
	public Shape pivotZ(float angle)
	{
		return pivot(angle, 0, 0, 1);
	}

	/**
	 * Copies the transformation from <b>shape</b> to this <code>Shape</code>.
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
	 * Applies the transformations matrices to this <code>Shape</code>. This modifies to position of the vertexes making up the faces of
	 * this <code>Shape</code>.
	 * 
	 * @return
	 */
	public Shape applyMatrix()
	{
		if (transformMatrix == null && selfRotationMatrix == null)
			return this;

		if (transformMatrix != null)
			transformMatrix.translate(new Vector3f(-0.5F, -0.5F, -0.5F));
		if (selfRotationMatrix != null)
			selfRotationMatrix.translate(new Vector3f(-0.5F, -0.5F, -0.5F));

		for (Face f : faces)
		{
			for (Vertex v : f.getVertexes())
			{
				if (selfRotationMatrix != null)
					v.applyMatrix(selfRotationMatrix);
				if (transformMatrix != null)
					v.applyMatrix(transformMatrix);
			}
		}

		transformMatrix = null;
		selfRotationMatrix = null;
		return this;
	}

	/**
	 * Shrinks the face matching <b>face</b> name by a certain <b>factor</b>. The vertexes of connected faces are moved too.
	 * 
	 * @param face
	 * @param factor
	 * @return
	 */
	public Shape shrink(Face face, float factor)
	{
		if (face == null)
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

		for (Face f : faces)
		{
			for (Vertex v : f.getVertexes())
			{
				Vertex tmpV = vertexNames.get(v.name());
				if (tmpV != null)
					v.set(tmpV.getX(), tmpV.getY(), tmpV.getZ());
			}
		}

		return this;
	}

	public Shape removeFace(Face face)
	{
		faces = ArrayUtils.removeElement(faces, face);
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
