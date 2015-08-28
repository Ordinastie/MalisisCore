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
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Ordinastie
 *
 */
public class SidesIconProvider implements IBlockIconProvider
{
	private MalisisIcon defaultIcon;
	private MalisisIcon[] sideIcons = new MalisisIcon[6];
	private PropertyDirection property;

	public SidesIconProvider(String defaultName, String[] sideNames)
	{
		setDefaultIcon(defaultName);
		setSideIcons(sideNames);
	}

	public SidesIconProvider(MalisisIcon defaultIcon, MalisisIcon[] sideIcons)
	{

	}

	public void setDefaultIcon(String name)
	{
		defaultIcon = MalisisIcon.get(name);
	}

	public void setDefaultIcon(MalisisIcon icon)
	{
		this.defaultIcon = icon;
	}

	public void setSideIcons(String[] names)
	{
		for (int i = 0; i < names.length; i++)
		{
			if (i < 6 && !StringUtils.isEmpty(names[i]))
				sideIcons[i] = new MalisisIcon(names[i]);
		}
	}

	public void setSideIcons(MalisisIcon[] icons)
	{
		this.sideIcons = icons;
	}

	@Override
	public void registerIcons(TextureMap map)
	{
		if (defaultIcon != null)
			defaultIcon.register(map);

		for (MalisisIcon icon : sideIcons)
		{
			if (icon != null)
				icon.register(map);
		}
	}

	public void setPropertyDirection(PropertyDirection property)
	{
		this.property = property;
	}

	public MalisisIcon getIcon(EnumFacing dir)
	{
		if (dir == null || dir.getIndex() > sideIcons.length)
			return null;

		return sideIcons[dir.getIndex()];
	}

	@Override
	public MalisisIcon getIcon(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing facing)
	{
		if (facing == null)
			return defaultIcon;

		EnumFacing direction = (EnumFacing) state.getValue(property);
		int count = getRotationCount(direction);
		if (facing != EnumFacing.UP && facing != EnumFacing.DOWN)
		{
			facing = rotateFacing(facing, count);
			count = 0;
		}

		MalisisIcon dirIcon = getIcon(facing);
		dirIcon.setRotation(count);
		return dirIcon != null ? dirIcon : defaultIcon;
	}

	@Override
	public MalisisIcon getIcon(ItemStack itemStack, EnumFacing facing)
	{
		return getIcon(facing);
	}

	@Override
	public MalisisIcon getIcon()
	{
		return defaultIcon;
	}

	private int getRotationCount(EnumFacing facing)
	{
		switch (facing)
		{
			case SOUTH:
				return 0;
			case EAST:
				return 1;
			case NORTH:
				return 2;
			case WEST:
				return 3;
			default:
				return 0;
		}
	}

	private EnumFacing rotateFacing(EnumFacing facing, int count)
	{
		while (count-- > 0)
			facing = facing.rotateAround(EnumFacing.Axis.Y);
		return facing;
	}
}
