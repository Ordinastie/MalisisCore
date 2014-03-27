package net.malisis.core;

import net.minecraft.block.Block;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.IBlockAccess;

public class RayTrace
{
	private static int maxBlocksPassed = 200;
	private IBlockAccess world;
	private Vec3 src;
	private Vec3 dest;
	private ChunkPosition blockSrc;
	private ChunkPosition blockDest;
	public ChunkPosition[] blockPassed = new ChunkPosition[maxBlocksPassed];

	public int options = 0;
	public int length = 200;

	public boolean debug = false;

	public RayTrace(IBlockAccess world, Vec3 src, Vec3 dest)
	{
		this.world = world;
		this.src = src;
		this.dest = dest;

		blockSrc = new ChunkPosition(src);
		blockDest = new ChunkPosition(dest);
	}

	public RayTrace(IBlockAccess world, double srcX, double srcY, double srcZ, double destX, double destY, double destZ)
	{
		this(world, world.getWorldVec3Pool().getVecFromPool(srcX, srcY, srcZ), world.getWorldVec3Pool().getVecFromPool(destX, destY, destZ));
	}

	public boolean hasOption(int opt)
	{
		return (options & opt) != 0;
	}

	public ChunkPosition nextBlock(ChunkPosition block, MovingObjectPosition mop)
	{
		int x = block.chunkPosX;
		int y = block.chunkPosY;
		int z = block.chunkPosZ;
		
		int[] sides;
		if(mop.hitInfo != null)
			sides = (int[]) mop.hitInfo;
		else
			sides = new int[] { mop.sideHit };

		for(int side : sides)
		{
			switch (side)
			{
				case 4:
					x--;
					break;
				case 5:
					x++;
					break;
				case 0:
					y--;
					break;
				case 1:
					y++;
					break;
				case 2:
					z--;
					break;
				case 3:
					z++;
					break;
				default:
			}
		}

		return new ChunkPosition(x, y, z);
	}

	public boolean isBetween(double value, double min, double max)
	{
		return value >= min && value <= max;
	}
	public boolean vec3Equals(Vec3 v1, Vec3 v2)
	{
		if(v1 == null || v2 == null)
			return false;
		return v1.xCoord == v2.xCoord && v1.yCoord == v2.yCoord && v1.zCoord == v2.zCoord;
	}

