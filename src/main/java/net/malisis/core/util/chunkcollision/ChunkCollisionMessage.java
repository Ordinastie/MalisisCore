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

package net.malisis.core.util.chunkcollision;

import io.netty.buffer.ByteBuf;
import net.malisis.core.MalisisCore;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.chunk.Chunk;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;

/**
 * @author Ordinastie
 *
 */
public class ChunkCollisionMessage implements IMessageHandler<ChunkCollisionMessage.Packet, IMessage>
{
	public static ChunkCollisionMessage instance = new ChunkCollisionMessage();

	public ChunkCollisionMessage()
	{
		MalisisCore.network.registerMessage(this, Packet.class, Side.CLIENT);
	}

	@Override
	public IMessage onMessage(Packet message, MessageContext ctx)
	{
		if (ctx.side != Side.CLIENT)
			return null;

		ChunkCollision.client.setCoords(message.x, message.z, message.coords);
		return null;
	}

	public static void sendCoords(Chunk chunk, long[] coords, EntityPlayerMP player)
	{
		MalisisCore.network.sendTo(new Packet(chunk, coords), player);
	}

	public static class Packet implements IMessage
	{
		private int x;
		private int z;
		private long[] coords;

		public Packet()
		{}

		public Packet(Chunk chunk, long[] coords)
		{
			this.x = chunk.xPosition;
			this.z = chunk.zPosition;
			this.coords = coords;
		}

		@Override
		public void fromBytes(ByteBuf buf)
		{
			x = buf.readInt();
			z = buf.readInt();
			coords = new long[buf.readInt()];
			for (int i = 0; i < coords.length; i++)
				coords[i] = buf.readLong();
		}

		@Override
		public void toBytes(ByteBuf buf)
		{
			buf.writeInt(x);
			buf.writeInt(z);
			buf.writeInt(coords.length);
			for (long c : coords)
				buf.writeLong(c);
		}
	}
}
