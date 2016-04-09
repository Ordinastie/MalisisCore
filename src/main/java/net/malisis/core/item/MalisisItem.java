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

import java.util.List;

import net.malisis.core.MalisisCore;
import net.malisis.core.block.IComponent;
import net.malisis.core.block.IComponentProvider;
import net.malisis.core.block.IRegisterable;
import net.malisis.core.renderer.DefaultRenderer;
import net.malisis.core.renderer.MalisisRendered;
import net.malisis.core.renderer.icon.MalisisIcon;
import net.malisis.core.renderer.icon.provider.IIconProvider;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;

/**
 * @author Ordinastie
 *
 */
@MalisisRendered(DefaultRenderer.Item.class)
public class MalisisItem extends Item implements IComponentProvider, IRegisterable
{
	protected String name;
	protected final List<IComponent> components = Lists.newArrayList();

	public MalisisItem setName(String name)
	{
		this.name = name;
		setUnlocalizedName(name);
		return this;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public void addComponent(IComponent component)
	{
		components.add(component);
	}

	@Override
	public List<IComponent> getComponents()
	{
		return components;
	}

	public void setTexture(String textureName)
	{
		if (!StringUtils.isEmpty(textureName) && MalisisCore.isClient())
			addComponent((IIconProvider) () -> MalisisIcon.from(textureName));
	}

	public void setTexture(Item item)
	{
		if (item != null && MalisisCore.isClient())
			addComponent((IIconProvider) () -> MalisisIcon.from(item));
	}

	public void setTexture(Block block)
	{
		if (block != null)
			setTexture(block.getDefaultState());
	}

	public void setTexture(IBlockState state)
	{
		if (state != null && MalisisCore.isClient())
			addComponent((IIconProvider) () -> MalisisIcon.from(state));
	}
}
