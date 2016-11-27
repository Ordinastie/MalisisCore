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
import net.malisis.core.MalisisCore;
import net.malisis.core.inventory.IInventoryProvider;
import net.malisis.core.inventory.IInventoryProvider.IDeferredInventoryProvider;
import net.malisis.core.inventory.IInventoryProvider.IDirectInventoryProvider;
import net.malisis.core.inventory.MalisisInventory;
import net.malisis.core.network.IMalisisMessageHandler;
import net.malisis.core.network.MalisisMessage;
import net.malisis.core.util.TileEntityUtils;
import net.malisis.core.util.Utils;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Message to tell the client to open a GUI.
 *
 * @author Ordinastie
 *
 */
@MalisisMessage
public class OpenInventoryMessage implements IMalisisMessageHandler<OpenInventoryMessage.Packet, IMessage>
{
	public enum ContainerType
	{
		TYPE_TILEENTITY,
		TYPE_ITEM;
	}

	public OpenInventoryMessage()
	{
		MalisisCore.network.registerMessage(this, Packet.class, Side.CLIENT);
	}

	/**
	 * Handles the received {@link Packet} on the client.<br>
	 * Opens the GUI for the {@link MalisisInventory}
	 *
	 * @param message the message
	 * @param ctx the ctx
	 */
	@Override
	public void process(Packet message, MessageContext ctx)
	{
		EntityPlayerSP player = (EntityPlayerSP) Utils.getClientPlayer();
		if (message.type == ContainerType.TYPE_TILEENTITY)
		{
			IDirectInventoryProvider inventoryProvider = TileEntityUtils.getTileEntity(IDirectInventoryProvider.class,
					Utils.getClientWorld(),
					message.pos);
			if (inventoryProvider != null)
				MalisisInventory.open(player, inventoryProvider, message.windowId);
		}

		else if (message.type == ContainerType.TYPE_ITEM)
		{
			//TODO: send and use slot number instead of limited to equipped
			ItemStack itemStack = player.getHeldItemMainhand();
			if (itemStack == null || !(itemStack.getItem() instanceof IDeferredInventoryProvider<?>))
				return;

			@SuppressWarnings("unchecked")
			IDeferredInventoryProvider<ItemStack> inventoryProvider = (IDeferredInventoryProvider<ItemStack>) itemStack.getItem();
			MalisisInventory.open(player, inventoryProvider, itemStack, message.windowId);
		}
	}

	/**
	 * Sends a packet to client to notify it to open a {@link MalisisInventory}.
	 *
	 * @param container the container
	 * @param player the player
	 * @param windowId the window id
	 */
	public static void send(IInventoryProvider container, EntityPlayerMP player, int windowId)
	{
		Packet packet = new Packet(container, windowId);
		MalisisCore.network.sendTo(packet, player);
	}

	public static class Packet implements IMessage
	{
		private ContainerType type;
		private BlockPos pos;
		private int windowId;

		public Packet()
		{}

		public Packet(IInventoryProvider container, int windowId)
		{
			this.windowId = windowId;
			if (container instanceof TileEntity)
			{
				this.type = ContainerType.TYPE_TILEENTITY;
				this.pos = ((TileEntity) container).getPos();
			}
			if (container instanceof Item)
				this.type = ContainerType.TYPE_ITEM;
		}

		@Override
		public void fromBytes(ByteBuf buf)
		{
			this.type = ContainerType.values()[buf.readByte()];
			if (type == ContainerType.TYPE_TILEENTITY)
			{
				this.pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
			}
			this.windowId = buf.readInt();
		}

		@Override
		public void toBytes(ByteBuf buf)
		{
			buf.writeByte(type.ordinal());
			if (type == ContainerType.TYPE_TILEENTITY)
			{
				buf.writeInt(pos.getX());
				buf.writeInt(pos.getY());
				buf.writeInt(pos.getZ());
			}
			buf.writeInt(windowId);
		}

	}
}
