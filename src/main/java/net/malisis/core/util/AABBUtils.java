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

import net.malisis.core.block.IBoundingBox;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.apache.commons.lang3.ArrayUtils;

/**
 * @author Ordinastie
 *
 */
public class AABBUtils
{
	private static int[] cos = { 1, 0, -1, 0 };
	private static int[] sin = { 0, 1, 0, -1 };

	/**
	 * Gets an empty {@link AxisAlignedBB} (size 0x0x0) at position 0,0,0.
	 *
	 * @return the axis aligned bb
	 */
	public static AxisAlignedBB empty()
	{
		return empty(BlockPos.ORIGIN);
	}

	/**
	 * Gets an empty {@link AxisAlignedBB} (size 0x0x0) at the {@link BlockPos} position.
	 *
	 * @param pos the pos
	 * @return the axis aligned bb
	 */
	public static AxisAlignedBB empty(BlockPos pos)
	{
		return new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
	}

	/**
	 * Gets an identity {@link AxisAlignedBB} (size 1x1x1) at position 0,0,0.
	 *
	 * @return the axis aligned bb
	 */
	public static AxisAlignedBB identity()
	{
		return Block.FULL_BLOCK_AABB;
	}

	/**
	 * Gets an identity {@link AxisAlignedBB} (size 1x1x1) at {@link BlockPos} position;
	 *
	 * @param pos the pos
	 * @return the axis aligned bb
	 */
	public static AxisAlignedBB identity(BlockPos pos)
	{
		return new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
	}

	/**
	 * Gets an identity {@link AxisAlignedBB} (size 1x1x1) at position 0,0,0 returned as an array.
	 *
	 * @return the axis aligned b b[]
	 */
	public static AxisAlignedBB[] identities()
	{
		return identities(BlockPos.ORIGIN);
	}

	/**
	 * Gets an identity {@link AxisAlignedBB} (size 1x1x1) at {@link BlockPos} position returned as an array.
	 *
	 * @param pos the pos
	 * @return the axis aligned b b[]
	 */
	public static AxisAlignedBB[] identities(BlockPos pos)
	{
		return new AxisAlignedBB[] { identity(pos) };
	}

	/**
	 * Rotates the {@link AxisAlignedBB} based on the specified direction.<br>
	 * Assumes {@link EnumFacing#SOUTH} to be the default non rotated direction.<br>
	 *
	 *
	 * @param aabb the aabb
	 * @param dir the dir
	 * @return the axis aligned bb
	 */
	public static AxisAlignedBB rotate(AxisAlignedBB aabb, EnumFacing dir)
	{
		return rotate(aabb, EnumFacingUtils.getRotationCount(dir));
	}

	/**
	 * Rotates an array of {@link AxisAlignedBB} around the Y axis based on the specified direction.<br>
	 * Assumes {@link EnumFacing#SOUTH} to be the default non rotated direction.<br>
	 *
	 * @param aabbs the aabbs
	 * @param dir the dir
	 * @return the axis aligned b b[]
	 */
	public static AxisAlignedBB[] rotate(AxisAlignedBB[] aabbs, EnumFacing dir)
	{
		return rotate(aabbs, EnumFacingUtils.getRotationCount(dir));
	}

	/**
	 * Rotates an array {@link AxisAlignedBB} around the Y axis based on the specified angle.<br>
	 *
	 * @param aabbs the aabbs
	 * @param angle the angle
	 * @return the axis aligned b b[]
	 */
	public static AxisAlignedBB[] rotate(AxisAlignedBB[] aabbs, int angle)
	{
		if (ArrayUtils.isEmpty(aabbs) || angle == 0)
			return aabbs;

		for (int i = 0; i < aabbs.length; i++)
			aabbs[i] = rotate(aabbs[i], angle);
		return aabbs;
	}

	/**
	 * Rotates the {@link AxisAlignedBB} around the Y axis based on the specified angle.<br>
	 *
	 * @param aabb the aabb
	 * @param angle the angle
	 * @return the axis aligned bb
	 */
	public static AxisAlignedBB rotate(AxisAlignedBB aabb, int angle)
	{
		return rotate(aabb, angle, Axis.Y);
	}

