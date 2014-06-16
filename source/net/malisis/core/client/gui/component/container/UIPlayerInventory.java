package net.malisis.core.client.gui.component.container;

import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.UISlot;
import net.malisis.core.inventory.MalisisInventory;
import net.malisis.core.inventory.player.PlayerInventorySlot;

public class UIPlayerInventory extends UIContainer
{
	@SuppressWarnings("unused")
	private MalisisInventory inventory;

	public UIPlayerInventory(MalisisInventory inventory)
	{
		super("container.inventory", 162, 87);
		this.inventory = inventory;

		for (int i = 0; i < inventory.getSizeInventory(); i++)
			addSlot((PlayerInventorySlot) inventory.getSlot(i), i);

		setPosition(0, 0, Anchor.BOTTOM | Anchor.CENTER);
	}

	protected void addSlot(PlayerInventorySlot slot, int number)
	{
		UISlot uislot = new UISlot(slot);
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
