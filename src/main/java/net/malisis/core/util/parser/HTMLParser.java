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

package net.malisis.core.util.parser;

import static net.malisis.core.util.parser.token.Token.*;

import java.util.LinkedList;

import net.malisis.core.util.parser.HTMLParser.HTMLNode;
import net.malisis.core.util.parser.token.Token;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * @author Ordinastie
 *
 */
public class HTMLParser extends Parser<HTMLNode>
{
	private HTMLNode currentNode;
	private HTMLNode rootNode;
	private LinkedList<HTMLNode> openTags = new LinkedList<>();

	public HTMLParser(String text)
	{
		super(text);
		withTokens(Inferior, Superior, Div, Identifier, Equal, StringToken);
		ignoreTokens(Space);
	}

	@Override
	public Token<?> readToken()
	{
		Token<?> t;
		while ((t = super.readToken()) == Space);
		return t;
	}

	@Override
	public HTMLNode parse()
	{
		rootNode = new HTMLNode("document", null);
		currentNode = rootNode;

		String content = null;
		Mutable<String> identifier = new MutableObject<>();
		String attr;

		while (!isEnd())
		{
			if (match(Token.Inferior))
			{
				if (match(Token.Div))
				{
					readToken();//skip identifier
					closeTag();
				}
				else if (match(Token.Identifier, identifier))
				{
					openTag(identifier.getValue());

					while (match(Token.Identifier, identifier))
					{
						attr = identifier.getValue();
						if (!match(Token.Equal))
							addAttribute(identifier.getValue());
						else if (match(Token.StringToken, identifier))
						{
							addAttribute(attr, identifier.getValue());
							//readToken();
						}
					}
					if (match(Token.Div))
						closeTag();
				}
				if (!match(Token.Superior))
					error(Token.Superior);
			}

			content = readUntil(Token.Inferior);
			if (!StringUtils.isEmpty(content))
				addContent(content);
		}

		return rootNode;
	}

	public void openTag(String tag)
	{
		//		self.Log('Opening : <b>' . tag . '</b><br />');
		if (this.currentNode == null)
			return;

		HTMLNode node = new HTMLNode(tag);
		currentNode.AddNode(node);
		openTags.add(currentNode);
		currentNode = node;
	}

	public void addAttribute(String attr)
	{
		addAttribute(attr, null);
	}

	public void addAttribute(String attr, String attr_val)
	{
		//	self.Log('Found attr : <b>' . attr . '</b><br />');
		if (this.currentNode != null)
			this.currentNode.AddAttribute(attr, attr_val);
	}

	public void addContent(String str)
	{
		//	self.Log('Adding content : <b>' . htmlentities(str) . '</b><br />');
		if (this.currentNode == null)
			return;
		this.currentNode.AddText(str);
	}

	public void closeTag()
	{
		//	self.Log('Closing : <b>' . this.current_tag.name . '</b><br />');
		if (this.openTags.size() == 0) //prevent malformed html with closing non opened tags
			return;

		this.currentNode = this.openTags.removeLast();
	}

	public HTMLNode getRootNode()
	{
		return rootNode;
	}

	public static class HTMLNode
	{
		public String name = null;
		public int level = 0;
		public HTMLNode parent = null;
		public Multimap<String, String> attributes = ArrayListMultimap.create();
		public LinkedList<HTMLNode> nodes = new LinkedList<>();
		public String text = null;

		public HTMLNode(String name, String text)
		{
			this.name = name;
			this.text = text;
		}

		public HTMLNode(String name)
		{
			this(name, null);
		}

		public void AddAttribute(String attr, String val)
		{
			attributes.put(attr, val);
		}

		public void AddNode(HTMLNode node)
		{
			if (!StringUtils.isEmpty(text))
				throw new IllegalArgumentException("Text already defined for node.");

			node.level = this.level + 1;
			node.parent = this;
			nodes.add(node);
		}

		public void AddText(String text)
		{
			this.AddNode(new HTMLNode(null, text));
		}

		public String prettyPrint()
		{
			int n = 4;
			if (text != null)
				return StringUtils.repeat(' ', level * n) + text + "\n";
			String s = StringUtils.repeat(' ', level * n) + "<" + name + (attributes.size() != 0 ? " " + attributes.toString() : "")
					+ ">\n";
			for (HTMLNode node : nodes)
				s += node.prettyPrint();
			if (nodes.size() != 0)
				s += StringUtils.repeat(' ', level * n) + "</" + name + ">\n";

			return s;
		}

		@Override
		public String toString()
		{
			return prettyPrint();
		}
	}
}