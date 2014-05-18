package net.malisis.core.demo.stargate;

import net.malisis.core.renderer.BaseRenderer;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

public class Stargate
{
	StargateBlock sgBlock;
	
	public void preInit() 
	{
		(sgBlock = new StargateBlock()).setBlockName("sgBlock");
		GameRegistry.registerBlock(sgBlock, sgBlock.getUnlocalizedName().substring(5));
		
		GameRegistry.registerTileEntity(StargateTileEntity.class, "stargateTileEntity");
	}
	
	public void init()
	{
		StargateRenderer r = BaseRenderer.create(StargateRenderer.class);
		RenderingRegistry.registerBlockHandler(r);
		ClientRegistry.bindTileEntitySpecialRenderer(StargateTileEntity.class, r);
	}
}
