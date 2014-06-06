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

import net.malisis.core.client.gui.GuiIcon;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.event.KeyboardEvent;
import net.malisis.core.client.gui.event.MouseEvent;
import net.malisis.core.renderer.element.RenderParameters;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.preset.ShapePreset;
import net.malisis.core.util.MouseButton;

import com.google.common.eventbus.Subscribe;

/**
 * UICheckBox
 * 
 * @author PaleoCrafter
 */
public class UICheckBox extends UIComponent
{

	private GuiIcon checkboxBackground = new GuiIcon(180, 0, 10, 10);
	private GuiIcon checkBoxChecked = new GuiIcon(200, 10, 12, 10);
	private GuiIcon checkBoxHovered = checkBoxChecked.offset(0, 10);
	private UILabel label;
	private boolean checked;


	public UICheckBox(String label)
	{
		if(label != null && !label.equals(""))
		{
			this.label = new UILabel(label);
			this.label.setPosition(x + 14, y + 2);
			width = this.label.getWidth() + 2;
		}
			
		width += 11;
		height = 10;
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
	public UIComponent setPosition(int x, int y)
	{
		super.setPosition(x, y);
		if (label != null)
			label.setPosition(x + 12, y);
		return this;
	}
	
	public boolean isChecked()
	{
		return this.checked;
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
		checkboxBackground = new GuiIcon(200, 0, 10, 10);
		rp.icon = checkboxBackground;
		Shape shape = ShapePreset.GuiElement(10, 10).translate(1, 0, 0);
		renderer.drawShape(shape, rp);

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
		if(checked)
		{
			RenderParameters rp = new RenderParameters();
			checkBoxChecked = new GuiIcon(200, 10, 12, 10);
			rp.icon = hovered || label.isHovered() ? checkBoxHovered : checkBoxChecked;
			Shape shape = ShapePreset.GuiElement(12, 10);
			renderer.drawShape(shape, rp);
		}
	}
	
	@Subscribe
	public void onButtonRelease(MouseEvent.Release event)
	{
		if(event.getButton() == MouseButton.LEFT)
			checked = !checked;
	}

	@Subscribe 
	public void onKeyTyped(KeyboardEvent event)
	{
		if(!this.focused)
			return;
		
		if(event.getKeyCode() == Keyboard.KEY_SPACE)
			checked = !checked;
	}


	@Override
	public String toString()
	{
		return this.getClass().getName() + "[ text=" + label + ", checked=" + this.checked + ", " + this.getPropertyString() + " ]";
	}

}
