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
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.malisis.core.client.gui.event.KeyboardEvent;
import net.malisis.core.client.gui.event.MouseEvent;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.preset.ShapePreset;
import net.malisis.core.util.MouseButton;
import net.minecraft.client.renderer.OpenGlHelper;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.google.common.eventbus.Subscribe;

/**
 * UICheckBox
 * 
 * @author PaleoCrafter
 */
public class UICheckBox extends UIComponent<UICheckBox>
{
	private GuiIcon checkboxBackground = new GuiIcon(242, 32, 10, 10);
	private GuiIcon checkboxBackgroundDisabled = checkboxBackground.offset(10, 0);
	private GuiIcon checkBoxDisabled = new GuiIcon(242, 42, 12, 10);
	private GuiIcon checkBoxChecked = checkBoxDisabled.offset(0, 10);
	private GuiIcon checkBoxHovered = checkBoxDisabled.offset(12, 0);
	private UILabel label;
	private boolean checked;

	public UICheckBox(String label)
	{
		if (label != null && !label.equals(""))
		{
			this.label = new UILabel(label);
			this.label.setPosition(x + 14, y + 2);
			width = this.label.getWidth() + 2;
		}

		width += 11;
		height = 10;

		checkboxBackground = new GuiIcon(242, 32, 10, 10);
		checkBoxDisabled = new GuiIcon(242, 42, 12, 10);
	}

	public UICheckBox()
	{
		this(null);
	}

	@Override
	public void setParent(UIContainer parent)
	{
		super.setParent(parent);
		if (label != null)
			label.setParent(parent);
	}

	@Override
	public UICheckBox setPosition(int x, int y)
	{
		super.setPosition(x, y);
		if (label != null)
			label.setPosition(x + 14, y + 2);
		return this;
	}

	public boolean isChecked()
	{
		return this.checked;
	}

	public UICheckBox setChecked(boolean checked)
	{
		this.checked = checked;
		return this;
	}

	@Override
	public GuiIcon getIcon(int face)
	{
		return null;
	}

	@Override
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		RenderParameters rp = new RenderParameters();
		rp.icon.set(isDisabled() ? checkboxBackgroundDisabled : checkboxBackground);
		Shape shape = ShapePreset.GuiElement(10, 10).translate(1, 0, 0);
		renderer.drawShape(shape, rp);

		renderer.next();

		// draw the white shade over the slot
		if (hovered)
		{
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			GL11.glShadeModel(GL11.GL_SMOOTH);

			rp = new RenderParameters();
			rp.colorMultiplier.set(0xFFFFFF);
			rp.alpha.set(80);
			rp.useTexture.set(false);

			shape = ShapePreset.GuiElement(8, 8).translate(2, 1, 0);
			renderer.drawShape(shape, rp);
			renderer.next();

			GL11.glShadeModel(GL11.GL_FLAT);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_ALPHA_TEST);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
		}

		if (label != null)
		{
			label.drawBackground(renderer, mouseX, mouseY, partialTick);
			renderer.next();
			label.drawForeground(renderer, mouseX, mouseY, partialTick);
		}
	}

	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		if (checked)
		{
			GL11.glEnable(GL11.GL_BLEND);
			RenderParameters rp = new RenderParameters();
			rp.icon.set(checkBoxChecked);
			if (isDisabled())
				rp.icon.set(checkBoxDisabled);
			else if (hovered || (label != null && label.isHovered()))
				rp.icon.set(checkBoxHovered);
			Shape shape = ShapePreset.GuiElement(12, 10);
			renderer.drawShape(shape, rp);
		}
	}

	@Subscribe
	public void onButtonRelease(MouseEvent.Release event)
	{
		if (event.getButton() == MouseButton.LEFT)
		{
			if (fireEvent(new CheckedEvent(this, !this.checked)))
				checked = !checked;
		}
	}

	@Subscribe
	public void onKeyTyped(KeyboardEvent event)
	{
		if (!this.focused)
			return;

		if (event.getKeyCode() == Keyboard.KEY_SPACE)
		{
			if (fireEvent(new CheckedEvent(this, !this.checked)))
				checked = !checked;
		}
	}

	@Override
	public String toString()
	{
		return this.getClass().getName() + "[ text=" + (label != null ? label.getText() : "") + ", checked=" + this.checked + ", "
				+ this.getPropertyString() + " ]";
	}

	public class CheckedEvent extends ComponentEvent<UICheckBox>
	{
		private boolean checked;

		public CheckedEvent(UICheckBox component, boolean checked)
		{
			super(component);
			this.checked = checked;
		}

		public boolean getNewState()
		{
			return checked;
		}

	}

}
