/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 PaleoCrafter, Ordinastie
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

package net.malisis.core.client.gui;

import net.malisis.core.client.gui.component.UISlot;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.decoration.UITooltip;
import net.malisis.core.client.gui.event.KeyboardEvent;
import net.malisis.core.client.gui.event.MouseEvent;
import net.malisis.core.inventory.MalisisInventoryContainer;
import net.malisis.core.inventory.MalisisInventoryContainer.ActionType;
import net.malisis.core.inventory.MalisisSlot;
import net.malisis.core.inventory.player.PlayerInventorySlot;
import net.malisis.core.packet.InventoryActionMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

/**
 * GuiScreenProxy
 * 
 * @author Ordinastie
 */
public class MalisisGui extends GuiScreen
{
	/**
	 * Renderer drawing the components.
	 */
	protected GuiRenderer renderer;
	/**
	 * Top level container which hold the user components. Spans across the whole screen.
	 */
	private UIContainer screen;
	/**
	 * UIContainer user provided.
	 */
	protected UIContainer container;
	/**
	 * Determines if the screen should be darkened when the GUI is opened.
	 */
	protected boolean guiscreenBackground = true;
	/**
	 * Last known position of the mouse.
	 */
	protected int lastMouseX, lastMouseY;
	/**
	 * How long since last click.
	 */
	protected long lastClick = 0;
	/**
	 * Inventory container that handles the inventories and slots actions.
	 */
	protected MalisisInventoryContainer inventoryContainer;
	/**
	 * Currently hovered itemStack.
	 */
	protected ItemStack hoveredItemStack;
	/**
	 * Time when the GUI was opened.
	 */
	private long startTime;
	/**
	 * Elasped time since the GUI was opened. Updated each render tick.
	 */
	private long elaspedTime;

	public MalisisGui(UIContainer container)
	{
		this();
		addToScreen(container);
	}

	protected MalisisGui()
	{
		this.renderer = new GuiRenderer();
		this.screen = new UIContainer();
		this.screen.clipContent = false;
		startTime = System.currentTimeMillis();
	}

	/**
	 * Sets the inventory container for this <code>MalisisGui</code>.
	 * 
	 * @param container
	 */
	public void setInventoryContainer(MalisisInventoryContainer container)
	{
		this.inventoryContainer = container;
	}

	/**
	 * Gets the inventory container for this <code>MalisisGui</code>.
	 * 
	 * @return
	 */
	public MalisisInventoryContainer getInventoryContainer()
	{
		return inventoryContainer;
	}

	/**
	 * Gets elapsed time since the GUI was opened.
	 * 
	 * @return
	 */
	public long getElapsedTime()
	{
		return elaspedTime;
	}

	/**
	 * Adds container to the screen.
	 * 
	 * @param container
	 */
	protected void addToScreen(UIContainer container)
	{
		this.container = container;
		screen.add(container);
	}

	/**
	 * Called when a mouse button is pressed down.
	 */
	@Override
	protected void mouseClicked(int x, int y, int button)
	{
		long time = System.currentTimeMillis();
		if (button == 0 && time - lastClick < 250)
		{
			doubleClick(x, y, button);
			lastClick = 0;
			return;
		}

		if (container.isInsideBounds(x, y))
			container.fireMouseEvent(new MouseEvent.Press(x, y, button));
		else if (inventoryContainer != null)
		{
			if (inventoryContainer.getPickedItemStack() != null)
			{
				ActionType action = button == 1 ? ActionType.DROP_ONE : ActionType.DROP_STACK;
				MalisisGui.sendAction(action, null, button);
			}
		}

		lastClick = time;
	}

	/**
	 * Called when the mouse is moved while a button is pressed.
	 */
	@Override
	protected void mouseClickMove(int x, int y, int button, long timer)
	{
		if (container.isInsideBounds(x, y))
			container.fireMouseEvent(new MouseEvent.Drag(lastMouseX, lastMouseY, x, y, button));
	}

