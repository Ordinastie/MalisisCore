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

import io.netty.buffer.ByteBuf;
import net.malisis.core.MalisisCore;
import net.malisis.core.network.IMalisisMessageHandler;
import net.malisis.core.network.MalisisMessage;
import net.malisis.core.util.clientnotif.NeighborChangedMessage.Packet;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
		doProcess(message, ctx);
	}

	@SideOnly(Side.CLIENT)
	private void doProcess(Packet message, MessageContext ctx)
	{
		IBlockState state = Minecraft.getMinecraft().theWorld.getBlockState(message.pos);
		state.neighborChanged(Minecraft.getMinecraft().theWorld, message.pos, message.neighbor);
	}

	public static void send(World world, BlockPos pos, Block neighbor)
	{
		Packet packet = new Packet(pos, neighbor);
		MalisisCore.network.sendToPlayersWatchingChunk(packet, world.getChunkFromChunkCoords(pos.getX() >> 4, pos.getZ() >> 4));
	}

	public static class Packet implements IMessage
	{
		private BlockPos pos;
		private Block neighbor;

		public Packet()
		{}

		public Packet(BlockPos pos, Block neighbor)
		{
			this.pos = pos;
			this.neighbor = neighbor;
		}

		@Override
		public void fromBytes(ByteBuf buf)
		{
			pos = BlockPos.fromLong(buf.readLong());
			neighbor = Block.getBlockById(buf.readInt());
		}

		@Override
		public void toBytes(ByteBuf buf)
		{
			buf.writeLong(pos.toLong());
			buf.writeInt(Block.getIdFromBlock(neighbor));
		}
	}
}
