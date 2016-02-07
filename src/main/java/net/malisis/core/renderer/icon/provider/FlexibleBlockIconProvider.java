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

package net.malisis.core.renderer.icon.provider;

import java.util.function.Function;

import net.malisis.core.renderer.icon.MalisisIcon;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;

/**
 * @author Ordinastie
 *
 */
public class FlexibleBlockIconProvider implements IBlockIconProvider
{
	private Block block;
	private MalisisIcon defaultIcon;
	private StateFacingFunc stateFacingFunc = null;

	public FlexibleBlockIconProvider(Block block, MalisisIcon defaultIcon, StateFacingFunc stateFacingFunc)
	{
		this.block = block;
		this.defaultIcon = defaultIcon;
		this.stateFacingFunc = stateFacingFunc;
	}

	@Override
	public void registerIcons(TextureMap textureMap)
	{
		if (defaultIcon != null)
			defaultIcon = defaultIcon.register(textureMap);

		if (stateFacingFunc == null)
			return;

		for (EnumFacing side : EnumFacing.VALUES)
		{
			for (Object state : block.getBlockState().getValidStates())
			{
				MalisisIcon icon = stateFacingFunc.apply((IBlockState) state, side);
				if (icon != null)
					icon.register(textureMap);
			}
		}
	}

	@Override
	public MalisisIcon getIcon()
	{
		return defaultIcon;
	}

	@Override
	public MalisisIcon getIcon(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side)
	{
		if (stateFacingFunc != null)
			return stateFacingFunc.apply(state, side);
		return getIcon();
	}

	public interface StateFunc extends Function<IBlockState, MalisisIcon>
	{
	};

	public interface FacingFunc extends Function<EnumFacing, MalisisIcon>
	{
	};

	public interface StateFacingFunc
	{
		public MalisisIcon apply(IBlockState state, EnumFacing facing);
	}

}