	/**
	 * Rotates the {@link AxisAlignedBB} around the axis based on the specified angle.<br>
	 *
	 * @param aabb the aabb
	 * @param angle the angle
	 * @param axis the axis
	 * @return the axis aligned bb
	 */
	public static AxisAlignedBB rotate(AxisAlignedBB aabb, int angle, Axis axis)
	{
		if (aabb == null || angle == 0 || axis == null)
			return aabb;

		int a = -angle & 3;
		int s = sin[a];
		int c = cos[a];

		aabb = aabb.offset(-0.5F, -0.5F, -0.5F);

		double minX = aabb.minX;
		double minY = aabb.minY;
		double minZ = aabb.minZ;
		double maxX = aabb.maxX;
		double maxY = aabb.maxY;
		double maxZ = aabb.maxZ;

		if (axis == Axis.X)
		{
			minY = (aabb.minY * c) - (aabb.minZ * s);
			maxY = (aabb.maxY * c) - (aabb.maxZ * s);
			minZ = (aabb.minY * s) + (aabb.minZ * c);
			maxZ = (aabb.maxY * s) + (aabb.maxZ * c);

		}
		if (axis == Axis.Y)
		{
			minX = (aabb.minX * c) - (aabb.minZ * s);
			maxX = (aabb.maxX * c) - (aabb.maxZ * s);
			minZ = (aabb.minX * s) + (aabb.minZ * c);
			maxZ = (aabb.maxX * s) + (aabb.maxZ * c);
		}

		if (axis == Axis.Z)
		{
			minX = (aabb.minX * c) - (aabb.minY * s);
			maxX = (aabb.maxX * c) - (aabb.maxY * s);
			minY = (aabb.minX * s) + (aabb.minY * c);
			maxY = (aabb.maxX * s) + (aabb.maxY * c);
		}

		aabb = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
		aabb = aabb.offset(0.5F, 0.5F, 0.5F);

		return aabb;
	}

	/**
	 * Reads a {@link AxisAlignedBB} from {@link NBTTagCompound}.<br>
	 * TODO: allow custom name prefix
	 *
	 * @param tag the tag
	 * @return the axis aligned bb
	 */
	public static AxisAlignedBB readFromNBT(NBTTagCompound tag)
	{
		return tag != null ? new AxisAlignedBB(tag.getDouble("minX"), tag.getDouble("minY"), tag.getDouble("minZ"), tag.getDouble("maxX"),
				tag.getDouble("maxY"), tag.getDouble("maxZ")) : null;
	}

	/**
	 * Writes a {@link AxisAlignedBB} to a {@link NBTTagCompound}.<br>
	 * TODO: allow custom name prefix
	 *
	 * @param tag the tag
	 * @param aabb the aabb
	 */
	public static void writeToNBT(NBTTagCompound tag, AxisAlignedBB aabb)
	{
		if (tag == null || aabb == null)
			return;
		tag.setDouble("minX", aabb.minX);
		tag.setDouble("minY", aabb.minY);
		tag.setDouble("minZ", aabb.minZ);
		tag.setDouble("maxX", aabb.maxX);
		tag.setDouble("maxY", aabb.maxY);
		tag.setDouble("maxZ", aabb.maxZ);
	}

	/**
	 * Gets a {@link AxisAlignedBB} that encompasses the passed {@code AxisAlignedBB}.
	 *
	 * @param aabbs the aabbs
	 * @return the axis aligned bb
	 */
	public static AxisAlignedBB combine(AxisAlignedBB[] aabbs)
	{
		if (ArrayUtils.isEmpty(aabbs))
			return null;

		AxisAlignedBB ret = null;
		for (AxisAlignedBB aabb : aabbs)
		{
			if (ret == null)
				ret = aabb;
			else if (aabb != null)
				ret = ret.union(aabb);
		}

		return ret;
	}

	/**
	 * Offsets the passed {@link AxisAlignedBB} array by the specified coordinates.
	 *
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @param aabbs the aabbs
	 */
	public static AxisAlignedBB[] offset(double x, double y, double z, AxisAlignedBB... aabbs)
	{
		return offset(new BlockPos(x, y, z), aabbs);
	}

	/**
	 * Offsets the passed {@link AxisAlignedBB} by the {@link BlockPos} coordinates.
	 *
	 * @param pos the pos
	 * @param aabb the aabb
	 * @return the axis aligned bb
	 */
	public static AxisAlignedBB offset(BlockPos pos, AxisAlignedBB aabb)
	{
		if (aabb == null || pos == null)
			return aabb;
		return aabb.offset(pos.getX(), pos.getY(), pos.getZ());
	}

	/**
	 * Offsets the passed {@link AxisAlignedBB} array by the {@link BlockPos} coordinates.
	 *
	 * @param pos the pos
	 * @param aabbs the aabbs
	 * @return the axis aligned b b[]
	 */
	public static AxisAlignedBB[] offset(BlockPos pos, AxisAlignedBB... aabbs)
	{
		if (ArrayUtils.isEmpty(aabbs))
			return aabbs;

		for (int i = 0; i < aabbs.length; i++)
			if (aabbs[i] != null)
				aabbs[i] = aabbs[i].offset(pos.getX(), pos.getY(), pos.getZ());
		return aabbs;
	}

	/**
	 * Checks if an {@link AxisAlignedBB} is colliding with an {@code AxisAlignedBB} array.
	 *
	 * @param aabb the aabb
	 * @param aabbs the aabbs
	 * @return true, if is colliding
	 */
	public static boolean isColliding(AxisAlignedBB aabb, AxisAlignedBB[] aabbs)
	{
		return isColliding(new AxisAlignedBB[] { aabb }, aabbs);
	}

