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

package net.malisis.core.util.multiblock;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.malisis.core.util.MBlockState;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

/**
 * @author Ordinastie
 *
 */
public class PatternMultiBlock extends MultiBlock
{
	private List<List<String>> pattern = new LinkedList<>();
	private Map<Character, MBlockState> blocks = new HashMap<>();

	public PatternMultiBlock()
	{

	}

	public PatternMultiBlock addLayer(String... layer)
	{
		pattern.add(Arrays.asList(layer));
		buildStates();
		return this;
	}

	public PatternMultiBlock withRef(char c, Block block)
	{
		return withRef(c, block.getDefaultState());
	}

	public PatternMultiBlock withRef(char c, IBlockState state)
	{
		return withRef(c, new MBlockState(state));
	}

	public PatternMultiBlock withRef(char c, MBlockState state)
	{
		blocks.put(c, state);
		buildStates();
		return this;
	}

	@Override
	public void setOffset(BlockPos offset)
	{
		super.setOffset(offset);
		buildStates();
	}

	@Override
	public void buildStates()
	{
		states.clear();
		BlockPos pos;
		MBlockState state;

		for (int y = 0; y < pattern.size(); y++)
		{
			List<String> layer = pattern.get(y);
			for (int z = 0; z < layer.size(); z++)
			{
				String row = layer.get(z);
				for (int x = 0; x < row.length(); x++)
				{
					pos = new BlockPos(x, y, z).add(offset);
					state = blocks.get(row.charAt(x));
					if (state != null)
						states.put(pos, new MBlockState(pos, state));
				}
			}
		}
	}

}
