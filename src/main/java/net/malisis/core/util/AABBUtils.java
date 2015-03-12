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

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;

/**
 * @author Ordinastie
 *
 */
public class AABBUtils
{

	public static AxisAlignedBB[] rotate(AxisAlignedBB[] aabbs, int angle)
	{
		for (AxisAlignedBB aabb : aabbs)
			rotate(aabb, angle);
		return aabbs;
	}

	public static AxisAlignedBB rotate(AxisAlignedBB aabb, int angle)
	{
		int[] cos = { 1, 0, -1, 0 };
		int[] sin = { 0, 1, 0, -1 };

		int a = angle % 4;
		if (a < 0)
			a += 4;

		AxisAlignedBB copy = AxisAlignedBB.getBoundingBox(1, aabb.minY, 1, 1, aabb.maxY, 1);
		aabb.offset(-0.5F, 0, -0.5F);

		copy.minX = (aabb.minX * cos[a]) - (aabb.minZ * sin[a]);
		copy.minZ = (aabb.minX * sin[a]) + (aabb.minZ * cos[a]);

		copy.maxX = (aabb.maxX * cos[a]) - (aabb.maxZ * sin[a]);
		copy.maxZ = (aabb.maxX * sin[a]) + (aabb.maxZ * cos[a]);

		aabb.setBB(fix(copy));
		aabb.offset(0.5F, 0, 0.5F);

		return aabb;
	}

	public static AxisAlignedBB fix(AxisAlignedBB aabb)
	{
		double tmp;
		if (aabb.minX > aabb.maxX)
		{
			tmp = aabb.minX;
			aabb.minX = aabb.maxX;
			aabb.maxX = tmp;
		}

		if (aabb.minZ > aabb.maxZ)
		{
			tmp = aabb.minZ;
			aabb.minZ = aabb.maxZ;
			aabb.maxZ = tmp;
		}

		return aabb;
	}

	public static AxisAlignedBB readFromNBT(NBTTagCompound tag, AxisAlignedBB aabb)
	{
		return aabb.setBounds(tag.getDouble("minX"), tag.getDouble("minY"), tag.getDouble("minZ"), tag.getDouble("maxX"),
				tag.getDouble("maxY"), tag.getDouble("maxZ"));
	}

	public static void writeToNBT(NBTTagCompound tag, AxisAlignedBB aabb)
	{
		if (aabb == null)
			return;
		tag.setDouble("minX", aabb.minX);
		tag.setDouble("minY", aabb.minY);
		tag.setDouble("minZ", aabb.minZ);
		tag.setDouble("maxX", aabb.maxX);
		tag.setDouble("maxY", aabb.maxY);
		tag.setDouble("maxZ", aabb.maxZ);
	}
}