	/**
	 * Checks if an {@link AxisAlignedBB} array is colliding with an {@code AxisAlignedBB}.
	 *
	 * @param aabbs the aabbs
	 * @param aabb the aabb
	 * @return true, if is colliding
	 */
	public static boolean isColliding(AxisAlignedBB[] aabbs, AxisAlignedBB aabb)
	{
		return isColliding(aabbs, new AxisAlignedBB[] { aabb });
	}

	/**
	 * Checks if an {@link AxisAlignedBB} array is colliding with another one.
	 *
	 * @param aabbs1 the aabbs1
	 * @param aabbs2 the aabbs2
	 * @return true, if is colliding
	 */
	public static boolean isColliding(AxisAlignedBB[] aabbs1, AxisAlignedBB[] aabbs2)
	{
		if (ArrayUtils.isEmpty(aabbs1) || ArrayUtils.isEmpty(aabbs2))
			return false;

		for (AxisAlignedBB aabb1 : aabbs1)
		{
			if (aabb1 != null)
			{
				for (AxisAlignedBB aabb2 : aabbs2)
					if (aabb2 != null && aabb1.intersectsWith(aabb2))
						return true;
			}
		}

		return false;
	}

	/**
	 * Gets the collision {@link AxisAlignedBB} for the {@link Block} as the {@link BlockPos} coordinates.
	 *
	 * @param world the world
	 * @param block the block
	 * @param pos the pos
	 * @return the collision bounding boxes
	 */
	public static AxisAlignedBB[] getCollisionBoundingBoxes(World world, Block block, BlockPos pos)
	{
		return getCollisionBoundingBoxes(world, new MBlockState(pos, block), false);
	}

	/**
	 * Gets the collision {@link AxisAlignedBB} for the {@link Block} as the {@link BlockPos} coordinates.
	 *
	 * @param world the world
	 * @param block the block
	 * @param pos the pos
	 * @param offset if true, the boxes are offset by the coordinate
	 * @return the collision bounding boxes
	 */
	public static AxisAlignedBB[] getCollisionBoundingBoxes(World world, Block block, BlockPos pos, boolean offset)
	{
		return getCollisionBoundingBoxes(world, new MBlockState(pos, block), offset);
	}

	/**
	 * Gets the collision {@link AxisAlignedBB} for the {@link MBlockState}.
	 *
	 * @param world the world
	 * @param state the state
	 * @return the collision bounding boxes
	 */
	public static AxisAlignedBB[] getCollisionBoundingBoxes(World world, MBlockState state)
	{
		return getCollisionBoundingBoxes(world, state, false);
	}

	/**
	 * Gets the collision {@link AxisAlignedBB} for the {@link MBlockState}.
	 *
	 * @param world the world
	 * @param state the state
	 * @param offset the offset
	 * @return the collision bounding boxes
	 */
	public static AxisAlignedBB[] getCollisionBoundingBoxes(World world, MBlockState state, boolean offset)
	{
		AxisAlignedBB[] aabbs = new AxisAlignedBB[0];
		if (world == null || state == null)
			return aabbs;

		if (state.getBlock() instanceof IBoundingBox)
			aabbs = ((IBoundingBox) state.getBlock()).getCollisionBoundingBoxes(world, state.getPos(), state.getBlockState());
		else
		{
			AxisAlignedBB aabb = state.getBlock().getCollisionBoundingBox(state.getBlockState(), world, state.getPos());
			if (aabb != null)
				aabbs = new AxisAlignedBB[] { aabb };
		}

		if (offset)
			AABBUtils.offset(state.getX(), state.getY(), state.getZ(), aabbs);

		return aabbs;
	}

	public static AxisAlignedBB[] slice(int slices, float fx[][], float fy[][], float fz[][], boolean vertical)
	{
		float delta = 1 / (float) slices;
		final int START = 0;
		final int MIN = 0;
		final int END = 1;
		final int MAX = 1;

		AxisAlignedBB[] aabb = new AxisAlignedBB[slices];
		for (int i = 0; i < slices; i++)
		{
			float bx = fx[START][MIN] + (fx[END][MIN] - fx[START][MIN]) * i * delta;
			float bX = fx[START][MAX] + (fx[END][MAX] - fx[START][MAX]) * i * delta;
			float by = fy[START][MIN] + (fy[END][MIN] - fy[START][MIN]) * i * delta;
			float bY = fy[START][MAX] + (fy[END][MAX] - fy[START][MAX]) * i * delta;
			float bz = fz[START][MIN] + (fz[END][MIN] - fz[START][MIN]) * i * delta;
			float bZ = fz[START][MAX] + (fz[END][MAX] - fz[START][MAX]) * i * delta;

			if (vertical)
			{
				by = i * delta;
				bY = by + delta;
			}
			else
			{
				bx = i * delta;
				bX = bx + delta;
			}

			aabb[i] = new AxisAlignedBB(bx, by, bz, bX, bY, bZ);
		}

		return aabb;
	}
}
