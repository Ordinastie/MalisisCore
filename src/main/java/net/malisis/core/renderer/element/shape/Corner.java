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

package net.malisis.core.renderer.element.shape;

import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.element.face.NorthFace;
import net.malisis.core.renderer.element.face.SouthEastFace;
import net.malisis.core.renderer.element.face.TriangleBottomSouthEastFace;
import net.malisis.core.renderer.element.face.TriangleTopSouthEastFace;
import net.malisis.core.renderer.element.face.WestFace;

/**
 * @author Ordinastie
 *
 */
public class Corner extends Shape
{
	public Corner()
	{
		super(new SouthEastFace(), new TriangleTopSouthEastFace(), new TriangleBottomSouthEastFace(), new NorthFace(), new WestFace());
		storeState();
	}
}
