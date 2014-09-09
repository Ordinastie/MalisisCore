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
import net.malisis.core.client.gui.element.SimpleGuiShape;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.malisis.core.client.gui.event.KeyboardEvent;
import net.malisis.core.client.gui.event.MouseEvent;
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
	private GuiIcon checkboxBackgroundDisabled = checkboxBackground.offsetCopy(10, 0);
	private GuiIcon checkBoxDisabled = new GuiIcon(242, 42, 12, 10);
	private GuiIcon checkBoxChecked = checkBoxDisabled.offsetCopy(0, 10);
	private GuiIcon checkBoxHovered = checkBoxDisabled.offsetCopy(12, 0);
	private String label;
	private boolean checked;

	public UICheckBox(String label)
	{
		if (label != null && !label.equals(""))
		{
			this.label = label;
			width = GuiRenderer.getStringWidth(label);
		}

		setSize(width + 11, 10);

		shape = new SimpleGuiShape();

	}

	public UICheckBox()
	{
		this(null);
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
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		shape.resetState();
		shape.setSize(10, 10).setPosition(1, 0);
		renderer.drawShape(shape, isDisabled() ? checkboxBackgroundDisabled : checkboxBackground);

		renderer.next();

		// draw the white shade over the slot
		if (hovered)
		{
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			GL11.glShadeModel(GL11.GL_SMOOTH);

			rp.colorMultiplier.set(0xFFFFFF);
			rp.alpha.set(80);
			rp.useTexture.set(false);

			shape.resetState();
			shape.setSize(8, 8).setPosition(2, 1);
			renderer.drawShape(shape, rp);
			renderer.next();

			GL11.glShadeModel(GL11.GL_FLAT);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_ALPHA_TEST);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
		}

		if (label != null)
		{
			renderer.drawText(label, 14, 2, 0x404040, false);
		}
	}

	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		if (checked)
		{
			GL11.glEnable(GL11.GL_BLEND);
			GuiIcon icon = isDisabled() ? checkBoxDisabled : (isHovered() ? checkBoxHovered : checkBoxChecked);
			shape.resetState();
			shape.setSize(12, 10);
			renderer.drawShape(shape, icon);
		}
	}

	@Subscribe
	public void onButtonRelease(MouseEvent.Release event)
	{
		if (event.getButton() == MouseButton.LEFT)
		{
			if (fireEvent(new ComponentEvent.ValueChanged(this, this.checked, !this.checked)))
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
			if (fireEvent(new ComponentEvent.ValueChanged(this, this.checked, !this.checked)))
				checked = !checked;
		}
	}

	@Override
	public String toString()
	{
		return this.getClass().getName() + "[ text=" + label + ", checked=" + this.checked + ", " + this.getPropertyString() + " ]";
	}

}
