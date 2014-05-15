package net.malisis.core.demo.stargate;

import java.util.Random;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class StargateBlock extends BlockContainer
{
	public static int deployTimer = 100;
	protected StargateBlock()
	{
		super(Material.iron);
		setCreativeTab(CreativeTabs.tabBlock);
		setBlockTextureName("malisiscore:sgplatformside");
	}
	
	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack itemStack)
	{
		world.scheduleBlockUpdate(x, y, z, this, deployTimer);
		((StargateTileEntity) world.getTileEntity(x, y, z)).placedTimer = world.getTotalWorldTime();
	}
	
	@Override
	public void updateTick(World world, int x, int y, int z, Random rand)
	{
		
		world.setBlockMetadataWithNotify(x, y, z, 1, 2);
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
		return StargateRenderer.renderId;
	}
}
