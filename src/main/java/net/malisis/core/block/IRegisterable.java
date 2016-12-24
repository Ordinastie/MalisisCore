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

import net.malisis.core.item.MalisisItemBlock;
import net.malisis.core.registry.MalisisRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;

/**
 * This interface allows {@link Block} or {@link Item} implementing it to be registered via {@link MalisisRegistry#register(IRegisterable)}.
 *
 * @author Ordinastie
 */
public interface IRegisterable
{
	/**
	 * Gets the registry name to use.
	 *
	 * @return the registry name
	 */
	public String getName();

	/**
	 * Gets the item to register the implementing {@link Block} with.<br>
	 *
	 * @param block the block
	 * @return the item
	 */
	public default Item getItem(Block block)
	{
		if (this instanceof MalisisBlock)
			return new MalisisItemBlock((MalisisBlock) block);

		return new ItemBlock(block);
	}

	/**
	 * Registers this {@link IRegisterable} into the {@link MalisisRegistry}.
	 */
	public default void register()
	{
		MalisisRegistry.register(this);
	}
}
