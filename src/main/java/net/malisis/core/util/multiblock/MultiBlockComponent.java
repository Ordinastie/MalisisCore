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

import net.malisis.core.block.IBlockComponent;
import net.malisis.core.block.component.DirectionalComponent;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * @author Ordinastie
 *
 */
public class MultiBlockComponent extends DirectionalComponent
{
	public static PropertyBool ORIGIN = PropertyBool.create("origin");

	private MultiBlock multiBlock;

	public MultiBlockComponent(MultiBlock multiBlock)
	{
		this.multiBlock = multiBlock;
	}

	public MultiBlock getMultiBlock(IBlockAccess world, BlockPos pos, IBlockState state, ItemStack itemStack)
	{
		return multiBlock;
	}

	public PropertyBool getOriginProperty()
	{
		return ORIGIN;
	}

	public PropertyDirection getDirectionProperty()
	{
		return HORIZONTAL;
	}

	@Override
	public IProperty[] getProperties()
	{
		return new IProperty[] { HORIZONTAL, ORIGIN };
	}

	@Override
	public IBlockState setDefaultState(Block block, IBlockState state)
	{
		return super.setDefaultState(block, state).withProperty(ORIGIN, false);
	}

	@Override
	public IBlockState onBlockPlaced(Block block, World world, BlockPos pos, IBlockState state, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
	{
		state = super.onBlockPlaced(block, world, pos, state, facing, hitX, hitY, hitZ, meta, placer);
		return state.withProperty(getOriginProperty(), true);
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
		state = super.getStateFromMeta(block, state, meta);
		return state.withProperty(getOriginProperty(), (meta & 8) != 0);
	}

	@Override
	public int getMetaFromState(Block block, IBlockState state)
	{
		int meta = super.getMetaFromState(block, state);
		return meta + (isOrigin(state) ? 8 : 0);
	}

	public static boolean isOrigin(World world, BlockPos pos)
	{
		return world != null && pos != null && isOrigin(world.getBlockState(pos));
	}

	public static boolean isOrigin(IBlockState state)
	{
		MultiBlockComponent mbc = IBlockComponent.getComponent(MultiBlockComponent.class, state.getBlock());
		if (mbc == null)
			return false;

		PropertyBool property = mbc.getOriginProperty();
		if (property == null || !state.getProperties().containsKey(property))
			return false;

		return (boolean) state.getValue(property);
	}
}
