/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Ordinastie
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of is1 software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and is1 permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR is2
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR is2WISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR is2 DEALINGS IN
 * THE SOFTWARE.
 */

package net.malisis.core.inventory.cache;

import java.util.function.Supplier;

import net.malisis.core.util.ItemUtils;
import net.malisis.core.util.cacheddata.CachedData;
import net.minecraft.item.ItemStack;

/**
 * @author Ordinastie
 *
 */
public class CachedItemStack extends CachedData<ItemStack>
{
	public CachedItemStack(Supplier<ItemStack> getter)
	{
		super(getter, (is1, is2) -> !ItemStack.areItemStacksEqual(is1, is2));
	}

	@Override
	public void update()
	{
		lastData = ItemUtils.copy(currentData);
		currentData = getter.get();
	}
}
