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

package net.malisis.core.inventory;

import static com.google.common.base.Preconditions.*;

import java.util.HashMap;
import java.util.Map;

import net.malisis.core.util.ItemUtils;
import net.malisis.core.util.ItemUtils.ItemStackSplitter;
import net.malisis.core.util.ItemUtils.ItemStacksMerger;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * Slots contained by {@link MalisisInventory}
 *
 * @author Ordinastie
 *
 */
public class MalisisSlot
{
	/** Inventory containing this {@link MalisisSlot}. */
	private MalisisInventory inventory;
	/** ItemStack held by this {@link MalisisSlot}. */
	private ItemStack itemStack = ItemStack.EMPTY;
	/** ItemStack cached to detect changes. */
	private Map<EntityPlayer, ItemStack> cachedItemStacks = new HashMap<>();
	/** ItemStack currently dragged into the slot. */
	private ItemStack draggedItemStack;
	/** ItemStack cached to detect changes. */
	private Map<EntityPlayer, ItemStack> cachedDraggedItemStacks = new HashMap<>();
	/** Slot position within its {@link MalisisInventory}. */
	public int index;
	/** {@link InventoryState} of this slot. */
	protected InventoryState state = new InventoryState();

	/**
	 * Instantiates a new {@link MalisisSlot}.
	 *
	 * @param inventory the inventory
	 * @param itemStack the item stack
	 */
	public MalisisSlot(MalisisInventory inventory, ItemStack itemStack)
	{
		this.inventory = inventory;
		this.itemStack = itemStack;
	}

	/**
	 * Instantiates a new {@link MalisisSlot}.
	 *
	 * @param inventory the inventory
	 */
	public MalisisSlot(MalisisInventory inventory)
	{
		this(inventory, ItemStack.EMPTY);
	}

	/**
	 * Instantiates a new {@link MalisisSlot}.
	 */
	public MalisisSlot()
	{
		this(null, ItemStack.EMPTY);
	}

	/**
	 * Registers an object to inventory changes.
	 *
	 * @param object the object
	 */
	public void register(Object object)
	{
		if (inventory != null)
			inventory.register(object);
	}

	/**
	 * Sets the inventory containing this {@link MalisisSlot}.
	 *
	 * @param inventory the new inventory
	 */
	public void setInventory(MalisisInventory inventory)
	{
		this.inventory = inventory;
	}

	/**
	 * Sets the slot index in the inventory.
	 *
	 * @param index the new slot index
	 */
	public void setSlotIndex(int index)
	{
		this.index = index;
	}

	/**
	 * Gets the slot index in the inventory.
	 *
	 * @return the slot index
	 */
	public int getSlotIndex()
	{
		return index;
	}

	/**
	 * Gets the {@link MalisisInventory} of this {@link MalisisSlot}.
	 *
	 * @return the inventory
	 */
	public MalisisInventory getInventory()
	{
		return inventory;
	}

	/**
	 * Gets the id of the {@link MalisisInventory} of this {@link MalisisSlot} inside the {@link MalisisInventoryContainer}.
	 *
	 * @return the inventory id
	 */
	public int getInventoryId()
	{
		return inventory.getInventoryId();
	}

	/**
	 * Sets the itemStack contained by this {@link MalisisSlot}. Does not check for slot validity, max stack size etc...
	 *
	 * @param itemStack the new item stack
	 */
	public void setItemStack(ItemStack itemStack)
	{
		this.itemStack = checkNotNull(itemStack);
	}

	/**
	 * Gets the item stack.
	 *
	 * @return the itemStack contained by this {@link MalisisSlot}.
	 */
	public ItemStack getItemStack()
	{
		return this.itemStack;
	}

	/**
	 * Sets the currently dragged ItemStack for this {@link MalisisSlot}.
	 *
	 * @param itemStack the new dragged item stack
	 */
	public void setDraggedItemStack(ItemStack itemStack)
	{
		this.draggedItemStack = checkNotNull(itemStack);
	}

	/**
	 * Gets the dragged item stack.
	 *
	 * @return the currently dragged ItemStack for this {@link MalisisSlot}
	 */
	public ItemStack getDraggedItemStack()
	{
		return draggedItemStack;
	}

