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

package net.malisis.core.util.multiblock;

import java.util.List;

import net.malisis.core.block.IBlockComponent;
import net.malisis.core.block.IComponent;
import net.malisis.core.block.component.DirectionalComponent;
import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.google.common.collect.Lists;

/**
 * @author Ordinastie
 *
 */
public class MultiBlockComponent implements IBlockComponent
{
	public static PropertyBool ORIGIN = PropertyBool.create("origin");

	private MultiBlock multiBlock;
	private IMultiBlockProvider provider;

	public MultiBlockComponent(MultiBlock multiBlock)
	{
		this.multiBlock = multiBlock;
	}

	public MultiBlockComponent(IMultiBlockProvider provider)
	{
		this.provider = provider;
	}

	public MultiBlock getMultiBlock()
	{
		return multiBlock;
	}

	public IMultiBlockProvider getProvider()
	{
		return provider;
	}

	@Override
	public PropertyBool getProperty()
	{
		return ORIGIN;
	}

	@Override
	public IBlockState setDefaultState(Block block, IBlockState state)
	{
		return state.withProperty(getProperty(), false);
	}

	@Override
	public List<IComponent> getDependencies()
	{
		return Lists.newArrayList(new DirectionalComponent());
	}

	@Override
	public IBlockState onBlockPlaced(Block block, World world, BlockPos pos, IBlockState state, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
	{
		return state.withProperty(getProperty(), true);
	}

	@Override
	public void onBlockPlacedBy(Block block, World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
	{
		MultiBlock multiBlock = getMultiBlock(world, pos, state, stack);
		if (multiBlock == null || !multiBlock.isBulkPlace())
			return;

		if (multiBlock.canPlaceBlockAt(world, pos, state, false))
			multiBlock.placeBlocks(world, pos, state, false);
		else
			world.setBlockToAir(pos);
	}

	@Override
	public void breakBlock(Block block, World world, BlockPos pos, IBlockState state)
	{
		MultiBlock multiBlock = getMultiBlock(world, pos, state, null);
		if (multiBlock != null && multiBlock.isBulkBreak())
			multiBlock.breakBlocks(world, pos, state);
	}

	@Override
	public IBlockState getStateFromMeta(Block block, IBlockState state, int meta)
	{
		return state.withProperty(getProperty(), (meta & 8) != 0);
	}

	@Override
	public int getMetaFromState(Block block, IBlockState state)
	{
		return (isOrigin(state) ? 8 : 0);
	}

	public static boolean isOrigin(World world, BlockPos pos)
	{
		return world != null && pos != null && isOrigin(world.getBlockState(pos));
	}

	public static boolean isOrigin(IBlockState state)
	{
		MultiBlockComponent mbc = IComponent.getComponent(MultiBlockComponent.class, state.getBlock());
		if (mbc == null)
			return false;

		PropertyBool property = mbc.getProperty();
		if (property == null || !state.getProperties().containsKey(property))
			return false;

		return state.getValue(property);
	}

	public static MultiBlock getMultiBlock(IBlockAccess world, BlockPos pos, IBlockState state, ItemStack itemStack)
	{
		MultiBlockComponent mbc = IComponent.getComponent(MultiBlockComponent.class, state.getBlock());
		if (mbc == null)
			return null;
		MultiBlock multiBlock = mbc.getMultiBlock();
		if (multiBlock != null)
			return multiBlock;

		IMultiBlockProvider provider = mbc.getProvider();
		return provider != null ? provider.getMultiBlock(world, pos, state, itemStack) : null;

	}

	public interface IMultiBlockProvider
	{
		public MultiBlock getMultiBlock(IBlockAccess world, BlockPos pos, IBlockState state, ItemStack itemStack);
	}
}
