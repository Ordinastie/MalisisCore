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

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.Item;

/**
 * @author Ordinastie
 *
 */
public class VanillaIcon extends ProxyIcon
{
	protected Item item;
	protected IBlockState blockState;
	protected int metadata;
	protected String name;

	public VanillaIcon(String name)
	{
		this.name = name;
	}

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

	@Override
	public void register(TextureMap map)
	{
		TextureAtlasSprite icon = null;
		if (name != null)
			icon = getNameIcon(map);
		else if (item != null)
			icon = getItemIcon();
		else if (blockState != null)
			icon = getBlockIcon();
		if (icon != null)
			setProxy(icon);
	}

	private TextureAtlasSprite getNameIcon(TextureMap map)
	{
		TextureAtlasSprite icon = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(name);
		if (icon == map.getMissingSprite()) //the models using it were overwritten by a resourcepack and don't use it anymore, so we have to register a new icon.
		{
			icon = new Icon(name);
			map.setTextureEntry(icon);
		}

		return icon;
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

}
