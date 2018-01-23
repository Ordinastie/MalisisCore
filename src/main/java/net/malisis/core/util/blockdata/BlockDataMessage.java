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

package net.malisis.core.util.blockdata;

import io.netty.buffer.ByteBuf;
import net.malisis.core.MalisisCore;
import net.malisis.core.network.IMalisisMessageHandler;
import net.malisis.core.registry.AutoLoad;
import net.malisis.core.util.blockdata.BlockDataMessage.Packet;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * @author Ordinastie
 *
 */
@AutoLoad(true)
public class BlockDataMessage implements IMalisisMessageHandler<Packet, IMessage>
{
	public BlockDataMessage()
	{
		MalisisCore.network.registerMessage(this, Packet.class, Side.CLIENT);
	}

	@Override
	public void process(Packet message, MessageContext ctx)
	{
		BlockDataHandler.setBlockData(message.x, message.z, message.identifier, message.data);
	}

	/**
	 * Sends the data to the specified {@link EntityPlayerMP}.
	 *
	 * @param chunk the chunk
	 * @param identifier the identifier
	 * @param data the data
	 * @param player the player
	 */
	public static void sendBlockData(Chunk chunk, String identifier, ByteBuf data, EntityPlayerMP player)
	{
		MalisisCore.network.sendTo(new Packet(chunk, identifier, data), player);
	}

	/**
	 * Sends the data to all the players currently watching the specified {@link Chunk}.
	 *
	 * @param chunk the chunk
	 * @param identifer the identifer
	 * @param data the data
	 */
	public static void sendBlockData(Chunk chunk, String identifer, ByteBuf data)
	{
		MalisisCore.network.sendToPlayersWatchingChunk(new Packet(chunk, identifer, data), chunk);
	}

	public static class Packet implements IMessage
	{
		private int x;
		private int z;
		private String identifier;
		private ByteBuf data;

		public Packet()
		{}

		public Packet(Chunk chunk, String identifier, ByteBuf data)
		{
			this.x = chunk.x;
			this.z = chunk.z;
			this.identifier = identifier;
			this.data = data;
		}

		@Override
		public void fromBytes(ByteBuf buf)
		{
			x = buf.readInt();
			z = buf.readInt();
			identifier = ByteBufUtils.readUTF8String(buf);
			data = buf.readBytes(buf.readableBytes());
		}

		@Override
		public void toBytes(ByteBuf buf)
		{
			buf.writeInt(x);
			buf.writeInt(z);
			ByteBufUtils.writeUTF8String(buf, identifier);
			buf.writeBytes(data);
		}
	}
}
