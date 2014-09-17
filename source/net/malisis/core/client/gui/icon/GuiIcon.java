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

package net.malisis.core.client.gui.icon;

import net.malisis.core.renderer.MalisisIcon;

/**
 * @author Ordinastie
 * 
 */
public class GuiIcon extends MalisisIcon
{
	private static final int GUI_TEXTURE_WIDTH = 300;
	private static final int GUI_TEXTURE_HEIGHT = 100;

	private int x;
	private int y;
	private int width;
	private int height;

	public GuiIcon()
	{
		super(null);
		x = 0;
		y = 0;
		width = GUI_TEXTURE_WIDTH;
		height = GUI_TEXTURE_HEIGHT;
	}

	public GuiIcon(int x, int y, int width, int height)
	{
		super(null);
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	private float getU(boolean max)
	{
		if (flippedU)
			max = !max;
		return max ? (float) (x + width) / GUI_TEXTURE_WIDTH : (float) x / GUI_TEXTURE_WIDTH;
	}

	private float getV(boolean max)
	{
		if (flippedV)
			max = !max;

		return max ? (float) (y + height) / GUI_TEXTURE_HEIGHT : (float) y / GUI_TEXTURE_HEIGHT;
	}

	@Override
	public float getMinU()
	{
		return getU(false);
	}

	@Override
	public float getMaxU()
	{
		return getU(true);
	}

	@Override
	public float getMinV()
	{
		return getV(false);
	}

	@Override
	public float getMaxV()
	{
		return getV(true);
	}

	@Override
	public GuiIcon clone()
	{
		return (GuiIcon) super.clone();
	}

	@Override
	public GuiIcon offset(int offsetX, int offsetY)
	{
		x += offsetX;
		y += offsetY;
		return this;
	}

	@Override
	public GuiIcon clip(int offsetX, int offsetY, int width, int height)
	{
		this.width = width;
		this.height = height;
		return offset(offsetX, offsetY);
	}

	public static GuiIcon[] XYResizable(int x, int y, int width, int height, int corner)
	{
		int w = width - corner * 2;
		int h = height - corner * 2;

		GuiIcon base = new GuiIcon(x, y, corner, corner);
		//@formatter:off
		GuiIcon[] icons = new GuiIcon[] { 
				base, 
				base.clone().clip(corner, 0, w, corner),
				base.clone().clip(corner + w, 0, corner, corner), 
				
				base.clone().clip(0, corner, corner, h),			
				base.clone().clip(corner, corner, w, h),
				base.clone().clip(corner + w, corner, corner, h),
				
				base.clone().clip(0, corner + h, corner, corner),
				base.clone().clip(corner, corner + h, w, corner),
				base.clone().clip(corner +w, corner + h, corner, corner)
		};
		//@formatter:on

		return icons;
	}

	public static GuiIcon[] XResizable(int x, int y, int width, int height, int side)
	{
		int w = width - side * 2;
		int h = height;
		GuiIcon base = new GuiIcon(x, y, side, h);
		//@formatter:off
		GuiIcon[] icons = new GuiIcon[] { 
				base, 
				base.clone().clip(side, 0, w, h),
				base.clone().clip(side + w, 0, side, h), 
		};
		//@formatter:on

		return icons;
	}
}
