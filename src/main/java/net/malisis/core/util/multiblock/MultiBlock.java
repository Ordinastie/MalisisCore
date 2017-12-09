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

import com.google.common.collect.Iterators;

import net.malisis.core.MalisisCore;
import net.malisis.core.block.IComponent;
import net.malisis.core.block.component.DirectionalComponent;
import net.malisis.core.registry.AutoLoad;
import net.malisis.core.util.BlockPosUtils;
import net.malisis.core.util.EnumFacingUtils;
import net.malisis.core.util.MBlockState;
import net.malisis.core.util.blockdata.BlockDataHandler;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * @author Ordinastie
 *
 */
@AutoLoad
public abstract class MultiBlock implements Iterable<MBlockState>
{
	public static String ORIGIN_BLOCK_DATA = MalisisCore.modid + ":multiBlockOrigin";

	static
	{
		BlockDataHandler.registerBlockData(ORIGIN_BLOCK_DATA, BlockPosUtils::fromBytes, BlockPosUtils::toBytes);
	}

	protected Map<BlockPos, MBlockState> states = new HashMap<>();
	protected BlockPos offset = BlockPos.ORIGIN;
	protected PropertyDirection property = DirectionalComponent.HORIZONTAL;
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

	public int getRotation(IBlockState state)
	{
		if (state == null || !state.getProperties().containsKey(property))
			return 0;

		EnumFacing direction = state.getValue(property);
		return EnumFacingUtils.getRotationCount(direction);
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

		for (MBlockState mstate : worldStates(world, origin))
		{
			if (mstate.getPos().equals(pos))
				return true;
		}
		return false;
	}

	public MBlockState getState(BlockPos pos)
	{
		//FIXME
		pos = BlockPosUtils.rotate(pos, 4 /*- rotation*/);
		return states.get(pos);
	}

	public boolean canPlaceBlockAt(World world, BlockPos origin, IBlockState originState, boolean placeOrigin)
	{
		for (MBlockState mstate : worldStates(origin, originState))
		{
			if ((!mstate.getPos().equals(origin) || placeOrigin)
					&& !world.getBlockState(mstate.getPos()).getBlock().isReplaceable(world, mstate.getPos()))
				return false;
		}
		return true;
	}

	public void placeBlocks(World world, BlockPos origin, IBlockState originState, boolean placeOrigin)
	{
		for (MBlockState mstate : worldStates(world, origin))
		{
			if (!mstate.getPos().equals(origin) || placeOrigin)
			{
				BlockDataHandler.setData(ORIGIN_BLOCK_DATA, world, mstate.getPos(), origin);
				mstate.placeBlock(world, 2);
			}
		}

		BlockDataHandler.setData(ORIGIN_BLOCK_DATA, world, origin, origin);
	}

	public void breakBlocks(World world, BlockPos pos, IBlockState state)
	{
		//we can't check the origin from world as the state is already changed in the chunk, so we check the passed state
		if (!MultiBlockComponent.isOrigin(state))
		{
			BlockPos originPos = getOrigin(world, pos);
			if (originPos == null) //should not happen
			{
				world.setBlockToAir(pos);
				return;
			}

			world.setBlockToAir(originPos);
			return;
		}

		doBreakBlocks(world, pos, state);
	}

	private void doBreakBlocks(World world, BlockPos origin, IBlockState originState)
	{
		BlockDataHandler.removeData(ORIGIN_BLOCK_DATA, world, origin);
		for (MBlockState mstate : worldStates(origin, originState))
		{
			if (mstate.matchesWorld(world))
			{
				mstate.breakBlock(world, 2);
				BlockDataHandler.removeData(ORIGIN_BLOCK_DATA, world, mstate.getPos());
			}
		}
	}

	public void setOriginData(World world, BlockPos pos, IBlockState state)
	{
		for (MBlockState mstate : this)
			BlockDataHandler.setData(ORIGIN_BLOCK_DATA, world, mstate.getPos(), pos);

		BlockDataHandler.setData(ORIGIN_BLOCK_DATA, world, pos, pos);
	}

	public boolean isComplete(World world, BlockPos pos)
	{
		return isComplete(world, pos, null);
	}

	public boolean isComplete(World world, BlockPos pos, MBlockState newState)
	{
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

	public Iterable<MBlockState> worldStates(IBlockAccess world, BlockPos origin)
	{
		return worldStates(origin, world.getBlockState(origin));
	}

	public Iterable<MBlockState> worldStates(BlockPos origin, IBlockState originState)
	{
		return new Iterable<MBlockState>()
		{
			@Override
			public Iterator<MBlockState> iterator()
			{
				return Iterators.transform(MultiBlock.this.iterator(), mstate -> getWorldState(mstate, origin, originState));
			}
		};
	}

	public MBlockState getWorldState(MBlockState mstate, BlockPos origin, IBlockState originState)
	{
		return mstate.rotate(getRotation(originState)).offset(origin);
	}

	protected abstract void buildStates();

	public static BlockPos getOrigin(IBlockAccess world, BlockPos pos)
	{
		BlockPos origin = BlockDataHandler.getData(ORIGIN_BLOCK_DATA, world, pos);
		if (origin != null && IComponent.getComponent(MultiBlockComponent.class, world.getBlockState(origin).getBlock()) == null)
		{
			origin = null;
			BlockDataHandler.removeData(ORIGIN_BLOCK_DATA, world, pos);
		}
		return world != null && pos != null ? origin : null;
	}

	public static boolean isOrigin(IBlockAccess world, BlockPos pos)
	{
		return world != null && pos != null && pos.equals(getOrigin(world, pos));
	}
}
