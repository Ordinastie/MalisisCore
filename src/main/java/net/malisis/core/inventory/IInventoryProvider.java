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

import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.util.ItemUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IInventoryProvider
{
	public interface IDirectInventoryProvider extends IInventoryProvider, IInventory
	{
		/**
		 * Gets the default inventory for this provider.<br>
		 * Default inventory is used for default vanilla {@link IInventory} interactions, or if you know that the provider only contains one
		 * inventory.
		 *
		 * @return the default inventory
		 */
		public MalisisInventory getInventory();

		/**
		 * Gets all the {@link MalisisInventory inventories} for this {@link IInventoryProvider}.
		 *
		 * @return the inventories
		 */
		public default MalisisInventory[] getInventories()
		{
			return new MalisisInventory[] { getInventory() };
		}

		/**
		 * Gets the {@link MalisisGui} associated with the {@link MalisisInventory}.
		 *
		 * @param container the container
		 * @return the GUI to open
		 */
		@SideOnly(Side.CLIENT)
		public MalisisGui getGui(MalisisInventoryContainer container);

		/**
		 * Empties all the inventories of this {@link IInventoryProvider}
		 */
		@Override
		public default void clear()
		{
			for (MalisisInventory inventory : getInventories())
				inventory.emptyInventory();
		}

		/**
		 * Break all the inventories of this {@link IInventoryProvider}.
		 *
		 * @param world the world
		 * @param pos the pos
		 */
		public default void breakInventories(World world, BlockPos pos)
		{
			for (MalisisInventory inventory : getInventories())
				inventory.breakInventory(world, pos);
		}

		//#region IInventory
		@Override
		public default boolean hasCustomName()
		{
			return getInventory() != null && getInventory().hasCustomName();
		}

		@Override
		public default String getCommandSenderName()
		{
			return getInventory() != null ? getInventory().getName() : null;
		}

		@Override
		public default IChatComponent getDisplayName()
		{
			return hasCustomName() ? new ChatComponentText(this.getCommandSenderName()) : new ChatComponentTranslation(
					this.getCommandSenderName());
		}

		/**
		 * Gets the number of slots in the default inventory (first one).
		 *
		 * @return the size inventory
		 */
		@Override
		public default int getSizeInventory()
		{
			return getInventory() != null ? getInventory().size : 0;
		}

		/**
		 * Gets the {@link ItemStack} is the slot
		 *
		 * @param index the index
		 * @return the stack in slot
		 */
		@Override
		public default ItemStack getStackInSlot(int index)
		{
			return getInventory() != null ? getInventory().getItemStack(index) : null;
		}

		/**
		 * Removes from an slot up to a specified count of items and returns them in a new stack.
		 *
		 * @param index the index
		 * @param count the count
		 * @return the item stack
		 */
		@Override
		public default ItemStack decrStackSize(int index, int count)
		{
			return getInventory() != null ? (new ItemUtils.ItemStackSplitter(getInventory().getItemStack(index))).split(count) : null;
		}

		/**
		 * When some containers are closed they call this on each slot, then drop whatever it returns as an EntityItem - like when you close
		 * a workbench GUI.
		 *
		 * @param index the index
		 * @return the stack in slot on closing
		 */
		@Override
		public default ItemStack getStackInSlotOnClosing(int index)
		{
			return getStackInSlot(index);
		}

		/**
		 * Sets the given item stack to the specified slot in the inventory
		 *
		 * @param index the index
		 * @param stack the stack
		 */
		@Override
		public default void setInventorySlotContents(int index, ItemStack stack)
		{
			MalisisInventory inventory = getInventory();
			if (inventory != null)
				inventory.setItemStack(index, stack);
		}

		/**
		 * Returns the maximum stack size for a inventory slot. Seems to always be 64, possibly will be extended.
		 */
		@Override
		public default int getInventoryStackLimit()
		{
			return getInventory() != null ? getInventory().getInventoryStackLimit() : 0;
		}

		/**
		 * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think it hasn't changed
		 * and skip it.
		 */
		@Override
		public default void markDirty()
		{}

		/**
		 * Do not give this method the name canInteractWith because it clashes with Container.
		 *
		 * @param player the player
		 * @return true, if is useable by player
		 */
		@Override
		public default boolean isUseableByPlayer(EntityPlayer player)
		{
			return true;
		}

		/**
		 * Opens inventory. *Does nothing*
		 *
		 * @param player the player
		 */
		@Override
		public default void openInventory(EntityPlayer player)
		{}

		/**
		 * Closes inventory. *Does nothing*
		 *
		 * @param player the player
		 */
		@Override
		public default void closeInventory(EntityPlayer player)
		{}

		/**
		 * Returns true if the {@link ItemStack} can be contained in the slot.
		 *
		 * @param index the index
		 * @param stack the stack
		 * @return true, if is item valid for slot
		 */
		@Override
		public default boolean isItemValidForSlot(int index, ItemStack stack)
		{
			MalisisInventory inventory = getInventory();
			if (inventory == null)
				return false;
			MalisisSlot slot = inventory.getSlot(index);
			return slot != null && slot.isItemValid(stack);
		}

		@Override
		public default int getField(int id)
		{
			return 0;
		}

		@Override
		public default void setField(int id, int value)
		{}

		@Override
		public default int getFieldCount()
		{
			return 0;
		}
		//#end IInventory
	}

	public interface IDeferredInventoryProvider<T> extends IInventoryProvider
	{
		/**
		 * Gets the default inventory for this provider.<br>
		 * Default inventory is used if you know that the provider only contains one inventory.
		 *
		 * @param data the data
		 * @return the default inventory
		 */
		public MalisisInventory getInventory(T data);

		/**
		 * Gets the {@link MalisisGui} associated with the {@link MalisisInventory}.
		 *
		 * @param data the data
		 * @param container the container
		 * @return the GUI to open
		 */
		@SideOnly(Side.CLIENT)
		public MalisisGui getGui(T data, MalisisInventoryContainer container);

		/**
		 * Gets all the {@link MalisisInventory inventories} for this {@link IInventoryProvider}.
		 *
		 * @param data null for TileEntity, ItemStack for Item
		 * @return the inventories
		 */
		public default MalisisInventory[] getInventories(T data)
		{
			return new MalisisInventory[] { getInventory(data) };
		}

		/**
		 * Empties all the inventories of this {@link IInventoryProvider}.
		 *
		 * @param data the data
		 */
		public default void clear(T data)
		{
			for (MalisisInventory inventory : getInventories(data))
				inventory.emptyInventory();
		}

		/**
		 * Break all the inventories of this {@link IInventoryProvider}.
		 *
		 * @param data the data
		 * @param world the world
		 * @param pos the pos
		 */
		public default void breakInventories(T data, World world, BlockPos pos)
		{
			for (MalisisInventory inventory : getInventories(data))
				inventory.breakInventory(world, pos);
		}
	}
}
