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

package net.malisis.core.renderer.icon.provider;

import net.malisis.core.renderer.icon.MalisisIcon;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.Item;

/**
 * @author Ordinastie
 *
 */
public class DefaultIconProvider implements IBlockIconProvider, IItemIconProvider
{
	protected MalisisIcon icon;
	protected Item item;
	protected IBlockState blockState;
	protected int metadata;

	public DefaultIconProvider(String name)
	{
		setIcon(name);
	}

	public DefaultIconProvider(Block block)
	{
		this.blockState = block.getDefaultState();
	}

	public DefaultIconProvider(IBlockState blockState)
	{
		this.blockState = blockState;
	}

	public DefaultIconProvider(Item item, int metadata)
	{
		this.item = item;
	}

	public DefaultIconProvider(MalisisIcon icon)
	{
		setIcon(icon);
	}

	public void setIcon(String name)
	{
		icon = MalisisIcon.get(name);
	}

	public void setIcon(Item item)
	{
		setIcon(item, 0);
	}

	public void setIcon(Item item, int metadata)
	{
		this.item = item;
		this.metadata = metadata;
		icon = MalisisIcon.get(item, metadata);
	}

	public void setIcon(Block block)
	{
		setIcon(block.getDefaultState());
	}

	public void setIcon(IBlockState blockState)
	{
		this.blockState = blockState;
		icon = MalisisIcon.get(blockState);
	}

	public void setIcon(MalisisIcon icon)
	{
		this.icon = icon;
	}

	@Override
	public void registerIcons(TextureMap map)
	{
		if (item == null && blockState == null)
			icon.register(map);
	}

	@Override
	public MalisisIcon getIcon()
	{
		if (icon == null && (item != null || blockState != null))
			icon = item != null ? MalisisIcon.get(item, metadata) : MalisisIcon.get(blockState);
		return icon;
	}
}
