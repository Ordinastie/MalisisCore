package net.malisis.core.util;

public class Vector
{
	public double x;
	public double y;
	public double z;

	public Vector(double x, double y, double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vector(Vector v)
	{
		x = v.x;
		y = v.y;
		z = v.z;
	}

	public Vector(Point p)
	{
		x = p.x;
		y = p.y;
		z = p.z;
	}

	public Vector(Point p1, Point p2)
	{
		x = p2.x - p1.x;
		y = p2.y - p1.y;
		z = p2.z - p1.z;
	}

	/**
	 * Set this vector to x, y, and z
	 * 
	 * @param x
	 * @param y
	 * @param z
	 */
	public void set(double x, double y, double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Squared length vector
	 * 
	 * @return
	 */
	public double lenghtSquared()
	{
		return x * x + y * y + z * z;
	}

	/**
	 * Length of vector
	 * 
	 * @return
	 */
	public double length()
	{
		return Math.sqrt(x * x + y * y + z * z);
	}

	/**
	 * Normalize this vector
	 */
	public void normalize()
	{
		double d = length();
		x /= d;
		y /= d;
		z /= d;
	}

	/**
	 * Subtract the given vector from this vector
	 * 
	 * @param v the vector to subtract
	 */
	public void subtract(Vector v)
	{
		x = x - v.x;
		y = y - v.y;
		z = z - v.z;
	}

	/**
	 * Add the given vector to this vector
	 * 
	 * @param v the vector to add
	 */
	public void add(Vector v)
	{
		x += v.x;
		y += v.y;
		z += v.z;
	}

	/**
	 * Cross product with a vector
	 * 
	 * @param v
	 */
	public void cross(Vector v)
	{
		x = (y * v.z) - (z * v.y);
		y = (z * v.x) - (x * v.z);
		z = (x * v.y) - (y * v.x);
	}

	/**
	 * Dot product witch a vector
	 * 
	 * @param v
	 * @return
	 */
	public double dot(Vector v)
	{
		return (x * v.x) + (y * v.y) + (z * v.z);
	}

	/**
	 * Dot product with a point
	 * 
	 * @param p
	 * @return
	 */
	public double dot(Point p)
	{
		return (x * p.x) + (y * p.y) + (z * p.z);
	}

	/**
	 * Scale the vector by factor
	 * 
	 * @param s
	 */
	public void scale(double factor)
	{
		x *= factor;
		y *= factor;
		z *= factor;
	}

	/**
	 * Inverse the vector
	 */
	public void negate()
	{
		x = -x;
		y = -y;
		z = -z;
	}

	public String toString()
	{
		return "[" + x + ", " + y + ", " + z + "]";
	}
}
