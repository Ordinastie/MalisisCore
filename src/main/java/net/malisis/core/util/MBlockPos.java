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

import java.util.Iterator;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/**
 * @author Ordinastie
 *
 */
public class MBlockPos
{
	//1.8 BlockPos constants
	private static final int NUM_X_BITS = 26;
	private static final int NUM_Z_BITS = NUM_X_BITS;
	private static final int NUM_Y_BITS = 64 - NUM_X_BITS - NUM_Z_BITS;
	private static final int Y_SHIFT = 0 + NUM_Z_BITS;
	private static final int X_SHIFT = Y_SHIFT + NUM_Y_BITS;
	private static final long X_MASK = (1L << NUM_X_BITS) - 1L;
	private static final long Y_MASK = (1L << NUM_Y_BITS) - 1L;
	private static final long Z_MASK = (1L << NUM_Z_BITS) - 1L;

	protected int x;
	protected int y;
	protected int z;

	public MBlockPos(int x, int y, int z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public MBlockPos(double x, double y, double z)
	{
		this(MathHelper.floor_double(x), MathHelper.floor_double(y), MathHelper.floor_double(z));
	}

	public int getX()
	{
		return x;
	}

	public int getY()
	{
		return y;
	}

	public int getZ()
	{
		return z;
	}

	public int chunkX()
	{
		return x >> 4;
	}

	public int chunkZ()
	{
		return z >> 4;
	}

	public Block getBlock(World world)
	{
		return getState(world).getBlock();
	}

	public IBlockState getState(World world)
	{
		return world.getBlockState(this.toBlockPos());
	}

	/**
	 * Add the given coordinates to the coordinates of this BlockPos
	 *
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param z Z coordinate
	 */
	public MBlockPos add(int x, int y, int z)
	{
		return new MBlockPos(this.getX() + x, this.getY() + y, this.getZ() + z);
	}

	public MBlockPos add(MBlockPos pos)
	{
		if (pos == null)
			return new MBlockPos(getX(), getY(), getZ());
		return add(pos.getX(), pos.getY(), pos.getZ());
	}

	public MBlockPos substract(MBlockPos pos)
	{
		return add(-pos.getX(), -pos.getY(), -pos.getZ());
	}

	//#region Moves

	/**
	 * Offset this BlockPos 1 block up
	 */
	public MBlockPos up()
	{
		return this.up(1);
	}

	/**
	 * Offset this BlockPos n blocks up
	 */
	public MBlockPos up(int n)
	{
		return this.offset(EnumFacing.UP, n);
	}

	/**
	 * Offset this BlockPos 1 block down
	 */
	public MBlockPos down()
	{
		return this.down(1);
	}

	/**
	 * Offset this BlockPos n blocks down
	 */
	public MBlockPos down(int n)
	{
		return this.offset(EnumFacing.DOWN, n);
	}

	/**
	 * Offset this BlockPos 1 block in northern direction
	 */
	public MBlockPos north()
	{
		return this.north(1);
	}

	/**
	 * Offset this BlockPos n blocks in northern direction
	 */
	public MBlockPos north(int n)
	{
		return this.offset(EnumFacing.NORTH, n);
	}

	/**
	 * Offset this BlockPos 1 block in southern direction
	 */
	public MBlockPos south()
	{
		return this.south(1);
	}

	/**
	 * Offset this BlockPos n blocks in southern direction
	 */
	public MBlockPos south(int n)
	{
		return this.offset(EnumFacing.SOUTH, n);
	}

	/**
	 * Offset this BlockPos 1 block in western direction
	 */
	public MBlockPos west()
	{
		return this.west(1);
	}

	/**
	 * Offset this BlockPos n blocks in western direction
	 */
	public MBlockPos west(int n)
	{
		return this.offset(EnumFacing.WEST, n);
	}

	/**
	 * Offset this BlockPos 1 block in eastern direction
	 */
	public MBlockPos east()
	{
		return this.east(1);
	}

	/**
	 * Offset this BlockPos n blocks in eastern direction
	 */
	public MBlockPos east(int n)
	{
		return this.offset(EnumFacing.EAST, n);
	}

	/**
	 * Offset this BlockPos 1 block in the given direction
	 */
	public MBlockPos offset(EnumFacing facing)
	{
		return this.offset(facing, 1);
	}

	/**
	 * Offsets this BlockPos n blocks in the given direction
	 *
	 * @param facing The direction of the offset
	 * @param n The number of blocks to offset by
	 */
	public MBlockPos offset(EnumFacing facing, int n)
	{
		return new MBlockPos(this.getX() + facing.getFrontOffsetX() * n, this.getY() + facing.getFrontOffsetY() * n, this.getZ()
				+ facing.getFrontOffsetZ() * n);
	}

	//#end Moves

	public boolean isInRange(MBlockPos pos, int range)
	{
		double x = pos.x - this.x;
		double y = pos.y - this.y;
		double z = pos.z - this.z;
		return (x * x + y * y + z * z) <= range * range;
	}

	public boolean isInside(AxisAlignedBB aabb)
	{
		return aabb.intersectsWith(AABBUtils.identity(this.toBlockPos()));
	}

	public BlockPos toBlockPos()
	{
		return new BlockPos(chunkX(), y, chunkZ());
	}

	/**
	 * Serialize this BlockPos into a long value
	 */
	public long toLong()
	{
		return (this.getX() & X_MASK) << X_SHIFT | (this.getY() & Y_MASK) << Y_SHIFT | (this.getZ() & Z_MASK) << 0;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
			return true;

		if (!(obj instanceof MBlockPos))
			return false;

		MBlockPos pos = (MBlockPos) obj;
		return this.getX() != pos.getX() ? false : (this.getY() != pos.getY() ? false : this.getZ() == pos.getZ());
	}

	@Override
	public int hashCode()
	{
		return (this.getY() + this.getZ() * 31) * 31 + this.getX();
	}

	@Override
	public String toString()
	{
		return x + ", " + y + ", " + z;
	}

	/**
	 * Create a BlockPos from a serialized long value (created by toLong)
	 */
	public static MBlockPos fromLong(long serialized)
	{
		int j = (int) (serialized << 64 - X_SHIFT - NUM_X_BITS >> 64 - NUM_X_BITS);
		int k = (int) (serialized << 64 - Y_SHIFT - NUM_Y_BITS >> 64 - NUM_Y_BITS);
		int l = (int) (serialized << 64 - NUM_Z_BITS >> 64 - NUM_Z_BITS);
		return new MBlockPos(j, k, l);
	}

	public static MBlockPos minOf(MBlockPos p1, MBlockPos p2)
	{
		return new MBlockPos(Math.min(p1.getX(), p2.getX()), Math.min(p1.getY(), p2.getY()), Math.min(p1.getZ(), p2.getZ()));
	}

	public static MBlockPos maxOf(MBlockPos p1, MBlockPos p2)
	{
		return new MBlockPos(Math.max(p1.getX(), p2.getX()), Math.max(p1.getY(), p2.getY()), Math.max(p1.getZ(), p2.getZ()));
	}

	public static Iterable<MBlockPos> getAllInBox(AxisAlignedBB aabb)
	{
		return getAllInBox(new MBlockPos(aabb.minX, aabb.minY, aabb.minZ), new MBlockPos(Math.ceil(aabb.maxX) - 1,
				Math.ceil(aabb.maxY) - 1, Math.ceil(aabb.maxZ) - 1));
	}

	/**
	 * Create an {@link Iterable} that returns all positions in the box specified by the given corners.
	 *
	 * @param from the first corner
	 * @param to the second corner
	 * @return the iterable
	 */
	public static Iterable<MBlockPos> getAllInBox(MBlockPos from, MBlockPos to)
	{
		return new BlockIterator(from, to).asIterable();
	}

	public static class BlockIterator implements Iterator<MBlockPos>
	{
		private MBlockPos from;
		private MBlockPos to;

		private int x;
		private int y;
		private int z;

		public BlockIterator(MBlockPos from, MBlockPos to)
		{
			this.from = minOf(from, to);
			this.to = maxOf(from, to);

			x = from.getX();
			y = from.getY();
			z = from.getZ();
		}

		@Override
		public boolean hasNext()
		{
			return x <= to.getX() && y <= to.getY() && z <= to.getZ();
		}

		@Override
		public MBlockPos next()
		{
			MBlockPos retVal = hasNext() ? new MBlockPos(x, y, z) : null;
			x++;
			if (x > to.getX())
			{
				x = from.getX();
				y++;
				if (y > to.getY())
				{
					y = from.getY();
					z++;
				}
			}
			return retVal;
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}

		public Iterable<MBlockPos> asIterable()
		{
			return new Iterable<MBlockPos>()
			{
				@Override
				public Iterator<MBlockPos> iterator()
				{
					return BlockIterator.this;
				}
			};
		}

	}

}