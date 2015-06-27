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
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.inventory.MalisisInventory;
import net.malisis.core.inventory.MalisisInventoryContainer;
import net.malisis.core.network.MalisisMessage;
import net.minecraft.entity.player.EntityPlayerMP;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Message to tell the client to open a GUI.
 *
 * @author Ordinastie
 *
 */
@MalisisMessage
public class CloseInventoryMessage implements IMessageHandler<CloseInventoryMessage.Packet, IMessage>
{
	public enum ContainerType
	{
		TYPE_TILEENTITY, TYPE_ITEM;
	}

	public CloseInventoryMessage()
	{
		MalisisCore.network.registerMessage(this, Packet.class, Side.CLIENT);
	}

	/**
	 * Handles the received {@link Packet} on the client. Close the GUI.
	 *
	 * @param message the message
	 * @param ctx the ctx
	 * @return the i message
	 */
	@Override
	public IMessage onMessage(Packet message, MessageContext ctx)
	{
		if (ctx.side == Side.CLIENT)
			closeGui();
		return null;
	}

	/**
	 * Open a the GUI for the {@link MalisisInventoryContainer}.
	 *
	 * @param type the type
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @param windowId the window id
	 */
	@SideOnly(Side.CLIENT)
	private void closeGui()
	{
		if (MalisisGui.currentGui() != null)
			MalisisGui.currentGui().close();
	}

	/**
	 * Sends a packet to client to notify it to open a {@link MalisisInventory}.
	 *
	 * @param container the container
	 * @param player the player
	 * @param windowId the window id
	 */
	public static void send(EntityPlayerMP player)
	{
		Packet packet = new Packet();
		MalisisCore.network.sendTo(packet, player);
	}

	public static class Packet implements IMessage
	{
		public Packet()
		{}

		@Override
		public void fromBytes(ByteBuf buf)
		{}

		@Override
		public void toBytes(ByteBuf buf)
		{}
	}
}
