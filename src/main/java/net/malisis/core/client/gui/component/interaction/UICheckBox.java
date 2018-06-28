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
import net.minecraft.util.text.TextFormatting;

/**
 * UICheckBox
 *
 * @author Ordinastie
 */
public class UICheckBox extends UIComponent implements IContentHolder
{
	protected final FontOptions fontOptions = FontOptions	.builder()
															.color(0x444444)
															.when(this::isHovered)
															.color(0x777777)
															.when(this::isDisabled)
															.color(0xCCCCCC)
															.build();
	/** The content for this {@link UICheckBox}. */
	protected IContent content;
	/** Whether this {@link UICheckBox} is checked. */
	protected boolean checked;

	public UICheckBox(String text)
	{
		setText(text);
		setSize(Size.sizeOfContent(this, 14, 12));

		//Background
		setBackground(GuiShape.builder(this).position().x(1).back().size(12, 12).icon(GuiIcon.CHECKBOX_BG).build());

		//Foreground
		GuiShape overlay = GuiShape.builder(this).position().x(2).y(1).back().size(10, 10).color(0xFFFFFF).alpha(80).build();
		GuiShape check = GuiShape	.builder(this)
									.position()
									.x(1)
									.y(1)
									.back()
									.size(12, 10)
									.zIndex(10)
									.icon(GuiIcon.forComponent(this, GuiIcon.CHECKBOX, GuiIcon.CHECKBOX_HOVER, GuiIcon.CHECKBOX_DISABLED))
									.build();

		setForeground(r -> {
			if (isHovered())
				overlay.render(r);
			if (isChecked())
				check.render(r);
			r.next();
			if (content() != null)
				content().render(r);
		});
	}

	public UICheckBox()
	{
		this(null);
	}

	//#region Getters/Setters
	/**
	 * Sets the content for this {@link UICheckBox}.
	 *
	 * @param content the content
	 */
	public void setContent(IContent content)
	{
		this.content = content;
		content.setParent(this);
		content.setPosition(Position.of(14, 2));
	}

	public void setText(String text)
	{
		GuiText gt = GuiText.of(text, fontOptions);
		setContent(gt);
	}

	/**
	 * Gets the {@link UIComponent} used as content for this {@link UICheckBox}.
	 *
	 * @return the content component
	 */
	@Override
	public IContent content()
	{
		return content;
	}

	//#end Getters/Setters
	/**
	 * Checks if this {@link UICheckBox} is checked.
	 *
	 * @return whether this {@link UICheckBox} is checked or not.
	 */
	public boolean isChecked()
	{
		return this.checked;
	}

	/**
	 * Sets the state for this {@link UICheckBox}. Does not fire {@link CheckEvent}.
	 *
	 * @param checked true if checked
	 * @return this {@link UIComponent}
	 */
	public UICheckBox setChecked(boolean checked)
	{
		this.checked = checked;
		return this;
	}

	@Override
	public boolean onClick()
	{
		if (fireEvent(new CheckEvent(this, !checked)))
			checked = !checked;
		return true;
	}

	@Override
	public boolean onKeyTyped(char keyChar, int keyCode)
	{
		if (!isFocused())
			return super.onKeyTyped(keyChar, keyCode);

		if (keyCode == Keyboard.KEY_SPACE)
		{
			if (fireEvent(new CheckEvent(this, !checked)))
				checked = !checked;
		}

		return false;
	}

	@Override
	public String getPropertyString()
	{
		return (checked ? "checked " : "") + "[" + TextFormatting.GREEN + content + TextFormatting.RESET + "] " + super.getPropertyString();
	}

	/**
	 * Event fired when a {@link UICheckBox} is checked or unchecked.<br>
	 * When catching the event, the state is not applied to the {@code UICheckbox} yet.<br>
	 * Canceling the event will prevent the state to be set for the {@code UICheckbox} .
	 */
	public static class CheckEvent extends ValueChange<UICheckBox, Boolean>
	{
		public CheckEvent(UICheckBox component, boolean checked)
		{
			super(component, component.isChecked(), checked);
		}

		/**
		 * @return the new state for the checkbox
		 */
		public boolean isChecked()
		{
			return newValue;
		}
	}

}
