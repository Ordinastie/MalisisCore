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

import static net.malisis.core.inventory.MalisisInventoryContainer.ActionType.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

import net.malisis.core.MalisisCore;
import net.malisis.core.inventory.player.PlayerInventory;
import net.malisis.core.inventory.player.PlayerInventorySlot;
import net.malisis.core.packet.UpdateInventorySlotsMessage;
import net.malisis.core.packet.UpdateInventorySlotsMessage.SlotType;
import net.malisis.core.util.ItemUtils;
import net.malisis.core.util.ItemUtils.ItemStacksMerger;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

public class MalisisInventoryContainer extends Container
{
	//@formatter:off
	public enum ActionType
	{
		LEFT_CLICK,
		RIGHT_CLICK,
		PICKBLOCK,
		SHIFT_LEFT_CLICK,
		SHIFT_RIGHT_CLICK,
		DOUBLE_LEFT_CLICK,
		DOUBLE_SHIFT_LEFT_CLICK,
		DRAG_START_LEFT_CLICK,
		DRAG_START_RIGHT_CLICK,
		DRAG_START_PICKUP,
		DRAG_ADD_SLOT,
		DRAG_END,
		DRAG_RESET,
		HOTBAR,
		DROP_ONE,
		DROP_STACK,
		DROP_SLOT_ONE,
		DROP_SLOT_STACK;

		public boolean isDragAction()
		{
			return this == DRAG_START_LEFT_CLICK || this == DRAG_START_RIGHT_CLICK || this == DRAG_START_PICKUP ||
					this == DRAG_ADD_SLOT || this == DRAG_END || this == DRAG_RESET;
		}
	}
	//@formatter:on

	/**
	 * Dragging the itemStack to be spread evenly among all crossed slots
	 */
	public static final int DRAG_TYPE_SPREAD = 0;
	/**
	 * Dragging the itemStack to leave only one item per crossed slot
	 */
	public static final int DRAG_TYPE_ONE = 1;
	/**
	 * Dragging the itemStack to pick up itemStacks held by crossed slots
	 */
	public static final int DRAG_TYPE_PICKUP = 2;

	/**
	 * Player that opened this <code>MalisisInventoryContainer</code>
	 */
	protected EntityPlayer owner;
	/**
	 * Base inventory for this <code>MalisisInventoryContainer</code>
	 */
	protected MalisisInventory inventory;
	/**
	 * Inventory of the player that opened this <code>MalisisInventoryContainer</code>
	 */
	protected MalisisInventory playerInventory;
	/**
	 * ItemStack cache for base inventory. Used to know what itemStacks need to be sent to client
	 */
	protected HashMap<Integer, ItemStack> inventoryCache = new HashMap<>();
	/**
	 * ItemStack cache for player's inventory. Used to know what itemStacks need to be sent to client.
	 */
	protected HashMap<Integer, ItemStack> playerInventoryCache = new HashMap<>();
	/**
	 * ItemStack currently picked and following the cursor
	 */
	protected ItemStack pickedItemStack;
	/**
	 * Cache for the itemStack currently picked
	 */
	protected ItemStack pickedItemStackCache;
	/**
	 * Stack size when dragging started
	 */
	protected int draggedAmount = 0;
	/**
	 * List of itemStacks that will be transfered in each slot at the end of Dragging. Integer key is offset by inventory size for player
	 * inventory slots.
	 */
	protected HashMap<Integer, ItemStack> draggedItemStacks = new HashMap<>();
	/**
	 * Determines whether the dragged itemStacks need to be sent to the client.
	 */
	protected boolean draggedItemStackChanged = false;
	/**
	 * Type drag action. Can be DRAG_TYPE_SPREAD, DRAG_TYPE_ONE or DRAG_TYPE_PICKUP. Set to -1 if not currently dragging.
	 */
	protected int dragType = -1;
	/**
	 * Stores the last itemStack that was shift clicked. Used for shift double click.
	 */
	protected ItemStack lastShiftClicked;

	public MalisisInventoryContainer(MalisisInventory inventory, EntityPlayer player, int windowId)
	{
		// if server
		if (player instanceof EntityPlayerMP)
		{
			EntityPlayerMP p = (EntityPlayerMP) player;
			p.closeContainer();
			p.getNextWindowId();
			windowId = p.currentWindowId;
		}

		this.owner = player;
		this.windowId = windowId;
		this.inventory = inventory;
		this.playerInventory = new PlayerInventory(player);
		this.owner.openContainer = this;
	}

