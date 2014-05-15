package net.malisis.core.renderer.animation;

import net.malisis.core.renderer.element.Shape;

public class Rotation extends Animation
{
	float angle;
	float offsetX, offsetY, offsetZ;

	public Rotation(float angle, float x, float y, float z, float offsetX, float offsetY, float offsetZ)
	{
		super(x, y, z);
		this.angle = angle;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.offsetZ = offsetZ;
	}

	@Override
	protected void animate(Shape s, float comp)
	{
		if (comp >= 0)
			s.rotate(angle * comp, x, y, z, offsetX, offsetY, offsetZ);
	}
}