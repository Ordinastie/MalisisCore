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

package net.malisis.core.renderer.icon;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;

/**
 * @author Ordinastie
 *
 */
public class MalisisIcon extends TextureAtlasSprite
{
	protected int sheetWidth;
	protected int sheetHeight;

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
	 * Main icon for connected textures
	 */
	protected MalisisIcon parentIcon;

	public MalisisIcon()
	{
		super("");
	}

	public MalisisIcon(String name)
	{
		super(name);
	}

	public MalisisIcon(MalisisIcon baseIcon)
	{
		super(baseIcon.getIconName());
		baseIcon.parentIcon = this;
	}

	//#region getters/setters
	public void setSize(int width, int height)
	{
		this.width = width;
		this.height = height;
	}

	@Override
	public float getMinU()
	{
		return this.flippedU ? maxU : minU;
	}

	@Override
	public float getMaxU()
	{
		return this.flippedU ? minU : maxU;
	}

	@Override
	public float getMinV()
	{
		return this.flippedV ? maxV : minV;
	}

	@Override
	public float getMaxV()
	{
		return this.flippedV ? minV : maxV;
	}

	public MalisisIcon flip(boolean horizontal, boolean vertical)
	{
		flippedU = horizontal;
		flippedV = vertical;
		return this;
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

	//#end getters/setters

	protected void initIcon(MalisisIcon baseIcon, int width, int height, int x, int y, boolean rotated)
	{
		copyFrom(baseIcon);
	}

	public MalisisIcon offset(int offsetX, int offsetY)
	{
		initSprite(sheetWidth, sheetHeight, getOriginX() + offsetX, getOriginY() + offsetY, isRotated());
		return this;
	}

	public MalisisIcon clip(int offsetX, int offsetY, int width, int height)
	{
		this.width = width + (useAnisotropicFiltering ? 16 : 0);
		this.height = height + (useAnisotropicFiltering ? 16 : 0);
		offset(offsetX, offsetY);

		return this;
	}

	public MalisisIcon clip(float offsetXFactor, float offsetYFactor, float widthFactor, float heightFactor)
	{
		if (useAnisotropicFiltering)
		{
			width -= 16;
			height -= 16;
		}

		int offsetX = Math.round(width * offsetXFactor);
		int offsetY = Math.round(height * offsetYFactor);

		width = Math.round(width * widthFactor);
		height = Math.round(height * heightFactor);

		if (useAnisotropicFiltering)
		{
			width += 16;
			height += 16;
		}

		offset(offsetX, offsetY);

		return this;
	}

	@Override
	public void initSprite(int width, int height, int x, int y, boolean rotated)
	{
		this.sheetWidth = width;
		this.sheetHeight = height;
		super.initSprite(width, height, x, y, rotated);
		if (parentIcon != null)
			parentIcon.initIcon(this, width, height, x, y, rotated);
	}

	public void copyFrom(MalisisIcon base)
	{
		super.copyFrom(base);
		this.useAnisotropicFiltering = base.useAnisotropicFiltering;
		this.sheetWidth = base.sheetWidth;
		this.sheetHeight = base.sheetHeight;
		this.flippedU = base.flippedU;
		this.flippedV = base.flippedV;
	}

	public MalisisIcon copy()
	{
		MalisisIcon icon = new MalisisIcon();
		icon.copyFrom(this);
		return icon;
	}

	public MalisisIcon register(TextureMap register)
	{
		register.setTextureEntry(getIconName(), this);
		return this;
	}
}
