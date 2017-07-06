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

import java.util.Random;

import net.malisis.core.block.BoundingBoxType;
import net.malisis.core.block.IBlockComponent;
import net.malisis.core.block.IComponent;
import net.malisis.core.block.IMergedBlock;
import net.malisis.core.block.ISmartCull;
import net.malisis.core.util.AABBUtils;
import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * @author Ordinastie
 *
 */
public class SlabComponent implements IBlockComponent, IMergedBlock, ISmartCull
{
	public PropertyBool BOTTOM = PropertyBool.create("bottom");
	public PropertyBool TOP = PropertyBool.create("top");

	private static final AxisAlignedBB BOTTOM_AABB = new AxisAlignedBB(0, 0, 0, 1, 0.5F, 1);
	private static final AxisAlignedBB TOP_AABB = new AxisAlignedBB(0, 0.5F, 0, 1, 1, 1);

	@Override
	public PropertyBool[] getProperties()
	{
		return new PropertyBool[] { TOP, BOTTOM };
	}

	public PropertyBool getTopProperty()
	{
		return TOP;

	}

	public PropertyBool getBottomProperty()
	{
		return BOTTOM;
	}

	@Override
	public IBlockState setDefaultState(Block block, IBlockState state)
	{
		return state.withProperty(getBottomProperty(), true).withProperty(getTopProperty(), false);
	}

	@Override
	public boolean canMerge(ItemStack itemStack, EntityPlayer player, World world, BlockPos pos, EnumFacing side)
	{
		return !isDoubleSlab(world, pos);
	}

	@Override
	public IBlockState mergeBlock(World world, BlockPos pos, IBlockState state, ItemStack itemStack, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		int x = (int) Math.floor(hitX + side.getFrontOffsetX() * 0.4F);
		int y = (int) Math.floor(hitY + side.getFrontOffsetY() * 0.4F);
		int z = (int) Math.floor(hitZ + side.getFrontOffsetZ() * 0.4F);
		BlockPos hitPos = new BlockPos(pos).add(x, y, z);
		return hitPos.equals(pos) ? state.withProperty(TOP, true).withProperty(BOTTOM, true) : null;
	}

	@Override
	public IBlockState getStateForPlacement(Block block, World world, BlockPos pos, IBlockState state, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand)
	{
		boolean bottom = facing != EnumFacing.DOWN && (facing == EnumFacing.UP || hitY <= 0.5F);
		return state.withProperty(getBottomProperty(), bottom).withProperty(getTopProperty(), !bottom);
	}

	@Override
	public AxisAlignedBB getBoundingBox(Block block, IBlockAccess world, BlockPos pos, IBlockState state, BoundingBoxType type)
	{
		if (isDoubleSlab(state))
			return AABBUtils.identity();

		return state.getValue(getBottomProperty()) ? BOTTOM_AABB : TOP_AABB;
	}

	@Override
	public IBlockState getStateFromMeta(Block block, IBlockState state, int meta)
	{
		return state.withProperty(getBottomProperty(), (meta & 1) != 0).withProperty(getTopProperty(), (meta & 2) != 0);
	}

	@Override
	public int getMetaFromState(Block block, IBlockState state)
	{
		return (state.getValue(getBottomProperty()) ? 1 : 0) + (state.getValue(getTopProperty()) ? 2 : 0);
	}

	@Override
	public Boolean isFullCube(Block block, IBlockState state)
	{
		return isDoubleSlab(state);
	}

	@Override
	public Boolean isFullBlock(Block block, IBlockState state)
	{
		return isDoubleSlab(state);
	}

	@Override
	public Boolean isOpaqueCube(Block block, IBlockState state)
	{
		return isDoubleSlab(state);
	}

	@Override
	public Integer getPackedLightmapCoords(Block block, IBlockAccess world, BlockPos pos, IBlockState state)
	{
		return null;
	}

	@Override
	public Integer quantityDropped(Block block, IBlockState state, int fortune, Random random)
	{
		return isDoubleSlab(state) ? 2 : 1;
	}

	@Override
	public int damageDropped(Block block, IBlockState state)
	{
		return 1;
	}

	@Override
	public Integer getLightOpacity(Block block, IBlockAccess world, BlockPos pos, IBlockState state)
	{
		return isDoubleSlab(world, pos) ? 255 : 0;
	}

	public static boolean isDoubleSlab(IBlockAccess world, BlockPos pos)
	{
		return isDoubleSlab(world.getBlockState(pos));
	}

	public static boolean isDoubleSlab(IBlockState state)
	{
		SlabComponent sc = IComponent.getComponent(SlabComponent.class, state.getBlock());
		if (sc == null)
			return false;

		return state.getValue(sc.getTopProperty()) && state.getValue(sc.getBottomProperty());
	}

	public static boolean isSlab(IBlockAccess world, BlockPos pos)
	{
		return isSlab(world.getBlockState(pos).getBlock());
	}

	public static boolean isSlab(Block block)
	{
		if (block == Blocks.STONE_SLAB || block == Blocks.STONE_SLAB2 || block == Blocks.WOODEN_SLAB)
			return true;

		return IComponent.getComponent(SlabComponent.class, block) != null;
	}
}