	/**
	 * Sets this {@link MalisisSlot} as an output slot. Sets the slot {@link InventoryState state} to deny inserts.
	 */
	public void setOutputSlot()
	{
		state.unset(InventoryState.PLAYER_INSERT | InventoryState.AUTO_INSERT);
	}

	/**
	 * Checks whether this {@link MalisisSlot} is an output slot (if {@link InventoryState state} denies inserts)
	 *
	 * @return true if is output slot, false otherwise
	 */
	public boolean isOutputSlot()
	{
		return !state.is(InventoryState.PLAYER_INSERT) && !state.is(InventoryState.AUTO_INSERT);
	}

	/**
	 * Checks whether this {@link MalisisSlot} is allowed for the <b>state</b>.
	 *
	 * @param state the state
	 * @return true if both the {@link MalisisInventory} and this {@link MalisisSlot} allow the state, false otherwise.
	 */
	public boolean isState(int state)
	{
		return inventory.state.is(state) && this.state.is(state);
	}

	/**
	 * Checks if this {@link MalisisSlot} can contain itemStack. Defers the test to this {@link MalisisInventory inventory}.
	 *
	 * @param itemStack the item stack
	 * @return true if the itemStack can be container in this {@link MalisisSlot}
	 */
	public boolean isItemValid(ItemStack itemStack)
	{
		if (inventory == null)
			return true;

		return inventory.itemValidForSlot(this, itemStack);
	}

	/**
	 * Checks if this {@link MalisisSlot} can accept more itemStack.
	 *
	 * @return true, if is full
	 */
	public boolean isFull()
	{
		return !itemStack.isEmpty() && itemStack.getCount() == Math.min(itemStack.getMaxStackSize(), getSlotStackLimit());
	}

	/**
	 * Checks if this {@link MalisisSlot} can accept more itemStack.
	 *
	 * @return true, if is full
	 */
	public boolean isNotFull()
	{
		return itemStack.isEmpty() || itemStack.getCount() < Math.min(itemStack.getMaxStackSize(), getSlotStackLimit());
	}

	/**
	 * Checks if this {@link MalisisSlot} is empty.
	 *
	 * @return true, if is empty
	 */
	public boolean isEmpty()
	{
		return itemStack.isEmpty();
	}

	/**
	 * Checks if this {@link MalisisSlot} is not empty.
	 *
	 * @return true, if is empty
	 */
	public boolean isNotEmpty()
	{
		return !itemStack.isEmpty();
	}

	/**
	 * Called when itemStack is set.
	 */
	public void onSlotChanged()
	{
		this.inventory.onSlotChanged(this);
	}

	/**
	 * Called when itemStack is picked up from this {@link MalisisSlot}.
	 *
	 * @param player the player
	 * @param itemStack the item stack
	 */
	public void onPickupFromSlot(EntityPlayer player, ItemStack itemStack)
	{
		onSlotChanged();
	}

	/**
	 * Sets the item stack size.
	 *
	 * @param stackSize the stack size
	 * @return the amount of items that were added to the slot.
	 */
	public int setItemStackSize(int stackSize)
	{
		if (itemStack.isEmpty())
			return 0;

		int start = itemStack.getCount();
		itemStack.setCount(Math.min(stackSize, Math.min(itemStack.getMaxStackSize(), getSlotStackLimit())));
		return itemStack.getCount() - start;
	}

	/**
	 * Adds the item stack size.
	 *
	 * @param stackSize the stack size
	 * @return the amount of items that were added to the slot
	 */
	public int addItemStackSize(int stackSize)
	{
		if (itemStack.isEmpty())
			return 0;

		return setItemStackSize(itemStack.getCount() + stackSize);
	}

	/**
	 * Extracts the full stack from this {@link MalisisSlot}.
	 *
	 * @return the item stack
	 */
	public ItemStack extract()
	{
		if (isEmpty())
			return ItemStack.EMPTY;
		ItemStack is = getItemStack();
		setItemStack(ItemStack.EMPTY);
		onSlotChanged();
		return is;
	}

