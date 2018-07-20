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

package net.malisis.core.client.gui.element.size;

import static com.google.common.base.Preconditions.*;

import java.util.function.Function;
import java.util.function.IntSupplier;

import net.malisis.core.client.gui.element.size.Size.ISize;

/**
 * @author Ordinastie
 *
 */
public interface ISizeBuilder<BUILDER, OWNER>
{
	public BUILDER size(Function<OWNER, ISize> func);

	public default BUILDER size(ISize size)
	{
		return size(o -> size);
	}

	public default BUILDER size(Function<OWNER, IntSupplier> width, Function<OWNER, IntSupplier> height)
	{
		checkNotNull(width);
		checkNotNull(height);
		return size(o -> Size.of(width.apply(o), height.apply(o)));
	}

	public default BUILDER size(int width, Function<OWNER, IntSupplier> height)
	{
		checkNotNull(width);
		checkNotNull(height);
		return size(o -> Size.of(width, height.apply(o)));
	}

	public default BUILDER size(Function<OWNER, IntSupplier> width, int height)
	{
		checkNotNull(width);
		checkNotNull(height);
		return size(o -> Size.of(width.apply(o), height));
	}

	public default BUILDER size(Function<OWNER, IntSupplier> width, IntSupplier height)
	{
		checkNotNull(width);
		checkNotNull(height);
		return size(o -> Size.of(width.apply(o), height));
	}

	public default BUILDER size(IntSupplier width, Function<OWNER, IntSupplier> height)
	{
		checkNotNull(width);
		checkNotNull(height);
		return size(o -> Size.of(width, height.apply(o)));
	}

	public default BUILDER size(IntSupplier width, IntSupplier height)
	{
		checkNotNull(width);
		checkNotNull(height);
		return size(o -> Size.of(width, height));
	}

	public default BUILDER size(int width, IntSupplier height)
	{
		checkNotNull(height);
		return size(o -> Size.of(width, height));
	}

	public default BUILDER size(IntSupplier width, int height)
	{
		checkNotNull(width);
		return size(o -> Size.of(width, height));
	}

	public default BUILDER size(int width, int height)
	{
		return size(o -> Size.of(width, height));
	}

}
