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

import net.malisis.core.MalisisCore;
import net.malisis.core.block.BoundingBoxType;
import net.malisis.core.block.IBlockComponent;
import net.malisis.core.block.IComponent;
import net.malisis.core.renderer.component.CornerShapeComponent;
import net.malisis.core.util.AABBUtils;
import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import com.google.common.collect.Lists;

/**
 * @author Ordinastie
 *
 */
public class CornerComponent implements IBlockComponent
{
	public CornerComponent()
	{

	}

	@Override
	public PropertyBool getProperty()
	{
		return null;
	}

	@Override
	public IBlockState setDefaultState(Block block, IBlockState state)
	{
		return state;
	}

	@Override
	public List<IComponent> getDependencies()
	{
		List<IComponent> deps = Lists.newArrayList(new DirectionalComponent());

		if (MalisisCore.isClient())
			deps.add(new CornerShapeComponent());

		return deps;
	}

	@Override
	public AxisAlignedBB[] getBoundingBoxes(Block block, IBlockAccess world, BlockPos pos, IBlockState state, BoundingBoxType type)
	{
		float[][] fx = { { 0, 1 }, { 0, 1 } };
		float[][] fy = { { 0, 1 }, { 0, 1 } };
		float[][] fz = { { 0, 1 }, { 0, 0 } };

		return AABBUtils.slice(8, fx, fy, fz, false);
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
}
