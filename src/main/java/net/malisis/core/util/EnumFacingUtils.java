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

package net.malisis.core.util;

import net.malisis.core.block.IBlockDirectional;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;

/**
 * @author Ordinastie
 *
 */
public class EnumFacingUtils
{
	/**
	 * Gets the rotation count for the facing.
	 *
	 * @param facing the facing
	 * @return the rotation count
	 */
	public static int getRotationCount(EnumFacing facing)
	{
		switch (facing)
		{
			case EAST:
				return 1;
			case NORTH:
				return 2;
			case WEST:
				return 3;
			case SOUTH:
			default:
				return 0;
		}
	}

	/**
	 * Rotates facing {@code count} times.
	 *
	 * @param facing the facing
	 * @param count the count
	 * @return the enum facing
	 */
	public static EnumFacing rotateFacing(EnumFacing facing, int count)
	{
		while (count-- > 0)
			facing = facing.rotateAround(EnumFacing.Axis.Y);
		return facing;
	}

	public static EnumFacing getRealSide(IBlockState state, EnumFacing side)
	{
		return getRealSide(state, IBlockDirectional.DIRECTION, side);
	}

	public static EnumFacing getRealSide(IBlockState state, PropertyDirection property, EnumFacing side)
	{
		if (side == EnumFacing.UP && side == EnumFacing.DOWN)
			return side;

		EnumFacing direction = (EnumFacing) state.getValue(property);
		int count = EnumFacingUtils.getRotationCount(direction);
		side = EnumFacingUtils.rotateFacing(side, count);

		return side;
	}
}
