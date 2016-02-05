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

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import net.malisis.core.MalisisCore;
import net.malisis.core.asm.AsmUtils;
import net.malisis.core.item.MalisisItemBlock;
import net.malisis.core.renderer.DefaultRenderer;
import net.malisis.core.renderer.MalisisRendered;
import net.malisis.core.renderer.icon.IIconProvider;
import net.malisis.core.renderer.icon.IMetaIconProvider;
import net.malisis.core.renderer.icon.provider.DefaultIconProvider;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
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
public class MalisisBlock extends Block implements IBoundingBox, IMetaIconProvider, IRegisterable, IComponentProvider
{
	private static Field blockStateField = AsmUtils.changeFieldAccess(Block.class, "blockState", "field_176227_L");

	protected String name;
	protected AxisAlignedBB boundingBox;
	protected final List<IBlockComponent> components = Lists.newArrayList();

	@SideOnly(Side.CLIENT)
	protected IIconProvider iconProvider;

	protected MalisisBlock(Material material)
	{
		super(material);
	}

	protected void buildBlockState()
	{
		List<IProperty> properties = Lists.newArrayList();
		for (IBlockComponent component : getComponents())
			properties.addAll(Arrays.asList(component.getProperties()));

		try
		{
			blockStateField.set(this, new BlockState(this, properties.toArray(new IProperty[0])));
		}
		catch (ReflectiveOperationException e)
		{
			MalisisCore.log.error("[MalisisBlock] Failed to set the new BlockState for {}.", this.getClass().getSimpleName(), e);
		}
	}

	private void buildDefaultState()
	{
		IBlockState state = blockState.getBaseState();
		for (IBlockComponent component : getComponents())
			state = component.setDefaultState(this, state);

		setDefaultState(state);
	}

	@Override
	public List<IBlockComponent> getComponents()
	{
		return components;
	}

	@Override
	public void addComponent(IBlockComponent component)
	{
		components.add(component);
		for (IBlockComponent dep : component.getDependencies())
			components.add(dep);

		buildBlockState();
		buildDefaultState();
		lightOpacity = isOpaqueCube() ? 255 : 0;
	}

	@Override
	public <T> T getComponent(Class<T> type)
	{
		for (IBlockComponent component : components)
		{
			if (type.isAssignableFrom(component.getClass()))
				return (T) component;
		}

		return null;
	}

	public Block setName(String name)
	{
		this.name = name;
		super.setUnlocalizedName(name);
		return this;
	}

