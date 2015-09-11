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
import net.malisis.core.MalisisRegistry;
import net.malisis.core.renderer.DefaultRenderer;
import net.malisis.core.renderer.icon.metaprovider.IBlockMetaIconProvider;
import net.malisis.core.renderer.icon.provider.DefaultIconProvider;
import net.malisis.core.renderer.icon.provider.IBlockIconProvider;
import net.malisis.core.util.AABBUtils;
import net.malisis.core.util.RaytraceBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Ordinastie
 *
 */
public class MalisisBlock extends Block implements IBoundingBox, IBlockMetaIconProvider
{
	protected String name;
	protected AxisAlignedBB boundingBox;
	protected IBlockIconProvider iconProvider;

	protected MalisisBlock(Material material)
	{
		super(material);
	}

	public Block setName(String name)
	{
		this.name = name;
		super.setUnlocalizedName(name);
		return this;
	}

	@Override
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

	public void setTextureName(String textureName)
	{
		if (StringUtils.isEmpty(textureName))
			return;

		if (MalisisCore.isClient())
			setBlockIconProvider(new DefaultIconProvider(textureName));
	}

	public void setBlockIconProvider(IBlockIconProvider iconProvider)
	{
		this.iconProvider = iconProvider;
	}

	@Override
	public IBlockIconProvider getBlockIconProvider()
	{
		return iconProvider;
	}

	public void register()
	{
		register(ItemBlock.class);
	}

	public void register(Class<? extends ItemBlock> item)
	{
		GameRegistry.registerBlock(this, item, getName());
		if (MalisisCore.isClient())
		{
			MalisisRegistry.registerIconProvider(iconProvider);
			if (useDefaultRenderer())
				DefaultRenderer.block.registerFor(this);
		}
	}

	@Override
	protected BlockState createBlockState()
	{
		if (this instanceof IBlockDirectional)
			return ((IBlockDirectional) this).createBlockState(this);

		return super.createBlockState();
	}

	@Override
	public IBlockState onBlockPlaced(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
	{
		if (this instanceof IBlockDirectional)
			return ((IBlockDirectional) this).onBlockPlaced(this, world, pos, facing, hitX, hitY, hitZ, meta, placer);

		return super.onBlockPlaced(world, pos, facing, hitX, hitY, hitZ, meta, placer);
	}

	@Override
	public AxisAlignedBB[] getBoundingBox(IBlockAccess world, BlockPos pos, BoundingBoxType type)
	{
		return new AxisAlignedBB[] { new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ) };
	}

	@Override
	public void addCollisionBoxesToList(World world, BlockPos pos, IBlockState state, AxisAlignedBB mask, List list, Entity collidingEntity)
	{
		for (AxisAlignedBB aabb : AABBUtils.offset(pos, getBoundingBox(world, pos, BoundingBoxType.COLLISION)))
		{
			if (aabb != null && mask.intersectsWith(aabb))
				list.add(aabb);
		}
	}

	@Override
	public MovingObjectPosition collisionRayTrace(World world, BlockPos pos, Vec3 src, Vec3 dest)
	{
		return RaytraceBlock.set(world, src, dest, pos).trace();
	}

	@Override
	public AxisAlignedBB getSelectedBoundingBox(World world, BlockPos pos)
	{
		AxisAlignedBB[] aabbs = getBoundingBox(world, pos, BoundingBoxType.SELECTION);
		if (ArrayUtils.isEmpty(aabbs) || aabbs[0] == null)
			return AABBUtils.empty(pos);
		return AABBUtils.offset(pos, aabbs)[0];
	}

	public boolean useDefaultRenderer()
	{
		return true;
	}

	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		if (this instanceof IBlockDirectional)
			return ((IBlockDirectional) this).getStateFromMeta(this, meta);

		return super.getStateFromMeta(meta);
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		if (this instanceof IBlockDirectional)
			return ((IBlockDirectional) this).getMetaFromState(this, state);

		return super.getMetaFromState(state);
	}

	@Override
	public int getRenderType()
	{
		return MalisisCore.malisisRenderType;
	}
}
