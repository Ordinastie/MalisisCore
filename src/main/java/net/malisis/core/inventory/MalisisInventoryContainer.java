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

import static com.google.common.base.Preconditions.*;
import static net.malisis.core.inventory.InventoryState.*;
import static net.malisis.core.inventory.MalisisInventoryContainer.ActionType.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.malisis.core.MalisisCore;
import net.malisis.core.inventory.message.CloseInventoryMessage;
import net.malisis.core.inventory.message.UpdateInventorySlotsMessage;
import net.malisis.core.inventory.player.PlayerInventory;
import net.malisis.core.inventory.player.PlayerInventorySlot;
import net.malisis.core.util.ItemUtils;
import net.malisis.core.util.ItemUtils.ItemStacksMerger;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * Complete rewrite of {@link Container}.
 *
 * @author Ordinastie
 *
 */
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

		/**
		 * Checks if this {@link ActionType} is a drag action.
		 *
		 * @return true, if is a drag action
		 */
		public boolean isDragAction()
		{
			return this == DRAG_START_LEFT_CLICK || this == DRAG_START_RIGHT_CLICK || this == DRAG_START_PICKUP ||
					this == DRAG_ADD_SLOT || this == DRAG_END || this == DRAG_RESET;
		}
	}
	//@formatter:on

	/** Dragging the itemStack to be spread evenly among all crossed slots. */
	public static final int DRAG_TYPE_SPREAD = 0;
	/** Dragging the itemStack to leave only one item per crossed slot. */
	public static final int DRAG_TYPE_ONE = 1;
	/** Dragging the itemStack to pick up itemStacks held by crossed slots. */
	public static final int DRAG_TYPE_PICKUP = 2;

	/** Player that opened this {@link MalisisInventoryContainer}. */
	protected EntityPlayer owner;
	/** Id to use for the next inventory added. */
	private int nexInventoryId = 0;
	/** List of inventories handled by this container. */
	protected HashMap<Integer, MalisisInventory> inventories = new HashMap<>();
	/** ItemStack currently picked and following the cursor. */
	protected ItemStack pickedItemStack = ItemStack.EMPTY;
	/** Cache for the itemStack currently picked. */
	protected ItemStack pickedItemStackCache = ItemStack.EMPTY;
	/** Stack size when dragging started. */
	protected int draggedAmount = 0;
	/** The dragged slots. */
	protected Set<MalisisSlot> draggedSlots = new HashSet<>();
	/** Type drag action. Can be DRAG_TYPE_SPREAD, DRAG_TYPE_ONE or DRAG_TYPE_PICKUP. Set to -1 if not currently dragging. */
	protected int dragType = -1;
	/** Stores the last itemStack that was shift clicked. Used for shift double click. */
	protected ItemStack lastShiftClicked = ItemStack.EMPTY;

	/**
	 * Instantiates a new {@link MalisisInventoryContainer}.
	 *
	 * @param player the player
	 * @param windowId the window id
	 */
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

	/**
	 * Adds the {@link MalisisInventory} to this {@link MalisisInventoryContainer}.<br>
	 *
	 * @param inventory the inventory
	 * @return the inventoryId for the inventory
	 */
	public int addInventory(MalisisInventory inventory)
	{
		inventory.setInventoryId(nexInventoryId);
		inventories.put(nexInventoryId, inventory);

		//do not add container to current player inventory
		if (nexInventoryId != 0)
			inventory.addOpenedContainer(this);

		return nexInventoryId++;
	}

	/**
	 * Removes the {@link MalisisInventory} from this {@link MalisisInventoryContainer}.
	 *
	 * @param inventory the inventory
	 */
	public void removeInventory(MalisisInventory inventory)
	{
		//do not remove player inventory
		if (inventory == null || inventory.getInventoryId() == 0)
			return;

		inventory.removeOpenedContainer(this);
		inventories.remove(inventory.getInventoryId());
	}

	// #region getters/setters
	/**
	 * Gets the {@link MalisisInventory} of this {@link MalisisInventoryContainer} with the specified id.
	 *
	 * @param id the id
	 * @return the inventory
	 */
	public MalisisInventory getInventory(int id)
	{
		return inventories.get(id);
	}

	/**
	 * Gets player's inventory of this {@link MalisisInventoryContainer}.
	 *
	 * @return the inventory
	 */
	public MalisisInventory getPlayerInventory()
	{
		return inventories.get(0);
	}

	/**
	 * Sets the currently picked itemStack. Update player inventory.
	 *
	 * @param itemStack the new picked item stack
	 */
	public void setPickedItemStack(ItemStack itemStack)
	{
		pickedItemStack = checkNotNull(itemStack);
		owner.inventory.setItemStack(itemStack);
	}

	/**
	 * Gets the picked item stack.
	 *
	 * @return currently picked itemStack
	 */
	public ItemStack getPickedItemStack()
	{
		return pickedItemStack;
	}

	/**
	 * Checks if currently dragging an itemStack.
	 *
	 * @return true, if currently dragging an itemStack.
	 */
	public boolean isDraggingItemStack()
	{
		return dragType != -1;
	}

	/**
	 * Checks if the dragging should end based on the mouse button clicked.
	 *
	 * @param button the button
	 * @return true, if dragging should end
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
	 * Checks if the dragging should be reset based on the mouse button clicked.
	 *
	 * @param button the button
	 * @return true, if dragging should reset
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
	 * Gets the dragging type.
	 *
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
				UpdateInventorySlotsMessage.updateSlots(inventory.getInventoryId(), inventory.getSlots(), (EntityPlayerMP) owner, windowId);
		}
	}

	/**
	 * Sends all changes for base inventory, player's inventory, picked up itemStack and dragged itemStacks.
	 */
	@Override
	public void detectAndSendChanges()
	{
		inventories.values().forEach(this::detectAndSendInventoryChanges);
		detectAndSendPickedItemStack();
	}

	/**
	 * Sends all changes for the {@link MalisisInventory}.
	 *
	 * @param inventory the inventory
	 */
	public void detectAndSendInventoryChanges(MalisisInventory inventory)
	{
		if (!(owner instanceof EntityPlayerMP))
		{
			MalisisCore.log.error("MalisisInventoryContainer tried to send inventory slots CLIENT side !.");
			return;
		}

		List<MalisisSlot> changedSlots = inventory	.getSlots()
													.stream()
													.filter(s -> s.hasChanged(owner))
													.peek(s -> s.updateCache(owner))
													.collect(Collectors.toList());
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
		pickedItemStackCache = pickedItemStack.copy();
	}

	// #end network

	/**
	 * Closes this {@link MalisisInventoryContainer}, sends a message to force close the client GUI.
	 */
	public void close()
	{
		onContainerClosed(owner);
		CloseInventoryMessage.send((EntityPlayerMP) owner);

	}

	/**
	 * Called when this {@link MalisisInventoryContainer} is closed.
	 *
	 * @param owner the owner
	 */
	@Override
	public void onContainerClosed(EntityPlayer owner)
	{
		super.onContainerClosed(owner);
		inventories.values().stream().filter(i -> i.getInventoryId() != 0).forEach(i -> i.removeOpenedContainer(this));
	}

	/**
	 * Handles the action for this {@link MalisisInventoryContainer}. See {@link ActionType} for possible actions.
	 *
	 * @param action the action
	 * @param inventoryId the inventory id
	 * @param slotNumber the slot number
	 * @param code the code
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

		if (slot.isState(FROZEN))
			return pickedItemStack;

		//first check if current slot is current providing inventory (for Items providing inventory)
		//TODO : freeze the slot at inventory creation
		if (slot.getItemStack().getItem() instanceof IInventoryProvider && slot.getItemStack().getTagCompound() != null)
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
			if (itemStack.isEmpty())
				lastShiftClicked = ItemStack.EMPTY;
			return itemStack;
		}

		if (action == DOUBLE_LEFT_CLICK || action == DOUBLE_SHIFT_LEFT_CLICK)
			return handleDoubleClick(inventoryId, slot, action == DOUBLE_SHIFT_LEFT_CLICK);

		return null;
	}

	/**
	 * Drops one or the full itemStack currently picked up.
	 *
	 * @param fullStack the full stack
	 * @return the item stack
	 */
	private ItemStack handleDropPickedStack(boolean fullStack)
	{
		ItemUtils.ItemStackSplitter iss = new ItemUtils.ItemStackSplitter(pickedItemStack);
		iss.split(fullStack ? ItemUtils.FULL_STACK : 1);

		owner.dropItem(iss.split, true);
		setPickedItemStack(iss.source);

		return iss.source;
	}

	/**
	 * Handles the normal left or right click.
	 *
	 * @param slot the slot
	 * @param fullStack the full stack
	 * @return the item stack
	 */
	private ItemStack handleNormalClick(MalisisSlot slot, boolean fullStack)
	{
		// already picked up an itemStack, insert/swap itemStack
		if (!pickedItemStack.isEmpty() && slot.isItemValid(pickedItemStack) && slot.isState(PLAYER_INSERT | PLAYER_EXTRACT))
			setPickedItemStack(slot.insert(pickedItemStack, fullStack ? ItemUtils.FULL_STACK : 1, true));
		// pick itemStack in slot
		else if (slot.isState(PLAYER_EXTRACT))
			setPickedItemStack(slot.extract(fullStack ? ItemUtils.FULL_STACK : ItemUtils.HALF_STACK));

		return getPickedItemStack();
	}

	/**
	 * Handles shift clicking a slot.
	 *
	 * @param inventoryId the inventory id
	 * @param slot the slot
	 * @return the item stack
	 */
	private ItemStack handleShiftClick(int inventoryId, MalisisSlot slot)
	{
		ItemStack itemStack = slot.getItemStack();
		if (itemStack.isEmpty())
			return ItemStack.EMPTY;

		//transfer into PlayerInventory
		if (inventoryId != 0)
		{
			if (!slot.isState(PLAYER_EXTRACT) || !inventories.get(0).state.is(PLAYER_INSERT))
				return ItemStack.EMPTY;

			itemStack = inventories.get(0).transferInto(itemStack);
			slot.setItemStack(itemStack);
			slot.onSlotChanged();

			return itemStack;
		}
		//comes from PlayerInventory
		else
		{
			if (!inventories.get(0).state.is(PLAYER_EXTRACT))
				return ItemStack.EMPTY;

			MalisisInventory targetInventory;
			int i = 1;
			while (!itemStack.isEmpty() && (targetInventory = inventories.get(i++)) != null)
			{
				if (targetInventory.state.is(PLAYER_INSERT))
				{
					itemStack = targetInventory.transferInto(itemStack);

					slot.setItemStack(itemStack);
					slot.onSlotChanged();
				}
			}
			return itemStack;
		}
	}

	/**
	 * Handles player pressing 1-9 key while hovering a slot.
	 *
	 * @param hoveredSlot hoveredSlot
	 * @param num the num
	 * @return the item stack
	 */
	private ItemStack handleHotbar(MalisisSlot hoveredSlot, int num)
	{
		boolean fromPlayerInv = hoveredSlot instanceof PlayerInventorySlot;
		MalisisSlot hotbarSlot = inventories.get(0).getSlot(num);

		// slot from player's inventory, swap itemStacks
		if (fromPlayerInv || hoveredSlot.getItemStack().isEmpty())
		{
			if (hoveredSlot.isState(PLAYER_INSERT))
			{
				ItemStack dest = hotbarSlot.extract(ItemUtils.FULL_STACK);
				ItemStack src = hoveredSlot.extract(ItemUtils.FULL_STACK);

				dest = hoveredSlot.insert(dest);
				//couldn't fit all into the slot, put back what's left in hotbar
				if (!dest.isEmpty())
				{
					hotbarSlot.insert(dest);
					//src should be empty but better safe than sorry
					inventories.get(0).transferInto(src);
				}
				else
					src = hotbarSlot.insert(src);
			}
		}
		// merge itemStack in slot into hotbar. If already holding an itemStack, move elsewhere inside player inventory
		else
		{
			if (hoveredSlot.isState(PLAYER_EXTRACT))
			{
				ItemStack dest = hoveredSlot.extract(ItemUtils.FULL_STACK);
				ItemStack left = hotbarSlot.insert(dest, ItemUtils.FULL_STACK, true);
				inventories.get(0).transferInto(left, false);
			}
		}

		return hotbarSlot.getItemStack();
	}

	/**
	 * Drops itemStack from hovering slot.
	 *
	 * @param hoveredSlot the slot
	 * @param fullStack the full stack
	 * @return the item stack
	 */
	private ItemStack handleDropSlot(MalisisSlot hoveredSlot, boolean fullStack)
	{
		ItemStack itemStack = hoveredSlot.getItemStack();
		if (itemStack.isEmpty() || !hoveredSlot.isState(PLAYER_EXTRACT))
			return itemStack;

		ItemUtils.ItemStackSplitter iss = new ItemUtils.ItemStackSplitter(hoveredSlot.getItemStack());
		iss.split(fullStack ? ItemUtils.FULL_STACK : 1);

		hoveredSlot.setItemStack(iss.source);
		//if (slot.hasChanged())
		hoveredSlot.onSlotChanged();
		owner.dropItem(iss.split, true);

		if (iss.amount != 0)
			hoveredSlot.onPickupFromSlot(owner, iss.split);

		return iss.split;
	}

	/**
	 * Handle double clicking on a slot.
	 *
	 * @param inventoryId the inventory id
	 * @param slot the slot
	 * @param shiftClick the shift click
	 * @return the item stack
	 */
	private ItemStack handleDoubleClick(int inventoryId, MalisisSlot slot, boolean shiftClick)
	{
		MalisisInventory inventory = inventories.get(inventoryId);
		if (!inventory.state.is(PLAYER_EXTRACT))
			return ItemStack.EMPTY;

		// normal double click, go through all hovered slot inventory to merge the slots with the currently picked one
		if (!shiftClick && pickedItemStack.isEmpty())
		{
			int currentSlot = 0;
			while (pickedItemStack.getCount() < pickedItemStack.getMaxStackSize() && currentSlot < inventory.getSize())
			{
				if (slot.isState(PLAYER_EXTRACT))
				{
					MalisisSlot s = inventory.getSlot(currentSlot);
					if (!s.getItemStack().isEmpty() && s.getItemStack().getCount() < s.getItemStack().getMaxStackSize())
					{
						ItemUtils.ItemStacksMerger ism = new ItemStacksMerger(s.getItemStack(), pickedItemStack);
						ism.merge();
						s.setItemStack(ism.merge);
						s.onSlotChanged();
						pickedItemStack = ism.into;
					}
				}
				currentSlot++;

			}
			setPickedItemStack(pickedItemStack);
		}
		// shift double click, go through all hovered slot's inventory to transfer matching itemStack to the other inventory
		else if (lastShiftClicked.isEmpty())
		{
			for (MalisisSlot s : inventory.getSlots())
			{
				MalisisInventory targetInventory;
				ItemStack itemStack = s.getItemStack();
				if (slot.isState(PLAYER_EXTRACT) && ItemUtils.areItemStacksStackable(itemStack, lastShiftClicked))
				{
					if (inventoryId != 0)
					{
						itemStack = inventories.get(0).transferInto(itemStack);
						s.setItemStack(itemStack);
						//if (s.hasChanged())
						s.onSlotChanged();
					}
					else
					{
						int i = 1;
						while (!itemStack.isEmpty() && (targetInventory = inventories.get(i++)) != null)
						{
							if (targetInventory.state.is(PLAYER_INSERT))
							{
								itemStack = targetInventory.transferInto(itemStack);
								s.setItemStack(itemStack);
								//if (s.hasChanged())
								s.onSlotChanged();
							}
						}
					}

					//all inventories are full, no need to try to transfer more
					if (!itemStack.isEmpty())
						return pickedItemStack;
				}

			}
		}
		lastShiftClicked = ItemStack.EMPTY;
		return pickedItemStack;
	}

	/**
	 * Picks up the itemStack in the slot.
	 *
	 * @param slot the slot
	 * @return the item stack
	 */
	private ItemStack handlePickBlock(MalisisSlot slot)
	{
		if (slot.getItemStack().isEmpty() || !pickedItemStack.isEmpty())
			return null;

		ItemStack itemStack = slot.getItemStack().copy();
		itemStack.setCount(itemStack.getMaxStackSize());
		setPickedItemStack(itemStack);

		return itemStack;
	}

	/**
	 * Handles all drag actions.
	 *
	 * @param action the action
	 * @param inventoryId the inventory id
	 * @param slot the slot
	 * @return the item stack
	 */
	private ItemStack handleDrag(ActionType action, int inventoryId, MalisisSlot slot)
	{
		if (pickedItemStack.isEmpty())
			return ItemStack.EMPTY;

		if ((action == DRAG_START_LEFT_CLICK || action == DRAG_START_RIGHT_CLICK) && isDraggingItemStack())
			return ItemStack.EMPTY;

		// was currently dragging an itemStack but pressed the other mouse button
		if (action == DRAG_RESET)
		{
			resetDrag();
			return pickedItemStack;
		}

		// released the button while dragging with control key down
		if (action == DRAG_END && dragType == DRAG_TYPE_PICKUP)
		{
			int size = pickedItemStack.getCount();
			resetDrag();
			pickedItemStack.setCount(size);
			return pickedItemStack;
		}

		// released the mouse button used to start dragging an itemStack
		if (action == DRAG_END)
		{
			int amountMerged = 0;
			for (MalisisSlot s : draggedSlots)
			{
				if (s.isItemValid(pickedItemStack) && s.isState(PLAYER_INSERT))
				{
					//should never be empty
					if (!s.getDraggedItemStack().isEmpty())
					{
						amountMerged += s.getDraggedItemStack().getCount();
						s.insert(s.getDraggedItemStack());
						s.setDraggedItemStack(ItemStack.EMPTY);
					}
				}
			}

			if (pickedItemStack.isEmpty())
				setPickedItemStack(ItemStack.EMPTY);

			resetDrag();

			if (!pickedItemStack.isEmpty())
				pickedItemStack.shrink(amountMerged);

			return pickedItemStack;
		}

		// start dragging an itemStack with left mouse button while pressing control key
		if (action == DRAG_START_PICKUP)
		{
			if (slot.isState(PLAYER_EXTRACT))
				dragType = DRAG_TYPE_PICKUP;
			return pickedItemStack;
		}

		// passing over a new slot while picking up stacks
		if (action == DRAG_ADD_SLOT && dragType == DRAG_TYPE_PICKUP)
		{
			if (pickedItemStack.getCount() >= pickedItemStack.getMaxStackSize())
				return pickedItemStack;

			if (!slot.isState(PLAYER_EXTRACT))
				return pickedItemStack;

			ItemUtils.ItemStacksMerger ism = new ItemStacksMerger(slot.getItemStack(), pickedItemStack);
			ism.merge();

			setPickedItemStack(ism.into);
			slot.setItemStack(ism.merge);
			slot.onSlotChanged();

			return pickedItemStack;
		}

		//we can't insert into slot, so no need to add to list
		if (!slot.isState(PLAYER_INSERT))
			return pickedItemStack;

		//add the current slot to the list of dragged slots
		draggedSlots.add(slot);

		if (action == DRAG_START_LEFT_CLICK || action == DRAG_START_RIGHT_CLICK)
		{
			draggedAmount = pickedItemStack.getCount();
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

		for (MalisisSlot s : draggedSlots)
		{
			if (s.isItemValid(pickedItemStack))
			{
				// work on a copy because we alter pickedItemStack only in the end
				ItemStack itemStack = pickedItemStack.copy();
				itemStack.setCount(draggedAmount);
				ItemStack slotStack = s.getItemStack();
				if (!slotStack.isEmpty()) //work on a copy because we don't want to alter the slot itemStack
					slotStack = slotStack.copy();

				ItemUtils.ItemStacksMerger ism = new ItemStacksMerger(itemStack, slotStack);
				ism.merge(amountPerSlot, s.getSlotStackLimit());
				ism.into.shrink(s.getItemStack().getCount());
				s.setDraggedItemStack(ism.into);

				amountTotal += ism.nbMerged;
			}

		}

		pickedItemStack.setCount(draggedAmount - amountTotal);
		return pickedItemStack;
	}

	/**
	 * Resets the dragging state.
	 */
	@Override
	protected void resetDrag()
	{
		if (!isDraggingItemStack())
			return;

		if (!pickedItemStack.isEmpty())
			pickedItemStack.setCount(draggedAmount);

		draggedSlots.forEach(s -> s.setDraggedItemStack(ItemStack.EMPTY));

		draggedSlots.clear();
		draggedAmount = 0;
		dragType = -1;
	}

	/*
	 * COMPATIBILITY
	 */

	/**
	 * Can interact with.
	 *
	 * @param var1 the var1
	 * @return true, if successful
	 */
	@Override
	public boolean canInteractWith(EntityPlayer var1)
	{
		return true;
	}

	@Override
	public Slot getSlot(int slotId)
	{
		//prevents IndexOutOfBoundsException
		return null;
	}
}
