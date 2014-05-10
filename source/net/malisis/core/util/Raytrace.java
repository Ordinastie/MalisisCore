package net.malisis.core.util;

import java.util.HashMap;

import net.malisis.core.MalisisCore;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;

public class Raytrace
{
	/**
	 * Number of blocks before we consider ray trace failed
	 */
	private static int MAX_BLOCKS = 200;
	/**
	 * World object (needed for ray tracing inside each block)
	 */
	private World world;
	/**
	 * Source of the ray trace
	 */
	private Point src;
	/**
	 * Destination of tey ray trace
	 */
	private Point dest;
	/**
	 * Ray describing the ray trace
	 */
	private Ray ray;
	/**
	 * Vector describing the direction of steps to take when reaching limits of
	 * a block
	 */
	private Vector step;
	/**
	 * The block coordinates of the source
	 */
	private ChunkPosition blockSrc;
	/**
	 * The block coordinates of the destination
	 */
	private ChunkPosition blockDest;

	/**
	 * Current X coordinates of the block being ray traced
	 */
	private int currentX;
	/**
	 * Current Y coordinates of the block being ray traced
	 */
	private int currentY;
	/**
	 * Current Z coordinates of the block being ray traced
	 */
	private int currentZ;

	/**
	 * The first block to be hit. If ray trace reaches <code>dest</code> without
	 * any hit, <code>firstHit</code> will have <code>typeOfHit</code> =
	 * <b>MISS</b>
	 */
	public MovingObjectPosition firstHit;
	/**
	 * List of blocks passed by the ray trace. Only set if options
	 * <code>LOG_BLOCK_PASSED</code> is set
	 */
	public HashMap<ChunkPosition, MovingObjectPosition> blockPassed;

	/**
	 * Options of ray trace
	 */
	public int options = 0;

	public Raytrace(Ray ray, int options)
	{
		this.world = Minecraft.getMinecraft().theWorld;
		this.src = ray.origin;
		this.ray = ray;
		this.options = options;

		blockSrc = new ChunkPosition(src.toVec3());

		int stepX = 1, stepY = 1, stepZ = 1;
		if (ray.direction.x < 0)
			stepX = -1;
		if (ray.direction.y < 0)
			stepY = -1;
		if (ray.direction.z < 0)
			stepZ = -1;

		step = new Vector(stepX, stepY, stepZ);

		if (hasOption(Options.LOG_BLOCK_PASSED))
			blockPassed = new HashMap<ChunkPosition, MovingObjectPosition>();
	}

	public Raytrace(Ray ray)
	{
		this(ray, 0);
	}

	public Raytrace(Point src, Vector v, int options)
	{
		this(new Ray(src, v), options);
	}

	public Raytrace(Point src, Vector v)
	{
		this(new Ray(src, v), 0);
	}

	public Raytrace(Point src, Point dest, int options)
	{
		this(new Ray(src, new Vector(src, dest)), options);
		this.dest = dest;
		blockDest = new ChunkPosition(dest.toVec3());
	}

	public Raytrace(Point src, Point dest)
	{
		this(new Ray(src, new Vector(src, dest)), 0);
		this.dest = dest;
		blockDest = new ChunkPosition(dest.toVec3());
	}

	/**
	 * Get the direction vector of the ray
	 * 
	 * @return
	 */
	public Vector direction()
	{
		return ray.direction;
	}

	/**
	 * Get the length of the ray
	 * 
	 * @return
	 */
	public double distance()
	{
		return ray.direction.length();
	}

	/**
	 * Check if the option <code>opt</code> is set
	 * 
	 * @param opt
	 * @return
	 */
	public boolean hasOption(int opt)
	{
		return (options & opt) != 0;
	}

	/**
	 * Do the ray tracing.
	 * 
	 * @return <code>MovingObjectPosition</code> with <code>typeOfHit</code>
	 *         <b>BLOCK</b> if a ray hits a block in the way, or <b>MISS</b> if
	 *         it reaches <code>dest</code> without any hit
	 */
	public MovingObjectPosition trace()
	{
		MovingObjectPosition mop = null;
		double tX, tY, tZ, min;
		int count = 0;
		boolean ret = false;

		firstHit = null;
		currentX = blockSrc.chunkPosX;
		currentY = blockSrc.chunkPosY;
		currentZ = blockSrc.chunkPosZ;

		while (!ret && count++ <= MAX_BLOCKS)
		{
			tX = ray.intersectX(currentX + (ray.direction.x > 0 ? 1 : 0));
			tY = ray.intersectY(currentY + (ray.direction.y > 0 ? 1 : 0));
			tZ = ray.intersectZ(currentZ + (ray.direction.z > 0 ? 1 : 0));

			min = getMin(tX, tY, tZ);

			// do not trace first block
			if (count != 1 || !hasOption(Options.IGNORE_FIRST_BLOCK))
				mop = rayTraceBlock(currentX, currentY, currentZ, ray.getPointAt(min));
			if (firstHit == null)
				firstHit = mop;
			if (hasOption(Options.LOG_BLOCK_PASSED))
				blockPassed.put(new ChunkPosition(currentX, currentY, currentZ), mop);

			if (dest != null && currentX == blockDest.chunkPosX && currentY == blockDest.chunkPosY && currentZ == blockDest.chunkPosZ)
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
			firstHit = new MovingObjectPosition(currentX, currentY, currentZ, -1, dest.toVec3(), false);

		if (dest != null)
			MalisisCore.Message("Trace fail : " + MAX_BLOCKS + " passed (" + currentX + "," + currentY + "," + currentZ + ")");
		return firstHit;
	}

	/**
	 * Get the minimum value of <code>x</code>, <code>y</code>, <code>z</code>
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return <code>Double.NaN</code> if <code>x</code>, <code>y</code> and
	 *         <code>z</code> are all three are <code>Double.NaN</code>
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
	 * Ray trace inside an actual block area. Calls
	 * <code>Block.collisionRayTrace()</code>
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public MovingObjectPosition rayTraceBlock(int x, int y, int z, Point exit)
	{
		Block block = world.getBlock(x, y, z);
		int metadata = world.getBlockMetadata(x, y, z);
		if (block.getCollisionBoundingBoxFromPool(world, x, y, z) == null)
			return null;
		if (!block.canCollideCheck(metadata, hasOption(Options.HIT_LIQUIDS)))
			return null;

		return block.collisionRayTrace(world, x, y, z, src.toVec3(), exit.toVec3());
	}

	public static class Options
	{
		/**
		 * Ray tracing through liquids returns a hit
		 */
		public static int HIT_LIQUIDS = 1 << 0;
		/**
		 * Don't stop ray tracing on hit
		 */
		public static int PASS_THROUGH = 1 << 1;
		/**
		 * Don't hit the block source of ray tracing
		 */
		public static int IGNORE_FIRST_BLOCK = 1 << 2;
		/**
		 * Store list of blocks passed through ray trace
		 */
		public static int LOG_BLOCK_PASSED = 1 << 3;

	}

}
