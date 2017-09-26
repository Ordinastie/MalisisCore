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

package net.malisis.core.util.raytrace;

import java.util.HashMap;

import org.apache.commons.lang3.tuple.Pair;

import net.malisis.core.MalisisCore;
import net.malisis.core.util.Point;
import net.malisis.core.util.Ray;
import net.malisis.core.util.Vector;
import net.malisis.core.util.chunkcollision.ChunkCollision;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

/**
 * RayTrace class that offers more control to handle raytracing.
 *
 * @author Ordinastie
 *
 */
public class RaytraceWorld extends Raytrace
{
	/** Number of blocks before we consider ray trace failed. */
	private static final int MAX_BLOCKS = 200;
	/** World object (needed for ray tracing inside each block). */
	private World world;
	/** Vector describing the direction of steps to take when reaching limits of a block. */
	private Vector step;
	/** The block coordinates of the source. */
	private BlockPos blockSrc;
	/** The block coordinates of the destination. */
	private BlockPos blockDest;

	/** List of blocks passed by the ray trace. Only set if options <code>LOG_BLOCK_PASSED</code> is set */
	public HashMap<BlockPos, RayTraceResult> blockPassed;
	/** Options for the ray tracing. */
	public int options = 0;

	/**
	 * Instantiates a new {@link RaytraceWorld}.
	 *
	 * @param ray the ray
	 * @param options the options
	 */
	public RaytraceWorld(World world, Ray ray, int options)
	{
		super(ray);
		this.world = world;
		this.options = options;

		blockSrc = new BlockPos(src.toVec3d());

		int stepX = 1, stepY = 1, stepZ = 1;
		if (ray.direction.x < 0)
			stepX = -1;
		if (ray.direction.y < 0)
			stepY = -1;
		if (ray.direction.z < 0)
			stepZ = -1;

		step = new Vector(stepX, stepY, stepZ);

		if (hasOption(Options.LOG_BLOCK_PASSED))
			blockPassed = new HashMap<>();
	}

	/**
	 * Instantiates a new {@link RaytraceWorld}.
	 *
	 * @param ray the ray
	 */
	public RaytraceWorld(World world, Ray ray)
	{
		this(world, ray, 0);
	}

	/**
	 * Instantiates a new {@link RaytraceWorld}.
	 *
	 * @param src the src
	 * @param v the v
	 * @param options the options
	 */
	public RaytraceWorld(World world, Point src, Vector v, int options)
	{
		this(world, new Ray(src, v), options);
	}

	/**
	 * Instantiates a new {@link RaytraceWorld}.
	 *
	 * @param src the src
	 * @param v the v
	 */
	public RaytraceWorld(World world, Point src, Vector v)
	{
		this(world, new Ray(src, v), 0);
	}

	/**
	 * Instantiates a new {@link RaytraceWorld}.
	 *
	 * @param src the src
	 * @param dest the dest
	 * @param options the options
	 */
	public RaytraceWorld(World world, Point src, Point dest, int options)
	{
		this(world, new Ray(src, new Vector(src, dest)), options);
		this.dest = dest;
		blockDest = new BlockPos(dest.toVec3d());
	}

	/**
	 * Instantiates a new {@link RaytraceWorld}.
	 *
	 * @param src the src
	 * @param dest the dest
	 */
	public RaytraceWorld(World world, Point src, Point dest)
	{
		this(world, new Ray(src, new Vector(src, dest)), 0);
		this.dest = dest;
		blockDest = new BlockPos(dest.toVec3d());
	}

	/**
	 * Sets the length of this {@link RaytraceWorld}.
	 *
	 * @param length the new length
	 */
	@Override
	public void setLength(double length)
	{
		super.setLength(length);
		blockDest = new BlockPos(dest.toVec3d());
	}

	/**
	 * Checks if the option <code>opt</code> is set.
	 *
	 * @param opt the option to check
	 * @return true, if option is present, false otherwise
	 */
	public boolean hasOption(int opt)
	{
		return (options & opt) != 0;
	}

