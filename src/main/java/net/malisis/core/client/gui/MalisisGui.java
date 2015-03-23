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

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;

import net.malisis.core.MalisisCore;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.UISlot;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.inventory.MalisisInventoryContainer;
import net.malisis.core.inventory.MalisisInventoryContainer.ActionType;
import net.malisis.core.inventory.MalisisSlot;
import net.malisis.core.inventory.message.InventoryActionMessage;
import net.malisis.core.renderer.animation.Animation;
import net.malisis.core.renderer.animation.AnimationRenderer;
import net.malisis.core.util.MouseButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

/**
 * GuiScreenProxy
 *
 * @author Ordinastie
 */
public abstract class MalisisGui extends GuiScreen
{
	public static GuiTexture BLOCK_TEXTURE = new GuiTexture(TextureMap.locationBlocksTexture);
	public static GuiTexture ITEM_TEXTURE = new GuiTexture(TextureMap.locationItemsTexture);

	/** Whether or not to cancel the next gui close event. */
	public static boolean cancelClose = false;

	/** Renderer drawing the components. */
	protected GuiRenderer renderer;
	/** Top level container which hold the user components. Spans across the whole screen. */
	private UIContainer screen;
	/** Determines if the screen should be darkened when the GUI is opened. */
	protected boolean guiscreenBackground = true;
	/** Last known position of the mouse. */
	protected int lastMouseX, lastMouseY;
	/** Last clicked button */
	protected long lastClickButton = -1;
	/** How long since last click. */
	protected long lastClickTime = 0;
	/** Inventory container that handles the inventories and slots actions. */
	protected MalisisInventoryContainer inventoryContainer;
	/** {@link AnimationRenderer} */
	private AnimationRenderer ar;
	/** Currently hovered child component. */
	protected UIComponent hoveredComponent;
	/** Currently focused child component */
	protected UIComponent focusedComponent;
	/** Whether this GUI has been constructed */
	protected boolean constructed = false;

	protected MalisisGui()
	{
		this.renderer = new GuiRenderer();
		this.screen = new UIContainer(this);
		this.ar = new AnimationRenderer();
		this.ar.autoClearAnimations();
		this.screen.setClipContent(false);
		Keyboard.enableRepeatEvents(true);
	}

	/**
	 * Called before display() if this {@link MalisisGui} is not constructed yet.<br>
	 * Called when Ctrl+R is pressed to rebuild the GUI.
	 */
	public abstract void construct();

	/**
	 * Sets the {@link MalisisInventoryContainer} for this {@link MalisisGui}.
	 *
	 * @param container the inventory container
	 */
	public void setInventoryContainer(MalisisInventoryContainer container)
	{
		this.inventoryContainer = container;
	}

	/**
	 * Gets the {@link MalisisInventoryContainer} for this {@link MalisisGui}.
	 *
	 * @return inventory container
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
		return renderer.getDefaultTexture();
	}

	/**
	 * Gets elapsed time since the GUI was opened.
	 *
	 * @return the time
	 */
	public long getElapsedTime()
	{
		return ar.getElapsedTime();
	}

	/**
	 * Adds container to the screen.
	 *
	 * @param component the component
	 */
	protected void addToScreen(UIComponent component)
	{
		screen.add(component);
		component.onAddedToScreen();
	}

	/**
	 * Removes all the components from the screen
	 */
	public void clearScreen()
	{
		screen.removeAll();
	}

	/**
	 * Gets the {@link UIContainer} at the specified coordinates inside this {@link MalisisGui}.
	 *
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @return the component, null if component is {@link #screen}
	 */
	public UIComponent getComponentAt(int x, int y)
	{
		UIComponent component = screen.getComponentAt(x, y);
		return component == screen ? null : component;
	}

