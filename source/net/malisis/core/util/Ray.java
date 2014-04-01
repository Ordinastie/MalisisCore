package net.malisis.core.util;

public class Ray
{
	public Point origin;
	public Vector direction;

	public Ray(Point p, Vector v)
	{
		origin = p;
		direction = v;
	}

	public Ray(Ray r)
	{
		origin = new Point(r.origin);
		direction = new Vector(r.direction);
	}

	public Point getPointAt(double t)
	{
		return new Point(origin.x + t * direction.x, origin.y + t * direction.y, origin.z + t * direction.z);
	}

	public double intersectX(double x)
	{
		if (direction.x == 0)
			return Double.NaN;
		return (x - origin.x) / direction.x;
	}

	public double intersectY(double y)
	{
		if (direction.y == 0)
			return Double.NaN;
		return (y - origin.y) / direction.y;
	}

	public double intersectZ(double z)
	{
		if (direction.z == 0)
			return Double.NaN;
		return (z - origin.z) / direction.z;
	}

}
