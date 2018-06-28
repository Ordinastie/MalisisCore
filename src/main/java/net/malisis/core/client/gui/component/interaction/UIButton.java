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

import java.util.function.Supplier;

import org.lwjgl.input.Keyboard;

import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.content.IContent;
import net.malisis.core.client.gui.component.content.IContentHolder;
import net.malisis.core.client.gui.element.Size;
import net.malisis.core.client.gui.element.position.Position;
import net.malisis.core.client.gui.element.position.Position.IPosition;
import net.malisis.core.client.gui.render.GuiIcon;
import net.malisis.core.client.gui.render.shape.GuiShape;
import net.malisis.core.client.gui.text.GuiText;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.util.MouseButton;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.text.TextFormatting;

/**
 * UIButton
 *
 * @author Ordinastie, PaleoCrafter
 */
public class UIButton extends UIComponent implements IContentHolder
{
	/** The {@link FontOptions} to use by default for the {@link UIButton} content. */
	protected final FontOptions fontOptions = FontOptions	.builder()
															.color(0xFFFFFF)
															.shadow()
															.when(this::isHovered)
															.color(0xFFFFA0)
															.when(this::isDisabled)
															.color(0xCCCCCC)
															.build();

	protected IPosition offsetPosition = Position.of(this).x(() -> isPressed() ? 1 : 0).y(() -> isPressed() ? 1 : 0).build();
	protected IPosition contentPosition = null;

	/** Content used for this {@link UIButton}. */
	protected IContent content;
	/** Whether this {@link UIButton} is currently being pressed. */
	protected boolean isPressed = false;

	/** Action to execute when the button is clicked. */
	protected Runnable action;

	/**
	 * Instantiates a new {@link UIButton}.
	 */
	public UIButton()
	{
		setAutoSize();
		setBackground(GuiShape.builder(this).border(5).icon((Supplier<GuiIcon>) () -> {
			if (isDisabled())
				return GuiIcon.BUTTON_DISABLED;
			if (isHovered())
				return isPressed() ? GuiIcon.BUTTON_HOVER_PRESSED : GuiIcon.BUTTON_HOVER;
			return GuiIcon.BUTTON;
		}).build());
		setForeground(this::content);
	}

	/**
	 * Instantiates a new {@link UIButton} with specified label.
	 *
	 * @param text the text
	 */
	public UIButton(String text)
	{
		this();
		setText(text);
	}

	/**
	 * Instantiates a new {@link UIButton} with specified content.
	 *
	 * @param content the content
	 */
	public UIButton(UIComponent content)
	{
		this();
		setContent(content);
	}

	//#region Getters/Setters
	/**
	 * Sets the content for this {@link UIButton}.
	 *
	 * @param content the content
	 */
	public void setContent(IContent content)
	{
		this.content = content;
		content.setParent(this);
		content.setPosition(Position.of(content).centered().middleAligned().build().plus(offsetPosition));
	}

	public void setText(String text)
	{
		GuiText gt = GuiText.of(text, fontOptions);
		setContent(gt);
	}

	/**
	 * Gets the {@link UIComponent} used as content for this {@link UIButton}.
	 *
	 * @return the content component
	 */
	@Override
	public IContent content()
	{
		return content;
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
		setSize(Size.sizeOfContent(this, 10, 10));
		return this;
	}

	public UIButton onClick(Runnable action)
	{
		this.action = action;
		return this;
	}

	public FontOptions defaultFontOptions()
	{
		return fontOptions;
	}

	//#end Getters/Setters
	protected void executeAction()
	{
		MalisisGui.playSound(SoundEvents.UI_BUTTON_CLICK);
		if (action != null)
			action.run();
	}

	@Override
	public boolean onClick()
	{
		executeAction();
		return true;
	}

	@Override
	public boolean onButtonPress(MouseButton button)
	{
		if (button == MouseButton.LEFT)
			isPressed = true;
		return super.onButtonPress(button);
	}

	@Override
	public boolean onButtonRelease(MouseButton button)
	{
		if (button == MouseButton.LEFT)
			isPressed = false;
		return super.onButtonRelease(button);
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
	public String getPropertyString()
	{
		return "[" + TextFormatting.GREEN + content + TextFormatting.RESET + "] " + super.getPropertyString();
	}

}
