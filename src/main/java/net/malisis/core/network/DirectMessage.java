/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 Ordinastie
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

import java.util.Map;

import com.google.common.collect.Maps;

import io.netty.buffer.ByteBuf;
import net.malisis.core.MalisisCore;
import net.malisis.core.registry.AutoLoad;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * DirectMessage class provides an easy way to trigger actions on the other side.<br>
 * Register a {@link Runnable} with {@link #registerMessage(Runnable)}, and trigger it with {@link #send(int)} or
 * {@link #send(int, EntityPlayerMP)}.
 *
 * @author Ordinastie
 *
 */
@AutoLoad(true)
public class DirectMessage implements IMalisisMessageHandler<DirectMessage.Packet, IMessage>
{
	private static int nextId = 0;
	private static Map<Integer, Runnable> runnables = Maps.newHashMap();

	public DirectMessage()
	{
		MalisisCore.network.registerMessage(this, DirectMessage.Packet.class, Side.SERVER);
	}

	/**
	 * Registers a {@link Runnable} to be executed on the other side.<br>
	 * Call {@link #send(int)} or {@link #send(int, EntityPlayerMP)} with the value returned by this method.
	 *
	 * @param runnable the runnable
	 * @return the message ID used for {@link #send(int)} and {@link #send(int, EntityPlayerMP)}
	 */
	public static int registerMessage(Runnable runnable)
	{
		runnables.put(nextId, runnable);
		return nextId++;
	}

	@Override
	public void process(Packet message, MessageContext ctx)
	{
		runnables.get(message.id).run();
	}

	/**
	 * Sends a message to the server to run the {@link Runnable} associated to the passed {@code message}.
	 *
	 * @param message the message
	 */
	public static void send(int message)
	{
		MalisisCore.network.sendToServer(new Packet(message));
	}

	/**
	 * Sends a message to the {@code player} to run the {@link Runnable} associated to the passed {@code message}.
	 *
	 * @param message the message
	 * @param player the player
	 */
	public static void send(int message, EntityPlayerMP player)
	{
		MalisisCore.network.sendTo(new Packet(message), player);
	}

	public static class Packet implements IMessage
	{
		int id;

		public Packet()
		{}

		public Packet(int id)
		{
			this.id = id;
		}

		@Override
		public void fromBytes(ByteBuf buf)
		{
			id = buf.readInt();
		}

		@Override
		public void toBytes(ByteBuf buf)
		{
			buf.writeInt(id);
		}
	}
}
