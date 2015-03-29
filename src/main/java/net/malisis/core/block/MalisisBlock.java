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

import net.malisis.core.util.RaytraceBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import org.apache.commons.lang3.ArrayUtils;

import cpw.mods.fml.common.registry.GameRegistry;

/**
 * @author Ordinastie
 *
 */
public class MalisisBlock extends Block
{
	protected String name;
	protected AxisAlignedBB boundingBox;

	protected MalisisBlock(Material material)
	{
		super(material);
	}

	@Override
	public Block setUnlocalizedName(String name)
	{
		this.name = name;
		if (textureName == null)
			textureName = name;
		super.setTextureName(name);
		super.setUnlocalizedName(name);
		return this;
	}

	public String getName()
	{
		return name;
	}

	public void register()
	{
		GameRegistry.registerBlock(this, getName());
	}

	public void register(Class<? extends ItemBlock> item)
	{
		GameRegistry.registerBlock(this, item, getName());
	}

	public AxisAlignedBB[] getBoundingBox(IBlockAccess world, int x, int y, int z, BoundingBoxType type)
	{
		return new AxisAlignedBB[] { AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ) };
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
}
