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

package net.malisis.core.util.syncer.message;

import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.malisis.core.MalisisCore;
import net.malisis.core.inventory.MalisisInventoryContainer;
import net.malisis.core.network.IMalisisMessageHandler;
import net.malisis.core.network.MalisisMessage;
import net.malisis.core.util.syncer.ISyncHandler;
import net.malisis.core.util.syncer.ISyncableData;
import net.malisis.core.util.syncer.ObjectData;
import net.malisis.core.util.syncer.Syncer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Message to update the slots in the opened {@link MalisisInventoryContainer} on the client.
 *
 * @author Ordinastie
 *
 */
@MalisisMessage
public class SyncerMessage implements IMalisisMessageHandler<SyncerMessage.Packet<?, ?>, IMessage>
{
	public SyncerMessage()
	{
		MalisisCore.network.registerMessage(this, getPacketClass(), Side.CLIENT);
	}

	@SuppressWarnings("unchecked")
	private static Class<Packet<?, ?>> getPacketClass()
	{
		return (Class<Packet<?, ?>>) new Packet<>().getClass();
	}

	/**
	 * Handles the received {@link Packet} on the client.<br>
	 * Updates the fields for the receiver object.
	 *
	 * @param message the message
	 * @param ctx the ctx
	 */
	@Override
	public void process(Packet<?, ?> message, MessageContext ctx)
	{
		doProcess(message, ctx);
	}

	public <T, S extends ISyncableData> void doProcess(Packet<T, S> message, MessageContext ctx)
	{
		ISyncHandler<T, S> handler = message.getHandler();
		T receiver = handler.getReceiver(ctx, message.data);
		Syncer.get().updateValues(receiver, handler, message.values);
	}

	public static class Packet<T, S extends ISyncableData> implements IMessage
	{
		private int handlerId;
		private S data;
		private int indexes;
		private Map<String, Object> values;

		public Packet()
		{}

		public Packet(int handlerId, S data, int fieldIndexes, Map<String, Object> fieldValues)
		{
			this.handlerId = handlerId;
			this.data = data;
			this.indexes = fieldIndexes;
			this.values = fieldValues;
		}

		@SuppressWarnings("unchecked")
		private ISyncHandler<T, S> getHandler()
		{
			return (ISyncHandler<T, S>) Syncer.get().getHandlerFromId(handlerId);
		}

		@Override
		public void fromBytes(ByteBuf buf)
		{
			handlerId = buf.readInt();
			//handler
			ISyncHandler<T, S> handler = getHandler();
			if (handler == null)
				return;

			//data
			data = handler.getSyncData(null);
			data.fromBytes(buf);

			//indexes
			indexes = buf.readInt();

			//values
			values = new HashMap<>();

			int index = 0;
			Object data;
			Class<?> clazz;
			while (indexes > 0)
			{
				if ((indexes & 1) != 0)
				{
					data = null;
					ObjectData od = handler.getObjectData(index);
					clazz = od.getType();

					if (ISyncableData.class.isAssignableFrom(clazz))
					{
						if (buf.readBoolean())
						{
							try
							{
								data = clazz.newInstance();
								((ISyncableData) data).fromBytes(buf);
							}
							catch (InstantiationException | IllegalAccessException e)
							{
								e.printStackTrace();
							}
						}
					}
					else if (clazz.isEnum())
					{
						data = clazz.getEnumConstants()[buf.readInt()];
					}
					else if (clazz == String.class)
					{
						if (buf.readBoolean())
							data = ByteBufUtils.readUTF8String(buf);
					}
					else if (clazz == boolean.class)
						data = buf.readBoolean();
					else if (clazz == byte.class)
						data = buf.readByte();
					else if (clazz == int.class)
						data = buf.readInt();
					else if (clazz == long.class)
						data = buf.readLong();
					else if (clazz == char.class)
						data = buf.readChar();
					else if (clazz == short.class)
						data = buf.readShort();
					else if (clazz == float.class)
						data = buf.readFloat();
					else if (clazz == double.class)
						data = buf.readDouble();

					values.put(od.getName(), data);
				}

				indexes = indexes >> 1;
				index++;
			}
		}

		@Override
		public void toBytes(ByteBuf buf)
		{
			//handler
			buf.writeInt(handlerId);
			//data
			data.toBytes(buf);
			//indexes
			buf.writeInt(indexes);

			//values
			for (Entry<String, Object> entry : values.entrySet())
			{
				Object obj = entry.getValue();

				if (obj == null)
					obj = false;
				else if (obj instanceof ISyncableData)
				{
					buf.writeBoolean(true);
					((ISyncableData) obj).toBytes(buf);
				}
				else if (obj instanceof Enum)
				{
					buf.writeInt(((Enum<?>) obj).ordinal());
				}
				else if (obj instanceof String)
				{
					buf.writeBoolean(true);
					ByteBufUtils.writeUTF8String(buf, (String) obj);
				}
				else if (obj instanceof Boolean)
					buf.writeBoolean((boolean) obj);
				else if (obj instanceof Byte)
					buf.writeByte((byte) obj);
				else if (obj instanceof Integer)
					buf.writeInt((int) obj);
				else if (obj instanceof Long)
					buf.writeLong((long) obj);
				else if (obj instanceof Character)
					buf.writeChar((char) obj);
				else if (obj instanceof Short)
					buf.writeShort((short) obj);
				else if (obj instanceof Float)
					buf.writeFloat((float) obj);
				else if (obj instanceof Double)
					buf.writeDouble((double) obj);
			}
		}
	}
}
