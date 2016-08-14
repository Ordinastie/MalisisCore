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
import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * @author Ordinastie
 *
 */
public class BooleanComponent implements IBlockComponent
{
	/** Name of the {@link PropertyBool} used. */
	private String name;
	/** ProperyBool used by this {@link BooleanComponent}. */
	private PropertyBool property;
	/** Default value for the property for the default state of the block. */
	private boolean defaultValue;
	/** Bit offset when writing/reading the metadata. */
	private int metaOffset = 0;

	/**
	 * Instantiates a new {@link BooleanComponent}.
	 *
	 * @param name the name
	 * @param defaultValue the default value
	 * @param metaOffset the meta offset
	 */
	public BooleanComponent(String name, boolean defaultValue, int metaOffset)
	{
		this.property = PropertyBool.create(name);
		this.defaultValue = defaultValue;
		this.metaOffset = metaOffset;
	}

	/**
	 * Instantiates a new {@link BooleanComponent}.
	 *
	 * @param name the name
	 * @param defaultValue the default value
	 */
	public BooleanComponent(String name, boolean defaultValue)
	{
		this(name, defaultValue, 0);
	}

	/**
	 * Instantiates a new {@link BooleanComponent}.
	 *
	 * @param name the name
	 */
	public BooleanComponent(String name)
	{
		this(name, false, 0);
	}

	/**
	 * Gets the name of the {@link PropertyBool} for this {@link BooleanComponent}.
	 *
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	@Override
	public PropertyBool getProperty()
	{
		return property;
	}

	@Override
	public IBlockState setDefaultState(Block block, IBlockState state)
	{
		return state.withProperty(getProperty(), defaultValue);
	}

	/**
	 * Gets the value of this {@link BooleanComponent} in the World.
	 *
	 * @param world the world
	 * @param pos the pos
	 * @return true, if successful
	 */
	public boolean get(IBlockAccess world, BlockPos pos)
	{
		return get(world.getBlockState(pos));
	}

	/**
	 * Gets the value of this {@link BooleanComponent} in the {@link IBlockState}.
	 *
	 * @param state the state
	 * @return true, if successful
	 */
	public boolean get(IBlockState state)
	{
		return state.getValue(getProperty());
	}

	/**
	 * Sets the {@link IBlockState} in the {@link World} with the specified value.
	 *
	 * @param world the world
	 * @param pos the pos
	 * @param value the value
	 * @return the resulting IBlockState
	 */
	public IBlockState set(World world, BlockPos pos, boolean value)
	{
		IBlockState state = world.getBlockState(pos);
		world.setBlockState(pos, set(state, value));
		return state;
	}

	/**
	 * Sets the {@link IBlockState} with the specified value.
	 *
	 * @param state the state
	 * @param value the value
	 * @return the i block state
	 */
	public IBlockState set(IBlockState state, boolean value)
	{
		return state.withProperty(getProperty(), value);
	}

	/**
	 * Inverts the {@link IBlockState} value in the {@link World}.
	 *
	 * @param world the world
	 * @param pos the pos
	 * @return the new value
	 */
	public boolean invert(World world, BlockPos pos)
	{
		boolean value = !get(world, pos);
		set(world, pos, value);
		return value;
	}

	/**
	 * Inverts the {@link IBlockState} value.
	 *
	 * @param state the state
	 * @return the new value
	 */
	public IBlockState invert(IBlockState state)
	{
		boolean value = !get(state);
		return set(state, value);
	}

	@Override
	public int getMetaFromState(Block block, IBlockState state)
	{
		return get(state) ? 1 << metaOffset : 0;
	}

	@Override
	public IBlockState getStateFromMeta(Block block, IBlockState state, int meta)
	{
		return set(state, ((meta >> metaOffset) & 1) != 0);
	}
}
