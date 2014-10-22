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

package net.malisis.core.renderer.icon;

import static net.minecraftforge.common.util.ForgeDirection.*;
import net.minecraft.block.Block;
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

	//@formatter:off
	public static ForgeDirection[][] sides = { 	{ WEST, NORTH, EAST, SOUTH },
												{ WEST, NORTH, EAST, SOUTH },
												{ EAST, UP, WEST, DOWN },
												{ WEST, UP, EAST, DOWN },
												{ NORTH, UP, SOUTH, DOWN },
												{ SOUTH, UP, NORTH, DOWN }};
	//@formatter:on

	private MalisisIcon[] icons = new MalisisIcon[16];

	public ConnectedTextureIcon(String name, MalisisIcon part1, MalisisIcon part2)
	{
		super(name);
		part1.parentIcon = this;
		part2.parentIcon = this;
	}

	public ConnectedTextureIcon(TextureMap register, String name)
	{
		super(name);
		MalisisIcon part1 = new MalisisIcon(name);
		MalisisIcon part2 = new MalisisIcon(name + "2");

		part1.parentIcon = this;
		part2.parentIcon = this;

		register.setTextureEntry(name, part1);
		register.setTextureEntry(name + "2", part2);
	}

	@Override
	protected void initIcon(MalisisIcon icon, int width, int height, int x, int y, boolean rotated)
	{
		float f = 1F / 3F;

		if (icon.getIconName().equals(getIconName()))
		{
			icons[LEFT | TOP] = icon.copy().clip(0, 0, f, f);
			icons[TOP] = icon.copy().clip(f, 0, f, f);
			icons[RIGHT | TOP] = icon.copy().clip(2 * f, 0, f, f);

			icons[LEFT] = icon.copy().clip(0, f, f, f);
			icons[NONE] = icon.copy().clip(f, f, f, f);
			icons[RIGHT] = icon.copy().clip(2 * f, f, f, f);

			icons[LEFT | BOTTOM] = icon.copy().clip(0, 2 * f, f, f);
			icons[BOTTOM] = icon.copy().clip(f, 2 * f, f, f);
			icons[RIGHT | BOTTOM] = icon.copy().clip(2 * f, 2 * f, f, f);
		}
		else
		{
			icons[LEFT | TOP | BOTTOM] = icon.copy().clip(0, 0, f, f);
			icons[TOP | BOTTOM] = icon.copy().clip(f, 0, f, f);
			icons[LEFT | RIGHT | TOP] = icon.copy().clip(2 * f, 0, f, f);

			icons[LEFT | RIGHT] = icon.copy().clip(0, f, f, f);
			icons[FULL] = icon.copy().clip(f, f, f, f);
			//icons[LEFT | RIGHT] = icon.copy().clip(2 * f, f, f, f);

			icons[LEFT | RIGHT | BOTTOM] = icon.copy().clip(0, 2 * f, f, f);
			//icons[TOP | BOTTOM] = icon.copy().clip(f, 2 * f, f, f);
			icons[RIGHT | TOP | BOTTOM] = icon.copy().clip(2 * f, 2 * f, f, f);
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
		ForgeDirection dir = getOrientation(side);

		int connection = 0;
		for (int i = 0; i < 4; i++)
		{
			if (world.getBlock(x + sides[dir.ordinal()][i].offsetX, y + sides[dir.ordinal()][i].offsetY, z
					+ sides[dir.ordinal()][i].offsetZ) == block)
				connection |= (1 << i);
		}
		return ~connection & 15;
	}

	@Override
	public void initSprite(int width, int height, int x, int y, boolean rotated)
	{}
}
