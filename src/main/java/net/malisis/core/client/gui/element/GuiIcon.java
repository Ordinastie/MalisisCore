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

import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.renderer.icon.Icon;

/**
 * @author Ordinastie
 *
 */
public class GuiIcon
{
	private float minU = 0;
	private float minV = 0;
	private float maxU = 1;
	private float maxV = 1;
	private int border = 0;

	/** Is the icon flipped on the horizontal axis. */
	protected boolean flippedU = false;
	/** Is the icon flipped on the vertical axis. */
	protected boolean flippedV = false;

	public GuiIcon()
	{

	}

	public GuiIcon(float u, float v, float U, float V)
	{
		this.minU = u;
		this.minV = v;
		this.maxU = U;
		this.maxV = V;
	}

	public GuiIcon(GuiTexture texture, int x, int y, int width, int height, int border)
	{
		int texWidth = texture.getWidth();
		int texHeight = texture.getHeight();

		this.minU = (float) x / texWidth;
		this.minV = (float) y / texHeight;
		this.maxU = (float) (x + width) / texWidth;
		this.maxV = (float) (y + height) / texHeight;

		this.border = border;
	}

	public GuiIcon(GuiTexture texture, int x, int y, int width, int height)
	{
		this(texture, x, y, width, height, 0);
	}

	/**
	 * Gets the min u.
	 *
	 * @return the min u
	 */

	public float getMinU()
	{
		return this.flippedU ? maxU : minU;
	}

	/**
	 * Gets the max u.
	 *
	 * @return the max u
	 */

	public float getMaxU()
	{
		return this.flippedU ? minU : maxU;
	}

	/**
	 * Gets the min v.
	 *
	 * @return the min v
	 */

	public float getMinV()
	{
		return this.flippedV ? maxV : minV;
	}

	/**
	 * Gets the max v.
	 *
	 * @return the max v
	 */

	public float getMaxV()
	{
		return this.flippedV ? minV : maxV;
	}

	/**
	 * Sets this {@link Icon} to be flipped.
	 *
	 * @param horizontal whether to flip horizontally
	 * @param vertical whether to flip vertically
	 * @return this {@link Icon}
	 */
	public void flip(boolean horizontal, boolean vertical)
	{
		flippedU = horizontal;
		flippedV = vertical;
	}

	/**
	 * Checks if is flipped u.
	 *
	 * @return true if this {@link Icon} is flipped horizontally.
	 */
	public boolean isFlippedU()
	{
		return flippedU;
	}

	/**
	 * Checks if is flipped v.
	 *
	 * @return true if this {@link Icon} is flipped vertically.
	 */
	public boolean isFlippedV()
	{
		return flippedV;
	}

	public float getInterpolatedU(float i)
	{
		return getMinU() + i * (getMaxU() - getMinU());
	}

	public float getInterpolatedV(float i)
	{
		return getMinV() + i * (getMaxV() - getMinV());
	}

	public int getBorder()
	{
		return border;
	}

	public GuiIcon clip(float fromU, float fromV, float toU, float toV)
	{
		return new GuiIcon(getInterpolatedU(fromU), getInterpolatedV(fromV), getInterpolatedU(toU), getInterpolatedV(toV));
	}

}
