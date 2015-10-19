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

package net.malisis.core.util.chunkcollision;

import net.malisis.core.block.BoundingBoxType;
import net.malisis.core.block.IBlockDirectional;
import net.malisis.core.block.IBoundingBox;
import net.malisis.core.util.AABBUtils;
import net.malisis.core.util.chunkblock.IChunkBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;

/**
 * This interface defines a block that will have wider bounding boxes that extend beyond they 1x1x1 block space.
 *
 * @author Ordinastie
 */
public interface IChunkCollidable extends IChunkBlock, IBoundingBox
{

	/**
	 * Gets the bounding box used to determine if the block has enough room to be placed at the position.
	 *
	 * @param world the world
	 * @param pos the pos
	 * @param side the side
	 * @param entity the entity
	 * @param itemStack the item stack
	 * @return the placed bounding box
	 */
	public default AxisAlignedBB[] getPlacedBoundingBox(IBlockAccess world, BlockPos pos, EnumFacing side, EntityPlayer entity, ItemStack itemStack)
	{
		AxisAlignedBB[] aabbs = getBoundingBoxes(world, pos, BoundingBoxType.PLACEDBOUNDINGBOX);
		if (this instanceof IBlockDirectional)
			aabbs = AABBUtils.rotate(aabbs, ((IBlockDirectional) this).getPlacingDirection(side, entity));

		return aabbs;
	}
}
