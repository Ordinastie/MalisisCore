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

package net.malisis.core.client.gui.component.container;

import net.malisis.core.client.gui.component.UISlot;
import net.malisis.core.inventory.MalisisInventory;
import net.malisis.core.inventory.MalisisSlot;

public class UIInventory extends UIContainer
{
	@SuppressWarnings("unused")
	private MalisisInventory inventory;

	public UIInventory(MalisisInventory inventory)
	{
		this.inventory = inventory;
		this.width = Math.min(inventory.getSizeInventory() * 18, 9 * 18);
		this.height = (int) Math.ceil((float) inventory.getSizeInventory() / 9) * 18;
		for (int i = 0; i < inventory.getSizeInventory(); i++)
			addSlot(inventory.getSlot(i), i);

	}

	protected void addSlot(MalisisSlot slot, int number)
	{
		UISlot uislot = new UISlot(slot);

		int row = number / 9;
		int col = number % 9;
		uislot.setPosition(col * 18, row * 18);

		add(uislot);
	}
}
