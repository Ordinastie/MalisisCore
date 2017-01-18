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

import org.apache.commons.lang3.StringUtils;

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.component.IGuiText;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.element.GuiIcon;
import net.malisis.core.client.gui.element.GuiShape;
import net.malisis.core.client.gui.element.GuiShape.ShapePosition;
import net.malisis.core.client.gui.event.ComponentEvent.ValueChange;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.renderer.font.MalisisFont;

/**
 * @author Ordinastie
 *
 */
public class UIRadioButton extends UIComponent<UIRadioButton> implements IGuiText<UIRadioButton>
{
	private static HashMap<String, List<UIRadioButton>> radioButtons = new HashMap<>();

	/** The {@link MalisisFont} to use for this {@link UIRadioButton}. */
	protected MalisisFont font = MalisisFont.minecraftFont;
	/** The {@link FontOptions} to use for this {@link UIRadioButton}. */
	protected FontOptions fontOptions = FontOptions.builder().color(0x444444).build();

	private String name;
	private String text;
	private boolean selected;

	protected GuiShape backgroundShape = GuiShape	.builder(this)
													.position(ShapePosition.fromComponent(this, 1, 0))
													.size(8, 8)
													.icon(GuiIcon.forComponent(this, GuiIcon.RADIO_BG, null, GuiIcon.RADIO_DISABLED_BG))
													.build();
	protected GuiShape radioShape = GuiShape.builder(this)
											.position(ShapePosition.fromComponent(this, 2, 1))
											.size(6, 6)
											.icon(GuiIcon.forComponent(this, GuiIcon.RADIO, GuiIcon.RADIO_HOVER, GuiIcon.RADIO_DISABLED))
											.build();
	//renderer.drawRectangle(2, 1, 0, 6, 6, 0xFFFFFF, 80, true);
	protected GuiShape overlayShape = GuiShape.builder(this).position(ShapePosition.fromComponent(this, 2, 1)).size(6, 6).alpha(80).build();

	public UIRadioButton(String name, String text)
	{
		this.name = name;
		setText(text);

		addRadioButton(this);
	}

	public UIRadioButton(String name)
	{
		this(name, null);
	}

	//#region Getters/Setters
	@Override
	public MalisisFont getFont()
	{
		return font;
	}

	@Override
	public UIRadioButton setFont(MalisisFont font)
	{
		this.font = font != null ? font : MalisisFont.minecraftFont;
		calculateSize();
		return this;
	}

	@Override
	public FontOptions getFontOptions()
	{
		return fontOptions;
	}

	@Override
	public UIRadioButton setFontOptions(FontOptions options)
	{
		this.fontOptions = options;
		calculateSize();
		return this;
	}

	/**
	 * Sets the text for this {@link UIRadioButton}.
	 *
	 * @param text the new text
	 */
	public UIRadioButton setText(String text)
	{
		this.text = text;
		calculateSize();
		return this;
	}

	/**
	 * Gets the text for this {@link UICheckBox}.
	 *
	 * @return the text
	 */
	public String getText()
	{
		return text;
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
	/**
	 * Calculates the size for this {@link UIRadioButton}.
	 */
	private void calculateSize()
	{
		int w = StringUtils.isEmpty(text) ? 0 : (int) font.getStringWidth(text, fontOptions);
		setSize(w + 11, 10);
	}

	@Override
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		backgroundShape.render();
		renderer.next();

		// draw the white shade over the slot
		if (hovered)
			overlayShape.render();

		if (text != null)
			renderer.drawText(font, text, 12, 0, 0, fontOptions);
	}

	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		if (!selected)
			return;

		radioShape.render();
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
