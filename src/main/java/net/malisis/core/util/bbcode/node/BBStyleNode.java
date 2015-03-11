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
import net.minecraft.util.EnumChatFormatting;

/**
 * @author Ordinastie
 *
 */
public class BBStyleNode extends BBNode
{
	protected EnumChatFormatting ecf;

	public BBStyleNode(String tag)
	{
		super(tag);
		switch (tag)
		{
			case "b":
				ecf = EnumChatFormatting.BOLD;
				break;
			case "i":
				ecf = EnumChatFormatting.ITALIC;
				break;
			case "u":
				ecf = EnumChatFormatting.UNDERLINE;
				break;
			case "s":
				ecf = EnumChatFormatting.STRIKETHROUGH;
				break;
			default:
				throw new IllegalArgumentException("Invalid tag for BBStyleNode : " + tag);
		}
	}

	public EnumChatFormatting getEcf()
	{
		return ecf;
	}

	public String toFormattedString()
	{
		return ecf.toString();
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
		element.styles.add(ecf);
	}
}
