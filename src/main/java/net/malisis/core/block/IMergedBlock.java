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

package net.malisis.core.block;

import net.malisis.core.block.component.SlabComponent;
import net.malisis.core.block.component.WallComponent;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

/**
 * The IMergedBlock interface defines a {@link Block} or a {@link IBlockComponent} associated to a block, that can be merged with the one in
 * the player's hand.<br>
 * For example, it allows stacking slabs (see {@link SlabComponent}), or converting walls into corners (see {@link WallComponent}).
 *
 * @author Ordinastie
 */
public interface IMergedBlock
{
	/**
	 * Checks whether the current {@link IBlockState} can be merged into another one.
	 *
	 * @param itemStack the item stack
	 * @param player the player
	 * @param world the world
	 * @param pos the pos
	 * @param side the side
	 * @return true, if successful
	 */
	public boolean canMerge(ItemStack itemStack, EntityPlayer player, World world, BlockPos pos, EnumFacing side);

	/**
	 * Merges the {@link IBlockState}.
	 *
	 * @param world the world
	 * @param pos the pos
	 * @param state the state
	 * @param itemStack the item stack
	 * @param player the player
	 * @param side the side
	 * @param hitX the hit x
	 * @param hitY the hit y
	 * @param hitZ the hit z
	 * @return the new {@link IBlockState} resulting of the merge, null if no merge is possible.
	 */
	public IBlockState mergeBlock(World world, BlockPos pos, IBlockState state, ItemStack itemStack, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ);

	public default boolean mergeSelfOnly()
	{
		return true;
	}

}
