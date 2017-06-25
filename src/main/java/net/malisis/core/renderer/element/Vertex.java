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

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;

import net.malisis.core.util.Point;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;

public class Vertex
{
	public static final int BRIGHTNESS_MAX = (240 << 16) | 240; //sky << 16 | block

	/** Base name of this {@link Vertex}, set when first instanciated and kept after transformation for easy access. */
	private String baseName;
	/** X coordinate of this {@link Vertex} **/
	private double x = 0;
	/** Y coordinate of this {@link Vertex} **/
	private double y = 0;
	/** Z coordinate of this {@link Vertex} **/
	private double z = 0;
	/** Brightness of this {@link Vertex} **/
	private int brightness = 0;
	/** Color of this {@link Vertex} **/
	private int color = 0xFFFFFF;
	private int alpha = 255;
	private int normal = 0;
	private double u = 0.0F;
	private double v = 0.0F;

	private Vertex initialState;

	public Vertex(double x, double y, double z, int rgba, int brightness, double u, double v, int normal, boolean isInitialState)
	{
		set(x, y, z);
		setRGBA(rgba);
		setBrightness(brightness);
		setUV(u, v);
		this.x = x;
		this.y = y;
		this.z = z;

		this.u = u;
		this.v = v;
		this.normal = normal;
		this.baseName();

		if (!isInitialState)
			initialState = new Vertex(x, y, z, rgba, brightness, u, v, normal, true);
	}

	public Vertex(double x, double y, double z, int rgba, int brightness)
	{
		this(x, y, z, rgba, brightness, 0, 0, 0, false);
	}

	public Vertex(double x, double y, double z)
	{
		this(x, y, z, 0xFFFFFFFF, BRIGHTNESS_MAX, 0, 0, 0, false);
	}

	public Vertex(Vertex vertex)
	{
		this(vertex.x, vertex.y, vertex.z, vertex.color << 8 | vertex.alpha, vertex.getBrightness(), vertex.u, vertex.v, 0, false);
		baseName = vertex.baseName;
	}

	public Vertex(Vertex vertex, int rgba, int brightness)
	{
		this(vertex.x, vertex.y, vertex.z, rgba, brightness);
	}

	public Vertex(Vertex vertex, int rgba, int brightness, float u, float v)
	{
		this(vertex.x, vertex.y, vertex.z, rgba, brightness, u, v, 0, false);
	}

	//#region Getters/Setters
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

	public int getColor()
	{
		return this.color;
	}

	public Vertex setColor(int color)
	{
		this.color = color & 0xFFFFFF;
		return this;
	}

	public int getAlpha()
	{
		return this.alpha;
	}

	public Vertex setAlpha(int alpha)
	{
		this.alpha = alpha & 255;
		return this;
	}

	public int getRGBA()
	{
		int r = (color >> 16) & 255;
		int g = (color >> 8) & 255;
		int b = color & 255;
		return alpha << 24 | b << 16 | g << 8 | r;
	}

	public Vertex setRGBA(int rgba)
	{
		this.color = (rgba >>> 8) & 0xFFFFFF;
		this.alpha = rgba & 255;
		return this;
	}

	public int getBlockBrightness()
	{
		return brightness & 240;
	}

	public int getSkyBrightness()
	{
		return (brightness >> 16) & 240;
	}

	public int getBrightness()
	{
		return brightness;
	}

	public Vertex setBrightness(int brightness)
	{
		this.brightness = brightness;
		return this;
	}

	public int getNormal()
	{
		return normal;
	}

	public Vertex setNormal(float x, float y, float z)
	{
		byte b0 = (byte) (x * 127.0F);
		byte b1 = (byte) (y * 127.0F);
		byte b2 = (byte) (z * 127.0F);
		normal = b0 & 255 | (b1 & 255) << 8 | (b2 & 255) << 16;
		return this;
	}

	public Vertex setNormal(EnumFacing facing)
	{
		if (facing == null)
			return this;
		return setNormal(facing.getFrontOffsetX(), facing.getFrontOffsetY(), facing.getFrontOffsetZ());
	}

	public double getU()
	{
		return this.u;
	}

	public double getV()
	{
		return this.v;
	}

	public void setUV(double u, double v)
	{
		this.u = u;
		this.v = v;
	}

	//#end Getters/Setters

	/**
	 * Translates this {@link Vertex} by the specified amount.
	 *
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @return the vertex
	 */
	public Vertex translate(double x, double y, double z)
	{
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}

	public Vertex scaleX(float f)
	{
		return scaleX(f, 0);
	}

	public Vertex scaleX(float f, float offset)
	{
		x = (x - offset) * f + offset;
		return this;
	}

