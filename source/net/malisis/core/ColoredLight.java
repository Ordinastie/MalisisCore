package net.malisis.core;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;

public class ColoredLight
{
	public String name;
	public double x;
	public double y;
	public double z;
	public int color;
	public int strength;
	public boolean emitting;
	public float distance;
	private static HashMap<String, ColoredLight> lights = new HashMap<String, ColoredLight>();

	public ColoredLight(String name, double x, double y, double z, int color, int str)
	{
		this.name = name;
		this.x = x;
		this.y = y;
		this.z = z;
		this.color = color;
		this.strength = str;
		lights.put(this.name, this);
	}

	public void on()
	{
		this.emitting = true;
	}

	public void off()
	{
		this.emitting = false;
	}

	public int red()
	{
		return red(color);
	}

	public int green()
	{
		return green(color);
	}

	public int blue()
	{
		return blue(color);
	}

	public static void clear()
	{
		lights = new HashMap<String, ColoredLight>();
	}

	public static ColoredLight getLight(String name)
	{
		return lights.get(name);
	}

	public static ColoredLight[] getLights()
	{
		return (ColoredLight[]) lights.values().toArray(new ColoredLight[0]);
	}

	public static int[] getLights(IBlockAccess world, float x, float y, float z)
	{
		int[] tmp = new int[lights.size()];
		int count = 0;
		for (Entry<String, ColoredLight> e : lights.entrySet())
		{
			ColoredLight l = e.getValue();
			if (l.emitting)
			{
				float d = (float) Math.sqrt(Math.pow(l.x - x, 2) + Math.pow(l.y - y, 2) + Math.pow(l.z - z, 2));
				if (d <= (l.strength) && l.directLight(world, x, y, z))
				{
					int alpha = (int) (255 - Math.pow((d / l.strength), 2) * 255);
					tmp[count++] = l.color | alpha << 24;
				}
			}
		}

		return Arrays.copyOf(tmp, count);
	}
	
	
	
	public boolean directLight(IBlockAccess world, float x, float y, float z)
	{

		Vec3 src = world.getWorldVec3Pool().getVecFromPool(this.x, this.y, this.z);
		Vec3 dest = world.getWorldVec3Pool().getVecFromPool(x, y, z);

	//	MalisisCore.Message("RT : "  + src + " / " + dest);
		MovingObjectPosition mop = Minecraft.getMinecraft().theWorld.func_147447_a(src, dest, false, false, false);
		return mop == null || mop.typeOfHit != MovingObjectType.BLOCK;
	}

	public String toString()
	{
		return this.name + " " + name(this.color) + " (" + this.x + "," + this.y + "," + this.z + ")";
	}

	/*
	 * STATIC COLOR HELPERS
	 */

	public static int alpha(int color)
	{
		return (color >>> 24) & 255;
	}

	public static int red(int color)
	{
		return (color >>> 16) & 255;
	}

	public static int green(int color)
	{
		return (color >>> 8) & 255;
	}

	public static int blue(int color)
	{
		return (color >>> 0) & 255;
	}

	/**
	 * Apply the alpha value for the color
	 * 
	 * @param color
	 * @return
	 */
	public static int applyAlpha(int color)
	{
		color = invert(color);
		int a = alpha(color);
		int r = red(color);
		int g = green(color);
		int b = blue(color);

		r = 255 - (r * a / 255);
		g = 255 - (g * a / 255);
		b = 255 - (b * a / 255);

		return (r & 255) << 16 | (g & 255) << 8 | (b & 255);
	}

	/**
	 * Invert color but keep alpha value
	 * 
	 * @param c
	 * @return
	 */
	public static int invert(int c)
	{
		return alpha(c) << 24 | (255 - red(c)) << 16 | (255 - green(c)) << 8 | (255 - blue(c));
	}

	/**
	 * Add two colors together (blending two lights)
	 * 
	 * @param c1
	 * @param c2
	 * @return
	 */
	public static int mergeAdd(int c1, int c2)
	{
		float a1 = (float) ColoredLight.alpha(c1) / 255;
		int r1 = ColoredLight.red(c1);
		int g1 = ColoredLight.green(c1);
		int b1 = ColoredLight.blue(c1);
		float a2 = (float) ColoredLight.alpha(c2) / 255;
		int r2 = ColoredLight.red(c2);
		int g2 = ColoredLight.green(c2);
		int b2 = ColoredLight.blue(c2);

		// if(a1 > a2)
		// return c1;
		// else if(a2 > a1)
		// return c2;

		float a = a1 + a2 - a1 * a2;
		
		int r = (int) ((r1 * a1 + r2 * a2) / (a1 + a2));
		int g = (int) ((g1 * a1 + g2 * a2) / (a1 + a2));
		int b = (int) ((b1 * a1 + b2 * a2) / (a1 + a2));

		return ((int) (a * 255)) << 24 | (r & 255) << 16 | (g & 255) << 8 | (b & 255);
	}

