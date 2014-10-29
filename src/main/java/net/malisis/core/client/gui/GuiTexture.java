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

package net.malisis.core.client.gui;

import net.malisis.core.client.gui.icon.GuiIcon;
import net.malisis.core.renderer.icon.MalisisIcon;
import net.minecraft.util.ResourceLocation;

/**
 * @author Ordinastie
 *
 */
public class GuiTexture
{
	protected ResourceLocation resourceLocation;
	protected int width;
	protected int height;

	public GuiTexture(ResourceLocation rl, int width, int height)
	{
		this.resourceLocation = rl;
		this.width = width;
		this.height = height;
	}

	public GuiTexture(ResourceLocation rl)
	{
		this(rl, 1, 1);
	}

	public ResourceLocation getResourceLocation()
	{
		return this.resourceLocation;
	}

	private MalisisIcon createIcon(int x, int y, int width, int height)
	{
		MalisisIcon icon = new MalisisIcon();
		icon.setSize(width, height);
		icon.initSprite(this.width, this.height, x, y, false);

		return icon;
	}

	public GuiIcon getIcon(int x, int y, int width, int height)
	{
		return new GuiIcon(createIcon(x, y, width, height));
	}

	public GuiIcon getXYResizableIcon(int x, int y, int width, int height, int corner)
	{
		int w = width - corner * 2;
		int h = height - corner * 2;

		//@formatter:off
		MalisisIcon[] icons = new MalisisIcon[] {
				createIcon(x, 					y, 					corner, 	corner),
				createIcon(x + corner, 		y, 					w, 			corner),
				createIcon(x + corner + w, 	y, 					corner, 	corner),

				createIcon(x, 					y + corner, 		corner, 	h),
				createIcon(x + corner, 		y + corner, 		w, 			h),
				createIcon(x + corner + w, 	y + corner, 		corner, 	h),

				createIcon(x, 					y + corner + h, 	corner, 	corner),
				createIcon(x + corner, 		y + corner + h, 	w, 			corner),
				createIcon(x + corner + w, 	y + corner + h, 	corner, 	corner),
		};
		//@formatter:on

		return new GuiIcon(icons);
	}

	public GuiIcon getXResizableIcon(int x, int y, int width, int height, int side)
	{
		int w = width - side * 2;
		int h = height;

		//@formatter:off
		MalisisIcon[] icons = new MalisisIcon[] {
				createIcon(x, 				y, 		side, 	h),
				createIcon(x + side, 		y, 		w, 		h),
				createIcon(x + side + w, 	y, 		side, 	h),
		};
		//@formatter:on

		return new GuiIcon(icons);
	}

	@Override
	public String toString()
	{
		return resourceLocation + " [" + width + ", " + height + "]";
	}
}