	public Vertex scaleY(float f)
	{
		return scaleY(f, 0);
	}

	public Vertex scaleY(float f, float offset)
	{
		y = (y - offset) * f + offset;
		return this;
	}

	public Vertex scaleZ(float f)
	{
		return scaleZ(f, 0);
	}

	public Vertex scaleZ(float f, float offset)
	{
		z = (z - offset) * f + offset;
		return this;
	}

	public Vertex scale(float f)
	{
		return scale(f, 0);
	}

	public Vertex scale(float f, float offset)
	{
		scaleX(f, offset);
		scaleY(f, offset);
		scaleZ(f, offset);
		return this;
	}

	public Vertex scale(float fx, float fy, float fz, float offsetX, float offsetY, float offsetZ)
	{
		scaleX(fx, offsetX);
		scaleY(fy, offsetY);
		scaleZ(fz, offsetZ);
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

	public Vertex setBaseName(String name)
	{
		this.baseName = name;
		return this;
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
		return name() + " 0x" + Integer.toHexString(color) + " (a:" + alpha + ", bb:" + getBlockBrightness() + ", sb:" + getSkyBrightness()
				+ ")";
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

	/**
	 * Gets the vertex data for this {@link Vertex}.
	 *
	 * @param vertexFormat the vertex format
	 * @param offset the offset
	 * @return the vertex data
	 */
	public int[] getVertexData(VertexFormat vertexFormat, Vec3d offset)
	{
		float x = (float) getX();
		float y = (float) getY();
		float z = (float) getZ();

		if (offset != null)
		{
			x += offset.x;
			y += offset.y;
			z += offset.z;
		}

		int[] data = new int[vertexFormat.getIntegerSize()];
		int index = 0;
		//private
		//if(vertexFormat.hasPosition())
		{
			data[index++] = Float.floatToRawIntBits(x);
			data[index++] = Float.floatToRawIntBits(y);
			data[index++] = Float.floatToRawIntBits(z);
		}
		if (vertexFormat.hasColor())
			data[index++] = getRGBA();
		if (vertexFormat.hasUvOffset(0)) //normal UVs
		{
			data[index++] = Float.floatToRawIntBits((float) getU());
			data[index++] = Float.floatToRawIntBits((float) getV());
		}
		if (vertexFormat.hasUvOffset(1)) //brightness UVs
			data[index++] = getBrightness();
		if (vertexFormat.hasNormal())
			data[index++] = getNormal();

		return data;
	}

	private void setState(Vertex vertex)
	{
		x = vertex.x;
		y = vertex.y;
		z = vertex.z;
		color = vertex.color;
		alpha = vertex.alpha;
		brightness = vertex.brightness;
		normal = vertex.normal;
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
	public int[][] getAoMatrix(EnumFacing offset)
	{
		int[][] a = new int[3][3];

		if (offset == EnumFacing.WEST || offset == EnumFacing.EAST)
		{
			a[0][0] = a[1][0] = a[2][0] = offset.getFrontOffsetX();
			a[1][1] += Math.round(y * 2 - 1); // -1 if 0, 1 if 1;
			a[2][1] += Math.round(y * 2 - 1); // -1 if 0, 1 if 1;
			a[0][2] += Math.round(z * 2 - 1); // -1 if 0, 1 if 1;
			a[1][2] += Math.round(z * 2 - 1); // -1 if 0, 1 if 1;
		}
		else if (offset == EnumFacing.UP || offset == EnumFacing.DOWN)
		{
			a[0][1] = a[1][1] = a[2][1] = offset.getFrontOffsetY();
			a[1][0] += Math.round(x * 2 - 1); // -1 if 0, 1 if 1;
			a[2][0] += Math.round(x * 2 - 1); // -1 if 0, 1 if 1;
			a[0][2] += Math.round(z * 2 - 1); // -1 if 0, 1 if 1;
			a[1][2] += Math.round(z * 2 - 1); // -1 if 0, 1 if 1;
		}
		else if (offset == EnumFacing.NORTH || offset == EnumFacing.SOUTH)
		{
			a[0][2] = a[1][2] = a[2][2] = offset.getFrontOffsetZ();
			a[1][0] += Math.round(x * 2 - 1); // -1 if 0, 1 if 1;
			a[2][0] += Math.round(x * 2 - 1); // -1 if 0, 1 if 1;
			a[0][1] += Math.round(y * 2 - 1); // -1 if 0, 1 if 1;
			a[1][1] += Math.round(y * 2 - 1); // -1 if 0, 1 if 1;
		}

		return a;
	}
}
