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

package net.malisis.core.util.bbcode.render;

import java.util.LinkedList;
import java.util.List;

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.util.bbcode.BBString;
import net.malisis.core.util.bbcode.node.BBNode;

/**
 * @author Ordinastie
 *
 */
public class BBCodeRenderer
{
	private BBString bbText;

	private LinkedList<BBRenderElement> renderElements = new LinkedList<>();

	public BBCodeRenderer(BBString bbText)
	{
		this.bbText = bbText;
	}

	public List<BBRenderElement> buildLines(List<String> lines)
	{
		renderElements.clear();
		buildRenderElements(bbText.getRoot(), new BBRenderElement(null));
		wrapRenderElements(lines);

		return renderElements;
	}

	private void buildRenderElements(BBNode node, BBRenderElement element)
	{
		for (BBNode n : node)
		{
			BBRenderElement nodeElement = new BBRenderElement(element);
			n.apply(nodeElement);
			if (n.isStandAlone())
				renderElements.add(nodeElement);
			else
				buildRenderElements(n, nodeElement);
		}
	}

	private void wrapRenderElements(List<String> lines)
	{
		if (renderElements.size() == 0 || lines.size() == 0)
			return;

		int line = 0;
		int index = 0;
		String currentLine;

		for (int i = 0; i < renderElements.size(); i++)
		{
			currentLine = lines.get(line);
			BBRenderElement el = renderElements.get(i);
			el.line = line;
			BBRenderElement split = el.split(currentLine.length() - index);
			index += el.text.length();
			if (split != null)
				renderElements.add(i + 1, split);

			if (index >= currentLine.length())
			{
				line++;
				index = 0;
			}
		}
	}

	public void render(GuiRenderer renderer, int x, int y, int z, IBBCodeRenderer bbsr)
	{
		int ox = x;
		int oy = y;
		int lineHeight = bbsr.getLineHeight();

		for (BBRenderElement el : renderElements)
		{
			if (el.line > bbsr.getStartLine() + bbsr.getVisibleLines())
				return;

			if (el.line >= bbsr.getStartLine())
			{
				el.render(renderer, ox, oy, z);
				if (el.newLine)
				{
					ox = x;
					oy += lineHeight;
				}
				else
				{
					ox += el.width();
				}
			}
		}
	}
}
