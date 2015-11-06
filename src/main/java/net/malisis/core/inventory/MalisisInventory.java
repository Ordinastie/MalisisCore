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
import net.malisis.core.inventory.message.OpenInventoryMessage;
import net.malisis.core.inventory.player.PlayerInventory;
import net.malisis.core.util.EntityUtils;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.eventbus.EventBus;

/**
 *
 * @author Ordinastie
 *
 */
public class MalisisInventory
{
	/** List of {@link MalisisInventory} that is currently containing this {@link MalisisInventory}. */
	protected Set<MalisisInventoryContainer> containers = Collections.newSetFromMap(new WeakHashMap<MalisisInventoryContainer, Boolean>());
	/** The inventory id inside the container. */
	protected int inventoryId;
	/** Object containing this {@link MalisisInventory}. */
	protected IInventoryProvider inventoryProvider;
	/** ItemStack holding the inventory when inventoryProvider is an Item. */
	protected ItemStack itemStackProvider;
	/** Slots for this {@link MalisisInventory}. */
	protected MalisisSlot[] slots;
	/** Name of this {@link MalisisInventory}. */
	protected String name;
	/** Number of slots inside this {@link MalisisInventory}. */
	protected int size;
	/** Maximum stack size for the slots. */
	protected int slotMaxStackSize = 64;
	/** Event bus on which inventory events will be fired. */
	private EventBus bus = new EventBus();
	/** Current inventory state. */
	public InventoryState state = new InventoryState();

	/**
	 * Instantiates a new {@link MalisisInventory}.
	 *
	 * @param provider the provider
	 * @param size the size
	 */
	public MalisisInventory(IInventoryProvider provider, int size)
	{
		this.inventoryProvider = provider;
		MalisisSlot[] slots = new MalisisSlot[size];
		for (int i = 0; i < size; i++)
			slots[i] = new MalisisSlot(this, i);

		setSlots(slots);
	}

	/**
	 * Instantiates a new {@link MalisisInventory}.
	 *
	 * @param provider the provider
	 * @param slots the slots
	 */
	public MalisisInventory(IInventoryProvider provider, MalisisSlot[] slots)
	{
		this.inventoryProvider = provider;
		setSlots(slots);
	}

	/**
	 * Gets the {@link IInventoryProvider} of this {@link MalisisInventory}.
	 *
	 * @return the provider.
	 */
	public IInventoryProvider getProvider()
	{
		return inventoryProvider;
	}

	/**
	 * Sets the slots for this {@link MalisisInventory}.
	 *
	 * @param slots the new slots
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
	 * @param slot the slot
	 * @param slotNumber the slot number
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
	 * @param object the object
	 */
	public void register(Object object)
	{
		bus.register(object);
	}

	/**
	 * Sets the id of this {@link MalisisInventory} inside its container.
	 *
	 * @param id the new inventory id
	 */
	public void setInventoryId(int id)
	{
		inventoryId = id;
		if (itemStackProvider == null)
			return;

		NBTTagCompound tag = itemStackProvider.getTagCompound();
		if (tag == null)
		{
			tag = new NBTTagCompound();
			itemStackProvider.setTagCompound(tag);
		}
		itemStackProvider.getTagCompound().setInteger("inventoryId", id);
	}

	/**
	 * Gets the id of this {@link MalisisInventory} inside the {@link MalisisInventoryContainer}.
	 *
	 * @return the inventory id.
	 */
	public int getInventoryId()
	{
		return inventoryId;
	}

	// #region getters/setters
	/**
	 * Sets this {@link MalisisInventory} name.
	 *
	 * @param name the new name
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Gets this {@link MalisisInventory} name.
	 *
	 * @return the inventory name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Checks if this {@link MalisisInventory} has a name set.
	 *
	 * @return true, if successful
	 */
	public boolean hasCustomName()
	{
		return !StringUtils.isEmpty(name);
	}

	/**
	 * Gets the slot at position slotNumber.
	 *
	 * @param slotNumber the slot number
	 * @return the slot
	 */
	public MalisisSlot getSlot(int slotNumber)
	{
		if (slotNumber < 0 || slotNumber >= getSizeInventory())
			return null;

		return slots[slotNumber];
	}

	/**
	 * Gets the slots.
	 *
	 * @return all slots from this {@link MalisisInventory}.
	 */
	public MalisisSlot[] getSlots()
	{
		return slots;
	}

