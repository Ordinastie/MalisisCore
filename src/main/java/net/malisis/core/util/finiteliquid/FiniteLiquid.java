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
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.util.EnumFacing;

/**
 * @author Ordinastie
 *
 */

public abstract class FiniteLiquid extends BlockDynamicLiquid
{
	public static int renderId = -1;
	protected String name;

	private static EnumFacing[] dirs = new EnumFacing[] { NORTH, SOUTH, EAST, WEST };
	private int delay = 5;

	public FiniteLiquid(Material material)
	{
		super(material);
	}

	/*	@Override
		public Block setUnlocalizedName(String name)
		{
			this.name = name;
			super.setUnlocalizedName(name);
			return this;
		}

		public String getName()
		{
			return name;
		}

		public void setDelay(int delay)
		{
			this.delay = delay;
		}

		public void register()
		{
			GameRegistry.registerBlock(this, getName());
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
			setAmount(world, new MBlockState(pos, state), 16);
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
			return state.getMetadata() + 1;
		}

		public void setAmount(World world, MBlockState state, int amount)
		{
			if (amount <= 0)
				world.setBlockToAir(state.getX(), state.getY(), state.getZ());
			else
			{
				if (getAmount(state) == amount)
					return;
				world.setBlock(state.getX(), state.getY(), state.getZ(), this, amount - 1, 2);
				world.scheduleBlockUpdate(state.getX(), state.getY(), state.getZ(), this, delay);
			}
		}

		public int addAmount(World world, MBlockState state, int amount)
		{
			int current = getAmount(state);
			if (current == -1)
				return amount;

			int newAmount = Math.min(16 - current, amount);
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

		public AxisAlignedBB[] getBoundingBox(IBlockAccess world, int x, int y, int z, BoundingBoxType type)
		{
			int metadata = world.getBlockMetadata(x, y, z);
			return new AxisAlignedBB[] { AxisAlignedBB.getBoundingBox(0, 0, 0, 1, (double) metadata / 16, 1) };
		}

		@Override
		public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB mask, List list, Entity entity)
		{
			for (AxisAlignedBB aabb : getBoundingBox(world, x, y, z, BoundingBoxType.COLLISION))
			{
				if (aabb != null && mask.intersectsWith(aabb.offset(x, y, z)))
					list.add(aabb);
			}
		}

		@Override
		public MovingObjectPosition collisionRayTrace(World world, int x, int y, int z, Vec3 src, Vec3 dest)
		{
			return RaytraceBlock.set(world, src, dest, x, y, z).trace();
		}

		@Override
		public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z)
		{
			AxisAlignedBB[] aabbs = getBoundingBox(world, x, y, z, BoundingBoxType.SELECTION);
			if (ArrayUtils.isEmpty(aabbs))
				return AxisAlignedBB.getBoundingBox(0, 0, 0, 0, 0, 0);
			return aabbs[0].offset(x, y, z);
		}

		@Override
		@SideOnly(Side.CLIENT)
		public boolean shouldSideBeRendered(IBlockAccess worldIn, int x, int y, int z, int side)
		{
			Material material = worldIn.getBlock(x, y, z).getMaterial();
			return material == this.blockMaterial ? false : (side == 1 ? true : super.shouldSideBeRendered(worldIn, x, y, z, side));
		}

		@Override
		public boolean isOpaqueCube()
		{
			return false;
		}

		@Override
		public boolean canRenderInPass(int pass)
		{
			return pass == getRenderBlockPass();
		}

		@Override
		public int getRenderBlockPass()
		{
			return 1;
		}

		@Override
		public int getRenderType()
		{
			return renderId;
		}

		public static class FloodFill
		{
			FiniteLiquid fl;
			Set<MBlockPos> parsed = new HashSet<>();
			LinkedList<MBlockPos> toParse = new LinkedList<>();
			World world;
			MBlockPos origin;
			int amount = 0;

			public FloodFill(FiniteLiquid fl, World world, MBlockState state)
			{
				this.fl = fl;
				this.world = world;
				this.origin = state.getPos();
				this.amount = fl.getAmount(state);
				toParse.add(state.getPos());
			}

			public boolean shouldParse(MBlockPos pos)
			{
				if (!origin.isInRange(pos, 16))
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
				if (da != -1 && da != 16)
				{
					int transfered = Math.min(amount, 4);
					transfered = Math.min(transfered, 16 - da);
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
				MBlockPos pos = state.getPos();
				if (state.getBlock() == fl)
					for (EnumFacing dir : dirs)
					{
						MBlockPos newPos = pos.offset(dir);
						if (!parsed.contains(newPos) && shouldParse(newPos))
							toParse.add(newPos);
					}
				parsed.add(state.getPos());
			}

		}

		public static class SpreadData
		{
			MBlockPos pos;
			int amount;

			public SpreadData(MBlockPos pos, int amount)
			{
				this.pos = pos;
				this.amount = amount;
			}
		}
		*/
}
