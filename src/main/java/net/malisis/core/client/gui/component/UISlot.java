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

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.decoration.UITooltip;
import net.malisis.core.client.gui.element.SimpleGuiShape;
import net.malisis.core.client.gui.event.component.StateChangeEvent.HoveredStateChange;
import net.malisis.core.client.gui.icon.GuiIcon;
import net.malisis.core.inventory.InventoryEvent;
import net.malisis.core.inventory.MalisisInventoryContainer;
import net.malisis.core.inventory.MalisisInventoryContainer.ActionType;
import net.malisis.core.inventory.MalisisSlot;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.util.MouseButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.google.common.eventbus.Subscribe;

public class UISlot extends UIComponent<UISlot>
{
	/** Icon to use for the background of this {@link UISlot} */
	protected GuiIcon icon;
	/** Icon for Mojang fix */
	protected GuiIcon iconLeft;
	/** Icon for Mojang fix */
	protected GuiIcon iconTop;

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
	public UISlot(MalisisGui gui, MalisisSlot slot)
	{
		super(gui);
		this.slot = slot;
		this.width = 18;
		this.height = 18;
		slot.register(this);

		shape = new SimpleGuiShape();

		icon = gui.getGuiTexture().getIcon(209, 30, 18, 18);
		iconLeft = gui.getGuiTexture().getIcon(209, 30, 1, 18);
		iconTop = gui.getGuiTexture().getIcon(209, 30, 18, 1);

	}

	/**
	 * Instantiates a new {@link UISlot}.
	 *
	 * @param gui the gui
	 */
	public UISlot(MalisisGui gui)
	{
		this(gui, null);
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
		if (slot.getItemStack() == null)
		{
			tooltip = defaultTooltip;
			return;
		}

		List<String> lines = slot.getItemStack().getTooltip(Minecraft.getMinecraft().thePlayer,
				Minecraft.getMinecraft().gameSettings.advancedItemTooltips);

		lines.set(0, slot.getItemStack().getRarity().rarityColor + lines.get(0));
		for (int i = 1; i < lines.size(); i++)
			lines.set(i, EnumChatFormatting.GRAY + lines.get(i));

		tooltip = new UITooltip(getGui()).setText(lines);
	}

	@Override
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		shape.resetState();
		shape.setSize(18, 18);
		rp.icon.set(icon);
		renderer.drawShape(shape, rp);
		renderer.next();
	}

	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		MalisisInventoryContainer container = MalisisGui.currentGui().getInventoryContainer();
		if (container == null)
			return;

		ItemStack itemStack = slot.getItemStack() != null ? slot.getItemStack().copy() : null;
		ItemStack draggedItemStack = slot.getDraggedItemStack();

		// if dragged slots contains an itemStack for this slot, add the stack size
		EnumChatFormatting format = null;
		if (itemStack == null)
			itemStack = draggedItemStack;
		else if (draggedItemStack != null)
		{
			itemStack.stackSize += draggedItemStack.stackSize;
			if (itemStack.stackSize == itemStack.getMaxStackSize())
				format = EnumChatFormatting.YELLOW;
		}

		if (itemStack != null)
			renderer.drawItemStack(itemStack, 1, 1, format);

		// draw the white shade over the slot
		if (hovered || draggedItemStack != null)
		{
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			GL11.glShadeModel(GL11.GL_SMOOTH);

			RenderParameters rp = new RenderParameters();
			rp.colorMultiplier.set(0xFFFFFF);
			rp.alpha.set(80);
			rp.useTexture.set(false);

			shape.resetState();
			shape.setSize(16, 16);
			shape.translate(1, 1, 100);
			renderer.drawShape(shape, rp);
			renderer.next();

			GL11.glShadeModel(GL11.GL_FLAT);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
		}
		GL11.glEnable(GL11.GL_ALPHA_TEST);

		// Dirty fix because Mojang can't count and masks overflow the slots
		shape.resetState();
		shape.setSize(1, 18);
		shape.translate(0, 0, 50);
		rp.icon.set(iconLeft);
		renderer.drawShape(shape, rp);
		shape.resetState();
		shape.setSize(18, 1);
		shape.translate(0, 0, 50);
		rp.icon.set(iconTop);
		renderer.drawShape(shape, rp);

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

		if (container.getPickedItemStack() != null)
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

		if (container.getPickedItemStack() == null || !buttonRelased)
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

		if (container.getPickedItemStack() != null && !container.isDraggingItemStack() && buttonRelased)
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
}
