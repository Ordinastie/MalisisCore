package net.malisis.core.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;

abstract public class TileEntityInventory extends TileEntity implements IInventory
{
	protected ItemStack[] slots;
	protected String customName;
	protected String unLocalizedName;

	public TileEntityInventory()
	{
		slots = new ItemStack[getSizeInventory()];
	}

	@Override
	public ItemStack getStackInSlot(int slot)
	{
		if (slot < 0 || slot >= getSizeInventory())
			return null;

		return slots[slot];
	}

	@Override
	public ItemStack decrStackSize(int slot, int count)
	{
		ItemStack stack = getStackInSlot(slot);
		if (stack == null)
			return null;

		if (stack.stackSize <= count)
		{
			setInventorySlotContents(slot, null);
			return stack;
		}
		else
		{
			ItemStack ret = stack.splitStack(count);
			if (stack.stackSize == 0)
				setInventorySlotContents(slot, null);
			return ret;
		}
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot)
	{
		if (this.slots[slot] != null)
		{
			ItemStack itemstack = getStackInSlot(slot);
			setInventorySlotContents(slot, null);
			return itemstack;
		}
		else
		{
			return null;
		}
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack)
	{
		if (slot < 0 || slot > getSizeInventory())
			return;

		this.slots[slot] = stack;

		if (stack != null && stack.stackSize > this.getInventoryStackLimit())
			stack.stackSize = this.getInventoryStackLimit();
		this.markDirty();
	}

	@Override
	public String getInventoryName()
	{
		return hasCustomInventoryName() ? customName : unLocalizedName;
	}

	@Override
	public boolean hasCustomInventoryName()
	{
		return this.customName != null && this.customName.length() > 0;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player)
	{
		if (worldObj.getTileEntity(xCoord, yCoord, zCoord) != this)
			return false;
		return player.getDistanceSq((double) xCoord + 0.5D, (double) yCoord + 0.5D, (double) zCoord + 0.5D) <= 64.0D;
	}

	@Override
	public void readFromNBT(NBTTagCompound tagCompound)
	{
		super.readFromNBT(tagCompound);
		this.slots = new ItemStack[this.getSizeInventory()];

		if (tagCompound.hasKey("CustomName", Constants.NBT.TAG_STRING))
			this.customName = tagCompound.getString("CustomName");

		NBTTagList nbttaglist = tagCompound.getTagList("Items", 10);
		for (int i = 0; i < nbttaglist.tagCount(); ++i)
		{
			NBTTagCompound stackTag = nbttaglist.getCompoundTagAt(i);
			int slot = stackTag.getByte("Slot") & 255;
			setInventorySlotContents(slot, ItemStack.loadItemStackFromNBT(stackTag));
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound tagCompound)
	{
		super.writeToNBT(tagCompound);

		NBTTagList itemList = new NBTTagList();
		for (int i = 0; i < slots.length; i++)
		{
			ItemStack stack = getStackInSlot(i);
			if (stack != null)
			{
				NBTTagCompound tag = new NBTTagCompound();
				tag.setByte("Slot", (byte) i);
				stack.writeToNBT(tag);
				itemList.appendTag(tag);
			}
		}
		tagCompound.setTag("Items", itemList);
	}

	@Override
	public Packet getDescriptionPacket()
	{
		NBTTagCompound nbt = new NBTTagCompound();
		this.writeToNBT(nbt);
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, nbt);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt)
	{
		this.readFromNBT(pkt.func_148857_g());
	}

	@Override
	public void openInventory()
	{}

	@Override
	public void closeInventory()
	{}

	@Override
	public boolean isItemValidForSlot(int var1, ItemStack var2)
	{
		return true;
	}

}
