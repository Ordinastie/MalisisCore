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

import net.minecraft.block.Block;
import net.minecraft.util.math.AxisAlignedBB;

/**
 * Defines a {@link IBlockComponent} or {@link Block} that should be smartly culled when rendering if {@link #shouldSmartCull()} return
 * <code>true</code>.<br>
 * This means the different {@link AxisAlignedBB} used for render bounds will be independently culled.
 *
 * @author Ordinastie
 */
public interface ISmartCull
{
	/**
	 * Whether this {@link IBlockComponent} or {@link Block} should use smart culling.
	 *
	 * @return true, if successful
	 */
	public default boolean shouldSmartCull()
	{
		return true;
	}

	/**
	 * Whether the {@link Block} should use smart culling.
	 *
	 * @param block the block
	 * @return true, if successful
	 */
	public static boolean shouldSmartCull(Block block)
	{
		ISmartCull sc = IBlockComponent.getComponent(ISmartCull.class, block);
		return sc != null && sc.shouldSmartCull();
	}

}
