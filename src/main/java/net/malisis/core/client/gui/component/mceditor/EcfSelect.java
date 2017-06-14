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

package net.malisis.core.client.gui.component.mceditor;

import java.util.Arrays;

import com.google.common.base.Function;

import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.interaction.UISelect;
import net.minecraft.util.text.TextFormatting;

/**
 * @author Ordinastie
 *
 */
public class EcfSelect extends UISelect<TextFormatting>
{
	private MCEditor editor;

	public EcfSelect(MalisisGui gui, MCEditor editor)
	{
		super(gui, 80);
		this.editor = editor;

		labelFunction = new Function<TextFormatting, String>()
		{
			@Override
			public String apply(TextFormatting input)
			{
				return input.toString() + input.getFriendlyName();
			}
		};

		setOptions(Arrays.asList(TextFormatting.values()));
	}

	@Override
	public void setSelectedOption(Option<TextFormatting> option)
	{
		editor.getTextfield().addText(option.getKey().toString());
	}

	@Override
	public boolean onClick(int x, int y)
	{
		super.onClick(x, y);
		if (!expanded)
			editor.getTextfield().setFocused(true);
		return true;
	}
}
