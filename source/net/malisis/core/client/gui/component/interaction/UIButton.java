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

import net.malisis.core.client.gui.GuiIcon;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.malisis.core.client.gui.event.MouseEvent;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.preset.ShapePreset;
import net.malisis.core.util.MouseButton;

import com.google.common.eventbus.Subscribe;

/**
 * UIButton
 * 
 * @author PaleoCrafter
 */
public class UIButton extends UIComponent<UIButton>
{
	//@formatter:off
	public static GuiIcon[] iconButton = new GuiIcon[] { 			new GuiIcon(0,		20, 	5, 		20),
																	new GuiIcon(5,		20, 	15, 	20),
																	new GuiIcon(195, 	20, 	5, 		20)};
	public static GuiIcon[] iconButtonHovered = new GuiIcon[] { 	iconButton[0].offset(0,  20),
																	iconButton[1].offset(0,  20),
																	iconButton[2].offset(0,  20) };
	public static GuiIcon[] iconButtonDisabled = new GuiIcon[] { 	iconButton[0].offset(0,  -20),
																	iconButton[1].offset(0,  -20),
																	iconButton[2].offset(0,  -20) };
	//@formatter:on
	private UILabel label = new UILabel();
	private boolean autoWidth = true;

	// this.mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
	public UIButton(String text, int width)
	{
		setText(text);
		label.setColor(0xFFFFFF);
		label.setDrawShadow(true);
		setSize(width);
	}

	public UIButton(String text)
	{
		this(text, 60);
	}

	public UIButton()
	{
		this(null, 60);
	}

	/**
	 * Sets the text of this <code>UIButton</code>. If a width of 0 was previously set, it will be recalculated for this text.
	 * 
	 * @param text
	 * @return this <code>UIButton</code>
	 */
	public UIButton setText(String text)
	{
		label.setText(text);
		setSize(autoWidth ? 0 : width);
		return this;
	}

	/**
	 * Sets the width of this <code>UIButton</code>. Height is fixed 20.
	 * 
	 * @param width
	 * @return this <code>UIButton</code>
	 */
	public UIButton setSize(int width)
	{
		autoWidth = width == 0;
		int extraWidth = label.getWidth() % 2 == 0 ? 6 : 7;
		this.width = Math.max(width, label.getWidth() + extraWidth);
		this.height = 20;
		return this;
	}

	/**
	 * Sets the width of this <code>UIButton</code>. Height parameter is ignored as it's fixed 20.
	 * 
	 * @param width
	 * @param height ignored
	 * @return this <code>UILabel</code>
	 */
	@Override
	public UIButton setSize(int width, int height)
	{
		return setSize(width);
	}

	@Override
	public UIButton setZIndex(int zIndex)
	{
		super.setZIndex(zIndex);
		label.setZIndex(zIndex + 1);
		return this;
	}

	@Override
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		GuiIcon[] icons = isDisabled() ? iconButtonDisabled : (hovered ? iconButtonHovered : iconButton);
		Shape shape = ShapePreset.GuiXResizable(width, height);
		renderer.drawShape(shape, icons);
	}

	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		int x = (width - label.getWidth()) / 2;
		int y = (height - label.getHeight() + 2) / 2;

		label.setPosition(screenX() + x, screenY() + y);
		label.draw(renderer, mouseX, mouseY, partialTick);
	}

	@Subscribe
	public void onClick(MouseEvent.Release event)
	{
		if (event.getButton() == MouseButton.LEFT)
			fireEvent(new ClickedEvent(this, event));
	}

	@Override
	public String toString()
	{
		return this.getClass().getName() + "[ text=" + label.getText() + ", " + this.getPropertyString() + " ]";
	}

	public static class ClickedEvent extends ComponentEvent<UIButton>
	{
		private int x, y;
		private MouseButton button;
		private int buttonCode;

		public ClickedEvent(UIButton component, MouseEvent.Release mouseEvent)
		{
			super(component);
			this.x = mouseEvent.getX();
			this.y = mouseEvent.getY();
			this.button = mouseEvent.getButton();
			this.buttonCode = mouseEvent.getButtonCode();
		}

		public int getX()
		{
			return x;
		}

		public int getY()
		{
			return y;
		}

		public MouseButton getButton()
		{
			return button;
		}

		public int getButtonCode()
		{
			return buttonCode;
		}

	}

}
