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

package net.malisis.core.client.gui.component.container;

import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.ClipArea;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.element.XYResizableGuiShape;

/**
 * @author Ordinastie, PaleoCrafter
 */
public class UIWindow extends UIContainer<UIWindow>
{
	public UIWindow(MalisisGui gui, String title, int width, int height, int anchor)
	{
		super(gui, title, width, height);
		setPadding(5, 5);
		this.anchor = anchor;

		shape = new XYResizableGuiShape();
		icon = gui.getGuiTexture().getXYResizableIcon(200, 0, 15, 15, 5);
	}

	public UIWindow(MalisisGui gui, String title, int width, int height)
	{
		this(gui, title, width, height, Anchor.CENTER | Anchor.MIDDLE);
	}

	public UIWindow(MalisisGui gui, int width, int height)
	{
		this(gui, null, width, height, Anchor.CENTER | Anchor.MIDDLE);
	}

	@Override
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		rp.colorMultiplier.set(backgroundColor);
		rp.icon.set(icon);
		renderer.drawShape(shape, rp);
	}

	@Override
	public ClipArea getClipArea()
	{
		return new ClipArea(this, 3);
	}

}
