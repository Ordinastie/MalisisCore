/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Ordinastie
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.malisis.core.client.gui.GuiIcon;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.element.SimpleGuiShape;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.malisis.core.client.gui.event.MouseEvent;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.util.MouseButton;

import org.lwjgl.opengl.GL11;

import com.google.common.eventbus.Subscribe;

/**
 * @author Ordinastie
 * 
 */
public class UIRadioButton extends UIComponent<UIRadioButton>
{
	private static HashMap<String, List<UIRadioButton>> radioButtons = new HashMap<>();

	private GuiIcon rbBackground = new GuiIcon(200, 54, 8, 8);
	private GuiIcon rbBackgroundDisabled = rbBackground.offsetCopy(0, 8);
	private GuiIcon rbDisabled = new GuiIcon(208, 54, 6, 6);
	private GuiIcon rbChecked = rbDisabled.offsetCopy(6, 0);
	private GuiIcon rbHovered = rbDisabled.offsetCopy(12, 0);

	private String name;
	private String label;
	private boolean selected;

	public UIRadioButton(String name, String label)
	{
		this.name = name;
		if (label != null && !label.equals(""))
		{
			this.label = label;
			width = GuiRenderer.getStringWidth(label);
		}

		setSize(width + 10, 11);

		shape = new SimpleGuiShape();

		addRadioButton(this);
	}

	public UIRadioButton(String name)
	{
		this(name, null);
	}

	public boolean isSelected()
	{
		return selected;
	}

	public void setSelected()
	{
		UIRadioButton rb = getSelected(name);
		if (rb != null)
			rb.selected = false;
		selected = true;

	}

	@Override
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		shape.resetState();
		shape.setSize(8, 8).translate(1, 0, 0);
		renderer.drawShape(shape, isDisabled() ? rbBackgroundDisabled : rbBackground);

		renderer.next();

		// draw the white shade over the slot
		if (hovered)
		{
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			renderer.enableBlending();

			rp = new RenderParameters();
			rp.colorMultiplier.set(0xFFFFFF);
			rp.alpha.set(80);
			rp.useTexture.set(false);

			shape.resetState();
			shape.setSize(6, 6).setPosition(2, 1);
			renderer.drawShape(shape, rp);
			renderer.next();

			GL11.glShadeModel(GL11.GL_FLAT);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_ALPHA_TEST);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
		}

		if (label != null)
		{
			renderer.drawText(label, 12, 0, 0x404040, false);
		}
	}

	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		if (selected)
		{
			GL11.glEnable(GL11.GL_BLEND);
			GuiIcon icon = isDisabled() ? rbDisabled : (isHovered() ? rbHovered : rbChecked);
			shape.resetState();
			shape.setSize(6, 6).setPosition(2, 1);
			renderer.drawShape(shape, icon);
		}
	}

	@Subscribe
	public void onButtonRelease(MouseEvent.Release event)
	{
		if (event.getButton() == MouseButton.LEFT)
		{
			if (fireEvent(new ComponentEvent.ValueChanged(this, this, getSelected(name))))
				setSelected();
		}
	}

	public static void addRadioButton(UIRadioButton rb)
	{
		List<UIRadioButton> listRb = radioButtons.get(rb.name);
		if (listRb == null)
			listRb = new ArrayList<>();
		listRb.add(rb);
		radioButtons.put(rb.name, listRb);
	}

	public static UIRadioButton getSelected(String name)
	{
		List<UIRadioButton> listRb = radioButtons.get(name);
		if (listRb == null)
			return null;
		for (UIRadioButton rb : listRb)
			if (rb.selected)
				return rb;
		return null;
	}
}
