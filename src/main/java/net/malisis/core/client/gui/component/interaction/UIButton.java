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

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.element.XYResizableGuiShape;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.malisis.core.client.gui.event.MouseEvent;
import net.malisis.core.client.gui.event.MouseEvent.DoubleClick;
import net.malisis.core.client.gui.event.MouseEvent.Press;
import net.malisis.core.client.gui.event.MouseEvent.Release;
import net.malisis.core.client.gui.icon.GuiIcon;
import net.malisis.core.util.MouseButton;

import com.google.common.eventbus.Subscribe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;

/**
 * UIButton
 *
 * @author Ordinastie, PaleoCrafter
 */
public class UIButton extends UIComponent<UIButton>
{
	protected GuiIcon iconHovered;
	protected GuiIcon iconDisabled;
	protected GuiIcon iconPressed;

	private String text;
	private UIImage image;
	private boolean autoWidth = true;
	private boolean isPressed = false;

	/**
	 * Instantiates a new {@link UIButton}.
	 *
	 * @param gui the gui
	 */
	public UIButton(MalisisGui gui)
	{
		super(gui);
		setSize(60);

		shape = new XYResizableGuiShape();
		icon = gui.getGuiTexture().getXYResizableIcon(0, 20, 200, 20, 5);
		iconHovered = gui.getGuiTexture().getXYResizableIcon(0, 40, 200, 20, 5);
		iconDisabled = gui.getGuiTexture().getXYResizableIcon(0, 0, 200, 20, 5);
		iconPressed = (GuiIcon) gui.getGuiTexture().getXYResizableIcon(0, 40, 200, 20, 5).flip(true, true);
	}

	/**
	 * Instantiates a new {@link UIButton}.
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
	 * Instantiates a new {@link UIButton}.
	 *
	 * @param gui the gui
	 * @param image the image
	 */
	public UIButton(MalisisGui gui, UIImage image)
	{
		this(gui);
		setImage(image);
	}

	/**
	 * Sets the text of this {@link UIButton}. If a width of 0 was previously set, it will be recalculated for this text.
	 *
	 * @param text the text
	 * @return this {@link UIButton}
	 */
	public UIButton setText(String text)
	{
		this.text = text;
		setSize(autoWidth ? 0 : width, height);
		image = null;
		return this;
	}

	/**
	 * Gets the text of this {@link UIButton}.
	 *
	 * @return the text of this {@link UIButton}.
	 */
	public String getText()
	{
		return text;
	}

	/**
	 * Sets the {@link UIImage} for this {@link UIButton}. If a width of 0 was previously set, it will be recalculated for this image.
	 *
	 * @param image the image
	 * @return this {@link UIButton}
	 */
	public UIButton setImage(UIImage image)
	{
		this.image = image;
		image.setParent(this);
		setSize(autoWidth ? 0 : width, height);
		text = null;
		return this;
	}

	/**
	 * Sets the width of this {@link UIButton} with a default height of 20px.
	 *
	 * @param width the width
	 * @return this {@link UIButton}
	 */
	public UIButton setSize(int width)
	{
		return setSize(width, 20);
	}

	/**
	 * Sets the size of this {@link UIButton}.
	 *
	 * @param width the width
	 * @param height the height
	 * @return this {@link UIButton}
	 */
	@Override
	public UIButton setSize(int width, int height)
	{
		autoWidth = width == 0;
		if (image != null)
		{
			int w = image.getRawWidth();
			int h = image.getRawHeight();
			width = Math.max(width, w + 6);
			height = Math.max(height, h + 6);
		}
		else
		{
			int w = GuiRenderer.getStringWidth(text);
			int h = GuiRenderer.getStringHeight();
			width = Math.max(width, w + 6);
			height = Math.max(height, h + 6);
		}

		this.width = width;
		this.height = height;

		return this;
	}

	@Override
	public void setHovered(boolean hovered)
	{
		super.setHovered(hovered);
		if (!hovered)
			isPressed = false;
	}

	@Override
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		final GuiIcon icon;
		if (isDisabled())
		{
			icon = iconDisabled;
		}
		else if (isPressed)
		{
			icon = iconPressed;
		}
		else if (isHovered())
		{
			icon = iconHovered;
		}
		else
		{
			icon = this.icon;
		}
		rp.icon.set(icon);
		renderer.drawShape(shape, rp);
	}

	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		int w = 0;
		int h = 0;
		if (image != null)
		{
			w = image.getWidth();
			h = image.getHeight();
		}
		else
		{
			w = GuiRenderer.getStringWidth(text);
			h = GuiRenderer.getStringHeight();
		}

		int x = (width - w) / 2;
		int y = (height - h) / 2;
		if (isPressed)
		{
			x += 1;
			y += 1;
		}

		if (image != null)
		{
			image.setPosition(x, y);
			image.setZIndex(zIndex);
			image.draw(renderer, mouseX, mouseY, partialTick);
		}
		else
		{
			renderer.drawText(text, x, y, isHovered() ? 0xFFFFA0 : 0xFFFFFF, true);
		}

	}

	@Subscribe
	public void onClick(MouseEvent.ButtonStateEvent event)
	{
		if (event.getButton() != MouseButton.LEFT)
			return;

		if (event instanceof DoubleClick)
			return;

		if (event instanceof Press)
		{
			isPressed = true;
			return;
		}

		if (event instanceof Release && !isPressed)
			return;

		isPressed = false;
		Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
		fireEvent(new ClickEvent(this, (Release) event));
	}

	@Override
	public String getPropertyString()
	{
		return (image != null ? "{" + image + "}" : text) + " , " + super.getPropertyString();
	}

	public static class ClickEvent extends ComponentEvent<UIButton>
	{
		private int x, y;
		private MouseButton button;
		private int buttonCode;

		public ClickEvent(UIButton component, MouseEvent.Release mouseEvent)
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
