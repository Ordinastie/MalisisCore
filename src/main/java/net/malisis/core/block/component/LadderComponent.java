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

import net.malisis.core.block.BoundingBoxType;
import net.malisis.core.block.IBlockComponent;
import net.malisis.core.block.IComponent;
import net.malisis.core.block.component.DirectionalComponent.Placement;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.google.common.collect.Lists;

/**
 * @author Ordinastie
 *
 */
public class LadderComponent implements IBlockComponent
{
	@Override
	public IProperty<?> getProperty()
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
		return Lists.newArrayList(new DirectionalComponent(Placement.BLOCKSIDE));
	}

	protected boolean canBlockStay(World world, BlockPos pos, EnumFacing side)
	{
		return world.isSideSolid(pos.offset(side.getOpposite()), side, true);
	}

	@Override
	public void onNeighborBlockChange(Block block, World world, BlockPos pos, IBlockState state, Block neighborBlock)
	{
		EnumFacing dir = DirectionalComponent.getDirection(world, pos);

		if (!canBlockStay(world, pos, dir))
		{
			block.dropBlockAsItem(world, pos, state, 0);
			world.setBlockToAir(pos);
		}
	}

	@Override
	public AxisAlignedBB getBoundingBox(Block block, IBlockAccess world, BlockPos pos, IBlockState state, BoundingBoxType type)
	{
		return new AxisAlignedBB(0, 0, 0, 1, 1, 0.125F);
	}

	@Override
	public boolean canPlaceBlockOnSide(Block block, World world, BlockPos pos, EnumFacing side)
	{
		if (side == EnumFacing.UP || side == EnumFacing.DOWN)
			return false;
		return world.isSideSolid(pos.offset(side.getOpposite()), side, true);
	}

	@Override
	public boolean canPlaceBlockAt(Block block, World world, BlockPos pos)
	{
		return world.isSideSolid(pos.west(), EnumFacing.EAST, true) || world.isSideSolid(pos.east(), EnumFacing.WEST, true)
				|| world.isSideSolid(pos.north(), EnumFacing.SOUTH, true) || world.isSideSolid(pos.south(), EnumFacing.NORTH, true);
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
}
