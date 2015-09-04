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

package net.malisis.core.item;

import net.malisis.core.MalisisCore;
import net.malisis.core.MalisisRegistry;
import net.malisis.core.renderer.DefaultRenderer;
import net.malisis.core.renderer.icon.metaprovider.IItemMetaIconProvider;
import net.malisis.core.renderer.icon.provider.DefaultIconProvider;
import net.malisis.core.renderer.icon.provider.IItemIconProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Ordinastie
 *
 */
public class MalisisItem extends Item implements IItemMetaIconProvider
{
	protected String name;
	protected IItemIconProvider iconProvider;

	public Item setName(String name)
	{
		this.name = name;
		super.setUnlocalizedName(name);
		return this;
	}

	@Override
	public Item setUnlocalizedName(String name)
	{
		this.name = name;
		super.setUnlocalizedName(name);
		return this;
	}

	public String getName()
	{
		return name;
	}

	public void setTextureName(String textureName)
	{
		if (StringUtils.isEmpty(textureName))
			return;

		if (MalisisCore.isClient())
			setItemIconProvider(new DefaultIconProvider(textureName));
	}

	@SideOnly(Side.CLIENT)
	public void setItemIconProvider(IItemIconProvider iconProvider)
	{
		this.iconProvider = iconProvider;
	}

	@Override
	public IItemIconProvider getItemIconProvider()
	{
		return iconProvider;
	}

	public void register()
	{
		register(ItemBlock.class);
	}

	public void register(Class<? extends ItemBlock> item)
	{
		GameRegistry.registerItem(this, getName());
		if (FMLCommonHandler.instance().getSide() == Side.CLIENT)
		{
			MalisisRegistry.registerIconProvider(iconProvider);
			if (useDefaultRenderer())
				DefaultRenderer.item.registerFor(this);
		}
	}

	public boolean useDefaultRenderer()
	{
		return true;
	}
}
