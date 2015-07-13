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

package net.malisis.core.renderer.icon;

import static net.minecraft.util.EnumFacing.*;

import java.util.HashMap;

import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;

/**
 * @author Ordinastie
 *
 */
public class MegaTextureIcon extends MalisisIcon
{
	private static HashMap<EnumFacing, EnumFacing[]> searchDirs = new HashMap<>();
	static
	{
		searchDirs.put(NORTH, new EnumFacing[] { DOWN, EAST });
		searchDirs.put(SOUTH, new EnumFacing[] { DOWN, WEST });
		searchDirs.put(EAST, new EnumFacing[] { DOWN, SOUTH });
		searchDirs.put(WEST, new EnumFacing[] { DOWN, NORTH });
	}

	private BlockPos base;
	private int numBlocks = -1;

	public MegaTextureIcon(String name)
	{
		super(name);
	}

	public MegaTextureIcon(String name, int numBlocks)
	{
		super(name);
		this.numBlocks = numBlocks;
	}

	public MegaTextureIcon(MalisisIcon icon)
	{
		super(icon);
	}

	public MegaTextureIcon(MalisisIcon icon, int numBlocks)
	{
		super(icon);
		this.numBlocks = numBlocks;
	}

	public MalisisIcon getIcon(IBlockAccess world, Block block, BlockPos pos, int side)
	{
		EnumFacing dir = EnumFacing.getFront(side);
		getBaseBlock(world, block, pos, dir);
		return getIcon(pos, dir);
	}

	private void getBaseBlock(IBlockAccess world, Block block, BlockPos pos, EnumFacing side)
	{
		base = new BlockPos(pos);
		EnumFacing[] dirs = searchDirs.get(side);
		if (dirs == null)
			return;

		for (EnumFacing dir : dirs)
		{
			while (world.getBlockState(base) == block)
				base = base.offset(dir);

			//not the block anymore, go one back
			dir = dir.getOpposite();
			base = base.offset(dir);
		}
	}

	private MalisisIcon getIcon(BlockPos pos, EnumFacing dir)
	{
		if (numBlocks == -1)
		{
			int w = width;
			numBlocks = w / 16;
		}

		int u = 0;
		int v = ((pos.getY() - base.getY()) % numBlocks) + 1;
		if (dir == NORTH || dir == SOUTH)
			u = Math.abs(pos.getX() - base.getX()) % numBlocks;
		else
			u = Math.abs(pos.getZ() - base.getZ()) % numBlocks;

		float factor = 1.0F / numBlocks;
		MalisisIcon icon = new MalisisIcon();
		icon.copyFrom(this);
		icon.clip(u * factor, 1 - v * factor, factor, factor);
		return icon;
	}
}
