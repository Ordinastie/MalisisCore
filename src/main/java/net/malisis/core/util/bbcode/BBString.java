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

package net.malisis.core.util.bbcode;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import joptsimple.internal.Strings;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.util.bbcode.node.BBNode;
import net.malisis.core.util.bbcode.node.BBRootNode;
import net.malisis.core.util.bbcode.node.BBTextNode;
import net.malisis.core.util.bbcode.render.BBCodeRenderer;
import net.malisis.core.util.bbcode.render.IBBCodeRenderer;

/**
 * @author Ordinastie
 *
 */
public class BBString
{
	private String text;
	private BBNode rootNode = new BBRootNode();
	private List<BBTextNode> textNodes = new LinkedList<>();
	private BBCodeRenderer renderer;

	public BBString()
	{
		setText("");

	}

	public BBString(String text)
	{
		this();
		setText(text);
		renderer = new BBCodeRenderer(this);
	}

	public void setText(String text)
	{
		this.text = text == null ? "" : text;
	}

	public String getText()
	{
		return text;
	}

	public BBNode getRoot()
	{
		return rootNode;
	}

	public String getRawText()
	{
		return getRoot().toRawString();
	}

	public String getBBString()
	{
		return getRoot().toBBString();
	}

	public void parseText()
	{
		new BBCodeParser(this).parse();
		buildTextNodeList(getRoot());
	}

	private void buildTextNodeList(BBNode node)
	{
		if (node == getRoot())
			textNodes.clear();

		for (BBNode n : node)
			if (n instanceof BBTextNode)
				textNodes.add((BBTextNode) n);
			else
				buildTextNodeList(n);
	}

	public void insertNode(BBNode node, int start, int end)
	{
		for (BBTextNode tn : textNodes)
		{
			if (tn.isInRange(start, end))
			{
				BBNode copy = node.copy();
				BBNode parent = tn.getParent();

				String newText = tn.delete(start, end);

				BBTextNode[] split = tn.split(start);
				if (split != null)
				{
					if (!node.isStandAlone() && !StringUtils.isEmpty(newText))
					{
						BBTextNode newTextNode = new BBTextNode(newText);
						newTextNode.setIndex(start);
						split[1].shiftIndex(newText.length());
						copy.insert(newTextNode);
					}
					parent.insertBefore(split[0], tn);
					parent.insertBefore(copy, tn);
					parent.insertBefore(split[1], tn);
					parent.remove(tn);
				}
			}
		}

		clean();
	}

	public void addText(String txt, int position)
	{
		if (textNodes.size() == 0)
		{
			getRoot().insert(new BBTextNode(txt));;
			buildTextNodeList(getRoot());
			return;
		}

		int shift = 0;
		for (BBTextNode tn : textNodes)
		{
			tn.shiftIndex(shift);
			shift += tn.insert(position, txt);

		}
	}

	public void deleteText(int start, int end)
	{
		int shift = 0;
		for (BBTextNode tn : textNodes)
		{
			tn.shiftIndex(shift);
			int amount = -tn.delete(start, end).length();
			shift += amount;
		}

		clean();
	}

	public void clean()
	{
		getRoot().clean();
		buildTextNodeList(getRoot());
	}

	public void buildRenderLines(List<String> lines)
	{
		renderer.buildLines(lines);
	}

	public void render(GuiRenderer guiRenderer, int x, int y, int z, IBBCodeRenderer<?> bbcr)
	{
		renderer.render(guiRenderer, x, y, z, bbcr);
	}

	public String debug(BBNode node, int level)
	{
		String str = Strings.repeat(' ', level * 3) + node.toString() + "\n";
		for (BBNode n : node)
			str += debug(n, level + 1);

		return str;
	}

	public String printTextNodes()
	{
		return textNodes.toString();
	}

	@Override
	public String toString()
	{
		String str = text + "\n" + debug(getRoot(), 0);
		return str;
	}
}
