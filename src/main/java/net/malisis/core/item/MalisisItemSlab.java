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

import net.malisis.core.block.IMergedBlock;
import net.malisis.core.block.IRegisterable;
import net.malisis.core.block.component.SlabComponent;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author Ordinastie
 *
 */
public class MalisisItemSlab extends ItemBlock implements IRegisterable
{
	private final Block singleSlab;
	private final Block doubleSlab;

	public MalisisItemSlab(Block singleSlab, Block doubleSlab)
	{
		super(singleSlab);
		this.singleSlab = singleSlab;
		this.doubleSlab = doubleSlab;
		this.setMaxDamage(0);
		this.setHasSubtypes(true);
	}

	@Override
	public String getRegistryName()
	{
		return singleSlab.getUnlocalizedName().substring(5);
	}

	@Override
	public int getMetadata(int damage)
	{
		return damage;
	}

	@Override
	public String getUnlocalizedName(ItemStack stack)
	{
		return this.singleSlab.getUnlocalizedName();
	}

	@Override
	public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if (itemStack.stackSize == 0)
			return false;
		if (!player.canPlayerEdit(pos.offset(side), side, itemStack))
			return false;

		IBlockState placedState = placeSlab(itemStack, player, world, pos, side, false);
		if (placedState == null)
		{
			pos = pos.offset(side);
			placedState = placeSlab(itemStack, player, world, pos, side, true);
		}

		if (placedState == null)
			return super.onItemUse(itemStack, player, world, pos, side, hitX, hitY, hitZ);

		Block block = placedState.getBlock();
		if (world.checkNoEntityCollision(block.getCollisionBoundingBox(world, pos, placedState))
				&& world.setBlockState(pos, placedState, 3))
		{
			world.playSoundEffect(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, block.stepSound.getPlaceSound(),
					(block.stepSound.getVolume() + 1.0F) / 2.0F, this.doubleSlab.stepSound.getFrequency() * 0.8F);
			--itemStack.stackSize;
		}

		return true;
	}

	private IBlockState placeSlab(ItemStack itemStack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, boolean offset)
	{
		IBlockState slabState = world.getBlockState(pos);

		//placing onto a slab
		if (slabState.getBlock() == singleSlab)
		{
			boolean isTop = SlabComponent.isTop(slabState);
			boolean doubleSlabbed = (side == EnumFacing.UP && !isTop) || (side == EnumFacing.DOWN && isTop) || offset;

			if (slabState.getBlock() instanceof IMergedBlock)
				return ((IMergedBlock) slabState.getBlock()).mergeBlock(world, pos, slabState, itemStack);
			else if (doubleSlabbed)
				return doubleSlab.getDefaultState();
		}

		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side, EntityPlayer player, ItemStack itemStack)
	{
		if (placeSlab(itemStack, player, world, pos, side, false) != null)
			return true;
		pos = pos.offset(side);
		if (placeSlab(itemStack, player, world, pos, side, true) != null)
			return true;
		return super.canPlaceBlockOnSide(world, pos, side, player, itemStack);
	}
}
