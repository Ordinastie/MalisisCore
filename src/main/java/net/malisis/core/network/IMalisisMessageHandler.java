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

package net.malisis.core.network;

import net.malisis.core.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Extension of {@link IMessageHandler} to automatically handle the task scheduling to the main thread of receiving side.
 *
 * @author Ordinastie
 *
 */
public interface IMalisisMessageHandler<REQ extends IMessage, REPLY extends IMessage> extends IMessageHandler<REQ, REPLY>
{
	@Override
	public default REPLY onMessage(REQ message, MessageContext ctx)
	{
		if (useTask())
		{
			if (ctx.side.isClient())
				Minecraft.getMinecraft().addScheduledTask(() -> process(message, ctx));
			else if (ctx.side.isServer())
				((WorldServer) ctx.getServerHandler().playerEntity.world).addScheduledTask(() -> process(message, ctx));
		}
		else
		{
			process(message, ctx);
		}
		return null;
	}

	/**
	 * Processes the received message. This is executed on the main thread.
	 *
	 * @param message the message
	 * @param ctx the ctx
	 */
	public void process(REQ message, MessageContext ctx);

	public default boolean useTask()
	{
		return true;
	}

	/**
	 * Gets the correct client or server {@link World} based on {@link MessageContext}.
	 *
	 * @param ctx the ctx
	 * @return the world
	 */
	public static World getWorld(MessageContext ctx)
	{
		if (ctx.side == Side.SERVER)
			return ctx.getServerHandler().playerEntity.world;
		else
			return Utils.getClientWorld();
	}

	/**
	 * Gets the correct client or server {@link EntityPlayer} based on {@link MessageContext}.
	 *
	 * @param ctx the ctx
	 * @return the player
	 */
	public static EntityPlayer getPlayer(MessageContext ctx)
	{
		if (ctx.side == Side.SERVER)
			return ctx.getServerHandler().playerEntity;
		else
			return Utils.getClientPlayer();
	}
}