	@Override
	public Item getItem(Block block)
	{
		for (IBlockComponent component : components)
		{
			Item item = component.getItem(this);
			if (item == null || item.getClass() != MalisisItemBlock.class)
				return item;
		}

		return IRegisterable.super.getItem(this);
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

	public IBlockState getStateFromItemStack(ItemStack itemStack)
	{
		return getStateFromMeta(itemStack.getItem().getMetadata(itemStack.getMetadata()));
	}

	//EVENTS
	@Override
	public IBlockState onBlockPlaced(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
	{
		IBlockState state = super.onBlockPlaced(world, pos, facing, hitX, hitY, hitZ, meta, placer);
		for (IBlockComponent component : getComponents())
			state = component.onBlockPlaced(this, world, pos, state, facing, hitX, hitY, hitZ, meta, placer);

		return state;
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
	{
		for (IBlockComponent component : getComponents())
			component.onBlockPlacedBy(this, world, pos, state, placer, stack);
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state)
	{
		for (IBlockComponent component : getComponents())
			component.breakBlock(this, world, pos, state);

		super.breakBlock(world, pos, state);
	}

	//BOUNDING BOX
	@Override
	public AxisAlignedBB getBoundingBox(IBlockAccess world, BlockPos pos, BoundingBoxType type)
	{
		for (IBlockComponent component : getComponents())
		{
			AxisAlignedBB aabb = component.getBoundingBox(this, world, pos, type);
			if (aabb != null)
				return aabb;
		}

		return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
	}

	@Override
	public AxisAlignedBB[] getBoundingBoxes(IBlockAccess world, BlockPos pos, BoundingBoxType type)
	{
		List<AxisAlignedBB> list = Lists.newArrayList();
		for (IBlockComponent component : getComponents())
		{
			AxisAlignedBB[] aabbs = component.getBoundingBoxes(this, world, pos, type);
			if (aabbs != null)
				list.addAll(Arrays.asList(aabbs));
		}

		return list.size() != 0 ? list.toArray(new AxisAlignedBB[0]) : IBoundingBox.super.getBoundingBoxes(world, pos, type);
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
		return IBoundingBox.super.collisionRayTrace(world, pos, src, dest);
	}

	//SUB BLOCKS
	@Override
	public int damageDropped(IBlockState state)
	{
		for (IBlockComponent component : getComponents())
		{
			int damage = component.damageDropped(this, state);
			if (damage != 0)
				return damage;
		}

		return super.damageDropped(state);
	}

	@Override
	public void getSubBlocks(Item item, CreativeTabs tab, List list)
	{
		List l = Lists.newArrayList();
		for (IBlockComponent component : getComponents())
			component.getSubBlocks(this, item, tab, l);

		if (l.isEmpty())
			super.getSubBlocks(item, tab, list);
		else
			list.addAll(l);
	}

	//COLORS
	@Override
	public int colorMultiplier(IBlockAccess world, BlockPos pos, int renderPass)
	{
		for (IBlockComponent component : components)
		{
			int color = component.colorMultiplier(this, world, pos, renderPass);
			if (color != 0xFFFFFF)
				return color;
		}

		return super.colorMultiplier(world, pos, renderPass);
	}

	@Override
	public MapColor getMapColor(IBlockState state)
	{
		for (IBlockComponent component : getComponents())
		{
			MapColor color = component.getMapColor(this, state);
			if (color != null)
				return color;
		}

		return super.getMapColor(state);
	}

	@Override
	public int getRenderColor(IBlockState state)
	{
		for (IBlockComponent component : getComponents())
		{
			int color = component.getRenderColor(this, state);
			if (color != 0xFFFFFF)
				return color;
		}

		return super.getRenderColor(state);
	}

	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		IBlockState state = getDefaultState();
		for (IBlockComponent component : getComponents())
			state = component.getStateFromMeta(this, state, meta);

		return state;
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		int meta = 0;
		for (IBlockComponent component : getComponents())
			meta += component.getMetaFromState(this, state);

		return meta;
	}

	//FULLNESS

	@Override
	public boolean shouldSideBeRendered(IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		for (IBlockComponent component : getComponents())
		{
			Boolean render = component.shouldSideBeRendered(this, world, pos, side);
			if (render != null)
				return render;
		}
		return super.shouldSideBeRendered(world, pos, side);
	}

	@Override
	public boolean isFullBlock()
	{
		for (IBlockComponent component : getComponents())
		{
			Boolean full = component.isFullCube(this);
			if (full != null)
				return full;
		}

		return super.isFullCube();
	}

	@Override
	public boolean isFullCube()
	{
		for (IBlockComponent component : getComponents())
		{
			Boolean full = component.isFullCube(this);
			if (full != null)
				return full;
		}

		return super.isFullCube();
	}

	@Override
	public boolean isOpaqueCube()
	{
		//parent constructor call
		if (getComponents() == null)
			return super.isOpaqueCube();

		for (IBlockComponent component : getComponents())
		{
			Boolean opaque = component.isOpaqueCube(this);
			if (opaque != null)
				return opaque;
		}
		return super.isOpaqueCube();
	}

	//OTHER

	@Override
	@SideOnly(Side.CLIENT)
	public int getMixedBrightnessForBlock(IBlockAccess world, BlockPos pos)
	{
		for (IBlockComponent component : getComponents())
		{
			//TODO: use max light value
			Integer light = component.getMixedBrightnessForBlock(this, world, pos);
			if (light != null)
				return light;
		}
		return super.getMixedBrightnessForBlock(world, pos);
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune)
	{
		for (IBlockComponent component : getComponents())
		{
			Item item = component.getItemDropped(this, state, rand, fortune);
			if (item != null)
				return item;
		}

		return super.getItemDropped(state, rand, fortune);

	}

	@Override
	public int quantityDropped(IBlockState state, int fortune, Random random)
	{
		for (IBlockComponent component : getComponents())
		{
			Integer quantity = component.quantityDropped(this, state, fortune, random);
			if (quantity != null)
				return quantity;
		}

		return super.quantityDropped(state, fortune, random);
	}

	@Override
	public int getLightOpacity(IBlockAccess world, BlockPos pos)
	{
		for (IBlockComponent component : getComponents())
		{
			Integer quantity = component.getLightOpacity(this, world, pos);
			if (quantity != null)
				return quantity;
		}
		return super.getLightOpacity(world, pos);
	}

	@Override
	public int getRenderType()
	{
		return MalisisCore.malisisRenderType;
	}
}