	// #region getters/setters
	/**
	 * @return base inventory of this <code>MalisisInventoryContainer</code>.
	 */
	public MalisisInventory getContainerInventory()
	{
		return inventory;
	}

	/**
	 * @return player's inventory of this <code>MalisisInventoryContainer</code>.
	 */
	public MalisisInventory getPlayerInventory()
	{
		return playerInventory;
	}

	/**
	 * Sets the currently picked itemStack. Update player inventory.
	 * 
	 * @param itemStack
	 * @param player
	 */
	public void setPickedItemStack(ItemStack itemStack)
	{
		pickedItemStack = itemStack;
		owner.inventory.setItemStack(itemStack);
	}

	/**
	 * @return currently picked itemStack
	 */
	public ItemStack getPickedItemStack()
	{
		return pickedItemStack;
	}

	/**
	 * Sets the list of itemStacks that will be transfered in each slot. Integer key is offset by inventory size for player inventory slots.
	 * 
	 * @param items
	 */
	public void setDraggedItems(HashMap<Integer, ItemStack> items)
	{
		draggedItemStacks = items;
	}

	/**
	 * Return the itemStack that will be transfered in <code>slot</code> at the end of the dragging.
	 * 
	 * @param slot
	 * @return
	 */
	public ItemStack getDraggedItemstack(MalisisSlot slot)
	{
		int slotNumber = slot instanceof PlayerInventorySlot ? slot.slotNumber + inventory.size : slot.slotNumber;
		return draggedItemStacks.get(slotNumber);
	}

	/**
	 * @return true if currently dragging an itemStack.
	 */
	public boolean isDraggingItemStack()
	{
		return dragType != -1;
	}

	/**
	 * Check if the dragging should end based on the mouse button clicked.
	 * 
	 * @param button
	 * @return
	 */
	public boolean shouldEndDrag(int button)
	{
		if (!isDraggingItemStack())
			return false;

		if (dragType == DRAG_TYPE_ONE || dragType == DRAG_TYPE_SPREAD)
			return dragType == button && draggedItemStacks.size() > 1;

		return dragType == DRAG_TYPE_PICKUP;
	}

	/**
	 * Check if the dragging should be reset based on the mouse button clicked.
	 * 
	 * @param button
	 * @return
	 */
	public boolean shouldResetDrag(int button)
	{
		if (!isDraggingItemStack())
			return false;

		if (dragType == DRAG_TYPE_SPREAD)
			return button == 1 && draggedItemStacks.size() > 1;
		if (dragType == DRAG_TYPE_ONE)
			return button == 0 && draggedItemStacks.size() > 1;

		return dragType != DRAG_TYPE_PICKUP;
	}

	/**
	 * @return the current dragging type.
	 */
	public int getDragType()
	{
		return dragType;
	}

	// #end getters/setters

	// #region network
	/**
	 * Sends the all the inventory slots to the client.
	 */
	public void sendInventoryContent()
	{
		if (!(owner instanceof EntityPlayerMP))
		{
			MalisisCore.log.error("MalisisInventoryContainer tried to send inventory contents CLIENT side!.");
			return;
		}
		ArrayList<MalisisSlot> slots = new ArrayList<>(Arrays.asList(inventory.getSlots()));
		UpdateInventorySlotsMessage.updateSlots(SlotType.TYPE_INVENTORY, slots, (EntityPlayerMP) owner, windowId);
	}

	/**
	 * Sends all changes for base inventory, player's inventory, picked up itemStack and dragged itemStacks.
	 */
	@Override
	public void detectAndSendChanges()
	{
		detectAndSendChanges(SlotType.TYPE_INVENTORY);
		detectAndSendChanges(SlotType.TYPE_PLAYERINVENTORY);
		detectAndSendPickedItemStack();
		detectAndSendDraggedItems();
	}

