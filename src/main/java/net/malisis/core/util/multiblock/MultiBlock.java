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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.malisis.core.MalisisCore;
import net.malisis.core.block.component.DirectionalComponent;
import net.malisis.core.util.BlockPosUtils;
import net.malisis.core.util.EnumFacingUtils;
import net.malisis.core.util.MBlockState;
import net.malisis.core.util.blockdata.BlockDataHandler;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * @author Ordinastie
 *
 */
public abstract class MultiBlock implements Iterable<MBlockState>
{
	public static String ORIGIN_BLOCK_DATA = MalisisCore.modid + ":multiBlockOrigin";

	protected Map<BlockPos, MBlockState> states = new HashMap<>();
	protected BlockPos offset = new BlockPos(0, 0, 0);
	protected PropertyDirection property = DirectionalComponent.HORIZONTAL;
	private int rotation;
	private boolean bulkPlace;
	private boolean bulkBreak;

	public void setOffset(BlockPos offset)
	{
		this.offset = offset;
		buildStates();
	}

	public void setPropertyDirection(PropertyDirection property)
	{
		this.property = property;
	}

	public void setRotation(IBlockState state)
	{
		if (state == null || !state.getProperties().containsKey(property))
			rotation = 0;
		else
		{
			EnumFacing direction = (EnumFacing) state.getValue(property);
			rotation = EnumFacingUtils.getRotationCount(direction);
		}
	}

	public int getRotation()
	{
		return rotation;
	}

	public void setBulkProcess(boolean bulkPlace, boolean bulkBreak)
	{
		this.bulkPlace = bulkPlace;
		this.bulkBreak = bulkBreak;
	}

	public boolean isBulkPlace()
	{
		return bulkPlace;
	}

	public boolean isBulkBreak()
	{
		return bulkBreak;
	}

	public boolean isFromMultiblock(World world, BlockPos pos)
	{
		BlockPos origin = getOrigin(world, pos);
		if (origin == null)
			return false;

		IBlockState state = world.getBlockState(origin);
		setRotation(state);
		for (MBlockState mstate : this)
		{
			mstate = mstate.rotate(rotation).offset(pos);
			if (mstate.getPos().equals(pos))
				return true;
		}
		return false;
	}

	public MBlockState getState(BlockPos pos)
	{
		pos = BlockPosUtils.rotate(pos, 4 - rotation);
		return states.get(pos);
	}

	public boolean canPlaceBlockAt(World world, BlockPos pos, IBlockState state, boolean placeOrigin)
	{
		setRotation(state);
		for (MBlockState mstate : this)
		{
			mstate = mstate.rotate(rotation).offset(pos);
			if ((!mstate.getPos().equals(pos) || placeOrigin)
					&& !world.getBlockState(mstate.getPos()).getBlock().isReplaceable(world, mstate.getPos()))
				return false;
		}
		return true;
	}

	public void placeBlocks(World world, BlockPos pos, IBlockState state, boolean placeOrigin)
	{
		setRotation(state);
		for (MBlockState mstate : this)
		{
			mstate = mstate.rotate(rotation).offset(pos);
			if (!mstate.getPos().equals(pos) || placeOrigin)
			{
				BlockDataHandler.setData(ORIGIN_BLOCK_DATA, world, mstate.getPos(), pos);
				mstate.placeBlock(world, 2);
			}
		}

		BlockDataHandler.setData(ORIGIN_BLOCK_DATA, world, pos, pos);
	}

	public void breakBlocks(World world, BlockPos pos, IBlockState state)
	{
		BlockPos origin = getOrigin(world, pos);
		if (origin == null)
		{
			world.setBlockToAir(pos);
			return;
		}
		if (!pos.equals(origin))
		{
			breakBlocks(world, origin, world.getBlockState(origin));
			return;
		}

		BlockDataHandler.removeData(ORIGIN_BLOCK_DATA, world, origin);
		setRotation(state);
		for (MBlockState mstate : this)
		{
			mstate = mstate.rotate(rotation).offset(origin);
			if (mstate.matchesWorld(world))
			{
				mstate.breakBlock(world, 2);
				BlockDataHandler.removeData(ORIGIN_BLOCK_DATA, world, mstate.getPos());
			}
		}
	}

	public boolean isComplete(World world, BlockPos pos)
	{
		return isComplete(world, pos, null);
	}

	public boolean isComplete(World world, BlockPos pos, MBlockState newState)
	{
		setRotation(world.getBlockState(pos));
		MultiBlockAccess mba = new MultiBlockAccess(this, world);
		for (MBlockState mstate : this)
		{
			mstate = new MBlockState(mba, mstate.getPos())/*.rotate(rotation)*/.offset(pos);
			boolean matches = mstate.matchesWorld(world);
			if (!matches)
				mstate.matchesWorld(world);
			if (!matches && (newState == null || !mstate.equals(newState)))
				return false;
		}

		return true;
	}

	@Override
	public Iterator<MBlockState> iterator()
	{
		return states.values().iterator();
	}

	protected abstract void buildStates();

	public static void registerBlockData()
	{
		BlockDataHandler.registerBlockData(ORIGIN_BLOCK_DATA, BlockPosUtils::fromBytes, BlockPosUtils::toBytes);
	}

	public static BlockPos getOrigin(IBlockAccess world, BlockPos pos)
	{
		return world != null && pos != null ? BlockDataHandler.getData(ORIGIN_BLOCK_DATA, world, pos) : null;
	}

	public static boolean isOrigin(IBlockAccess world, BlockPos pos)
	{
		return world != null && pos != null && pos.equals(getOrigin(world, pos));
	}
}
