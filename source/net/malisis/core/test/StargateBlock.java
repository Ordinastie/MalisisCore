package net.malisis.core.test;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class StargateBlock extends BlockContainer
{
	protected StargateBlock()
	{
		super(Material.iron);
		setCreativeTab(CreativeTabs.tabBlock);
		setBlockTextureName("malisiscore:sgplatformside");
	}

	
	@Override
	public IIcon getIcon(int side, int metadata)
	{
		return blockIcon;
	}
	
	@Override
	public TileEntity createNewTileEntity(World world, int metadata)
	{
		StargateTileEntity te = new StargateTileEntity();
		te.placedTimer = world.getTotalWorldTime();
		return te;
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
