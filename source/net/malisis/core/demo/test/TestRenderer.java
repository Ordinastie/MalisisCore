package net.malisis.core.demo.test;

import net.malisis.core.renderer.BaseRenderer;
import net.malisis.core.renderer.preset.ShapePreset;

public class TestRenderer extends BaseRenderer
{
	public static int renderId;

	@Override
	public void render()
	{
		if (typeRender != TYPE_WORLD)
			drawShape(ShapePreset.Cube());
		else
			drawShape(ShapePreset.Cube());
	
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId)
	{
		return true;
	}

}
