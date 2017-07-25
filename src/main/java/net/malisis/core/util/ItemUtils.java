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

import static com.google.common.base.Preconditions.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.malisis.core.block.MalisisBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

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

	/** Regex pattern to convert a string into an {@link ItemStack}. Format : [modid:]item[@damage[xsize]] */
	public static final Pattern pattern = Pattern.compile("((?<modid>.*?):)?(?<item>[^@]*)(@(?<damage>\\d+|[*])(x(?<size>\\d+))?)?");

	/**
	 * Utility class to help merge {@link ItemStack itemStacks}.<br>
	 * After calling {@link ItemStacksMerger#merge() merge()}, {@link ItemStacksMerger#merge merge} and {@link ItemStacksMerger#into into}
	 * will hold the results.
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
			this.merge = checkNotNull(merge);
			this.into = checkNotNull(into);
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
		 * Amount will be capped to the {@link ItemStack#getMaxStackSize()} amount of {@link ItemStacksMerger#merge merge}.
		 *
		 * @param amount the amount to be merged
		 * @return true, if stacks could be merged, false otherwise
		 */
		public boolean merge(int amount)
		{
			return merge(amount, merge.getMaxStackSize());
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
			if (merge == ItemStack.EMPTY)
				return false;

			if (amount == FULL_STACK)
				amount = merge.getCount();
			amount = Math.min(amount, merge.getCount());

			if (into.isEmpty())
			{
				nbMerged = Math.min(amount, intoMaxStackSize);
				into = merge.copy();
				into.setCount(nbMerged);
				merge.shrink(into.getCount());
				if (merge.isEmpty())
					merge = ItemStack.EMPTY;
				return true;
			}

			nbMerged = Math.min(intoMaxStackSize, into.getMaxStackSize()) - into.getCount();
			if (nbMerged == 0)
				return false;
			nbMerged = Math.min(nbMerged, amount);

			merge.shrink(nbMerged);
			if (merge.isEmpty())
				merge = ItemStack.EMPTY;
			into.grow(nbMerged);

			return true;
		}

		/**
		 * @return true, if {@link ItemStacksMerger#merge merge} and {@link ItemStacksMerger#into into} can be merged.
		 */
		public boolean canMerge()
		{
			return merge.isEmpty() || into.isEmpty() || areItemStacksStackable(merge, into);
		}

		@Override
		public String toString()
		{
			return ItemUtils.toString(merge) + " -> " + ItemUtils.toString(into) + " (" + nbMerged + ")";
		}
	}

	/**
	 * Utility class to help split an {@link ItemStack}.
	 */
	public static class ItemStackSplitter
	{
		/** The {@link ItemStack} to be split. */
		public ItemStack source;

		/** The restulting {@link ItemStack} after the split. */
		public ItemStack split;

		/** The amount of items to split. */
		public int amount = -1;

		/**
		 * Instantiates the {@link ItemStackSplitter}.
		 *
		 * @param source the source
		 */
		public ItemStackSplitter(ItemStack source)
		{
			this.source = checkNotNull(source);
		}

		/**
		 * Splits the {@link ItemStackSplitter#source} by the specified amount.
		 *
		 * @param amount the amount
		 * @return the item stack
		 */
		public ItemStack split(int amount)
		{
			if (source.isEmpty())
			{
				split = ItemStack.EMPTY;
				this.amount = 0;
				return split;
			}

			if (amount == FULL_STACK)
				amount = source.getCount();
			else if (amount == HALF_STACK)
				amount = (int) Math.ceil((float) source.getCount() / 2);
			this.amount = Math.min(amount, source.getCount());

			split = source.splitStack(this.amount);
			if (source.isEmpty())
				source = ItemStack.EMPTY;

			return split;
		}

		@Override
		public String toString()
		{
			return ItemUtils.toString(source) + " -> " + ItemUtils.toString(split) + " (" + amount + ")";
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
		return !(stack1.isEmpty() || stack2.isEmpty()) && stack1.isStackable() && stack1.getItem() == stack2.getItem()
				&& (!stack2.getHasSubtypes() || stack2.getMetadata() == stack1.getMetadata())
				&& ItemStack.areItemStackTagsEqual(stack2, stack1);

	}

	/**
	 * Gets a {@link IBlockState} corresponding to the specified {@link ItemStack}
	 *
	 * @param itemStack the item stack
	 * @return the state from item stack
	 */
	@SuppressWarnings("deprecation")
	public static IBlockState getStateFromItemStack(ItemStack itemStack)
	{
		if (itemStack.isEmpty())
			return null;

		Block block = Block.getBlockFromItem(itemStack.getItem());
		if (block == null)
			return null;

		if (block instanceof MalisisBlock)
			return ((MalisisBlock) block).getStateFromItemStack(itemStack);

		//special case for pistons, because Mojang.
		if (block instanceof BlockPistonBase)
			return block.getDefaultState();

		try
		{
			return block.getStateFromMeta(itemStack.getItem().getMetadata(itemStack.getMetadata()));
		}
		catch (Exception e)
		{
			return block.getDefaultState();
		}
	}

	/**
	 * Gets the {@link ItemStack} matching the specified {@link IBlockState}
	 *
	 * @param state the state
	 * @return the item stack from state
	 */
	public static ItemStack getItemStackFromState(IBlockState state)
	{
		if (state == null)
			return null;
		Item item = Item.getItemFromBlock(state.getBlock());
		if (item == null)
			return ItemStack.EMPTY;
		return new ItemStack(item, 1, state.getBlock().damageDropped(state));
	}

	/**
	 * Constructs an {@link ItemStack} from a string in the format modid:itemName@damagexstackSize.
	 *
	 * @param str the str
	 * @return the item
	 */
	public static ItemStack getItemStack(String str)
	{
		Matcher matcher = pattern.matcher(str);
		if (!matcher.find())
			return ItemStack.EMPTY;

		String itemString = matcher.group("item");
		if (itemString == null)
			return ItemStack.EMPTY;

		String modid = matcher.group("modid");
		if (modid == null)
			modid = "minecraft";

		int damage = 0;
		String strDamage = matcher.group("damage");
		if (strDamage != null)
			damage = strDamage.equals("*") ? OreDictionary.WILDCARD_VALUE : Silenced.get(() -> Integer.parseInt(matcher.group("damage")));
		int size = matcher.group("size") == null ? 1 : Silenced.get(() -> Integer.parseInt(matcher.group("size")));
		if (size == 0)
			size = 1;

		Item item = Item.getByNameOrId(modid + ":" + itemString);
		if (item == null)
			return ItemStack.EMPTY;

		return new ItemStack(item, size, damage);
	}

	/**
	 * {@link ItemStack#copy()} version that doesn't loose the actual item if the stack is empty.
	 *
	 * @param itemStack the item stack
	 * @return the item stack
	 */
	public static ItemStack copy(ItemStack itemStack)
	{
		//we need to make sure the itemStack is not empty before copying
		int originalSize = itemStack.getCount();
		itemStack.setCount(1);

		ItemStack copy = itemStack.copy();
		//set the size back
		copy.setCount(originalSize);
		itemStack.setCount(originalSize);
		return copy;
	}

	/**
	 * ToString version of {@link ItemStack} that doesn't hide the actual item if the stack is empty.
	 *
	 * @param itemStack the item stack
	 * @return the string
	 */
	public static String toString(ItemStack itemStack)
	{
		if (itemStack == null)
			return "null";
		if (itemStack == ItemStack.EMPTY)
			return "Empty";

		return itemStack.getCount() + "x" + itemStack.item.getUnlocalizedName() + "@" + itemStack.getItemDamage();
	}
}
