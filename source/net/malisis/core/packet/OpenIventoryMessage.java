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

package net.malisis.core.packet;

import io.netty.buffer.ByteBuf;
import net.malisis.core.inventory.IInventoryProvider;
import net.malisis.core.inventory.MalisisInventory;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class OpenIventoryMessage implements IMessageHandler<OpenIventoryMessage.Packet, IMessage>
{
	public enum ContainerType
	{
		TYPE_TILEENTITY, TYPE_ITEM;
	}

	@Override
	public IMessage onMessage(Packet message, MessageContext ctx)
	{
		if (ctx.side == Side.CLIENT)
			openGui(message.type, message.x, message.y, message.z, message.windowId);
		return null;
	}

	/**
	 * Open a the GUI for the container.
	 * 
	 * @param type
	 * @param x
	 * @param y
	 * @param z
	 * @param windowId
	 */
	@SideOnly(Side.CLIENT)
	private void openGui(ContainerType type, int x, int y, int z, int windowId)
	{
		if (type == ContainerType.TYPE_TILEENTITY)
		{
			TileEntity te = Minecraft.getMinecraft().theWorld.getTileEntity(x, y, z);
			if (te instanceof IInventoryProvider)
			{
				((IInventoryProvider) te).getInventory().open(Minecraft.getMinecraft().thePlayer, windowId);
			}
		}
	}

	/**
	 * Send a packet to client to notify it to open a {@link MalisisInventory}.
	 * 
	 * @param container
	 * @param player
	 * @param windowId
	 */
	public static void send(IInventoryProvider container, EntityPlayerMP player, int windowId)
	{
		Packet packet = new Packet(container, windowId);
		MalisisPacket.network.sendTo(packet, player);
	}

	public static class Packet implements IMessage
	{
		private ContainerType type;
		private int x, y, z;
		private int windowId;

		public Packet()
		{}

		public Packet(IInventoryProvider container, int windowId)
		{
			this.windowId = windowId;
			if (container instanceof TileEntity)
			{
				this.type = ContainerType.TYPE_TILEENTITY;
				this.x = ((TileEntity) container).xCoord;
				this.y = ((TileEntity) container).yCoord;
				this.z = ((TileEntity) container).zCoord;
			}
		}

		@Override
		public void fromBytes(ByteBuf buf)
		{
			this.type = ContainerType.values()[buf.readByte()];
			if (type == ContainerType.TYPE_TILEENTITY)
			{
				this.x = buf.readInt();
				this.y = buf.readInt();
				this.z = buf.readInt();
				this.windowId = buf.readInt();
			}
		}

		@Override
		public void toBytes(ByteBuf buf)
		{
			buf.writeByte(type.ordinal());
			if (type == ContainerType.TYPE_TILEENTITY)
			{
				buf.writeInt(x);
				buf.writeInt(y);
				buf.writeInt(z);
				buf.writeInt(windowId);
			}
		}

	}
}
