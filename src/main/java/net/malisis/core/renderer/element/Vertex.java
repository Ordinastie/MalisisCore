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

import net.malisis.core.util.Point;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;

public class Vertex
{
	public static final int BRIGHTNESS_MAX = 15728880;

	//@formatter:off
	public static class TopNorthWest extends Vertex { public TopNorthWest() { super(0, 1, 0); }}
	public static class TopNorthEast extends Vertex { public TopNorthEast() { super(1, 1, 0); }}
	public static class TopSouthWest extends Vertex { public TopSouthWest() { super(0, 1, 1); }}
	public static class TopSouthEast extends Vertex { public TopSouthEast() { super(1, 1, 1); }}
	public static class BottomNorthWest extends Vertex { public BottomNorthWest() { super(0, 0, 0); }}
	public static class BottomNorthEast extends Vertex { public BottomNorthEast() { super(1, 0, 0); }}
	public static class BottomSouthWest extends Vertex { public BottomSouthWest() { super(0, 0, 1); }}
	public static class BottomSouthEast extends Vertex { public BottomSouthEast() { super(1, 0, 1); }}
	//@formatter:on

	private String baseName;
	private double x = 0;
	private double y = 0;
	private double z = 0;
	private int brightness = 0;
	private int color = 0xFFFFFF;
	private int alpha = 255;
	private double u = 0.0F;
	private double v = 0.0F;

	private Vertex initialState;

	public Vertex(double x, double y, double z, int rgba, int brightness, double u, double v, boolean isInitialState)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.color = (rgba >>> 8) & 0xFFFFFF;
		this.alpha = rgba & 0xFF;
		this.brightness = brightness;
		this.u = u;
		this.v = v;
		this.baseName();

