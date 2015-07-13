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

package net.malisis.core.renderer.font;

import net.malisis.core.renderer.icon.MalisisIcon;

/**
 * @author Ordinastie
 *
 */
public class CharData
{
	protected char c;
	protected float ascent;
	protected float width;
	protected float height;
	protected float u;
	protected float U;
	protected float v;
	protected float V;

	public CharData(char c, float ascent, float width, float height)
	{
		this.c = c;
		this.c = c;
		this.ascent = ascent;
		this.width = width;
		this.height = height;
	}

	//#region Getters/Setters

	public char getChar()
	{
		return c;
	}

	public float u()
	{
		return u;
	}

	public float U()
	{
		return U;
	}

	public float v()
	{
		return v;
	}

	public float V()
	{
		return V;
	}

	public float getCharWidth()
	{
		return width;
	}

	public float getCharHeight()
	{
		return height;
	}

	public float getAscent()
	{
		return ascent;
	}

	public float getFullWidth(FontGeneratorOptions options)
	{
		return width + options.mx + options.px;
	}

	public float getFullHeight(FontGeneratorOptions options)
	{
		return height + options.my + options.py;
	}

	//#end Getters/Setters

	public void setUVs(float u, float v, float U, float V)
	{
		this.u = u;
		this.v = v;
		this.U = U;
		this.V = V;
	}

	public void setUVs(int x, int y, int size, FontGeneratorOptions options)
	{
		u = ((x - options.mx) / size);
		v = (y - options.my) / size;
		U = ((x + width + options.px) / size);
		V = (y + height + options.py) / size;
	}

	public MalisisIcon getIcon()
	{
		return new MalisisIcon("" + getChar(), u(), v(), U(), V());
	}
}
