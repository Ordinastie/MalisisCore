package net.malisis.core.minty;

import net.malisis.core.renderer.BaseRenderer;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;

public class Minty
{
	
	public void preInit() 
	{
		ArmoryOre ore = new ArmoryOre();
		GameRegistry.registerBlock(ore, ItemBlockArmoryOre.class, ore.getUnlocalizedName().substring(5));
		
		if(FMLCommonHandler.instance().getSide() == Side.CLIENT)
		{
			RenderingRegistry.registerBlockHandler(BaseRenderer.create(MintyOreRenderer.class));
		}
		
	}
}
