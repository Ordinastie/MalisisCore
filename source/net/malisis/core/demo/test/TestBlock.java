package net.malisis.core.demo.test;

import net.malisis.core.renderer.IBaseRendering;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class TestBlock extends Block implements ITileEntityProvider, IBaseRendering
{
	private int renderId;

	public TestBlock()
	{
		super(Material.ground);
		setCreativeTab(CreativeTabs.tabBlock);
		setLightLevel(0.9375F);
	}

	@Override
	public void registerBlockIcons(IIconRegister p_149651_1_)
	{}

	@Override
	public IIcon getIcon(int side, int metadata)
	{
		return Blocks.lit_pumpkin.getIcon(side, metadata);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
	{
		if (world.isRemote)
			return true;

		TestTileEntity te = (TestTileEntity) world.getTileEntity(x, y, z);
		if (te != null)
			te.getInventory().open((EntityPlayerMP) player);

		return true;
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
		return renderId;
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int var2)
	{
		return new TestTileEntity();
	}

	@Override
	public void setRenderId(int id)
	{
		renderId = id;

	}

}
