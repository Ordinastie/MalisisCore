package net.malisis.core.util;

import net.minecraft.item.ItemStack;

public class ItemUtils
{
	public static class ItemStacksMerger
	{
		public ItemStack merge;
		public ItemStack into;
		public int nbMerged = -1;

		public ItemStacksMerger(ItemStack merge, ItemStack into)
		{
			this.merge = merge;
			this.into = into;
		}

		public boolean merge()
		{
			int nbMerged = 0;
			if (!canMerge())
				return false;
			if (merge == null)
				return false;
			if (into == null)
			{
				nbMerged = merge.stackSize;
				into = merge.copy();
				merge.stackSize = 0;
				return true;
			}
			
			nbMerged = into.getMaxStackSize() - into.stackSize;
			if(nbMerged == 0)
				return false;
			nbMerged = Math.min(merge.stackSize, nbMerged);
			
			merge.stackSize -= nbMerged;
			into.stackSize += nbMerged;
		
			return true;
		}

		public boolean canMerge()
		{
			if (merge == null || into == null)
				return true;
			if(!merge.isStackable())
				return false;
			return merge.getItem() == into.getItem() && (!into.getHasSubtypes() || into.getItemDamage() == merge.getItemDamage())
					&& ItemStack.areItemStackTagsEqual(into, merge);
		}
	}
}
