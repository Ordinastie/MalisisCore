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
import net.malisis.core.renderer.icon.VanillaIcon;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.Item;

/**
 * @author Ordinastie
 *
 */
public class DefaultIconProvider implements IBlockIconProvider
{
	protected MalisisIcon icon;

	public DefaultIconProvider(MalisisIcon icon)
	{
		this.icon = icon;
	}

	@Override
	public void registerIcons(TextureMap map)
	{
		icon = icon.register(map);
	}

	@Override
	public MalisisIcon getIcon()
	{
		return icon;
	}

	public static DefaultIconProvider from(Object object)
	{
		if (object == null)
			return null;

		if (object instanceof String)
			return (String) object != "" ? new DefaultIconProvider(new MalisisIcon((String) object)) : null;

		if (object instanceof Item)
			return new DefaultIconProvider(new VanillaIcon((Item) object));

		if (object instanceof Block)
			return new DefaultIconProvider(new VanillaIcon((Block) object));

		if (object instanceof IBlockState)
			return new DefaultIconProvider(new VanillaIcon((IBlockState) object));

		throw new IllegalArgumentException("Parameter has to be a String, a Block, an IBlockState or an Item");
	}
}
