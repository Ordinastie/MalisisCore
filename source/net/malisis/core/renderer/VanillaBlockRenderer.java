package net.malisis.core.renderer;

import net.malisis.core.renderer.element.RenderParameters;
import net.malisis.core.renderer.preset.FacePreset;
import net.malisis.core.renderer.preset.ShapePreset;
import net.minecraft.block.BlockGrass;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.init.Blocks;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import de.mineformers.core.event.RenderBlockEvent;

public class VanillaBlockRenderer extends BaseRenderer
{

	@SubscribeEvent
	public boolean VanillaRenderEvent(RenderBlockEvent.Pre event)
	{
		// only render type 0 (standard blocks) and 39 (quartz)
		if (event.renderType != 0 && event.renderType != 39)
			return false;

		// set informations in the renderer
		set(event.blockAccess, event.block, event.x, event.y, event.z, event.blockAccess.getBlockMetadata(event.x, event.y, event.z));

		// uncomment to render only wool (for debugging)
		// if(event.block != Blocks.wool)
		// return true;

		// special case for grass
		if (event.block == Blocks.grass)
			return renderGrass(event);

		// special case for leaves
		if (event.block == Blocks.leaves)
			return renderLeaves(event);

		// Default rendering
		renderWorldBlock();
		event.setCanceled(true);
		return true;
	}

	/**
	 * Render Grass
	 * 
	 * @param event
	 * @return
	 */
	private boolean renderGrass(RenderBlockEvent.Pre event)
	{
		prepare(BaseRenderer.TYPE_WORLD);
		RenderParameters rp = new RenderParameters();
		rp.colorMultiplier = event.block.colorMultiplier(event.blockAccess, event.x, event.y, event.z);
		drawShape(ShapePreset.Cube().setParameters(FacePreset.Top(), rp, true));

		if (RenderBlocks.fancyGrass)
		{
			rp.icon = BlockGrass.getIconSideOverlay();
			drawShape(ShapePreset.CubeSides(), rp);
		}

		clean();
		event.setCanceled(true);
		return true;
	}

	/**
	 * Render Leaves
	 * 
	 * @param event
	 * @return
	 */
	private boolean renderLeaves(RenderBlockEvent.Pre event)
	{
		prepare(BaseRenderer.TYPE_WORLD);
		RenderParameters rp = new RenderParameters();
		rp.colorMultiplier = event.block.colorMultiplier(event.blockAccess, event.x, event.y, event.z);
		drawShape(ShapePreset.Cube(), rp);
		clean();
		event.setCanceled(true);
		return true;
	}
}
