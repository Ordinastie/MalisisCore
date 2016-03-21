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
import net.minecraft.util.text.TextFormatting;

/**
 * @author Ordinastie
 *
 */
public class BBStyleNode extends BBNode
{
	protected TextFormatting format;

	public BBStyleNode(String tag)
	{
		super(tag);
		switch (tag)
		{
			case "b":
				format = TextFormatting.BOLD;
				break;
			case "i":
				format = TextFormatting.ITALIC;
				break;
			case "u":
				format = TextFormatting.UNDERLINE;
				break;
			case "s":
				format = TextFormatting.STRIKETHROUGH;
				break;
			default:
				throw new IllegalArgumentException("Invalid tag for BBStyleNode : " + tag);
		}
	}

	public TextFormatting getEcf()
	{
		return format;
	}

	public String toFormattedString()
	{
		return format.toString();
	}

	@Override
	public BBStyleNode copy()
	{
		return new BBStyleNode(tag);
	}

	@Override
	public void clean()
	{
		BBStyleNode node = getChildStyleNode(tag);
		if (node != null && node.getParent() != null)
		{
			for (BBNode n : node)
				node.getParent().insertBefore(n, node);
			node.getParent().remove(node);
		}

		super.clean();
	}

	@Override
	public void apply(BBRenderElement element)
	{
		element.styles.add(format);
	}
}
