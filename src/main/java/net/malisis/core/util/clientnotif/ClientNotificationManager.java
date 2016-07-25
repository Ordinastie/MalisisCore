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

import java.lang.reflect.Method;
import java.util.Set;

import net.malisis.core.MalisisCore;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import com.google.common.collect.Sets;

/**
 * @author Ordinastie
 *
 */
public class ClientNotificationManager
{
	private Set<Block> clientBlocks = Sets.newHashSet();

	private static ClientNotificationManager instance;

	private ClientNotificationManager()
	{

	}

	private void registerBlockNotif(Block block)
	{
		clientBlocks.add(block);
	}

	private boolean needsNotification(Block block)
	{
		return clientBlocks.contains(block);
	}

	public void notifyClientBlock(World world, BlockPos pos, Block neighbor)
	{
		if (world.isRemote)
			return;

		IBlockState state = world.getBlockState(pos);
		if (!needsNotification(state.getBlock()))
			return;

		NeighborChangedMessage.send(world, pos, neighbor);
	}

	public void discover(Block block)
	{
		try
		{
			Class<? extends Block> clazz = block.getClass();
			Method m = clazz.getMethod(MalisisCore.isObfEnv ? "func_189540_a" : "neighborChanged",
					IBlockState.class,
					World.class,
					BlockPos.class,
					Block.class);
			ClientNotification anno = m.getAnnotation(ClientNotification.class);
			if (anno != null)
				registerBlockNotif(block);
		}
		catch (ReflectiveOperationException e)
		{
			MalisisCore.log.error("Failed to find @ClientNotification annotation for {} : ", block, e);
		}

	}

	public static ClientNotificationManager get()
	{
		if (instance == null)
			instance = new ClientNotificationManager();
		return instance;
	}

	public static void notify(World world, BlockPos pos, Block neighbor)
	{
		get().notifyClientBlock(world, pos, neighbor);
	}

}
