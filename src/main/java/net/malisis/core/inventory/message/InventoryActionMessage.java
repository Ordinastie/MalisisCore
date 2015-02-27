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
import net.malisis.core.inventory.MalisisInventoryContainer;
import net.malisis.core.inventory.MalisisInventoryContainer.ActionType;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Container;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Message to handle the inventory actions sent from a GUI.
 *
 * @author Ordinastie
 *
 */
public class InventoryActionMessage implements IMessageHandler<InventoryActionMessage.Packet, IMessage>
{
	public static InventoryActionMessage instance = new InventoryActionMessage();

	public InventoryActionMessage()
	{
		MalisisCore.network.registerMessage(this, Packet.class, Side.SERVER);
	}

	/**
	 * Handles the {@link Packet} received from the client. Pass the action to the {@link MalisisInventoryContainer}, and send the changes
	 * back to the client.
	 *
	 * @param message the message
	 * @param ctx the ctx
	 * @return the i message
	 */
	@Override
	public IMessage onMessage(Packet message, MessageContext ctx)
	{
		if (ctx.side != Side.SERVER)
			return null;

		Container c = ctx.getServerHandler().playerEntity.openContainer;
		if (message.windowId != c.windowId || !(c instanceof MalisisInventoryContainer))
			return null;

		MalisisInventoryContainer container = (MalisisInventoryContainer) c;
		container.handleAction(message.action, message.inventoryId, message.slotNumber, message.code);
		container.detectAndSendChanges();

		return null;
	}

	/**
	 * Sends GUI action to the server {@link MalisisInventoryContainer}.
	 *
	 * @param action the action
	 * @param inventoryId the inventory id
	 * @param slotNumber the slot number
	 * @param code the code
	 */
	@SideOnly(Side.CLIENT)
	public static void sendAction(ActionType action, int inventoryId, int slotNumber, int code)
	{
		int windowId = Minecraft.getMinecraft().thePlayer.openContainer.windowId;
		Packet packet = new Packet(action, inventoryId, slotNumber, code, windowId);
		MalisisCore.network.sendToServer(packet);
	}

	/**
	 * The packet holding the data
	 */
	public static class Packet implements IMessage
	{
		private ActionType action;
		private int inventoryId;
		private int slotNumber;
		private int code;
		private int windowId;

		public Packet()
		{}

		public Packet(ActionType action, int inventoryId, int slotNumber, int code, int windowId)
		{
			this.action = action;
			this.inventoryId = inventoryId;
			this.slotNumber = slotNumber;
			this.code = code;
			this.windowId = windowId;
		}

		@Override
		public void fromBytes(ByteBuf buf)
		{
			action = ActionType.values()[buf.readByte()];
			inventoryId = buf.readInt();
			slotNumber = buf.readInt();
			code = buf.readInt();
			windowId = buf.readInt();
		}

		@Override
		public void toBytes(ByteBuf buf)
		{
			buf.writeByte(action.ordinal());
			buf.writeInt(inventoryId);
			buf.writeInt(slotNumber);
			buf.writeInt(code);
			buf.writeInt(windowId);
		}
	}

}
