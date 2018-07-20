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

import org.apache.commons.lang3.StringUtils;

import net.malisis.core.client.gui.component.UISlot;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.element.position.Position;
import net.malisis.core.client.gui.element.size.Size;
import net.malisis.core.inventory.MalisisInventory;
import net.malisis.core.inventory.MalisisSlot;

public class UIInventory extends UIContainer
{
	protected UILabel label;
	protected MalisisInventory inventory;
	protected int numCols;
	protected boolean hasTitle;

	public UIInventory(String title, MalisisInventory inventory, int numCols)
	{
		if (!StringUtils.isEmpty(title))
			label = new UILabel(title, false);
		else if (inventory.hasCustomName())
			label = new UILabel(inventory.getName(), false);

		this.inventory = inventory;
		this.numCols = numCols;
		setSize(Size.of(() -> Math.min(inventory.getSize() * 18, numCols * 18),
						() -> (int) Math.ceil((float) inventory.getSize() / numCols) * 18 + (hasTitle ? 11 : 0)));

		if (label != null)
			add(label);

		for (int i = 0; i < inventory.getSize(); i++)
			addSlot(inventory.getSlot(i), i);
	}

	public UIInventory(MalisisInventory inventory, int numCols)
	{
		this(null, inventory, numCols);

	}

	public UIInventory(String title, MalisisInventory inventory)
	{
		this(title, inventory, 9);
	}

	public UIInventory(MalisisInventory inventory)
	{
		this(null, inventory, 9);
	}

	public UILabel getLabel()
	{
		return label;
	}

	protected void addSlot(MalisisSlot slot, int number)
	{
		UISlot uislot = new UISlot(slot);

		int row = number / numCols;
		int col = number % numCols;
		uislot.setPosition(Position.of(col * 18, row * 18 + (hasTitle ? 11 : 0)));

		add(uislot);
	}
}