	/**
	 * Extract a specified <b>amount</b> from this {@link MalisisSlot}.
	 *
	 * @param amount the amount
	 * @return the {@link ItemStack} extracted
	 */
	public ItemStack extract(int amount)
	{
		ItemStackSplitter iss = new ItemUtils.ItemStackSplitter(getItemStack());
		iss.split(amount);
		setItemStack(iss.source);
		//if (hasChanged())
		onSlotChanged();
		return iss.split;
	}

	/**
	 * Inserts an {@link ItemStack} into this {@link MalisisSlot}.
	 *
	 * @param insert the itemStack to insert
	 * @return the itemStack that couldn't fit into the slot
	 */
	public ItemStack insert(ItemStack insert)
	{
		return insert(insert, insert.getCount(), false);
	}

	/**
	 * Inserts a specified <b>amount</b> of {@link ItemStack} into this {@link MalisisSlot}.
	 *
	 * @param insert the itemStack to insert
	 * @param amount the amount to insert
	 * @return the itemStack that couldn't fit into the slot
	 */
	public ItemStack insert(ItemStack insert, int amount)
	{
		return insert(insert, amount, false);
	}

	/**
	 * Inserts a specified <b>amount</b> of {@link ItemStack} into this {@link MalisisSlot}.<br>
	 * If <b>force</b> is <code>true</code>, the current <code>ItemStack</code> in the slot will be replaced if it cannot be merged.
	 *
	 * @param insert the itemStack to insert
	 * @param amount the amount to insert
	 * @param force whether the itemStack should be forced in the slot
	 * @return the itemStack that couldn't fit into the slot
	 */
	public ItemStack insert(ItemStack insert, int amount, boolean force)
	{
		if (insert.isEmpty())
			return ItemStack.EMPTY;

		if (!isItemValid(insert))
			return insert;

		ItemStacksMerger ism = new ItemUtils.ItemStacksMerger(insert, itemStack);
		if (!ism.canMerge() || isFull())
		{
			if (!force)
				return insert;

			ItemStack slotStack = extract(ItemUtils.FULL_STACK);
			ItemStack insertStack = insert.copy();
			if (insert(insertStack, amount, false).isEmpty())
			{
				setItemStack(slotStack);
				return insert;
			}
			else
				return slotStack;
		}

		ism.merge(amount, getSlotStackLimit());
		setItemStack(ism.into);

		//if (hasChanged())
		onSlotChanged();

		return ism.merge;
	}

	/**
	 * Gets the maximum stackSize available for this slot. Gets the value from this {@link MalisisInventory inventory}.
	 *
	 * @return the slot stack limit.
	 */
	public int getSlotStackLimit()
	{
		if (inventory == null)
			return 64;

		return this.inventory.getInventoryStackLimit();
	}

	/**
	 * Checks whether this slot has changed for the {@link EntityPlayer}.
	 *
	 * @param player the player
	 * @return true, if changed, false otherwise
	 */
	public boolean hasChanged(EntityPlayer player)
	{
		ItemStack cached = cachedItemStacks.get(player);
		ItemStack cachedDragged = cachedDraggedItemStacks.get(player);
		return !ItemStack.areItemStacksEqual(itemStack, cached != null ? cached : ItemStack.EMPTY)
				|| !ItemStack.areItemStacksEqual(draggedItemStack, cachedDragged != null ? cachedDragged : ItemStack.EMPTY);
	}

	/**
	 * Update the cached {@link ItemStack itemStacks} of this {@link MalisisSlot} for the {@link EntityPlayer}.
	 *
	 * @param player the player
	 */
	public void updateCache(EntityPlayer player)
	{
		cachedItemStacks.put(player, itemStack.copy());
		cachedDraggedItemStacks.put(player, draggedItemStack.copy());
	}

	/**
	 * Clear cache for the {@link EntityPlayer}.
	 *
	 * @param player the player
	 */
	public void clearCache(EntityPlayer player)
	{
		cachedItemStacks.remove(player);
		cachedDraggedItemStacks.remove(player);
	}

	@Override
	public String toString()
	{
		return index + (inventory != null ? "/" + inventory.getSize() : "") + " > " + itemStack;
	}
}
