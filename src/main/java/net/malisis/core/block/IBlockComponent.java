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

import net.malisis.core.item.MalisisItemBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.collect.ImmutableList;

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
		return new MalisisItemBlock(block);
	}

	/**
	 * Gets the additional components that this {@link IBlockComponent} depends on.
	 *
	 * @return the dependencies
	 */
	public default List<IBlockComponent> getDependencies()
	{
		return ImmutableList.of();
	}

	/**
	 * Gets the unlocalized name for the specific {@link IBlockState}.
	 *
	 * @param block the block
	 * @param state the state
	 * @return the unlocalized name
	 */
	public default String getUnlocalizedName(Block block, IBlockState state)
	{
		return null;
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
	 * Called when the {@link Block} is right-clicked by the player.
	 *
	 * @param block the block
	 * @param world the world
	 * @param pos the pos
	 * @param state the state
	 * @param player the player
	 * @param side the side
	 * @param hitX the hit x
	 * @param hitY the hit y
	 * @param hitZ the hit z
	 * @return true, if successful
	 */
	public default boolean onBlockActivated(Block block, World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		return false;
	}

	/**
	 * Called when a neighboring {@link Block} changes.
	 *
	 * @param block the block
	 * @param world the world
	 * @param pos the pos
	 * @param state the state
	 * @param neighborBlock the neighbor block
	 */
	public default void onNeighborBlockChange(Block block, World world, BlockPos pos, IBlockState state, Block neighborBlock)
	{

	}

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
	 * Gets the bounding box for the {@link Block}.
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

	/**
	 * Gets the bounding boxes for the {@link Block}.
	 *
	 * @param block the block
	 * @param world the world
	 * @param pos the pos
	 * @param type the type
	 * @return the bounding boxes
	 */
	public default AxisAlignedBB[] getBoundingBoxes(Block block, IBlockAccess world, BlockPos pos, BoundingBoxType type)
	{
		return null;
	}

	/**
	 * Whether the {@link Block} can be placed on the side of another block.
	 *
	 * @param block the block
	 * @param world the world
	 * @param pos the pos
	 * @param side the side
	 * @return true, if successful
	 */
	public default boolean canPlaceBlockOnSide(Block block, World world, BlockPos pos, EnumFacing side)
	{
		return true;
	}

	/**
	 * Whether the {@link Block} can be placed at the position.
	 *
	 * @param world the world
	 * @param pos the pos
	 * @return true, if successful
	 */
	public default boolean canPlaceBlockAt(Block block, World world, BlockPos pos)
	{
		return true;
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
	 */
	@SideOnly(Side.CLIENT)
	public default void getSubBlocks(Block block, Item item, CreativeTabs tab, List list)
	{}

	//#end Sub-blocks

	//#region Colors
	/**
	 * Gets the color multiplier to render the block in the world.
	 *
	 * @param block the block
	 * @param world the world
	 * @param pos the pos
	 * @param renderPass the render pass
	 * @return the color
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
	/**
	 * Checks whether a side should be rendered.
	 *
	 * @param block the block
	 * @param world the world
	 * @param pos the pos
	 * @param side the side
	 * @return the boolean
	 */
	public default Boolean shouldSideBeRendered(Block block, IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		return null;
	}

	/**
	 * Checks whether this {@link IBlockComponent} represents a full {@link Block}.
	 *
	 * @param block the block
	 * @return the boolean
	 */
	public default Boolean isFullBlock(Block block)
	{
		return null;
	}

	/**
	 * Checks whether this {@link IBlockComponent} represents a full cube.
	 *
	 * @param block the block
	 * @return the boolean
	 */
	public default Boolean isFullCube(Block block)
	{
		return null;
	}

	/**
	 * Checks whether this {@link IBlockComponent} represents an opaque cube.
	 *
	 * @param block the block
	 * @return the boolean
	 */
	public default Boolean isOpaqueCube(Block block)
	{
		return null;
	}

	//#end Fullness

	//#region Other
	/**
	 * Gets the mixed brightness for the {@link Block}.
	 *
	 * @param block the block
	 * @param world the world
	 * @param pos the pos
	 * @return the mixed brightness for block
	 */
	public default Integer getMixedBrightnessForBlock(Block block, IBlockAccess world, BlockPos pos)
	{
		return null;
	}

	/**
	 * Gets the item dropped by the {@link Block} when broken.
	 *
	 * @param state the state
	 * @param rand the rand
	 * @param fortune the fortune
	 * @return the item dropped
	 */
	public default Item getItemDropped(Block block, IBlockState state, Random rand, int fortune)
	{
		return null;
	}

	/**
	 * Quantity the quantity dropped by the {@link Block} when broken.
	 *
	 * @param block the block
	 * @param state the state
	 * @param fortune the fortune
	 * @param random the random
	 * @return the integer
	 */
	public default Integer quantityDropped(Block block, IBlockState state, int fortune, Random random)
	{
		return null;
	}

	/**
	 * Gets the light opacity for the {@link Block}.
	 *
	 * @param block the block
	 * @param world the world
	 * @param pos the pos
	 * @return the light opacity
	 */
	public default Integer getLightOpacity(Block block, IBlockAccess world, BlockPos pos)
	{
		return null;
	}

	//#end Other

	/**
	 * Gets the component of the specify <code>type</code> for the {@link Block}.<br>
	 * The returned object may <b>not</b> be a component but the block itself if it implements an interface used for a
	 * {@link IBlockComponent}.
	 *
	 * @param <T> the generic type
	 * @param type the type
	 * @param block the block
	 * @return the component
	 */
	public static <T> T getComponent(Class<T> type, Block block)
	{
		if (block.getClass().isAssignableFrom(type))
			return (T) block;

		if (!(block instanceof IComponentProvider))
			return null;

		return ((IComponentProvider) block).getComponent(type);
	}

	public static IProperty getProperty(Class<?> type, Block block)
	{
		Object component = getComponent(type, block);
		if (component instanceof IBlockComponent)
			return ((IBlockComponent) component).getProperty();
		return null;
	}
}
