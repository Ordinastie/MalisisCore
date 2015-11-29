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

import java.awt.image.BufferedImage;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.item.Item;

/**
 * @author Ordinastie
 *
 */
public class VanillaIcon extends MalisisIcon
{
	protected Item item;
	protected IBlockState blockState;
	protected int metadata;

	public VanillaIcon(Block block)
	{
		this.blockState = block.getDefaultState();
	}

	public VanillaIcon(IBlockState blockState)
	{
		this.blockState = blockState;
	}

	public VanillaIcon(Item item, int metadata)
	{
		this.item = item;
		this.metadata = metadata;
	}

	public VanillaIcon(Item item)
	{
		this(item, 0);
	}

	private TextureAtlasSprite getIcon()
	{
		TextureAtlasSprite icon = item != null ? getItemIcon() : getBlockIcon();
		return icon == null ? missing : icon;
	}

	private TextureAtlasSprite getItemIcon()
	{
		if (Minecraft.getMinecraft().getRenderItem() == null)
			return null;
		return Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getParticleIcon(item, metadata);
	}

	private TextureAtlasSprite getBlockIcon()
	{
		if (blockState == null)
			return null;
		return Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getTexture(blockState);
	}

	@Override
	public void initSprite(int inX, int inY, int originInX, int originInY, boolean rotatedIn)
	{}

	@Override
	public MalisisIcon register(TextureMap textureMap)
	{
		return this;
	}

	@Override
	public void copyFrom(TextureAtlasSprite atlasSpirit)
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
	public void loadSprite(BufferedImage[] images, AnimationMetadataSection meta)
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
	public void setFramesTextureData(List newFramesTextureData)
	{}

	@Override
	public String toString()
	{
		return "VanillaIcon [" + getIcon() + "]";
	}
}
