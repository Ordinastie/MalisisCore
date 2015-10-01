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

import net.malisis.core.block.BoundingBoxType;
import net.malisis.core.block.MalisisBlock;
import net.malisis.core.util.chunkcollision.IChunkCollidable;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

/**
 * @author Ordinastie
 *
 */
public class AABBUtils
{
	public static enum Axis
	{
		X, Y, Z
	};

	private static int[] cos = { 1, 0, -1, 0 };
	private static int[] sin = { 0, 1, 0, -1 };

	public static AxisAlignedBB empty()
	{
		return empty(new BlockPos(0, 0, 0));
	}

	public static AxisAlignedBB empty(BlockPos pos)
	{
		return new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
	}

	public static AxisAlignedBB identity()
	{
		return identity(new BlockPos(0, 0, 0));
	}

	public static AxisAlignedBB identity(BlockPos pos)
	{
		return new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
	}

	public static AxisAlignedBB[] identities()
	{
		return identities(new BlockPos(0, 0, 0));
	}

	public static AxisAlignedBB[] identities(BlockPos pos)
	{
		return new AxisAlignedBB[] { identity(pos) };
	}

	public static AxisAlignedBB copy(AxisAlignedBB aabb)
	{
		return new AxisAlignedBB(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
	}

	/**
	 * Rotate the {@link AxisAlignedBB} based on the specified direction.<br>
	 * Assumes {@link ForgeDirection#NORTH} to be the default non rotated direction.<br>
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

	public static AxisAlignedBB[] rotate(AxisAlignedBB[] aabbs, EnumFacing dir)
	{
		return rotate(aabbs, EnumFacingUtils.getRotationCount(dir));
	}

	public static AxisAlignedBB[] rotate(AxisAlignedBB[] aabbs, int angle)
	{
		for (int i = 0; i < aabbs.length; i++)
			aabbs[i] = rotate(aabbs[i], angle);
		return aabbs;
	}

	public static AxisAlignedBB rotate(AxisAlignedBB aabb, int angle)
	{
		return rotate(aabb, angle, Axis.Y);
	}

	public static AxisAlignedBB rotate(AxisAlignedBB aabb, int angle, Axis axis)
	{
		if (aabb == null)
			return null;

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

	public static AxisAlignedBB readFromNBT(NBTTagCompound tag)
	{
		return new AxisAlignedBB(tag.getDouble("minX"), tag.getDouble("minY"), tag.getDouble("minZ"), tag.getDouble("maxX"),
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

	/**
	 * Gets a {@link AxisAlignedBB} that englobes the passed {@code AxisAlignedBB}.
	 *
	 * @param aabbs the aabbs
	 * @return the axis aligned bb
	 */
	public static AxisAlignedBB combine(AxisAlignedBB[] aabbs)
	{
		if (aabbs == null || aabbs.length == 0)
			return null;

		AxisAlignedBB ret = null;
		for (AxisAlignedBB aabb : aabbs)
		{
			if (ret == null)
				ret = aabb;
			else if (aabb != null)
				ret.union(aabb);
		}

		return ret;
	}

	/**
	 * Offsets the passed {@link AxisAlignedBB}s by the specified coordinates.
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

	public static AxisAlignedBB offset(BlockPos pos, AxisAlignedBB aabb)
	{
		return aabb.offset(pos.getX(), pos.getY(), pos.getZ());
	}

	public static AxisAlignedBB[] offset(BlockPos pos, AxisAlignedBB... aabbs)
	{
		if (aabbs == null)
			return null;

		for (int i = 0; i < aabbs.length; i++)
			if (aabbs[i] != null)
				aabbs[i] = aabbs[i].offset(pos.getX(), pos.getY(), pos.getZ());
		return aabbs;
	}

	public static boolean isColliding(AxisAlignedBB aabb, AxisAlignedBB[] aabbs)
	{
		return isColliding(new AxisAlignedBB[] { aabb }, aabbs);
	}

	public static boolean isColliding(AxisAlignedBB[] aabbs, AxisAlignedBB aabb)
	{
		return isColliding(aabbs, new AxisAlignedBB[] { aabb });
	}

	/**
	 * Checks if a group of {@link AxisAlignedBB} is colliding with another one.
	 *
	 * @param aabbs1 the aabbs1
	 * @param aabbs2 the aabbs2
	 * @return true, if is colliding
	 */
	public static boolean isColliding(AxisAlignedBB[] aabbs1, AxisAlignedBB[] aabbs2)
	{
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
	 * Gets the collision bounding boxes.
	 *
	 * @param world the world
	 * @param block the block
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @return the collision bounding boxes
	 */
	public static AxisAlignedBB[] getCollisionBoundingBoxes(World world, Block block, BlockPos pos)
	{
		return getCollisionBoundingBoxes(world, new MBlockState(pos, block), false);
	}

	/**
	 * Gets the collision bounding boxes for the block.
	 *
	 * @param world the world
	 * @param block the block
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @param offset if true, the boxes are offset by the coordinate
	 * @return the collision bounding boxes
	 */
	public static AxisAlignedBB[] getCollisionBoundingBoxes(World world, Block block, BlockPos pos, boolean offset)
	{
		return getCollisionBoundingBoxes(world, new MBlockState(pos, block), offset);
	}

	/**
	 * Gets the collision bounding boxes.
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
	 * Gets the collision bounding boxes for the state.
	 *
	 * @param world the world
	 * @param state the state
	 * @return the collision bounding boxes
	 */
	public static AxisAlignedBB[] getCollisionBoundingBoxes(World world, MBlockState state, boolean offset)
	{
		AxisAlignedBB[] aabbs = new AxisAlignedBB[0];

		if (state.getBlock() instanceof IChunkCollidable)
			aabbs = ((IChunkCollidable) state.getBlock()).getBoundingBox(world, state.getPos(), BoundingBoxType.CHUNKCOLLISION);
		else if (state.getBlock() instanceof MalisisBlock)
			aabbs = ((MalisisBlock) state.getBlock()).getBoundingBox(world, state.getPos(), BoundingBoxType.CHUNKCOLLISION);
		else
		{
			AxisAlignedBB aabb = state.getBlock().getCollisionBoundingBox(world, state.getPos(), state.getBlockState());
			if (aabb != null)
				aabbs = new AxisAlignedBB[] { aabb.offset(-state.getX(), -state.getY(), -state.getZ()) };
		}

		if (offset)
			AABBUtils.offset(state.getX(), state.getY(), state.getZ(), aabbs);

		return aabbs;
	}
}
