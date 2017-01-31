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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.eventbus.EventBus;

import net.malisis.core.ExceptionHandler;
import net.malisis.core.MalisisCore;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.inventory.IInventoryProvider.IDeferredInventoryProvider;
import net.malisis.core.inventory.IInventoryProvider.IDirectInventoryProvider;
import net.malisis.core.inventory.message.OpenInventoryMessage;
import net.malisis.core.inventory.player.PlayerInventory;
import net.malisis.core.util.EntityUtils;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
	protected NonNullList<MalisisSlot> slots = NonNullList.create();
	/** Name of this {@link MalisisInventory}. */
	protected String name;
	/** Maximum stack size for the slots. */
	protected int slotMaxStackSize = 64;
	/** Event bus on which inventory events will be fired. */
	private EventBus bus = new EventBus(ExceptionHandler.instance);
	/** Current inventory state. */
	public InventoryState state = new InventoryState();

	/**
	 * Instantiates a new {@link MalisisInventory} with <code>size</size> amount of slots from supplied by the <code>supplier</code>.
	 *
	 * @param provider the provider
	 * @param supplier the supplier
	 * @param size the size
	 */
	public MalisisInventory(IInventoryProvider provider, Supplier<? extends MalisisSlot> supplier, int size)
	{
		this.inventoryProvider = provider;
		for (int index = 0; index < size; index++)
			slots.add(supplier.get());
		setupSlots();
	}

	/**
	 * Instantiates a new {@link MalisisInventory}.
	 *
	 * @param provider the provider
	 * @param size the size
	 */
	public MalisisInventory(IInventoryProvider provider, int size)
	{
		this(provider, MalisisSlot::new, size);
	}

	/**
	 * Instantiates a new {@link MalisisInventory} with the specified {@link MalisisSlot}.
	 *
	 * @param provider the provider
	 * @param slots the slots
	 */
	public MalisisInventory(IInventoryProvider provider, MalisisSlot... slots)
	{
		this.inventoryProvider = provider;
		Arrays.stream(slots).filter(Objects::nonNull).forEach(this.slots::add);
		setupSlots();
	}

	/**
	 * Setup the inventory and slot index for all the {@link MalisisSlot} added to this {@link MalisisInventory}.
	 */
	private void setupSlots()
	{
		for (int i = 0; i < slots.size(); i++)
			slots.get(i).setup(this, i);
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
	 * Overrides a specific slot with a new one.
	 *
	 * @param slot the slot
	 * @param slotIndex the slot index
	 */
	public void overrideSlot(MalisisSlot slot, int slotIndex)
	{
		if (slotIndex < 0 || slotIndex >= getSize())
			return;
		slots.get(slotIndex).setup(null, -1);
		slots.add(slotIndex, slot);
		slot.setup(this, slotIndex);
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
	 * @param slotIndex the slot index
	 * @return the slot
	 */
	public MalisisSlot getSlot(int slotIndex)
	{
		if (slotIndex < 0 || slotIndex >= getSize())
			return null;

		return slots.get(slotIndex);
	}

	/**
	 * Gets the slots.
	 *
	 * @return all slots from this {@link MalisisInventory}.
	 */
	public List<MalisisSlot> getSlots()
	{
		return slots;
	}

	/**
	 * Gets the non empty slots.
	 *
	 * @return the non empty slots
	 */
	public List<MalisisSlot> getNonEmptySlots()
	{
		return getSlots().stream().filter(MalisisSlot::isNotEmpty).collect(Collectors.toList());
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
		return slot != null ? slot.getItemStack() : ItemStack.EMPTY;
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

		if (itemStack.isEmpty())
			itemStack = ItemStack.EMPTY;
		else
		{
			int max = Math.min(slot.getSlotStackLimit(), itemStack.getMaxStackSize());
			itemStack.setCount(Math.min(itemStack.getCount(), max));
		}

		if (ItemStack.areItemStacksEqual(itemStack, slot.getItemStack()))
			return;

		slot.setItemStack(itemStack);
		slot.onSlotChanged();
	}

	/**
	 * Gets all the non empty {@link ItemStack} stored in this {@link MalisisInventory}.
	 *
	 * @return the item stack list
	 */
	public NonNullList<ItemStack> getItemStackList()
	{
		return getSlots()	.stream()
							.filter(MalisisSlot::isNotEmpty)
							.map(MalisisSlot::getItemStack)
							.collect(Collectors.toCollection(NonNullList::create));
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
	public int getSize()
	{
		return this.getSlots().size();
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
	 * Checks if at least one non-empty {@link ItemStack} is present in this {@link MalisisInventory}.
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
		return !getSlots().stream().anyMatch(MalisisSlot::isNotFull);
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
	public Optional<MalisisSlot> getFirstOccupiedSlot()
	{
		return getSlots().stream().filter(MalisisSlot::isNotEmpty).findFirst();
	}

	/**
	 * Removes the first non-empty {@link ItemStack} in this {@link MalisisInventory} and returns it.
	 *
	 * @return the item stack
	 */
	public ItemStack pullItemStack()
	{
		return getFirstOccupiedSlot().map(MalisisSlot::extract).get();
	}

	/**
	 * Pulls all the possible non-full {@link ItemStack} into <code>itemStack</code>.
	 *
	 * @param itemStack the item stack
	 * @return true, if the destination itemStack is now full
	 */
	public boolean pullItemStacks(ItemStack itemStack, boolean ignoreFullStacks)
	{
		for (MalisisSlot s : getNonEmptySlots())
		{
			ItemStack is = s.getItemStack();
			if ((!ignoreFullStacks || is.getCount() < is.getMaxStackSize()) && s.extractInto(itemStack))
				return true;
		}
		return false;
	}

	/**
	 * Transfers into this {@link MalisisInventory} the contents of <code>inventory</code>.
	 *
	 * @param inventory the inventory
	 */
	public void transfer(MalisisInventory inventory)
	{
		for (MalisisSlot s : inventory.getNonEmptySlots())
		{
			ItemStack itemStack = transferInto(s.getItemStack());
			s.setItemStack(itemStack);
			s.onSlotChanged();
			if (!itemStack.isEmpty())
				return;
		}
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
		int start = reversed ? getSlots().size() - 1 : 0;
		int end = reversed ? 0 : getSlots().size() - 1;

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

		while (!itemStack.isEmpty() && current >= start && current <= end)
		{
			slot = getSlot(current);
			if (slot.isItemValid(itemStack) && !slot.isOutputSlot() && (emptySlot || !slot.getItemStack().isEmpty()))
				itemStack = slot.insert(itemStack);
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
			slot.setItemStack(ItemStack.EMPTY);
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
				slot.setItemStack(new ItemStack(stackTag));
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
		getNonEmptySlots().forEach(slot -> {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setByte("Slot", (byte) slot.getSlotIndex());
			slot.getItemStack().writeToNBT(tag);
			itemList.appendTag(tag);

		});
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
	 * @return the {@link MalisisInventoryContainer}
	 */
	public static MalisisInventoryContainer open(EntityPlayerMP player, IDirectInventoryProvider inventoryProvider)
	{
		if (inventoryProvider == null)
			return null;

		return openInventories(player, inventoryProvider, inventoryProvider.getInventories(), 0);
	}

	/**
	 * Opens this {@link MalisisInventory}. Called server-side only.
	 *
	 * @param <T> the generic type
	 * @param player the player
	 * @param inventoryProvider the inventory provider
	 * @param data the data
	 * @return the {@link MalisisInventoryContainer}
	 */
	public static <T> MalisisInventoryContainer open(EntityPlayerMP player, IDeferredInventoryProvider<T> inventoryProvider, T data)
	{
		if (inventoryProvider == null)
			return null;

		return openInventories(player, inventoryProvider, inventoryProvider.getInventories(data), 0);
	}

	/**
	 * Opens this {@link MalisisInventory}. Called client-side only.
	 *
	 * @param player the player
	 * @param inventoryProvider the inventory provider
	 * @param windowId the window id
	 * @return the {@link MalisisInventoryContainer}
	 */
	@SideOnly(Side.CLIENT)
	public static MalisisInventoryContainer open(EntityPlayerSP player, IDirectInventoryProvider inventoryProvider, int windowId)
	{
		if (inventoryProvider == null)
			return null;

		MalisisInventoryContainer c = openInventories(player, inventoryProvider, inventoryProvider.getInventories(), windowId);
		MalisisGui gui = inventoryProvider.getGui(c);
		if (gui != null)
			gui.display();

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
	public static <T> MalisisInventoryContainer open(EntityPlayerSP player, IDeferredInventoryProvider<T> inventoryProvider, T data, int windowId)
	{
		if (inventoryProvider == null)
			return null;

		MalisisInventoryContainer c = openInventories(player, inventoryProvider, inventoryProvider.getInventories(data), windowId);
		MalisisGui gui = inventoryProvider.getGui(data, c);
		if (gui != null)
			gui.display();

		return c;
	}

	private static MalisisInventoryContainer openInventories(EntityPlayer player, IInventoryProvider inventoryProvider, MalisisInventory[] inventories, int windowId)
	{
		MalisisInventoryContainer c = new MalisisInventoryContainer(player, windowId);
		if (!ArrayUtils.isEmpty(inventories))
		{
			Arrays.stream(inventories).filter(Objects::nonNull).forEach(inv -> {
				c.addInventory(inv);
				inv.bus.post(new InventoryEvent.Open(c, inv));
			});
		}

		//called server-side, send windowId and inventory contents
		if (player instanceof EntityPlayerMP)
		{
			OpenInventoryMessage.send(inventoryProvider, (EntityPlayerMP) player, c.windowId);
			c.sendInventoryContent();
		}

		return c;
	}
}