package net.malisis.core.renderer;

import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.RenderParameters;
import net.malisis.core.renderer.element.Vertex;
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
		if(event.renderType != 0 && event.renderType != 39)
			return false;
	
		set(event.blockAccess, event.block, event.x, event.y, event.z, event.blockAccess.getBlockMetadata(event.x, event.y, event.z));
		
		if(event.block == Blocks.quartz_block)
		{
			set(event.blockAccess, event.block, event.x + (event.x % 2), event.y, event.z, event.blockAccess.getBlockMetadata(event.x, event.y, event.z));
			Face f = FacePreset.South();
			Vertex[] vs = f.getVertexes();
			vs[0].setColor(0x0000FF);
			vs[1].setColor(0x0000FF);
			vs[2].setColor(0x0000FF);
			vs[3].setColor(0x0000FF);
			
			prepare(BaseRenderer.TYPE_WORLD);
			RenderParameters rp = new RenderParameters();
			rp.usePerVertexColor = true;
			drawFace(f, RenderParameters.merge(f.getParameters(), rp));
			clean();
			event.setCanceled(true);
			return true;
		}
		
		
//		if(event.block != Blocks.wool)
//			return true;
		
		
		if(event.block == Blocks.grass)
			return renderGrass(event);
		
//		
//		if(event.block == Blocks.sandstone || event.block == Blocks.stone_slab)
//			return true;
		
		if(event.block == Blocks.leaves)
		{
			prepare(BaseRenderer.TYPE_WORLD);
			RenderParameters rp = new RenderParameters();
			rp.colorMultiplier = event.block.colorMultiplier(event.blockAccess, event.x, event.y, event.z);
			drawShape(ShapePreset.Cube(), rp);
			clean();
			event.setCanceled(true);
			return true;
		}
		
		//Message("" + Integer.toHexString(light.red()) + Integer.toHexString(light.green()) + Integer.toHexString(light.blue()));
		renderWorldBlock();
		event.setCanceled(true);
		return true;
	}
	
	
	private boolean renderGrass(RenderBlockEvent.Pre event)
	{
		RenderParameters rp = new RenderParameters();
		rp.colorMultiplier = event.block.colorMultiplier(event.blockAccess, event.x, event.y, event.z);
		//Shape s = ShapePreset.Cube();
		prepare(BaseRenderer.TYPE_WORLD);
		drawShape(ShapePreset.Cube().setParameters(FacePreset.Top(), rp, true));
		
		if(RenderBlocks.fancyGrass)
		{
			rp.icon = BlockGrass.getIconSideOverlay();
			drawShape(ShapePreset.CubeSides(), rp);			
		}
		
		clean();
		
		event.setCanceled(true);
		return true;
	}
}
