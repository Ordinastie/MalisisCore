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

import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import org.apache.commons.lang3.ArrayUtils;

/**
 * @author Ordinastie
 *
 */
public interface ISidedInventoryProvider extends IInventoryProvider, ISidedInventory
{

	/**
	 * Gets the {@link MalisisInventory} accessible from the side of this {@link IInventoryProvider}.
	 *
	 * @param side the side
	 * @param data the data
	 * @return the inventory
	 */
	public MalisisInventory getInventory(EnumFacing side);

	/**
	 * Gets all the {@link MalisisInventory inventories} accessible from the side of this {@link IInventoryProvider}.
	 *
	 * @param side the side
	 * @param data null for TileEntity, ItemStack for Item
	 * @return the inventories
	 */
	public default MalisisInventory[] getInventories(EnumFacing side)
	{
		return new MalisisInventory[] { getInventory(side) };
	}

	/**
	 * Gets all the {@link MalisisInventory inventories} accessible from all the side of this {@link IInventoryProvider}.
	 *
	 * @param data the data
	 * @return the inventories
	 */
	@Override
	public default MalisisInventory[] getInventories(Object... data)
	{
		MalisisInventory[] inventories = new MalisisInventory[0];
		for (EnumFacing facing : EnumFacing.values())
			inventories = ArrayUtils.addAll(inventories, getInventories(facing));

		return inventories;
	}

	/**
	 * Converts a global slot number to its actual number in a {@link MalisisInventory}.
	 *
	 * @param slotNumber the slot number
	 * @return the int
	 */
	public default int convertSlotNumber(int slotNumber)
	{
		return slotNumber >> 3;
	}

	public default boolean isSlotInSide(int slotNumber, EnumFacing side)
	{
		return getInventory(slotNumber) == getInventory(side);
	}

	/**
	 * Gets the {@link MalisisInventory} associated with a global slot number.
	 *
	 * @param slotNumber the slot number
	 * @return the inventory
	 */
	public default MalisisInventory getInventory(int slotNumber)
	{
		return getInventory(EnumFacing.getFront(slotNumber & 7));
	}

	@Override
	public default boolean isItemValidForSlot(int slotNumber, ItemStack itemStack)
	{
		MalisisSlot slot = getInventory(slotNumber).getSlot(convertSlotNumber(slotNumber));
		return slot != null && slot.isItemValid(itemStack);
	}

	/**
	 * Gets the global numbers for the slots for accessible from a side in this {@link IInventoryProvider}.
	 *
	 * @param side the side
	 * @return the slots for face
	 */
	@Override
	public default int[] getSlotsForFace(EnumFacing side)
	{
		MalisisInventory inventory = getInventory(side);
		if (inventory == null)
			return new int[0];

		int[] a = new int[inventory.getSizeInventory()];
		for (int i = 0; i < inventory.getSizeInventory(); i++)
			a[i] = (i << 3) | side.getIndex();

		return a;
	}

	/**
	 * Checks whether you can insert the {@link ItemStack} in the slot associated to the global number.
	 *
	 * @param slotNumber the slot number
	 * @param itemStack the item stack
	 * @param side the side
	 * @return true, if the itemStack can be inserted
	 */
	@Override
	public default boolean canInsertItem(int slotNumber, ItemStack itemStack, EnumFacing side)
	{
		if (isSlotInSide(slotNumber, side))
			return false;

		MalisisInventory inventory = getInventory(side);
		if (inventory == null)
			return false;

		MalisisSlot slot = inventory.getSlot(convertSlotNumber(slotNumber));
		return slot != null && slot.state.is(InventoryState.AUTO_INSERT) && slot.isItemValid(itemStack);
	}

	/**
	 * Checks whether you can extract the {@link ItemStack} in the slot associated to the global number.
	 *
	 * @param slotNumber the slot number
	 * @param itemStack the item stack
	 * @param side the side
	 * @return true, if successful
	 */
	@Override
	public default boolean canExtractItem(int slotNumber, ItemStack itemStack, EnumFacing side)
	{
		if (isSlotInSide(slotNumber, side))
			return false;

		MalisisInventory inventory = getInventory(side);
		if (inventory == null)
			return false;

		MalisisSlot slot = inventory.getSlot(convertSlotNumber(slotNumber));
		return slot != null && slot.state.is(InventoryState.AUTO_EXTRACT);
	}
}
