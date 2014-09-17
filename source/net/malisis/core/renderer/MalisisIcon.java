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

import java.awt.image.BufferedImage;

import net.malisis.core.renderer.icon.ConnectedTextureIcon;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.data.AnimationMetadataSection;

/**
 * @author Ordinastie
 * 
 */
public class MalisisIcon extends TextureAtlasSprite implements Cloneable
{
	protected int sheetWidth;
	protected int sheetHeight;
	protected boolean useAnisotropicFiltering;

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
	protected ConnectedTextureIcon connectedTextureIcon;

	public MalisisIcon(String name)
	{
		super(name);
	}

	@Override
	public float getMinU()
	{
		return this.flippedU ? super.getMaxU() : super.getMinU();
	}

	@Override
	public float getMaxU()
	{
		return this.flippedU ? super.getMinU() : super.getMaxU();
	}

	@Override
	public float getMinV()
	{
		return this.flippedV ? super.getMaxV() : super.getMinV();
	}

	@Override
	public float getMaxV()
	{
		return this.flippedV ? super.getMinV() : super.getMaxV();
	}

	public void setConnectedTextureIcon(ConnectedTextureIcon icon)
	{
		connectedTextureIcon = icon;
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

	@Override
	public MalisisIcon clone()
	{
		MalisisIcon clone = null;
		try
		{
			clone = (MalisisIcon) super.clone();
			clone.connectedTextureIcon = null;
		}
		catch (CloneNotSupportedException e)
		{
			e.printStackTrace();
		}
		return clone;
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
	public void loadSprite(BufferedImage[] buffer, AnimationMetadataSection metadataSection, boolean useAnisotropicFiltering)
	{
		super.loadSprite(buffer, metadataSection, useAnisotropicFiltering);
		this.useAnisotropicFiltering = useAnisotropicFiltering;
	}

	@Override
	public void initSprite(int width, int height, int x, int y, boolean rotated)
	{
		this.sheetWidth = width;
		this.sheetHeight = height;
		super.initSprite(width, height, x, y, rotated);
		if (connectedTextureIcon != null)
			connectedTextureIcon.initIcons(this);
	}

	public MalisisIcon register(TextureMap register)
	{
		register.setTextureEntry(getIconName(), this);
		return this;
	}
}
