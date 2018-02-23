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

import javax.annotation.Nonnull;

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.element.Padding;
import net.malisis.core.client.gui.element.GuiShape;
import net.malisis.core.client.gui.element.XYResizableGuiShape;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.animation.transformation.ITransformable;
import net.malisis.core.renderer.icon.provider.GuiIconProvider;

/**
 * @author Ordinastie
 *
 */
public class TexturedBackground implements IGuiRenderer, ITransformable.Color, ITransformable.Alpha
{
	protected final MalisisGui gui;
	protected final GuiShape shape = new XYResizableGuiShape();
	protected final RenderParameters rp = new RenderParameters();
	protected final GuiIconProvider iconProvider;
	protected Padding padding = Padding.NO_PADDING;

	public TexturedBackground(MalisisGui gui, GuiIconProvider iconProvider, int color)
	{
		this.gui = gui;
		this.iconProvider = iconProvider;
		rp.iconProvider.set(iconProvider);
		rp.colorMultiplier.set(color);
	}

	public TexturedBackground(MalisisGui gui, GuiIconProvider iconProvider)
	{
		this(gui, iconProvider, 0xFFFFFF);
	}

	@Override
	public void setColor(int color)
	{
		rp.colorMultiplier.set(color);
	}

	@Override
	public void setAlpha(int alpha)
	{
		rp.alpha.set(alpha);
	}

	@Override
	@Nonnull
	public Padding getPadding()
	{
		return padding;
	}

	@Override
	public void render(UIComponent<?> component, GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		shape.resetState();
		shape.setSize(component.size().width(), component.size().height());
		//rp.reset();
		renderer.drawShape(shape, rp);
	}

	public static class WindowBackground extends TexturedBackground
	{
		public WindowBackground(MalisisGui gui, int color)
		{
			super(gui, new GuiIconProvider(gui.getGuiTexture().getXYResizableIcon(200, 0, 15, 15, 5)), color);
			padding = Padding.of(5);
		}

		public WindowBackground(MalisisGui gui)
		{
			this(gui, 0xFFFFFF);
		}

		@Override
		@Nonnull
		public Padding getPadding()
		{
			return padding;
		}
	}

	public static class PanelBackground extends TexturedBackground
	{
		public PanelBackground(MalisisGui gui, int color)
		{
			super(gui, new GuiIconProvider(gui.getGuiTexture().getXYResizableIcon(200, 15, 15, 15, 5)), color);
			padding = Padding.of(3);
		}

		public PanelBackground(MalisisGui gui)
		{
			this(gui, 0xFFFFFF);
		}
	}

	public static class BoxBackground extends TexturedBackground
	{
		public BoxBackground(MalisisGui gui, int color)
		{
			super(gui,
					new GuiIconProvider(gui.getGuiTexture().getXYResizableIcon(200, 30, 9, 12, 5),
										null,
										gui.getGuiTexture().getXResizableIcon(200, 42, 9, 12, 5)),
					color);
			padding = Padding.of(1);
		}

		public BoxBackground(MalisisGui gui)
		{
			this(gui, 0xFFFFFF);
		}
	}

}
