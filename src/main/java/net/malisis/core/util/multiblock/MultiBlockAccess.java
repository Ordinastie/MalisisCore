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

import net.malisis.core.renderer.element.Vertex;
import net.malisis.core.util.MBlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;

/**
 * @author Ordinastie
 *
 */
public class MultiBlockAccess implements IBlockAccess
{
	private MultiBlock multiBlock;
	private IBlockAccess world;

	public MultiBlockAccess(MultiBlock multiBlock)
	{
		this.multiBlock = multiBlock;
	}

	public MultiBlockAccess(MultiBlock multiBlock, IBlockAccess world)
	{
		this.multiBlock = multiBlock;
		this.world = world;
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
	public IBlockState getBlockState(BlockPos pos)
	{
		MBlockState state = multiBlock.getState(pos);
		if (state != null)
			return state.getBlockState();

		return world != null ? world.getBlockState(pos) : Blocks.air.getDefaultState();
	}

	@Override
	public boolean isAirBlock(BlockPos pos)
	{
		return getBlockState(pos).getBlock().isAir(this, pos);
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
	public int getStrongPower(BlockPos pos, EnumFacing direction)
	{
		return 0;
	}

	@Override
	public WorldType getWorldType()
	{
		return null;
	}

	@Override
	public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default)
	{
		return getBlockState(pos).getBlock().isSideSolid(this, pos, side);
	}

}
