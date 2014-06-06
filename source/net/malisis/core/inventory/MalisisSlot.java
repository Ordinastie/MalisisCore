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
	 * Slot position within {@link MalisisInventory inventory}
	 */
	public int slotNumber;

	public MalisisSlot(MalisisInventory inventory, ItemStack itemStack, int index)
	{
		this.inventory = inventory;
		slotNumber = index;
		this.itemStack = itemStack;
	}

	public MalisisSlot(MalisisInventory inventory, int index)
	{
		this(inventory, null, index);
	}

	public void register(Object object)
	{
		inventory.register(object);
	}

	/**
	 * Set the itemStack contained by this <code>MalisisSlot</code>
	 * 
	 * @param itemStack
	 */
	public void setItemStack(ItemStack itemStack)
	{
		if (ItemStack.areItemStacksEqual(this.itemStack, itemStack))
			return;

		this.itemStack = itemStack;
		onSlotChanged();
	}

	/**
	 * @return the itemStack contained by this <code>MalisisSlot</code>
	 */
	public ItemStack getItemStack()
	{
		return this.itemStack;
	}

	/**
	 * Check if this <code>MalisisSlot</code> can contain itemStack. Defers the test to this {@link MalisisInventory inventory}.
	 * 
	 * @return true if the itemStack can be container in this <code>MalisisSlot</code>
	 */
	public boolean isItemValid(ItemStack itemStack)
	{
		return inventory.itemValidForSlot(this, itemStack);
	}

	/**
	 * Called when itemStack is set
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

	/**
	 * @return the maximum stackSize available for this slot. Gets the value from this {@link MalisisInventory inventory}.
	 */
	public int getSlotStackLimit()
	{
		return this.inventory.getInventoryStackLimit();
	}

	@Override
	public String toString()
	{
		return slotNumber + "/" + inventory.getSizeInventory() + " > " + itemStack;
	}
}
