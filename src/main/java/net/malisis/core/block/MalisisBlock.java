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

package net.malisis.core.block;

import java.util.List;

import net.malisis.core.MalisisCore;
import net.malisis.core.renderer.DefaultRenderer;
import net.malisis.core.renderer.MalisisRendered;
import net.malisis.core.renderer.icon.IIconProvider;
import net.malisis.core.renderer.icon.IMetaIconProvider;
import net.malisis.core.renderer.icon.provider.DefaultIconProvider;
import net.malisis.core.util.RaytraceBlock;
import net.malisis.core.util.multiblock.IMultiBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.collect.Lists;

/**
 * @author Ordinastie
 *
 */
@MalisisRendered(DefaultRenderer.Block.class)
public class MalisisBlock extends Block implements IBoundingBox, IMetaIconProvider, IRegisterable
{
	protected String name;
	protected AxisAlignedBB boundingBox;

	@SideOnly(Side.CLIENT)
	protected IIconProvider iconProvider;

	protected MalisisBlock(Material material)
	{
		super(material);

		setDefaultState(blockState.getBaseState());
		if (this instanceof IBlockDirectional)
			setDefaultState(getDefaultState().withProperty(((IBlockDirectional) this).getPropertyDirection(), EnumFacing.SOUTH));
		if (this instanceof IMultiBlock)
			setDefaultState(getDefaultState().withProperty(((IMultiBlock) this).getOriginProperty(), false));
	}

	public Block setName(String name)
	{
		this.name = name;
		super.setUnlocalizedName(name);
		return this;
	}

	@Override
	public String getRegistryName()
	{
		return name;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void createIconProvider(Object object)
	{
		if (object != null)
			iconProvider = DefaultIconProvider.from(object);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider()
	{
		return iconProvider;
	}

	@Override
	protected BlockState createBlockState()
	{
		List<IProperty> properties = Lists.newArrayList();
		if (this instanceof IBlockDirectional)
			properties.add(((IBlockDirectional) this).getPropertyDirection());

		if (this instanceof IMultiBlock)
			properties.add(((IMultiBlock) this).getOriginProperty());

		return new BlockState(this, properties.toArray(new IProperty[0]));
	}

	public IBlockState getStateFromItemStack(ItemStack itemStack)
	{
		return getStateFromMeta(itemStack.getItem().getMetadata(itemStack.getMetadata()));
	}

	@Override
	public IBlockState onBlockPlaced(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
	{
		IBlockState state = super.onBlockPlaced(world, pos, facing, hitX, hitY, hitZ, meta, placer);
		if (this instanceof IMultiBlock)
			state = ((IMultiBlock) this).onBlockPlaced(this, world, pos, state, facing, hitX, hitY, hitZ, meta, placer);
		else if (this instanceof IBlockDirectional)
			state = ((IBlockDirectional) this).onBlockPlaced(this, world, pos, state, facing, hitX, hitY, hitZ, meta, placer);

		return state;
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
	{
		if (this instanceof IMultiBlock)
			((IMultiBlock) this).onBlockPlacedBy(this, world, pos, state, placer, stack);
		else
			super.onBlockPlacedBy(world, pos, state, placer, stack);
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state)
	{
		if (this instanceof IMultiBlock)
			((IMultiBlock) this).breakBlock(this, world, pos, state);

		super.breakBlock(world, pos, state);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockAccess world, BlockPos pos, BoundingBoxType type)
	{
		return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
	}

	@Override
	public void addCollisionBoxesToList(World world, BlockPos pos, IBlockState state, AxisAlignedBB mask, List list, Entity collidingEntity)
	{
		IBoundingBox.super.addCollisionBoxesToList(world, pos, state, mask, list, collidingEntity);
	}

	@Override
	public AxisAlignedBB getSelectedBoundingBox(World world, BlockPos pos)
	{
		return IBoundingBox.super.getSelectedBoundingBox(world, pos);
	}

	@Override
	public MovingObjectPosition collisionRayTrace(World world, BlockPos pos, Vec3 src, Vec3 dest)
	{
		return new RaytraceBlock(world, src, dest, pos).trace();
	}

	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		IBlockState state = getDefaultState();
		if (this instanceof IMultiBlock)
			state = ((IMultiBlock) this).getStateFromMeta(this, state, meta);
		else if (this instanceof IBlockDirectional)
			state = ((IBlockDirectional) this).getStateFromMeta(this, state, meta);

		return state;
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		int meta = 0;
		if (this instanceof IMultiBlock)
			meta = ((IMultiBlock) this).getMetaFromState(this, state);
		else if (this instanceof IBlockDirectional)
			meta = ((IBlockDirectional) this).getMetaFromState(this, state);

		return meta;
	}

	@Override
	public int getRenderType()
	{
		return MalisisCore.malisisRenderType;
	}
}
