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

import java.util.List;

import net.malisis.core.MalisisCore;
import net.malisis.core.block.BoundingBoxType;
import net.malisis.core.block.IBlockComponent;
import net.malisis.core.block.IComponent;
import net.malisis.core.renderer.component.SlopedCornerShapeComponent;
import net.malisis.core.util.AABBUtils;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.google.common.collect.Lists;

/**
 * @author Ordinastie
 *
 */
public class SlopedCornerComponent implements IBlockComponent
{
	public static PropertyBool INVERTED = PropertyBool.create("inverted");
	public static PropertyBool DOWN = PropertyBool.create("down");

	public SlopedCornerComponent()
	{

	}

	@Override
	public PropertyBool getProperty()
	{
		return null;
	}

	@Override
	public IProperty<?>[] getProperties()
	{
		return new IProperty<?>[] { INVERTED, DOWN };
	}

	@Override
	public IBlockState setDefaultState(Block block, IBlockState state)
	{
		return state.withProperty(INVERTED, false).withProperty(DOWN, false);
	}

	@Override
	public List<IComponent> getDependencies()
	{
		List<IComponent> deps = Lists.newArrayList(new DirectionalComponent());

		if (MalisisCore.isClient())
			deps.add(new SlopedCornerShapeComponent());

		return deps;
	}

	@Override
	public IBlockState getStateForPlacement(Block block, World world, BlockPos pos, IBlockState state, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, ItemStack itemStack)
	{
		//boolean inverted = meta == 4;
		boolean down = facing == EnumFacing.DOWN || (facing != EnumFacing.UP && hitY > 0.5F);
		return state.withProperty(DOWN, down);//.withProperty(INVERTED, inverted);
	}

	@Override
	public AxisAlignedBB[] getBoundingBoxes(Block block, IBlockAccess world, BlockPos pos, IBlockState state, BoundingBoxType type)
	{
		boolean inverted = isInverted(state);
		boolean down = isDown(state);
		float[][] fx;
		float[][] fy = { { 0, 1 }, { 0, 1 } };
		float[][] fz;
		if (inverted)
		{
			if (down)
			{
				fx = new float[][] { { 0, .25F }, { 0, 1F } };
				fz = new float[][] { { 0, .25F }, { 0, 1F } };
			}
			else
			{
				fx = new float[][] { { 0, 1 }, { 0, .25F } };
				fz = new float[][] { { 0, 1 }, { 0, .25F } };
			}

		}
		else
		{
			if (down)
			{
				fx = new float[][] { { 0, 1 / 8F }, { 0, 9 / 8F } };
				fz = new float[][] { { 0, 1 / 8F }, { 0, 9 / 8F } };
			}
			else
			{
				fx = new float[][] { { 0, .5F }, { 0, 0 } };
				fz = new float[][] { { 0, .5F }, { 0, 0 } };
			}
		}

		return AABBUtils.slice(8, fx, fy, fz, true);
	}

	@Override
	public void getSubBlocks(Block block, Item item, CreativeTabs tab, List<ItemStack> list)
	{
		list.add(new ItemStack(item, 1, 0));
		list.add(new ItemStack(item, 1, 4)); //inverted : use 4 as metadata so it maps to the right state with getStateFromMeta
	}

	@Override
	public int damageDropped(Block block, IBlockState state)
	{
		return isInverted(state) ? 1 : 0;
	}

	@Override
	public int getMetaFromState(Block block, IBlockState state)
	{
		return (isInverted(state) ? 4 : 0) + (isDown(state) ? 8 : 0);
	}

	@Override
	public IBlockState getStateFromMeta(Block block, IBlockState state, int meta)
	{
		return state.withProperty(DOWN, (meta & 8) != 0).withProperty(INVERTED, (meta & 4) != 0);
	}

	@Override
	public Boolean isOpaqueCube(Block block, IBlockState state)
	{
		return false;
	}

	@Override
	public Boolean isFullBlock(Block block, IBlockState state)
	{
		return false;
	}

	@Override
	public Boolean isFullCube(Block block, IBlockState state)
	{
		return false;
	}

	public static boolean isDown(IBlockAccess world, BlockPos pos)
	{
		return world != null && pos != null ? isInverted(world.getBlockState(pos)) : false;
	}

	public static boolean isDown(IBlockState state)
	{
		SlopedCornerComponent sc = IComponent.getComponent(SlopedCornerComponent.class, state.getBlock());
		if (sc == null)
			return false;

		if (!state.getProperties().containsKey(DOWN))
			return false;

		return state.getValue(DOWN);
	}

	public static boolean isInverted(IBlockAccess world, BlockPos pos)
	{
		return world != null && pos != null ? isInverted(world.getBlockState(pos)) : false;
	}

	public static boolean isInverted(IBlockState state)
	{
		SlopedCornerComponent sc = IComponent.getComponent(SlopedCornerComponent.class, state.getBlock());
		if (sc == null)
			return false;

		if (!state.getProperties().containsKey(INVERTED))
			return false;

		return state.getValue(INVERTED);
	}

}
