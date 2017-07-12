/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Ordinastie
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

package net.malisis.core.inventory;

import static com.google.common.base.Preconditions.*;

import java.util.List;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

/**
 * @author Ordinastie
 *
 */
public class MalisisTab extends CreativeTabs
{
	private Supplier<Item> item;
	private ItemStack iconStack;
	private List<Object> items = Lists.newArrayList();
	private NonNullList<ItemStack> itemStacks = NonNullList.create();

	public MalisisTab(String name, Supplier<Item> item)
	{
		super(name);
		this.item = item;
	}

	@Override
	public ItemStack getTabIconItem()
	{
		if (iconStack == null)
			iconStack = new ItemStack(item.get());
		return iconStack;
	}

	@Override
	public void displayAllRelevantItems(NonNullList<ItemStack> list)
	{
		if (itemStacks.isEmpty())
		{
			for (Object o : items)
			{
				if (o instanceof Item)
					((Item) o).getSubItems(((Item) o).getCreativeTab(), itemStacks);
				else
				{
					Item i = Item.getItemFromBlock((Block) o);
					i.getSubItems(i.getCreativeTab(), itemStacks);
				}
			}
		}
		list.addAll(itemStacks);
	}

	public void addItem(Item item)
	{
		items.add(checkNotNull(item));
	}

	public void addItem(Block block)
	{
		items.add(checkNotNull(block));
	}

}
