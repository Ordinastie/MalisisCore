/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 Ordinastie
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

package net.malisis.core.client.gui.element.position;

import java.util.function.ToIntFunction;

import net.malisis.core.client.gui.element.Size.ISized;
import net.malisis.core.client.gui.element.position.Position.IPositioned;

/**
 * @author Ordinastie
 *
 */
public interface IPositionBuilder
{
	public IPositionBuilder x(int x);

	public IPositionBuilder y(int y);

	public IPositionBuilder x(ToIntFunction<?> xFunction);

	public IPositionBuilder y(ToIntFunction<?> yFunction);

	public IPositionBuilder leftAligned();

	public IPositionBuilder leftAligned(int x);

	public IPositionBuilder topAligned();

	public IPositionBuilder topAligned(int y);

	public IPositionBuilder centered();

	public IPositionBuilder centered(int offset);

	public IPositionBuilder rightAligned();

	public IPositionBuilder rightAligned(int spacing);

	public IPositionBuilder middleAligned();

	public IPositionBuilder middleAligned(int offset);

	public IPositionBuilder bottomAligned();

	public IPositionBuilder bottomAligned(int spacing);

	public <T extends IPositioned & ISized> IPositionBuilder leftOf(T other);

	public <T extends IPositioned & ISized> IPositionBuilder leftOf(T other, int spacing);

	public <T extends IPositioned & ISized> IPositionBuilder rightOf(T other);

	public <T extends IPositioned & ISized> IPositionBuilder rightOf(T other, int spacing);

	public IPositionBuilder above(IPositioned other);

	public IPositionBuilder above(IPositioned other, int spacing);

	public <T extends IPositioned & ISized> IPositionBuilder below(T other);

	public <T extends IPositioned & ISized> IPositionBuilder below(T other, int spacing);

	public IPositionBuilder leftAlignedTo(IPositioned other);

	public IPositionBuilder leftAlignedTo(IPositioned other, int offset);

	public <T extends IPositioned & ISized> IPositionBuilder rightAlignedTo(T other);

	public <T extends IPositioned & ISized> IPositionBuilder rightAlignedTo(T other, int offset);

	public <T extends IPositioned & ISized> IPositionBuilder centeredTo(T other);

	public <T extends IPositioned & ISized> IPositionBuilder centeredTo(T other, int offset);

	public IPositionBuilder topAlignedTo(IPositioned other);

	public IPositionBuilder topAlignedTo(IPositioned other, int offset);

	public <T extends IPositioned & ISized> IPositionBuilder bottomAlignedTo(T other);

	public <T extends IPositioned & ISized> IPositionBuilder bottomAlignedTo(T other, int offset);

	public <T extends IPositioned & ISized> IPositionBuilder middleAlignedTo(T other);

	public <T extends IPositioned & ISized> IPositionBuilder middleAlignedTo(T other, int offset);
}