	/**
	 * Sends all changes for the inventory determined by {@link SlotType type}.
	 * 
	 * @param type
	 */
	public void detectAndSendChanges(SlotType type)
	{
		if (!(owner instanceof EntityPlayerMP))
		{
			MalisisCore.log.error("MalisisInventoryContainer tried to send " + type + " slots CLIENT side !.");
			return;
		}

		MalisisInventory inventory = type == SlotType.TYPE_PLAYERINVENTORY ? playerInventory : this.inventory;
		HashMap<Integer, ItemStack> cache = type == SlotType.TYPE_PLAYERINVENTORY ? playerInventoryCache : inventoryCache;
		ArrayList<MalisisSlot> changedSlots = new ArrayList<>();

		for (MalisisSlot slot : inventory.getSlots())
		{
			if (!ItemStack.areItemStacksEqual(slot.getItemStack(), cache.get(slot.slotNumber)))
			{
				changedSlots.add(slot);
				cache.put(slot.slotNumber, slot.getItemStack() != null ? slot.getItemStack().copy() : null);
			}
		}

		if (changedSlots.size() > 0)
			UpdateInventorySlotsMessage.updateSlots(type, changedSlots, (EntityPlayerMP) owner, windowId);
	}

	/**
	 * Sends the currently picked itemStack if changed.
	 */
	public void detectAndSendPickedItemStack()
	{
		if (!(owner instanceof EntityPlayerMP))
		{
			MalisisCore.log.error(" MalisisInventoryContainer tried to send picked itemStack CLIENT side !.");
			return;
		}

		if (ItemStack.areItemStacksEqual(pickedItemStack, pickedItemStackCache))
			return;

		UpdateInventorySlotsMessage.updatePickedItemStack(pickedItemStack, (EntityPlayerMP) owner, windowId);
		pickedItemStackCache = pickedItemStack != null ? pickedItemStack.copy() : null;
	}

	/**
	 * Sends all the dragged itemStacks if any has changed.
	 */
	public void detectAndSendDraggedItems()
	{
		if (!(owner instanceof EntityPlayerMP))
		{
			MalisisCore.log.error("MalisisInventoryContainer tried to send dragged itemStack CLIENT side !");
			return;
		}

		if (!draggedItemStackChanged)
			return;

		UpdateInventorySlotsMessage.updateDraggedItemStacks(draggedItemStacks, (EntityPlayerMP) owner, windowId);
		draggedItemStackChanged = false;
	}

	// #end network

	/**
	 * Handle the action for this <code>MalisisInventoryContainer</code>. See {@link ActionType} for possible actions.
	 * 
	 * @param action
	 * @param slotNumber
	 * @param code
	 * @param isPlayerInv
	 * @return itemStack resulting of the actions. Should be used the check client/server coherence.
	 */
	public ItemStack handleAction(ActionType action, int slotNumber, int code, boolean isPlayerInv)
	{
		MalisisSlot slot = isPlayerInv ? playerInventory.getSlot(slotNumber) : inventory.getSlot(slotNumber);
		if (slot == null)
		{
			MalisisCore.log.error("MalisisInventoryContainer try to handle an action for an wrong slotNumber.");
			return null;
		}

		// player pressed 1-9 key while hovering a slot
		if (action == HOTBAR && code >= 0 && code < 9)
			return handleHotbar(slot, code);

		// player pressed keyBindDrop key while hovering a slot
		if (action == DROP_SLOT_STACK || action == DROP_SLOT_ONE)
			return handleDropSlot(slot, action == DROP_SLOT_STACK);

		// player started/ended/reset/is currently dragging
		if (action.isDragAction())
			return handleDrag(action, slot);

		// from this point, any action should stop the dragging
		resetDrag();

		// player clicked outside the GUI with an item picked up
		if (action == DROP_ONE || action == DROP_STACK)
			return handleDropPickedStack(action == DROP_STACK);

		//
		if (action == LEFT_CLICK || action == RIGHT_CLICK)
			return handleNormalClick(slot, action == LEFT_CLICK);

		if (action == PICKBLOCK && owner.capabilities.isCreativeMode)
			return handlePickBlock(slot);

		if (action == SHIFT_LEFT_CLICK)
		{
			// we need to store last shift clicked item for double click because at the time of the second click, slot may be empty and we
			// wouldn't know what itemStack to move
			lastShiftClicked = slot.getItemStack();
			ItemStack itemStack = handleShiftClick(slot);
			if (itemStack != null)
				lastShiftClicked = null;
			return itemStack;
		}

		if (action == DOUBLE_LEFT_CLICK || action == DOUBLE_SHIFT_LEFT_CLICK)
			return handleDoubleClick(slot, action == DOUBLE_SHIFT_LEFT_CLICK);

		return null;
	}

