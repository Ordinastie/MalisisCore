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

import static net.malisis.core.util.parser.token.Token.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import net.malisis.core.util.bbcode.node.BBColorNode;
import net.malisis.core.util.bbcode.node.BBItemNode;
import net.malisis.core.util.bbcode.node.BBNode;
import net.malisis.core.util.bbcode.node.BBStyleNode;
import net.malisis.core.util.bbcode.node.BBTextNode;
import net.malisis.core.util.parser.Parser;

/**
 * @author Ordinastie
 *
 */
public class BBCodeParser extends Parser<BBNode>
{
	private BBString bbText;
	private BBNode currentNode;
	private BBNode node = null;
	private BBTextNode textNode = new BBTextNode("");
	private int charIndex;

	public BBCodeParser(BBString bbText)
	{
		super(bbText.getText());
		withTokens(OpenCar, CloseCar, HexNumber, Identifier, Equal, Div);

		this.bbText = bbText;
		currentNode = bbText.getRoot();
	}

	@Override
	public BBNode parse()
	{
		Mutable<String> str = new MutableObject<>();
		Mutable<Character> c = new MutableObject<>();
		boolean close = false;

		while (!isEnd())
		{
			matched = "";
			if (match(OpenCar))
			{
				close = match(Div);
				if (match(Identifier, str))
				{
					switch (str.toString().toLowerCase())
					{
						case "b":
						case "i":
						case "u":
						case "s":
							node = new BBStyleNode(str.toString());
							break;
						case "color":
						case "bgcolor":
							node = new BBColorNode(str.toString());
							break;
						case "item":
							node = new BBItemNode("");
							break;
					}
				}

				if (close)
				{
					if (node.getClass() == currentNode.getClass() && match(CloseCar))
					{
						addText();
						currentNode = currentNode.getParent();
					}
					else
					{
						close = false;
						node = null;
					}
				}

				else if (node instanceof BBColorNode && match(Equal) && match(HexNumber, str))
				{
					((BBColorNode) node).setColor(str.toString());
				}

				else if (node instanceof BBItemNode && match(Equal) && match(Identifier, str))
				{
					String name = readUntil(CloseCar);
					((BBItemNode) node).setName(str.toString());
					matched += name;
				}
			}

			if (node != null && match(CloseCar, c))
			{
				addText();

				currentNode.insert(node);
				if (!node.isStandAlone())
					currentNode = node;
			}
			else if (!close)
				textNode.append(matched);

			textNode.append(readUntil(OpenCar));

		}

		if (textNode != null && !StringUtils.isEmpty(textNode.getText()))
			currentNode.insert(textNode);

		return bbText.getRoot();
	}

	private void addText()
	{
		if (StringUtils.isEmpty(textNode.getText()))
			return;
		charIndex += textNode.getText().length();
		currentNode.insert(textNode);
		textNode = new BBTextNode("");
		textNode.setIndex(charIndex);

	}
}
