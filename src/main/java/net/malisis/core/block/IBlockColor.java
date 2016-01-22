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

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author Ordinastie
 *
 */
public interface IBlockColor
{
	public default PropertyEnum getPropertyColor()
	{
		return BlockColored.COLOR;
	}

	public default int damageDropped(Block block, IBlockState state)
	{
		return ((EnumDyeColor) state.getValue(getPropertyColor())).getMetadata();
	}

	@SideOnly(Side.CLIENT)
	public default void getSubBlocks(Block block, Item item, CreativeTabs tab, List list)
	{
		for (EnumDyeColor color : EnumDyeColor.values())
			list.add(new ItemStack(item, color.getMetadata()));
	}

	/**
	 * Gets the render color for this {@link Block}.
	 *
	 * @param block the block
	 * @param state the state
	 * @return the render color
	 */
	public default int getRenderColor(Block block, IBlockState state)
	{
		return ItemDye.dyeColors[getColor(state).getDyeDamage()];
	}

	/**
	 * Get the {@link MapColor} for this {@link Block} and the given {@link IBlockState}.
	 *
	 * @param block the block
	 * @param state the state
	 * @return the map color
	 */
	public default MapColor getMapColor(Block block, IBlockState state)
	{
		return ((EnumDyeColor) state.getValue(getPropertyColor())).getMapColor();
	}

	/**
	 * Get the {@link IBlockState} from the metadata
	 *
	 * @param block the block
	 * @param state the state
	 * @param meta the meta
	 * @return the state from meta
	 */
	public default IBlockState getStateFromMeta(Block block, IBlockState state, int meta)
	{
		return state.withProperty(getPropertyColor(), EnumDyeColor.byMetadata(meta));
	}

	/**
	 * Get the metadata from the {@link IBlockState}
	 *
	 * @param block the block
	 * @param state the state
	 * @return the meta from state
	 */
	public default int getMetaFromState(Block block, IBlockState state)
	{
		return ((EnumDyeColor) state.getValue(getPropertyColor())).getMetadata();
	}

	/**
	 * Gets the {@link EnumDyeColor color} for the {@link Block} at world coords.
	 *
	 * @param world the world
	 * @param pos the pos
	 * @return the EnumDyeColor, null if the block is not {@link IBlockColor}
	 */
	public static EnumDyeColor getColor(World world, BlockPos pos)
	{
		return world != null && pos != null ? getColor(world.getBlockState(pos)) : EnumDyeColor.WHITE;
	}

	/**
	 * Gets the {@link EnumDyeColor color} for the {@link IBlockState}.
	 *
	 * @param state the state
	 * @return the EnumDyeColor, null if the block is not {@link IBlockColor}
	 */
	public static EnumDyeColor getColor(IBlockState state)
	{
		if (!(state.getBlock() instanceof IBlockColor))
			return EnumDyeColor.WHITE;

		PropertyEnum property = ((IBlockColor) state.getBlock()).getPropertyColor();
		if (property == null || !state.getProperties().containsKey(property))
			return EnumDyeColor.WHITE;

		return (EnumDyeColor) state.getValue(property);

	}
}
