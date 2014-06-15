package net.malisis.core.demo.minty;

import net.malisis.core.renderer.BaseRenderer;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.preset.ShapePreset;
import net.minecraft.init.Blocks;

public class MintyOreRenderer extends BaseRenderer
{
	public static int renderId;

	@Override
	public void render()
	{
		// {"Obsidium/Lava", "Azurite", "Crimsonite", "Titanium"}
		RenderParameters rp = new RenderParameters();
		ArmoryOre block = (ArmoryOre) this.block;

		if (blockMetadata == 0)
			rp.icon.set(Blocks.lava.getIcon(0, 0));

		if (blockMetadata != 3)
		{
			rp.useBlockBrightness.set(false);
			rp.calculateBrightness.set(false);
			rp.brightness.set(block.getOreBrightness(blockMetadata));
		}

		rp.colorMultiplier.set(block.colorMultiplier(blockMetadata));

		drawShape(ShapePreset.Cube(), rp);

		rp = new RenderParameters();
		rp.icon.set(block.getOverlayIcon(0, blockMetadata));
		drawShape(ShapePreset.Cube(), rp);

	}

	@Override
	public boolean shouldRender3DInInventory(int modelId)
	{
		return true;
	}
}
