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

package net.malisis.core.renderer.icon.provider;

import static net.minecraft.util.EnumFacing.*;

import java.util.HashMap;

import net.malisis.core.block.component.DirectionalComponent;
import net.malisis.core.renderer.icon.MalisisIcon;
import net.malisis.core.util.MBlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;

/**
 * @author Ordinastie
 *
 */
public class MegaTextureIconProvider extends SidesIconProvider
{
	private static HashMap<EnumFacing, EnumFacing[]> searchDirs = new HashMap<>();
	static
	{
		searchDirs.put(NORTH, new EnumFacing[] { DOWN, EAST });
		searchDirs.put(SOUTH, new EnumFacing[] { DOWN, WEST });
		searchDirs.put(EAST, new EnumFacing[] { DOWN, SOUTH });
		searchDirs.put(WEST, new EnumFacing[] { DOWN, NORTH });
		searchDirs.put(UP, new EnumFacing[] { SOUTH, WEST });
		searchDirs.put(DOWN, new EnumFacing[] { SOUTH, WEST });
	}

	private int[] numBlocks = new int[6];

	public MegaTextureIconProvider(MalisisIcon defaultIcon)
	{
		super(defaultIcon);
	}

	public void setMegaTexture(EnumFacing side, MalisisIcon icon)
	{
		setMegaTexture(side, icon, -1);
	}

	public void setMegaTexture(EnumFacing side, MalisisIcon icon, int numBlocks)
	{
		setSideIcon(side, icon);
		this.numBlocks[side.getIndex()] = numBlocks;
	}

	@Override
	public void setSideIcon(EnumFacing side, MalisisIcon icon)
	{
		super.setSideIcon(side, icon);
		numBlocks[side.getIndex()] = 0;
	}

	private boolean isMegaTexture(EnumFacing side)
	{
		return numBlocks[side.getIndex()] != 0;
	}

	private int getNumBlocks(MalisisIcon icon, EnumFacing side)
	{
		if (this.numBlocks[side.getIndex()] == -1)
		{
			int w = icon.getIconWidth();
			this.numBlocks[side.getIndex()] = w / 16;
		}

		return this.numBlocks[side.getIndex()];
	}

	@Override
	public MalisisIcon getIcon(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side)
	{
		MalisisIcon icon = super.getIcon(world, pos, state, side);
		EnumFacing blockDir = DirectionalComponent.getDirection(state);
		int numBlocks = getNumBlocks(icon, side);
		if (!isMegaTexture(side))
			return icon;

		MBlockState baseState = getBaseState(world, new MBlockState(pos, state), blockDir);
		if (baseState == null)
			return icon;

		return getIconPart(icon, pos, baseState, blockDir, numBlocks);
	}

	private MBlockState getBaseState(IBlockAccess world, MBlockState state, EnumFacing side)
	{
		MBlockState baseState = state;
		MBlockState lastState = state;
		EnumFacing[] dirs = searchDirs.get(side);
		if (dirs == null)
			return null;

		for (EnumFacing dir : dirs)
		{
			lastState = baseState;
			while (lastState.getBlock() == state.getBlock())
			{
				baseState = lastState;
				try
				{
					lastState = new MBlockState(world, baseState.getPos().offset(dir));
				}
				catch (Exception e)
				{
					break;
				}
			}
		}

		return baseState;
	}

	private MalisisIcon getIconPart(MalisisIcon icon, BlockPos pos, MBlockState state, EnumFacing side, int numBlocks)
	{
		int u = 0;
		int v = ((pos.getY() - state.getY()) % numBlocks) + 1;
		if (side == NORTH || side == SOUTH)
			u = Math.abs(pos.getX() - state.getX()) % numBlocks;
		else if (side == EAST || side == WEST)
			u = Math.abs(pos.getZ() - state.getZ()) % numBlocks;
		else
		{
			u = Math.abs(pos.getX() - state.getX()) % numBlocks;
			v = (Math.abs(pos.getZ() - state.getZ()) % numBlocks) + 1;
		}

		float factor = 1.0F / numBlocks;
		MalisisIcon copy = new MalisisIcon();
		copy.copyFrom(icon);
		copy.clip(u * factor, 1 - v * factor, factor, factor);
		return copy;
	}
}
