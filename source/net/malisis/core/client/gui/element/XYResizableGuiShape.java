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

import net.malisis.core.renderer.element.Face;

/**
 * @author Ordinastie
 * 
 */
public class XYResizableGuiShape extends GuiShape
{
	protected int cornerWidth, cornerHeight;

	public XYResizableGuiShape(int cornerWidth, int cornerHeight)
	{
		this.cornerWidth = cornerWidth;
		this.cornerHeight = cornerHeight;

		createFaces();
		storeState();

	}

	public XYResizableGuiShape(int corner)
	{
		this(corner, corner);
	}

	public XYResizableGuiShape()
	{
		this(5, 5);
	}

	@Override
	protected void createFaces()
	{
		faces = new Face[] { guiFace(), guiFace(), guiFace(), guiFace(), guiFace(), guiFace(), guiFace(), guiFace(), guiFace() };
	}

	@Override
	public GuiShape setSize(int w, int h)
	{
		w = Math.max(w - 2 * cornerWidth, 0);
		h = Math.max(h - 2 * cornerHeight, 0);

		faces[0].factor(cornerWidth, cornerHeight, 0);
		faces[1].factor(w, cornerHeight, 0);
		faces[2].factor(cornerWidth, cornerHeight, 0);
		faces[3].factor(cornerWidth, h, 0);
		faces[4].factor(w, h, 0);
		faces[5].factor(cornerWidth, h, 0);
		faces[6].factor(cornerWidth, cornerHeight, 0);
		faces[7].factor(w, cornerHeight, 0);
		faces[8].factor(cornerWidth, cornerHeight, 0);

		faces[1].translate(cornerWidth, 0, 0);
		faces[2].translate(cornerWidth + w, 0, 0);
		faces[3].translate(0, cornerHeight, 0);
		faces[4].translate(cornerWidth, cornerHeight, 0);
		faces[5].translate(cornerWidth + w, cornerHeight, 0);
		faces[6].translate(0, cornerHeight + h, 0);
		faces[7].translate(cornerWidth, cornerHeight + h, 0);
		faces[8].translate(cornerWidth + w, cornerHeight + h, 0);

		return this;
	}

	@Override
	public GuiShape scale(float x, float y)
	{
		return this;
	}
}
