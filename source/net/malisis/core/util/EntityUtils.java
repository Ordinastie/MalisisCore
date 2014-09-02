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

import java.util.List;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class EntityUtils
{

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

	public static EntityPlayerMP findPlayerFromUUID(UUID uuid)
	{
		List<EntityPlayerMP> listPlayers = MinecraftServer.getServer().getConfigurationManager().playerEntityList;

		for (EntityPlayerMP player : listPlayers)
			if (player.getUniqueID().equals(uuid))
				return player;

		return null;
	}

	public static ForgeDirection getEntityFacing(Entity entity)
	{
		return getEntityFacing(entity, false);
	}

	public static ForgeDirection getEntityFacing(Entity entity, boolean sixWays)
	{
		float pitch = entity.rotationPitch;
		if (sixWays && pitch < -45)
			return ForgeDirection.UP;
		if (sixWays && pitch > 45)
			return ForgeDirection.DOWN;

		int facing = MathHelper.floor_double(entity.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
		ForgeDirection[] dirs = new ForgeDirection[] { ForgeDirection.SOUTH, ForgeDirection.WEST, ForgeDirection.NORTH, ForgeDirection.EAST };
		return dirs[facing];
	}

}
