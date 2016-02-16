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

package net.malisis.core.util.bbcode.node;

import net.malisis.core.util.bbcode.render.BBRenderElement;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * @author Ordinastie
 *
 */
public class BBItemNode extends BBNode
{
	protected String name;
	protected ItemStack itemStack;

	public BBItemNode(String name)
	{
		super("item", name);
		setName(name);
		standAlone = true;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
		this.attribute = name;
	}

	@Override
	public boolean hasTextNode()
	{
		return true;
	}

	@Override
	public BBItemNode copy()
	{
		return new BBItemNode(name);
	}

	@Override
	public void clean()
	{

	}

	@Override
	public void apply(BBRenderElement element)
	{
		element.itemStack = getItemStack();
	}

	public ItemStack getItemStack()
	{
		if (name == null || itemStack != null)
			return itemStack;

		String[] split = name.split("@");
		String name = split[0];
		int metadata = split.length > 1 ? Integer.valueOf(split[1]) : 0;

		Block b = Block.getBlockFromName(name);
		if (b != null)
			itemStack = new ItemStack(b, 0, metadata);
		else
		{
			Item i = Item.getByNameOrId(name);
			if (i != null)
				itemStack = new ItemStack(i, 0, metadata);
		}

		return itemStack;
	}

	@Override
	public String toRawString()
	{
		return "";
	}

}
