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

package net.malisis.core.inventory.message;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import net.malisis.core.MalisisCore;
import net.malisis.core.inventory.MalisisInventory;
import net.malisis.core.inventory.MalisisInventoryContainer;
import net.malisis.core.inventory.MalisisSlot;
import net.malisis.core.network.IMalisisMessageHandler;
import net.malisis.core.network.MalisisMessage;
import net.malisis.core.util.Utils;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Message to update the slots in the opened {@link MalisisInventoryContainer} on the client.
 *
 * @author Ordinastie
 *
 */
@MalisisMessage
public class UpdateInventorySlotsMessage implements IMalisisMessageHandler<UpdateInventorySlotsMessage.Packet, IMessage>
{
	public static int PICKEDITEM = -1;

	public UpdateInventorySlotsMessage()
	{
		MalisisCore.network.registerMessage(this, Packet.class, Side.CLIENT);
	}

	/**
	 * Handles the received {@link Packet} on the client.<br>
	 * Updates the slots in the client {@link MalisisInventory}
	 *
	 * @param message the message
	 * @param ctx the ctx
	 */
	@Override
	public void process(Packet message, MessageContext ctx)
	{
		EntityPlayerSP player = (EntityPlayerSP) Utils.getClientPlayer();
		Container c = player.openContainer;
		if (message.windowId != c.windowId || !(c instanceof MalisisInventoryContainer))
			return;

		MalisisInventoryContainer container = (MalisisInventoryContainer) c;
		if (message.inventoryId == PICKEDITEM)
		{
			container.setPickedItemStack(message.slots.get(-1));
			return;
		}

		MalisisInventory inventory = container.getInventory(message.inventoryId);
		if (inventory == null)
			return;

		for (Entry<Integer, ItemStack> entry : message.slots.entrySet())
		{
			Integer slotNumber = entry.getKey();
			ItemStack itemStack = entry.getValue();

			inventory.setItemStack(slotNumber, itemStack);
		}
	}

	/**
	 * Sends a {@link Packet} to player to update the picked {@link ItemStack}.
	 *
	 * @param itemStack the item stack
	 * @param player the player
	 * @param windowId the window id
	 */
	public static void updatePickedItemStack(ItemStack itemStack, EntityPlayerMP player, int windowId)
	{
		Packet packet = new Packet(PICKEDITEM, windowId);
		packet.draggedItemStack(itemStack);
		MalisisCore.network.sendTo(packet, player);
	}

	/**
	 * Sends a {@link Packet} to player to update the inventory slots.
	 *
	 * @param inventoryId the inventory id
	 * @param slots the slots
	 * @param player the player
	 * @param windowId the window id
	 */
	public static void updateSlots(int inventoryId, ArrayList<MalisisSlot> slots, EntityPlayerMP player, int windowId)
	{
		Packet packet = new Packet(inventoryId, windowId);
		for (MalisisSlot slot : slots)
			packet.addSlot(slot);
		MalisisCore.network.sendTo(packet, player);
	}

	public static class Packet implements IMessage
	{
		private int inventoryId;
		private HashMap<Integer, ItemStack> slots = new HashMap<>();
		private int windowId;

		public Packet()
		{}

		public Packet(int inventoryId, int windowId)
		{
			this.inventoryId = inventoryId;
			this.windowId = windowId;
		}

		public void addSlot(MalisisSlot slot)
		{
			slots.put(slot.slotNumber, slot.getItemStack());
		}

		public void setSlots(HashMap<Integer, ItemStack> slots)
		{
			if (slots != null)
				this.slots = slots;
		}

		public void draggedItemStack(ItemStack itemStack)
		{
			slots.put(-1, itemStack);
		}

		@Override
		public void fromBytes(ByteBuf buf)
		{
			this.inventoryId = buf.readInt();
			this.windowId = buf.readInt();
			int size = buf.readInt();

			for (int i = 0; i < size; i++)
				slots.put(buf.readInt(), ByteBufUtils.readItemStack(buf));
		}

		@Override
		public void toBytes(ByteBuf buf)
		{
			buf.writeInt(inventoryId);
			buf.writeInt(windowId);
			buf.writeInt(slots.size());

			for (Entry<Integer, ItemStack> entry : slots.entrySet())
			{
				Integer slotNumber = entry.getKey();
				ItemStack itemStack = entry.getValue();

				buf.writeInt(slotNumber);
				ByteBufUtils.writeItemStack(buf, itemStack);
			}
		}
	}
}
