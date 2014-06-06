package net.malisis.core.client.gui.component.container;

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.component.UISlot;
import net.malisis.core.inventory.MalisisInventory;
import net.malisis.core.inventory.player.PlayerInventorySlot;

public class UIPlayerInventory extends UIContainer
{
	@SuppressWarnings("unused")
	private MalisisInventory inventory;
	
	public UIPlayerInventory(MalisisInventory inventory)
	{
		super(162, 76);
		this.inventory = inventory;
		
		for(int i = 0; i < inventory.getSizeInventory(); i++)
			addSlot((PlayerInventorySlot) inventory.getSlot(i), i);
	}
	
	private void addSlot(PlayerInventorySlot slot, int number)
	{
		UISlot uislot = new UISlot(slot);
		if(number < 9)
			uislot.setPosition(number * 18, 58);
		else if(number < 36)
		{
			int row = (number - 9) / 9;
			int col = number % 9;
			uislot.setPosition(col * 18, row * 18);
		}
		else
			return;
		
		add(uislot);
	}
	
	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		super.drawForeground(renderer, mouseX, mouseY, partialTick);
	}
}
