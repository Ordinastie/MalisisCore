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

import net.minecraft.client.renderer.texture.TextureMap;

/**
 * Clipped icons are icons designed to represent a part of a {@link MalisisIcon} registered and stiched into a {@link TextureMap}. The
 * clipping will occur when the icon its based of is stiched.
 *
 * @author Ordinastie
 *
 */
public class ClippedIcon extends MalisisIcon
{
	protected float clipX;
	protected float clipY;
	protected float clipWidth;
	protected float clipHeight;

	public ClippedIcon(MalisisIcon parent, float clipX, float clipY, float clipWidth, float clipHeight)
	{
		super(parent);
		setClipping(clipX, clipY, clipWidth, clipHeight);
	}

	/**
	 * Sets the clipping for this {@link ClippedIcon}. Has no effect after the parent icon has been stiched.
	 *
	 * @param clipX x offset
	 * @param clipY y offset
	 * @param clipWidth the clip width
	 * @param clipHeight the clip height
	 */
	public void setClipping(float clipX, float clipY, float clipWidth, float clipHeight)
	{
		this.clipX = clipX;
		this.clipY = clipY;
		this.clipWidth = clipWidth;
		this.clipHeight = clipHeight;
	}

	/**
	 * Initializes this {@link ClippedIcon} and clips it based on the clipping set.
	 *
	 * @param baseIcon the base icon
	 * @param width the width
	 * @param height the height
	 * @param x the x
	 * @param y the y
	 * @param rotated the rotated
	 */
	@Override
	protected void initIcon(MalisisIcon baseIcon, int width, int height, int x, int y, boolean rotated)
	{
		super.initIcon(baseIcon, width, height, x, y, rotated);
		clip(clipX, clipY, clipWidth, clipHeight);
	}
}
