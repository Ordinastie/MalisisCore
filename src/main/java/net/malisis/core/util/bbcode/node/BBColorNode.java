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

package net.malisis.core.util.bbcode.node;

import net.malisis.core.util.bbcode.render.BBRenderElement;

/**
 * @author Ordinastie
 *
 */
public class BBColorNode extends BBNode
{
	protected int color;

	public BBColorNode(String tag)
	{
		super(tag);
	}

	public BBColorNode(String tag, int color)
	{
		this(tag);
		setColor(color);
	}

	public int getColor()
	{
		return color;
	}

	public void setColor(String hexColor)
	{
		setColor(Integer.decode(hexColor));
	}

	@Override
	public BBColorNode copy()
	{
		return new BBColorNode(tag, color);
	}

	@Override
	public void apply(BBRenderElement element)
	{
		if (tag.equals("color"))
			element.color = color;
		else if (tag.equals("bgcolor"))
			element.bgColor = color;
	}

	public void setColor(int color)
	{
		this.color = color;
		this.attribute = "#" + Integer.toHexString(color);
	}
}
