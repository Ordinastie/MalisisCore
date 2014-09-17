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

package net.malisis.core.renderer.icon;

import net.malisis.core.renderer.MalisisIcon;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/**
 * @author Ordinastie
 * 
 */
public class TextureIcon extends MalisisIcon
{
	protected TextureAtlasSprite baseIcon;
	protected double offsetX;
	protected double offsetY;

	protected double widthFactor;
	protected double heightFactor;

	public TextureIcon(TextureAtlasSprite icon)
	{
		super(icon.getIconName());
		baseIcon = icon;
	}

	@Override
	public int getIconWidth()
	{
		return baseIcon.getIconWidth();
	}

	@Override
	public int getIconHeight()
	{
		return baseIcon.getIconHeight();
	}

	public double currentWidth()
	{
		return 16 * widthFactor;
	}

	public double currentHeight()
	{
		return 16 * widthFactor;
	}

	public double currentOffsetX()
	{
		return 16 * offsetX;
	}

	public double currentOffsetY()
	{
		return 16 * offsetY;
	}

	@Override
	public float getMinU()
	{
		return this.flippedU ? baseIcon.getInterpolatedU(currentOffsetX() + currentWidth()) : baseIcon.getInterpolatedU(currentOffsetX());
	}

	@Override
	public float getMaxU()
	{
		return this.flippedU ? baseIcon.getInterpolatedU(currentOffsetX()) : baseIcon.getInterpolatedU(currentOffsetX() + currentWidth());
	}

	@Override
	public float getMinV()
	{
		return this.flippedV ? baseIcon.getInterpolatedV(currentOffsetY() + currentHeight()) : baseIcon.getInterpolatedV(currentOffsetY());
	}

	@Override
	public float getMaxV()
	{
		return this.flippedV ? baseIcon.getInterpolatedV(currentOffsetY()) : baseIcon.getInterpolatedV(currentOffsetY() + currentHeight());
	}

	@Override
	public TextureIcon clone()
	{
		return new TextureIcon(baseIcon);
	}

	@Override
	public TextureIcon offset(int offsetX, int offsetY)
	{
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		return this;
	}

	public TextureIcon clip(double offsetX, double offsetY, double widthFactor, double heightFactor)
	{
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.widthFactor = widthFactor;
		this.heightFactor = heightFactor;
		return this;
	}
}
