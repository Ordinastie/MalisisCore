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

package net.malisis.core.renderer;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/**
 * @author Ordinastie
 * 
 */
public class TextureIcon extends MalisisIcon
{
	protected TextureAtlasSprite baseIcon;
	protected int offsetX;
	protected int offsetY;

	public TextureIcon(TextureAtlasSprite icon)
	{
		baseIcon = icon;
	}

	@Override
	public int getX()
	{
		return baseIcon.getOriginX() + offsetX;
	}

	@Override
	public int getY()
	{
		return baseIcon.getOriginY() + offsetY;
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

	@Override
	public float getMinU()
	{
		return this.flippedU ? baseIcon.getInterpolatedU(offsetX + width) : baseIcon.getInterpolatedU(offsetX);
	}

	@Override
	public float getMaxU()
	{
		return this.flippedU ? baseIcon.getInterpolatedU(offsetX) : baseIcon.getInterpolatedU(offsetX + width);
	}

	@Override
	public float getMinV()
	{
		return this.flippedV ? baseIcon.getInterpolatedV(offsetY + height) : baseIcon.getInterpolatedV(offsetY);
	}

	@Override
	public float getMaxV()
	{
		return this.flippedV ? baseIcon.getInterpolatedV(offsetY) : baseIcon.getInterpolatedV(offsetY + height);
	}

	@Override
	public TextureIcon clone()
	{
		return new TextureIcon(baseIcon);
	}

	@Override
	public void offset(int offsetX, int offsetY)
	{
		this.offsetX = offsetX;
		this.offsetY = offsetY;
	}

	@Override
	public void clip(int offsetX, int offsetY, int width, int height)
	{
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.width = width;
		this.height = height;
	}

}
