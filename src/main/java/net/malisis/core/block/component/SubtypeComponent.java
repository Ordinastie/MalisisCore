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
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;

/**
 * The {@link SubtypeComponent} automatically handles multiple blocks variants and its different items based on a specific Enum.
 *
 * @author Ordinastie
 * @param <T> the generic type
 */
public class SubtypeComponent<T extends Enum<T> & IStringSerializable> implements IBlockComponent
{
	/** The class of the Enum used. */
	private Class<T> enumClass;
	/** The {@link PropertyEnum} for this component. */
	private PropertyEnum<T> property;

	/**
	 * Instantiates a new {@link SubtypeComponent}.
	 *
	 * @param clazz the clazz
	 * @param property the property
	 */
	public SubtypeComponent(Class<T> clazz, PropertyEnum<T> property)
	{
		this.enumClass = clazz;
		this.property = property;

		if (clazz.getEnumConstants().length > 16)
			throw new IllegalArgumentException("Cannot save all the state derived from " + clazz.getSimpleName() + " in 4 bits metadata");
	}

	/**
	 * Instantiates a new {@link SubtypeComponent}.
	 *
	 * @param clazz the clazz
	 */
	public SubtypeComponent(Class<T> clazz)
	{
		this(clazz, PropertyEnum.create(clazz.getSimpleName().toLowerCase(), clazz));
	}

	@Override
	public String getUnlocalizedName(Block block, IBlockState state)
	{
		return block.getUnlocalizedName() + "." + state.getValue(getProperty()).getName();
	}

	@Override
	public boolean getHasSubtypes(Block block, Item item)
	{
		return true;
	}

	@Override
	public PropertyEnum<T> getProperty()
	{
		return property;
	}

	@Override
	public IBlockState setDefaultState(Block block, IBlockState state)
	{
		return state.withProperty(getProperty(), enumClass.getEnumConstants()[0]);
	}

	@Override
	public void getSubBlocks(Block block, Item item, CreativeTabs tab, NonNullList<ItemStack> list)
	{
		for (int i = 0; i < enumClass.getEnumConstants().length; i++)
			list.add(new ItemStack(item, 1, i));
	}

	@Override
	public IBlockState getStateFromMeta(Block block, IBlockState state, int meta)
	{
		return state.withProperty(getProperty(), enumClass.getEnumConstants()[meta]);
	}

	@Override
	public int getMetaFromState(Block block, IBlockState state)
	{
		return state.getValue(getProperty()).ordinal();
	}
}
