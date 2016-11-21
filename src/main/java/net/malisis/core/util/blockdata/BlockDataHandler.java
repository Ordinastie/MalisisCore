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

import net.malisis.core.MalisisCore;
import net.malisis.core.asm.AsmUtils;
import net.malisis.core.registry.AutoLoad;
import net.malisis.core.util.Silenced;
import net.malisis.core.util.Utils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import com.google.common.base.Function;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * {@link BlockDataHandler} handles custom data being stored for a specific {@link BlockPos}.
 *
 * <p>
 * Custom data is identified by a {@link String} identifier, and a way to convert from and into a {@link ByteBuf} using
 * {@link #registerBlockData(String, Function, Function)}.
 *
 * <p>
 * Custom data is then stored and retreived using {@link #setData(String, IBlockAccess, BlockPos, Object)},
 * {@link #getData(String, IBlockAccess, BlockPos)} and {@link #removeData(String, IBlockAccess, BlockPos)} with the corresponding
 * identifier.
 *
 * @author Ordinastie
 */
@AutoLoad
public class BlockDataHandler
{
	private static BlockDataHandler instance = new BlockDataHandler();
	private static Field chunkCacheField = MalisisCore.isClient() && FMLClientHandler.instance().hasOptifine() ? AsmUtils.changeFieldAccess(Silenced.get(() -> Class.forName("ChunkCacheOF")),
			"chunkCache") : null;

	private Map<String, HandlerInfo<?>> handlerInfos = new HashMap<>();
	private Table<String, Chunk, ChunkData<?>> serverDatas = HashBasedTable.create();
	private Table<String, Chunk, ChunkData<?>> clientDatas = HashBasedTable.create();

	private BlockDataHandler()
	{
		MinecraftForge.EVENT_BUS.register(this);
	}

	public Map<String, HandlerInfo<?>> getHandlerInfos()
	{
		return handlerInfos;
	}

	/**
	 * Get the server or client data depending on the passed {@link World}.
	 *
	 * @param world the world
	 * @return the table
	 */
	private Table<String, Chunk, ChunkData<?>> data(World world)
	{
		return world(world).isRemote ? instance.clientDatas : instance.serverDatas;
	}

	/**
	 * Gets the actual world object based on the passed {@link IBlockAccess}.
	 *
	 * @param world the world
	 * @return the world
	 */
	private World world(IBlockAccess world)
	{
		if (world instanceof World)
			return (World) world;
		else if (world instanceof ChunkCache)
			return ((ChunkCache) world).world;

		if (FMLClientHandler.instance().hasOptifine() && chunkCacheField != null)
			return world(Silenced.get(() -> ((ChunkCache) chunkCacheField.get(world))));

		return null;
	}

	/**
	 * Gets the {@link ChunkData} for the specified identifier and {@link BlockPos}
	 *
	 * @param <T> the generic type
	 * @param identifier the identifier
	 * @param world the world
	 * @param pos the pos
	 * @return the chunk data
	 */
	private <T> ChunkData<T> chunkData(String identifier, World world, BlockPos pos)
	{
		return world != null ? chunkData(identifier, world, world.getChunkFromBlockCoords(pos)) : null;
	}

	/**
	 * Gets the {@link ChunkData} for the specified identifier and {@link Chunk}.
	 *
	 * @param <T> the generic type
	 * @param identifier the identifier
	 * @param world the world
	 * @param chunk the chunk
	 * @return the chunk data
	 */
	@SuppressWarnings("unchecked")
	private <T> ChunkData<T> chunkData(String identifier, World world, Chunk chunk)
	{
		return (ChunkData<T>) instance.data(world).get(identifier, chunk);

	}

	/**
	 * Creates the {@link ChunkData} for specified identifier for the {@link Chunk} at the {@link BlockPos}.
	 *
	 * @param <T> the generic type
	 * @param identifier the identifier
	 * @param world the world
	 * @param pos the pos
	 * @return the chunk data
	 */
	@SuppressWarnings("unchecked")
	private <T> ChunkData<T> createChunkData(String identifier, World world, BlockPos pos)
	{
		Chunk chunk = world.getChunkFromBlockCoords(pos);

		//System.out.println("createChunkData (" + chunk.xPosition + "/" + chunk.zPosition + ") for " + identifier);

		ChunkData<T> chunkData = new ChunkData<>((HandlerInfo<T>) handlerInfos.get(identifier));
		instance.data(world).put(identifier, chunk, chunkData);
		return chunkData;
	}

	//#region Events
	/**
	 * Saves the data in NBT for the {@link Chunk}.<br>
	 * Also unloads the data if the <code>Chunk</code> is marked as <i>unloaded</i>.
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

			//			MalisisCore.message("onDataLoad (" + event.getChunk().xPosition + "/" + event.getChunk().zPosition + ") for "
			//					+ handlerInfo.identifier);
			ChunkData<?> chunkData = new ChunkData<>(handlerInfo);
			chunkData.fromBytes(Unpooled.copiedBuffer(nbt.getByteArray(handlerInfo.identifier)));
			data(event.getWorld()).put(handlerInfo.identifier, event.getChunk(), chunkData);
		}
	}

	/**
	 * On data save.
	 *
	 * @param event the event
	 */
	@SubscribeEvent
	public void onDataSave(ChunkDataEvent.Save event)
	{
		NBTTagCompound nbt = event.getData();

		for (HandlerInfo<?> handlerInfo : handlerInfos.values())
		{
			ChunkData<?> chunkData = chunkData(handlerInfo.identifier, event.getWorld(), event.getChunk());
			if (chunkData != null && chunkData.hasData())
			{
				//				MalisisCore.message("onDataSave (" + event.getChunk().xPosition + "/" + event.getChunk().zPosition + ") for "
				//						+ handlerInfo.identifier);
				ByteBuf buf = Unpooled.buffer();
				chunkData.toBytes(buf);
				nbt.setByteArray(handlerInfo.identifier, buf.capacity(buf.writerIndex()).array());
			}

			//unload data on save because saving is called after unload
			if (event.getChunk().unloaded)
				data(event.getWorld()).remove(handlerInfo.identifier, event.getChunk());

		}
	}

	/**
	 * Unloads the data for the {@link Chunk}.<br>
	 * Does the process client side only because unloading happens before saving on the server.
	 *
	 * @param event the event
	 */
	@SubscribeEvent
	public void onDataUnload(ChunkEvent.Unload event)
	{
		//only unload on client, server unloads on save
		if (!event.getWorld().isRemote)
			return;

		for (HandlerInfo<?> handlerInfo : handlerInfos.values())
		{
			data(event.getWorld()).remove(handlerInfo.identifier, event.getChunk());
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
		Chunk chunk = event.getPlayer().world.getChunkFromChunkCoords(event.getChunk().chunkXPos, event.getChunk().chunkZPos);
		for (HandlerInfo<?> handlerInfo : handlerInfos.values())
		{
			ChunkData<?> chunkData = instance.chunkData(handlerInfo.identifier, chunk.getWorld(), chunk);
			if (chunkData != null && chunkData.hasData())
				BlockDataMessage.sendBlockData(chunk, handlerInfo.identifier, chunkData.toBytes(Unpooled.buffer()), event.getPlayer());
		}
	}

	//#end Events

	/**
	 * Registers a custom block data with the specified identifier.
	 *
	 * @param <T> the generic type
	 * @param identifier the identifier
	 * @param from the from
	 * @param to the to
	 */
	public static <T> void registerBlockData(String identifier, Function<ByteBuf, T> from, Function<T, ByteBuf> to)
	{
		instance.handlerInfos.put(identifier, new HandlerInfo<>(identifier, from, to));
	}

	/**
	 * Gets the custom data stored at the {@link BlockPos} for the specified identifier.
	 *
	 * @param <T> the generic type
	 * @param identifier the identifier
	 * @param world the world
	 * @param pos the pos
	 * @return the data
	 */
	public static <T> T getData(String identifier, IBlockAccess world, BlockPos pos)
	{
		ChunkData<T> chunkData = instance.<T> chunkData(identifier, instance.world(world), pos);
		return chunkData != null ? chunkData.getData(pos) : null;
	}

	/**
	 * Sets the custom data to be stored at the {@link BlockPos} for the specified identifier.
	 *
	 * @param <T> the generic type
	 * @param identifier the identifier
	 * @param world the world
	 * @param pos the pos
	 * @param data the data
	 */
	public static <T> void setData(String identifier, IBlockAccess world, BlockPos pos, T data)
	{
		ChunkData<T> chunkData = instance.<T> chunkData(identifier, instance.world(world), pos);
		if (chunkData == null)
			chunkData = instance.<T> createChunkData(identifier, instance.world(world), pos);

		chunkData.setData(pos, data);
	}

	/**
	 * Removes the custom data stored at the {@link BlockPos} for the specified identifier.
	 *
	 * @param <T> the generic type
	 * @param identifier the identifier
	 * @param world the world
	 * @param pos the pos
	 */
	public static <T> void removeData(String identifier, IBlockAccess world, BlockPos pos)
	{
		setData(identifier, world, pos, null);
	}

	static void setBlockData(int chunkX, int chunkZ, String identifier, ByteBuf data)
	{
		HandlerInfo<?> handlerInfo = instance.handlerInfos.get(identifier);
		if (handlerInfo == null)
			return;

		//MalisisCore.message("SetBlockData (" + chunkX + "/" + chunkZ + ") for " + identifier);
		Chunk chunk = Utils.getClientWorld().getChunkFromChunkCoords(chunkX, chunkZ);
		ChunkData<?> chunkData = new ChunkData<>(handlerInfo).fromBytes(data);
		instance.data(chunk.getWorld()).put(handlerInfo.identifier, chunk, chunkData);
	}

	public static BlockDataHandler get()
	{
		return instance;
	}

	/**
	 * Internal container for custom data identifier and conversion from/to {@link ByteBuf}.
	 *
	 * @param <T> the generic type
	 */
	public static class HandlerInfo<T>
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

	/**
	 * Internal data storage for a specified {@link HandlerInfo}.
	 *
	 * @param <T> the generic type
	 */
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