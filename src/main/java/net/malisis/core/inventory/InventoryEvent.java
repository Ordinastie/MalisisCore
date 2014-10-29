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

package net.malisis.core.inventory;

public abstract class InventoryEvent
{
	private MalisisInventory inventory;

	public InventoryEvent(MalisisInventory inventory)
	{
		this.inventory = inventory;
	}

	public MalisisInventory getInventory()
	{
		return this.inventory;
	}

	/**
	 * Event fired when a {@link MalisisSlot} has its itemStack changed.
	 */
	public static class SlotChanged extends InventoryEvent
	{
		private MalisisSlot slot;

		public SlotChanged(MalisisInventory inventory, MalisisSlot slot)
		{
			super(inventory);
			this.slot = slot;
		}

		public MalisisSlot getSlot()
		{
			return this.slot;
		}
	}

	/**
	 * Event fired when a {@link MalisisInventory} is opened.
	 */
	public static class Open extends InventoryEvent
	{
		private MalisisInventoryContainer container;

		public Open(MalisisInventoryContainer container, MalisisInventory inventory)
		{
			super(inventory);
			this.container = container;
		}

		public MalisisInventoryContainer getContainer()
		{
			return this.container;
		}

	}

}
