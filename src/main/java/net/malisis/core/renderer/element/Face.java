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
import java.util.List;

import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.icon.MalisisIcon;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

public class Face
{
	protected String baseName;
	protected Vertex[] vertexes;
	protected RenderParameters params = new RenderParameters();

	public Face(Vertex[] vertexes, RenderParameters params)
	{
		this.vertexes = vertexes;
		if (params != null)
			this.params = params;
		this.baseName();
	}

	public Face(Vertex... vertexes)
	{
		this(vertexes, null);
	}

	public Face(List<Vertex> vertexes)
	{
		this(vertexes.toArray(new Vertex[0]), null);
	}

	public Face(Face face)
	{
		this(face, new RenderParameters(face.params));
	}

	public Face(Face face, RenderParameters params)
	{
		this(face.vertexes, params);
		baseName = face.baseName;
	}

	public Vertex[] getVertexes()
	{
		return vertexes;
	}

	public Face setParameters(RenderParameters params)
	{
		this.params = params != null ? params : new RenderParameters();
		return this;
	}

	public RenderParameters getParameters()
	{
		return params;
	}

	public Face setColor(int color)
	{
		for (Vertex v : vertexes)
			v.setColor(color);
		return this;
	}

	public Face setAlpha(int alpha)
	{
		for (Vertex v : vertexes)
			v.setAlpha(alpha);
		return this;
	}

	public Face setBrightness(int brightness)
	{
		for (Vertex v : vertexes)
			v.setBrightness(brightness);
		return this;
	}

	public Face setTexture(IIcon icon)
	{
		return setTexture(icon, params.flipU.get(), params.flipV.get(), false);
	}

	public Face setStandardUV()
	{
		vertexes[0].setUV(0, 0);
		vertexes[1].setUV(0, 1);
		vertexes[2].setUV(1, 1);
		vertexes[3].setUV(1, 0);
		return this;
	}

	public Face interpolateUV()
	{
		float u = 0;
		float v = 0;
		float U = 1;
		float V = 1;

		double factorU, factorV;

		float uvs[][] = new float[vertexes.length][2];
		for (int i = 0; i < vertexes.length; i++)
		{
			Vertex vertex = vertexes[i];

			factorU = getFactorU(vertex);
			factorV = getFactorV(vertex);

			int k = i;
			uvs[k] = new float[] { interpolate(u, U, factorU, false), interpolate(v, V, factorV, false) };
		}

		for (int i = 0; i < vertexes.length; i++)
			vertexes[i].setUV(uvs[i][0], uvs[i][1]);

		return this;
	}

	public Face setTexture(IIcon icon, boolean flippedU, boolean flippedV, boolean interpolate)
	{
		if (icon == null)
			return this;

		float u = icon.getMinU();
		float v = icon.getMinV();
		float U = icon.getMaxU();
		float V = icon.getMaxV();

		double factorU, factorV;

		float uvs[][] = new float[vertexes.length][2];
		for (int i = 0; i < vertexes.length; i++)
		{
			Vertex vertex = vertexes[i];

			factorU = interpolate ? getFactorU(vertex) : vertex.getU();
			factorV = interpolate ? getFactorV(vertex) : vertex.getV();

			int k = i;
			if (icon instanceof MalisisIcon)
			{
				k = (i + ((MalisisIcon) icon).getRotation()) % vertexes.length;
			}
			uvs[k] = new float[] { interpolate(u, U, factorU, flippedU), interpolate(v, V, factorV, flippedV) };
		}

		for (int i = 0; i < vertexes.length; i++)
			vertexes[i].setUV(uvs[i][0], uvs[i][1]);

		return this;
	}

	private double getFactorU(Vertex vertex)
	{
		if (params.textureSide.get() == null)
			return vertex.getU();

		switch (params.textureSide.get())
		{
			case EAST:
				return vertex.getZ();
			case WEST:
				return vertex.getZ();
			case NORTH:
				return vertex.getX();
			case SOUTH:
			case UP:
			case DOWN:
				return vertex.getX();
			default:
				return 0;
		}
	}

