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

import org.apache.logging.log4j.util.Strings;

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.IContentComponent;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.element.Position;
import net.malisis.core.client.gui.component.element.Size;
import net.malisis.core.client.gui.element.SimpleGuiShape;
import net.malisis.core.client.gui.event.ComponentEvent.ValueChange;
import net.malisis.core.renderer.icon.provider.GuiIconProvider;

/**
 * @author Ordinastie
 *
 */
public class UIRadioButton extends UIComponent<UIRadioButton> implements IContentComponent
{
	private static HashMap<String, List<UIRadioButton>> radioButtons = new HashMap<>();

	private UIComponent<?> content;
	private String name;
	private boolean selected;

	private GuiIconProvider rbIconProvider;

	public UIRadioButton(MalisisGui gui, String name, String text)
	{
		super(gui);
		this.name = name;
		setText(text);
		setSize(Size.contentSize(14, 4));

		shape = new SimpleGuiShape();

		iconProvider = new GuiIconProvider(gui.getGuiTexture().getIcon(200, 54, 8, 8), null, gui.getGuiTexture().getIcon(200, 62, 8, 8));
		rbIconProvider = new GuiIconProvider(	gui.getGuiTexture().getIcon(214, 54, 6, 6),
												gui.getGuiTexture().getIcon(220, 54, 6, 6),
												gui.getGuiTexture().getIcon(208, 54, 6, 6));

		addRadioButton(this);
	}

	public UIRadioButton(MalisisGui gui, String name)
	{
		this(gui, name, null);
	}

	//#region Getters/Setters
	@Override
	public UIComponent<?> getContent()
	{
		return content;
	}

	@Override
	public void setContent(UIComponent<?> content)
	{
		this.content = content;
	}

	/**
	 * Sets the text for this {@link UICheckBox}.
	 *
	 * @param text the new text
	 */
	@Override
	public void setText(String text)
	{
		if (Strings.isEmpty(text))
			setContent(null);
		IContentComponent.super.setText(text);
		content.setPosition(Position.of(12, 1));
		content.setParent(this);
	}

	/**
	 * Checks if this {@link UIRadioButton} is selected.
	 *
	 * @return true, if is selected
	 */
	public boolean isSelected()
	{
		return selected;
	}

	/**
	 * Sets state of this {@link UIRadioButton} to selected.<br>
	 * If a radiobutton with the same name is currently selected, unselects it.<br>
	 * Does not fire {@link SelectEvent}.
	 *
	 * @return the UI radio button
	 */
	public UIRadioButton setSelected()
	{
		UIRadioButton rb = getSelected(name);
		if (rb != null)
			rb.selected = false;
		selected = true;
		return this;
	}

	//#end Getters/Setters

	@Override
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		shape.resetState();
		shape.setSize(8, 8);
		shape.translate(1, 1, 0);
		renderer.drawShape(shape, rp);
	}

	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		if (content != null)
			content.draw(renderer, mouseX, mouseY, partialTick);

		if (selected)
		{
			rp.reset();
			shape.resetState();
			shape.setSize(6, 6);
			shape.setPosition(2, 2);
			rp.iconProvider.set(rbIconProvider);
			renderer.drawShape(shape, rp);
		}

		renderer.next();
		// draw the white shade over the slot
		if (hovered)
			renderer.drawRectangle(2, 2, 0, 6, 6, 0xFFFFFF, 80);
	}

	@Override
	public boolean onClick(int x, int y)
	{
		if (fireEvent(new UIRadioButton.SelectEvent(this)))
			setSelected();
		return true;
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

	public static UIRadioButton getSelected(UIRadioButton rb)
	{
		return getSelected(rb.name);
	}

	/**
	 * Event fired when a {@link UIRadioButton} changes its selection.<br>
	 * When catching the event, the state is not applied to the {@code UIRadioButton} yet.<br>
	 * Cancelling the event will prevent the value to be changed.
	 */
	public static class SelectEvent extends ValueChange<UIRadioButton, UIRadioButton>
	{
		public SelectEvent(UIRadioButton component)
		{
			super(component, getSelected(component), component);
		}
	}
}
