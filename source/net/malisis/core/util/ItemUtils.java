package net.malisis.core.util;

import net.minecraft.item.ItemStack;

public class ItemUtils
{
	public static final int FULL_STACK = -1;
	public static final int HALF_STACK = -2;

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
			return merge(FULL_STACK);
		}

		public boolean merge(int amount)
		{
			int max = 64;
			if (into != null)
				max = into.getMaxStackSize();
			else if (merge != null)
				max = merge.getMaxStackSize();
			return merge(amount, max);
		}

		public boolean merge(int amount, int intoMaxStackSize)
		{
			nbMerged = 0;
			if (!canMerge())
				return false;
			if (merge == null)
				return false;

			if (amount == FULL_STACK)
				amount = merge.stackSize;
			amount = Math.min(amount, merge.stackSize);

			if (into == null)
			{
				nbMerged = Math.min(amount, intoMaxStackSize);
				into = merge.copy();
				into.stackSize = nbMerged;
				merge.stackSize -= into.stackSize;
				if (merge.stackSize <= 0)
					merge = null;
				return true;
			}

			nbMerged = intoMaxStackSize - into.stackSize;
			if (nbMerged == 0)
				return false;
			nbMerged = Math.min(nbMerged, amount);

			merge.stackSize -= nbMerged;
			if (merge.stackSize == 0)
				merge = null;
			into.stackSize += nbMerged;

			return true;
		}

		public boolean canMerge()
		{
			if (merge == null || into == null)
				return true;
			return areItemStacksStackable(merge, into);
		}
	}

	public static class ItemStackSplitter
	{
		public ItemStack source;
		public ItemStack split;
		public int amount;

		public ItemStackSplitter(ItemStack source)
		{
			this.source = source;
		}

		public ItemStack split(int amount)
		{
			if (source == null)
			{
				split = null;
				amount = 0;
				return null;
			}

			if (amount == FULL_STACK)
				amount = source.stackSize;
			else if (amount == HALF_STACK)
				amount = (int) Math.ceil((float) source.stackSize / 2);
			this.amount = amount;

			split = source.splitStack(amount);
			if (source.stackSize == 0)
				source = null;

			return split;
		}
	}

	public static boolean areItemStacksStackable(ItemStack stack1, ItemStack stack2)
	{
		if (stack1 == null || stack2 == null)
			return false;

		if (!stack1.isStackable())
			return false;
		return stack1.getItem() == stack2.getItem() && (!stack2.getHasSubtypes() || stack2.getItemDamage() == stack1.getItemDamage())
				&& ItemStack.areItemStackTagsEqual(stack2, stack1);
	}
}