	/**
	 * Gets the itemStack from the slot at position slotNumber.
	 *
	 * @param slotNumber the slot number
	 * @return the item stack
	 */
	public ItemStack getItemStack(int slotNumber)
	{
		MalisisSlot slot = getSlot(slotNumber);
		return slot != null ? slot.getItemStack() : null;
	}

	/**
	 * Sets the itemStack for the slot at position slotNumber.
	 *
	 * @param slotNumber the slot number
	 * @param itemStack the item stack
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

	/**
	 * Gets the item stack list.
	 *
	 * @return the item stack list
	 */
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
	 * @param slot the slot
	 * @param itemStack the item stack
	 * @return true, if successful
	 */
	public boolean itemValidForSlot(MalisisSlot slot, ItemStack itemStack)
	{
		return true;
	}

	/**
	 * Checks whether itemStack can be contained by the slot at position slotNumber.
	 *
	 * @param slotNumber the slot number
	 * @param itemStack the item stack
	 * @return true, if is item valid for slot
	 */
	public boolean isItemValidForSlot(int slotNumber, ItemStack itemStack)
	{
		MalisisSlot slot = getSlot(slotNumber);
		if (slot == null)
			return false;
		return slot.isItemValid(itemStack);
	}

	/**
	 * Gets the size inventory.
	 *
	 * @return size of this {@link MalisisInventory}.
	 */
	public int getSizeInventory()
	{
		return this.size;
	}

	/**
	 * Gets the inventory stack limit.
	 *
	 * @return stack size limit for the slots
	 */
	public int getInventoryStackLimit()
	{
		return slotMaxStackSize;
	}

	/**
	 * Sets the inventory stack limit.
	 *
	 * @param limit the new inventory stack limit
	 */
	public void setInventoryStackLimit(int limit)
	{
		slotMaxStackSize = limit;
	}

	/**
	 * Set this {@link MalisisInventory} contents based on the itemStack NBT. <br>
	 * The inventoryProvider need to be an Item.
	 *
	 * @param itemStack the new item stack provider
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

	/**
	 * Adds the opened container.
	 *
	 * @param container the container
	 */
	public void addOpenedContainer(MalisisInventoryContainer container)
	{
		containers.add(container);
	}

	/**
	 * Removes the opened container.
	 *
	 * @param container the container
	 */
	public void removeOpenedContainer(MalisisInventoryContainer container)
	{
		containers.remove(container);
		if (containers.size() == 0 && itemStackProvider != null && itemStackProvider.getTagCompound() != null)
			itemStackProvider.getTagCompound().removeTag("inventoryId");
	}

	/**
	 * Gets the opened containers.
	 *
	 * @return the opened containers
	 */
	public Set<MalisisInventoryContainer> getOpenedContainers()
	{
		return containers;
	}

	/**
	 * Checks if at least one itemStack is present in inventory.
	 *
	 * @return true, if is empty
	 */
	public boolean isEmpty()
	{
		return getItemStackList().size() == 0;
	}

	/**
	 * Checks if at least one slot is not full.
	 *
	 * @return true, if is full
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
	 * Called when itemStack change in slot.
	 *
	 * @param slot the slot
	 */
	public void onSlotChanged(MalisisSlot slot)
	{
		if (inventoryProvider instanceof Item && itemStackProvider != null)
			this.writeToNBT(itemStackProvider.getTagCompound());

		bus.post(new InventoryEvent.SlotChanged(this, slot));
	}

	/**
	 * Gets the first {@link MalisisSlot} containing an {@link ItemStack}.
	 *
	 * @return the first occupied slot
	 */
	public MalisisSlot getFirstOccupiedSlot()
	{
		for (MalisisSlot slot : slots)
			if (slot.getItemStack() != null)
				return slot;
		return null;
	}

	/**
	 * Removes the first {@link ItemStack} in this {@link MalisisInventory} and returns it.
	 *
	 * @return the item stack
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
	 * Transfers an {@link ItemStack} inside this {@link MalisisInventory}.
	 *
	 * @param itemStack the item stack
	 * @return the itemStack that could not fit inside this inventory
	 */
	public ItemStack transferInto(ItemStack itemStack)
	{
		return transferInto(itemStack, false);
	}