	/**
	 * Called every frame to handle mouse input.
	 */
	@Override
	public void handleMouseInput()
	{
		try
		{
			super.handleMouseInput();

			int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
			int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

			if (lastMouseX != mouseX || lastMouseY != mouseY)
			{
				UIComponent component = getComponentAt(mouseX, mouseY);
				if (component != null && !component.isDisabled())
				{
					component.onMouseMove(lastMouseX, lastMouseY, mouseX, mouseY);
					component.setHovered(true);
				}
				else
					setHoveredComponent(null, false);
			}

			lastMouseX = mouseX;
			lastMouseY = mouseY;

			int delta = Mouse.getEventDWheel();
			if (delta == 0)
				return;
			else if (delta > 1)
				delta = 1;
			else if (delta < -1)
				delta = -1;

			UIComponent component = getComponentAt(mouseX, mouseY);
			if (component != null && !component.isDisabled())
			{
				component.onScrollWheel(mouseX, mouseY, delta);
			}
		}
		catch (Exception e)
		{
			MalisisCore.message("A problem occured : " + e.getClass().getSimpleName() + ": " + e.getMessage());
			e.printStackTrace(new PrintStream(new FileOutputStream(FileDescriptor.out)));
		}
	}

	/**
	 * Called when a mouse button is pressed down.
	 */
	@Override
	protected void mouseClicked(int x, int y, int button)
	{
		try
		{
			long time = System.currentTimeMillis();

			UIComponent component = getComponentAt(x, y);
			if (component != null && !component.isDisabled())
			{
				component.onButtonPress(x, y, MouseButton.getButton(button));
				//double click

				if (button == lastClickButton && time - lastClickTime < 250)
				{
					component.onDoubleClick(x, y, MouseButton.getButton(button));
					lastClickTime = 0;
				}
				component.setFocused(true);
			}
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
		catch (Exception e)
		{
			MalisisCore.message("A problem occured : " + e.getClass().getSimpleName() + ": " + e.getMessage());
			e.printStackTrace(new PrintStream(new FileOutputStream(FileDescriptor.out)));
		}
	}

	/**
	 * Called when the mouse is moved while a button is pressed.
	 */
	@Override
	protected void mouseClickMove(int x, int y, int button, long timer)
	{
		try
		{
			if (focusedComponent != null)
				focusedComponent.onDrag(lastMouseX, lastMouseY, x, y, MouseButton.getButton(button));
		}
		catch (Exception e)
		{
			MalisisCore.message("A problem occured : " + e.getClass().getSimpleName() + ": " + e.getMessage());
			e.printStackTrace(new PrintStream(new FileOutputStream(FileDescriptor.out)));
		}

	}

	/**
	 * Called when a mouse button is released.
	 */
	@Override
	protected void mouseReleased(int x, int y, int button)
	{
		try
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

			UIComponent component = getComponentAt(x, y);
			if (component != null && !component.isDisabled())
			{
				MouseButton mb = MouseButton.getButton(button);
				component.onButtonRelease(x, y, mb);
				if (component == focusedComponent)
				{
					if (mb == MouseButton.LEFT)
						component.onClick(x, y);
					else if (mb == MouseButton.RIGHT)
						component.onRightClick(x, y);
				}
			}
		}
		catch (Exception e)
		{
			MalisisCore.message("A problem occured : " + e.getClass().getSimpleName() + ": " + e.getMessage());
			e.printStackTrace(new PrintStream(new FileOutputStream(FileDescriptor.out)));
		}
	}

	/**
	 * Called when a key is pressed on the keyboard.
	 */
	@Override
	protected void keyTyped(char keyChar, int keyCode)
	{
		try
		{
			if (focusedComponent != null && focusedComponent.onKeyTyped(keyChar, keyCode))
				return;

			if (hoveredComponent != null && hoveredComponent.onKeyTyped(keyChar, keyCode))
				return;

			if (isGuiCloseKey(keyCode))
				close();

			if (!MalisisCore.isObfEnv && isCtrlKeyDown() && keyCode == Keyboard.KEY_R && currentGui() != null)
			{
				screen.removeAll();
				construct();
			}
		}
		catch (Exception e)
		{
			MalisisCore.message("A problem occured : " + e.getClass().getSimpleName() + ": " + e.getMessage());
			e.printStackTrace(new PrintStream(new FileOutputStream(FileDescriptor.out)));
		}

	}

	/**
	 * Called when game resolution changes.
	 */
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

	/**
	 * Draws this {@link MalisisGui}.
	 */
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
		ar.animate();

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

		//		if (hoveredComponent != null)
		//			renderer.drawString(hoveredComponent.toString(), 5, 5, 0, 0xFFFFFF, true);

