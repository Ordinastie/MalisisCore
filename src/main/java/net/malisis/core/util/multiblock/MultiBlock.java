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
import net.malisis.core.block.IBlockDirectional;
import net.malisis.core.util.BlockPosUtils;
import net.malisis.core.util.EnumFacingUtils;
import net.malisis.core.util.MBlockState;
import net.malisis.core.util.blockdata.BlockDataHandler;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

/**
 * @author Ordinastie
 *
 */
public abstract class MultiBlock implements Iterable<MBlockState>//, IBlockAccess
{
	public static String ORIGIN_BLOCK_DATA = MalisisCore.modid + ":multiBlockOrigin";

	protected Map<BlockPos, MBlockState> states = new HashMap<>();
	protected BlockPos offset = new BlockPos(0, 0, 0);
	protected PropertyDirection property = IBlockDirectional.DIRECTION;
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

	private void setRotation(IBlockState state)
	{
		EnumFacing direction = (EnumFacing) state.getValue(property);
		rotation = EnumFacingUtils.getRotationCount(direction);
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

	public BlockPos getOrigin(World world, BlockPos pos)
	{
		return BlockDataHandler.getData(ORIGIN_BLOCK_DATA, world, pos);
	}

	public boolean isOrigin(World world, BlockPos pos)
	{
		return pos.equals(getOrigin(world, pos));
	}

	public boolean canPlaceBlockAt(World world, BlockPos pos, IBlockState state)
	{
		setRotation(state);
		for (MBlockState mstate : this)
		{
			mstate = mstate.rotate(rotation).offset(pos);
			if (!pos.equals(mstate.getPos()) && world.getBlockState(pos).getBlock().isReplaceable(world, pos))
				//if (!pos.equals(mstate.getPos()) && !mstate.getBlock().canPlaceBlockAt(world, mstate.getPos()))
				return false;
		}
		return true;
	}

	public void placeBlocks(World world, BlockPos pos, IBlockState state)
	{
		setRotation(state);
		for (MBlockState mstate : this)
		{
			mstate = mstate.rotate(rotation).offset(pos);
			if (!mstate.getPos().equals(pos))
			{
				mstate.placeBlock(world, 2);
				BlockDataHandler.setData(ORIGIN_BLOCK_DATA, world, mstate.getPos(), pos);
			}
		}

		BlockDataHandler.setData(ORIGIN_BLOCK_DATA, world, pos, pos);
	}

	public void breakBlocks(World world, BlockPos pos, IBlockState state)
	{
		BlockPos origin = getOrigin(world, pos);
		if (origin == null)
			return;
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
		for (MBlockState state : this)
		{
			state = state.offset(pos);
			if (!state.matchesWorld(world) && (newState == null || !state.equals(newState)))
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

	/*
	@Override
	public IBlockState getBlockState(BlockPos pos)
	{
		MBlockState state = getState(pos);
		if (state == null)
			return Blocks.air.getDefaultState();
		return state.getBlockState();
	}

	@Override
	public TileEntity getTileEntity(BlockPos pos)
	{
		return null;
	}

	@Override
	public int getCombinedLight(BlockPos pos, int lightValue)
	{
		return Vertex.BRIGHTNESS_MAX;
	}

	@Override
	public boolean isAirBlock(BlockPos pos)
	{
		return getState(pos).getBlock() == Blocks.air;
	}

	@Override
	public BiomeGenBase getBiomeGenForCoords(BlockPos pos)
	{
		return null;
	}

	@Override
	public boolean extendedLevelsInChunkCache()
	{
		return false;
	}

	@Override
	public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default)
	{
		return getBlockState(pos).getBlock().isSideSolid(this, pos, side);
	}

	@Override
	public int getStrongPower(BlockPos pos, EnumFacing direction)
	{
		return 0;
	}

	@Override
	public WorldType getWorldType()
	{
		return null;
	}
	*/
	public static void regsiterBlockData()
	{
		BlockDataHandler.registerBlockData(ORIGIN_BLOCK_DATA, BlockPosUtils::fromBytes, BlockPosUtils::toBytes);
	}

}
