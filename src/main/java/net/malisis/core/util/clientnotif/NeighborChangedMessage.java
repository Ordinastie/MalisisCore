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

package net.malisis.core.util.clientnotif;

import java.util.List;

import org.apache.commons.lang3.tuple.Triple;

import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;
import net.malisis.core.MalisisCore;
import net.malisis.core.network.IMalisisMessageHandler;
import net.malisis.core.network.MalisisMessage;
import net.malisis.core.util.clientnotif.NeighborChangedMessage.Packet;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * @author Ordinastie
 *
 */
@MalisisMessage
public class NeighborChangedMessage implements IMalisisMessageHandler<Packet, IMessage>
{
	public NeighborChangedMessage()
	{
		MalisisCore.network.registerMessage(this, Packet.class, Side.CLIENT);
	}

	@Override
	public void process(Packet message, MessageContext ctx)
	{
		World world = IMalisisMessageHandler.getWorld(ctx);
		for (Triple<BlockPos, Block, BlockPos> t : message.list)
			world.getBlockState(t.getLeft()).neighborChanged(world, t.getLeft(), t.getMiddle(), t.getRight());
	}

	public static void send(Chunk chunk, List<Triple<BlockPos, Block, BlockPos>> list)
	{
		Packet packet = new Packet(list);
		MalisisCore.network.sendToPlayersWatchingChunk(packet, chunk);
	}

	public static class Packet implements IMessage
	{
		private List<Triple<BlockPos, Block, BlockPos>> list = Lists.newArrayList();

		public Packet()
		{}

		public Packet(List<Triple<BlockPos, Block, BlockPos>> list)
		{
			this.list = list;
		}

		@Override
		public void fromBytes(ByteBuf buf)
		{
			int size = buf.readInt();
			for (int i = 0; i < size; i++)
				list.add(
						Triple.of(BlockPos.fromLong(buf.readLong()), Block.getBlockById(buf.readInt()), BlockPos.fromLong(buf.readLong())));
		}

		@Override
		public void toBytes(ByteBuf buf)
		{
			buf.writeInt(list.size());
			for (Triple<BlockPos, Block, BlockPos> t : list)
			{
				buf.writeLong(t.getLeft().toLong());
				buf.writeInt(Block.getIdFromBlock(t.getMiddle()));
				buf.writeLong(t.getRight().toLong());
			}
		}
	}
}
