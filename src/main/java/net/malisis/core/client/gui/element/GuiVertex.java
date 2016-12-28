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

package net.malisis.core.client.gui.element;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;

import net.malisis.core.renderer.element.Vertex;
import net.malisis.core.util.Point;

/**
 * @author Ordinastie
 *
 */
public class GuiVertex
{
	public static enum VertexPosition
	{
		TOPLEFT(0, 0),
		TOPRIGHT(1, 0),
		BOTTOMRIGHT(1, 1),
		BOTTOMLEFT(0, 1);

		private int x, y;

		VertexPosition(int x, int y)
		{
			this.x = x;
			this.y = y;
		}

		public int getX(int x, int width)
		{
			return x + this.x * width;
		}

		public int getY(int y, int height)
		{
			return y + this.y * height;
		}
	}

	VertexPosition position;
	/** X coordinate of this {@link GuiVertex} **/
	private int x = 0;
	/** Y coordinate of this {@link GuiVertex} **/
	private int y = 0;
	/** Z index of this {@link GuiVertex} **/
	private int zIndex = 0;
	/** Brightness of this {@link GuiVertex} **/
	private int brightness = 0;
	/** Color of this {@link GuiVertex} **/
	private int color = 0xFFFFFF;
	private int alpha = 255;
	private int normal = 0;
	private float u = 0.0F;
	private float v = 0.0F;

	public GuiVertex(VertexPosition position)
	{
		this.position = position;
		reset();
	}

	public void reset()
	{
		set(position.x, position.y, 0);
		setUV(position.x, position.y);
	}

	//#region Getters/Setters
	public VertexPosition getPosition()
	{
		return position;
	}

	public int getX()
	{
		return x;
	}

	public void setX(int x)
	{
		this.x = x;
	}

	public int getY()
	{
		return y;
	}

	public void setY(int y)
	{
		this.y = y;
	}

	public int getZIndex()
	{
		return zIndex;
	}

	public void setZIndex(int zIndex)
	{
		this.zIndex = zIndex;
	}

	public void set(int x, int y, int z)
	{
		this.x = x;
		this.y = y;
		this.zIndex = z;
	}

	public int getColor()
	{
		return this.color;
	}

	public void setColor(int color)
	{
		this.color = color & 0xFFFFFF;
	}

	public int getAlpha()
	{
		return this.alpha;
	}

	public void setAlpha(int alpha)
	{
		this.alpha = alpha & 255;
	}

	public int getRGBA()
	{
		int r = (color >> 16) & 255;
		int g = (color >> 8) & 255;
		int b = color & 255;
		return alpha << 24 | b << 16 | g << 8 | r;
	}

	public void setRGBA(int rgba)
	{
		this.color = (rgba >>> 8) & 0xFFFFFF;
		this.alpha = rgba & 255;
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

	public void setBrightness(int brightness)
	{
		this.brightness = brightness;
	}

	public int getNormal()
	{
		return normal;
	}

	public void setNormal(float x, float y, float z)
	{
		byte b0 = (byte) (x * 127.0F);
		byte b1 = (byte) (y * 127.0F);
		byte b2 = (byte) (z * 127.0F);
		normal = b0 & 255 | (b1 & 255) << 8 | (b2 & 255) << 16;
	}

	public float getU()
	{
		return this.u;
	}

	public float getV()
	{
		return this.v;
	}

	public void setUV(float u, float v)
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
	public void translate(double x, double y)
	{
		this.x += x;
		this.y += y;
	}

	public void scaleX(float f)
	{
		scaleX(f, 0);
	}

	public void scaleX(float f, float offset)
	{
		x = Math.round((x - offset) * f + offset);
	}

	public void scaleY(float f)
	{
		scaleY(f, 0);
	}

	public void scaleY(float f, float offset)
	{
		y = Math.round((y - offset) * f + offset);
	}

	public void scale(float f)
	{
		scale(f, 0);
	}

	public void scale(float f, float offset)
	{
		scaleX(f, offset);
		scaleY(f, offset);
	}

	public void scale(float fx, float fy, float offsetX, float offsetY)
	{
		scaleX(fx, offsetX);
		scaleY(fy, offsetY);
	}

	public void rotate(double angle)
	{
		rotate(angle, 0.5, 0.5, 0.5);
	}

	public void rotate(double angle, double centerX, double centerY, double centerZ)
	{
		angle = Math.toRadians(angle);
		double x = this.x - centerX;
		double y = this.y - centerY;
		x = x * Math.cos(angle) - y * Math.sin(angle);
		y = x * Math.sin(angle) + y * Math.cos(angle);
		x += centerX;
		y += centerY;

		this.x = (int) Math.round(x);
		this.y = (int) Math.round(y);
	}

	public void limitU(float min, float max)
	{
		u = Math.max(Math.min(u, max), min);
	}

	public void limitV(float min, float max)
	{
		v = Math.max(Math.min(v, max), min);
	}

	public Point toPoint()
	{
		return new Point(x, y, zIndex);
	}

	public void applyMatrix(Matrix4f transformMatrix)
	{
		Vector4f vec = new Vector4f(x, y, zIndex, 1F);
		Matrix4f.transform(transformMatrix, vec, vec);
		x = Math.round(vec.x);
		y = Math.round(vec.y);
		zIndex = Math.round(vec.z);
	}

	/**
	 * Gets the vertex data for this {@link Vertex}.
	 *
	 * @param vertexFormat the vertex format
	 * @param offset the offset
	 * @return the vertex data
	 */
	public int[] getVertexData(GuiShape shape, int offsetX, int offsetY, int offsetZ)
	{
		float x = position.getX(shape.getX() + offsetX, shape.getWidth());
		float y = position.getX(shape.getY() + offsetY, shape.getHeight());
		float z = shape.getZIndex() + getZIndex() + offsetZ;
		float u = shape.getIcon().getInterpolatedU(getU());
		float v = shape.getIcon().getInterpolatedV(getV());

		return new int[] {	Float.floatToRawIntBits(x),
							Float.floatToRawIntBits(y),
							Float.floatToRawIntBits(z),
							getRGBA(),
							Float.floatToRawIntBits(u),
							Float.floatToRawIntBits(v),
							getBrightness(),
							getNormal() };
	}

	public String name()
	{
		return position + " [" + x + ", " + y + ", " + zIndex + "|" + u + ", " + v + "]";
	}

	@Override
	public String toString()
	{
		return name() + " 0x" + Integer.toHexString(color) + " (a:" + alpha + ", bb:" + getBlockBrightness() + ", sb:" + getSkyBrightness()
				+ ")";
	}
}
