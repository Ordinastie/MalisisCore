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

import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.UISlot;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.event.GuiEvent;
import net.malisis.core.client.gui.event.KeyboardEvent;
import net.malisis.core.client.gui.event.MouseEvent;
import net.malisis.core.inventory.MalisisInventoryContainer;
import net.malisis.core.inventory.MalisisInventoryContainer.ActionType;
import net.malisis.core.inventory.MalisisSlot;
import net.malisis.core.packet.InventoryActionMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

/**
 * GuiScreenProxy
 *
 * @author Ordinastie
 */
public class MalisisGui extends GuiScreen
{
	public static GuiTexture BLOCK_TEXTURE = new GuiTexture(TextureMap.locationBlocksTexture);
	public static GuiTexture ITEM_TEXTURE = new GuiTexture(TextureMap.locationItemsTexture);
	/**
	 * Renderer drawing the components.
	 */
	protected GuiRenderer renderer;
	/**
	 * Top level container which hold the user components. Spans across the whole screen.
	 */
	private UIContainer screen;
	/**
	 * Determines if the screen should be darkened when the GUI is opened.
	 */
	protected boolean guiscreenBackground = true;
	/**
	 * Last known position of the mouse.
	 */
	protected int lastMouseX, lastMouseY;
	/**
	 *
	 */
	protected long lastClickButton = -1;
	/**
	 * How long since last click.
	 */
	protected long lastClickTime = 0;
	/**
	 * Inventory container that handles the inventories and slots actions.
	 */
	protected MalisisInventoryContainer inventoryContainer;
	/**
	 * Currently hovered child component
	 */
	protected UIComponent hoveredComponent;
	/**
	 * Currently focused child component
	 */
	protected UIComponent focusedComponent;

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
		this.screen = new UIContainer(this);
		this.screen.clipContent = false;
		startTime = System.currentTimeMillis();
		Keyboard.enableRepeatEvents(true);
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
	 * @return the defaultGuiTexture
	 */
	public GuiTexture getGuiTexture()
	{
		return renderer.getGuiTexture();
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
		screen.add(container);
	}

	public void clearScreen()
	{
		screen.removeAll();
	}

	public UIComponent getComponentAt(int x, int y)
	{
		UIComponent component = screen.getComponentAt(x, y);
		return component == screen ? null : component;
	}

	protected boolean fireEvent(GuiEvent event)
	{
		if (event instanceof MouseEvent)
		{
			MouseEvent me = (MouseEvent) event;
			UIComponent component = getComponentAt(me.getX(), me.getY());
			if (component != null)
			{
				if (me instanceof MouseEvent.Press)
					component.setFocused(true);
				else if (me instanceof MouseEvent.Move)
					component.setHovered(true);
				component.fireMouseEvent(me);
			}
			else
				setHoveredComponent(null, false);
		}
		else if (event instanceof KeyboardEvent)
			screen.fireKeyboardEvent((KeyboardEvent) event);
		return !event.isCancelled();
	}

	@Override
	public void handleMouseInput()
	{
		super.handleMouseInput();

		int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
		int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

		if (lastMouseX != mouseX || lastMouseY != mouseY)
			fireEvent(new MouseEvent.Move(lastMouseX, lastMouseY, mouseX, mouseY));

		lastMouseX = mouseX;
		lastMouseY = mouseY;

		int delta = Mouse.getEventDWheel();
		if (delta == 0)
			return;
		else if (delta > 1)
			delta = 1;
		else if (delta < -1)
			delta = -1;

		fireEvent(new MouseEvent.ScrollWheel(mouseX, mouseY, delta));

	}

	/**
	 * Called when a mouse button is pressed down.
	 */
	@Override
	protected void mouseClicked(int x, int y, int button)
	{
		long time = System.currentTimeMillis();
		if (button == lastClickButton && time - lastClickTime < 250)
		{
			doubleClick(x, y, button);
			lastClickTime = 0;
			return;
		}

		if (screen.isInsideBounds(x, y))
			fireEvent(new MouseEvent.Press(x, y, button));
		else
		{
			setFocusedComponent(null, true);
			if (inventoryContainer != null && inventoryContainer.getPickedItemStack() != null)
			{
				ActionType action = button == 1 ? ActionType.DROP_ONE : ActionType.DROP_STACK;
				MalisisGui.sendAction(action, null, button);
			}
		}

		lastClickTime = time;
		lastClickButton = button;
	}

	/**
	 * Called when the mouse is moved while a button is pressed.
	 */
	@Override
	protected void mouseClickMove(int x, int y, int button, long timer)
	{
		if (focusedComponent != null)
			focusedComponent.fireMouseEvent(new MouseEvent.Drag(lastMouseX, lastMouseY, x, y, button));
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

		fireEvent(new MouseEvent.Release(x, y, button));
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
		fireEvent(new MouseEvent.DoubleClick(x, y, button));
	}

