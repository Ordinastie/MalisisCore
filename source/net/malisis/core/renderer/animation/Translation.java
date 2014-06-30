package net.malisis.core.renderer.animation;

import net.malisis.core.renderer.element.Shape;

public class Translation extends Animation<Translation>
{
	protected float fromX, fromY, fromZ;
	protected float toX, toY, toZ;

	public Translation(float x, float y, float z)
	{
		to(x, y, z);
	}

	public Translation(float fromX, float fromY, float fromZ, float toX, float toY, float toZ)
	{
		from(fromX, fromY, fromZ);
		to(toX, toY, toZ);
	}

	public Translation from(float x, float y, float z)
	{
		fromX = x;
		fromY = y;
		fromZ = z;
		return this;
	}

	public Translation to(float x, float y, float z)
	{
		toX = x;
		toY = y;
		toZ = z;
		return this;
	}

	@Override
	protected void animate(Shape s, float comp)
	{
		if (comp >= 0)
			s.translate(fromX + (toX - fromX) * comp, fromY + (toY - fromY) * comp, fromZ + (toZ - fromZ) * comp);
	}

	@Override
	public Translation reversed(boolean reversed)
	{
		if (!reversed)
			return this;

		float tmpX = fromX;
		float tmpY = fromY;
		float tmpZ = fromZ;
		fromX = toX;
		fromY = toY;
		fromZ = toZ;
		toX = tmpX;
		toY = tmpY;
		toZ = tmpZ;
		return this;
	}

}