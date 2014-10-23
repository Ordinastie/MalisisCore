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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

import net.malisis.core.MalisisCore;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.inventory.player.PlayerInventory;
import net.malisis.core.packet.OpenInventoryMessage;
import net.malisis.core.util.EntityUtils;
import net.malisis.core.util.ItemUtils;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

import com.google.common.eventbus.EventBus;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class MalisisInventory implements IInventory
{
	private static EventBus bus = new EventBus();

	protected Set<MalisisInventoryContainer> containers = Collections.newSetFromMap(new WeakHashMap<MalisisInventoryContainer, Boolean>());
	/**
	 * The inventory id inside the container.
	 */
	protected int inventoryId;
	/**
	 * Object containing this {@link MalisisInventory}.
	 */
	protected IInventoryProvider inventoryProvider;
	/**
	 * ItemStack holding the inventory when inventoryProvider is an Item
	 */
	protected ItemStack itemStackProvider;

	/**
	 * Slots for this {@link MalisisInventory}.
	 */
	protected MalisisSlot[] slots;
	/**
	 * Name for this inventory
	 */
	protected String name;
	/**
	 * Size of this {@link MalisisInventory}.
	 */
	protected int size;
	/**
	 * Maximum stack size for the slots
	 */
	protected int slotMaxStackSize = 64;

	public InventoryState state = new InventoryState();

	public MalisisInventory(IInventoryProvider provider, int size)
	{
		this.inventoryProvider = provider;
		MalisisSlot[] slots = new MalisisSlot[size];
		for (int i = 0; i < size; i++)
			slots[i] = new MalisisSlot(this, i);

		setSlots(slots);
	}

	public MalisisInventory(IInventoryProvider provider, MalisisSlot[] slots)
	{
		this.inventoryProvider = provider;
		setSlots(slots);
	}

	/**
	 * @return the {@link IInventoryProvider} of this {@link MalisisInventory}.
	 */
	public IInventoryProvider getProvider()
	{
		return inventoryProvider;
	}

	/**
	 * Sets the slots for this {@link MalisisInventory}.
	 *
	 * @param slots
	 */
	public void setSlots(MalisisSlot[] slots)
	{
		this.size = slots.length;
		this.slots = slots;
		for (MalisisSlot slot : slots)
			slot.setInventory(this);
	}

	/**
	 * Overrides a specific slot with a new one.
	 *
	 * @param slot
	 * @param slotNumber
	 */
	public void overrideSlot(MalisisSlot slot, int slotNumber)
	{
		if (slotNumber < 0 || slotNumber >= getSizeInventory())
			return;

		slots[slotNumber] = slot;
		slot.setInventory(this);
	}

	/**
	 * Registers an object for the events fired by this {@link MalisisInventory}.
	 *
	 * @param object
	 */
	public void register(Object object)
	{
		bus.register(object);
	}

	/**
	 * Sets the id of this {@link MalisisInventory} inside its container.
	 *
	 * @param id
	 */
	public void setInventoryId(int id)
	{
		inventoryId = id;
		if (itemStackProvider == null)
			return;

		NBTTagCompound tag = itemStackProvider.stackTagCompound;
		if (tag == null)
		{
			tag = new NBTTagCompound();
			itemStackProvider.stackTagCompound = tag;
		}
		itemStackProvider.stackTagCompound.setInteger("inventoryId", id);
	}

	/**
	 * @return the id of this {@link MalisisInventory} inside the {@link MalisisInventoryContainer}.
	 */
	public int getInventoryId()
	{
		return inventoryId;
	}

	// #region getters/setters
	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public String getInventoryName()
	{
		return name;
	}

	@Override
	public boolean hasCustomInventoryName()
	{
		return name != null;
	}

	/**
	 * Gets the slot at position slotNumber.
	 *
	 * @param slotNumber
	 * @return
	 */
	public MalisisSlot getSlot(int slotNumber)
	{
		if (slotNumber < 0 || slotNumber >= getSizeInventory())
			return null;

		return slots[slotNumber];
	}

	/**
	 * @return all slots from this {@link MalisisInventory}.
	 */
	public MalisisSlot[] getSlots()
	{
		return slots;
	}

	/**
	 * Gets the itemStack from the slot at position slotNumber.
	 *
	 * @param slotNumber
	 * @return
	 */
	public ItemStack getItemStack(int slotNumber)
	{
		MalisisSlot slot = getSlot(slotNumber);
		return slot != null ? slot.getItemStack() : null;
	}

	/**
	 * Sets the itemStack for the slot at position slotNumber.
	 *
	 * @param slotNumber
	 * @param itemStack
	 */
	public void setItemStack(int slotNumber, ItemStack itemStack)
	{
		MalisisSlot slot = getSlot(slotNumber);
		if (slot == null)
			return;

		if (itemStack != null)
		{
			int max = Math.min(slot.getSlotStackLimit(), itemStack.getMaxStackSize());
			itemStack.stackSize = Math.min(itemStack.stackSize, max);
		}

		if (ItemStack.areItemStacksEqual(itemStack, slot.getItemStack()))
			return;

		slot.setItemStack(itemStack);
		slot.onSlotChanged();
	}

	public List<ItemStack> getItemStackList()
	{
		ArrayList<ItemStack> list = new ArrayList<>();
		for (int i = 0; i < slots.length; i++)
			if (getItemStack(i) != null)
				list.add(getItemStack(i));

		return list;
	}

	/**
	 * Checks whether itemStack can be contained by slot.
	 *
	 * @param slot
	 * @param itemStack
	 * @return
	 */
	public boolean itemValidForSlot(MalisisSlot slot, ItemStack itemStack)
	{
		return true;
	}

	/**
	 * Checks whether itemStack can be contained by the slot at position slotNumber.
	 */
	@Override
	public boolean isItemValidForSlot(int slotNumber, ItemStack itemStack)
	{
		MalisisSlot slot = getSlot(slotNumber);
		if (slot == null)
			return false;
		return slot.isItemValid(itemStack);
	}

	/**
	 * @return size of this {@link MalisisInventory}.
	 */
	@Override
	public int getSizeInventory()
	{
		return this.size;
	}

	/**
	 * @return stack size limit for the slots
	 */
	@Override
	public int getInventoryStackLimit()
	{
		return slotMaxStackSize;
	}

	public void setInventoryStackLimit(int limit)
	{
		slotMaxStackSize = limit;
	}

	/**
	 * Set this {@link MalisisInventory} contents based on the itemStack NBT. <br />
	 * The inventoryProvider need to be an Item.
	 *
	 * @param itemStack
	 */
	public void setItemStackProvider(ItemStack itemStack)
	{
		if (!(inventoryProvider instanceof Item))
			throw new IllegalArgumentException("setItemStack not allowed with " + inventoryProvider.getClass().getSimpleName()
					+ " provider.");

		if (itemStack.getItem() != inventoryProvider)
		{
			MalisisCore.log.error("[MalisisInventory] Tried to set itemStack with an different item (" + itemStack.getItem()
					+ ") than the provider (" + inventoryProvider + ")");
			return;
		}

		this.itemStackProvider = itemStack;
		readFromNBT(itemStack.getTagCompound());
	}

	public void addOpenedContainer(MalisisInventoryContainer container)
	{
		containers.add(container);
	}

	public void removeOpenedContainer(MalisisInventoryContainer container)
	{
		containers.remove(container);
		if (containers.size() == 0 && itemStackProvider != null && itemStackProvider.stackTagCompound != null)
			itemStackProvider.stackTagCompound.removeTag("inventoryId");
	}

	public Set<MalisisInventoryContainer> getOpenedContainers()
	{
		return containers;
	}

	/**
	 * Checks if at least one itemStack is present in inventory.
	 *
	 * @return
	 */
	public boolean isEmpty()
	{
		return getItemStackList().size() == 0;
	}

	/**
	 * Checks if at least one slot is not full.
	 *
	 * @return
	 */
	public boolean isFull()
	{
		for (MalisisSlot slot : slots)
			if (!slot.isFull())
				return false;

		return true;
	}

	// #end getters/setters

	/**
	 * Called when itemStack change in slot
	 *
	 * @param malisisSlot
	 */
	public void onSlotChanged(MalisisSlot slot)
	{
		if (inventoryProvider instanceof Item && itemStackProvider != null)
			this.writeToNBT(itemStackProvider.getTagCompound());

		bus.post(new InventoryEvent.SlotChanged(this, slot));
	}

	/**
	 * Called when this {@link MalisisInventory} is opened.
	 */
	@Override
	public void openInventory()
	{}

	/**
	 * Transfer itemStack inside this {@link MalisisInventory}.
	 *
	 * @param itemStack that could not fit inside this {@link MalisisInventory}
	 * @return
	 */
	public ItemStack transferInto(ItemStack itemStack)
	{
		return transferInto(itemStack, false);
	}

	/**
	 * Gets the first slot containing an itemStack.
	 *
	 * @return
	 */
	public MalisisSlot getFirstOccupiedSlot()
	{
		for (MalisisSlot slot : slots)
			if (slot.getItemStack() != null)
				return slot;
		return null;
	}

	/**
	 * Removes the first itemStack in the inventory and returns it.
	 *
	 * @return
	 */
	public ItemStack pullItemStack()
	{
		MalisisSlot slot = getFirstOccupiedSlot();
		if (slot == null)
			return null;

		ItemStack itemStack = slot.getItemStack();
		slot.setItemStack(null);
		slot.onSlotChanged();
		return itemStack;
	}

	/**
	 * Transfer itemStack inside this {@link MalisisInventory}.
	 *
	 * @param itemStack that could not fit inside this {@link MalisisInventory}
	 * @param reversed start filling slots from the last slot
	 * @return
	 */
	public ItemStack transferInto(ItemStack itemStack, boolean reversed)
	{
		int start = reversed ? size - 1 : 0;
		int end = reversed ? 0 : size - 1;

		itemStack = transferInto(itemStack, false, start, end);
		if (itemStack != null)
			itemStack = transferInto(itemStack, true, start, end);

		return itemStack;
	}

	/**
	 * Transfer itemStack inside this {@link MalisisInventory} into slots at position from start to end. If start > end, the slots will be
	 * filled backwards.
	 *
	 * @param itemStack
	 * @param emptySlot
	 * @param start
	 * @param end
	 * @return
	 */
	protected ItemStack transferInto(ItemStack itemStack, boolean emptySlot, int start, int end)
	{
		MalisisSlot slot;
		int current = start, step = 1;
		if (start > end)
		{
			step = -1;
			start = end;
			end = current;
		}

		while (itemStack != null && current >= start && current <= end)
		{
			slot = getSlot(current);
			if (slot.isItemValid(itemStack) && (emptySlot || slot.getItemStack() != null))
			{
				ItemUtils.ItemStacksMerger ism = new ItemUtils.ItemStacksMerger(itemStack, slot.getItemStack());
				if (ism.merge(ItemUtils.FULL_STACK, slot.getSlotStackLimit()))
				{
					itemStack = ism.merge;
					slot.setItemStack(ism.into);
					slot.onSlotChanged();
				}
			}
			current += step;
		}

		return itemStack;
	}

	/**
	 * Spills out all the itemStack contained inside this <code>MalisisInvnetory</code>
	 */
	public void breakInventory(World world, int x, int y, int z)
	{
		for (ItemStack itemStack : getItemStackList())
			EntityUtils.spawnEjectedItem(world, x, y, z, itemStack);
	}

	/**
	 * Read this {@link MalisisInventory} data from tagCompound
	 *
	 * @param tagCompound
	 */
	public void readFromNBT(NBTTagCompound tagCompound)
	{
		if (tagCompound == null)
			return;

		NBTTagList nbttaglist = tagCompound.getTagList("Items", NBT.TAG_COMPOUND);
		for (int i = 0; i < nbttaglist.tagCount(); ++i)
		{
			NBTTagCompound stackTag = nbttaglist.getCompoundTagAt(i);
			int slotNumber = stackTag.getByte("Slot") & 255;
			MalisisSlot slot = getSlot(slotNumber);
			if (slot != null)
				slot.setItemStack(ItemStack.loadItemStackFromNBT(stackTag));
		}
	}

	/**
	 * Writes this {@link MalisisInventory} data inside tagCompound
	 *
	 * @param tagCompound
	 */
	public void writeToNBT(NBTTagCompound tagCompound)
	{
		if (tagCompound == null)
			return;

		NBTTagList itemList = new NBTTagList();
		for (int i = 0; i < slots.length; i++)
		{
			ItemStack stack = getItemStack(i);
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
	public String toString()
	{
		String provider = "Player";
		if (!(this instanceof PlayerInventory))
			provider = inventoryProvider != null ? inventoryProvider.getClass().getSimpleName() : "null	";

		return (name != null ? name : getClass().getSimpleName()) + " (" + inventoryId + ") from " + provider;
	}

	/**
	 * Open this {@link MalisisInventory}. Called server-side only
	 *
	 * @param player
	 * @return
	 *
	 */
	public static MalisisInventoryContainer open(EntityPlayerMP player, IInventoryProvider inventoryProvider, Object... data)
	{
		if (inventoryProvider == null)
			return null;

		MalisisInventoryContainer c = new MalisisInventoryContainer(player, 0);

		MalisisInventory[] inventories = inventoryProvider.getInventories(data);
		if (inventories != null)
			for (MalisisInventory inv : inventories)
			{
				c.addInventory(inv);
				inv.openInventory();
				bus.post(new InventoryEvent.Open(c, inv));
			}

		OpenInventoryMessage.send(inventoryProvider, player, c.windowId);
		c.sendInventoryContent();

		return c;
	}

	/**
	 * Open this {@link MalisisInventory}. Called client-side only.
	 *
	 * @param player
	 * @param windowId
	 * @return
	 */
	@SideOnly(Side.CLIENT)
	public static MalisisInventoryContainer open(EntityClientPlayerMP player, IInventoryProvider inventoryProvider, int windowId, Object... data)
	{
		if (inventoryProvider == null)
			return null;

		MalisisInventoryContainer c = new MalisisInventoryContainer(player, windowId);
		MalisisInventory[] inventories = inventoryProvider.getInventories(data);
		if (inventories != null)
			for (MalisisInventory inv : inventories)
			{
				c.addInventory(inv);
				inv.openInventory();
				bus.post(new InventoryEvent.Open(c, inv));
			}

		if (FMLCommonHandler.instance().getSide().isClient())
		{
			MalisisGui gui = inventoryProvider.getGui(c);
			if (gui != null)
				gui.display();
		}

		return c;
	}

	// #region Unused

	/**
	 * Unused
	 */
	@Override
	public boolean isUseableByPlayer(EntityPlayer player)
	{
		return true;
	}

	/**
	 * Unused
	 */
	@Override
	public void closeInventory()
	{}

	/**
	 * Unused
	 */
	@Override
	public void markDirty()
	{}

	/**
	 * Unused : always returns null
	 */
	@Override
	public ItemStack decrStackSize(int slot, int count)
	{
		return null;
	}

	/**
	 * Unused : always returns null
	 */
	@Override
	public ItemStack getStackInSlotOnClosing(int slot)
	{
		return null;
	}

	/**
	 * Use MalisisInventory.getItemStack(int slotNumber);
	 */
	@Override
	@Deprecated
	public ItemStack getStackInSlot(int slotNumber)
	{
		return getItemStack(slotNumber);
	}

	/**
	 * Use MalisisInventory.setItemStack(int slotNumber, ItemStack itemStack)
	 */
	@Override
	@Deprecated
	public void setInventorySlotContents(int slotNumber, ItemStack itemStack)
	{
		setItemStack(slotNumber, itemStack);
	}

	// #end Unused
}