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

package net.malisis.core.util.syncer.handlers;

import io.netty.buffer.ByteBuf;
import net.malisis.core.MalisisCore;
import net.malisis.core.util.syncer.ISyncableData;
import net.malisis.core.util.syncer.handlers.TileEntitySyncHandler.TESyncData;
import net.malisis.core.util.syncer.message.SyncerMessage.Packet;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

/**
 * @author Ordinastie
 *
 */
public class TileEntitySyncHandler extends DefaultSyncHandler<TileEntity, TESyncData>
{
	@Override
	public String getName()
	{
		return "TileEntity";
	}

	@Override
	public TileEntity getReceiver(MessageContext ctx, TESyncData data)
	{
		return Minecraft.getMinecraft().theWorld.getTileEntity(data.x, data.y, data.z);
	}

	@Override
	public TESyncData getSyncData(TileEntity caller)
	{
		return new TESyncData(caller);
	}

	@Override
	public void send(TileEntity caller, Packet packet)
	{
		MalisisCore.network.sendToPlayersWatchingChunk(packet,
				caller.getWorld().getChunkFromChunkCoords(caller.xCoord >> 4, caller.zCoord >> 4));
	}

	public static class TESyncData implements ISyncableData
	{
		private int x, y, z;

		public TESyncData(TileEntity te)
		{
			if (te == null)
				return;
			x = te.xCoord;
			y = te.yCoord;
			z = te.zCoord;
		}

		@Override
		public void fromBytes(ByteBuf buf)
		{
			x = buf.readInt();
			y = buf.readInt();
			z = buf.readInt();
		}

		@Override
		public void toBytes(ByteBuf buf)
		{
			buf.writeInt(x);
			buf.writeInt(y);
			buf.writeInt(z);
		}
	}

}
