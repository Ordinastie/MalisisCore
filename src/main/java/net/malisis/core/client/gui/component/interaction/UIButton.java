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

package net.malisis.core.client.gui.component.interaction;

import org.lwjgl.input.Keyboard;

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.IContentComponent;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.element.Position;
import net.malisis.core.client.gui.component.element.Position.IPosition;
import net.malisis.core.client.gui.component.element.Size;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.malisis.core.client.gui.render.ButtonBackground;
import net.malisis.core.client.gui.text.IGuiText;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.util.MouseButton;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.text.TextFormatting;

/**
 * UIButton
 *
 * @author Ordinastie, PaleoCrafter
 */
public class UIButton extends UIComponent<UIButton> implements IContentComponent
{
	/** The {@link FontOptions} to use by default for the {@link UILabel} content. */
	protected FontOptions fontOptions = FontOptions	.builder()
													.color(0xFFFFFF)
													.shadow()
													.when(this::isHovered)
													.color(0xFFFFA0)
													.when(this::isDisabled)
													.color(0xCCCCCC)
													.build();

	protected IPosition contentPosition = new ContentPosition(Position.centered().middleAligned());

	/** Content used for this {@link UIButton}. */
	protected UIComponent<?> content;
	/** Whether this {@link UIButton} is currently being pressed. */
	protected boolean isPressed = false;

	/** Offset for the contents */
	protected int offsetX, offsetY;
	/** Action to execute when the button is clicked. */
	protected Runnable action;

	/**
	 * Instantiates a new {@link UIButton}.
	 *
	 * @param gui the gui
	 */
	public UIButton(MalisisGui gui)
	{
		super(gui);
		setAutoSize();
		setBackground(new ButtonBackground(gui));
	}

	/**
	 * Instantiates a new {@link UIButton} with specified label.
	 *
	 * @param gui the gui
	 * @param text the text
	 */
	public UIButton(MalisisGui gui, String text)
	{
		this(gui);
		setText(text);
	}

	/**
	 * Instantiates a new {@link UIButton} with specified content.
	 *
	 * @param gui the gui
	 * @param content the content
	 */
	public UIButton(MalisisGui gui, UIComponent<?> content)
	{
		this(gui);
		setContent(content);
	}

	//#region Getters/Setters
	/**
	 * Sets the text of this {@link UIButton}.<br>
	 * Create a {@link UILabel} for this button.
	 *
	 * @param text the text
	 */
	@Override
	public void setText(String text)
	{
		boolean setOptions = !(content instanceof IGuiText);
		IContentComponent.super.setText(text);
		if (setOptions)
			setFontOptions(fontOptions);
	}

	/**
	 * Sets the content for this {@link UIButton}.<br>
	 * Note that the position of the content is overwritten with {@link #contentPosition}. For custom content position, use
	 * {@link #setContentPosition(IPosition)}.
	 *
	 * @param content the content
	 */
	@Override
	public void setContent(UIComponent<?> content)
	{
		this.content = content;
		content.setParent(this);
		content.setPosition(contentPosition);
	}

	/**
	 * Gets the {@link UIComponent} used as content for this {@link UIButton}.
	 *
	 * @return the content component
	 */
	@Override
	public UIComponent<?> getContent()
	{
		return content;
	}

	/**
	 * Sets the position for the content.
	 *
	 * @param position the new content position
	 */
	public void setContentPosition(IPosition position)
	{
		contentPosition = new ContentPosition(position);
		if (content != null)
			content.setPosition(contentPosition);
	}

	/**
	 * Checks if this {@link UIButton} is currently being pressed.
	 *
	 * @return true, if is pressed
	 */
	public boolean isPressed()
	{
		return isPressed;
	}

	/**
	 * Sets whether the size of this {@link UIButton} should be calculated automatically.
	 *
	 * @return the UI button
	 */
	public UIButton setAutoSize()
	{
		setSize(Size.contentSize(4, 4));
		return this;
	}

	/**
	 * Gets the text offset of this {@link UIButton}.
	 *
	 * @return the text offset x
	 */
	public int getOffsetX()
	{
		return offsetX;
	}

	/**
	 * Gets the text offset of this {@link UIButton}.
	 *
	 * @return the text offset y
	 */
	public int getOffsetY()
	{
		return offsetY;
	}

	/**
	 * Sets the text offset of this {@link UIButton}.
	 *
	 * @param x the x
	 * @param y the y
	 * @return the UI button
	 */
	public UIButton setOffset(int x, int y)
	{
		offsetX = x;
		offsetY = y;
		return this;
	}

	public UIButton onClick(Runnable action)
	{
		this.action = action;
		return this;
	}

	//#end Getters/Setters
	protected void executeAction()
	{
		MalisisGui.playSound(SoundEvents.UI_BUTTON_CLICK);
		if (action != null)
			action.run();
	}

	@Override
	public boolean onClick(int x, int y)
	{
		executeAction();
		fireEvent(new ClickEvent(this, x, y));
		return true;
	}

	@Override
	public boolean onButtonPress(int x, int y, MouseButton button)
	{
		if (button == MouseButton.LEFT)
			isPressed = true;
		return super.onButtonPress(x, y, button);
	}

	@Override
	public boolean onButtonRelease(int x, int y, MouseButton button)
	{
		if (button == MouseButton.LEFT)
			isPressed = false;
		return super.onButtonRelease(x, y, button);
	}

	@Override
	public boolean onKeyTyped(char keyChar, int keyCode)
	{
		if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER)
		{
			executeAction();
			return true;
		}

		return false;
	}

	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		if (content == null)
			return;
		content.draw(renderer, mouseX, mouseY, partialTick);
	}

	@Override
	public String getPropertyString()
	{
		return "" + TextFormatting.GREEN + content + TextFormatting.RESET + " | " + super.getPropertyString();
	}

	/**
	 * Event fired when a {@link UIButton} is clicked.
	 */
	public static class ClickEvent extends ComponentEvent<UIButton>
	{
		/** Position of the mouse when clicked . */
		private int x, y;

		/**
		 * Instantiates a new {@link ClickEvent}.
		 *
		 * @param component the component
		 * @param x the x coordinate of the mouse
		 * @param y the y coordinate of the mouse
		 */
		public ClickEvent(UIButton component, int x, int y)
		{
			super(component);
			this.x = x;
			this.y = y;
		}

		/**
		 * Gets the x coordinate of the mouse.
		 *
		 * @return the x
		 */
		public int getX()
		{
			return x;
		}

		/**
		 * Gets the y coordinate of the mouse.
		 *
		 * @return the y
		 */
		public int getY()
		{
			return y;
		}
	}

	private class ContentPosition implements IPosition
	{
		private final IPosition pos;

		public ContentPosition(IPosition basePosition)
		{
			pos = basePosition;
		}

		@Override
		public void setOwner(UIComponent<?> component)
		{
			pos.setOwner(component);
		}

		@Override
		public int x()
		{
			return pos.x() + offsetX + (isPressed ? 1 : 0);
		}

		@Override
		public int y()
		{
			return pos.y() + offsetY + (isPressed ? 1 : 0);
		}
	}
}
