package net.malisis.core.renderer.animation;

import net.malisis.core.renderer.element.Shape;

public class Scale extends Animation
{
	float toX, toY, toZ;

	public Scale(float x, float y, float z)
	{
		super(x, y, z);
	}

	protected void scaleTo(float x, float y, float z)
	{
		toX = x;
		toY = y;
		toZ = z;
	}

	@Override
	protected void animate(Shape s, float comp)
	{
		comp = Math.max(comp, 0);
		s.scale(x + (toX - x) * comp, y + (toY - y) * comp, z + (toZ - z) * comp);
	}
}