	/**
	 * Transfers itemStack inside this {@link MalisisInventory}.
	 *
	 * @param itemStack the item stack
	 * @param reversed if true, start filling slots from the last slot
	 * @return the itemStack that could not fit inside this inventory
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
	 * Transfers itemStack inside this {@link MalisisInventory} into slots at position from start to end. If <b>start</b> &gt; <b>end</b>,
	 * the slots will be filled backwards.
	 *
	 * @param itemStack the item stack
	 * @param emptySlot whether to fill empty slots only
	 * @param start the start
	 * @param end the end
	 * @return the itemStack that could not fit inside this inventory
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
			if (slot.isItemValid(itemStack) && !slot.isOutputSlot() && (emptySlot || slot.getItemStack() != null))
			{
				itemStack = slot.insert(itemStack);
				//				ItemUtils.ItemStacksMerger ism = new ItemUtils.ItemStacksMerger(itemStack, slot.getItemStack());
				//				if (ism.merge(ItemUtils.FULL_STACK, slot.getSlotStackLimit()))
				//				{
				//					itemStack = ism.merge;
				//					slot.setItemStack(ism.into);
				//					slot.onSlotChanged();
				//				}
			}
			current += step;
		}

		return itemStack;
	}

	/**
	 * Spills out all the itemStack contained inside this {@link MalisisInventory}.
	 *
	 * @param world the world
	 * @param pos the pos
	 */
	public void breakInventory(World world, BlockPos pos)
	{
		for (ItemStack itemStack : getItemStackList())
			EntityUtils.spawnEjectedItem(world, pos, itemStack);
		closeContainers();
		emptyInventory();
	}

	/**
	 * Empties this {@link MalisisInventory} and sets all slots contents to null.
	 */
	public void emptyInventory()
	{
		for (MalisisSlot slot : slots)
			slot.setItemStack(null);
	}

	/**
	 * Closes all currently opened containers
	 */
	public void closeContainers()
	{
		for (MalisisInventoryContainer container : containers)
			container.close();
	}

	/**
	 * Reads this {@link MalisisInventory} data from tagCompound.
	 *
	 * @param tagCompound the tag compound
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
	 * Writes this {@link MalisisInventory} data inside tagCompound.
	 *
	 * @param tagCompound the tag compound
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
	 * Opens this {@link MalisisInventory}. Called server-side only.
	 *
	 * @param player the player
	 * @param inventoryProvider the inventory provider
	 * @param data the data
	 * @return the {@link MalisisInventoryContainer}
	 */
	public static MalisisInventoryContainer open(EntityPlayerMP player, IInventoryProvider inventoryProvider, Object... data)
	{
		if (inventoryProvider == null)
			return null;

		MalisisInventoryContainer c = new MalisisInventoryContainer(player, 0);

		MalisisInventory[] inventories = inventoryProvider.getInventories(data);
		if (!ArrayUtils.isEmpty(inventories))
		{
			for (MalisisInventory inv : inventories)
			{
				if (inv != null)
				{
					c.addInventory(inv);
					inv.bus.post(new InventoryEvent.Open(c, inv));
				}
			}
		}
		OpenInventoryMessage.send(inventoryProvider, player, c.windowId);
		c.sendInventoryContent();

		return c;
	}

	/**
	 * Opens this {@link MalisisInventory}. Called client-side only.
	 *
	 * @param player the player
	 * @param inventoryProvider the inventory provider
	 * @param windowId the window id
	 * @param data the data
	 * @return the {@link MalisisInventoryContainer}
	 */
	@SideOnly(Side.CLIENT)
	public static MalisisInventoryContainer open(EntityPlayerSP player, IInventoryProvider inventoryProvider, int windowId, Object... data)
	{
		if (inventoryProvider == null)
			return null;

		MalisisInventoryContainer c = new MalisisInventoryContainer(player, windowId);
		MalisisInventory[] inventories = inventoryProvider.getInventories(data);
		if (!ArrayUtils.isEmpty(inventories))
		{
			for (MalisisInventory inv : inventories)
			{
				if (inv != null)
				{
					c.addInventory(inv);
					inv.bus.post(new InventoryEvent.Open(c, inv));
				}
			}
		}

		if (FMLCommonHandler.instance().getSide().isClient())
		{
			MalisisGui gui = inventoryProvider.getGui(c);
			if (gui != null)
				gui.display();
		}

		return c;
	}
}