package net.malisis.core.block;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Random;

import javax.swing.Icon;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class ProxyBlock extends Block
{
	public final Block originalBlock;

	/** Grass Properties **/
	@SideOnly(Side.CLIENT)
	private Icon iconGrassTop;
	@SideOnly(Side.CLIENT)
	private Icon iconSnowSide;
	@SideOnly(Side.CLIENT)
	private Icon iconGrassSideOverlay;

	public ProxyBlock(Block block, boolean replace)
	{
		super(block.getMaterial());
		originalBlock = block;
		copyFields(block, replace);
	}

	@SuppressWarnings("rawtypes")
	private void copyFields(Block block, boolean copyId)
	{
		Class blockClass = block.getClass();

		while (blockClass != null)
		{
			Field[] fields = blockClass.getDeclaredFields();
			for (Field f : fields)
			{
				try
				{
					if (!Modifier.isStatic(f.getModifiers())
							&& (copyId || (!f.getName().equals("blockID") && !f.getName().equals("field_71990_ca"))))
					{
						f.setAccessible(true);
						f.set(this, f.get(block));
					}
				}
				catch (IllegalArgumentException e)
				{}
				catch (IllegalAccessException e)
				{}
			}

			blockClass = blockClass.getSuperclass();
		}
	}

	@Override
	public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side)
	{
		return originalBlock.isSideSolid(world, x, y, z, side);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata)
	{
		return originalBlock.getIcon(side, metadata);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister)
	{
		originalBlock.registerBlockIcons(par1IconRegister);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getBlockColor()
	{
		return originalBlock.getBlockColor();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderColor(int par1)
	{
		return originalBlock.getRenderColor(par1);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int colorMultiplier(IBlockAccess par1IBlockAccess, int par2, int par3, int par4)
	{
		return originalBlock.colorMultiplier(par1IBlockAccess, par2, par3, par4);
	}

	@Override
	public Item getItemDropped(int par1, Random par2Random, int par3)
	{
		return originalBlock.getItemDropped(par1, par2Random, par3);
	}

	@Override
	public Item getItem(World world, int x, int y, int z)
	{
		return originalBlock.getItem(world, x, y, z);
	}

	@Override
	public void updateTick(World world, int x, int y, int z, Random rand)
	{
		originalBlock.updateTick(world, x, y, z, rand);
	}

	@Override
	public int tickRate(World par1World)
	{
		return originalBlock.tickRate(par1World);
	}

	@Override
	public void onBlockClicked(World world, int x, int y, int z, EntityPlayer p)
	{
		originalBlock.onBlockClicked(world, x, y, z, p);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer p, int par6, float par7, float par8, float par9)
	{
		return originalBlock.onBlockActivated(world, x, y, z, p, par6, par7, par8, par9);
	}

	@Override
	public void onBlockAdded(World world, int x, int y, int z)
	{
		originalBlock.onBlockAdded(world, x, y, z);
	}

	@Override
	public int onBlockPlaced(World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata)
	{
		return originalBlock.onBlockPlaced(world, x, y, z, side, hitX, hitY, hitZ, metadata);
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block)
	{
		originalBlock.onNeighborBlockChange(world, x, y, z, block);
	}

	@Override
	public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side)
	{
		return originalBlock.shouldSideBeRendered(world, x, y, z, side);
	}

	@Override
	public int getRenderType()
	{
		return originalBlock.getRenderType();
	}

}
