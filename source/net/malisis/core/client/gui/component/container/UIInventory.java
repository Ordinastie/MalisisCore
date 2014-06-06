package net.malisis.core.client.gui.component.container;

import net.malisis.core.inventory.MalisisInventory;

public class UIInventory extends UIContainer
{
	@SuppressWarnings("unused")
	private MalisisInventory inventory;
	
	public UIInventory(MalisisInventory inventory)
	{
		this.inventory = inventory;
	}
	
}
