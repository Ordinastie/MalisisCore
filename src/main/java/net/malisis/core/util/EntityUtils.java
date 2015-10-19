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
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityDiggingFX;
import net.minecraft.client.particle.EntityDiggingFX.Factory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerManager;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Utility class for Entities.
 *
 * @author Ordinastie
 *
 */

public class EntityUtils
{
	private static EnumFacing[] facings = new EnumFacing[] { EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST,
			EnumFacing.UP, EnumFacing.DOWN };

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
	public static void spawnEjectedItem(World world, BlockPos pos, ItemStack itemStack)
	{
		float rx = world.rand.nextFloat() * 0.8F + 0.1F;
		float ry = world.rand.nextFloat() * 0.8F + 0.1F;
		float rz = world.rand.nextFloat() * 0.8F + 0.1F;

		EntityItem entityItem = new EntityItem(world, pos.getX() + rx, pos.getY() + ry, pos.getZ() + rz, itemStack);

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
	 * Gets the {@link EnumFacing} the {@link Entity} is currently facing.
	 *
	 * @param entity the entity
	 * @return the direction
	 */
	public static EnumFacing getEntityFacing(Entity entity)
	{
		return getEntityFacing(entity, false);
	}

	/**
	 * Gets the {@link EnumFacing} the {@link Entity} is currently facing.<br>
	 * If <b>sixWays</b> is <code>true</code>, the direction can be {@link EnumFacing#UP UP} or {@link EnumFacing#DOWN DOWN} if the entity
	 * is looking up or down.
	 *
	 * @param entity the entity
	 * @param sixWays whether to consider UP and DOWN for directions
	 * @return the direction
	 */
	public static EnumFacing getEntityFacing(Entity entity, boolean sixWays)
	{
		return facings[getEntityRotation(entity, sixWays)];
	}

	/**
	 * Gets the entity rotation based on where it's currently facing.
	 *
	 * @param entity the entity
	 * @return the entity rotation
	 */
	public static int getEntityRotation(Entity entity)
	{
		return getEntityRotation(entity, false);
	}

	/**
	 * Gets the entity rotation based on where it's currently facing.
	 *
	 * @param entity the entity
	 * @param sixWays the six ways
	 * @return the entity rotation
	 */
	public static int getEntityRotation(Entity entity, boolean sixWays)
	{
		if (entity == null)
			return 6;

		float pitch = entity.rotationPitch;
		if (sixWays && pitch < -45)
			return 4;
		if (sixWays && pitch > 45)
			return 5;

		return (MathHelper.floor_double(entity.rotationYaw * 4.0F / 360.0F + 0.5D) + 2) & 3;
	}

	/**
	 * Checks if is the {@link Item} is equipped for the player.
	 *
	 * @param player the player
	 * @param item the item
	 * @return true, if is equipped
	 */
	public static boolean isEquipped(EntityPlayer player, Item item)
	{
		return player != null && player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().getItem() == item;
	}

	/**
	 * Checks if is the {@link Item} contained in the {@link ItemStack} is equipped for the player.
	 *
	 * @param player the player
	 * @param itemStack the item stack
	 * @return true, if is equipped
	 */
	public static boolean isEquipped(EntityPlayer player, ItemStack itemStack)
	{
		return isEquipped(player, itemStack != null ? itemStack.getItem() : null);
	}

	/**
	 * Gets the list of players currently watching the {@link Chunk}.
	 *
	 * @param chunk the chunk
	 * @return the players watching chunk
	 */
	public static List<EntityPlayerMP> getPlayersWatchingChunk(Chunk chunk)
	{
		return getPlayersWatchingChunk((WorldServer) chunk.getWorld(), chunk.xPosition, chunk.zPosition);
	}

	/**
	 * Gets the list of players currently watching the chunk at the coordinate.
	 *
	 * @param world the world
	 * @param x the x
	 * @param z the z
	 * @return the players watching chunk
	 */
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

	public static void addDestroyEffects(World world, BlockPos pos, EffectRenderer effectRenderer, IBlockState... states)
	{
		if (ArrayUtils.isEmpty(states))
			states = new IBlockState[] { world.getBlockState(pos) };

		byte nb = 4;
		Factory factory = new EntityDiggingFX.Factory();

		for (int i = 0; i < nb; ++i)
		{
			for (int j = 0; j < nb; ++j)
			{
				for (int k = 0; k < nb; ++k)
				{
					double fxX = pos.getX() + (i + 0.5D) / nb;
					double fxY = pos.getY() + (j + 0.5D) / nb;
					double fxZ = pos.getZ() + (k + 0.5D) / nb;

					int id = Block.getStateId(states[world.rand.nextInt(states.length)]);

					EntityDiggingFX fx = (EntityDiggingFX) factory.getEntityFX(0, world, fxX, fxY, fxZ, fxX - pos.getX() - 0.5D,
							fxY - pos.getY() - 0.5D, fxZ - pos.getZ() - 0.5D, id);
					effectRenderer.addEffect(fx);
				}
			}
		}
	}

	public static void addHitEffects(World world, MovingObjectPosition target, EffectRenderer effectRenderer, IBlockState... states)
	{
		if (ArrayUtils.isEmpty(states))
			states = new IBlockState[] { world.getBlockState(target.getBlockPos()) };

		Block block = world.getBlockState(target.getBlockPos()).getBlock();

		double fxX = target.getBlockPos().getX() + world.rand.nextDouble();
		double fxY = target.getBlockPos().getY() + world.rand.nextDouble();
		double fxZ = target.getBlockPos().getZ() + world.rand.nextDouble();

		switch (target.sideHit)
		{
			case DOWN:
				fxY = target.getBlockPos().getY() + block.getBlockBoundsMinY() - 0.1F;
				break;
			case UP:
				fxY = target.getBlockPos().getY() + block.getBlockBoundsMaxY() + 0.1F;
				break;
			case NORTH:
				fxZ = target.getBlockPos().getZ() + block.getBlockBoundsMinZ() - 0.1F;
				break;
			case SOUTH:
				fxZ = target.getBlockPos().getZ() + block.getBlockBoundsMaxY() + 0.1F;
				break;
			case EAST:
				fxX = target.getBlockPos().getX() + block.getBlockBoundsMaxX() + 0.1F;
				break;
			case WEST:
				fxX = target.getBlockPos().getX() + block.getBlockBoundsMinX() + 0.1F;
				break;
			default:
				break;
		}

		int id = Block.getStateId(states[world.rand.nextInt(states.length)]);

		Factory factory = new EntityDiggingFX.Factory();
		EntityDiggingFX fx = (EntityDiggingFX) factory.getEntityFX(0, world, fxX, fxY, fxZ, 0, 0, 0, id);
		fx.multiplyVelocity(0.2F).multipleParticleScaleBy(0.6F);
		effectRenderer.addEffect(fx);
	}
}
