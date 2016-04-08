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

import net.malisis.core.block.component.DirectionalComponent;
import net.malisis.core.renderer.icon.MalisisIcon;
import net.malisis.core.renderer.icon.provider.IBlockIconProvider.ISidesIconProvider;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * This {@link IIconProvider} allows a {@link Block} to have different icons for its sides.<br>
 * By default, it will also use the {@link DirectionalComponent#HORIZONTAL} property to determine the facing of the block and rotate the
 * icons accordingly.
 *
 * @author Ordinastie
 *
 */
public class SidesIconProvider implements ISidesIconProvider
{
	private MalisisIcon defaultIcon;
	private MalisisIcon[] sideIcons = new MalisisIcon[6];

	public SidesIconProvider(String defaultName, String[] sideNames)
	{
		setDefaultIcon(defaultName);
		setSideIcons(sideNames);
	}

	public SidesIconProvider(String defaultName)
	{
		setDefaultIcon(defaultName);
	}

	public SidesIconProvider(MalisisIcon defaultIcon, MalisisIcon[] sideIcons)
	{
		setDefaultIcon(defaultIcon);
		setSideIcons(sideIcons);
	}

	public SidesIconProvider(MalisisIcon defaultIcon)
	{
		setDefaultIcon(defaultIcon);
	}

	/**
	 * Sets the default {@link MalisisIcon} to use if no icon is set for a face.
	 *
	 * @param name the new default icon
	 */
	public void setDefaultIcon(String name)
	{
		defaultIcon = new MalisisIcon(name);
	}

	/**
	 * Sets the default {@link MalisisIcon} to use if no icon is set for a side of the block.
	 *
	 * @param icon the new default icon
	 */
	public void setDefaultIcon(MalisisIcon icon)
	{
		this.defaultIcon = icon;
	}

	/**
	 * Sets the {@link MalisisIcon} to use for each side of the block.<br>
	 * The index of the array is the {@link EnumFacing#getIndex()} value.<br>
	 * If no icon is set for a side, {@link #defaultIcon} will be used instead.
	 *
	 * @param names the new side icons
	 */
	public void setSideIcons(String[] names)
	{
		for (int i = 0; i < names.length; i++)
		{
			if (!StringUtils.isEmpty(names[i]))
				setSideIcon(EnumFacing.getFront(i), new MalisisIcon(names[i]));
		}
	}

	/**
	 * Sets the {@link MalisisIcon} to use for each side of the block.<br>
	 * The index of the array is the {@link EnumFacing#getIndex()} value.<br>
	 * If no icon is set for a side, {@link #defaultIcon} will be used instead.
	 *
	 * @param icons the new side icons
	 */
	public void setSideIcons(MalisisIcon[] icons)
	{
		for (int i = 0; i < icons.length; i++)
		{
			if (icons[i] != null)
				setSideIcon(EnumFacing.getFront(i), icons[i]);
		}
	}

	public void setSideIcon(EnumFacing side, MalisisIcon icon)
	{
		//set default icon too to get at least an icon for particles
		if (defaultIcon == null)
			defaultIcon = icon;
		sideIcons[side.getIndex()] = icon;
	}

	public void setSideIcon(EnumFacing side, String name)
	{
		setSideIcon(side, new MalisisIcon(name));
	}

	/**
	 * Gets the {@link MalisisIcon} associated with a side.
	 *
	 * @param side the dir
	 * @return the icon
	 */
	@Override
	public MalisisIcon getIcon(EnumFacing side)
	{
		if (side == null || side.getIndex() > sideIcons.length)
			return defaultIcon;

		return ObjectUtils.firstNonNull(sideIcons[side.getIndex()], defaultIcon);
	}

	/**
	 * Gets the {@link MalisisIcon} for the side for the block in world.<br>
	 * Takes in account the facing of the block.<br>
	 * If no icon was set for the side, {@link #defaultIcon} is used.
	 *
	 * @param world the world
	 * @param pos the pos
	 * @param state the state
	 * @param side the side
	 * @return the icon
	 */
	@Override
	public MalisisIcon getIcon(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side)
	{
		return getIcon(side);
	}

	/**
	 * Gets the {@link MalisisIcon} for the side for the block in inventory.<br>
	 * If no icon was set for the side, {@link #defaultIcon} is used.
	 *
	 * @param itemStack the item stack
	 * @param side the facing
	 * @return the icon
	 */
	@Override
	public MalisisIcon getIcon(ItemStack itemStack, EnumFacing side)
	{
		return getIcon(side);
	}

	@Override
	public MalisisIcon getIcon()
	{
		return defaultIcon;
	}
}
