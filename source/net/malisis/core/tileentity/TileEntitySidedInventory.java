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

package net.malisis.core.tileentity;

import java.util.HashMap;

import net.malisis.core.inventory.IInventoryProvider;
import net.malisis.core.inventory.MalisisInventory;
import net.malisis.core.inventory.MalisisSlot;
import net.malisis.core.util.ItemUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public abstract class TileEntitySidedInventory extends TileEntity implements IInventoryProvider, ISidedInventory
{
	protected HashMap<ForgeDirection, MalisisInventory> inventories = new HashMap<>();
	protected HashMap<Integer, ForgeDirection> ranges = new HashMap<>();
	protected int totalSize = 0;

	public TileEntitySidedInventory()
	{}

	protected void addSidedInventory(MalisisInventory inventory, ForgeDirection... sides)
	{
		int size = inventory.getSizeInventory();
		totalSize += size;
		for (ForgeDirection side : sides)
		{
			if (inventories.get(side) == null)
			{
				inventories.put(side, inventory);
				ranges.put(totalSize, side);
			}
		}
	}

	/*private int convertSlotNumber(int slotNumber)
	{
		int offset = 0;
		for (Entry<Integer, ForgeDirection> entry : ranges.entrySet())
		{
			if (slotNumber < entry.getKey())
				return slotNumber - offset;
			offset += entry.getKey();
		}

		return -1;
	}

	public MalisisInventory getInventory(int slotNumber)
	{
		for (Entry<Integer, ForgeDirection> entry : ranges.entrySet())
		{
			if (slotNumber < entry.getKey())
				return getInventory(entry.getValue());
		}

		return null;
	}*/

	private int convertSlotNumber(int slotNumber)
	{
		return slotNumber >> 3;
	}

	public MalisisInventory getInventory(int slotNumber)
	{
		return inventories.get(ForgeDirection.getOrientation(slotNumber & 0b111));
	}

	@Override
	public MalisisInventory getInventory(ForgeDirection side, Object... data)
	{
		return inventories.get(side);
	}

	@Override
	public void readFromNBT(NBTTagCompound tagCompound)
	{
		super.readFromNBT(tagCompound);

	}

	@Override
	public void writeToNBT(NBTTagCompound tagCompound)
	{
		super.writeToNBT(tagCompound);
	}

	@Override
	public int getSizeInventory()
	{
		return totalSize;
	}

	@Override
	public ItemStack getStackInSlot(int slotNumber)
	{
		return getInventory(slotNumber).getItemStack(slotNumber);
	}

	@Override
	public ItemStack decrStackSize(int slotNumber, int amount)
	{
		return (new ItemUtils.ItemStackSplitter(getInventory(slotNumber).getItemStack(slotNumber))).split(amount);
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slotNumber)
	{
		return getInventory(slotNumber).getItemStack(slotNumber);
	}

	@Override
	public void setInventorySlotContents(int slotNumber, ItemStack itemStack)
	{
		getInventory(slotNumber).setItemStack(slotNumber, itemStack);
	}

	@Override
	public String getInventoryName()
	{
		return null;
	}

	@Override
	public boolean hasCustomInventoryName()
	{
		return false;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player)
	{
		return true;
	}

	@Override
	public void openInventory()
	{}

	@Override
	public void closeInventory()
	{}

	@Override
	public boolean isItemValidForSlot(int slotNumber, ItemStack itemStack)
	{
		MalisisSlot slot = getInventory(slotNumber).getSlot(convertSlotNumber(slotNumber));
		if (slot == null)
			return false;
		return slot.isItemValid(itemStack);
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		MalisisInventory inventory = inventories.get(ForgeDirection.getOrientation(side));
		if (inventory == null)
			return new int[0];

		int[] a = new int[inventory.getSizeInventory()];
		for (int i = 0; i < inventory.getSizeInventory(); i++)
			a[i] = (i << 3) | side;

		return a;
	}

	@Override
	public boolean canInsertItem(int slotNumber, ItemStack itemStack, int side)
	{
		MalisisInventory inventory = inventories.get(ForgeDirection.getOrientation(side));
		if (inventory == null)
			return false;

		return inventory.isItemValidForSlot(convertSlotNumber(slotNumber), itemStack);
	}

	@Override
	public boolean canExtractItem(int slotNumber, ItemStack itemStack, int side)
	{
		return true;
	}
}
