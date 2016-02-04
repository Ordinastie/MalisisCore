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
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author Ordinastie
 *
 */
public interface IBlockComponent
{

	/**
	 * Gets the {@link IProperty} used by this {@link IBlockComponent}.<br>
	 * Only use this if the component only has one property.
	 *
	 * @return the property
	 */
	public IProperty getProperty();

	/**
	 * Gets the all the {@link IProperty properties} used by this {@link IBlockComponent}.
	 *
	 * @return the properties
	 */
	public default IProperty[] getProperties()
	{
		if (getProperty() == null)
			return new IProperty[0];
		return new IProperty[] { getProperty() };
	}

	/**
	 * Sets the default values for the {@link IBlockState}.
	 *
	 * @param block the block
	 * @param state the state
	 */
	public IBlockState setDefaultState(Block block, IBlockState state);

	public default Item getItem(Block block)
	{
		return new ItemBlock(block);
	}

	//#region Events
	/**
	 * Called when the {@link Block} is placed in the {@link World}.
	 *
	 * @param block the block
	 * @param world the world
	 * @param pos the pos
	 * @param state the state
	 * @param facing the facing
	 * @param hitX the hit x
	 * @param hitY the hit y
	 * @param hitZ the hit z
	 * @param meta the meta
	 * @param placer the placer
	 * @return the i block state
	 */
	public default IBlockState onBlockPlaced(Block block, World world, BlockPos pos, IBlockState state, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
	{
		return state;
	}

	/**
	 * Called when the {@link Block} is placed by an {@link EntityLivingBase} in the {@link World}
	 *
	 * @param block the block
	 * @param world the world
	 * @param pos the pos
	 * @param state the state
	 * @param placer the placer
	 * @param stack the stack
	 */
	public default void onBlockPlacedBy(Block block, World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
	{}

	/**
	 * Called when the {@link Block} is broken.
	 *
	 * @param block the block
	 * @param world the world
	 * @param pos the pos
	 * @param state the state
	 */
	public default void breakBlock(Block block, World world, BlockPos pos, IBlockState state)
	{}

	//#end Events

	/**
	 * Gets the bounding box for the Block.
	 *
	 * @param world the world
	 * @param pos the pos
	 * @param type the type
	 * @return the bounding box
	 */
	public default AxisAlignedBB getBoundingBox(Block block, IBlockAccess world, BlockPos pos, BoundingBoxType type)
	{
		return null;
	}

	//#region Sub-Blocks
	/**
	 * Gets the damage value for the item when the {@link Block} is dropped.
	 *
	 * @param block the block
	 * @param state the state
	 * @return the int
	 */
	public default int damageDropped(Block block, IBlockState state)
	{
		return 0;
	}

	/**
	 * Fills the list with the sub-blocks associated with this {@link Block}.
	 *
	 * @param block the block
	 * @param item the item
	 * @param tab the tab
	 * @param list the list
	 * @return the sub blocks
	 */
	@SideOnly(Side.CLIENT)
	public default void getSubBlocks(Block block, Item item, CreativeTabs tab, List list)
	{}

	//#end Sub-blocks

	//#region Colors
	/**
	 * Gets the color multiplier to render the block in the world
	 *
	 * @param block the block
	 * @param world the world
	 * @param state the state
	 * @param renderPass the render pass
	 */
	public default int colorMultiplier(Block block, IBlockAccess world, BlockPos pos, int renderPass)
	{
		return 0xFFFFFF;
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
		return 0xFFFFFF;
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
		return null;
	}

	//#end Colors

	//#region State<->Meta
	/**
	 * Gets the {@link IBlockState} from <code>meta</code>.
	 *
	 * @param block the block
	 * @param meta the meta
	 * @return the state from meta
	 */
	public default IBlockState getStateFromMeta(Block block, IBlockState state, int meta)
	{
		return state;
	}

	/**
	 * Gets the metadata from the {@link IBlockState}.
	 *
	 * @param block the block
	 * @param state the state
	 * @return the meta from state
	 */
	public default int getMetaFromState(Block block, IBlockState state)
	{
		return 0;
	}

	//#end State<->Meta

	//#region Fullness
	public default Boolean shouldSideBeRendered(Block block, IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		return null;
	}

	public default Boolean isFullBlock(Block block)
	{
		return null;
	}

	public default Boolean isFullCube(Block block)
	{
		return null;
	}

	public default Boolean isOpaqueCube(Block block)
	{
		return null;
	}

	//#end Fullness

	//#region Other
	public default Integer getMixedBrightnessForBlock(Block block, IBlockAccess world, BlockPos pos)
	{
		return null;
	}

	public default Integer quantityDropped(Block block, IBlockState state, int fortune, Random random)
	{
		return null;
	}

	public default Integer getLightOpacity(Block block, IBlockAccess world, BlockPos pos)
	{
		return null;
	}

	//#end Other

	public static <T extends IBlockComponent> T getComponent(Class<T> type, Block block)
	{
		if (!(block instanceof IComponentProvider))
			return null;

		return ((IComponentProvider) block).getComponent(type);
	}
}
