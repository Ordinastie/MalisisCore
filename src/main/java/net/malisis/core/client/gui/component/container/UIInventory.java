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

import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UISlot;
import net.malisis.core.inventory.MalisisInventory;
import net.malisis.core.inventory.MalisisSlot;

public class UIInventory extends UIContainer<UIInventory>
{
	@SuppressWarnings("unused")
	private MalisisInventory inventory;
	private int numCols;
	private boolean hasTitle;

	public UIInventory(MalisisGui gui, String title, MalisisInventory inventory, int numCols)
	{
		super(gui, title != null ? title : inventory.getInventoryName(), 0, 0);
		this.hasTitle = title != null || inventory.isCustomInventoryName();
		this.inventory = inventory;
		this.numCols = numCols;
		this.width = Math.min(inventory.getSizeInventory() * 18, numCols * 18);
		this.height = (int) Math.ceil((float) inventory.getSizeInventory() / numCols) * 18 + (hasTitle ? 11 : 0);
		for (int i = 0; i < inventory.getSizeInventory(); i++)
			addSlot(gui, inventory.getSlot(i), i);
	}

	public UIInventory(MalisisGui gui, MalisisInventory inventory, int numCols)
	{
		this(gui, null, inventory, numCols);

	}

	public UIInventory(MalisisGui gui, String title, MalisisInventory inventory)
	{
		this(gui, title, inventory, 9);
	}

	public UIInventory(MalisisGui gui, MalisisInventory inventory)
	{
		this(gui, null, inventory, 9);
	}

	protected void addSlot(MalisisGui gui, MalisisSlot slot, int number)
	{
		UISlot uislot = new UISlot(gui, slot);

		int row = number / numCols;
		int col = number % numCols;
		uislot.setPosition(col * 18, row * 18 + (hasTitle ? 11 : 0));

		add(uislot);
	}
}
