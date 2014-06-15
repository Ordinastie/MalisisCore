package net.malisis.core.renderer.animation;

import net.malisis.core.renderer.element.Shape;

public class Scale extends Animation<Scale>
{
	protected float fromX = 1, fromY = 1, fromZ = 1;
	protected float toX = 1, toY = 1, toZ = 1;

	public Scale()
	{}

	public Scale(float x, float y, float z)
	{
		to(x, y, z);
	}

	public Scale(float fromX, float fromY, float fromZ, float toX, float toY, float toZ)
	{
		from(fromX, fromY, fromZ);
		to(toX, toY, toZ);
	}

	protected Scale from(float x, float y, float z)
	{
		fromX = x;
		fromY = y;
		fromZ = z;
		return this;
	}

	protected Scale to(float x, float y, float z)
	{
		toX = x;
		toY = y;
		toZ = z;
		return this;
	}

	@Override
	protected void animate(Shape s, float comp)
	{
		comp = Math.max(comp, 0);
		s.scale(fromX + (toX - fromX) * comp, fromY + (toY - fromY) * comp, fromZ + (toZ - fromZ) * comp);
	}
}
