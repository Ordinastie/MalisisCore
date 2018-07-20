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
import java.util.stream.Collectors;

import org.lwjgl.input.Keyboard;

import com.google.common.eventbus.Subscribe;

import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.decoration.UITooltip;
import net.malisis.core.client.gui.element.size.Size;
import net.malisis.core.client.gui.event.component.StateChangeEvent.HoveredStateChange;
import net.malisis.core.client.gui.render.GuiIcon;
import net.malisis.core.client.gui.render.GuiRenderer;
import net.malisis.core.client.gui.render.shape.GuiShape;
import net.malisis.core.inventory.InventoryEvent;
import net.malisis.core.inventory.MalisisInventoryContainer;
import net.malisis.core.inventory.MalisisInventoryContainer.ActionType;
import net.malisis.core.inventory.MalisisSlot;
import net.malisis.core.util.MouseButton;
import net.malisis.core.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;

public class UISlot extends UIComponent
{
	/** Whether the mouse button has been released at least once. */
	public static boolean buttonRelased = true;
	/** Slot to draw containing the itemStack */
	protected MalisisSlot slot;
	/** Default tooltip to use when slot is empty. */
	protected UITooltip defaultTooltip;

	/** Shaped used for the hovered overlay. */
	protected GuiShape overlay = GuiShape.builder(this).position(1, 1).color(0xFFFFFF).zIndex(180).size(16, 16).alpha(0x80).build();

	/**
	 * Instantiates a new {@link UISlot}.
	 *
	 * @param slot the slot
	 */
	public UISlot(MalisisSlot slot)
	{
		super();
		this.slot = slot;
		setSize(Size.of(18, 18));

		setBackground(GuiShape.builder(this).size(18, 18).icon(GuiIcon.SLOT).build());
		setForeground(this::drawForeground);
		slot.register(this);
	}

	/**
	 * Instantiates a new {@link UISlot}.
	 */
	public UISlot()
	{
		this(null);
	}

	@Override
	public void setTooltip(UITooltip tooltip)
	{
		defaultTooltip = tooltip;
		if (this.tooltip == null)
			this.tooltip = defaultTooltip;
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

		List<String> lines = slot	.getItemStack()
									.getTooltip(Utils.getClientPlayer(),
												Minecraft.getMinecraft().gameSettings.advancedItemTooltips	? ITooltipFlag.TooltipFlags.ADVANCED
																											: ITooltipFlag.TooltipFlags.NORMAL);

		String text = lines.stream().collect(Collectors.joining(TextFormatting.RESET + "\n" + TextFormatting.GRAY));
		//		lines.set(0, slot.getItemStack().getRarity().rarityColor + lines.get(0));
		//		for (int i = 1; i < lines.size(); i++)
		//			lines.set(i, TextFormatting.GRAY + lines.get(i));

		tooltip = new UITooltip(slot.getItemStack().getRarity().rarityColor + text);
	}

	public void drawForeground(GuiRenderer renderer)
	{
		ItemStack itemStack = slot.getItemStack();
		TextFormatting format = null;
		//if dragged slots contains an itemStack for this slot, draw it instead
		if (!slot.getDraggedItemStack().isEmpty())
		{
			itemStack = slot.getDraggedItemStack();
			if (itemStack.getCount() == itemStack.getMaxStackSize())
				format = TextFormatting.YELLOW;
		}

		if (!itemStack.isEmpty())
			renderer.drawItemStack(itemStack, 1, 1, new Style().setColor(format));

		// draw the white shade over the slot
		if (hovered || !slot.getDraggedItemStack().isEmpty())
			overlay.render(renderer);
	}

	@Override
	public boolean onButtonPress(MouseButton button)
	{
		ActionType action = null;
		MalisisInventoryContainer container = MalisisGui.getInventoryContainer();

		if (!container.getPickedItemStack().isEmpty())
			return super.onButtonPress(button);

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
	public boolean onButtonRelease(MouseButton button)
	{
		ActionType action = null;
		MalisisGui.current();
		MalisisInventoryContainer container = MalisisGui.getInventoryContainer();

		if (container.getPickedItemStack().isEmpty() || !buttonRelased)
		{
			buttonRelased = true;
			return super.onButtonRelease(button);
		}

		if (button == MouseButton.LEFT)
			action = GuiScreen.isShiftKeyDown() ? ActionType.SHIFT_LEFT_CLICK : ActionType.LEFT_CLICK;

		if (button == MouseButton.RIGHT)
			action = GuiScreen.isShiftKeyDown() ? ActionType.SHIFT_RIGHT_CLICK : ActionType.RIGHT_CLICK;

		MalisisGui.sendAction(action, slot, button.getCode());
		return true;
	}

	@Override
	public boolean onDrag(MouseButton button)
	{
		MalisisGui.current();
		MalisisInventoryContainer container = MalisisGui.getInventoryContainer();
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
	public boolean onDoubleClick(MouseButton button)
	{
		if (button != MouseButton.LEFT)
			return super.onDoubleClick(button);

		ActionType action = GuiScreen.isShiftKeyDown() ? ActionType.DOUBLE_SHIFT_LEFT_CLICK : ActionType.DOUBLE_LEFT_CLICK;
		MalisisGui.sendAction(action, slot, button.getCode());
		buttonRelased = true;
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

		MalisisGui.current();
		if (event.getState() && MalisisGui.getInventoryContainer().isDraggingItemStack())
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
