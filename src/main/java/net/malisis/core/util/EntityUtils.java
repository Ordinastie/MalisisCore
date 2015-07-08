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

package net.malisis.core.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.malisis.core.MalisisCore;
import net.malisis.core.asm.AsmUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerManager;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Utility class for Entities.
 *
 * @author Ordinastie
 *
 */

public class EntityUtils
{
	private static ForgeDirection[] facings = new ForgeDirection[] { ForgeDirection.NORTH, ForgeDirection.EAST, ForgeDirection.SOUTH,
			ForgeDirection.WEST, ForgeDirection.UP, ForgeDirection.DOWN, ForgeDirection.UNKNOWN };

	private static Method getPlayerInstance;
	private static Field playersWatchingChunk;
	static
	{
		try
		{
			getPlayerInstance = AsmUtils.changeMethodAccess(PlayerManager.class, "getPlayerInstance", "func_72690_a", "IIZ");
			Class<?> clazz = Class.forName("net.minecraft.server.management.PlayerManager$PlayerInstance");
			playersWatchingChunk = AsmUtils.changeFieldAccess(clazz, "playersWatchingChunk", "field_73263_b");
		}
		catch (ClassNotFoundException e)
		{
			MalisisCore.log.error("Failed to get PlayerInstance class.", e);
		}

	}

	/**
	 * Eject a new item corresponding to the {@link ItemStack}.
	 *
	 * @param world the world
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @param itemStack the item stack
	 */
	public static void spawnEjectedItem(World world, int x, int y, int z, ItemStack itemStack)
	{
		float rx = world.rand.nextFloat() * 0.8F + 0.1F;
		float ry = world.rand.nextFloat() * 0.8F + 0.1F;
		float rz = world.rand.nextFloat() * 0.8F + 0.1F;

		EntityItem entityItem = new EntityItem(world, x + rx, y + ry, z + rz, itemStack);

		float factor = 0.05F;
		entityItem.motionX = world.rand.nextGaussian() * factor;
		entityItem.motionY = world.rand.nextGaussian() * factor + 0.2F;
		entityItem.motionZ = world.rand.nextGaussian() * factor;
		world.spawnEntityInWorld(entityItem);

	}

	/**
	 * Finds a player by its UUID.
	 *
	 * @param uuid the uuid
	 * @return the player
	 */
	public static EntityPlayerMP findPlayerFromUUID(UUID uuid)
	{
		List<EntityPlayerMP> listPlayers = MinecraftServer.getServer().getConfigurationManager().playerEntityList;

		for (EntityPlayerMP player : listPlayers)
			if (player.getUniqueID().equals(uuid))
				return player;

		return null;
	}

	/**
	 * Gets the {@link ForgeDirection} the {@link Entity} is currently facing.
	 *
	 * @param entity the entity
	 * @return the direction
	 */
	public static ForgeDirection getEntityFacing(Entity entity)
	{
		return getEntityFacing(entity, false);
	}

	/**
	 * Gets the {@link ForgeDirection} the {@link Entity} is currently facing.<br>
	 * If <b>sixWays</b> is <code>true</code>, the direction can be {@link ForgeDirection#UP UP} or {@link ForgeDirection#DOWN DOWN} if the
	 * entity is looking up or down.
	 *
	 * @param entity the entity
	 * @param sixWays whether to consider UP and DOWN for directions
	 * @return the direction
	 */
	public static ForgeDirection getEntityFacing(Entity entity, boolean sixWays)
	{
		return facings[getEntityFacingInt(entity, sixWays)];
	}

	public static int getEntityFacingInt(Entity entity, boolean sixWays)
	{
		if (entity == null)
			return 6;

		float pitch = entity.rotationPitch;
		if (sixWays && pitch < -45)
			return 5;
		if (sixWays && pitch > 45)
			return 4;

		return (MathHelper.floor_double(entity.rotationYaw * 4.0F / 360.0F + 0.5D) + 2) & 3;
	}

	public static boolean isEquipped(EntityPlayer player, Item item)
	{
		return player != null && player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().getItem() == item;
	}

	public static boolean isEquipped(EntityPlayer player, ItemStack itemStack)
	{
		return isEquipped(player, itemStack != null ? itemStack.getItem() : null);
	}

	public static List<EntityPlayerMP> getPlayersWatchingChunk(Chunk chunk)
	{
		return getPlayersWatchingChunk((WorldServer) chunk.worldObj, chunk.xPosition, chunk.zPosition);
	}

	public static List<EntityPlayerMP> getPlayersWatchingChunk(WorldServer world, int x, int z)
	{
		if (playersWatchingChunk == null)
			return new ArrayList<>();

		try
		{
			Object playerInstance = getPlayerInstance.invoke(world.getPlayerManager(), x, z, false);
			if (playerInstance == null)
				return new ArrayList<>();
			return (List<EntityPlayerMP>) playersWatchingChunk.get(playerInstance);
		}
		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
			MalisisCore.log.info("Failed to get players watching chunk :", e);
			return new ArrayList<>();
		}
	}
}
