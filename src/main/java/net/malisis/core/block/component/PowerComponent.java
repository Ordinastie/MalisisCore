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

import net.malisis.core.block.IComponent;
import net.malisis.core.block.IComponentProvider;
import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * @author Ordinastie
 *
 */
public class PowerComponent extends BooleanComponent
{
	/** InterractionType defines how the {@link PowerComponent} is changed. */
	public static enum InteractionType
	{
		/** The {@link PowerComponent} if affected by right click. */
		RIGHT_CLICK,
		/** The {@link PowerComponent} if affected by neighbor redstone power change. */
		REDSTONE,
		/** The {@link PowerComponent} if affected by both right click and neighbor redstone power change. */
		BOTH
	}

	/**
	 * ComponentType whether the {@link PowerComponent} should receive or provide redstone power.
	 */
	public static enum ComponentType
	{
		/** The {@link PowerComponent} is a power provider and emits redstone current. */
		PROVIDER,
		/** The {@link PowerComponent} is a power receiver and reacts to redstone current. */
		RECEIVER,
		/** The {@link PowerComponent} is both a power provider and receiver. */
		BOTH;
	}

	/** Type of interraction. */
	private final InteractionType interractionType;
	/** Type of component. */
	private final ComponentType componentType;
	/** Whether this {@link PowerComponent} {@link IComponentProvider} also has a {@link DirectionalComponent}. */
	private boolean hasDirectionalComponent = false;

	public PowerComponent(InteractionType interractionType, ComponentType componentType)
	{
		//default meta = 8
		super("power", false, 3);
		this.interractionType = interractionType;
		this.componentType = componentType;
	}

	@Override
	public void onComponentAdded(IComponentProvider provider)
	{
		hasDirectionalComponent = IComponent.getComponent(DirectionalComponent.class, provider) != null;
	}

	@Override
	public boolean onBlockActivated(Block block, World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if (interractionType == InteractionType.REDSTONE)
			return false;

		invert(world, pos);
		if (hasDirectionalComponent)
			world.notifyNeighborsOfStateChange(pos.offset(DirectionalComponent.getDirection(state).getOpposite()), block, true);
		return true;
	}

	@Override
	public void onNeighborBlockChange(Block block, World world, BlockPos pos, IBlockState state, Block neighborBlock, BlockPos neighborPos)
	{
		if (interractionType == InteractionType.RIGHT_CLICK || componentType == ComponentType.PROVIDER
				|| !neighborBlock.getDefaultState().canProvidePower())
			return;

		set(world, pos, world.isBlockPowered(pos));
	}

	@Override
	public boolean canProvidePower(Block block, IBlockState state)
	{
		return componentType != ComponentType.RECEIVER;
	}

	@Override
	public void breakBlock(Block block, World world, BlockPos pos, IBlockState state)
	{
		if (componentType == ComponentType.RECEIVER || !isPowered(state))
			return;

		world.notifyNeighborsOfStateChange(pos, block, true);
		//if block has direction, assume it provides power to the adjacent block
		if (IComponent.getComponent(DirectionalComponent.class, block) != null)
			world.notifyNeighborsOfStateChange(pos.offset(DirectionalComponent.getDirection(state).getOpposite()), block, true);

	}

	/**
	 * Checks whether the block in world is powered.
	 *
	 * @param world the world
	 * @param pos the pos
	 * @return true, if is powered
	 */
	public static boolean isPowered(World world, BlockPos pos)
	{
		return isPowered(world.getBlockState(pos));
	}

	/**
	 * Checks if is powered of this {@link IBlockState}.
	 *
	 * @param state the state
	 * @return true, if is powered
	 */
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

	public static PropertyBool getProperty(Block block)
	{
		PowerComponent pc = IComponent.getComponent(PowerComponent.class, block);
		return pc != null ? pc.getProperty() : null;
	}
}
