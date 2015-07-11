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

package net.malisis.core.configuration.setting;

import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author Ordinastie
 *
 */
public class DoubleSetting extends Setting<Double>
{
	private UITextField textField;

	public DoubleSetting(String key, Double defaultValue)
	{
		super(key, defaultValue);
		type = Property.Type.DOUBLE;
	}

	@Override
	public Double readValue(String stringValue)
	{
		try
		{
			return Double.parseDouble(stringValue);
		}
		catch (NumberFormatException e)
		{
			return 0D;
		}
	}

	@Override
	public String writeValue(Double value)
	{
		return value.toString();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public UIComponent getComponent(MalisisGui gui)
	{
		UILabel label = new UILabel(gui, key);
		textField = new UITextField(gui, writeValue(value)).setSize(50, 0).setPosition(label.getWidth() + 2, 0);

		UIContainer container = new UIContainer(gui, label.getWidth() + 54, 12);
		container.add(label);
		container.add(textField);

		return container;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public Double getValueFromComponent()
	{
		return readValue(textField.getText());
	}
}