	/**
	 * Apply <b>c2</b> color over <b>c1</b> (blending light over colored
	 * surface)
	 * 
	 * @param c1
	 * @param c2
	 * @return
	 */
	public static int mergeSubtract(int c1, int c2)
	{
		c2 = applyAlpha(c2);
		int r1 = ColoredLight.red(c1);
		int g1 = ColoredLight.green(c1);
		int b1 = ColoredLight.blue(c1);
		int r2 = ColoredLight.red(c2);
		int g2 = ColoredLight.green(c2);
		int b2 = ColoredLight.blue(c2);

		int r = (int) ((r2 * r1) / 255);
		int g = (int) ((g2 * g1) / 255);
		int b = (int) ((b2 * b1) / 255);

		return (r & 255) << 16 | (g & 255) << 8 | (b & 255);
	}

	/**
	 * Merge lights together
	 * 
	 * @return
	 */
	public static int computeLights(int[] lights, int baseBrightness)
	{
		if (lights.length == 0)
			return 0;

		int color = lights[0];
		for (int i = 1; i < lights.length; i++)
		{
			color = ColoredLight.mergeAdd(color, lights[i]);
		}

		if (baseBrightness != 0)
		{
			int cb = (baseBrightness * 16) << 24 | 0xF1FF94;
			color = ColoredLight.mergeAdd(color, cb);
		}

		return color;
	}

	/**
	 * Get readable (hex values) name for a color
	 * 
	 * @param c
	 * @return
	 */
	public static String name(int c)
	{
		return Integer.toHexString(c);
	}

	public boolean rayTrace(IBlockAccess world, Vec3 src, Vec3 dest)
	{
		if (Double.isNaN(src.xCoord) || Double.isNaN(src.yCoord) || Double.isNaN(src.zCoord) || Double.isNaN(dest.xCoord)
				|| Double.isNaN(dest.yCoord) || Double.isNaN(dest.zCoord))
			return false;

		int destX = MathHelper.floor_double(dest.xCoord);
		int destY = MathHelper.floor_double(dest.yCoord);
		int destZ = MathHelper.floor_double(dest.zCoord);
		int srcX = MathHelper.floor_double(src.xCoord);
		int srcY = MathHelper.floor_double(src.yCoord);
		int srcZ = MathHelper.floor_double(src.zCoord);

		int length = 200;
		boolean firstBlock = true;

		while (length-- >= 0)
		{
			if (Double.isNaN(src.xCoord) || Double.isNaN(src.yCoord) || Double.isNaN(src.zCoord))
				return false;

			if (!firstBlock && (srcX == destX && srcY == destY && srcZ == destZ))
				return false;
			firstBlock = false;

			boolean nextBlockX = true;
			boolean nextBlockY = true;
			boolean nextBlockZ = true;
			double dx = 999.0D;
			double dy = 999.0D;
			double dz = 999.0D;

			if (destX > srcX)
				dx = (double) srcX + 1.0D;
			else if (destX < srcX)
				dx = (double) srcX + 0.0D;
			else
				nextBlockX = false;

			if (destY > srcY)
				dy = (double) srcY + 1.0D;
			else if (destY < srcY)
				dy = (double) srcY + 0.0D;
			else
				nextBlockY = false;

			if (destZ > srcZ)
				dz = (double) srcZ + 1.0D;
			else if (destZ < srcZ)
				dz = (double) srcZ + 0.0D;
			else
				nextBlockZ = false;

			double d2x = 999.0D;
			double d2y = 999.0D;
			double d2z = 999.0D;
			double distX = dest.xCoord - src.xCoord;
			double distY = dest.yCoord - src.yCoord;
			double distZ = dest.zCoord - src.zCoord;

			if (nextBlockX)
				d2x = (dx - src.xCoord) / distX;

			if (nextBlockY)
				d2y = (dy - src.yCoord) / distY;

			if (nextBlockZ)
				d2z = (dz - src.zCoord) / distZ;

			byte side;
			if (d2x < d2y && d2x < d2z)
			{
				if (destX > srcX)
					side = 4;
				else
					side = 5;

				src.xCoord = dx;
				src.yCoord += distY * d2x;
				src.zCoord += distZ * d2x;
			}
			else if (d2y < d2z)
			{
				if (destY > srcY)
					side = 0;
				else
					side = 1;

				src.xCoord += distX * d2y;
				src.yCoord = dy;
				src.zCoord += distZ * d2y;
			}
			else
			{
				if (destZ > srcZ)
					side = 2;
				else
					side = 3;

				src.xCoord += distX * d2z;
				src.yCoord += distY * d2z;
				src.zCoord = dz;
			}

			Vec3 vec32 = world.getWorldVec3Pool().getVecFromPool(src.xCoord, src.yCoord, src.zCoord);
			srcX = (int) (vec32.xCoord = (double) MathHelper.floor_double(src.xCoord));

			if (side == 5)
			{
				--srcX;
				++vec32.xCoord;
			}

			srcY = (int) (vec32.yCoord = (double) MathHelper.floor_double(src.yCoord));

			if (side == 1)
			{
				--srcY;
				++vec32.yCoord;
			}

			srcZ = (int) (vec32.zCoord = (double) MathHelper.floor_double(src.zCoord));

			if (side == 3)
			{
				--srcZ;
				++vec32.zCoord;
			}

			if (world.getBlock(srcX, srcY, srcZ).isNormalCube())
				return true;
		}

		return false;
	}
}
