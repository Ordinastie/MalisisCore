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
import net.malisis.core.client.gui.element.GuiShape;
import net.malisis.core.client.gui.element.XYResizableGuiShape;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.icon.provider.GuiIconProvider;

/**
 * @author Ordinastie
 *
 */
public class BackgroundTexture implements IGuiRenderer
{
	protected final MalisisGui gui;
	protected final GuiShape shape = new XYResizableGuiShape();
	protected final RenderParameters rp = new RenderParameters();

	public BackgroundTexture(MalisisGui gui, GuiIconProvider iconProvider, int color)
	{
		this.gui = gui;
		rp.iconProvider.set(iconProvider);
		rp.colorMultiplier.set(color);
	}

	public BackgroundTexture(MalisisGui gui, GuiIconProvider iconProvider)
	{
		this(gui, iconProvider, 0xFFFFFF);
	}

	public BackgroundTexture setColor(int color)
	{
		rp.colorMultiplier.set(color);
		return this;
	}

	@Override
	public void render(UIComponent<?> component, GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		shape.resetState();
		shape.setSize(component.getWidth(), component.getHeight());
		//rp.reset();
		renderer.drawShape(shape, rp);
	}

	public static class WindowBackground extends BackgroundTexture
	{
		public WindowBackground(MalisisGui gui)
		{
			super(gui, new GuiIconProvider(gui.getGuiTexture().getXYResizableIcon(200, 0, 15, 15, 5)));
		}

		@Override
		public int getPadding()
		{
			return 5;
		}
	}

	public static class PanelBackground extends BackgroundTexture
	{
		public PanelBackground(MalisisGui gui, int color)
		{
			super(gui, new GuiIconProvider(gui.getGuiTexture().getXYResizableIcon(200, 15, 15, 15, 5)));
		}

		public PanelBackground(MalisisGui gui)
		{
			this(gui, 0xFFFFFF);
		}

		@Override
		public int getPadding()
		{
			return 3;
		}
	}
}
