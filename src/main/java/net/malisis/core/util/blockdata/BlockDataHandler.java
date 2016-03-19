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
import io.netty.buffer.Unpooled;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.malisis.core.asm.AsmUtils;
import net.malisis.core.util.Silenced;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import com.google.common.base.Function;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * @author Ordinastie
 *
 */
public class BlockDataHandler
{
	private static BlockDataHandler instance = new BlockDataHandler();
	private static Field worldField = AsmUtils.changeFieldAccess(ChunkCache.class, "worldObj", "field_72815_e");

	private Map<String, HandlerInfo<?>> handlerInfos = new HashMap<>();
	private Table<String, Chunk, ChunkData<?>> serverDatas = HashBasedTable.create();
	private Table<String, Chunk, ChunkData<?>> clientDatas = HashBasedTable.create();

	public BlockDataHandler()
	{

	}

	private Table<String, Chunk, ChunkData<?>> data(World world)
	{
		return world(world).isRemote ? instance.clientDatas : instance.serverDatas;
	}

	private World world(IBlockAccess world)
	{
		if (world instanceof World)
			return (World) world;
		else if (world instanceof ChunkCache)
			return Silenced.get(() -> ((World) worldField.get(world)));
		return null;
	}

	private <T> ChunkData<T> chunkData(String identifier, World world, BlockPos pos)
	{
		return chunkData(identifier, world, world.getChunkFromBlockCoords(pos));
	}

	@SuppressWarnings("unchecked")
	private <T> ChunkData<T> chunkData(String identifier, World world, Chunk chunk)
	{
		return (ChunkData<T>) instance.data(world).get(identifier, chunk);

	}

	@SuppressWarnings("unchecked")
	private <T> ChunkData<T> createChunkData(String identifier, World world, BlockPos pos)
	{
		Chunk chunk = world.getChunkFromBlockCoords(pos);

		System.out.println("createChunkData (" + chunk.xPosition + "/" + chunk.zPosition + ") for " + identifier);

		ChunkData<T> chunkData = new ChunkData<>((HandlerInfo<T>) handlerInfos.get(identifier));
		instance.data(world).put(identifier, chunk, chunkData);
		return chunkData;
	}

	public static <T> void registerBlockData(String identifier, Function<ByteBuf, T> from, Function<T, ByteBuf> to)
	{
		instance.handlerInfos.put(identifier, new HandlerInfo<>(identifier, from, to));
	}

	public static <T> T getData(String identifier, IBlockAccess world, BlockPos pos)
	{
		ChunkData<T> chunkData = instance.<T> chunkData(identifier, instance.world(world), pos);
		return chunkData != null ? chunkData.getData(pos) : null;
	}

	public static <T> void setData(String identifier, IBlockAccess world, BlockPos pos, T data)
	{
		ChunkData<T> chunkData = instance.<T> chunkData(identifier, instance.world(world), pos);
		if (chunkData == null)
			chunkData = instance.<T> createChunkData(identifier, instance.world(world), pos);

		chunkData.setData(pos, data);
	}

	public static <T> void removeData(String identifier, IBlockAccess world, BlockPos pos)
	{
		setData(identifier, world, pos, null);
	}

	//#region Events
	/**
	 * On data load.
	 *
	 * @param event the event
	 */
	@SubscribeEvent
	public void onDataLoad(ChunkDataEvent.Load event)
	{
		NBTTagCompound nbt = event.getData();

		for (HandlerInfo<?> handlerInfo : handlerInfos.values())
		{
			if (!nbt.hasKey(handlerInfo.identifier))
				continue;

			System.out.println("onDataLoad (" + event.getChunk().xPosition + "/" + event.getChunk().zPosition + ") for "
					+ handlerInfo.identifier);
			ChunkData<?> chunkData = new ChunkData<>(handlerInfo);
			chunkData.fromBytes(Unpooled.copiedBuffer(nbt.getByteArray(handlerInfo.identifier)));
			data(event.world).put(handlerInfo.identifier, event.getChunk(), chunkData);
		}
	}

