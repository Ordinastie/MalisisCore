package net.malisis.core.light;

import java.util.HashMap;

import net.malisis.core.renderer.element.Vertex;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;

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

	/**
	 * Set light to on
	 */
	public void on()
	{
		this.emitting = true;
	}

	/**
	 * Set light to off
	 */
	public void off()
	{
		this.emitting = false;
	}

	/**
	 * Get the red component of the light
	 * 
	 * @return
	 */
	public int red()
	{
		return red(color);
	}

	/**
	 * Get the green component of the light
	 * 
	 * @return
	 */
	public int green()
	{
		return green(color);
	}

	/**
	 * Get the blue component of the light
	 * 
	 * @return
	 */
	public int blue()
	{
		return blue(color);
	}

	/**
	 * Remove all lights
	 */
	public static void clear()
	{
		lights = new HashMap<String, ColoredLight>();
	}

	/**
	 * Get the light specified by <code>name</code>
	 * 
	 * @param name
	 * @return
	 */
	public static ColoredLight getLight(String name)
	{
		return lights.get(name);
	}

	/**
	 * Get all the lights
	 * 
	 * @return
	 */
	public static ColoredLight[] getLights()
	{
		return (ColoredLight[]) lights.values().toArray(new ColoredLight[0]);
	}

	/**
	 * WIP : calculate how much we need to move the vertex to simulate a shadow
	 * 
	 * @param mop
	 * @param vertex
	 * @param src
	 * @param dest
	 * @param side
	 * @return
	 */
	public boolean calculateOffset(MovingObjectPosition mop, Vertex vertex, Vec3 src, Vec3 dest, ForgeDirection side)
	{
		double A = src.subtract(dest).lengthVector();
		Vec3 aVec = src.subtract(mop.hitVec);
		double a = aVec.lengthVector();
		if (a == 0)
			return false;

		Vec3 v = mop.hitVec.addVector(-mop.blockX, -mop.blockY, -mop.blockZ);
		double bx = vertex.getX() > src.xCoord ? 1 - v.xCoord : v.xCoord;
//		double by = vertex.getY() > src.yCoord ? 1 - v.yCoord : v.yCoord;
//		double bz = vertex.getZ() > src.zCoord ? 1 - v.zCoord : v.zCoord;
		double lx = A * bx / a;
//		double ly = A * by / a;
//		double lz = A * bz / a;

		if (side == ForgeDirection.UP || side == ForgeDirection.DOWN)
		{
			if (lx > 1 || lx < 0)
				return false;
			vertex.setX(vertex.getX() + (float) lx * (aVec.xCoord < 0 ? -1 : 1));
		}
		else
			;// vertex.setZ(vertex.getZ() + (float)lz * side.offsetX);

		return true;
	}

	public String toString()
	{
		return this.name + " " + name(this.color) + " (" + this.x + "," + this.y + "," + this.z + ")";
	}

	/*
	 * STATIC COLOR HELPERS
	 */
	/**
	 * Get the alpha of an <code>int</code> representing a color
	 * 
	 * @param color
	 * @return
	 */
	public static int alpha(int color)
	{
		return (color >>> 24) & 255;
	}

	/**
	 * Get the red component of an <code>int</code> representing a color
	 * 
	 * @param color
	 * @return
	 */
	public static int red(int color)
	{
		return (color >>> 16) & 255;
	}

	/**
	 * Get the green component of an <code>int</code> representing a color
	 * 
	 * @param color
	 * @return
	 */
	public static int green(int color)
	{
		return (color >>> 8) & 255;
	}

	/**
	 * Get the gren blue component of an <code>int</code> representing a color
	 * 
	 * @param color
	 * @return
	 */
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

		float a = a1 + a2 - a1 * a2;
		// a = Math.max(a1, a2);

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
	 * Get readable (hex values) name for a color
	 * 
	 * @param c
	 * @return
	 */
	public static String name(int c)
	{
		return Integer.toHexString(c);
	}
}
