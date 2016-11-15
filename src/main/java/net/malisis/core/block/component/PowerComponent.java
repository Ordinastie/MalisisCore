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
import net.malisis.core.block.IComponent;
import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * @author Ordinastie
 *
 */
public class PowerComponent implements IBlockComponent
{
	public static PropertyBool POWER = PropertyBool.create("power");

	public static enum Type
	{
		RIGHT_CLICK,
		REDSTONE
	}

	private final Type type;

	public PowerComponent(Type type)
	{
		this.type = type;
	}

	@Override
	public PropertyBool getProperty()
	{
		return POWER;
	}

	@Override
	public IBlockState setDefaultState(Block block, IBlockState state)
	{
		return state.withProperty(getProperty(), false);
	}

	@Override
	public boolean onBlockActivated(Block block, World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if (type == Type.REDSTONE)
			return false;

		boolean isPowered = isPowered(state);
		world.setBlockState(pos, state.withProperty(getProperty(), !isPowered));
		return true;
	}

	@Override
	public void onNeighborBlockChange(Block block, World world, BlockPos pos, IBlockState state, Block neighborBlock)
	{
		if (type == Type.RIGHT_CLICK)
			return;

		boolean isPowered = isRemotelyPowered(world, pos);
		world.setBlockState(pos, state.withProperty(getProperty(), isPowered));
	}

	@Override
	public boolean canProvidePower(Block block, IBlockState state)
	{
		return true;
	}

	@Override
	public IBlockState getStateFromMeta(Block block, IBlockState state, int meta)
	{
		return state.withProperty(getProperty(), (meta & 8) != 0);
	}

	@Override
	public int getMetaFromState(Block block, IBlockState state)
	{
		return isPowered(state) ? 8 : 0;
	}

	public static boolean isPowered(IBlockAccess world, BlockPos pos)
	{
		return world != null ? isPowered(world.getBlockState(pos)) : false;
	}

	public static boolean isPowered(IBlockState state)
	{
		PowerComponent pc = IComponent.getComponent(PowerComponent.class, state.getBlock());
		if (pc == null)
			return false;

		PropertyBool property = pc.getProperty();
		if (property == null || !state.getProperties().containsKey(property))
			return false;

		return state.getValue(property);
	}

	public static boolean isRemotelyPowered(World world, BlockPos pos)
	{
		return isRemotelyPowered(world, pos, true);
	}

	public static boolean isRemotelyPowered(World world, BlockPos pos, boolean strongPower)
	{
		boolean powered = world.isBlockIndirectlyGettingPowered(pos) != 0;
		if (powered || !strongPower)
			return powered;

		for (EnumFacing side : EnumFacing.VALUES)
			if (world.isBlockIndirectlyGettingPowered(pos.offset(side)) != 0)
				return true;
		return false;
	}

	public static PropertyBool getProperty(Block block)
	{
		PowerComponent pc = IComponent.getComponent(PowerComponent.class, block);
		return pc != null ? pc.getProperty() : null;
	}

}