		if (inventoryContainer != null)
		{
			ItemStack itemStack = inventoryContainer.getPickedItemStack();
			if (itemStack != null)
				renderer.renderPickedItemStack(itemStack);
			else if (hoveredComponent != null && hoveredComponent.isHovered()) //do not draw the tooltip if an itemStack is picked up
				renderer.drawTooltip(hoveredComponent.getTooltip());
		}
		else if (hoveredComponent != null && hoveredComponent.isHovered())
			renderer.drawTooltip(hoveredComponent.getTooltip());

		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_DEPTH_TEST);

	}

	/**
	 * Called every frame.
	 *
	 * @param mouseX the mouse x
	 * @param mouseY the mouse y
	 * @param partialTick the partial tick
	 */
	public void update(int mouseX, int mouseY, float partialTick)
	{}

	/**
	 * Called from TE when TE is updated. Override this method when you want to change displayed informations when the TileEntity changes.
	 */
	public void updateGui()
	{}

	public void animate(Animation animation)
	{
		animate(animation, 0);
	}

	public void animate(Animation animation, int delay)
	{
		animation.setDelay((int) ar.getElapsedTicks() + delay);
		ar.addAnimation(animation);
	}

	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}

	/**
	 * Displays this {@link MalisisGui}.
	 */
	public void display()
	{
		display(false);
	}

	/**
	 * Display this {@link MalisisGui}.
	 *
	 * @param cancelClose the wether or not to cancel the next Gui close event (used for when the GUI is opened from command)
	 */
	public void display(boolean cancelClose)
	{
		try
		{
			if (!constructed)
			{
				construct();
				constructed = true;
			}

			MalisisGui.cancelClose = cancelClose;
			Minecraft.getMinecraft().displayGuiScreen(this);
		}
		catch (Exception e)
		{
			MalisisCore.message("A problem occured : " + e.getClass().getSimpleName() + ": " + e.getMessage());
			e.printStackTrace(new PrintStream(new FileOutputStream(FileDescriptor.out)));
		}
	}

	/**
	 * Closes this {@link MalisisGui}.
	 */
	public void close()
	{
		Keyboard.enableRepeatEvents(false);
		if (this.mc.thePlayer != null)
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
	 * Gets the current {@link MalisisGui} displayed.
	 *
	 * @return null if no GUI being displayed or if not a {@link MalisisGui}
	 */
	public static MalisisGui currentGui()
	{
		return currentGui(MalisisGui.class);
	}

	/**
	 * Gets the current {@link MalisisGui} of the specified type displayed.<br>
	 * If the current gu is not of <i>type</i>, null is returned.
	 *
	 * @param <T> the generic type
	 * @param type the type
	 * @return the t
	 */
	public static <T extends MalisisGui> T currentGui(Class<T> type)
	{
		GuiScreen gui = Minecraft.getMinecraft().currentScreen;
		if (gui == null || !(gui instanceof MalisisGui))
			return null;
		try
		{
			return type.cast(gui);
		}
		catch (ClassCastException e)
		{
			return null;
		}
	}

	/**
	 * Sends a GUI action to the server.
	 *
	 * @param action the action
	 * @param slot the slot
	 * @param code the keyboard code
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
	 * @return the currently hovered {@link UIComponent}. null if there is no current GUI.
	 */
	public static UIComponent getHoveredComponent()
	{
		return currentGui() != null ? currentGui().hoveredComponent : null;
	}

	/**
	 * Sets the hovered state for a {@link UIComponent}. If a <code>UIComponent</code> is currently hovered, it will be "unhovered" first.
	 *
	 * @param component the component that gets his state changed
	 * @param hovered the hovered state
	 * @return true, if the state was changed
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

	/**
	 * Gets the currently focused {@link UIComponent}
	 *
	 * @return the component
	 */
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

		if (gui.focusedComponent != null)
			gui.focusedComponent.setFocused(false);

		gui.focusedComponent = component;
		return true;
	}

	public static void playSound(String name)
	{
		playSound(name, 1.0F);
	}

	public static void playSound(String name, float level)
	{
		Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.createPositionedSoundRecord(new ResourceLocation(name), level));
	}

	public static boolean isGuiCloseKey(int keyCode)
	{
		MalisisGui gui = currentGui();
		return keyCode == Keyboard.KEY_ESCAPE
				|| (gui != null && gui.inventoryContainer != null && keyCode == gui.mc.gameSettings.keyBindInventory.getKeyCode());
	}
}
