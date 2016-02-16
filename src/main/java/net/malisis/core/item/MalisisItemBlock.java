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

package net.malisis.core.item;

import net.malisis.core.block.IBlockComponent;
import net.malisis.core.block.IMergedBlock;
import net.malisis.core.block.IRegisterable;
import net.malisis.core.block.MalisisBlock;
import net.malisis.core.renderer.icon.IIconProvider;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

/**
 * @author Ordinastie
 *
 */
public class MalisisItemBlock extends ItemBlock implements IRegisterable
{
	protected String name;
	protected IIconProvider iconProvider;

	public MalisisItemBlock(Block block)
	{
		super(block);
	}

	public MalisisItemBlock setName(String name)
	{
		this.name = name;
		setRegistryName(name);
		setUnlocalizedName(name);
		return this;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public String getUnlocalizedName(ItemStack itemStack)
	{
		if (block instanceof MalisisBlock)
		{
			IBlockState state = ((MalisisBlock) block).getStateFromItemStack(itemStack);
			return ((MalisisBlock) block).getUnlocalizedName(state);
		}

		return super.getUnlocalizedName(itemStack);
	}

	@Override
	public int getMetadata(int damage)
	{
		return damage;
	}

	@Override
	public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if (itemStack.stackSize == 0)
			return false;
		if (!player.canPlayerEdit(pos.offset(side), side, itemStack))
			return false;

		IBlockState placedState = checkMerge(itemStack, player, world, pos, side, hitX, hitY, hitZ, false);
		BlockPos p = pos;
		if (placedState == null)
		{
			p = pos.offset(side);
			float x = hitX - side.getFrontOffsetX();
			float y = hitY - side.getFrontOffsetY();
			float z = hitZ - side.getFrontOffsetZ();
			placedState = checkMerge(itemStack, player, world, p, side, x, y, z, true);
		}

		if (placedState == null)
			return super.onItemUse(itemStack, player, world, pos, side, hitX, hitY, hitZ);

		Block block = placedState.getBlock();
		if (world.checkNoEntityCollision(block.getCollisionBoundingBox(world, p, placedState)) && world.setBlockState(p, placedState, 3))
		{
			world.playSoundEffect(p.getX() + 0.5F, p.getY() + 0.5F, p.getZ() + 0.5F, block.stepSound.getPlaceSound(),
					(block.stepSound.getVolume() + 1.0F) / 2.0F, block.stepSound.getFrequency() * 0.8F);
			--itemStack.stackSize;
		}

		return true;
	}

	@Override
	public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side, EntityPlayer player, ItemStack itemStack)
	{
		IMergedBlock mergedBlock = getMerged(world.getBlockState(pos));
		if (mergedBlock != null && mergedBlock.canMerge(itemStack, player, world, pos, side))
			return true;

		pos = pos.offset(side);
		mergedBlock = getMerged(world.getBlockState(pos));
		if (mergedBlock != null && mergedBlock.canMerge(itemStack, player, world, pos, side))
			return true;

		return super.canPlaceBlockOnSide(world, pos, side, player, itemStack);
	}

	private IMergedBlock getMerged(IBlockState state)
	{
		IMergedBlock mergedBlock = IBlockComponent.getComponent(IMergedBlock.class, state.getBlock());
		if (mergedBlock == null)
			return null;

		if (mergedBlock.mergeSelfOnly() && state.getBlock() != this.block)
			return null;

		return mergedBlock;
	}

	private IBlockState checkMerge(ItemStack itemStack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, boolean offset)
	{
		IBlockState state = world.getBlockState(pos);
		IMergedBlock mergedBlock = getMerged(state);
		if (mergedBlock == null // || (offset && !mergedBlock.doubleCheckMerge())
				|| !mergedBlock.canMerge(itemStack, player, world, pos, side))
			return null;

		return mergedBlock.mergeBlock(world, pos, state, itemStack, player, side, hitX, hitY, hitZ);
	}
}
