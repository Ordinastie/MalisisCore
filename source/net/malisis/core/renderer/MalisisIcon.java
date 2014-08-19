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

package net.malisis.core.renderer;

import net.minecraft.util.IIcon;

/**
 * @author Ordinastie
 * 
 */
public class MalisisIcon implements IIcon, Cloneable
{
	/**
	 * Position in pixels
	 */
	protected int x = 0, y = 0;
	/**
	 * Dimension in pixels
	 */
	protected int width = 16, height = 16;
	/**
	 * Minimum UVs
	 */
	protected float u = 0, v = 0;
	/**
	 * Maximum UVs
	 */
	protected float U = 1, V = 1;
	/**
	 * Is the icon flipped on the U axis
	 */
	protected boolean flippedU = false;
	/**
	 * Is the icon flipped on the V axis
	 */
	protected boolean flippedV = false;
	/**
	 * Rotation value (clockwise)
	 */
	protected int rotation = 0;
	/**
	 * Name of the icon
	 */
	protected String name;

	public MalisisIcon()
	{}

	public int getX()
	{
		return x;
	}

	public int getY()
	{
		return y;
	}

	@Override
	public int getIconWidth()
	{
		return width;
	}

	@Override
	public int getIconHeight()
	{
		return height;
	}

	@Override
	public float getMinU()
	{
		return this.flippedU ? U : u;
	}

	@Override
	public float getMaxU()
	{
		return this.flippedU ? u : U;
	}

	@Override
	public float getInterpolatedU(double f)
	{
		return (float) (getMinU() + (f / 16) * (getMaxU() - getMinU()));
	}

	@Override
	public float getMinV()
	{
		return this.flippedV ? V : v;
	}

	@Override
	public float getMaxV()
	{
		return this.flippedV ? v : V;
	}

	@Override
	public float getInterpolatedV(double f)
	{
		return (float) (getMinV() + (f / 16) * (getMaxV() - getMinV()));
	}

	public void flip(boolean horizontal, boolean vertical)
	{
		flippedU = horizontal;
		flippedV = vertical;
	}

	public boolean isFlippedU()
	{
		return flippedU;
	}

	public boolean isFlippedV()
	{
		return flippedV;
	}

	public boolean isRotated()
	{
		return rotation != 0;
	}

	public void setRotation(int rotation)
	{
		this.rotation = rotation;
	}

	public int getRotation()
	{
		return rotation;
	}

	@Override
	public String getIconName()
	{
		return name;
	}

	@Override
	public MalisisIcon clone()
	{
		MalisisIcon clone = null;
		try
		{
			clone = (MalisisIcon) super.clone();
		}
		catch (CloneNotSupportedException e)
		{
			e.printStackTrace();
		}
		return clone;
	}

	public void offset(int offsetX, int offsetY)
	{
		x += offsetX;
		y += offsetY;
		u = getInterpolatedU(offsetX);
		v = getInterpolatedU(offsetY);
		U = getInterpolatedU(offsetX + width);
		V = getInterpolatedU(offsetY + height);
	}

	public void clip(int offsetX, int offsetY, int width, int height)
	{
		x += offsetX;
		y += offsetY;
		this.width = width;
		this.height = height;
		float u = getInterpolatedU(offsetX);
		float v = getInterpolatedV(offsetY);
		float U = getInterpolatedU(offsetX + width);
		float V = getInterpolatedV(offsetY + height);

		this.u = u;
		this.v = v;
		this.U = U;
		this.V = V;
	}

}
