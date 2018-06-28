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

import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.content.IContent;
import net.malisis.core.client.gui.component.content.IContentHolder;
import net.malisis.core.client.gui.element.Size;
import net.malisis.core.client.gui.element.position.Position;
import net.malisis.core.client.gui.event.ComponentEvent.ValueChange;
import net.malisis.core.client.gui.render.GuiIcon;
import net.malisis.core.client.gui.render.shape.GuiShape;
import net.malisis.core.client.gui.text.GuiText;
import net.malisis.core.renderer.font.FontOptions;

/**
 * @author Ordinastie
 *
 */
public class UIRadioButton extends UIComponent implements IContentHolder
{
	//TODO:needs to cleared at some point
	private final static HashMap<String, List<UIRadioButton>> radioButtons = new HashMap<>();

	protected final FontOptions fontOptions = FontOptions	.builder()
															.color(0x444444)
															.when(this::isHovered)
															.color(0x777777)
															.when(this::isDisabled)
															.color(0xCCCCCC)
															.build();

	private IContent content;
	private String name;
	private boolean selected;

	public UIRadioButton(String name, String text)
	{
		this.name = name;
		setText(text);
		setSize(Size.sizeOfContent(this, 14, 0));

		//Background
		setBackground(GuiShape	.builder(this)
								.position()
								.x(1)
								.y(1)
								.back()
								.size(8, 8)
								.icon(GuiIcon.forComponent(this, GuiIcon.RADIO_BG, null, GuiIcon.RADIO_DISABLED_BG))
								.build());

		//Foreground
		GuiShape radio = GuiShape	.builder(this)
									.position()
									.x(2)
									.y(2)
									.back()
									.size(6, 6)
									.icon(GuiIcon.forComponent(this, GuiIcon.RADIO, GuiIcon.RADIO_HOVER, GuiIcon.RADIO_DISABLED))
									.build();
		//Overlay
		GuiShape overlay = GuiShape.builder(this).position().x(2).y(2).back().size(6, 6).alpha(80).build();

		setForeground(r -> {
			if (isSelected())
				radio.render(r);
			if (isHovered())
				overlay.render(r);
			r.next();
			if (content() != null)
				content().render(r);
		});

		addRadioButton(this);
	}

	public UIRadioButton(String name)
	{
		this(name, null);
	}

	//#region Getters/Setters
	/**
	 * Sets the content for this {@link UIRadioButton}.
	 *
	 * @param content the content
	 */
	public void setContent(IContent content)
	{
		this.content = content;
		content.setParent(this);
		content.setPosition(Position.of(12, 1));
	}

	public void setText(String text)
	{
		GuiText gt = GuiText.of(text, fontOptions);
		setContent(gt);
	}

	/**
	 * Gets the {@link UIComponent} used as content for this {@link UIRadioButton}.
	 *
	 * @return the content component
	 */
	@Override
	public IContent content()
	{
		return content;
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
	 * If a radio button with the same name is currently selected, unselects it.<br>
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
	public boolean onClick()
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