	private double getFactorV(Vertex vertex)
	{
		if (params.textureSide.get() == null)
			return vertex.getV();

		switch (params.textureSide.get())
		{
			case EAST:
			case WEST:
			case NORTH:
			case SOUTH:
				return 1 - vertex.getY();
			case UP:
			case DOWN:
				return vertex.getZ();
			default:
				return 0;
		}
	}

	private float interpolate(float min, float max, double factor, boolean flipped)
	{
		if (factor > 1)
			factor = 1;
		if (factor < 0)
			factor = 0;
		if (flipped)
			factor = 1 - factor;

		return min + (max - min) * (float) factor;
	}

	public Face factor(float fx, float fy, float fz)
	{
		for (Vertex v : vertexes)
		{
			v.factorX(fx);
			v.factorY(fy);
			v.factorZ(fz);
		}
		return this;
	}

	public Face translate(double x, double y, double z)
	{
		for (Vertex v : vertexes)
			v.add(x, y, z);
		return this;
	}

	public void scale(float f)
	{
		scale(f, 0.5, 0.5, 0.5);
	}

	public void scale(float f, double x, double y, double z)
	{
		for (Vertex v : vertexes)
			v.scale(f, x, y, z);
	}

	public void rotateAroundX(double angle)
	{
		rotateAroundX(angle, 0.5, 0.5, 0.5);
	}

	public void rotateAroundX(double angle, double centerX, double centerY, double centerZ)
	{
		for (Vertex v : vertexes)
			v.rotateAroundX(angle, centerX, centerY, centerZ);
	}

	public void rotateAroundY(double angle)
	{
		rotateAroundY(angle, 0.5, 0.5, 0.5);
	}

	public void rotateAroundY(double angle, double centerX, double centerY, double centerZ)
	{
		for (Vertex v : vertexes)
			v.rotateAroundY(angle, centerX, centerY, centerZ);
	}

	public void rotateAroundZ(double angle)
	{
		rotateAroundZ(angle, 0.5, 0.5, 0.5);
	}

	public void rotateAroundZ(double angle, double centerX, double centerY, double centerZ)
	{
		for (Vertex v : vertexes)
			v.rotateAroundZ(angle, centerX, centerY, centerZ);
	}

	/**
	 * Automatically calculate AoMatrix for this {@link Face}. Only works for regular N/S/E/W/T/B faces
	 *
	 * @param face
	 * @param offset
	 */
	public int[][][] calculateAoMatrix(ForgeDirection offset)
	{
		int[][][] aoMatrix = new int[vertexes.length][3][3];

		for (int i = 0; i < vertexes.length; i++)
			aoMatrix[i] = vertexes[i].getAoMatrix(offset);

		return aoMatrix;
	}

	public void setBaseName(String name)
	{
		baseName = name;
	}

	public String baseName()
	{
		if (baseName == null)
		{
			baseName = "";
			HashMap<String, Integer> map = new HashMap<String, Integer>();
			String[] dirs = new String[] { "North", "South", "East", "West", "Top", "Bottom" };
			for (String dir : dirs)
			{
				map.put(dir, 0);
				for (Vertex v : vertexes)
				{
					if (v.name().contains(dir))
						map.put(dir, map.get(dir) + 1);
				}
				if (map.get(dir) == 4)
					baseName = dir;
			}
		}
		return baseName;
	}

	public String name()
	{
		String s = baseName() + " {";
		for (Vertex v : vertexes)
			s += v.name() + ", ";
		return s + "}";
	}

	@Override
	public String toString()
	{
		return name();
	}

	/**
	 * Gets a {@link Face} name from a {@link ForgeDirection}.
	 * 
	 * @param dir
	 * @return
	 */
	public static String name(ForgeDirection dir)
	{
		if (dir == ForgeDirection.UP)
			return "top";
		else if (dir == ForgeDirection.DOWN)
			return "bottom";
		else
			return dir.toString();
	}

}