	/**
	 * Called when a mouse button is released.
	 */
	@Override
	protected void mouseMovedOrUp(int x, int y, int button)
	{
		if (inventoryContainer != null)
		{
			if (inventoryContainer.shouldResetDrag(button))
			{
				MalisisGui.sendAction(ActionType.DRAG_RESET, null, 0);
				UISlot.buttonRelased = false;
				return;
			}
			if (inventoryContainer.shouldEndDrag(button))
			{
				MalisisGui.sendAction(ActionType.DRAG_END, null, 0);
				return;
			}
		}

		if (container.isInsideBounds(x, y))
			container.fireMouseEvent(new MouseEvent.Release(x, y, button));
	}

	/**
	 * Called when a button is double clicked.
	 * 
	 * @param x
	 * @param y
	 * @param button
	 */
	protected void doubleClick(int x, int y, int button)
	{
		if (container.isInsideBounds(x, y))
			container.fireMouseEvent(new MouseEvent.DoubleClick(x, y, button));
	}

	/**
	 * Called when a key is pressed on the keyboard.
	 */
	@Override
	protected void keyTyped(char keyChar, int keyCode)
	{
		KeyboardEvent event = new KeyboardEvent(keyChar, keyCode);
		container.fireKeyboardEvent(event);

		if (event.isCancelled())
			return;

		if (keyCode == Keyboard.KEY_ESCAPE || inventoryContainer != null && keyCode == this.mc.gameSettings.keyBindInventory.getKeyCode())
		{
			this.mc.thePlayer.closeScreen();
			this.mc.displayGuiScreen((GuiScreen) null);
			this.mc.setIngameFocus();
			return;
		}
	}

	@Override
	public void setWorldAndResolution(Minecraft minecraft, int width, int height)
	{
		super.setWorldAndResolution(minecraft, width, height);
		screen.setSize(width, height);
		renderer.updateGuiScale();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
		elaspedTime = System.currentTimeMillis() - startTime;

		update(mouseX, mouseY, partialTicks);

		if (guiscreenBackground)
			drawWorldBackground(1);

		if (container.isInsideBounds(mouseX, mouseY) && (lastMouseX != mouseX || lastMouseY != mouseY))
			container.fireMouseEvent(new MouseEvent.Move(lastMouseX, lastMouseY, mouseX, mouseY));

		RenderHelper.enableGUIStandardItemLighting();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_DEPTH_TEST);

		renderer.drawScreen(container, mouseX, mouseY, partialTicks);

		if (inventoryContainer != null)
		{
			ItemStack itemStack = inventoryContainer.getPickedItemStack();
			if (itemStack != null)
				renderer.renderPickedItemStack(itemStack);
			else
				renderer.drawTooltip();
		}

		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_DEPTH_TEST);

		lastMouseX = mouseX;
		lastMouseY = mouseY;
	}

	public void update(int mouseX, int mouseY, float partialTick)
	{

	}

	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}

	/**
	 * Display this <code>MalisisGui</code>
	 */
	public void display()
	{
		Minecraft.getMinecraft().displayGuiScreen(this);
	}

	/**
	 * Get the current <code>MalisisGui</code> displayed.
	 * 
	 * @return null if no GUI being displayed or if not a <code>MalisisGui</code>
	 */
	public static MalisisGui currentGui()
	{
		GuiScreen gui = Minecraft.getMinecraft().currentScreen;
		if (gui instanceof MalisisGui)
			return (MalisisGui) gui;
		return null;
	}

	/**
	 * Sends a GUI action to the server.
	 * 
	 * @param action
	 * @param slot
	 * @param code
	 */
	public static void sendAction(ActionType action, MalisisSlot slot, int code)
	{
		if (action == null || currentGui() == null || currentGui().inventoryContainer == null)
			return;

		int slotNumber = slot != null ? slot.slotNumber : 0;
		boolean playerInv = slot instanceof PlayerInventorySlot;

		currentGui().inventoryContainer.handleAction(action, slotNumber, code, playerInv);
		InventoryActionMessage.sendAction(action, slotNumber, code, playerInv);
	}

	/**
	 * Set the itemStack currently hovered by the cursor.
	 * 
	 * @param itemStack
	 */
	public static void setHoveredItemStack(ItemStack itemStack)
	{
		currentGui().hoveredItemStack = itemStack;
	}

	/**
	 * Set the tooltip to be drawn
	 * 
	 * @param tooltip
	 */
	public static void setTooltip(UITooltip tooltip)
	{
		currentGui().renderer.setTooltip(tooltip);
	}
}
