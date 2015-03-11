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

import java.util.Iterator;
import java.util.LinkedList;

import net.malisis.core.util.bbcode.render.BBRenderElement;

/**
 * @author Ordinastie
 *
 */
public abstract class BBNode implements Iterable<BBNode>
{
	protected String tag;
	protected BBNode parent;
	protected LinkedList<BBNode> children = new LinkedList<>();
	protected String attribute;
	protected boolean standAlone = false;
	protected int charIndex;

	public BBNode(String tag, String attribute)
	{
		this.tag = tag;
		this.attribute = attribute;
	}

	public BBNode(String tag)
	{
		this(tag, "");
	}

	public String getTag()
	{
		return tag;
	}

	public void setParent(BBNode parent)
	{
		this.parent = parent;
	}

	public BBNode getParent()
	{
		return parent;
	}

	@Override
	public Iterator<BBNode> iterator()
	{
		return children.iterator();
	}

	public void insert(BBNode node)
	{
		insert(node, children.size());
	}

	public void insertAfter(BBNode node, BBNode after)
	{
		insert(node, children.indexOf(after) + 1);
	}

	public void insertBefore(BBNode node, BBNode before)
	{
		insert(node, children.indexOf(before));
	}

	public void insert(BBNode node, int position)
	{
		if (isStandAlone())
			throw new IllegalArgumentException("Can't add nodes to " + getClass().getSimpleName());

		children.add(position, node);
		node.parent = this;
	}

	public void remove(BBNode node)
	{
		if (isStandAlone())
			throw new IllegalArgumentException("Can't remove nodes from " + getClass().getSimpleName());

		children.remove(node);
		node.parent = null;
	}

	public String getAttribute()
	{
		if (attribute != "")
			return "=" + attribute;
		return "";
	}

	public boolean isStandAlone()
	{
		return standAlone;
	}

	public boolean hasTextNode()
	{
		for (BBNode n : this)
			if (n.hasTextNode())
				return true;
		return false;
	}

	public BBStyleNode getChildStyleNode(String tag)
	{
		for (BBNode n : this)
			if (n instanceof BBStyleNode && n.tag.equals(tag))
				return (BBStyleNode) n;
		return null;
	}

	public abstract BBNode copy();

	public void clean()
	{
		if (getParent() != null && !isStandAlone() && !hasTextNode())
		{
			getParent().remove(this);
			return;
		}

		for (BBNode node : this)
			node.clean();

		Iterator<BBNode> it = iterator();
		BBNode lastNode = null;
		while (it.hasNext())
		{
			BBNode node = it.next();
			if (lastNode instanceof BBTextNode && node instanceof BBTextNode)
			{
				((BBTextNode) lastNode).append(((BBTextNode) node).getText());
				it.remove();
			}
			else
				lastNode = node;
		}
	}

	public void apply(BBRenderElement element)
	{

	}

	public String toRawString()
	{
		StringBuilder str = new StringBuilder();

		for (BBNode n : this)
			str.append(n.toRawString());

		return str.toString();
	}

	public String toBBString()
	{
		StringBuilder str = new StringBuilder();

		str.append("[" + tag);
		str.append(getAttribute());
		str.append("]");

		if (standAlone)
			return str.toString();

		for (BBNode n : this)
			str.append(n.toBBString());

		str.append("[/" + tag + "]");

		return str.toString();
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " : [" + tag + getAttribute() + "]";
	}
}
