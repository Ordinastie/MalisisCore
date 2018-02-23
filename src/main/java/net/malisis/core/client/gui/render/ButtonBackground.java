/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 Ordinastie
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

package net.malisis.core.client.gui.render;

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.renderer.icon.GuiIcon;
import net.malisis.core.renderer.icon.provider.GuiIconProvider;

/**
 * @author Ordinastie
 *
 */
public class ButtonBackground extends TexturedBackground
{
	private final GuiIconProvider iconPressedProvider;

	public ButtonBackground(MalisisGui gui)
	{
		super(gui,
				new GuiIconProvider(gui.getGuiTexture().getXYResizableIcon(0, 20, 200, 20, 5),
									gui.getGuiTexture().getXYResizableIcon(0, 40, 200, 20, 5),
									gui.getGuiTexture().getXYResizableIcon(0, 0, 200, 20, 5)));

		iconPressedProvider = new GuiIconProvider((GuiIcon) gui.getGuiTexture().getXYResizableIcon(0, 40, 200, 20, 5).flip(true, true));
	}

	@Override
	public void render(UIComponent<?> component, GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		boolean pressed = component instanceof UIButton ? ((UIButton) component).isPressed() : false;
		rp.iconProvider.set(pressed ? iconPressedProvider : iconProvider);
		super.render(component, renderer, mouseX, mouseY, partialTick);
	}
}
