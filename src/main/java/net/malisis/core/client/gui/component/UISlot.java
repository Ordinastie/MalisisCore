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

package net.malisis.core.client.gui.component;

import java.util.List;

import org.lwjgl.input.Keyboard;

import com.google.common.eventbus.Subscribe;

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.decoration.UITooltip;
import net.malisis.core.client.gui.element.GuiIcon;
import net.malisis.core.client.gui.element.GuiShape;
import net.malisis.core.client.gui.event.component.StateChangeEvent.HoveredStateChange;
import net.malisis.core.inventory.InventoryEvent;
import net.malisis.core.inventory.MalisisInventoryContainer;
import net.malisis.core.inventory.MalisisInventoryContainer.ActionType;
import net.malisis.core.inventory.MalisisSlot;
import net.malisis.core.util.MouseButton;
import net.malisis.core.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;

public class UISlot extends UIComponent<UISlot>
{
	protected GuiShape shape = new GuiShape(0, 0, 18, 18, GuiIcon.SLOT);
	//	/** IconProvider for Mojang fix */
	//	protected GuiIconProvider iconLeftProvider;
	//	/** IconProvider for Mojang fix */
	//	protected GuiIconProvider iconTopProvider;

	/** Whether the mouse button has been released at least once. */
	public static boolean buttonRelased = true;
	/** Slot to draw containing the itemStack */
	protected MalisisSlot slot;
	/** */
	protected UITooltip defaultTooltip;

	/**
	 * Instantiates a new {@link UISlot}.
	 *
	 * @param gui the gui
	 * @param slot the slot
	 */
	public UISlot(MalisisSlot slot)
	{
		this.slot = slot;
		this.width = 18;
		this.height = 18;
		slot.register(this);

		//		iconLeftProvider = new GuiIconProvider(gui.getGuiTexture().getIcon(209, 30, 1, 18));
		//		iconTopProvider = new GuiIconProvider(gui.getGuiTexture().getIcon(209, 30, 18, 1));

	}

	@Override
	public UISlot setTooltip(UITooltip tooltip)
	{
		defaultTooltip = tooltip;
		if (tooltip == null)
			tooltip = defaultTooltip;
		return this;
	}

	/**
	 * Updates the {@link UITooltip} of this {@link UISlot} based on the contained ItemStack.<br>
	 * If no ItemStack is present in slot, revert the tooltip back to default one.
	 */
	protected void updateTooltip()
	{
		if (slot.getItemStack().isEmpty())
		{
			tooltip = defaultTooltip;
			return;
		}

		List<String> lines = slot.getItemStack().getTooltip(Utils.getClientPlayer(),
															Minecraft.getMinecraft().gameSettings.advancedItemTooltips);

		lines.set(0, slot.getItemStack().getRarity().rarityColor + lines.get(0));
		for (int i = 1; i < lines.size(); i++)
			lines.set(i, TextFormatting.GRAY + lines.get(i));

		tooltip = new UITooltip().setText(lines);
	}

