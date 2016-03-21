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
import net.malisis.core.block.IMergedBlock;
import net.malisis.core.block.ISmartCull;
import net.malisis.core.block.MalisisBlock;
import net.malisis.core.item.MalisisItemBlock;
import net.malisis.core.util.AABBUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockSlab.EnumBlockHalf;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
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
	private MalisisBlock singleSlab;
	private MalisisBlock doubleSlab;

	public SlabComponent(MalisisBlock singleSlab, MalisisBlock doubleSlab)
	{
		this.singleSlab = singleSlab;
		this.doubleSlab = doubleSlab;

		singleSlab.addComponent(this);
		doubleSlab.addComponent(this);
		doubleSlab.setName(singleSlab.getName() + "Double");
	}

	public boolean isDouble(Block block)
	{
		return block == doubleSlab;
	}

	public void register()
	{
		singleSlab.register();
		doubleSlab.register();

		//	GameData.getBlockItemMap().put(doubleSlab, Item.getItemFromBlock(singleSlab));
	}

	@Override
	public PropertyEnum<EnumBlockHalf> getProperty()
	{
		return BlockSlab.HALF;
	}

	@Override
	public IBlockState setDefaultState(Block block, IBlockState state)
	{
		return state.withProperty(getProperty(), EnumBlockHalf.BOTTOM);
	}

	@Override
	public Item getItem(Block block)
	{
		if (isDouble(block))
			return null;
		return new MalisisItemBlock(singleSlab);
	}

	@Override
	public boolean canMerge(ItemStack itemStack, EntityPlayer player, World world, BlockPos pos, EnumFacing side)
	{
		return world.getBlockState(pos).getBlock() == singleSlab;
	}

	@Override
	public IBlockState mergeBlock(World world, BlockPos pos, IBlockState state, ItemStack itemStack, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		int x = (int) Math.floor(hitX + side.getFrontOffsetX() * 0.4F);
		int y = (int) Math.floor(hitY + side.getFrontOffsetY() * 0.4F);
		int z = (int) Math.floor(hitZ + side.getFrontOffsetZ() * 0.4F);
		BlockPos hitPos = new BlockPos(pos).add(x, y, z);
		return hitPos.equals(pos) ? doubleSlab.getDefaultState() : null;
	}

	@Override
	public IBlockState onBlockPlaced(Block block, World world, BlockPos pos, IBlockState state, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
	{
		if (isDouble(block))
			return state;

		return state.withProperty(getProperty(),
				facing != EnumFacing.DOWN && (facing == EnumFacing.UP || hitY <= 0.5F) ? EnumBlockHalf.BOTTOM : EnumBlockHalf.TOP);
	}

	@Override
	public AxisAlignedBB getBoundingBox(Block block, IBlockAccess world, BlockPos pos, BoundingBoxType type)
	{
		if (isDouble(block))
			return AABBUtils.identity();

		AxisAlignedBB aabb = new AxisAlignedBB(0, 0, 0, 1, 0.5F, 1);
		if (isTop(world, pos))
			aabb = aabb.offset(0, 0.5F, 0);
		return aabb;
	}

	@Override
	public IBlockState getStateFromMeta(Block block, IBlockState state, int meta)
	{
		return state.withProperty(getProperty(), (meta & 8) != 0 ? EnumBlockHalf.TOP : EnumBlockHalf.BOTTOM);
	}

	@Override
	public int getMetaFromState(Block block, IBlockState state)
	{
		return isTop(state) ? 8 : 0;
	}

	@Override
	public Boolean isFullCube(Block block, IBlockState state)
	{
		return false;//isDouble(block);
	}

	@Override
	public Boolean isFullBlock(Block block, IBlockState state)
	{
		return false;// isDouble(block);
	}

	@Override
	public Boolean isOpaqueCube(Block block, IBlockState state)
	{
		return false;//isDouble(block);
	}

	@Override
	public Integer getPackedLightmapCoords(Block block, IBlockAccess world, BlockPos pos, IBlockState state)
	{
		return null;
	}

	@Override
	public Item getItemDropped(Block block, IBlockState state, Random rand, int fortune)
	{
		return Item.getItemFromBlock(singleSlab);
	}

	@Override
	public Integer quantityDropped(Block block, IBlockState state, int fortune, Random random)
	{
		return isDouble(block) ? 2 : 1;
	}

	@Override
	public Integer getLightOpacity(Block block, IBlockAccess world, BlockPos pos, IBlockState state)
	{
		return isDouble(block) ? 255 : 0;
	}

	public static boolean isTop(IBlockAccess world, BlockPos pos)
	{
		return world != null ? isTop(world.getBlockState(pos)) : false;
	}

	public static boolean isTop(IBlockState state)
	{
		SlabComponent sc = IBlockComponent.getComponent(SlabComponent.class, state.getBlock());
		if (sc == null)
			return false;

		PropertyEnum<EnumBlockHalf> property = sc.getProperty();
		if (property == null || !state.getProperties().containsKey(property))
			return false;

		return state.getValue(property) == EnumBlockHalf.TOP;
	}

	public static boolean isDoubleSlab(IBlockAccess world, BlockPos pos)
	{
		return isDoubleSlab(world.getBlockState(pos).getBlock());
	}

	public static boolean isDoubleSlab(Block block)
	{
		SlabComponent sc = IBlockComponent.getComponent(SlabComponent.class, block);
		return sc != null && sc.isDouble(block);
	}

	public static boolean isSlab(IBlockAccess world, BlockPos pos)
	{
		return isSlab(world.getBlockState(pos).getBlock());
	}

	public static boolean isSlab(Block block)
	{
		if (block == Blocks.stone_slab || block == Blocks.wooden_slab || block == Blocks.stone_slab2)
			return true;

		return IBlockComponent.getComponent(SlabComponent.class, block) != null;
	}
}
