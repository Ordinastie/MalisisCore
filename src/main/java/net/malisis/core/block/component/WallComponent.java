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

package net.malisis.core.block.component;

import java.util.List;
import java.util.Random;

import net.malisis.core.MalisisCore;
import net.malisis.core.block.BoundingBoxType;
import net.malisis.core.block.IBlockComponent;
import net.malisis.core.block.IMergedBlock;
import net.malisis.core.block.MalisisBlock;
import net.malisis.core.util.AABBUtils;
import net.malisis.core.util.EnumFacingUtils;
import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.google.common.collect.Lists;

/**
 * A WallComponent when applied to a {@link MalisisBlock} makes it behave like walls.<br>
 * Walls have a 3/16 thickness and can be made into a corner when placing a second wall into the same block.<br>
 * A {@link DirectionalComponent} is automatically added to the block.
 *
 * @author Ordinastie
 */
public class WallComponent implements IBlockComponent, IMergedBlock
{
	public static PropertyBool CORNER = PropertyBool.create("corner");

	/**
	 * Gets the property to use for this {@link WallComponent}.
	 *
	 * @return the property
	 */
	@Override
	public PropertyBool getProperty()
	{
		return CORNER;
	}

	/**
	 * Sets the default value to use for this {@link WallComponent}.
	 *
	 * @param block the block
	 * @param state the state
	 * @return the i block state
	 */
	@Override
	public IBlockState setDefaultState(Block block, IBlockState state)
	{
		return state.withProperty(getProperty(), false);
	}

	/**
	 * Gets the dependencies needed for this {@link WallComponent}.
	 *
	 * @return the dependencies
	 */
	@Override
	public List<IBlockComponent> getDependencies()
	{
		return Lists.newArrayList(new DirectionalComponent());
	}

	/**
	 * Checks whether the block can be merged into a corner.
	 *
	 * @param itemStack the item stack
	 * @param player the player
	 * @param world the world
	 * @param pos the pos
	 * @param side the side
	 * @return true, if successful
	 */
	@Override
	public boolean canMerge(ItemStack itemStack, EntityPlayer player, World world, BlockPos pos, EnumFacing side)
	{
		IBlockState state = world.getBlockState(pos);
		if (isCorner(state))
			return false;

		return EnumFacingUtils.getRealSide(state, side) != EnumFacing.NORTH;
	}

	/**
	 * Merges the {@link IBlockState} into a corner if possible.
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
	 * @return the i block state
	 */
	@Override
	public IBlockState mergeBlock(World world, BlockPos pos, IBlockState state, ItemStack itemStack, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		EnumFacing direction = DirectionalComponent.getDirection(state);
		EnumFacing realSide = EnumFacingUtils.getRealSide(state, side);

		if (!world.isRemote)
			MalisisCore.message(hitX + ", " + hitY + ", " + hitZ);

		if (realSide == EnumFacing.EAST && hitX == 1)
			return null;
		if (realSide == EnumFacing.WEST && hitX == 0)
			return null;
		if (realSide == EnumFacing.UP && hitY == 1)
			return null;
		if (realSide == EnumFacing.DOWN && hitX == 0)
			return null;

		boolean rotate = false;
		switch (direction)
		{
			case SOUTH:
				rotate = hitX < 0.5F;
				break;
			case NORTH:
				rotate = hitX >= 0.5F;
				break;
			case WEST:
				rotate = hitZ < 0.5F;
				break;
			case EAST:
				rotate = hitZ >= 0.5F;
				break;
			default:
				break;
		}

		if (rotate)
			state = DirectionalComponent.rotate(state);

		return state.withProperty(getProperty(), true);
	}

	/**
	 * Gets the {@link IBlockState} from <code>meta</code>.
	 *
	 * @param block the block
	 * @param state the state
	 * @param meta the meta
	 * @return the state from meta
	 */
	@Override
	public IBlockState getStateFromMeta(Block block, IBlockState state, int meta)
	{
		return state.withProperty(getProperty(), (meta & 8) != 0);
	}

	/**
	 * Gets the metadata from the {@link IBlockState}.
	 *
	 * @param block the block
	 * @param state the state
	 * @return the meta from state
	 */
	@Override
	public int getMetaFromState(Block block, IBlockState state)
	{
		return isCorner(state) ? 8 : 0;
	}

	/**
	 * Gets the bounding boxes for the block.
	 *
	 * @param block the block
	 * @param world the world
	 * @param pos the pos
	 * @param type the type
	 * @return the bounding boxes
	 */
	@Override
	public AxisAlignedBB[] getBoundingBoxes(Block block, IBlockAccess world, BlockPos pos, BoundingBoxType type)
	{
		boolean corner = isCorner(world, pos);
		if (type == BoundingBoxType.SELECTION && corner)
			return AABBUtils.identities();

		AxisAlignedBB aabb = new AxisAlignedBB(0, 0, 0, 1, 1, 3 / 16F);

		if (!corner)
			return new AxisAlignedBB[] { aabb };

		return new AxisAlignedBB[] { aabb, AABBUtils.rotate(aabb, -1) };
	}

	/**
	 * Checks whether a side should be rendered.
	 *
	 * @param block the block
	 * @param world the world
	 * @param pos the pos
	 * @param side the side
	 * @return the boolean
	 */
	@Override
	public Boolean shouldSideBeRendered(Block block, IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		IBlockState state = world.getBlockState(pos);
		EnumFacing realSide = EnumFacingUtils.getRealSide(state, side);
		if (realSide == EnumFacing.SOUTH || (isCorner(state) && realSide == EnumFacing.EAST))
			return true;

		return null;
	}

	@Override
	public Boolean isOpaqueCube(Block block)
	{
		return false;
	}

	@Override
	public Boolean isFullBlock(Block block)
	{
		return false;
	}

	@Override
	public Boolean isFullCube(Block block)
	{
		return false;
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
	@Override
	public Integer quantityDropped(Block block, IBlockState state, int fortune, Random random)
	{
		return isCorner(state) ? 2 : 1;
	}

	/**
	 * Gets whether the wall is a corner or not.
	 *
	 * @param world the world
	 * @param pos the pos
	 * @return the EnumFacing, null if the block is not {@link DirectionalComponent}
	 */
	public static boolean isCorner(IBlockAccess world, BlockPos pos)
	{
		return world != null && pos != null ? isCorner(world.getBlockState(pos)) : false;
	}

	/**
	 * Gets whether the wall is a corner or not.
	 *
	 * @param state the state
	 * @return the EnumFacing, null if the block is not {@link DirectionalComponent}
	 */
	public static boolean isCorner(IBlockState state)
	{
		WallComponent wc = IBlockComponent.getComponent(WallComponent.class, state.getBlock());
		if (wc == null)
			return false;

		PropertyBool property = wc.getProperty();
		if (property == null || !state.getProperties().containsKey(property))
			return false;

		return (boolean) state.getValue(property);
	}

}
