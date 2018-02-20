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

import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.element.Size;
import net.malisis.core.client.gui.component.element.Position.AlignedPosition;
import net.malisis.core.client.gui.element.XYResizableGuiShape;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.renderer.icon.GuiIcon;
import net.malisis.core.renderer.icon.provider.GuiIconProvider;
import net.malisis.core.util.MouseButton;
import net.minecraft.init.SoundEvents;

/**
 * UIButton
 *
 * @author Ordinastie, PaleoCrafter
 */
public class UIButton extends UIComponent<UIButton>
{
	private final Size AUTO_SIZE = new AutoSize();

	/** The {@link FontOptions} to use by default for the {@link UILabel} content. */
	protected FontOptions defaultFontOptions = FontOptions.builder().color(0xFFFFFF).shadow().build();
	/** The base {@link FontOptions} of the {@link UILabel} content. */
	protected FontOptions fontOptions = FontOptions.builder().color(0xFFFFA0).shadow().build();
	/** The {@link FontOptions} to use for the {@link UILabel} content when hovered. */
	protected FontOptions hoveredFontOptions = FontOptions.builder().color(0xFFFFA0).shadow().build();
	/** Content used for this {@link UIButton}. */
	protected UIComponent<?> content;
	/** Whether the size of this {@link UIButton} is automatically calculated based on its contents. */
	protected boolean autoSize = true;
	/** Whether this {@link UIButton} is currently being pressed. */
	protected boolean isPressed = false;

	/** The background color of this {@link UIButton}. */
	protected int bgColor = 0xFFFFFF;
	/** Offset for the contents */
	protected int offsetX, offsetY;

	protected GuiIconProvider iconPressedProvider;

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

		setSize(AUTO_SIZE);
		shape = new XYResizableGuiShape();
		iconProvider = new GuiIconProvider(	gui.getGuiTexture().getXYResizableIcon(0, 20, 200, 20, 5),
											gui.getGuiTexture().getXYResizableIcon(0, 40, 200, 20, 5),
											gui.getGuiTexture().getXYResizableIcon(0, 0, 200, 20, 5));

		iconPressedProvider = new GuiIconProvider((GuiIcon) gui.getGuiTexture().getXYResizableIcon(0, 40, 200, 20, 5).flip(true, true));
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
	 * Gets the {@link FontOptions} used when this {@link UIButton} is hovered.<br>
	 * Only used if the content is a {@link UILabel}.
	 *
	 * @return the hovered font options
	 */
	public FontOptions getHoveredFontOptions()
	{
		return hoveredFontOptions;
	}

	/**
	 * Sets the {@link FontOptions} used when this {@link UIButton} is hovered.<br>
	 * Only used if the content is a {@link UILabel}.
	 *
	 * @param hoveredOptions the hovered options to set
	 * @return this {@link UIButton}
	 */
	public UIButton setHoveredFontOptions(FontOptions hoveredOptions)
	{
		this.hoveredFontOptions = hoveredOptions;
		return this;
	}

	/**
	 * Sets the text of this {@link UIButton}.<br>
	 * Create a label for this button.
	 *
	 * @param text the text
	 * @return this {@link UIButton}
	 */
	public UIButton setText(String text)
	{
		UILabel label = new UILabel(getGui(), text);
		label.setFontOptions(defaultFontOptions);
		setContent(label);
		return this;
	}

	/**
	 * Gets the {@link UIComponent} used as content for this {@link UIButton}.
	 *
	 * @return the content component
	 */
	public UIComponent<?> getContent()
	{
		return content;
	}

	/**
	 * Sets the {@link UIImage} for this {@link UIButton}. If a width of 0 was previously set, it will be recalculated for this image.
	 *
	 * @param content the content
	 * @return this {@link UIButton}
	 */
	public UIButton setContent(UIComponent<?> content)
	{
		this.content = content;
		content.setParent(this);
		content.setPosition(new ContentPosition(content));

		//store the fontOptions, to be able to revert back from hoveredOptions
		if (content instanceof UILabel)
			fontOptions = ((UILabel) content).getFontOptions();
		return this;
	}

	/**
	 * Checks if is width is automatically calculated.<br>
	 * If true, the size of this {@link UIButton} will match it's contents
	 *
	 * @return the autoWidth
	 */
	public boolean isAutoSize()
	{
		return size() == AUTO_SIZE;
	}

	/**
	 * Sets whether the size of this {@link UIButton} should be calculated automatically.
	 *
	 * @return the UI button
	 */
	public UIButton setAutoSize()
	{
		setSize(AUTO_SIZE);
		return this;
	}

	/**
	 * Gets the background color of this {@link UIButton}.
	 *
	 * @return the bg color
	 */
	public int getBgColor()
	{
		return bgColor;
	}

	/**
	 * Sets the background color of this {@link UIButton}.
	 *
	 * @param bgColor the bg color
	 * @return the UI button
	 */
	public UIButton setBgColor(int bgColor)
	{
		this.bgColor = bgColor;
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
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		if (isPressed && isHovered())
			rp.iconProvider.set(iconPressedProvider);

		rp.colorMultiplier.set(bgColor);
		renderer.drawShape(shape, rp);
	}

	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		if (content == null)
			return;

		if (content instanceof UILabel)
			((UILabel) content).setFontOptions(isHovered() ? hoveredFontOptions : fontOptions);

		content.draw(renderer, mouseX, mouseY, partialTick);
	}

	@Override
	public String getPropertyString()
	{
		return content + " | " + super.getPropertyString();
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

	private class ContentPosition extends AlignedPosition
	{
		public ContentPosition(UIComponent<?> owner)
		{
			super(owner, Anchor.CENTER | Anchor.MIDDLE, 1, 1);
		}

		@Override
		public int x()
		{
			return super.x() + (isPressed ? 1 : 0);
		}

		@Override
		public int y()
		{
			return super.y() + (isPressed ? 1 : 0);
		}
	}

	private class AutoSize implements Size
	{
		@Override
		public int width()
		{
			return content.size().width() + (content instanceof UILabel ? 6 : 2);
		}

		@Override
		public int height()
		{
			return content.size().height() + (content instanceof UILabel ? 6 : 2);
		}
	}

}
