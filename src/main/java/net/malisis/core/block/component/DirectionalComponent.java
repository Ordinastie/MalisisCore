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

package net.malisis.core.block.component;

import net.malisis.core.block.IBlockComponent;
import net.malisis.core.block.MalisisBlock;
import net.malisis.core.util.EntityUtils;
import net.malisis.core.util.EnumFacingUtils;
import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * A DirectionalComponent, when added to a {@link MalisisBlock} allows the block to automatically have an orientation.<br>
 * The state is handled automatically when the block is placed, as well as the {@link IBlockState}<->metadata conversion.
 * {@link #HORIZONTAL} and {@link #ALL} properties are available by default, but the component can be used with any
 * {@link PropertyDirection}.
 *
 * @author Ordinastie
 */
public class DirectionalComponent implements IBlockComponent
{
	public static final PropertyDirection HORIZONTAL = PropertyDirection.create("direction", EnumFacing.Plane.HORIZONTAL);
	public static final PropertyDirection ALL = PropertyDirection.create("direction");

	/** The property used for this {@link DirectionalComponent}. */
	private PropertyDirection property = HORIZONTAL;
	/** Whether the placement is based on the side the player clicked. */
	private boolean placedOnSide = false;
	/** Whether the neigbor determines the direction of the block placed. */
	private boolean mimicNeighbor = false;

	/**
	 * Instantiates a new {@link DirectionalComponent} with {@link #HORIZONTAL} property by default.
	 */
	public DirectionalComponent()
	{}

	/**
	 * Instantiates a new {@link DirectionalComponent} with specified property.
	 *
	 * @param property the property
	 */
	public DirectionalComponent(PropertyDirection property)
	{
		this.property = property;
	}

	/**
	 * Sets the behavior of the placement.<br>
	 * The direction of the block placed will be determined by the side clicked on when placing the block.
	 *
	 * @param placedOnSide the placed on side
	 * @return the directional component
	 */
	public DirectionalComponent setPlacedOnSide(boolean placedOnSide)
	{
		this.placedOnSide = placedOnSide;
		return this;
	}

	/**
	 * Sets the behavior of the placement.<br>
	 * The direction of the block will be determined by the block it's placed against, granted it's of the same type.<br>
	 * Overrides {@link #setPlacedOnSide(boolean)}.
	 *
	 * @param mimicNeighbor the mimic neighbor
	 * @return the directional component
	 */
	public DirectionalComponent mimicNeighbor(boolean mimicNeighbor)
	{
		this.mimicNeighbor = mimicNeighbor;
		return this;
	}

	/**
	 * Gets the property direction to use for this {@link DirectionalComponent}.
	 *
	 * @return the property direction
	 */
	@Override
	public PropertyDirection getProperty()
	{
		return property;
	}

	/**
	 * Sets the default value to use for the {@link IBlockState}.
	 *
	 * @param block the block
	 * @param state the state
	 * @return the i block state
	 */
	@Override
	public IBlockState setDefaultState(Block block, IBlockState state)
	{
		return state.withProperty(getProperty(), EnumFacing.SOUTH);
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
	@Override
	public IBlockState onBlockPlaced(Block block, World world, BlockPos pos, IBlockState state, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
	{
		EnumFacing direction = facing;
		if (!placedOnSide)
			direction = EntityUtils.getEntityFacing(placer, getProperty() == ALL).getOpposite();
		if (mimicNeighbor)
		{
			IBlockState neighbor = world.getBlockState(pos.offset(facing.getOpposite()));
			DirectionalComponent dc = IBlockComponent.getComponent(DirectionalComponent.class, neighbor.getBlock());
			if (neighbor.getBlock() == block && dc != null)
				direction = DirectionalComponent.getDirection(neighbor);
		}

		return state.withProperty(getProperty(), direction);
	}

	/**
	 * Gets the {@link IBlockState} from <code>meta</code>.
	 *
	 * @param block the block
	 * @param meta the meta
	 * @return the state from meta
	 */
	@Override
	public IBlockState getStateFromMeta(Block block, IBlockState state, int meta)
	{
		EnumFacing facing = null;
		if (getProperty() == HORIZONTAL)
			facing = EnumFacing.getHorizontal(meta & 3);
		else
			facing = EnumFacing.getFront(meta & 7);
		return state.withProperty(getProperty(), facing);
	}

	/**
	 * Gets the metadata from the {@link IBlockState}.
	 *
	 * @param block the block
	 * @param state the state
	 * @return the meta from state
	 */
	@Override
	public int getMetaFromState(Block block, IBlockState state)
	{
		if (getProperty() == HORIZONTAL)
			return getDirection(state).getHorizontalIndex();
		else
			return getDirection(state).getIndex();
	}

	/**
	 * Gets the {@link EnumFacing direction} for the {@link Block} at world coords.
	 *
	 * @param world the world
	 * @param pos the pos
	 * @return the EnumFacing, null if the block is not {@link DirectionalComponent}
	 */
	public static EnumFacing getDirection(IBlockAccess world, BlockPos pos)
	{
		return world != null && pos != null ? getDirection(world.getBlockState(pos)) : EnumFacing.SOUTH;
	}

	/**
	 * Gets the {@link EnumFacing direction} for the {@link IBlockState}
	 *
	 * @param state the state
	 * @return the EnumFacing, null if the block is not {@link DirectionalComponent}
	 */
	public static EnumFacing getDirection(IBlockState state)
	{
		DirectionalComponent dc = IBlockComponent.getComponent(DirectionalComponent.class, state.getBlock());
		if (dc == null)
			return EnumFacing.SOUTH;

		PropertyDirection property = dc.getProperty();
		if (property == null || !state.getProperties().containsKey(property))
			return EnumFacing.SOUTH;

		return (EnumFacing) state.getValue(property);
	}

	/**
	 * Rotates the {@link IBlockState} by 90 degrees counter-clockwise.
	 *
	 * @param world the world
	 * @param pos the pos
	 * @param state the state
	 * @return the i block state
	 */
	public static IBlockState rotate(IBlockState state)
	{
		return rotate(state, 1);
	}

	/**
	 * Rotates the {@link IBlockState} by a factor of 90 degrees counter-clockwise.
	 *
	 * @param state the state
	 * @param angle the angle
	 * @return the i block state
	 */
	public static IBlockState rotate(IBlockState state, int angle)
	{
		int a = -angle & 3;
		if (a == 0)
			return state;

		DirectionalComponent dc = IBlockComponent.getComponent(DirectionalComponent.class, state.getBlock());
		if (dc == null)
			return state;

		PropertyDirection property = dc.getProperty();
		if (property == null || !state.getProperties().containsKey(property))
			return state;

		return state.withProperty(property, EnumFacingUtils.rotateFacing((EnumFacing) state.getValue(property), a));
	}

}
