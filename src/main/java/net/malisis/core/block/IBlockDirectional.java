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

package net.malisis.core.block;

import net.malisis.core.util.EntityUtils;
import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * The Interface IBlockDirectional gives default implementations for blocks that have a facing.<br>
 * If the block extends {@link MalisisBlock}, relevant methods automatically redirect the implement to this interface.
 *
 * @author Ordinastie
 */
public interface IBlockDirectional
{
	/** The {@link PropertyDirection} direction that is used by default. */
	public static final PropertyDirection HORIZONTAL = PropertyDirection.create("direction", EnumFacing.Plane.HORIZONTAL);
	public static final PropertyDirection ALL = PropertyDirection.create("direction");

	/**
	 * Gets the property direction to use for this {@link IBlockDirectional}.
	 *
	 * @return the property direction
	 */
	public default PropertyDirection getPropertyDirection()
	{
		return HORIZONTAL;
	}

	/**
	 * Gets the direction to use when placing this {@link IBlockDirectional}.
	 *
	 * @param side the side
	 * @param placer the placer
	 * @return the placing direction
	 */
	public default EnumFacing getPlacingDirection(EnumFacing side, EntityLivingBase placer)
	{
		return EntityUtils.getEntityFacing(placer, getPropertyDirection() == ALL).getOpposite();
	}

	/**
	 * Automatically gets the right {@link IBlockState} based on the <code>placer</code> facing.
	 *
	 * @param block the block
	 * @param world the world
	 * @param pos the pos
	 * @param facing the facing
	 * @param hitX the hit x
	 * @param hitY the hit y
	 * @param hitZ the hit z
	 * @param meta the meta
	 * @param placer the placer
	 * @return the i block state
	 */
	public default IBlockState onBlockPlaced(Block block, World world, BlockPos pos, IBlockState state, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
	{
		return state.withProperty(getPropertyDirection(), getPlacingDirection(facing, placer));
	}

	/**
	 * Gets the {@link IBlockState} from <code>meta</code>.
	 *
	 * @param block the block
	 * @param meta the meta
	 * @return the state from meta
	 */
	public default IBlockState getStateFromMeta(Block block, IBlockState state, int meta)
	{
		EnumFacing facing = null;
		if (getPropertyDirection() == HORIZONTAL)
			facing = EnumFacing.getHorizontal(meta & 3);
		else
			facing = EnumFacing.getFront(meta & 7);
		return state.withProperty(getPropertyDirection(), facing);
	}

	/**
	 * Gets the metadata from the {@link IBlockState}.
	 *
	 * @param block the block
	 * @param state the state
	 * @return the meta from state
	 */
	public default int getMetaFromState(Block block, IBlockState state)
	{
		if (getPropertyDirection() == HORIZONTAL)
			return getDirection(state).getHorizontalIndex();
		else
			return getDirection(state).getIndex();
	}

	/**
	 * Gets the {@link EnumFacing direction} for the {@link Block} at world coords.
	 *
	 * @param world the world
	 * @param pos the pos
	 * @return the EnumFacing, null if the block is not {@link IBlockDirectional}
	 */
	public static EnumFacing getDirection(IBlockAccess world, BlockPos pos)
	{
		return world != null && pos != null ? getDirection(world.getBlockState(pos)) : EnumFacing.SOUTH;
	}

	/**
	 * Gets the {@link EnumFacing direction} for the {@link IBlockState}
	 *
	 * @param state the state
	 * @return the EnumFacing, null if the block is not {@link IBlockDirectional}
	 */
	public static EnumFacing getDirection(IBlockState state)
	{
		if (!(state.getBlock() instanceof IBlockDirectional))
			return EnumFacing.SOUTH;

		PropertyDirection property = ((IBlockDirectional) state.getBlock()).getPropertyDirection();
		if (property == null || !state.getProperties().containsKey(property))
			return EnumFacing.SOUTH;

		return (EnumFacing) state.getValue(property);
	}
}
