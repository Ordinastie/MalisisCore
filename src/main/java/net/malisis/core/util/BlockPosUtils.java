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

package net.malisis.core.util;

import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;

/**
 * @author Ordinastie
 *
 */
public class BlockPosUtils
{
	public static BlockPos rotate(BlockPos pos, int rotation)
	{
		int[] cos = { 1, 0, -1, 0 };
		int[] sin = { 0, 1, 0, -1 };

		int a = rotation % 4;
		if (a < 0)
			a += 4;

		int newX = (pos.getX() * cos[a]) - (pos.getZ() * sin[a]);
		int newZ = (pos.getX() * sin[a]) + (pos.getZ() * cos[a]);

		return new BlockPos(newX, pos.getY(), newZ);
	}

	public static BlockPos chunkPosition(BlockPos pos)
	{
		return new BlockPos(pos.getX() - (pos.getX() >> 4) * 16, pos.getY() - (pos.getY() >> 4) * 16, pos.getZ() - (pos.getZ() >> 4) * 16);
	}

	public static Iterable<BlockPos> getAllInBox(AxisAlignedBB aabb)
	{
		return BlockPos.getAllInBox(new BlockPos(aabb.minX, aabb.minY, aabb.minZ),
				new BlockPos(Math.ceil(aabb.maxX) - 1, Math.ceil(aabb.maxY) - 1, Math.ceil(aabb.maxZ) - 1));
	}
}
