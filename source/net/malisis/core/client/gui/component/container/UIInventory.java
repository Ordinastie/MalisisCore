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
