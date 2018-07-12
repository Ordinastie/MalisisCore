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

import static com.google.common.base.Preconditions.*;

import java.util.function.Function;
import java.util.function.IntSupplier;

import net.malisis.core.client.gui.element.position.Position.IPosition;

/**
 * @author Ordinastie
 *
 */
public interface IPositionBuilder<BUILDER, OWNER>
{
	public BUILDER position(Function<OWNER, IPosition> func);

	public default BUILDER position(IPosition position)
	{
		return position(o -> position);
	}

	public default BUILDER position(Function<OWNER, IntSupplier> x, Function<OWNER, IntSupplier> y)
	{
		checkNotNull(x);
		checkNotNull(y);
		return position(o -> Position.of(x.apply(o), y.apply(o)));
	}

	public default BUILDER position(int x, Function<OWNER, IntSupplier> y)
	{
		checkNotNull(x);
		checkNotNull(y);
		return position(o -> Position.of(x, y.apply(o)));
	}

	public default BUILDER position(Function<OWNER, IntSupplier> x, int y)
	{
		checkNotNull(x);
		checkNotNull(y);
		return position(o -> Position.of(x.apply(o), y));
	}

	public default BUILDER position(Function<OWNER, IntSupplier> x, IntSupplier y)
	{
		checkNotNull(x);
		checkNotNull(y);
		return position(o -> Position.of(x.apply(o), y));
	}

	public default BUILDER position(IntSupplier x, Function<OWNER, IntSupplier> y)
	{
		checkNotNull(x);
		checkNotNull(y);
		return position(o -> Position.of(x, y.apply(o)));
	}

	public default BUILDER position(IntSupplier x, IntSupplier y)
	{
		checkNotNull(x);
		checkNotNull(y);
		return position(o -> Position.of(x, y));
	}

	public default BUILDER position(int x, IntSupplier y)
	{
		checkNotNull(y);
		return position(o -> Position.of(x, y));
	}

	public default BUILDER position(IntSupplier x, int y)
	{
		checkNotNull(x);
		return position(o -> Position.of(x, y));
	}

	public default BUILDER position(int x, int y)
	{
		return position(o -> Position.of(x, y));
	}

}
