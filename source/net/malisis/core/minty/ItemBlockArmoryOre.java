package net.malisis.core.minty;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockArmoryOre extends ItemBlock
{
	public static final String[] oreTypes = {"Obsidium", "Azurite", "Crimsonite", "Titanium"};
	
	public ItemBlockArmoryOre(Block block)
	{
		super(block);
		setHasSubtypes(true);
	}

	@Override
	public String getItemStackDisplayName(ItemStack is)
	{
		return oreTypes[is.getItemDamage()] + " Ore";
	}

	@Override
	public int getMetadata(int meta)
	{
		return meta;
	}

}
