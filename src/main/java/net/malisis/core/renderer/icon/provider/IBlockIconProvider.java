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

import net.malisis.core.renderer.icon.IIconProvider;
import net.malisis.core.renderer.icon.MalisisIcon;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;

/**
 * This interface allows implementers to provide {@link MalisisIcon icons} when rendering Blocks.
 *
 * @author Ordinastie
 */
public interface IBlockIconProvider extends IIconProvider
{

	/**
	 * Gets the {@link MalisisIcon} to use.
	 *
	 * @param world the world
	 * @param pos the pos
	 * @param state the state
	 * @param side the side
	 * @return the icon
	 */
	public default MalisisIcon getIcon(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side)
	{
		return getIcon();
	}

	/**
	 * Gets the {@link MalisisIcon} to use for the item. (Only used if the item associated with the block isn't already a
	 * {@link IItemIconProvider}).
	 *
	 * @param itemStack the item stack
	 * @param side the side
	 * @return the icon
	 */
	public default MalisisIcon getIcon(ItemStack itemStack, EnumFacing side)
	{
		return getIcon();
	}

	/**
	 * Gets the particle {@link MalisisIcon} to use for the {@link IBlockState}.
	 *
	 * @param state the state
	 * @return the particle icon
	 */
	public default MalisisIcon getParticleIcon(IBlockState state)
	{
		return getIcon();
	}

}
