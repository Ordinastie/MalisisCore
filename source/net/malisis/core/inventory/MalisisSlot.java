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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class MalisisSlot
{
	/**
	 * Inventory containing this <code>MalisisSlot</code>.
	 */
	private MalisisInventory inventory;
	/**
	 * ItemStack held by this <code>MalisisSlot</code>
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
	 * Slot position within {@link MalisisInventory inventory}
	 */
	public int slotNumber;
	/**
	 * Whether the slot is an output slot. If set to true, isItemValid() always return false
	 */
	protected boolean isOutputSlot = false;

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
	 * Sets the inventory containing this <code>MalisisSlot</code>
	 *
	 * @param inventory
	 */
	public void setInventory(MalisisInventory inventory)
	{
		this.inventory = inventory;
	}

	public MalisisInventory getInventory()
	{
		return inventory;
	}

	public int getInventoryId()
	{
		return inventory.getInventoryId();
	}

	/**
	 * Sets the itemStack contained by this <code>MalisisSlot</code>
	 *
	 * @param itemStack
	 */
	public void setItemStack(ItemStack itemStack)
	{
		this.itemStack = itemStack;
	}

	/**
	 * @return the itemStack contained by this <code>MalisisSlot</code>
	 */
	public ItemStack getItemStack()
	{
		return this.itemStack;
	}

	/**
	 *
	 */
	public void setDraggedItemStack(ItemStack itemStack)
	{
		this.draggedItemStack = itemStack;
	}

	public ItemStack getDraggedItemStack()
	{
		return draggedItemStack;
	}

	/**
	 * Sets whether this <code>MalisisSlot</code> is an output slot. If set to true, isItemValid() always return false
	 *
	 * @param isOutput
	 */
	public void setOutputSlot(boolean isOutput)
	{
		isOutputSlot = isOutput;
	}

	/**
	 * Checks if this <code>MalisisSlot</code> can contain itemStack. Defers the test to this {@link MalisisInventory inventory}.
	 *
	 * @return true if the itemStack can be container in this <code>MalisisSlot</code>
	 */
	public boolean isItemValid(ItemStack itemStack)
	{
		if (isOutputSlot)
			return false;

		if (inventory == null)
			return true;

		return inventory.itemValidForSlot(this, itemStack);
	}

	/**
	 * Checks if this <code>MalisisSlot</code> can accept more itemStack.
	 * 
	 * @return
	 */
	public boolean isFull()
	{
		return itemStack != null && itemStack.stackSize == Math.min(itemStack.getMaxStackSize(), getSlotStackLimit());
	}

	/**
	 * Called when itemStack is set.
	 */
	public void onSlotChanged()
	{
		this.inventory.onSlotChanged(this);
	}

	/**
	 * Called when itemStack is picked up from this <code>MalisisSlot</code>
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
		int start = itemStack.stackSize;
		if (stackSize <= 0)
		{
			itemStack = null;
			return start;
		}

		itemStack.stackSize = Math.min(stackSize, Math.min(itemStack.getMaxStackSize(), getSlotStackLimit()));
		return itemStack.stackSize - start;
	}

	public int addItemStackSize(int stackSize)
	{
		if (itemStack == null)
			return 0;

		return setItemStackSize(itemStack.stackSize + stackSize);
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
