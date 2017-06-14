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

import com.google.common.collect.Lists;

import net.malisis.core.MalisisCore;
import net.malisis.core.block.BoundingBoxType;
import net.malisis.core.block.IBlockComponent;
import net.malisis.core.block.IComponent;
import net.malisis.core.renderer.component.SlopeShapeComponent;
import net.malisis.core.util.AABBUtils;
import net.minecraft.block.Block;
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
public class SlopeComponent implements IBlockComponent
{
	public static PropertyBool DOWN = PropertyBool.create("down");

	public SlopeComponent()
	{

	}

	@Override
	public PropertyBool getProperty()
	{
		return DOWN;
	}

	@Override
	public IBlockState setDefaultState(Block block, IBlockState state)
	{
		return state.withProperty(getProperty(), false);
	}

	@Override
	public List<IComponent> getDependencies()
	{
		List<IComponent> deps = Lists.newArrayList(new DirectionalComponent());

		if (MalisisCore.isClient())
			deps.add(new SlopeShapeComponent());

		return deps;
	}

	@Override
	public IBlockState getStateForPlacement(Block block, World world, BlockPos pos, IBlockState state, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand)
	{
		boolean down = facing == EnumFacing.DOWN || (facing != EnumFacing.UP && hitY > 0.5F);
		return state.withProperty(getProperty(), down);
	}

	@Override
	public AxisAlignedBB[] getBoundingBoxes(Block block, IBlockAccess world, BlockPos pos, IBlockState state, BoundingBoxType type)
	{
		boolean down = isDown(state);
		float[][] fx = { { 0, 1 }, { 0, 1 } };
		float[][] fy = { { 0, 1 }, { 0, 1 } };
		float[][] fz = { { 0, 1 }, { 0, 0 } };
		if (down)
			fz = new float[][] { { 0, 1 / 8F }, { 0, 9 / 8F } };

		return AABBUtils.slice(8, fx, fy, fz, true);
	}

	@Override
	public int getMetaFromState(Block block, IBlockState state)
	{
		return isDown(state) ? 8 : 0;
	}

	@Override
	public IBlockState getStateFromMeta(Block block, IBlockState state, int meta)
	{
		return state.withProperty(getProperty(), (meta & 8) != 0);
	}

	@Override
	public Boolean isOpaqueCube(Block block, IBlockState state)
	{
		return false;
	}

	@Override
	public Boolean isFullBlock(Block block, IBlockState state)
	{
		return false;
	}

	@Override
	public Boolean isFullCube(Block block, IBlockState state)
	{
		return false;
	}

	public static boolean isDown(IBlockAccess world, BlockPos pos)
	{
		return world != null && pos != null ? isDown(world.getBlockState(pos)) : false;
	}

	public static boolean isDown(IBlockState state)
	{
		SlopeComponent sc = IComponent.getComponent(SlopeComponent.class, state.getBlock());
		if (sc == null)
			return false;

		PropertyBool property = sc.getProperty();
		if (property == null || !state.getProperties().containsKey(property))
			return false;

		return state.getValue(property);
	}

}
