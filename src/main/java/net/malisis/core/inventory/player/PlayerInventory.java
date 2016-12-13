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

package net.malisis.core.inventory.player;

import net.malisis.core.inventory.MalisisInventory;
import net.malisis.core.inventory.MalisisSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class PlayerInventory extends MalisisInventory
{

	public PlayerInventory(EntityPlayer p)
	{
		super(null, 0);
		for (int i = 0; i < 36; i++)
			slots.add(new PlayerInventorySlot(this, p, i));
	}

	@Override
	public void setSlots(MalisisSlot... slots)
	{
		throw new IllegalStateException();
	}

	@Override
	public void overrideSlot(MalisisSlot slot, int slotIndex)
	{
		if (slot instanceof PlayerInventorySlot)
			super.overrideSlot(slot, slotIndex);
	}

	@Override
	public ItemStack transferInto(ItemStack itemStack)
	{
		return transferInto(itemStack, true);
	}

	@Override
	public ItemStack transferInto(ItemStack itemStack, boolean reversed)
	{
		if (itemStack.isEmpty())
			return ItemStack.EMPTY;
		//fill existing stacks in the hotbar first
		itemStack = transferIntoHotbar(itemStack, false, reversed);
		if (itemStack.isEmpty())
			return ItemStack.EMPTY;
		//fill existing stacks in the inventory
		itemStack = transferIntoInventory(itemStack, false, reversed);
		if (itemStack.isEmpty())
			return ItemStack.EMPTY;
		//fill empty slots in the hotbar
		itemStack = transferIntoHotbar(itemStack, true, reversed);
		if (itemStack.isEmpty())
			return ItemStack.EMPTY;
		//fill empty slots in the inventory (returns what's left)
		return transferIntoInventory(itemStack, true, reversed);
	}

	private ItemStack transferIntoHotbar(ItemStack itemStack, boolean emptySlot, boolean reversed)
	{
		return transferInto(itemStack, emptySlot, reversed ? 8 : 0, reversed ? 0 : 8);
	}

	private ItemStack transferIntoInventory(ItemStack itemStack, boolean emptySlot, boolean reversed)
	{
		return transferInto(itemStack, emptySlot, reversed ? 35 : 9, reversed ? 9 : 35);
	}

	@Override
	public boolean isItemValidForSlot(int slotNumber, ItemStack itemStack)
	{
		return true;
	}

}
