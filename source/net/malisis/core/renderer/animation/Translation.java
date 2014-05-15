package net.malisis.core.renderer.animation;

import net.malisis.core.renderer.element.Shape;

public class Translation extends Animation
{
	protected Translation(float x, float y, float z)
	{
		super(x, y, z);
	}

	@Override
	protected void animate(Shape s, float comp)
	{
		if (comp >= 0)
			s.translate(x * comp, y * comp, z * comp);
	}

}