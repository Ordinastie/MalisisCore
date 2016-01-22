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

import java.util.EnumMap;
import java.util.Map.Entry;

import net.malisis.core.block.MalisisBlock;
import net.malisis.core.renderer.icon.MalisisIcon;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;

/**
 * @author Ordinastie
 *
 */
public class PropertyEnumIconProvider<T extends Enum<T>> implements IBlockIconProvider
{
	private PropertyEnum property;
	private MalisisIcon defaultIcon;
	private EnumMap<T, MalisisIcon> icons;

	public PropertyEnumIconProvider(PropertyEnum property, Class<T> enumClass, String defaultName)
	{
		this.property = property;
		this.icons = new EnumMap<>(enumClass);
		this.defaultIcon = new MalisisIcon(defaultName);
	}

	public PropertyEnumIconProvider(PropertyEnum property, Class<T> enumClass, MalisisIcon defaultIcon)
	{
		this.property = property;
		this.icons = new EnumMap<>(enumClass);
		this.defaultIcon = defaultIcon;
	}

	public void setIcon(T enumValue, MalisisIcon icon)
	{
		icons.put(enumValue, icon);
	}

	public void setIcon(T enumValue, String iconName)
	{
		icons.put(enumValue, new MalisisIcon(iconName));
	}

	@Override
	public void registerIcons(TextureMap map)
	{
		if (defaultIcon != null)
			defaultIcon = defaultIcon.register(map);

		for (Entry<T, MalisisIcon> entry : icons.entrySet())
			entry.setValue(entry.getValue().register(map));
	}

	@Override
	public MalisisIcon getIcon()
	{
		return defaultIcon;
	}

	public MalisisIcon getIcon(T value)
	{
		MalisisIcon icon = icons.get(value);
		return icon != null ? icon : getIcon();
	}

	@Override
	public MalisisIcon getIcon(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing facing)
	{
		return getIcon((T) state.getValue(property));
	}

	@Override
	public MalisisIcon getIcon(ItemStack itemStack, EnumFacing facing)
	{
		if (!(itemStack.getItem() instanceof ItemBlock))
			return getIcon();
		ItemBlock ib = (ItemBlock) itemStack.getItem();
		if (!(ib.getBlock() instanceof MalisisBlock))
			return getIcon();

		IBlockState state = ((MalisisBlock) ib.getBlock()).getStateFromItemStack(itemStack);
		return getIcon((T) state.getValue(property));
	}
}