	public MovingObjectPosition[] traceBlock(ChunkPosition position, Vec3 start, Vec3 end)
	{
		int x = position.chunkPosX;
		int y = position.chunkPosY;
		int z = position.chunkPosZ;
		
		double bx = 0;
		double bX = 1;
		double by = 0;
		double bY = 1;
		double bz = 0;
		double bZ = 1;

		Block block = world.getBlock(x, y, z);
		
		start = start.addVector((double) (-x), (double) (-y), (double) (-z));
		end = end.addVector((double) (-x), (double) (-y), (double) (-z));
		
		if(hasOption(Options.HIT_BLOCKS))
		{
			//block bounds lower = min, upper = max
			bx = block.getBlockBoundsMinX();
			bX = block.getBlockBoundsMaxX();
			by = block.getBlockBoundsMinY();
			bY = block.getBlockBoundsMaxY();
			bz = block.getBlockBoundsMinZ();
			bZ = block.getBlockBoundsMaxZ();
		}
		
		Vec3 minX = start.getIntermediateWithXValue(end, bx);
		Vec3 maxX = start.getIntermediateWithXValue(end, bX);
		Vec3 minY = start.getIntermediateWithYValue(end, by);
		Vec3 maxY = start.getIntermediateWithYValue(end, bY);
		Vec3 minZ = start.getIntermediateWithZValue(end, bz);
		Vec3 maxZ = start.getIntermediateWithZValue(end, bZ);

		if(minX != null && (!isBetween(minX.yCoord, by, bY) || !isBetween(minX.zCoord, bz, bZ)))
			minX = null;

		if(maxX != null && (!isBetween(maxX.yCoord, by, bY) || !isBetween(maxX.zCoord, bz, bZ)))
			maxX = null;

		if(minY != null && (!isBetween(minY.xCoord, bx, bX) || !isBetween(minY.zCoord, bz, bZ)))
			minY = null;

		if(maxY != null && (!isBetween(maxY.xCoord, bx, bX) || !isBetween(maxY.zCoord, bz, bZ)))
			maxY = null;
	
		if(minZ != null && (!isBetween(minZ.xCoord, bx, bX) || !isBetween(minZ.yCoord, by, bY)))
			minZ = null;

		if(maxZ != null && (!isBetween(maxZ.xCoord, bx, bX) || !isBetween(maxZ.yCoord, by, bY)))
			maxZ = null;

		Vec3[] entryExit = getEntrYAndExit(start, new Vec3[] { minX, maxX, minY, maxY, minZ, maxZ });
		MovingObjectPosition[] ret = new MovingObjectPosition[2];
		int i = 0;
		for(Vec3 v : entryExit)
		{
			if(v != null)
			{
				int[] sides = new int[2];
				int sideCount = 0;
				if(vec3Equals(v,minX))
					sides[sideCount++] = 4;
				if (vec3Equals(v, maxX))
					sides[sideCount++] = 5;
				if (vec3Equals(v, minY))
					sides[sideCount++] = 0;
				if (vec3Equals(v, maxY))
					sides[sideCount++] = 1;
				if (vec3Equals(v, minZ))
					sides[sideCount++] = 2;
				if (vec3Equals(v, maxZ))
					sides[sideCount++] = 3;
				
				int side = -1;
				if(sideCount > 0)
					side = sides[0];
				
				MovingObjectPosition mop = new MovingObjectPosition(x, y, z, side, v.addVector((double) x, (double) y, (double) z), !block.isAir(world, x, y, z));
				if(sideCount > 1)
					mop.hitInfo = sides;
				ret[i++] = mop;
			}
		}
		
		return ret;
	}

	private Vec3[] getEntrYAndExit(Vec3 start, Vec3[] intersections)
	{
		Vec3 entry = null;
		Vec3 exit = null;
		for (Vec3 v : intersections)
		{
			if (v != null && (entry == null || start.squareDistanceTo(v) < start.squareDistanceTo(entry)))
				entry = v;
			if (v != null && (exit == null || start.squareDistanceTo(v) > start.squareDistanceTo(exit)))
				exit = v;
		}

		return new Vec3[] { entry, exit };
	}