	/**
	 * Does the raytracing.
	 *
	 * @return {@link RayTraceResult} with <code>typeOfHit</code> <b>BLOCK</b> if a ray hits a block in the way, or <b>MISS</b> if it
	 *         reaches <code>dest</code> without any hit
	 */
	public RayTraceResult trace()
	{
		RayTraceResult mop = null, firstHit = null;
		double tX, tY, tZ, min;
		int count = 0;
		boolean ret = false;

		int currentX = blockSrc.getX();
		int currentY = blockSrc.getY();
		int currentZ = blockSrc.getZ();

		while (!ret && count++ <= MAX_BLOCKS)
		{
			tX = ray.intersectX(currentX + (ray.direction.x > 0 ? 1 : 0));
			tY = ray.intersectY(currentY + (ray.direction.y > 0 ? 1 : 0));
			tZ = ray.intersectZ(currentZ + (ray.direction.z > 0 ? 1 : 0));

			min = getMin(tX, tY, tZ);

			// do not trace first block
			if (count != 1 || !hasOption(Options.IGNORE_FIRST_BLOCK))
				mop = rayTraceBlock(new BlockPos(currentX, currentY, currentZ), ray.getPointAt(min));
			if (firstHit == null)
				firstHit = mop;
			if (hasOption(Options.LOG_BLOCK_PASSED))
				blockPassed.put(new BlockPos(currentX, currentY, currentZ), mop);

			if (dest != null && currentX == blockDest.getX() && currentY == blockDest.getY() && currentZ == blockDest.getZ())
				ret = true;

			if (!ret)
			{
				if (min == tX)
					currentX += step.x;
				if (min == tY)
					currentY += step.y;
				if (min == tZ)
					currentZ += step.z;
			}

			if (dest != null && dest.equals(ray.getPointAt(min)))
				ret = true;

		}

		if (firstHit == null && dest != null)
			firstHit = new RayTraceResult(RayTraceResult.Type.MISS, dest.toVec3d(), null, new BlockPos(currentX, currentY, currentZ));

		firstHit = ChunkCollision.get().getRayTraceResult(	world,
															Pair.of(src, dest),
															firstHit,
															hasOption(Options.HIT_LIQUIDS),
															hasOption(Options.CHECK_COLLISION),
															true);

		if (!ret)
			MalisisCore.message("Trace fail : " + MAX_BLOCKS + " blocks passed (" + currentX + "," + currentY + "," + currentZ + ")");
		return firstHit;
	}

	/**
	 * Gets the minimum value of <code>x</code>, <code>y</code>, <code>z</code>.
	 *
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @return <code>Double.NaN</code> if <code>x</code>, <code>y</code> and <code>z</code> are all three <code>Double.NaN</code>
	 */
	public double getMin(double x, double y, double z)
	{
		double ret = Double.NaN;
		if (!Double.isNaN(x))
			ret = x;
		if (!Double.isNaN(y))
		{
			if (!Double.isNaN(ret))
				ret = Math.min(ret, y);
			else
				ret = y;
		}
		if (!Double.isNaN(z))
		{
			if (!Double.isNaN(ret))
				ret = Math.min(ret, z);
			else
				ret = z;
		}
		return ret;
	}

	/**
	 * Raytraces inside an actual block area. Calls
	 * {@link Block#collisionRayTrace(IBlockState, World, BlockPos, net.minecraft.util.math.Vec3d, net.minecraft.util.math.Vec3d)}
	 *
	 * @param pos the pos
	 * @param exit the exit
	 * @return the {@link RayTraceResult} return by block raytrace
	 */
	public RayTraceResult rayTraceBlock(BlockPos pos, Point exit)
	{
		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		//TODO: fix getBoundingBox for IBoundingBox ?
		if (hasOption(Options.CHECK_COLLISION) && state.getBoundingBox(world, pos) == null)
			return null;
		if (!block.canCollideCheck(state, hasOption(Options.HIT_LIQUIDS)))
			return null;
		return new RaytraceBlock(world, src, exit, pos).trace();
	}

	/**
	 * The Class Options.
	 */
	public static class Options
	{
		/** Ray tracing through liquids returns a hit. */
		public static int HIT_LIQUIDS = 1;
		/** Don't stop ray tracing on hit. */
		public static int PASS_THROUGH = 1 << 1;
		/** Don't hit the block source of ray tracing. */
		public static int IGNORE_FIRST_BLOCK = 1 << 2;
		/** Stores list of blocks passed through ray trace. */
		public static int LOG_BLOCK_PASSED = 1 << 3;
		/** Whether a block has to have a collision bounding box to rayTrace it. */
		public static int CHECK_COLLISION = 1 << 5;

	}

}
