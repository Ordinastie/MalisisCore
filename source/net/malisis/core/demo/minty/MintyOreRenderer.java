package net.malisis.core.demo.minty;

import net.malisis.core.renderer.BaseRenderer;
import net.malisis.core.renderer.element.RenderParameters;
import net.malisis.core.renderer.preset.ShapePreset;
import net.minecraft.init.Blocks;

public class MintyOreRenderer extends BaseRenderer
{
	public static int renderId;
	
	@Override
	public void render()
	{
		//{"Obsidium/Lava", "Azurite", "Crimsonite", "Titanium"}
		RenderParameters rp = RenderParameters.setDefault();
		ArmoryOre block = (ArmoryOre) this.block;
		
		if(blockMetadata == 0)
			rp.icon = Blocks.lava.getIcon(0, 0);
		
		if(blockMetadata != 3)
		{
			rp.useBlockBrightness = false;
			rp.calculateBrightness = false;
			rp.brightness = block.getOreBrightness(blockMetadata);
		}
		
		rp.colorMultiplier = block.colorMultiplier(blockMetadata);
			
		drawShape(ShapePreset.Cube(), rp);
		
		rp = RenderParameters.setDefault();
		rp.icon = block.getOverlayIcon(0, blockMetadata);
		drawShape(ShapePreset.Cube(), rp);   	
    			
	}
	
	@Override
	public boolean shouldRender3DInInventory(int modelId)
	{
		return true;
	}
}
