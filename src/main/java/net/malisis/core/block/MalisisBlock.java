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
import net.malisis.core.block.component.LadderComponent;
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
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
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

	protected List<IProperty<?>> getProperties()
	{
		return Lists.newArrayList();
	}

	protected void buildBlockState()
	{
		List<IProperty<?>> properties = getProperties();
		for (IBlockComponent component : getComponents())
			properties.addAll(Arrays.asList(component.getProperties()));

		try
		{
			blockStateField.set(this, new BlockStateContainer(this, properties.toArray(new IProperty[0])));
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
		lightOpacity = getDefaultState().isOpaqueCube() ? 255 : 0;
	}

	@Override
	public <T> T getComponent(Class<T> type)
	{
		for (IBlockComponent component : components)
		{
			if (type.isAssignableFrom(component.getClass()))
				return type.cast(component);
		}

		return null;
	}

	public Block setName(String name)
	{
		this.name = name;
		setUnlocalizedName(name);
		return this;
	}

	@Override
	public String getName()
	{
		return name;
	}

	public String getUnlocalizedName(IBlockState state)
	{
		for (IBlockComponent component : components)
		{
			String name = component.getUnlocalizedName(this, state);
			if (name != null)
				return name;
		}
		return getUnlocalizedName();
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
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		boolean b = false;
		for (IBlockComponent component : getComponents())
			b |= component.onBlockActivated(this, world, pos, state, player, hand, side, hitX, hitY, hitZ);

		return b;
	}

	@Override
	public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock)
	{
		for (IBlockComponent component : getComponents())
			component.onNeighborBlockChange(this, world, pos, state, neighborBlock);
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
	public AxisAlignedBB getBoundingBox(IBlockAccess world, BlockPos pos, IBlockState state, BoundingBoxType type)
	{
		for (IBlockComponent component : getComponents())
		{
			AxisAlignedBB aabb = component.getBoundingBox(this, world, pos, state, type);
			if (aabb != null)
				return aabb;
		}

		return FULL_BLOCK_AABB;
	}

	@Override
	public AxisAlignedBB[] getBoundingBoxes(IBlockAccess world, BlockPos pos, IBlockState state, BoundingBoxType type)
	{
		List<AxisAlignedBB> list = Lists.newArrayList();
		for (IBlockComponent component : getComponents())
		{
			AxisAlignedBB[] aabbs = component.getBoundingBoxes(this, world, pos, state, type);
			if (aabbs != null)
				list.addAll(Arrays.asList(aabbs));
		}

		return list.size() != 0 ? list.toArray(new AxisAlignedBB[0]) : IBoundingBox.super.getBoundingBoxes(world, pos, state, type);
	}

	@Override
	public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity)
	{
		IBoundingBox.super.addCollisionBoxToList(state, world, pos, mask, list, collidingEntity);
	}

	@Override
	public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World world, BlockPos pos)
	{
		return IBoundingBox.super.getSelectedBoundingBox(state, world, pos);
	}

	@Override
	public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d src, Vec3d dest)
	{
		return IBoundingBox.super.collisionRayTrace(state, world, pos, src, dest);
	}

	@Override
	public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side)
	{
		for (IBlockComponent component : getComponents())
			if (!component.canPlaceBlockOnSide(this, world, pos, side))
				return false;

		return super.canPlaceBlockOnSide(world, pos, side);
	}

	@Override
	public boolean canPlaceBlockAt(World world, BlockPos pos)
	{
		for (IBlockComponent component : getComponents())
			if (!component.canPlaceBlockAt(this, world, pos))
				return false;

		return super.canPlaceBlockAt(world, pos);
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

		return getMetaFromState(state);
	}

	@Override
	public void getSubBlocks(Item item, CreativeTabs tab, List<ItemStack> list)
	{
		List<ItemStack> l = Lists.newArrayList();
		for (IBlockComponent component : getComponents())
			component.getSubBlocks(this, item, tab, l);

		if (l.isEmpty())
			super.getSubBlocks(item, tab, list);
		else
			list.addAll(l);
	}

	//COLORS
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
	public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		for (IBlockComponent component : getComponents())
		{
			Boolean render = component.shouldSideBeRendered(this, world, pos, state, side);
			if (render != null)
				return render;
		}
		return super.shouldSideBeRendered(state, world, pos, side);
	}

	@Override
	public boolean isFullBlock(IBlockState state)
	{
		for (IBlockComponent component : getComponents())
		{
			Boolean full = component.isFullBlock(this, state);
			if (full != null)
				return full;
		}

		return super.isFullCube(state);
	}

	@Override
	public boolean isFullCube(IBlockState state)
	{
		for (IBlockComponent component : getComponents())
		{
			Boolean full = component.isFullCube(this, state);
			if (full != null)
				return full;
		}

		return super.isFullCube(state);
	}

	@Override
	public boolean isOpaqueCube(IBlockState state)
	{
		//parent constructor call
		if (getComponents() == null)
			return super.isOpaqueCube(state);

		for (IBlockComponent component : getComponents())
		{
			Boolean opaque = component.isOpaqueCube(this, state);
			if (opaque != null)
				return opaque;
		}
		return super.isOpaqueCube(state);
	}

	//OTHER

	@Override
	@SideOnly(Side.CLIENT)
	public int getPackedLightmapCoords(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		for (IBlockComponent component : getComponents())
		{
			//TODO: use max light value
			Integer light = component.getPackedLightmapCoords(this, world, pos, state);
			if (light != null)
				return light;
		}
		return super.getPackedLightmapCoords(state, world, pos);
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
	public int getLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		for (IBlockComponent component : getComponents())
		{
			Integer quantity = component.getLightOpacity(this, world, pos, state);
			if (quantity != null)
				return quantity;
		}
		return super.getLightOpacity(state, world, pos);
	}

	@Override
	public boolean isLadder(IBlockState state, IBlockAccess world, BlockPos pos, EntityLivingBase entity)
	{
		return IBlockComponent.getComponent(LadderComponent.class, this) != null;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state)
	{
		return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
	}

}