	/**
	 * Called when a key is pressed on the keyboard.
	 */
	@Override
	protected void keyTyped(char keyChar, int keyCode)
	{
		KeyboardEvent event = new KeyboardEvent(keyChar, keyCode);
		fireEvent(event);

		if (event.isCancelled())
			return;

		if (keyCode == Keyboard.KEY_ESCAPE || inventoryContainer != null && keyCode == this.mc.gameSettings.keyBindInventory.getKeyCode())
		{
			close();
		}
	}

	@Override
	public void setWorldAndResolution(Minecraft minecraft, int width, int height)
	{
		int factor = renderer.updateGuiScale();
		if (renderer.isIgnoreScale())
		{
			width *= factor;
			height *= factor;
		}

		super.setWorldAndResolution(minecraft, width, height);
		screen.setSize(width, height);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
		elaspedTime = System.currentTimeMillis() - startTime;

		//if we ignore scaling, use real mouse position on screen
		if (renderer.isIgnoreScale())
		{
			mouseX = Mouse.getX();
			mouseY = this.height - Mouse.getY() - 1;
		}

		update(mouseX, mouseY, partialTicks);

		if (guiscreenBackground)
			drawWorldBackground(1);

		RenderHelper.enableGUIStandardItemLighting();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_LIGHTING);

		renderer.drawScreen(screen, mouseX, mouseY, partialTicks);

		if (inventoryContainer != null)
		{
			ItemStack itemStack = inventoryContainer.getPickedItemStack();
			if (itemStack != null)
				renderer.renderPickedItemStack(itemStack);
			else if (hoveredComponent != null) //do not draw the tooltip if an itemStack is picked up
				renderer.drawTooltip(hoveredComponent.getTooltip());
		}
		else if (hoveredComponent != null)
			renderer.drawTooltip(hoveredComponent.getTooltip());

		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
	}

	/**
	 * Called every frame.
	 *
	 * @param mouseX
	 * @param mouseY
	 * @param partialTick
	 */
	public void update(int mouseX, int mouseY, float partialTick)
	{}

	/**
	 * Called from TE when TE is updated. Override this method when you want to change displayed informations when the TileEntity changes.
	 */
	public void updateGui()
	{}

	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}

	/**
	 * Displays this <code>MalisisGui</code>
	 */
	public void display()
	{
		Minecraft.getMinecraft().displayGuiScreen(this);
	}

	/**
	 * Closes this <code>MalisisGui</code>
	 */
	public void close()
	{
		Keyboard.enableRepeatEvents(false);
		this.mc.thePlayer.closeScreen();
		this.mc.displayGuiScreen((GuiScreen) null);
		this.mc.setIngameFocus();
		return;
	}

	@Override
	public void onGuiClosed()
	{
		if (inventoryContainer != null)
			inventoryContainer.onContainerClosed(this.mc.thePlayer);
	}

	/**
	 * Gets the current <code>MalisisGui</code> displayed.
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

		int inventoryId = slot != null ? slot.getInventoryId() : 0;
		int slotNumber = slot != null ? slot.slotNumber : 0;

		currentGui().inventoryContainer.handleAction(action, inventoryId, slotNumber, code);
		InventoryActionMessage.sendAction(action, inventoryId, slotNumber, code);
	}

	/**
	 * Sets the hovered state for a component. If a component is currently hovered, it will be "unhovered" first.
	 *
	 * @param component
	 * @param hovered
	 * @return
	 */
	public static boolean setHoveredComponent(UIComponent component, boolean hovered)
	{
		MalisisGui gui = currentGui();
		if (gui == null)
			return false;

		if (gui.hoveredComponent == component)
		{
			if (!hovered)
			{
				gui.hoveredComponent = null;
				return true;
			}
			return false;
		}

		if (gui.hoveredComponent != null)
			gui.hoveredComponent.setHovered(false);

		gui.hoveredComponent = component;
		return true;
	}

	public static UIComponent getFocusedComponent()
	{
		return currentGui() != null ? currentGui().focusedComponent : null;
	}

	public static boolean setFocusedComponent(UIComponent component, boolean focused)
	{
		MalisisGui gui = currentGui();
		if (gui == null)
			return false;

		if (gui.focusedComponent == component)
		{
			if (!focused)
			{
				gui.focusedComponent = null;
				return true;
			}
			return false;
		}

		if (gui.focusedComponent != null && component != null)
			gui.focusedComponent.setFocused(false);

		gui.focusedComponent = component;
		return true;
	}
}
