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

import net.malisis.core.inventory.IInventoryProvider;
import net.malisis.core.inventory.MalisisInventory;
import net.malisis.core.inventory.MalisisSlot;
import net.malisis.core.util.ItemUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public abstract class TileEntityInventory extends TileEntity implements IInventoryProvider, IInventory
{
	protected MalisisInventory inventory;

	public TileEntityInventory(MalisisInventory inventory)
	{
		this.inventory = inventory;
	}

	@Override
	public MalisisInventory[] getInventories(Object... data)
	{
		return new MalisisInventory[] { inventory };
	}

	@Override
	public MalisisInventory[] getInventories(EnumFacing side, Object... data)
	{
		return getInventories(data);
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
		return inventory.getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot(int slotNumber)
	{
		return inventory.getItemStack(slotNumber);
	}

	@Override
	public ItemStack decrStackSize(int slotNumber, int amount)
	{
		return (new ItemUtils.ItemStackSplitter(inventory.getItemStack(slotNumber))).split(amount);
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slotNumber)
	{
		return inventory.getItemStack(slotNumber);
	}

	@Override
	public void setInventorySlotContents(int slotNumber, ItemStack itemStack)
	{
		inventory.setItemStack(slotNumber, itemStack);
	}

	@Override
	public String getCommandSenderName()
	{
		return null;
	}

	@Override
	public boolean hasCustomName()
	{
		return false;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return inventory.getInventoryStackLimit();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player)
	{
		return true;
	}

	@Override
	public boolean isItemValidForSlot(int slotNumber, ItemStack itemStack)
	{
		MalisisSlot slot = inventory.getSlot(slotNumber);
		return slot != null && slot.isItemValid(itemStack);
	}
}
