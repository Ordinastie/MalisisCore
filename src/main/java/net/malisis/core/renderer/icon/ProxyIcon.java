/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Ordinastie
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

import java.io.IOException;
import java.util.List;

import net.minecraft.client.renderer.texture.PngSizeInfo;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;

/**
 * @author Ordinastie
 *
 */
public class ProxyIcon extends Icon
{
	protected TextureAtlasSprite proxy;

	public ProxyIcon()
	{}

	public ProxyIcon(TextureAtlasSprite proxy)
	{
		setProxy(proxy);
	}

	public TextureAtlasSprite getIcon()
	{
		return proxy != null ? proxy : missing;
	}

	protected void setProxy(TextureAtlasSprite proxy)
	{
		this.proxy = proxy;
	}

	@Override
	public void register(TextureMap map)
	{}

	@Override
	public void initSprite(int inX, int inY, int originInX, int originInY, boolean rotatedIn)
	{}

	@Override
	public void copyFrom(TextureAtlasSprite atlasSprite)
	{}

	@Override
	public int getOriginX()
	{
		return getIcon().getOriginX();
	}

	@Override
	public int getOriginY()
	{
		return getIcon().getOriginY();
	}

	@Override
	public int getIconWidth()
	{
		return getIcon().getIconWidth();
	}

	@Override
	public int getIconHeight()
	{
		return getIcon().getIconHeight();
	}

	@Override
	public float getMinU()
	{
		return flippedU ? getIcon().getMaxU() : getIcon().getMinU();
	}

	@Override
	public float getMaxU()
	{
		return flippedU ? getIcon().getMinU() : getIcon().getMaxU();
	}

	@Override
	public float getInterpolatedU(double u)
	{
		float f = getMaxU() - this.getMinU();
		return getMinU() + f * (float) u / 16.0F;
	}

	@Override
	public float getMinV()
	{
		return flippedV ? getIcon().getMaxV() : getIcon().getMinV();
	}

	@Override
	public float getMaxV()
	{
		return flippedV ? getIcon().getMinV() : getIcon().getMaxV();
	}

	@Override
	public float getInterpolatedV(double v)
	{
		float f = getMaxV() - this.getMinV();
		return getMinV() + f * (float) v / 16.0F;
	}

	@Override
	public String getIconName()
	{
		return getIcon().getIconName();
	}

	@Override
	public void updateAnimation()
	{}

	@Override
	public int[][] getFrameTextureData(int index)
	{
		return getIcon().getFrameTextureData(index);
	}

	@Override
	public int getFrameCount()
	{
		return getIcon().getFrameCount();
	}

	@Override
	public void setIconWidth(int newWidth)
	{}

	@Override
	public void setIconHeight(int newHeight)
	{}

	@Override
	public void loadSprite(PngSizeInfo sizeInfo, boolean animated) throws IOException
	{}

	@Override
	public void generateMipmaps(int level)
	{}

	@Override
	public void clearFramesTextureData()
	{}

	@Override
	public boolean hasAnimationMetadata()
	{
		return getIcon().hasAnimationMetadata();
	}

	@Override
	public void setFramesTextureData(List<int[][]> newFramesTextureData)
	{}

	@Override
	public String toString()
	{
		return "VanillaIcon [" + getIcon() + "]";
	}

}
