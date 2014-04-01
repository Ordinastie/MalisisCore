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

	public Raytrace(Point src, Point dest, int options)
	{
		this.world = Minecraft.getMinecraft().theWorld;
		this.src = src;
		this.dest = dest;
		this.ray = new Ray(src, new Vector(src, dest));
		this.options = options;

		blockSrc = new ChunkPosition(src.toVec3());
		blockDest = new ChunkPosition(dest.toVec3());

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

	public Raytrace(Point src, Point dest)
	{
		this(src, dest, 0);
	}

	public Raytrace(double srcX, double srcY, double srcZ, double destX, double destY, double destZ)
	{
		this(new Point(srcX, srcY, srcZ), new Point(destX, destY, destZ), 0);
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
		firstHit = null;
		currentX = blockSrc.chunkPosX;
		currentY = blockSrc.chunkPosY;
		currentZ = blockSrc.chunkPosZ;
		int count = 0;
		Point exit = null;
		boolean ret = false;
		while (count++ <= MAX_BLOCKS)
		{
			// do not trace first block
			if (count != 1 || !hasOption(Options.IGNORE_FIRST_BLOCK))
				mop = rayTraceBlock(currentX, currentY, currentZ);
			if (firstHit == null)
				firstHit = mop;
			if (hasOption(Options.LOG_BLOCK_PASSED))
				blockPassed.put(new ChunkPosition(currentX, currentY, currentZ), mop);

			if (currentX == blockDest.chunkPosX && currentY == blockDest.chunkPosY && currentZ == blockDest.chunkPosZ)
				ret = true;

			if (!ret)
				exit = nextBlock();

			if (dest.equals(exit) || ret == true)
			{
				if (firstHit == null)
					firstHit = new MovingObjectPosition(currentX, currentY, currentZ, -1, dest.toVec3(), false);

				return firstHit;
			}

		}

		MalisisCore.Message("Trace fail : " + MAX_BLOCKS + " passed (" + currentX + "," + currentY + "," + currentZ + ")");
		return null;
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
	 * Calculate the next block the ray passes through
	 * 
	 * @return
	 */
	public Point nextBlock()
	{
		double tX = ray.intersectX(currentX + (ray.direction.x > 0 ? 1 : 0));
		double tY = ray.intersectY(currentY + (ray.direction.y > 0 ? 1 : 0));
		double tZ = ray.intersectZ(currentZ + (ray.direction.z > 0 ? 1 : 0));

		double min = getMin(tX, tY, tZ);
		if (min == tX)
			currentX += step.x;
		if (min == tY)
			currentY += step.y;
		if (min == tZ)
			currentZ += step.z;

		return ray.getPointAt(min);
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
	public MovingObjectPosition rayTraceBlock(int x, int y, int z)
	{
		Block block = world.getBlock(x, y, z);
		int metadata = world.getBlockMetadata(x, y, z);
		if (block.getCollisionBoundingBoxFromPool(world, x, y, z) == null)
			return null;
		if (!block.canCollideCheck(metadata, hasOption(Options.HIT_LIQUIDS)))
			return null;

		return block.collisionRayTrace(world, x, y, z, src.toVec3(), dest.toVec3());
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
