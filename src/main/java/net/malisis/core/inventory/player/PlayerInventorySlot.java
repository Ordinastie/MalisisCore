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

package net.malisis.core.inventory.player;

import static com.google.common.base.Preconditions.*;

import net.malisis.core.inventory.MalisisInventory;
import net.malisis.core.inventory.MalisisSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class PlayerInventorySlot extends MalisisSlot
{
	private EntityPlayer player;

	public PlayerInventorySlot(EntityPlayer p)
	{
		player = p;
	}

	@Override
	protected void setup(MalisisInventory inventory, int slotIndex)
	{
		super.setup(inventory, slotIndex);
		itemStack = player.inventory.getStackInSlot(slotIndex);
	}

	@Override
	public final void setItemStack(ItemStack itemStack)
	{
		super.setItemStack(checkNotNull(itemStack));
		player.inventory.setInventorySlotContents(index, itemStack);
	}

}
