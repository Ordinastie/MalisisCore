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

package net.malisis.core.client.gui.element;

import net.malisis.core.client.gui.component.container.UITabGroup;
import net.malisis.core.client.gui.icon.GuiIcon;
import net.malisis.core.renderer.element.Face;

import org.apache.commons.lang3.ArrayUtils;

/**
 * @author Ordinastie
 * 
 */
public class TabShape extends GuiShape
{
	private UITabGroup.Position position;
	protected int cornerWidth, cornerHeight;

	private int tl = -1;
	private int t = -1;
	private int tr = -1;
	private int l = -1;
	private int c = -1;
	private int r = -1;
	private int bl = -1;
	private int b = -1;
	private int br = -1;

	public TabShape(UITabGroup.Position position, int cornerWidth, int cornerHeight)
	{
		this.position = position;
		this.cornerWidth = cornerWidth;
		this.cornerHeight = cornerHeight;

		createFaces();
		storeState();
	}

	public TabShape(UITabGroup.Position position, int corner)
	{
		this(position, corner, corner);
	}

	public TabShape(UITabGroup.Position position)
	{
		this(position, 5, 5);
	}

	@Override
	protected void createFaces()
	{
		faces = new Face[] { guiFace(), guiFace(), guiFace(), guiFace(), guiFace(), guiFace() };

		switch (position)
		{
			case TOP:
				tl = 0;
				t = 1;
				tr = 2;
				l = 3;
				c = 4;
				r = 5;
				break;
			case BOTTOM:
				l = 0;
				c = 1;
				r = 2;
				bl = 3;
				b = 4;
				br = 5;
				break;
			case LEFT:
				tl = 0;
				t = 1;
				l = 2;
				c = 3;
				bl = 4;
				b = 5;
				break;
			case RIGHT:
				t = 0;
				tr = 1;
				c = 2;
				r = 3;
				b = 4;
				br = 5;
				break;
			default:
				break;
		}

	}

	public GuiIcon[] getIcons(GuiIcon[] icons)
	{
		switch (position)
		{
			case TOP:
				return ArrayUtils.removeAll(icons, 6, 7, 8);
			case BOTTOM:
				return ArrayUtils.removeAll(icons, 0, 1, 2);
			case LEFT:
				return ArrayUtils.removeAll(icons, 2, 5, 8);
			case RIGHT:
				return ArrayUtils.removeAll(icons, 0, 3, 6);
			default:
				return icons;
		}

	}

	@Override
	public GuiShape setSize(int w, int h)
	{
		w = Math.max(w - 2 * cornerWidth, 0);
		h = Math.max(h - 2 * cornerHeight, 0);

		if (tl != -1)
			faces[tl].factor(cornerWidth, cornerHeight, 0);
		if (t != -1)
			faces[t].factor(w, cornerHeight, 0);
		if (tr != -1)
			faces[tr].factor(cornerWidth, cornerHeight, 0);
		if (l != -1)
			faces[l].factor(cornerWidth, h, 0);
		if (c != -1)
			faces[c].factor(w, h, 0);
		if (r != -1)
			faces[r].factor(cornerWidth, h, 0);
		if (bl != -1)
			faces[bl].factor(cornerWidth, cornerHeight, 0);
		if (b != -1)
			faces[b].factor(w, cornerHeight, 0);
		if (br != -1)
			faces[br].factor(cornerWidth, cornerHeight, 0);

		if (t != -1)
			faces[t].translate(cornerWidth, 0, 0);
		if (tr != -1)
			faces[tr].translate(cornerWidth + w, 0, 0);
		if (l != -1)
			faces[l].translate(0, cornerHeight, 0);
		if (c != -1)
			faces[c].translate(cornerWidth, cornerHeight, 0);
		if (r != -1)
			faces[r].translate(cornerWidth + w, cornerHeight, 0);
		if (bl != -1)
			faces[bl].translate(0, cornerHeight + h, 0);
		if (b != -1)
			faces[b].translate(cornerWidth, cornerHeight + h, 0);
		if (br != -1)
			faces[br].translate(cornerWidth + w, cornerHeight + h, 0);

		return this;
	}

	@Override
	public GuiShape scale(float x, float y)
	{
		return this;
	}
}
