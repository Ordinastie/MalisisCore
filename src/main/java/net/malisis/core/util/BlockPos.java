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

import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkPosition;

/**
 * @author Ordinastie
 *
 */
public class BlockPos
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

	public BlockPos(int x, int y, int z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public BlockPos(double x, double y, double z)
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

	/**
	 * Add the given coordinates to the coordinates of this BlockPos
	 *
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param z Z coordinate
	 */
	public BlockPos add(int x, int y, int z)
	{
		return new BlockPos(this.getX() + x, this.getY() + y, this.getZ() + z);
	}

	public BlockPos add(BlockPos pos)
	{
		return add(pos.getX(), pos.getY(), pos.getZ());
	}

	public BlockPos substract(BlockPos pos)
	{
		return add(-pos.getX(), -pos.getY(), -pos.getZ());
	}

	//#region Moves

	/**
	 * Offset this BlockPos 1 block up
	 */
	public BlockPos up()
	{
		return this.up(1);
	}

	/**
	 * Offset this BlockPos n blocks up
	 */
	public BlockPos up(int n)
	{
		return this.offset(EnumFacing.UP, n);
	}

	/**
	 * Offset this BlockPos 1 block down
	 */
	public BlockPos down()
	{
		return this.down(1);
	}

	/**
	 * Offset this BlockPos n blocks down
	 */
	public BlockPos down(int n)
	{
		return this.offset(EnumFacing.DOWN, n);
	}

	/**
	 * Offset this BlockPos 1 block in northern direction
	 */
	public BlockPos north()
	{
		return this.north(1);
	}

	/**
	 * Offset this BlockPos n blocks in northern direction
	 */
	public BlockPos north(int n)
	{
		return this.offset(EnumFacing.NORTH, n);
	}

	/**
	 * Offset this BlockPos 1 block in southern direction
	 */
	public BlockPos south()
	{
		return this.south(1);
	}

	/**
	 * Offset this BlockPos n blocks in southern direction
	 */
	public BlockPos south(int n)
	{
		return this.offset(EnumFacing.SOUTH, n);
	}

	/**
	 * Offset this BlockPos 1 block in western direction
	 */
	public BlockPos west()
	{
		return this.west(1);
	}

	/**
	 * Offset this BlockPos n blocks in western direction
	 */
	public BlockPos west(int n)
	{
		return this.offset(EnumFacing.WEST, n);
	}

	/**
	 * Offset this BlockPos 1 block in eastern direction
	 */
	public BlockPos east()
	{
		return this.east(1);
	}

	/**
	 * Offset this BlockPos n blocks in eastern direction
	 */
	public BlockPos east(int n)
	{
		return this.offset(EnumFacing.EAST, n);
	}

	/**
	 * Offset this BlockPos 1 block in the given direction
	 */
	public BlockPos offset(EnumFacing facing)
	{
		return this.offset(facing, 1);
	}

	/**
	 * Offsets this BlockPos n blocks in the given direction
	 *
	 * @param facing The direction of the offset
	 * @param n The number of blocks to offset by
	 */
	public BlockPos offset(EnumFacing facing, int n)
	{
		return new BlockPos(this.getX() + facing.getFrontOffsetX() * n, this.getY() + facing.getFrontOffsetY() * n, this.getZ()
				+ facing.getFrontOffsetZ() * n);
	}

	//#end Moves

	public boolean isInRange(BlockPos pos, int range)
	{
		double x = pos.x - this.x;
		double y = pos.y - this.y;
		double z = pos.z - this.z;
		return (x * x + y * y + z * z) <= range * range;
	}

	public ChunkPosition toChunkPosition()
	{
		return new ChunkPosition(chunkX(), y, chunkZ());
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

		if (!(obj instanceof BlockPos))
			return false;

		BlockPos pos = (BlockPos) obj;
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
	public static BlockPos fromLong(long serialized)
	{
		int j = (int) (serialized << 64 - X_SHIFT - NUM_X_BITS >> 64 - NUM_X_BITS);
		int k = (int) (serialized << 64 - Y_SHIFT - NUM_Y_BITS >> 64 - NUM_Y_BITS);
		int l = (int) (serialized << 64 - NUM_Z_BITS >> 64 - NUM_Z_BITS);
		return new BlockPos(j, k, l);
	}

	public static BlockPos minOf(BlockPos p1, BlockPos p2)
	{
		return new BlockPos(Math.min(p1.getX(), p2.getX()), Math.min(p1.getY(), p2.getY()), Math.min(p1.getZ(), p2.getZ()));
	}

	public static BlockPos maxOf(BlockPos p1, BlockPos p2)
	{
		return new BlockPos(Math.max(p1.getX(), p2.getX()), Math.max(p1.getY(), p2.getY()), Math.max(p1.getZ(), p2.getZ()));
	}

	public static Iterable<BlockPos> getAllInBox(AxisAlignedBB aabb)
	{
		AABBUtils.fix(aabb);
		return getAllInBox(new BlockPos(aabb.minX, aabb.minY, aabb.minZ), new BlockPos(Math.ceil(aabb.maxX) - 1, Math.ceil(aabb.maxY) - 1,
				Math.ceil(aabb.maxZ) - 1));
	}

	/**
	 * Create an {@link Iterable} that returns all positions in the box specified by the given corners.
	 *
	 * @param from the first corner
	 * @param to the second corner
	 * @return the iterable
	 */
	public static Iterable<BlockPos> getAllInBox(BlockPos from, BlockPos to)
	{
		return new BlockIterator(from, to).asIterable();
	}

	public static class BlockIterator implements Iterator<BlockPos>
	{
		private BlockPos from;
		private BlockPos to;

		private int x;
		private int y;
		private int z;

		public BlockIterator(BlockPos from, BlockPos to)
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
		public BlockPos next()
		{
			BlockPos retVal = hasNext() ? new BlockPos(x, y, z) : null;
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

		public Iterable<BlockPos> asIterable()
		{
			return new Iterable<BlockPos>()
			{
				@Override
				public Iterator<BlockPos> iterator()
				{
					return BlockIterator.this;
				}
			};
		}

	}

}