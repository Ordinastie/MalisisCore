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

import net.malisis.core.renderer.element.Vertex;
import net.malisis.core.util.BlockPos;
import net.malisis.core.util.BlockState;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * @author Ordinastie
 *
 */
public abstract class MultiBlock implements Iterable<BlockState>, IBlockAccess
{
	protected Map<BlockPos, BlockState> states = new HashMap<>();

	protected BlockPos offset;
	protected int rotation;

	public void setOffset(BlockPos offset)
	{
		this.offset = offset;
		buildStates();
	}

	public void setRotation(int rotation)
	{
		this.rotation = rotation;
	}

	public boolean isFromMultiblock(BlockPos pos)
	{
		return getBlockState(pos) != null;
	}

	public BlockState getBlockState(BlockPos pos)
	{
		pos = pos.rotate(4 - rotation);
		return states.get(pos);
	}

	public boolean canPlaceBlockAt(World world, BlockPos pos)
	{
		for (BlockState state : this)
		{
			BlockPos p = state.getPos().add(pos);
			if (!state.getBlock().canPlaceBlockAt(world, p.getX(), p.getY(), p.getZ()))
				return false;
		}
		return true;
	}

	public void placeBlocks(World world, BlockPos pos)
	{
		for (BlockState state : this)
		{
			state = state.rotate(rotation).offset(pos);
			if (!state.getPos().equals(pos))
			{
				state.placeBlock(world, 2);
				state.rotateInWorld(world, rotation);
			}
		}
	}

	public void breakBlocks(World world, BlockPos pos)
	{
		for (BlockState state : this)
		{
			state = state.rotate(rotation).offset(pos);
			if (!state.getPos().equals(pos))
				state.breakBlock(world, 2);
		}
	}

	public boolean isComplete(World world, BlockPos pos)
	{
		return isComplete(world, pos, null);
	}

	public boolean isComplete(World world, BlockPos pos, BlockState newState)
	{
		for (BlockState state : this)
		{
			state = state.offset(pos);
			if (!state.matchesWorld(world) && (newState == null || !state.equals(newState)))
				return false;
		}

		return true;
	}

	@Override
	public Iterator<BlockState> iterator()
	{
		return states.values().iterator();
	}

	protected abstract void buildStates();

	@Override
	public Block getBlock(int x, int y, int z)
	{
		BlockState state = getBlockState(new BlockPos(x, y, z));
		if (state == null)
			return Blocks.air;
		return state.getBlock();
	}

	@Override
	public TileEntity getTileEntity(int x, int y, int z)
	{
		return null;
	}

	@Override
	public int getLightBrightnessForSkyBlocks(int p_72802_1_, int p_72802_2_, int p_72802_3_, int p_72802_4_)
	{
		return Vertex.BRIGHTNESS_MAX;
	}

	@Override
	public int getBlockMetadata(int x, int y, int z)
	{
		BlockState state = getBlockState(new BlockPos(x, y, z));
		if (state == null)
			return 0;
		return state.getMetadata();
	}

	@Override
	public int isBlockProvidingPowerTo(int x, int y, int z, int directionIn)
	{
		return 0;
	}

	@Override
	public boolean isAirBlock(int x, int y, int z)
	{
		return getBlock(x, y, z).isAir(this, x, y, z);
	}

	@Override
	public BiomeGenBase getBiomeGenForCoords(int x, int z)
	{
		return null;
	}

	@Override
	public int getHeight()
	{
		return 0;
	}

	@Override
	public boolean extendedLevelsInChunkCache()
	{
		return false;
	}

	@Override
	public boolean isSideSolid(int x, int y, int z, ForgeDirection side, boolean _default)
	{
		return getBlock(x, y, z).isSideSolid(this, x, y, z, side);
	}
}
