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

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import net.malisis.core.block.BoundingBoxType;
import net.malisis.core.block.IBlockComponent;
import net.malisis.core.block.IComponent;
import net.malisis.core.block.ISmartCull;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * @author Ordinastie
 *
 */
public class StairComponent implements IBlockComponent, ISmartCull
{
	public static PropertyBool TOP = PropertyBool.create("top");

	@Override
	public PropertyBool getProperty()
	{
		return TOP;
	}

	@Override
	public IBlockState setDefaultState(Block block, IBlockState state)
	{
		return state.withProperty(getProperty(), false);
	}

	@Override
	public List<IComponent> getDependencies()
	{
		return Lists.newArrayList(new DirectionalComponent());
	}

	@Override
	public IBlockState getStateForPlacement(Block block, World world, BlockPos pos, IBlockState state, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand)
	{
		boolean top = facing == EnumFacing.DOWN || (facing != EnumFacing.UP && hitY > 0.5F);
		return state.withProperty(getProperty(), top);
	}

	@Override
	public AxisAlignedBB[] getBoundingBoxes(Block block, IBlockAccess world, BlockPos pos, IBlockState state, BoundingBoxType type)
	{
		return getBounds(state.getBlock() == block ? world : null, pos, state).toArray(new AxisAlignedBB[0]);
	}

	public List<AxisAlignedBB> getBounds(IBlockAccess world, BlockPos pos, IBlockState state)
	{
		List<AxisAlignedBB> list = new ArrayList<>();
		list.add(getBaseBounds(world, pos, state));
		list.addAll(getStepBounds(world, pos, state));

		return list;
	}

	private AxisAlignedBB getBaseBounds(IBlockAccess world, BlockPos pos, IBlockState state)
	{
		AxisAlignedBB aabb = new AxisAlignedBB(0, 0, 0, 1, 0.5F, 1);
		return isTop(state) ? aabb.offset(0, 0.5F, 0) : aabb;
	}

	private boolean hasSameStairs(IBlockState state1, IBlockState state2)
	{
		return isStairs(state2.getBlock()) && getStairDirection(state1) == getStairDirection(state2) && isTop(state1) == isTop(state2);
	}

	private boolean shouldConnect(IBlockState state1, IBlockState state2, boolean hasLeftStairs, boolean hasRightStairs)
	{
		if (!isStairs(state2.getBlock()))
			return false;

		if (hasLeftStairs && hasRightStairs)
			return false;

		EnumFacing dir1 = getStairDirection(state1);
		EnumFacing dir2 = getStairDirection(state2);
		if (dir1.getAxis() == dir2.getAxis() || isTop(state1) != isTop(state2))
			return false;

		if (dir1 == dir2.rotateY() && hasRightStairs)
			return false;
		if (dir1 == dir2.rotateYCCW() && hasLeftStairs)
			return false;
		return true;
	}

	private List<AxisAlignedBB> getStepBounds(IBlockAccess world, BlockPos pos, IBlockState state)
	{
		if (world == null)
			return Lists.newArrayList(new AxisAlignedBB(0F, 0.5F, 0.5F, 1, 1, 1));

		EnumFacing dir = getStairDirection(state);
		boolean isTop = isTop(state);

		//make the corner
		AxisAlignedBB aabb = new AxisAlignedBB(0, 0.5F, 0, 0.5F, 1, 0.5F);
		if (isTop)
			aabb = aabb.offset(0, -0.5F, 0);

		boolean hasLeftStairs = hasSameStairs(state, world.getBlockState(pos.offset(dir.rotateY())));
		boolean hasRightStairs = hasSameStairs(state, world.getBlockState(pos.offset(dir.rotateYCCW())));

		//check front side (little corner instead of full width):
		IBlockState stateOther = world.getBlockState(pos.offset(dir.getOpposite()));
		EnumFacing dirOther = getStairDirection(stateOther);
		if (shouldConnect(state, stateOther, hasLeftStairs, hasRightStairs))
		{
			//move the corner to the right
			if (dirOther == dir.rotateY())
				return Lists.newArrayList(aabb.offset(0.5F, 0, 0));
			else
				return Lists.newArrayList(aabb);
		}

		//extend the corner to full width and add to list
		List<AxisAlignedBB> list = Lists.newArrayList(aabb.expand(0.5F, 0, 0));

		//check back side (full width + corner) :
		stateOther = world.getBlockState(pos.offset(dir));
		dirOther = getStairDirection(stateOther);
		if (shouldConnect(state, stateOther, hasRightStairs, hasLeftStairs))
		{
			//move the corner closer
			aabb = aabb.offset(0, 0, 0.5F);
			//move the corner to the right
			if (dirOther == dir.rotateY())
				list.add(aabb.offset(0.5F, 0, 0));
			else
				list.add(aabb);
		}

		return list;
	}

	@Override
	public IBlockState getStateFromMeta(Block block, IBlockState state, int meta)
	{
		return state.withProperty(getProperty(), (meta & 8) != 0);
	}

	@Override
	public int getMetaFromState(Block block, IBlockState state)
	{
		return isTop(state) ? 8 : 0;
	}

	@Override
	public Boolean isOpaqueCube(Block block, IBlockState state)
	{
		return false;
	}

	@Override
	public Boolean isFullCube(Block block, IBlockState state)
	{
		return false;
	}

	public static boolean isStairs(Block block)
	{
		if (block instanceof BlockStairs)
			return true;

		return IComponent.getComponent(StairComponent.class, block) != null;

	}

	public static boolean isTop(IBlockAccess world, BlockPos pos)
	{
		return world != null ? isTop(world.getBlockState(pos)) : false;
	}

	public static boolean isTop(IBlockState state)
	{
		StairComponent sc = IComponent.getComponent(StairComponent.class, state.getBlock());
		if (sc == null)
			return false;

		PropertyBool property = sc.getProperty();
		if (property == null || !state.getProperties().containsKey(property))
			return false;

		return state.getValue(property);
	}

	public static EnumFacing getStairDirection(IBlockState state)
	{
		if (!isStairs(state.getBlock()))
			return null;

		if (state.getBlock() instanceof BlockStairs)
			return state.getValue(BlockStairs.FACING).getOpposite();
		else
			return DirectionalComponent.getDirection(state);
	}

}
