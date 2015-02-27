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
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Message to update the slots in the opened {@link MalisisInventoryContainer} on the client.
 *
 * @author Ordinastie
 *
 */
public class UpdateInventorySlotsMessage implements IMessageHandler<UpdateInventorySlotsMessage.Packet, IMessage>
{
	public static UpdateInventorySlotsMessage instance = new UpdateInventorySlotsMessage();

	public static int PICKEDITEM = -1;

	public UpdateInventorySlotsMessage()
	{
		MalisisCore.network.registerMessage(this, Packet.class, Side.CLIENT);
	}

	/**
	 * Handles the received {@link Packet} on the client. Updates the slots.
	 *
	 * @param message the message
	 * @param ctx the ctx
	 * @return the i message
	 */
	@Override
	public IMessage onMessage(Packet message, MessageContext ctx)
	{
		if (ctx.side == Side.CLIENT)
			updateSlots(message.inventoryId, message.slots, message.windowId);

		return null;
	}

	/**
	 * Handles the reception of packets that update the inventory of the client.
	 *
	 * @param inventoryId the inventory id
	 * @param slots the slots
	 * @param windowId the window id
	 */
	@SideOnly(Side.CLIENT)
	private void updateSlots(int inventoryId, HashMap<Integer, ItemStack> slots, int windowId)
	{
		EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
		Container c = player.openContainer;
		if (windowId != c.windowId || !(c instanceof MalisisInventoryContainer))
			return;

		MalisisInventoryContainer container = (MalisisInventoryContainer) c;
		if (inventoryId == PICKEDITEM)
		{
			container.setPickedItemStack(slots.get(-1));
			return;
		}

		MalisisInventory inventory = container.getInventory(inventoryId);
		if (inventory == null)
			return;

		for (Entry<Integer, ItemStack> entry : slots.entrySet())
		{
			Integer slotNumber = entry.getKey();
			ItemStack itemStack = entry.getValue();

			inventory.setItemStack(slotNumber, itemStack);
		}
	}

	/**
	 * Sends a packet to player to update the picked itemStack.
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
	 * Sends a packet to player to update the inventory slots.
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