	/**
	 * Drops one or the full itemStack currently picked up
	 * 
	 * @param fullStack
	 * @return
	 */
	private ItemStack handleDropPickedStack(boolean fullStack)
	{
		ItemUtils.ItemStackSplitter iss = new ItemUtils.ItemStackSplitter(pickedItemStack);
		iss.split(fullStack ? ItemUtils.FULL_STACK : 1);

		owner.dropPlayerItemWithRandomChoice(iss.split, true);
		setPickedItemStack(iss.source);

		return iss.source;
	}

	/**
	 * Handles the normal left or right click.
	 * 
	 * @param slot
	 * @param fullStack
	 * @return
	 */
	private ItemStack handleNormalClick(MalisisSlot slot, boolean fullStack)
	{
		ItemStack slotStack = slot.getItemStack();

		if (pickedItemStack != null && !slot.isItemValid(pickedItemStack))
			return null;

		// already picked up an itemStack
		if (pickedItemStack != null)
		{
			int amount = fullStack ? ItemUtils.FULL_STACK : 1;
			ItemUtils.ItemStacksMerger ism = new ItemUtils.ItemStacksMerger(pickedItemStack, slotStack);
			if (ism.merge(amount, slot.getSlotStackLimit()))
			{
				slot.setItemStack(ism.into);
				setPickedItemStack(ism.merge);
			}
			else if (pickedItemStack.stackSize <= slot.getSlotStackLimit())
			// couldn't merge, swap the itemStacks
			{
				slot.setItemStack(ism.merge);
				setPickedItemStack(ism.into);
			}

			if (ism.nbMerged != 0)
				slot.onSlotChanged();

			return ism.merge;
		}
		else
		// pick itemStack in slot
		{
			int amount = fullStack ? ItemUtils.FULL_STACK : ItemUtils.HALF_STACK;
			ItemUtils.ItemStackSplitter iss = new ItemUtils.ItemStackSplitter(slotStack);
			iss.split(amount);
			slot.setItemStack(iss.source);
			setPickedItemStack(iss.split);

			if (iss.amount != 0)
				slot.onPickupFromSlot(owner, iss.split);

			return iss.split;
		}
	}

	/**
	 * Handles shift clicking a slot
	 * 
	 * @param slot
	 * @return
	 */
	private ItemStack handleShiftClick(MalisisSlot slot)
	{
		if (slot.getItemStack() == null)
			return null;

		MalisisInventory targetInventory = slot instanceof PlayerInventorySlot ? inventory : playerInventory;

		ItemStack stackMoved = slot.getItemStack().copy();
		ItemStack itemStack = targetInventory.transferInto(slot.getItemStack());

		slot.setItemStack(itemStack);
		slot.onSlotChanged();

		stackMoved.stackSize = stackMoved.stackSize - (itemStack == null ? 0 : itemStack.stackSize);
		return itemStack;

	}

	/**
	 * Handles player pressing 1-9 key while hovering a slot
	 * 
	 * @param slot
	 * @param num
	 * @return
	 */
	private ItemStack handleHotbar(MalisisSlot slot, int num)
	{
		boolean fromPlayerInv = slot instanceof PlayerInventorySlot;
		MalisisSlot destSlot = playerInventory.getSlot(num);

		if (fromPlayerInv || slot.getItemStack() == null) // slot from player's inventory, swap itemStacks
		{
			ItemUtils.ItemStacksMerger ism = new ItemUtils.ItemStacksMerger(destSlot.getItemStack(), slot.getItemStack());
			ism.merge(ItemUtils.FULL_STACK, slot.getSlotStackLimit());

			destSlot.setItemStack(ism.merge);
			destSlot.onSlotChanged();
			slot.setItemStack(ism.into);
			slot.onSlotChanged();
			return null;
		}
		else
		{
			// merge itemStack in slot into slot in hotbar. If already holding an itemStack, move elsewhere inside player inventory
			ItemUtils.ItemStacksMerger ism = new ItemUtils.ItemStacksMerger(slot.getItemStack(), destSlot.getItemStack());
			ism.merge();

			destSlot.setItemStack(ism.into);
			destSlot.onSlotChanged();

			ItemStack itemStack = playerInventory.transferInto(ism.merge, false);
			slot.setItemStack(itemStack);
			slot.onSlotChanged();

		}

		return destSlot.getItemStack();
	}

