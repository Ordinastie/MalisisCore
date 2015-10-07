/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Ordinastie
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.malisis.core.util;

import net.malisis.core.block.MalisisBlock;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Utility class for items.
 *
 * @author Ordinastie
 *
 */
public class ItemUtils
{
	/** Defines a full stack amount to process. */
	public static final int FULL_STACK = -1;

	/** Defines a half stack amount to process. */
	public static final int HALF_STACK = -2;

	/**
	 * Utility class to help merge {@link ItemStack itemStacks}.<br>
	 * After calling {@link ItemStacksMerger#merge() merge()}, {@link ItemStacksMerger#merge merge} and {@link ItemStacksMerger#into into}
	 * will hold the results. An empty itemStack will automatically be converted to <code>null</code>.
	 */
	public static class ItemStacksMerger
	{
		/** The {@link ItemStack} to merge. */
		public ItemStack merge;

		/** The targetted {@link ItemStack} receiving the merge. */
		public ItemStack into;

		/** Amount of the {@link ItemStack} that was merged. -1 until {@link ItemStacksMerger#merge() merge()} is called. */
		public int nbMerged = -1;

		/**
		 * Instantiates the {@link ItemStacksMerger}.
		 *
		 * @param merge the merge
		 * @param into the into
		 */
		public ItemStacksMerger(ItemStack merge, ItemStack into)
		{
			this.merge = merge;
			this.into = into;
		}

		/**
		 * Merges the full amount for the {@link ItemStacksMerger#merge merge} {@link ItemStack}.
		 *
		 * @return true, if stacks could be merged, false otherwise
		 */
		public boolean merge()
		{
			return merge(FULL_STACK);
		}

		/**
		 * Merges the specified amount for the {@link ItemStacksMerger#merge merge} {@link ItemStack}.<br>
		 * Amount will be capped to the {@link ItemStack#getMaxStackSize()} amount, or 64 if both {@link ItemStacksMerger#merge merge} and
		 * {@link ItemStacksMerger#into into} are <code>null</code>.
		 *
		 * @param amount the amount to be merged
		 * @return true, if stacks could be merged, false otherwise
		 */
		public boolean merge(int amount)
		{
			int max = 64;
			if (into != null)
				max = into.getMaxStackSize();
			else if (merge != null)
				max = merge.getMaxStackSize();
			return merge(amount, max);
		}

		/**
		 * Merges the specified amount for the {@link ItemStacksMerger#merge merge} {@link ItemStack}.<br>
		 * Amount will be capped by <b>intoMaxStackSize</b> (used for inventory slots for example).
		 *
		 * @param amount the amount to be merged
		 * @param intoMaxStackSize max amount the into itemStack can hold
		 * @return true, if successful
		 */
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

			nbMerged = Math.min(intoMaxStackSize, into.getMaxStackSize()) - into.stackSize;
			if (nbMerged == 0)
				return false;
			nbMerged = Math.min(nbMerged, amount);

			merge.stackSize -= nbMerged;
			if (merge.stackSize == 0)
				merge = null;
			into.stackSize += nbMerged;

			return true;
		}

		/**
		 * @return true, if {@link ItemStacksMerger#merge merge} and {@link ItemStacksMerger#into into} can be merged.
		 */
		public boolean canMerge()
		{
			return merge == null || into == null || areItemStacksStackable(merge, into);
		}
	}

	/**
	 * Utility class to help split an {@link ItemStack}.<br>
	 * An empty itemStack will automatically be converted to <code>null</code>.
	 */
	public static class ItemStackSplitter
	{
		/** The {@link ItemStack} to be split. */
		public ItemStack source;

		/** The restulting {@link ItemStack} after the split. */
		public ItemStack split;

		/** The amount of items to split. */
		public int amount;

		/**
		 * Instantiates the {@link ItemStackSplitter}.
		 *
		 * @param source the source
		 */
		public ItemStackSplitter(ItemStack source)
		{
			this.source = source;
		}

		/**
		 * Splits the {@link ItemStackSplitter#source} by the specified amount.
		 *
		 * @param amount the amount
		 * @return the item stack
		 */
		public ItemStack split(int amount)
		{
			if (source == null)
			{
				split = null;
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

	/**
	 * Checks whether two {@link ItemStack itemStacks} can be stacked together
	 *
	 * @param stack1 first itemStack
	 * @param stack2 second itemStack
	 * @return true, if the itemStack can be stacked, false otherwise
	 */
	public static boolean areItemStacksStackable(ItemStack stack1, ItemStack stack2)
	{
		return !(stack1 == null || stack2 == null) && stack1.isStackable() && stack1.getItem() == stack2.getItem()
				&& (!stack2.getHasSubtypes() || stack2.getMetadata() == stack1.getMetadata())
				&& ItemStack.areItemStackTagsEqual(stack2, stack1);

	}

	public static IBlockState getStateFromItemStack(ItemStack itemStack)
	{
		if (itemStack == null)
			return null;

		Block block = Block.getBlockFromItem(itemStack.getItem());
		if (block instanceof MalisisBlock)
			return ((MalisisBlock) block).getStateFromItemStack(itemStack);

		return block.getStateFromMeta(itemStack.getItem().getMetadata(itemStack.getMetadata()));
	}

	public static ItemStack getItemStackFromState(IBlockState state)
	{
		Item item = Item.getItemFromBlock(state.getBlock());
		return new ItemStack(item, 0, state.getBlock().getMetaFromState(state));
	}
}
