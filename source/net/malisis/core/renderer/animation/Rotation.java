package net.malisis.core.renderer.animation;

import net.malisis.core.renderer.element.Shape;

public class Rotation extends Animation<Rotation>
{
	protected float fromAngle;
	protected float toAngle;
	protected float axisX, axisY, axisZ;
	protected float offsetX, offsetY, offsetZ;

	public Rotation(float angle)
	{
		to(angle);
	}

	public Rotation(float fromAngle, float toAngle)
	{
		from(fromAngle);
		to(toAngle);

	}

	public Rotation(float angle, float axisX, float axisY, float axisZ)
	{
		to(angle);
		aroundAxis(axisX, axisY, axisZ);
	}

	public Rotation(float angle, float axisX, float axisY, float axisZ, float offsetX, float offsetY, float offsetZ)
	{
		to(angle);
		aroundAxis(axisX, axisY, axisZ);
		offset(offsetX, offsetY, offsetZ);
	}

	public Rotation from(float angle)
	{
		fromAngle = angle;
		return this;
	}

	public Rotation to(float angle)
	{
		toAngle = angle;
		return this;
	}

	public Rotation aroundAxis(float x, float y, float z)
	{
		axisX = x;
		axisY = y;
		axisZ = z;
		return this;
	}

	public Rotation offset(float x, float y, float z)
	{
		offsetX = x;
		offsetY = y;
		offsetZ = z;
		return this;
	}

	@Override
	protected void animate(Shape s, float comp)
	{
		if (comp >= 0)
			s.rotate(fromAngle + (toAngle - fromAngle) * comp, axisX, axisY, axisZ, offsetX, offsetY, offsetZ);
	}
}