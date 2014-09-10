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

package net.malisis.core.renderer;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * @author Ordinastie
 * 
 */
public class ConnectedTextureIcon extends MalisisIcon
{
	private static int NONE = 0;
	private static int LEFT = 1;
	private static int TOP = 1 << 1;
	private static int RIGHT = 1 << 2;
	private static int BOTTOM = 1 << 3;
	private static int FULL = LEFT | TOP | RIGHT | BOTTOM;

	private MalisisIcon[] icons = new MalisisIcon[16];

	public ConnectedTextureIcon(TextureMap register, String name)
	{
		this.name = name;
		CTPart part = new CTPart(name);
		register.setTextureEntry(name, part);
		part = new CTPart(name + "2");
		register.setTextureEntry(name + "2", part);
	}

	private void initIcons(CTPart part)
	{
		TextureIcon icon = new TextureIcon(part);
		double size = 1D / 3D;

		if (part.getIconName().equals(name))
		{
			icons[LEFT | TOP] = icon.clone().clip(0, 0, size, size);
			icons[TOP] = icon.clone().clip(size, 0, size, size);
			icons[RIGHT | TOP] = icon.clone().clip(2 * size, 0, size, size);

			icons[LEFT] = icon.clone().clip(0, size, size, size);
			icons[NONE] = icon.clone().clip(size, size, size, size);
			icons[RIGHT] = icon.clone().clip(2 * size, size, size, size);

			icons[LEFT | BOTTOM] = icon.clone().clip(0, 2 * size, size, size);
			icons[BOTTOM] = icon.clone().clip(size, 2 * size, size, size);
			icons[RIGHT | BOTTOM] = icon.clone().clip(2 * size, 2 * size, size, size);
		}
		else
		{
			icons[LEFT | TOP | BOTTOM] = icon.clone().clip(0, 0, size, size);
			icons[TOP | BOTTOM] = icon.clone().clip(size, 0, size, size);
			icons[LEFT | RIGHT | TOP] = icon.clone().clip(2 * size, 0, size, size);

			icons[LEFT | RIGHT] = icon.clone().clip(0, size, size, size);
			icons[FULL] = icon.clone().clip(size, size, size, size);
			//icons[LEFT | RIGHT] = icon.clone().clip(2 * size, size, size, size);

			icons[LEFT | RIGHT | BOTTOM] = icon.clone().clip(0, 2 * size, size, size);
			//icons[TOP | BOTTOM] = icon.clone().clip(size, 2 * size, size, size);
			icons[RIGHT | TOP | BOTTOM] = icon.clone().clip(2 * size, 2 * size, size, size);
		}
	}

	public IIcon getFullIcon()
	{
		return icons[FULL];
	}

	public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side)
	{
		int connections = getConnections(world, x, y, z, side);
		MalisisIcon icon = icons[connections];

		return icon;
	}

	private int getConnections(IBlockAccess world, int x, int y, int z, int side)
	{
		Block block = world.getBlock(x, y, z);
		ForgeDirection dir = ForgeDirection.getOrientation(side);

		int connection = 0;

		if (dir == ForgeDirection.WEST)
		{
			if (world.getBlock(x, y, z - 1) == block)
				connection |= LEFT;
			if (world.getBlock(x, y, z + 1) == block)
				connection |= RIGHT;
			if (world.getBlock(x, y + 1, z) == block)
				connection |= TOP;
			if (world.getBlock(x, y - 1, z) == block)
				connection |= BOTTOM;
		}
		else if (dir == ForgeDirection.EAST)
		{
			if (world.getBlock(x, y, z + 1) == block)
				connection |= LEFT;
			if (world.getBlock(x, y, z - 1) == block)
				connection |= RIGHT;
			if (world.getBlock(x, y + 1, z) == block)
				connection |= TOP;
			if (world.getBlock(x, y - 1, z) == block)
				connection |= BOTTOM;
		}
		else if (dir == ForgeDirection.SOUTH)
		{
			if (world.getBlock(x - 1, y, z) == block)
				connection |= LEFT;
			if (world.getBlock(x + 1, y, z) == block)
				connection |= RIGHT;
			if (world.getBlock(x, y + 1, z) == block)
				connection |= TOP;
			if (world.getBlock(x, y - 1, z) == block)
				connection |= BOTTOM;
		}
		else if (dir == ForgeDirection.NORTH)
		{
			if (world.getBlock(x + 1, y, z) == block)
				connection |= LEFT;
			if (world.getBlock(x - 1, y, z) == block)
				connection |= RIGHT;
			if (world.getBlock(x, y + 1, z) == block)
				connection |= TOP;
			if (world.getBlock(x, y - 1, z) == block)
				connection |= BOTTOM;
		}
		else if (dir == ForgeDirection.UP || dir == ForgeDirection.DOWN)
		{
			if (world.getBlock(x - 1, y, z) == block)
				connection |= LEFT;
			if (world.getBlock(x + 1, y, z) == block)
				connection |= RIGHT;
			if (world.getBlock(x, y, z - 1) == block)
				connection |= TOP;
			if (world.getBlock(x, y, z + 1) == block)
				connection |= BOTTOM;
		}

		return ~connection & 15;
	}

	private class CTPart extends TextureAtlasSprite
	{
		protected CTPart(String name)
		{
			super(name);
		}

		@Override
		public void initSprite(int width, int height, int x, int y, boolean rotated)
		{
			super.initSprite(width, height, x, y, rotated);
			ConnectedTextureIcon.this.initIcons(this);
		}

	}
}