	/**
	 * Drops itemStack from hovering slot
	 * 
	 * @param slot
	 * @param player
	 * @return
	 */
	private ItemStack handleDropSlot(MalisisSlot slot, boolean fullStack)
	{
		if (slot.getItemStack() == null)
			return null;

		ItemUtils.ItemStackSplitter iss = new ItemUtils.ItemStackSplitter(slot.getItemStack());
		iss.split(fullStack ? ItemUtils.FULL_STACK : 1);

		slot.setItemStack(iss.source);
		owner.dropPlayerItemWithRandomChoice(iss.split, true);

		if (iss.amount != 0)
			slot.onPickupFromSlot(owner, iss.split);

		return iss.split;
	}

	/**
	 * Handle double clicking on a slot.
	 * 
	 * @param slot
	 * @param shiftClick
	 * @return
	 */
	private ItemStack handleDoubleClick(MalisisSlot slot, boolean shiftClick)
	{
		MalisisInventory inventory = slot instanceof PlayerInventorySlot ? playerInventory : this.inventory;
		if (!shiftClick && pickedItemStack != null)
		{
			// normal double click, go through all hovered slot inventory to merge the slots with the currently picked one
			int i = 0;
			while (pickedItemStack.stackSize < pickedItemStack.getMaxStackSize() && i < inventory.size)
			{
				MalisisSlot s = inventory.getSlot(i);
				ItemUtils.ItemStacksMerger ism = new ItemStacksMerger(s.getItemStack(), pickedItemStack);
				ism.merge();
				s.setItemStack(ism.merge);
				s.onSlotChanged();
				pickedItemStack = ism.into;
				i++;
			}
			setPickedItemStack(pickedItemStack);
		}
		else if (lastShiftClicked != null)
		{
			// shift double click, go through all hovered slot inventory to transfer matching itemStack to the other inventory
			MalisisInventory targetInventory = slot instanceof PlayerInventorySlot ? this.inventory : playerInventory;
			for (MalisisSlot s : inventory.getSlots())
			{
				if (s.getItemStack() != null && ItemUtils.areItemStacksStackable(s.getItemStack(), lastShiftClicked))
				{
					ItemStack itemStack = targetInventory.transferInto(s.getItemStack());
					s.setItemStack(itemStack);
					s.onSlotChanged();
					if (itemStack != null)
						return pickedItemStack;
				}
			}
		}
		lastShiftClicked = null;
		return pickedItemStack;
	}

	/**
	 * Picks up the itemStack in the slot.
	 * 
	 * @param slot
	 * @return
	 */
	private ItemStack handlePickBlock(MalisisSlot slot)
	{
		if (slot.getItemStack() == null || pickedItemStack != null)
			return null;

		ItemStack itemStack = slot.getItemStack().copy();
		itemStack.stackSize = itemStack.getMaxStackSize();
		setPickedItemStack(itemStack);

		return itemStack;
	}