		if (!isInitialState)
			initialState = new Vertex(x, y, z, rgba, brightness, u, v, true);
	}

	public Vertex(double x, double y, double z, int rgba, int brightness)
	{
		this(x, y, z, rgba, brightness, 0, 0, false);
	}

	public Vertex(double x, double y, double z)
	{
		this(x, y, z, 0xFFFFFFFF, BRIGHTNESS_MAX, 0, 0, false);
	}

	public Vertex(Vertex vertex)
	{
		this(vertex.x, vertex.y, vertex.z, vertex.color << 8 | vertex.alpha, vertex.brightness, vertex.u, vertex.v, false);
		baseName = vertex.baseName;
	}

	public Vertex(Vertex vertex, int rgba, int brightness)
	{
		this(vertex.x, vertex.y, vertex.z, rgba, brightness);
	}

	public Vertex(Vertex vertex, int rgba, int brightness, float u, float v)
	{
		this(vertex.x, vertex.y, vertex.z, rgba, brightness, u, v, false);
	}

	public double getX()
	{
		return x;
	}

	public int getIntX()
	{
		return (int) Math.round(x);
	}

	public Vertex setX(double x)
	{
		this.x = x;
		return this;
	}

	public double getY()
	{
		return y;
	}

	public int getIntY()
	{
		return (int) Math.round(y);
	}

	public Vertex setY(double y)
	{
		this.y = y;
		return this;
	}

	public double getZ()
	{
		return z;
	}

	public int getIntZ()
	{
		return (int) Math.round(z);
	}

	public Vertex setZ(double z)
	{
		this.z = z;
		return this;
	}

	public void set(double x, double y, double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void limit(double min, double max)
	{
		x = clamp(x, min, max);
		y = clamp(y, min, max);
		z = clamp(z, min, max);
	}

	public void interpolateCoord(AxisAlignedBB bounds)
	{
		if (bounds == null)
			return;

		double fx = bounds.maxX - bounds.minX;
		double fy = bounds.maxY - bounds.minY;
		double fz = bounds.maxZ - bounds.minZ;

		x = x * fx + bounds.minX;
		y = y * fy + bounds.minY;
		z = z * fz + bounds.minZ;
	}

	public Vertex add(double x, double y, double z)
	{
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}

	public Vertex factorX(float f)
	{
		x *= f;
		return this;
	}

	public Vertex factorY(float f)
	{
		y *= f;
		return this;
	}

	public Vertex factorZ(float f)
	{
		z *= f;
		return this;
	}

	public Vertex factor(float f)
	{
		factorX(f);
		factorY(f);
		factorZ(f);
		return this;
	}

	public Vertex scale(float f)
	{
		return scale(f, 0.5, 0.5, 0.5);
	}

	public Vertex scale(float f, double centerX, double centerY, double centerZ)
	{
		x = (x - centerX) * f + centerX;
		y = (y - centerY) * f + centerY;
		z = (z - centerZ) * f + centerZ;
		return this;
	}

	public Vertex rotateAroundX(double angle)
	{
		return rotateAroundX(angle, 0.5, 0.5, 0.5);
	}

	public Vertex rotateAroundX(double angle, double centerX, double centerY, double centerZ)
	{
		angle = Math.toRadians(angle);
		double ty = y - centerY;
		double tz = z - centerZ;
		y = ty * Math.cos(angle) - tz * Math.sin(angle);
		z = ty * Math.sin(angle) + tz * Math.cos(angle);
		y += centerY;
		z += centerZ;
		return this;
	}

	public Vertex rotateAroundY(double angle)
	{
		return rotateAroundY(angle, 0.5, 0.5, 0.5);
	}

	public Vertex rotateAroundY(double angle, double centerX, double centerY, double centerZ)
	{
		angle = Math.toRadians(angle);
		double tx = x - centerX;
		double tz = z - centerZ;
		x = tx * Math.cos(angle) + tz * Math.sin(angle);
		z = -tx * Math.sin(angle) + tz * Math.cos(angle);
		x += centerX;
		z += centerZ;
		return this;
	}

	public Vertex rotateAroundZ(double angle)
	{
		return rotateAroundZ(angle, 0.5, 0.5, 0.5);
	}

	public Vertex rotateAroundZ(double angle, double centerX, double centerY, double centerZ)
	{
		angle = Math.toRadians(angle);
		double tx = x - centerX;
		double ty = y - centerY;
		x = tx * Math.cos(angle) - ty * Math.sin(angle);
		y = tx * Math.sin(angle) + ty * Math.cos(angle);
		x += centerX;
		y += centerY;
		return this;
	}

	public Vertex setColor(int color)
	{
		this.color = color;
		return this;
	}

	public int getColor()
	{
		return this.color;
	}

	public Vertex setAlpha(int alpha)
	{
		this.alpha = alpha;
		return this;
	}

	public int getAlpha()
	{
		return this.alpha;
	}

	public Vertex setBrightness(int brightness)
	{
		this.brightness = brightness;
		return this;
	}

	public int getBrightness()
	{
		return this.brightness;
	}

	public void setUV(float u, float v)
	{
		this.u = u;
		this.v = v;
	}

	public double getU()
	{
		return this.u;
	}

	public double getV()
	{
		return this.v;
	}

	public void limitU(float min, float max)
	{
		u = Math.max(Math.min(u, max), min);
	}

	public void limitV(float min, float max)
	{
		v = Math.max(Math.min(v, max), min);
	}

	public boolean isCorner()
	{
		return (x == 1 || x == 0) && (y == 1 || y == 0) && (z == 1 || z == 0);
	}

	public String baseName()
	{
		if (baseName == null)
		{
			baseName = "";
			if (isCorner())
				baseName = (y == 1 ? "Top" : "Bottom") + (z == 1 ? "South" : "North") + (x == 1 ? "East" : "West");
		}
		return baseName;
	}

	public String name()
	{
		return baseName() + " [" + x + ", " + y + ", " + z + "|" + u + ", " + v + "]";
	}

	@Override
	public String toString()
	{
		return name() + " 0x" + Integer.toHexString(color) + " (a:" + alpha + ", b:" + brightness + ")";
	}

	public Point toPoint()
	{
		return new Point(x, y, z);
	}

	public static double clamp(double value)
	{
		return clamp(value, 0, 1);
	}

	public static double clamp(double value, double min, double max)
	{
		if (value < min)
			return min;
		if (value > max)
			return max;
		return value;
	}

	public void applyMatrix(Matrix4f transformMatrix)
	{
		Vector4f vec = new Vector4f((float) x, (float) y, (float) z, 1F);
		Matrix4f.transform(transformMatrix, vec, vec);
		x = vec.x;
		y = vec.y;
		z = vec.z;
	}

	private void setState(Vertex vertex)
	{
		x = vertex.x;
		y = vertex.y;
		z = vertex.z;
		brightness = vertex.alpha;
		color = vertex.color;
		alpha = vertex.alpha;
		u = vertex.u;
		v = vertex.v;
	}

	public void setInitialState()
	{
		initialState.setState(this);
	}

	public void resetState()
	{
		setState(initialState);
	}

	/**
	 * Calculates AoMatrix for a vertex based on the vertex position and the face it belongs. Only works for regular N/S/E/W/T/B faces
	 *
	 * @param offset the offset
	 * @return the matrix
	 */
	public int[][] getAoMatrix(ForgeDirection offset)
	{
		int[][] a = new int[3][3];

		if (offset == ForgeDirection.WEST || offset == ForgeDirection.EAST)
		{
			a[0][0] = a[1][0] = a[2][0] = offset.offsetX;
			a[1][1] += y * 2 - 1; // 1 if 0, 1 if 1;
			a[2][1] += y * 2 - 1; // -1 if 0, 1 if 1;
			a[0][2] += z * 2 - 1; // -1 if 0, 1 if 1;
			a[1][2] += z * 2 - 1; // -1 if 0, 1 if 1;
		}
		else if (offset == ForgeDirection.UP || offset == ForgeDirection.DOWN)
		{
			a[0][1] = a[1][1] = a[2][1] = offset.offsetY;
			a[1][0] += x * 2 - 1; // 1 if 0, 1 if 1;
			a[2][0] += x * 2 - 1; // -1 if 0, 1 if 1;
			a[0][2] += z * 2 - 1; // -1 if 0, 1 if 1;
			a[1][2] += z * 2 - 1; // -1 if 0, 1 if 1;
		}
		else if (offset == ForgeDirection.NORTH || offset == ForgeDirection.SOUTH)
		{
			a[0][2] = a[1][2] = a[2][2] = offset.offsetZ;
			a[1][0] += x * 2 - 1; // 1 if 0, 1 if 1;
			a[2][0] += x * 2 - 1; // -1 if 0, 1 if 1;
			a[0][1] += y * 2 - 1; // -1 if 0, 1 if 1;
			a[1][1] += y * 2 - 1; // -1 if 0, 1 if 1;
		}

		return a;
	}
}
