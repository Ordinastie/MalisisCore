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

import net.malisis.core.block.IBoundingBox;
import net.malisis.core.client.gui.MalisisGui;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Utility class for {@link TileEntity}.
 *
 * @author Ordinastie
 */
public class TileEntityUtils
{
	/** Reference to the {@link TileEntity} currently being use for current opened {@link MalisisGui}. */
	@SideOnly(Side.CLIENT)
	private static TileEntity currentTileEntity;

	/** Reference the to currently opened {@link MalisisGui}. */
	@SideOnly(Side.CLIENT)
	private static MalisisGui currenGui;

	/**
	 * Gets the {@link TileEntity} of type <b>T</b> at the specified {@link BlockPos}.<br>
	 * If no <code>TileEntity</code> was found at the coordinates, or if the <code>TileEntity</code> is not of type <b>T</b>, returns
	 * <code>null</code> instead.
	 *
	 * @param <T> type of TileEntity requested
	 * @param clazz the class of the TileEntity
	 * @param world the world
	 * @param pos the pos
	 * @return the tile entity at the coordinates, or null if no tile entity, or not of type T
	 */
	public static <T> T getTileEntity(Class<T> clazz, IBlockAccess world, BlockPos pos)
	{
		if (world == null)
			return null;

		TileEntity te = world.getTileEntity(pos);
		return te != null ? Silenced.get(() -> clazz.cast(te)) : null;
	}

	/**
	 * Links the {@link TileEntity} to the {@link MalisisGui}.<br>
	 * Allows the TileEntity to notify the MalisisGui of updates.
	 *
	 * @param te the TileEntity
	 * @param gui the MalisisGui
	 */
	@SideOnly(Side.CLIENT)
	public static void linkTileEntityToGui(TileEntity te, MalisisGui gui)
	{
		currentTileEntity = te;
		currenGui = gui;
		currenGui.updateGui();
	}

	/**
	 * Notifies the currently opened {@link MalisisGui} to update.
	 *
	 * @param te the {@link TileEntity} linked to the MalisisGui
	 */
	@SideOnly(Side.CLIENT)
	public static void updateGui(TileEntity te)
	{
		if (te != currentTileEntity)
			return;
		currenGui.updateGui();
	}

	public static AxisAlignedBB getRenderingBounds(TileEntity tileEntity)
	{
		Block block = tileEntity.getBlockType();
		BlockPos pos = tileEntity.getPos();
		World world = tileEntity.getWorld();
		AxisAlignedBB aabb = null;
		if (block instanceof IBoundingBox)
			aabb = AABBUtils.offset(pos, ((IBoundingBox) block).getRenderBoundingBox(world, pos, world.getBlockState(pos))[0]);

		return aabb != null ? aabb : AABBUtils.identity(pos);

	}
}