	public boolean trace()
	{
		if (hasOption(Options.HIT_FIRST_BLOCK) && src.equals(dest))
		{
			return true;
		}

		ChunkPosition currentBlock = new ChunkPosition(src);
		Vec3 currentSrc = world.getWorldVec3Pool().getVecFromPool(src.xCoord, src.yCoord, src.zCoord);
		MovingObjectPosition[] mop;
		int count = 0;
		blockPassed[count++] = currentBlock;
		while (!currentBlock.equals(blockDest))
		{

			if (count >= maxBlocksPassed - 1)
			{
				MalisisCore.Message("Trace fail : " + count + " block passed!");
				return false;
			}

			mop = traceBlock(currentBlock, currentSrc, dest);
			if (mop[0] == null)
			{
				String s = "(" + currentBlock.chunkPosX + ", " + currentBlock.chunkPosY + ", " + currentBlock.chunkPosZ + ")"; 
				MalisisCore.Message("Trace fail : vector not in block " + s);
				return false;
			}

			if(mop[1] == null)
			{
				MalisisCore.Message("Trace fail : no exit in block");
				return false;
			}
			currentBlock = nextBlock(currentBlock, mop[1]);
			currentSrc = mop[1].hitVec;
			blockPassed[count++] = currentBlock;

		}

		return true;

		// while (length-- >= 0)
		// {
		// if (Double.isNaN(src.xCoord) || Double.isNaN(src.yCoord) ||
		// Double.isNaN(src.zCoord))
		// return false;
		//
		// // are blocks on the same line boolean diffBlockX = true; boolean
		// diffBlockY = true;
		// boolean diffBlockZ = true;
		// double startX = 999.0D;
		// double startY = 999.0D;
		// double startZ = 999.0D;
		//
		// if (blockDest.chunkPosX > blockSrc.chunkPosX)
		// startX = (double) blockSrc.chunkPosX + 1.0D;
		// else if (blockDest.chunkPosX < blockSrc.chunkPosX)
		// startX = (double) blockSrc.chunkPosX + 0.0D;
		// else
		// diffBlockX = false;
		//
		// if (blockDest.chunkPosY > blockSrc.chunkPosY)
		// startY = (double) blockSrc.chunkPosY + 1.0D;
		// else if (blockDest.chunkPosY < blockSrc.chunkPosY)
		// startY = (double) blockSrc.chunkPosY + 0.0D;
		// else
		// diffBlockY = false;
		//
		// if (blockDest.chunkPosZ > blockSrc.chunkPosZ)
		// startZ = (double) blockSrc.chunkPosZ + 1.0D;
		// else if (blockDest.chunkPosZ < blockSrc.chunkPosZ)
		// startZ = (double) blockSrc.chunkPosZ + 0.0D;
		// else
		// diffBlockZ = false;
		//
		// double d2x = 999.0D;
		// double d2y = 999.0D;
		// double d2z = 999.0D;
		// double distX = dest.xCoord - src.xCoord;
		// double distY = dest.yCoord - src.yCoord;
		// double distZ = dest.zCoord - src.zCoord;
		//
		// if (diffBlockX)
		// d2x = (startX - src.xCoord) / distX;
		//
		// if (diffBlockY)
		// d2y = (startY - src.yCoord) / distY;
		//
		// if (diffBlockZ)
		// d2z = (startZ - src.zCoord) / distZ;
		//
		// byte side;
		// if (d2x < d2y && d2x < d2z)
		// {
		// if (blockDest.chunkPosX > blockSrc.chunkPosX)
		// side = 4;
		// else
		// side = 5;
		//
		// src.xCoord = startX;
		// src.yCoord += distY * d2x;
		// src.zCoord += distZ * d2x;
		// }
		// else if (d2y < d2z)
		// {
		// if (blockDest.chunkPosY > blockSrc.chunkPosY)
		// side = 0;
		// else
		// side = 1;
		//
		// src.xCoord += distX * d2y;
		// src.yCoord = startY;
		// src.zCoord += distZ * d2y;
		// }
		// else
		// {
		// if (blockDest.chunkPosZ > blockSrc.chunkPosZ)
		// side = 2;
		// else
		// side = 3;
		//
		// src.xCoord += distX * d2z;
		// src.yCoord += distY * d2z;
		// src.zCoord = startZ;
		// }
		//
		// Vec3 vec32 = world.getWorldVec3Pool().getVecFromPool(src.xCoord,
		// src.yCoord, src.zCoord);
		// blockSrc.chunkPosX = (int) (vec32.xCoord = (double)
		// MathHelper.floor_double(src.xCoord));
		//
		// if (side == 5)
		// {
		// --blockSrc.chunkPosX;
		// ++vec32.xCoord;
		// }
		//
		// blockSrc.chunkPosY = (int) (vec32.yCoord = (double)
		// MathHelper.floor_double(src.yCoord));
		//
		// if (side == 1)
		// {
		// --blockSrc.chunkPosY;
		// ++vec32.yCoord;
		// }
		//
		// blockSrc.chunkPosZ = (int) (vec32.zCoord = (double)
		// MathHelper.floor_double(src.zCoord));
		//
		// if (side == 3)
		// {
		// --blockSrc.chunkPosZ;
		// ++vec32.zCoord;
		// }
		//
		// if (world.getBlock(blockSrc.chunkPosX, blockSrc.chunkPosY,
		// blockSrc.chunkPosZ).isNormalCube())
		// return true;
		// }
		//
		// return false;
	}

	public static class Options
	{
		public static int HIT_FIRST_BLOCK = 1 << 0;
		public static int HIT_BLOCKS = 1 << 1;
	}

}
