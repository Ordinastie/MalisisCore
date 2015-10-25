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

import net.malisis.core.MalisisCore;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Ordinastie
 *
 */
public interface IMetaIconProvider
{
	public default void setTexture(String textureName)
	{
		if (!StringUtils.isEmpty(textureName) && MalisisCore.isClient())
			createIconProvider(textureName);
	}

	public default void setTexture(Item item)
	{
		if (item != null && MalisisCore.isClient())
			createIconProvider(item);
	}

	public default void setTexture(Block block)
	{
		if (block != null && block.getDefaultState() != null && MalisisCore.isClient())
			createIconProvider(block.getDefaultState());
	}

	public default void setTexture(IBlockState blockState)
	{
		if (blockState != null && MalisisCore.isClient())
			createIconProvider(blockState);
	}

	@SideOnly(Side.CLIENT)
	public void createIconProvider(Object object);

	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider();
}
