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

package net.malisis.core.client.gui.component.decoration;

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.element.SimpleGuiShape;
import net.malisis.core.client.gui.icon.GuiIcon;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

/**
 * UIImage
 * 
 * @author PaleoCrafter
 */
public class UIImage extends UIComponent
{
	public static final ResourceLocation BLOCKS_TEXTURE = TextureMap.locationBlocksTexture;
	public static final ResourceLocation ITEMS_TEXTURE = TextureMap.locationItemsTexture;

	/**
	 * Resource location for the texture to use
	 */
	private ResourceLocation texture;
	/**
	 * IIcon to use for the texture
	 */
	private IIcon icon = null;

	public UIImage(IIcon icon, ResourceLocation rl)
	{
		setIcon(icon, rl);
		setSize(16, 16);

		shape = new SimpleGuiShape();
	}

	public UIImage setIcon(IIcon icon)
	{
		this.icon = icon != null ? icon : new GuiIcon();
		return this;
	}

	public UIImage setIcon(IIcon icon, ResourceLocation rl)
	{
		this.icon = icon;
		this.texture = rl;
		return this;
	}

	@Override
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{}

	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		renderer.bindTexture(texture);
		renderer.drawShape(shape, icon);
	}

	@Override
	public String toString()
	{
		return this.getClass().getName() + "[ texture=" + this.texture + ", " + this.getPropertyString() + " ]";
	}

}
