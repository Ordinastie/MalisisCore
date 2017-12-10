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
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

/**
 * AABBMultiBlock allows to define a MultiBlock from an {@link AxisAlignedBB}.<br>
 * The first block placed will be the origin of the MultiBlock and all the block spaces intersecting the AABB (relative to the origin) will
 * be filled.<br>
 * The {@link IBlockState} used when placing is the default one altered to have the proper values for the origin and direction properties.
 *
 * @author Ordinastie
 */
public class AABBMultiBlock extends MultiBlock
{
	private AxisAlignedBB aabb;
	private IBlockState blockState;

	public AABBMultiBlock(AxisAlignedBB aabb)
	{
		this.aabb = aabb;
	}

	public AABBMultiBlock(IBlockState state, AxisAlignedBB aabb)
	{
		this(aabb);
		setDefaultState(state);
	}

	public AxisAlignedBB getBoundingBox()
	{
		return aabb;
	}

	public void setDefaultState(IBlockState state)
	{
		blockState = state;
		buildStates();
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
}
