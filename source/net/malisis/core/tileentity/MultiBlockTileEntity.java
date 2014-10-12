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

package net.malisis.core.tileentity;

import net.malisis.core.util.MultiBlock;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * You can extend this class to make your Tile Entity use a MultiBlock system. If your TileEntity already has an ancestor, you need to
 * implement MultiBlock.IProvider and copy the method implementations provided here.
 *
 * @author Ordinastie
 *
 */
public class MultiBlockTileEntity extends TileEntity implements MultiBlock.IProvider
{
	protected MultiBlock multiBlock;

	@Override
	public void setMultiBlock(MultiBlock multiBlock)
	{
		this.multiBlock = multiBlock;
	}

	@Override
	public MultiBlock getMultiBlock()
	{
		return multiBlock;
	}

	@Override
	public void setWorldObj(World world)
	{
		super.setWorldObj(world);
		if (multiBlock != null)
			multiBlock.setWorld(world);
	}

	@Override
	public void readFromNBT(NBTTagCompound tag)
	{
		super.readFromNBT(tag);
		multiBlock = new MultiBlock(tag);
	}

	@Override
	public void writeToNBT(NBTTagCompound tag)
	{
		super.writeToNBT(tag);
		if (multiBlock != null)
			multiBlock.writeToNBT(tag);
	}

}
