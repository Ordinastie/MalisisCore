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
public class BBTextNode extends BBNode
{
	private StringBuilder text = new StringBuilder();
	private int index;

	public BBTextNode(String txt)
	{
		super("");
		text.append(txt);
		standAlone = true;
	}

	public void setIndex(int index)
	{
		this.index = index;
	}

	public void shiftIndex(int amount)
	{
		index += amount;
	}

	public String getText()
	{
		return text.toString();
	}

	@Override
	public boolean hasTextNode()
	{
		return text.length() > 0;
	}

	public void append(String txt)
	{
		text.append(txt);
	}

	public boolean isInRange(int position)
	{
		return position - index >= 0 && position - index <= text.length();
	}

	public boolean isInRange(int start, int end)
	{
		return start - index < text.length() && end - index >= 0;
	}

	public int insert(int position, String txt)
	{
		if (!isInRange(position))
			return 0;

		int p = position - index;
		text.insert(p, txt);

		return txt.length();
	}

	public String delete(int start, int end)
	{
		if (!isInRange(start, end))
			return "";

		int s = start - index;
		int e = end - index;
		if (s < 0)
			s = 0;
		if (e > text.length())
			e = text.length();

		String ret = text.substring(s, e);
		text.delete(s, e);

		return ret;
	}

	public BBTextNode[] split(int position)
	{
		int p = position - index;
		if (p < 0)
			p = 0;

		BBTextNode tn1 = new BBTextNode(text.substring(0, p));
		tn1.setIndex(index);
		BBTextNode tn2 = new BBTextNode(text.substring(p));
		tn2.setIndex(index + p);
		return new BBTextNode[] { tn1, tn2 };
	}

	@Override
	public BBTextNode copy()
	{
		return new BBTextNode(text.toString());
	}

	@Override
	public void clean()
	{
		if (parent != null && text.length() < 0)
			parent.remove(this);
	}

	@Override
	public void apply(BBRenderElement element)
	{
		element.text = text.toString();
	}

	@Override
	public String toRawString()
	{
		return text.toString();
	}

	@Override
	public String toBBString()
	{
		return text.toString();
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " : (" + index + ") " + text.toString();
	}
}