	@Override
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		renderer.drawShape(shape);
	}

	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		MalisisInventoryContainer container = MalisisGui.currentGui().getInventoryContainer();
		if (container == null)
			return;

		ItemStack itemStack = slot.getItemStack().copy();
		ItemStack draggedItemStack = slot.getDraggedItemStack();

		// if dragged slots contains an itemStack for this slot, add the stack size
		TextFormatting format = null;
		if (itemStack.isEmpty())
			itemStack = draggedItemStack;
		else if (!draggedItemStack.isEmpty())
		{
			itemStack.grow(draggedItemStack.getCount());
			if (itemStack.getCount() == itemStack.getMaxStackSize())
				format = TextFormatting.YELLOW;
		}

		if (!itemStack.isEmpty())
			renderer.drawItemStack(itemStack, 1, 1, new Style().setColor(format));

		// draw the white shade over the slot
		if (hovered || !draggedItemStack.isEmpty())
		{
			renderer.disableTextures();
			GlStateManager.disableLighting();
			GlStateManager.disableDepth();
			GlStateManager.colorMask(true, true, true, false);

			renderer.next();
			renderer.drawRectangle(1, 1, 100, 16, 16, 0xFFFFFF, 80);
			renderer.next();

			GlStateManager.colorMask(true, true, true, true);
			GlStateManager.enableLighting();
			GlStateManager.enableDepth();
			renderer.enableTextures();
		}
		//GL11.glEnable(GL11.GL_ALPHA_TEST);
	}

	@Override
	public boolean onClick(int x, int y)
	{
		return super.onClick(x, y);
	}

	@Override
	public boolean onRightClick(int x, int y)
	{
		return super.onRightClick(x, y);
	}

	@Override
	public boolean onButtonPress(int x, int y, MouseButton button)
	{
		ActionType action = null;
		MalisisInventoryContainer container = MalisisGui.currentGui().getInventoryContainer();

		if (!container.getPickedItemStack().isEmpty())
			return super.onButtonPress(x, y, button);

		if (button.getCode() == Minecraft.getMinecraft().gameSettings.keyBindPickBlock.getKeyCode() + 100)
			action = ActionType.PICKBLOCK;

		if (button == MouseButton.LEFT)
			action = GuiScreen.isShiftKeyDown() ? ActionType.SHIFT_LEFT_CLICK : ActionType.LEFT_CLICK;

		if (button == MouseButton.RIGHT)
			action = GuiScreen.isShiftKeyDown() ? ActionType.SHIFT_RIGHT_CLICK : ActionType.RIGHT_CLICK;

		buttonRelased = false;
		MalisisGui.sendAction(action, slot, button.getCode());
		return true;

	}

	@Override
	public boolean onButtonRelease(int x, int y, MouseButton button)
	{
		ActionType action = null;
		MalisisInventoryContainer container = MalisisGui.currentGui().getInventoryContainer();

		if (container.getPickedItemStack().isEmpty() || !buttonRelased)
		{
			buttonRelased = true;
			return super.onButtonPress(x, y, button);
		}

		if (button == MouseButton.LEFT)
			action = GuiScreen.isShiftKeyDown() ? ActionType.SHIFT_LEFT_CLICK : ActionType.LEFT_CLICK;

		if (button == MouseButton.RIGHT)
			action = GuiScreen.isShiftKeyDown() ? ActionType.SHIFT_RIGHT_CLICK : ActionType.RIGHT_CLICK;

		MalisisGui.sendAction(action, slot, button.getCode());
		return true;
	}

	@Override
	public boolean onDrag(int lastX, int lastY, int x, int y, MouseButton button)
	{
		MalisisInventoryContainer container = MalisisGui.currentGui().getInventoryContainer();
		ActionType action = null;

		if (!container.getPickedItemStack().isEmpty() && !container.isDraggingItemStack() && buttonRelased)
		{
			if (button == MouseButton.LEFT)
				action = GuiScreen.isCtrlKeyDown() ? ActionType.DRAG_START_PICKUP : ActionType.DRAG_START_LEFT_CLICK;

			if (button == MouseButton.RIGHT)
				action = ActionType.DRAG_START_RIGHT_CLICK;
		}

		MalisisGui.sendAction(action, slot, button.getCode());
		return true;
	}

	@Override
	public boolean onDoubleClick(int x, int y, MouseButton button)
	{
		if (button != MouseButton.LEFT)
			return super.onDoubleClick(x, y, button);

		ActionType action = GuiScreen.isShiftKeyDown() ? ActionType.DOUBLE_SHIFT_LEFT_CLICK : ActionType.DOUBLE_LEFT_CLICK;
		MalisisGui.sendAction(action, slot, button.getCode());
		buttonRelased = false;
		return true;
	}

	@Override
	public boolean onKeyTyped(char keyChar, int keyCode)
	{
		if (!isHovered() || MalisisGui.isGuiCloseKey(keyCode))
			return super.onKeyTyped(keyChar, keyCode);

		ActionType action = null;
		int code = keyCode;
		if (keyCode == Minecraft.getMinecraft().gameSettings.keyBindDrop.getKeyCode())
			action = GuiScreen.isShiftKeyDown() ? ActionType.DROP_SLOT_STACK : ActionType.DROP_SLOT_ONE;

		if (keyCode == Minecraft.getMinecraft().gameSettings.keyBindPickBlock.getKeyCode())
			action = ActionType.PICKBLOCK;

		if (keyCode >= Keyboard.KEY_1 && keyCode <= Keyboard.KEY_9)
		{
			action = ActionType.HOTBAR;
			code -= 2;
		}

		MalisisGui.sendAction(action, slot, code);
		return true;
	}

	@Subscribe
	public void onSlotChanged(InventoryEvent.SlotChanged event)
	{
		if (event.getSlot() != slot)
			return;
		updateTooltip();
	}

	@Subscribe
	public void onHovered(HoveredStateChange<UISlot> event)
	{
		updateTooltip();

		if (event.getState() && MalisisGui.currentGui().getInventoryContainer().isDraggingItemStack())
		{
			//if (MalisisGui.currentGui().getInventoryContainer().getDraggedItemstack(slot) == null)
			MalisisGui.sendAction(ActionType.DRAG_ADD_SLOT, slot, 0);
		}

	}

	@Override
	public String toString()
	{
		return (this.name == null ? getClass().getSimpleName() : this.name) + " (Slot : " + slot + ")";
	}
}