	@SubscribeEvent
	public void onDataSave(ChunkDataEvent.Save event)
	{
		NBTTagCompound nbt = event.getData();

		for (HandlerInfo<?> handlerInfo : handlerInfos.values())
		{
			ChunkData<?> chunkData = chunkData(handlerInfo.identifier, event.world, event.getChunk());
			if (chunkData != null && chunkData.hasData())
			{
				ByteBuf buf = Unpooled.buffer();
				chunkData.toBytes(buf);
				nbt.setByteArray(handlerInfo.identifier, buf.capacity(buf.writerIndex()).array());
			}
		}
	}

	@SubscribeEvent
	public void onDataUnload(ChunkEvent.Unload event)
	{
		for (HandlerInfo<?> handlerInfo : handlerInfos.values())
		{
			data(event.world).remove(handlerInfo.identifier, event.getChunk());
		}
	}

	/**
	 * Server only.<br>
	 * Sends the chunks coordinates to the client when they get watched by them.
	 *
	 * @param event the event
	 */
	@SubscribeEvent
	public void onChunkWatched(ChunkWatchEvent.Watch event)
	{
		Chunk chunk = event.player.worldObj.getChunkFromChunkCoords(event.chunk.chunkXPos, event.chunk.chunkZPos);
		for (HandlerInfo<?> handlerInfo : handlerInfos.values())
		{
			ChunkData<?> chunkData = instance.chunkData(handlerInfo.identifier, chunk.getWorld(), chunk);
			if (chunkData != null && chunkData.hasData())
				BlockDataMessage.sendBlockData(chunk, handlerInfo.identifier, chunkData.toBytes(Unpooled.buffer()), event.player);
		}
	}

	//#end Events

	static void setBlockData(int chunkX, int chunkZ, String identifier, ByteBuf data)
	{
		HandlerInfo<?> handlerInfo = instance.handlerInfos.get(identifier);
		if (handlerInfo == null)
			return;

		System.out.println("SetBlockData (" + chunkX + "/" + chunkZ + ") for " + identifier);
		Chunk chunk = Minecraft.getMinecraft().theWorld.getChunkFromChunkCoords(chunkX, chunkZ);
		ChunkData<?> chunkData = new ChunkData<>(handlerInfo).fromBytes(data);
		instance.data(chunk.getWorld()).put(handlerInfo.identifier, chunk, chunkData);
	}

	public static BlockDataHandler get()
	{
		return instance;
	}

	static class HandlerInfo<T>
	{
		String identifier;
		private Function<ByteBuf, T> from;
		private Function<T, ByteBuf> to;

		public HandlerInfo(String identifier, Function<ByteBuf, T> from, Function<T, ByteBuf> to)
		{
			this.identifier = identifier;
			this.from = from;
			this.to = to;
		}
	}

	static class ChunkData<T>
	{
		private HandlerInfo<T> handlerInfos;
		private HashMap<BlockPos, T> data = new HashMap<>();

		public ChunkData(HandlerInfo<T> handlerInfo)
		{
			this.handlerInfos = handlerInfo;
		}

		public boolean hasData()
		{
			return data.size() > 0;
		}

		public T getData(BlockPos pos)
		{
			return data.get(pos);
		}

		public void setData(BlockPos pos, T blockData)
		{
			if (blockData != null)
				data.put(pos, blockData);
			else
				data.remove(pos);
		}

		public ChunkData<T> fromBytes(ByteBuf buf)
		{
			while (buf.isReadable())
			{
				BlockPos pos = BlockPos.fromLong(buf.readLong());
				ByteBuf b = buf.readBytes(buf.readInt());
				T blockData = handlerInfos.from.apply(b);
				data.put(pos, blockData);
			}

			return this;
		}

		public ByteBuf toBytes(ByteBuf buf)
		{
			for (Entry<BlockPos, T> entry : data.entrySet())
			{
				ByteBuf b = handlerInfos.to.apply(entry.getValue());
				buf.writeLong(entry.getKey().toLong());
				buf.writeInt(b.writerIndex());
				buf.writeBytes(b);
			}
			return buf;
		}

	}

}