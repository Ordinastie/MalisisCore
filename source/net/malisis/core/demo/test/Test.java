package net.malisis.core.demo.test;

import net.malisis.core.renderer.BaseRenderer;
import net.malisis.core.renderer.IBaseRendering;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;

public class Test
{
	TestBlock testBlock;

	public void preInit()
	{
		(testBlock = new TestBlock()).setBlockName("testBlock");
		GameRegistry.registerBlock(testBlock, testBlock.getUnlocalizedName().substring(5));

		GameRegistry.registerTileEntity(TestTileEntity.class, "testTileEntity");
	}

	public void init()
	{
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
		{
			TestRenderer r = BaseRenderer.create(TestRenderer.class, (IBaseRendering) testBlock);
			RenderingRegistry.registerBlockHandler(r);
		}
	}

}
