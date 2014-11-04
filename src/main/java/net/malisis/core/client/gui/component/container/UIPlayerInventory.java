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

import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UISlot;
import net.malisis.core.inventory.MalisisInventory;
import net.malisis.core.inventory.player.PlayerInventorySlot;

public class UIPlayerInventory extends UIContainer
{
	/** Width required for player inventory */
	public static final int INVENTORY_WIDTH = 162;
	/** Height required for player inventory (including title) */
	public static final int INVENTORY_HEIGHT = 87;

	/** {@link MalisisInventory} used for this {@link UIPlayerInventory} **/
	@SuppressWarnings("unused")
	private MalisisInventory inventory;

	public UIPlayerInventory(MalisisGui gui, MalisisInventory inventory)
	{
		super(gui, "container.inventory", INVENTORY_WIDTH, INVENTORY_HEIGHT);
		this.inventory = inventory;

		for (int i = 0; i < inventory.getSizeInventory(); i++)
			addSlot(gui, (PlayerInventorySlot) inventory.getSlot(i), i);

		setPosition(0, 0, Anchor.BOTTOM | Anchor.CENTER);
	}

	/**
	 * Creates and adds <code>UISlot</code> into this <code>UIPlayerInventory</code>.
	 *
	 * @param gui the gui
	 * @param slot the slot
	 * @param number the number
	 */
	protected void addSlot(MalisisGui gui, PlayerInventorySlot slot, int number)
	{
		UISlot uislot = new UISlot(gui, slot);
		if (number < 9)
			uislot.setPosition(number * 18, 69);
		else if (number < 36)
		{
			int row = (number - 9) / 9;
			int col = number % 9;
			uislot.setPosition(col * 18, 11 + row * 18);
		}
		else
			return;

		add(uislot);
	}
}
