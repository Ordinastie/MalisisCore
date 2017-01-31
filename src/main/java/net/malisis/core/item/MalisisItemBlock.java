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

import java.util.List;

import net.malisis.core.block.IComponent;
import net.malisis.core.block.IComponentProvider;
import net.malisis.core.block.IMergedBlock;
import net.malisis.core.block.IRegisterable;
import net.malisis.core.block.MalisisBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * ItemBlock adapted for MalisisCore.<br>
 * Users shouldn't need to extend this class as all the functionalities are queried from the corresponding block
 *
 * @author Ordinastie
 */
public class MalisisItemBlock extends ItemBlock implements IRegisterable, IComponentProvider
{
	public MalisisItemBlock(MalisisBlock block)
	{
		super(block);
	}

	private MalisisBlock block()
	{
		return (MalisisBlock) block;
	}

	@Override
	public String getName()
	{
		return block().getName();
	}

	@Override
	public void addComponent(IComponent component)
	{
		block().addComponent(component);
	}

	@Override
	public List<IComponent> getComponents()
	{
		return block().getComponents();
	}

	@Override
	public String getUnlocalizedName(ItemStack itemStack)
	{
		return block().getUnlocalizedName(block().getStateFromItemStack(itemStack));
	}

	@Override
	public int getMetadata(int damage)
	{
		return damage;
	}

	//onItemUse needs to be overriden to be able to handle merged blocks and components.
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		ItemStack itemStack = player.getHeldItem(hand);
		if (itemStack.isEmpty())
			return EnumActionResult.FAIL;
		if (!player.canPlayerEdit(pos.offset(side), side, itemStack))
			return EnumActionResult.FAIL;

		//first check if the block clicked can be merged with the one in hand
		IBlockState placedState = checkMerge(itemStack, player, world, pos, side, hitX, hitY, hitZ);
		BlockPos p = pos;
		//can't merge, offset the placed position to where the block should be placed in the world
		if (placedState == null)
		{
			p = pos.offset(side);
			float x = hitX - side.getFrontOffsetX();
			float y = hitY - side.getFrontOffsetY();
			float z = hitZ - side.getFrontOffsetZ();
			//check for merge at the new position too
			placedState = checkMerge(itemStack, player, world, p, side, x, y, z);
		}

		if (placedState == null)
			return super.onItemUse(player, world, pos, hand, side, hitX, hitY, hitZ);

		//block can be merged
		Block block = placedState.getBlock();
		if (world.checkNoEntityCollision(placedState.getCollisionBoundingBox(world, p)) && world.setBlockState(p, placedState, 3))
		{
			SoundType soundType = block.getSoundType(placedState, world, pos, player);
			world.playSound(player,
							pos,
							soundType.getPlaceSound(),
							SoundCategory.BLOCKS,
							(soundType.getVolume() + 1.0F) / 2.0F,
							soundType.getPitch() * 0.8F);
			itemStack.shrink(1);
		}

		return EnumActionResult.SUCCESS;
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

	/**
	 * Gets the {@link IMergedBlock} associated with the passed {@link IBlockState}.<br>
	 * If a component found doesn't allow merging between two different blocks and the passed state's block doesn't this block, null is
	 * returned instead.
	 *
	 * @param state the state
	 * @return the component, or null if there is no component, or if the component
	 */
	private IMergedBlock getMerged(IBlockState state)
	{
		IMergedBlock mergedBlock = IComponent.getComponent(IMergedBlock.class, state.getBlock());
		if (mergedBlock == null)
			return null;

		if (mergedBlock.mergeSelfOnly() && state.getBlock() != block())
			return null;

		return mergedBlock;
	}

	/**
	 * Checks whether the block can be merged with the one already in the world.
	 *
	 * @param itemStack the item stack
	 * @param player the player
	 * @param world the world
	 * @param pos the pos
	 * @param side the side
	 * @param hitX the hit X
	 * @param hitY the hit Y
	 * @param hitZ the hit Z
	 * @return the i block state
	 */
	private IBlockState checkMerge(ItemStack itemStack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		IBlockState state = world.getBlockState(pos);
		IMergedBlock mergedBlock = getMerged(state);
		if (mergedBlock == null // || (offset && !mergedBlock.doubleCheckMerge())
				|| !mergedBlock.canMerge(itemStack, player, world, pos, side))
			return null;

		return mergedBlock.mergeBlock(world, pos, state, itemStack, player, side, hitX, hitY, hitZ);
	}

	/**
	 * Checks whether the associated {@link MalisisItem} has subtypes.
	 *
	 * @return true, if successful
	 */
	@Override
	public boolean getHasSubtypes()
	{
		return block instanceof MalisisBlock ? ((MalisisBlock) block).hasItemSubtypes(this) : false;
	}
}
