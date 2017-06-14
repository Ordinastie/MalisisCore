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

import net.malisis.core.block.BoundingBoxType;
import net.malisis.core.block.IBlockComponent;
import net.malisis.core.block.ISmartCull;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPane;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * @author Ordinastie
 *
 */
public class PaneComponent implements IBlockComponent, ISmartCull
{
	public static final PropertyBool NORTH = BlockPane.NORTH;
	public static final PropertyBool EAST = BlockPane.EAST;
	public static final PropertyBool SOUTH = BlockPane.SOUTH;
	public static final PropertyBool WEST = BlockPane.WEST;

	@Override
	public IProperty<?> getProperty()
	{
		return null;
	}

	@Override
	public IProperty<?>[] getProperties()
	{
		return new IProperty[] { NORTH, EAST, SOUTH, WEST };
	}

	@Override
	public IBlockState setDefaultState(Block block, IBlockState state)
	{
		return state.withProperty(NORTH, false).withProperty(EAST, false).withProperty(SOUTH, false).withProperty(WEST, false);
	}

	public IBlockState getFullState(Block block, IBlockAccess world, BlockPos pos)
	{
		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() != block)
			return block.getDefaultState();
		return state.withProperty(NORTH, canPaneConnectTo(block, world, pos, EnumFacing.NORTH))
					.withProperty(SOUTH, canPaneConnectTo(block, world, pos, EnumFacing.SOUTH))
					.withProperty(WEST, canPaneConnectTo(block, world, pos, EnumFacing.WEST))
					.withProperty(EAST, canPaneConnectTo(block, world, pos, EnumFacing.EAST));
	}

	@Override
	public AxisAlignedBB[] getBoundingBoxes(Block block, IBlockAccess world, BlockPos pos, IBlockState state, BoundingBoxType type)
	{
		float f = 0.4375F;
		AxisAlignedBB base = new AxisAlignedBB(f, 0, f, 1 - f, 1, 1 - f);
		state = world != null ? getFullState(block, world, pos) : block.getDefaultState();
		boolean north = state.getValue(NORTH);
		boolean south = state.getValue(SOUTH);
		boolean east = state.getValue(EAST);
		boolean west = state.getValue(WEST);

		if (world == null)
		{
			north = true;
			south = true;
		}

		if (!north && !south && !east && !west)
			return new AxisAlignedBB[] { base };

		List<AxisAlignedBB> list = Lists.newArrayList();
		if (north || south)
			list.add(base.expand(0, 0, north ? -f : 0).expand(0, 0, south ? f : 0));
		if (east || west)
			list.add(base.expand(west ? -f : 0, 0, 0).expand(east ? f : 0, 0, 0));

		return list.toArray(new AxisAlignedBB[0]);
	}

	@Override
	public Boolean isFullCube(Block block, IBlockState state)
	{
		return false;
	}

	@Override
	public Boolean isOpaqueCube(Block block, IBlockState state)
	{
		return false;
	}

	public final boolean canPaneConnectToBlock(Block block, IBlockState state)
	{
		return state.isFullBlock() || block == Blocks.GLASS || block == Blocks.STAINED_GLASS || block == Blocks.STAINED_GLASS_PANE
				|| block instanceof BlockPane;
	}

	public boolean canPaneConnectTo(Block block, IBlockAccess world, BlockPos pos, EnumFacing dir)
	{
		BlockPos offset = pos.offset(dir);
		IBlockState state = world.getBlockState(offset);
		Block connected = state.getBlock();
		BlockFaceShape shape = state.getBlockFaceShape(world, pos, dir);
		return connected == block || canPaneConnectToBlock(state.getBlock(), state) || shape == BlockFaceShape.SOLID
				|| shape == BlockFaceShape.MIDDLE_POLE_THIN;
	}

	public static boolean isConnected(IBlockState state, PropertyBool property)
	{
		return state.getValue(property);
	}
}
