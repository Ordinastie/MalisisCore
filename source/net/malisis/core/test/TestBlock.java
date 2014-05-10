package net.malisis.core.test;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;

public class TestBlock extends Block
{
	
	
	public TestBlock()
	{
		super(Material.ground);
		setCreativeTab(CreativeTabs.tabBlock);
	}
	
	@Override
	public void registerBlockIcons(IIconRegister p_149651_1_)
	{
	}
	
	@Override
	public IIcon getIcon(int side, int metadata)
	{
		return Blocks.quartz_block.getIcon(side, metadata);
	}
	
	@Override
	public boolean isNormalCube()
	{
		return false;
	}
	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}
	
	@Override
	public int getRenderType()
	{
		return TestRenderer.renderId;
	}

}
