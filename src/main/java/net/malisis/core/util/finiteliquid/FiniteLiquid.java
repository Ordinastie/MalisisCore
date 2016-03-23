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

package net.malisis.core.util.finiteliquid;

import static net.minecraft.util.EnumFacing.*;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;

import net.malisis.core.block.BoundingBoxType;
import net.malisis.core.block.MalisisBlock;
import net.malisis.core.renderer.MalisisRendered;
import net.malisis.core.util.MBlockState;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * @author Ordinastie
 *
 */
@MalisisRendered(FiniteLiquidRenderer.class)
public abstract class FiniteLiquid extends MalisisBlock
{
	protected String name;

	public static final PropertyInteger AMOUNT = PropertyInteger.create("amount", 0, 15);
	private static EnumFacing[] dirs = new EnumFacing[] { NORTH, SOUTH, EAST, WEST };
	private int delay = 5;

	public FiniteLiquid(Material material)
	{
		super(material);

	}

	public void setDelay(int delay)
	{
		this.delay = delay;
	}

	@Override
	public void register()
	{
		super.register();
	}

	@Override
	protected BlockStateContainer createBlockState()
	{
		return new BlockStateContainer(this, AMOUNT);
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state)
	{
		if (!world.isRemote)
			world.scheduleBlockUpdate(pos, this, delay, 0);
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
	{
		setAmount(world, new MBlockState(pos, state), 15);
	}

	@Override
	public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock)
	{
		if (!world.isRemote)
			world.scheduleBlockUpdate(pos, this, delay, 0);
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand)
	{
		if (!world.isRemote)
			spreadLiquid(world, pos);
	}

	public int getAmount(MBlockState state)
	{
		if (state.getBlock() == Blocks.air)
			return 0;
		else if (state.getBlock() != this)
			return -1;
		return state.getBlockState().getValue(AMOUNT);
	}

	public void setAmount(World world, MBlockState state, int amount)
	{
		if (amount <= 0)
			world.setBlockToAir(state.getPos());
		else
		{
			if (getAmount(state) == amount)
				return;
			world.setBlockState(state.getPos(), getDefaultState().withProperty(AMOUNT, amount));
			world.scheduleBlockUpdate(state.getPos(), this, delay, 0);
		}
	}

	public int addAmount(World world, MBlockState state, int amount)
	{
		int current = getAmount(state);
		if (current == -1 || current == 15)
			return amount;

		int newAmount = Math.min(15 - current, amount);
		setAmount(world, state, current + newAmount);
		return amount - newAmount;
	}

	private void spreadLiquid(World world, BlockPos pos)
	{
		MBlockState state = new MBlockState(world, pos);
		//		if (getAmount(state) <= 1)
		//			return;

		FloodFill ff = new FloodFill(this, world, state);
		ff.parse();

	}

	@Override
	public AxisAlignedBB[] getBoundingBoxes(IBlockAccess world, BlockPos pos, IBlockState state, BoundingBoxType type)
	{
		int amount = world != null ? getAmount(new MBlockState(world, pos)) : 15;
		return new AxisAlignedBB[] { new AxisAlignedBB(0, 0, 0, 1, (double) amount / 16F, 1) };
	}

	@Override
	public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		Material material = world.getBlockState(pos).getMaterial();
		return material == this.blockMaterial ? false : (side == UP ? true : super.shouldSideBeRendered(state, world, pos, side));
	}

	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		return this.getDefaultState().withProperty(AMOUNT, Integer.valueOf(meta));
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		return state.getValue(AMOUNT).intValue();
	}

	@Override
	public boolean isOpaqueCube(IBlockState state)
	{
		return false;
	}

	@Override
	public boolean canRenderInLayer(BlockRenderLayer layer)
	{
		return layer == BlockRenderLayer.TRANSLUCENT;
	}

	public static class FloodFill
	{
		FiniteLiquid fl;
		Set<BlockPos> parsed = new HashSet<>();
		LinkedList<BlockPos> toParse = new LinkedList<>();
		World world;
		BlockPos origin;
		int amount = 0;

		public FloodFill(FiniteLiquid fl, World world, MBlockState state)
		{
			this.fl = fl;
			this.world = world;
			this.origin = state.getPos();
			this.amount = fl.getAmount(state);
			toParse.add(state.getPos());
		}

		public boolean shouldParse(BlockPos pos)
		{
			if (origin.distanceSq(pos) > 16 * 16)
				return false;

			if (toParse.contains(pos))
				return false;

			MBlockState state = new MBlockState(world, pos);
			return state.getBlock() == fl || state.getBlock() == Blocks.air;
		}

		public void parse()
		{
			while (toParse.size() > 0)
			{
				MBlockState state = new MBlockState(world, toParse.removeFirst());
				if (!process(state))
					break;
				parse(state);
			}

			fl.setAmount(world, new MBlockState(world, origin), amount);
		}

		public boolean process(MBlockState state)
		{
			MBlockState down = new MBlockState(world, state.getPos().down());
			int da = fl.getAmount(down);
			if (da != -1 && da != 15)
			{
				int transfered = Math.min(amount, 4);
				transfered = Math.min(transfered, 15 - da);
				fl.setAmount(world, down, da + transfered);
				amount -= transfered;
				return amount > 0;
			}

			if (state.getPos().equals(origin))
				return true;

			int a = fl.getAmount(state);
			if (a < amount - 1)
			{
				fl.setAmount(world, state, a + 1);
				amount--;
			}
			return amount > 1;
		}

		public void parse(MBlockState state)
		{
			BlockPos pos = state.getPos();
			if (state.getBlock() == fl)
				for (EnumFacing dir : dirs)
				{
					BlockPos newPos = pos.offset(dir);
					if (!parsed.contains(newPos) && shouldParse(newPos))
						toParse.add(newPos);
				}
			parsed.add(state.getPos());
		}

	}

	public static class SpreadData
	{
		BlockPos pos;
		int amount;

		public SpreadData(BlockPos pos, int amount)
		{
			this.pos = pos;
			this.amount = amount;
		}
	}

}
