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

import net.malisis.core.util.ItemUtils;
import net.malisis.core.util.ItemUtils.ItemStackSplitter;
import net.malisis.core.util.ItemUtils.ItemStacksMerger;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class MalisisSlot
{
	/**
	 * Inventory containing this {@link MalisisSlot}.
	 */
	private MalisisInventory inventory;
	/**
	 * ItemStack held by this {@link MalisisSlot}
	 */
	private ItemStack itemStack;
	/**
	 * ItemStack cached to detect changes
	 */
	private ItemStack cachedItemStack;
	/**
	 * ItemStack currently dragged into the slot
	 */
	private ItemStack draggedItemStack;
	/**
	 * ItemStack cached to detect changes
	 */
	private ItemStack cachedDraggedItemStack;
	/**
	 * Slot position within its {@link MalisisInventory}
	 */
	public int slotNumber;
	/**
	 * {@link InventoryState} of this slot.
	 */
	protected InventoryState state = new InventoryState();

	public MalisisSlot(MalisisInventory inventory, ItemStack itemStack, int index)
	{
		this.inventory = inventory;
		this.slotNumber = index;
		this.itemStack = itemStack;
	}

	public MalisisSlot(MalisisInventory inventory, int index)
	{
		this(inventory, null, index);
	}

	public MalisisSlot(int index)
	{
		this(null, null, index);
	}

	/**
	 * Registers an object to inventory changes.
	 *
	 * @param object
	 */
	public void register(Object object)
	{
		if (inventory != null)
			inventory.register(object);
	}

	/**
	 * Sets the inventory containing this {@link MalisisSlot}.
	 *
	 * @param inventory
	 */
	public void setInventory(MalisisInventory inventory)
	{
		this.inventory = inventory;
	}

	/**
	 * Gets the {@link MalisisInventory} of this {@link MalisisSlot}.
	 *
	 * @return
	 */
	public MalisisInventory getInventory()
	{
		return inventory;
	}

	/**
	 * Gets the id of the {@link MalisisInventory} of this {@link MalisisSlot} inside the {@link MalisisInventoryContainer}.
	 *
	 * @return
	 */
	public int getInventoryId()
	{
		return inventory.getInventoryId();
	}

	/**
	 * Sets the itemStack contained by this {@link MalisisSlot}. Does not check for slot validity, max stack size etc...
	 *
	 * @param itemStack
	 */
	public void setItemStack(ItemStack itemStack)
	{
		this.itemStack = itemStack;
	}

	/**
	 * @return the itemStack contained by this {@link MalisisSlot}.
	 */
	public ItemStack getItemStack()
	{
		return this.itemStack;
	}

	/**
	 * Sets the currently dragged ItemStack for this {@link MalisisSlot}.
	 *
	 * @param itemStack
	 */
	public void setDraggedItemStack(ItemStack itemStack)
	{
		this.draggedItemStack = itemStack;
	}

	/**
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
	 * @return whether this {@link MalisisSlot} is an output slot (if {@link InventoryState state} denies inserts)
	 */
	public boolean isOutputSlot()
	{
		return !state.is(InventoryState.PLAYER_INSERT) && !state.is(InventoryState.AUTO_INSERT);
	}

	/**
	 * Checks whether this {@link MalisisSlot} is allowed for the <b>state</b>.
	 *
	 * @param state
	 * @return false if either the inventory or the slot denies the state.
	 */
	public boolean isState(int state)
	{
		return inventory.state.is(state) && this.state.is(state);
	}

	/**
	 * Checks if this {@link MalisisSlot} can contain itemStack. Defers the test to this {@link MalisisInventory inventory}.
	 *
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
	 * @return
	 */
	public boolean isFull()
	{
		return itemStack != null && itemStack.stackSize == Math.min(itemStack.getMaxStackSize(), getSlotStackLimit());
	}

	/**
	 * Checks if this {@link MalisisSlot} is empty.
	 *
	 * @return
	 */
	public boolean isEmpty()
	{
		return itemStack == null || itemStack.stackSize == 0;
	}

	/**
	 * Called when itemStack is set.
	 */
	public void onSlotChanged()
	{
		this.inventory.onSlotChanged(this);
	}

	/**
	 * Called when itemStack is picked up from this {@link MalisisSlot}
	 *
	 * @param player
	 * @param itemStack
	 */
	public void onPickupFromSlot(EntityPlayer player, ItemStack itemStack)
	{
		onSlotChanged();
	}

	public int setItemStackSize(int stackSize)
	{
		if (itemStack == null)
			return 0;
		if (stackSize <= 0)
			stackSize = 0;

		int start = itemStack.stackSize;
		itemStack.stackSize = Math.min(stackSize, Math.min(itemStack.getMaxStackSize(), getSlotStackLimit()));
		return itemStack.stackSize - start;
	}

	public int addItemStackSize(int stackSize)
	{
		if (itemStack == null)
			return 0;

		return setItemStackSize(itemStack.stackSize + stackSize);
	}

	public ItemStack extract(int amount)
	{
		ItemStackSplitter iss = new ItemUtils.ItemStackSplitter(getItemStack());
		iss.split(amount);
		setItemStack(iss.source);
		if (hasChanged())
			onSlotChanged();
		return iss.split;
	}

	public ItemStack insert(ItemStack insert)
	{
		return insert(insert, insert != null ? insert.stackSize : 0, false);
	}

	public ItemStack insert(ItemStack insert, int amount)
	{
		return insert(insert, amount, false);
	}

	public ItemStack insert(ItemStack insert, int amount, boolean force)
	{
		if (insert == null)
			return null;

		if (!isItemValid(insert))
			return insert;

		ItemStacksMerger ism = new ItemUtils.ItemStacksMerger(insert, itemStack);
		if (!ism.canMerge() || isFull())
		{
			if (!force)
				return insert;

			ItemStack slotStack = extract(ItemUtils.FULL_STACK);
			ItemStack insertStack = insert.copy();
			if (insert(insertStack, amount, false) != null)
			{
				setItemStack(slotStack);
				return insert;
			}
			else
				return slotStack;
		}

		ism.merge(amount, getSlotStackLimit());
		setItemStack(ism.into);

		if (hasChanged())
			onSlotChanged();

		return ism.merge;
	}

	/**
	 * @return the maximum stackSize available for this slot. Gets the value from this {@link MalisisInventory inventory}.
	 */
	public int getSlotStackLimit()
	{
		if (inventory == null)
			return 64;

		return this.inventory.getInventoryStackLimit();
	}

	public boolean hasChanged()
	{
		return !ItemStack.areItemStacksEqual(itemStack, cachedItemStack)
				|| !ItemStack.areItemStacksEqual(draggedItemStack, cachedDraggedItemStack);
	}

	public void updateCache()
	{
		cachedItemStack = itemStack != null ? itemStack.copy() : null;
		cachedDraggedItemStack = draggedItemStack != null ? draggedItemStack.copy() : null;
	}

	@Override
	public String toString()
	{
		return slotNumber + (inventory != null ? "/" + inventory.getSizeInventory() : "") + " > " + itemStack;
	}
}
