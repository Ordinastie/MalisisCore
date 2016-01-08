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

import java.lang.ref.WeakReference;

import net.malisis.core.block.IBoundingBox;
import net.malisis.core.util.AABBUtils;
import net.malisis.core.util.Point;
import net.malisis.core.util.Ray;
import net.malisis.core.util.Vector;
import net.minecraft.block.Block;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import org.apache.commons.lang3.tuple.Pair;

/**
 * RayTrace class that offers more control to handle raytracing.
 *
 * @author Ordinastie
 *
 */
public class RaytraceBlock extends Raytrace
{
	/** World reference **/
	private WeakReference<World> world;
	/** Position of the block being ray traced **/
	private BlockPos pos;
	/** Block being ray traced. */
	private Block block;

	/**
	 * Sets the parameters for this {@link RaytraceBlock}.
	 *
	 * @param world the world
	 * @param ray the ray
	 * @param pos the pos
	 */
	public RaytraceBlock(World world, Ray ray, BlockPos pos)
	{
		super(ray);
		this.world = new WeakReference<World>(world);
		this.pos = pos;
		this.block = world().getBlockState(pos).getBlock();
	}

	/**
	 * Sets the parameters for this {@link RaytraceBlock}.
	 *
	 * @param world the world
	 * @param src the src
	 * @param v the v
	 * @param pos the pos
	 */
	public RaytraceBlock(World world, Point src, Vector v, BlockPos pos)
	{
		this(world, new Ray(src, v), pos);
	}

	/**
	 * Sets the parameters for this {@link RaytraceBlock}.
	 *
	 * @param world the world
	 * @param src the src
	 * @param dest the dest
	 * @param pos the pos
	 */
	public RaytraceBlock(World world, Point src, Point dest, BlockPos pos)
	{
		this(world, new Ray(src, new Vector(src, dest)), pos);
		this.dest = dest;
	}

	/**
	 * Sets the parameters for this {@link RaytraceBlock}.
	 *
	 * @param world the world
	 * @param src the src
	 * @param dest the dest
	 * @param pos the pos
	 */
	public RaytraceBlock(World world, Vec3 src, Vec3 dest, BlockPos pos)
	{
		this(world, new Ray(src, dest), pos);
		this.dest = new Point(dest);
	}

	/**
	 * Get the world used by this {@link RaytraceBlock}.
	 *
	 * @return the world
	 */
	public World world()
	{
		return world.get();
	}

	/**
	 * Does the raytracing.
	 *
	 * @return {@link MovingObjectPosition} with <code>typeOfHit</code> <b>BLOCK</b> if a ray hits a block in the way, or <b>MISS</b> if it
	 *         reaches <code>dest</code> without any hit
	 */
	public MovingObjectPosition trace()
	{
		if (!(block instanceof IBoundingBox))
			return block.collisionRayTrace(world(), pos, ray.origin.toVec3(), dest.toVec3());

		IBoundingBox block = (IBoundingBox) this.block;
		AxisAlignedBB[] aabbs = block.getRayTraceBoundingBox(world(), pos, world().getBlockState(pos));
		Pair<EnumFacing, Point> closest = super.trace(AABBUtils.offset(pos, aabbs));
		if (closest == null)
			return null;

		return new MovingObjectPosition(closest.getRight().toVec3(), closest.getLeft(), pos);
	}
}
