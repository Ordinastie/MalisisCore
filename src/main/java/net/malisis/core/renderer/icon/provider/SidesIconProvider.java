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

import net.malisis.core.block.IBlockDirectional;
import net.malisis.core.renderer.icon.MalisisIcon;
import net.malisis.core.util.EnumFacingUtils;
import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * This {@link IIconProvider} allows a {@link Block} to have different icons for its sides.<br>
 * By default, it will also use the {@link IBlockDirectional#DIRECTION} property to determine the facing of the block and rotate the icons
 * accordingly.
 *
 * @author Ordinastie
 *
 */
public class SidesIconProvider implements IBlockIconProvider
{
	private MalisisIcon defaultIcon;
	private MalisisIcon[] sideIcons = new MalisisIcon[6];
	private PropertyDirection property = IBlockDirectional.DIRECTION;

	public SidesIconProvider(String defaultName, String[] sideNames)
	{
		setDefaultIcon(defaultName);
		setSideIcons(sideNames);
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
			if (i < 6 && !StringUtils.isEmpty(names[i]))
				sideIcons[i] = new MalisisIcon(names[i]);
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
		this.sideIcons = icons;
	}

	public void setSideIcon(EnumFacing side, MalisisIcon icon)
	{
		sideIcons[side.getIndex()] = icon;
	}

	/**
	 * Sets the property direction to used to determine the facing of the block.<br>
	 * By default, uses {@link IBlockDirectional#DIRECTION}.
	 *
	 * @param property the new property direction
	 */
	public void setPropertyDirection(PropertyDirection property)
	{
		this.property = property;
	}

	@Override
	public void registerIcons(TextureMap map)
	{
		if (defaultIcon != null)
			defaultIcon = defaultIcon.register(map);

		for (int i = 0; i < sideIcons.length; i++)
		{
			if (sideIcons[i] != null)
				sideIcons[i] = sideIcons[i].register(map);
		}
	}

	/**
	 * Gets the {@link MalisisIcon} associated with a side.
	 *
	 * @param side the dir
	 * @return the icon
	 */
	public MalisisIcon getIcon(EnumFacing side)
	{
		if (side == null || side.getIndex() > sideIcons.length)
			return null;

		return sideIcons[side.getIndex()];
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
		if (side == null)
			return defaultIcon;

		EnumFacing direction = (EnumFacing) state.getValue(property);
		int count = EnumFacingUtils.getRotationCount(direction);
		if (side != EnumFacing.UP && side != EnumFacing.DOWN)
		{
			side = EnumFacingUtils.rotateFacing(side, count);
			count = 0;
		}

		MalisisIcon dirIcon = ObjectUtils.firstNonNull(getIcon(side), defaultIcon);
		if (dirIcon != null)
			dirIcon.setRotation(count);
		return dirIcon;
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
		MalisisIcon dirIcon = getIcon(side);
		return dirIcon != null ? dirIcon : defaultIcon;
	}

	@Override
	public MalisisIcon getIcon()
	{
		return defaultIcon;
	}
}