	/**
	 * Handles all drag actions.
	 * 
	 * @param action
	 * @param slot
	 * @return
	 */
	private ItemStack handleDrag(ActionType action, MalisisSlot slot)
	{
		if (pickedItemStack == null)
			return null;

		if ((action == DRAG_START_LEFT_CLICK || action == DRAG_START_RIGHT_CLICK) && isDraggingItemStack())
			return null;

		// was currently dragging an itemStack but pressed the other mouse button
		if (action == DRAG_RESET)
		{
			resetDrag();
			return pickedItemStack;
		}

		// released the button while dragging with control key down
		if (action == DRAG_END && dragType == DRAG_TYPE_PICKUP)
		{
			int size = pickedItemStack.stackSize;
			resetDrag();
			pickedItemStack.stackSize = size;
			return pickedItemStack;
		}

		// released the mouse button used to start dragging an itemStack
		if (action == DRAG_END)
		{
			if (pickedItemStack.stackSize == 0)
				setPickedItemStack(null);

			int amountMerged = 0;
			HashMap<Integer, MalisisSlot> draggedSlots = getDraggedSlots();
			for (Entry<Integer, ItemStack> entry : draggedItemStacks.entrySet())
			{
				MalisisSlot s = draggedSlots.get(entry.getKey());

				ItemUtils.ItemStacksMerger ism = new ItemStacksMerger(entry.getValue(), s.getItemStack());
				ism.merge();
				amountMerged += ism.nbMerged;
				s.setItemStack(ism.into);
				s.onSlotChanged();
			}

			resetDrag();

			if (pickedItemStack != null)
				pickedItemStack.stackSize -= amountMerged;

			return pickedItemStack;
		}

		// start dragging an itemStack with left mouse button while pressing control key
		if (action == DRAG_START_PICKUP)
		{
			dragType = DRAG_TYPE_PICKUP;
			return pickedItemStack;
		}

		// passing over a new slot while picking up stacks
		if (action == DRAG_ADD_SLOT && dragType == DRAG_TYPE_PICKUP)
		{
			if (pickedItemStack.stackSize >= pickedItemStack.getMaxStackSize())
				return pickedItemStack;

			ItemUtils.ItemStacksMerger ism = new ItemStacksMerger(slot.getItemStack(), pickedItemStack);
			ism.merge();

			setPickedItemStack(ism.into);
			slot.setItemStack(ism.merge);
			slot.onSlotChanged();

			return pickedItemStack;
		}

		int slotNumber = slot instanceof PlayerInventorySlot ? slot.slotNumber + inventory.size : slot.slotNumber;
		draggedItemStacks.put(slotNumber, null);
		draggedItemStackChanged = true;

		if (action == DRAG_START_LEFT_CLICK || action == DRAG_START_RIGHT_CLICK)
		{
			draggedAmount = pickedItemStack.stackSize;
			dragType = action == DRAG_START_LEFT_CLICK ? DRAG_TYPE_SPREAD : DRAG_TYPE_ONE;
			return pickedItemStack;
		}

		if (draggedItemStacks.size() > draggedAmount)
			return null;

		// action == DRAG_ADD_SLOT
		int amountPerSlot = dragType == DRAG_TYPE_SPREAD ? Math.max(draggedAmount / draggedItemStacks.size(), 1) : 1;
		int amountTotal = 0;
		HashMap<Integer, MalisisSlot> draggedSlots = getDraggedSlots();
		for (Entry<Integer, ItemStack> entry : draggedItemStacks.entrySet())
		{
			// work on a copy because we alter pickedItemStack only in the end
			ItemStack itemStack = pickedItemStack.copy();
			itemStack.stackSize = draggedAmount;
			ItemStack slotStack = draggedSlots.get(entry.getKey()).getItemStack();
			if (slotStack != null)
				slotStack = slotStack.copy();

			ItemUtils.ItemStacksMerger ism = new ItemStacksMerger(itemStack, slotStack);
			ism.merge(amountPerSlot, draggedSlots.get(entry.getKey()).getSlotStackLimit());

			itemStack.stackSize = ism.nbMerged;
			amountTotal += ism.nbMerged;
			draggedItemStacks.put(entry.getKey(), itemStack);

		}

		pickedItemStack.stackSize = draggedAmount - amountTotal;
		return pickedItemStack;
	}

	/**
	 * Gets the corresponding slots for the draggedItemStacks
	 * 
	 * @return
	 */
	private HashMap<Integer, MalisisSlot> getDraggedSlots()
	{
		HashMap<Integer, MalisisSlot> slots = new HashMap<>();

		for (Entry<Integer, ItemStack> entry : draggedItemStacks.entrySet())
		{
			int slotNumber = entry.getKey();
			MalisisInventory inventory = this.inventory;
			if (slotNumber >= inventory.size)
			{
				inventory = playerInventory;
				slotNumber -= this.inventory.size;
			}

			slots.put(entry.getKey(), inventory.getSlot(slotNumber));
		}
		return slots;
	}

	/**
	 * Reset the dragging state
	 */
	private void resetDrag()
	{
		if (!isDraggingItemStack())
			return;

		if (pickedItemStack != null)
			pickedItemStack.stackSize = draggedAmount;
		draggedItemStacks.clear();
		draggedAmount = 0;
		draggedItemStackChanged = true;
		dragType = -1;
	}

	/*
	 * COMPATIBILITY
	 */
	@Override
	public boolean canInteractWith(EntityPlayer var1)
	{
		return true;
	}
}
