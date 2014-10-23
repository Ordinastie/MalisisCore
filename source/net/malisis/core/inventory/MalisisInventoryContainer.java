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

import static net.malisis.core.inventory.InventoryState.*;
import static net.malisis.core.inventory.MalisisInventoryContainer.ActionType.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import net.malisis.core.MalisisCore;
import net.malisis.core.inventory.player.PlayerInventory;
import net.malisis.core.inventory.player.PlayerInventorySlot;
import net.malisis.core.packet.UpdateInventorySlotsMessage;
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
	 * Id to use for the next inventory added
	 */
	private int nexInventoryId = 0;
	/**
	 * List of inventories handled by this container
	 */
	protected HashMap<Integer, MalisisInventory> inventories = new HashMap<>();
	/**
	 * Base inventory for this <code>MalisisInventoryContainer</code>
	 */
	//protected MalisisInventory inventory;
	/**
	 * Inventory of the player that opened this <code>MalisisInventoryContainer</code>
	 */
	//protected MalisisInventory playerInventory;
	/**
	 * ItemStack cache for base inventory. Used to know what itemStacks need to be sent to client
	 */
	protected HashMap<Integer, MalisisInventory> inventoryCache = new HashMap<>();
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
	 *
	 */
	protected Set<MalisisSlot> draggedSlots = new HashSet<>();
	/**
	 * Type drag action. Can be DRAG_TYPE_SPREAD, DRAG_TYPE_ONE or DRAG_TYPE_PICKUP. Set to -1 if not currently dragging.
	 */
	protected int dragType = -1;
	/**
	 * Stores the last itemStack that was shift clicked. Used for shift double click.
	 */
	protected ItemStack lastShiftClicked;

	public MalisisInventoryContainer(EntityPlayer player, int windowId)
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
		addInventory(new PlayerInventory(player));
		this.owner.openContainer = this;
	}

	public int addInventory(MalisisInventory inventory)
	{
		inventory.setInventoryId(nexInventoryId);
		inventories.put(nexInventoryId, inventory);
		inventoryCache.put(nexInventoryId, inventory);

		//do not add container to current player inventory
		if (nexInventoryId != 0)
			inventory.addOpenedContainer(this);

		return nexInventoryId++;
	}

	public void removeInventory(MalisisInventory inventory)
	{
		//do not remove player inventory
		if (inventory == null || inventory.getInventoryId() == 0)
			return;

		inventory.removeOpenedContainer(this);
		inventories.remove(inventory.getInventoryId());
		inventoryCache.remove(inventory.getInventoryId());
	}

	// #region getters/setters
	/**
	 * @return the inventory of this <code>MalisisInventoryContainer</code> with the specified id.
	 */
	public MalisisInventory getInventory(int id)
	{
		return inventories.get(id);
	}

	/**
	 * @return player's inventory of this <code>MalisisInventoryContainer</code>.
	 */
	public MalisisInventory getPlayerInventory()
	{
		return inventories.get(0);
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
			return dragType == button && draggedSlots.size() > 1;

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
			return button == 1 && draggedSlots.size() > 1;
		if (dragType == DRAG_TYPE_ONE)
			return button == 0 && draggedSlots.size() > 1;

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

		for (MalisisInventory inventory : inventories.values())
		{
			if (inventory.getInventoryId() != 0)
			{
				ArrayList<MalisisSlot> slots = new ArrayList<>(Arrays.asList(inventory.getSlots()));
				UpdateInventorySlotsMessage.updateSlots(inventory.getInventoryId(), slots, (EntityPlayerMP) owner, windowId);
			}
		}
	}

	/**
	 * Sends all changes for base inventory, player's inventory, picked up itemStack and dragged itemStacks.
	 */
	@Override
	public void detectAndSendChanges()
	{
		for (MalisisInventory inventory : inventories.values())
			detectAndSendInventoryChanges(inventory);
		detectAndSendPickedItemStack();
	}

	/**
	 * Sends all changes for the inventory determined by {@link SlotType type}.
	 *
	 * @param type
	 */
	public void detectAndSendInventoryChanges(MalisisInventory inventory)
	{
		if (!(owner instanceof EntityPlayerMP))
		{
			MalisisCore.log.error("MalisisInventoryContainer tried to send inventory slots CLIENT side !.");
			return;
		}

		ArrayList<MalisisSlot> changedSlots = new ArrayList<>();

		for (MalisisSlot slot : inventory.getSlots())
		{
			if (slot.hasChanged())
			{
				changedSlots.add(slot);
				slot.updateCache();
			}
		}

		if (changedSlots.size() > 0)
			UpdateInventorySlotsMessage.updateSlots(inventory.getInventoryId(), changedSlots, (EntityPlayerMP) owner, windowId);
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

	// #end network

	@Override
	public void onContainerClosed(EntityPlayer owner)
	{
		super.onContainerClosed(owner);
		for (MalisisInventory inventory : inventories.values())
			if (inventory.getInventoryId() != 0)
				inventory.removeOpenedContainer(this);
	}

	/**
	 * Handle the action for this <code>MalisisInventoryContainer</code>. See {@link ActionType} for possible actions.
	 *
	 * @param action
	 * @param slotNumber
	 * @param code
	 * @param isPlayerInv
	 * @return itemStack resulting of the actions. Should be used to check client/server coherence.
	 */
	public ItemStack handleAction(ActionType action, int inventoryId, int slotNumber, int code)
	{
		MalisisInventory inv = inventories.get(inventoryId);
		if (inv == null)
		{
			MalisisCore.log.error("[MalisisInventoryContainer] Tried to handle an action for a wrong inventory (" + inventoryId + ").");
			return null;
		}
		MalisisSlot slot = inv.getSlot(slotNumber);
		if (slot == null)
		{
			MalisisCore.log.error("[MalisisInventoryContainer] Tried to handle an action for a wrong slotNumber (" + slotNumber + ").");
			return null;
		}

		if (slot.getState().is(FROZEN))
			return pickedItemStack;

		//first check if current slot is current providing inventory (for Items providing inventory)
		if (slot.getItemStack() != null && slot.getItemStack().getItem() instanceof IInventoryProvider
				&& slot.getItemStack().getTagCompound() != null)
		{
			if (slot.getItemStack().getTagCompound().getInteger("inventoryId") == 1)
			{
				owner.closeScreen();
				return null;
			}
			else if (slot.getItemStack().getTagCompound().hasKey("inventoryId"))
				slot.getItemStack().getTagCompound().removeTag("inventoryId");
		}

		// player pressed 1-9 key while hovering a slot
		if (action == HOTBAR && code >= 0 && code < 9)
			return handleHotbar(slot, code);

		// player pressed keyBindDrop key while hovering a slot
		if (action == DROP_SLOT_STACK || action == DROP_SLOT_ONE)
			return handleDropSlot(slot, action == DROP_SLOT_STACK);

		// player started/ended/reset/is currently dragging
		if (action.isDragAction())
			return handleDrag(action, inventoryId, slot);

		// from this point, any action should stop the dragging
		resetDrag();

		// player clicked outside the GUI with an item picked up
		if (action == DROP_ONE || action == DROP_STACK)
			return handleDropPickedStack(action == DROP_STACK);

		//player middle click on a slot
		if (action == PICKBLOCK && owner.capabilities.isCreativeMode)
			return handlePickBlock(slot);

		//normal left/right click
		if (action == LEFT_CLICK || action == RIGHT_CLICK)
			return handleNormalClick(slot, action == LEFT_CLICK);

		if (action == SHIFT_LEFT_CLICK)
		{
			// we need to store last shift clicked item for double click because at the time of the second click, slot may be empty and we
			// wouldn't know what itemStack to move
			lastShiftClicked = slot.getItemStack();
			ItemStack itemStack = handleShiftClick(inventoryId, slot);
			if (itemStack != null)
				lastShiftClicked = null;
			return itemStack;
		}

		if (action == DOUBLE_LEFT_CLICK || action == DOUBLE_SHIFT_LEFT_CLICK)
			return handleDoubleClick(inventoryId, slot, action == DOUBLE_SHIFT_LEFT_CLICK);

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

			if (slot.hasChanged())
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
			if (slot.hasChanged())
				slot.onSlotChanged();
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
	private ItemStack handleShiftClick(int inventoryId, MalisisSlot slot)
	{
		if (slot.getItemStack() == null)
			return null;

		ItemStack itemStack = slot.getItemStack();
		//transfer into PlayerInventory
		if (inventoryId != 0)
		{
			if (!slot.getState().is(PLAYER_EXTRACT))
				return null;
			if (!inventories.get(0).state.is(PLAYER_INSERT))
				return null;

			itemStack = inventories.get(0).transferInto(itemStack);

			slot.setItemStack(itemStack);
			if (slot.hasChanged())
				slot.onSlotChanged();

			return itemStack;
		}
		else
		//comes from PlayerInventory
		{
			if (!inventories.get(0).state.is(PLAYER_EXTRACT))
				return null;

			MalisisInventory targetInventory;
			int i = 1;
			while (itemStack != null && (targetInventory = inventories.get(i++)) != null)
			{
				if (targetInventory.state.is(PLAYER_INSERT))
				{
					itemStack = targetInventory.transferInto(itemStack);

					slot.setItemStack(itemStack);
					if (slot.hasChanged())
						slot.onSlotChanged();
				}
			}
			return itemStack;
		}

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
		MalisisSlot destSlot = inventories.get(0).getSlot(num);

		// slot from player's inventory, swap itemStacks
		if (fromPlayerInv || slot.getItemStack() == null)
		{
			ItemUtils.ItemStacksMerger ism = new ItemUtils.ItemStacksMerger(destSlot.getItemStack(), slot.getItemStack());
			ism.merge(ItemUtils.FULL_STACK, slot.getSlotStackLimit());

			destSlot.setItemStack(ism.merge);
			if (destSlot.hasChanged())
				destSlot.onSlotChanged();
			slot.setItemStack(ism.into);
			if (slot.hasChanged())
				slot.onSlotChanged();
			return null;
		}
		else
		// merge itemStack in slot into slot in hotbar. If already holding an itemStack, move elsewhere inside player inventory
		{
			ItemUtils.ItemStacksMerger ism = new ItemUtils.ItemStacksMerger(slot.getItemStack(), destSlot.getItemStack());
			ism.merge();

			destSlot.setItemStack(ism.into);
			if (destSlot.hasChanged())
				destSlot.onSlotChanged();

			ItemStack itemStack = inventories.get(0).transferInto(ism.merge, false);
			slot.setItemStack(itemStack);
			if (slot.hasChanged())
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

		if (!slot.getState().is(PLAYER_EXTRACT))
			return null;

		ItemUtils.ItemStackSplitter iss = new ItemUtils.ItemStackSplitter(slot.getItemStack());
		iss.split(fullStack ? ItemUtils.FULL_STACK : 1);

		slot.setItemStack(iss.source);
		if (slot.hasChanged())
			slot.onSlotChanged();
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
	private ItemStack handleDoubleClick(int inventoryId, MalisisSlot slot, boolean shiftClick)
	{
		MalisisInventory inventory = inventories.get(inventoryId);
		if (!inventory.state.is(PLAYER_EXTRACT))
			return null;

		// normal double click, go through all hovered slot inventory to merge the slots with the currently picked one
		if (!shiftClick && pickedItemStack != null)
		{
			int i = 0;
			while (pickedItemStack.stackSize < pickedItemStack.getMaxStackSize() && i < inventory.size)
			{
				MalisisSlot s = inventory.getSlot(i);
				ItemUtils.ItemStacksMerger ism = new ItemStacksMerger(s.getItemStack(), pickedItemStack);
				ism.merge();
				s.setItemStack(ism.merge);
				if (s.hasChanged())
					s.onSlotChanged();
				pickedItemStack = ism.into;
				i++;
			}
			setPickedItemStack(pickedItemStack);
		}
		// shift double click, go through all hovered slot inventory to transfer matching itemStack to the other inventory
		else if (lastShiftClicked != null)
		{
			for (MalisisSlot s : inventory.getSlots())
			{
				MalisisInventory targetInventory;
				int i = 1;
				ItemStack itemStack = s.getItemStack();
				if (itemStack != null && ItemUtils.areItemStacksStackable(itemStack, lastShiftClicked))
				{
					if (inventoryId != 0)
					{
						itemStack = inventories.get(0).transferInto(itemStack);
						s.setItemStack(itemStack);
						if (s.hasChanged())
							s.onSlotChanged();
					}
					else
					{
						while (itemStack != null && (targetInventory = inventories.get(i++)) != null)
						{
							if (targetInventory.state.is(PLAYER_INSERT))
							{
								itemStack = targetInventory.transferInto(itemStack);
								s.setItemStack(itemStack);
								if (s.hasChanged())
									s.onSlotChanged();
							}
						}
					}

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
	private ItemStack handleDrag(ActionType action, int inventoryId, MalisisSlot slot)
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
			int amountMerged = 0;
			for (MalisisSlot s : draggedSlots)
			{
				if (s.isItemValid(pickedItemStack) && s.getState().is(PLAYER_INSERT))
				{
					ItemUtils.ItemStacksMerger ism = new ItemStacksMerger(s.getDraggedItemStack(), s.getItemStack());
					ism.merge();
					amountMerged += ism.nbMerged;
					s.setItemStack(ism.into);
					s.setDraggedItemStack(null);
					if (s.hasChanged())
						s.onSlotChanged();
				}
			}

			if (pickedItemStack.stackSize == 0)
				setPickedItemStack(null);

			resetDrag();

			if (pickedItemStack != null)
				pickedItemStack.stackSize -= amountMerged;

			return pickedItemStack;
		}

		// start dragging an itemStack with left mouse button while pressing control key
		if (action == DRAG_START_PICKUP)
		{
			if (slot.getState().is(PLAYER_EXTRACT))
				dragType = DRAG_TYPE_PICKUP;
			return pickedItemStack;
		}

		// passing over a new slot while picking up stacks
		if (action == DRAG_ADD_SLOT && dragType == DRAG_TYPE_PICKUP)
		{
			if (pickedItemStack.stackSize >= pickedItemStack.getMaxStackSize())
				return pickedItemStack;

			if (!slot.getState().is(PLAYER_EXTRACT))
				return pickedItemStack;

			ItemUtils.ItemStacksMerger ism = new ItemStacksMerger(slot.getItemStack(), pickedItemStack);
			ism.merge();

			setPickedItemStack(ism.into);
			slot.setItemStack(ism.merge);
			if (slot.hasChanged())
				slot.onSlotChanged();

			return pickedItemStack;
		}

		//we can't insert into slot, so no need to add to list
		if (!slot.getState().is(PLAYER_INSERT))
			return pickedItemStack;

		//add the current slot to the list of dragged slots
		draggedSlots.add(slot);

		if (action == DRAG_START_LEFT_CLICK || action == DRAG_START_RIGHT_CLICK)
		{
			draggedAmount = pickedItemStack.stackSize;
			dragType = action == DRAG_START_LEFT_CLICK ? DRAG_TYPE_SPREAD : DRAG_TYPE_ONE;
			return pickedItemStack;
		}

		if (draggedSlots.size() > draggedAmount)
			return null;

		// action == DRAG_ADD_SLOT
		if (draggedSlots.size() <= 1) // do not start spreading before it's dragged at least over two slots
			return pickedItemStack;
		int amountPerSlot = dragType == DRAG_TYPE_SPREAD ? Math.max(draggedAmount / draggedSlots.size(), 1) : 1;
		int amountTotal = 0;

		if (amountPerSlot == 16)
		{
			amountPerSlot += 0;
		}

		for (MalisisSlot s : draggedSlots)
		{
			if (s.isItemValid(pickedItemStack))
			{
				// work on a copy because we alter pickedItemStack only in the end
				ItemStack itemStack = pickedItemStack.copy();
				itemStack.stackSize = draggedAmount;
				ItemStack slotStack = s.getItemStack();
				if (slotStack != null) //work on a copy because we don't want to alter the slot itemStack
					slotStack = slotStack.copy();

				ItemUtils.ItemStacksMerger ism = new ItemStacksMerger(itemStack, slotStack);
				ism.merge(amountPerSlot, s.getSlotStackLimit());
				s.setDraggedItemStack(ism.into);

				amountTotal += ism.nbMerged;
			}

		}

		pickedItemStack.stackSize = draggedAmount - amountTotal;
		return pickedItemStack;
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

		for (MalisisSlot s : draggedSlots)
			s.setDraggedItemStack(null);

		draggedSlots.clear();
		draggedAmount = 0;

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
