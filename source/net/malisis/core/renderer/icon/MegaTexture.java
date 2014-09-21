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

import static net.minecraftforge.common.util.ForgeDirection.*;

import java.util.HashMap;

import net.malisis.core.renderer.MalisisIcon;
import net.minecraft.block.Block;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * @author Ordinastie
 * 
 */
public class MegaTexture extends MalisisIcon
{
	private static HashMap<ForgeDirection, ForgeDirection[]> searchDirs = new HashMap<>();
	static
	{
		searchDirs.put(NORTH, new ForgeDirection[] { DOWN, EAST });
		searchDirs.put(SOUTH, new ForgeDirection[] { DOWN, WEST });
		searchDirs.put(EAST, new ForgeDirection[] { DOWN, SOUTH });
		searchDirs.put(WEST, new ForgeDirection[] { DOWN, NORTH });
	}

	int baseX, baseY, baseZ;
	int numBlocks = -1;

	public MegaTexture(String name)
	{
		super(name);
	}

	public MegaTexture(String name, int numBlocks)
	{
		super(name);
		this.numBlocks = numBlocks;
	}

	public IIcon getIcon(IBlockAccess world, Block block, int x, int y, int z, int side)
	{
		ForgeDirection dir = ForgeDirection.getOrientation(side);
		getBaseBlock(world, block, x, y, z, dir);
		return getIcon(x, y, z, dir);
	}

	private void getBaseBlock(IBlockAccess world, Block block, int x, int y, int z, ForgeDirection side)
	{
		baseX = x;
		baseY = y;
		baseZ = z;
		ForgeDirection[] dirs = searchDirs.get(side);
		if (dirs == null)
			return;

		for (ForgeDirection dir : dirs)
		{
			while (world.getBlock(baseX, baseY, baseZ) == block)
			{
				baseX += dir.offsetX;
				baseY += dir.offsetY;
				baseZ += dir.offsetZ;
			}
			//not the block anymore, go one back
			dir = dir.getOpposite();
			baseX += dir.offsetX;
			baseY += dir.offsetY;
			baseZ += dir.offsetZ;
		}
	}

	private IIcon getIcon(int x, int y, int z, ForgeDirection dir)
	{
		if (numBlocks == -1)
		{
			int w = width;
			if (useAnisotropicFiltering)
				w -= 16;
			numBlocks = w / 16;
		}

		int u = 0;
		int v = ((y - baseY) % numBlocks) + 1;
		if (dir == NORTH || dir == SOUTH)
			u = Math.abs(x - baseX) % numBlocks;
		else
			u = Math.abs(z - baseZ) % numBlocks;

		float factor = 1.0F / numBlocks;
		MalisisIcon icon = clone();
		icon.clip(u * factor, 1 - v * factor, factor, factor);
		return icon;
	}
}
