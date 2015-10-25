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

package net.malisis.core.util.multiblock;

import net.malisis.core.util.BlockPosUtils;
import net.malisis.core.util.MBlockState;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

/**
 * @author Ordinastie
 *
 */
public class AABBMultiBlock extends MultiBlock
{
	private AxisAlignedBB aabb;
	private IBlockState blockState;

	public AABBMultiBlock(Block block, AxisAlignedBB aabb)
	{
		this.aabb = aabb;
		this.blockState = block.getDefaultState();
		buildStates();
	}

	public AxisAlignedBB getBoundingBox()
	{
		return aabb;
	}

	public AxisAlignedBB getRelativeBoundingBox(BlockPos pos, BlockPos origin)
	{
		pos = pos.subtract(origin);
		return aabb.offset(-pos.getX(), -pos.getY(), -pos.getZ());
	}

	@Override
	protected void buildStates()
	{
		states.clear();
		for (BlockPos pos : BlockPosUtils.getAllInBox(aabb))
		{
			if (offset != null)
				pos = pos.add(offset);
			states.put(pos, new MBlockState(pos, blockState));
		}
	}

	@Override
	public void placeBlocks(World world, BlockPos pos, IBlockState state, boolean placeOrigin)
	{
		blockState = state.withProperty(IMultiBlock.ORIGIN, false);
		buildStates();
		super.placeBlocks(world, pos, state, placeOrigin);
	}
}
