/*
 * The MIT License (MIT)
 *
 * Copheightright (c) 2018 Ordinastie
 *
 * Permission is herebheight granted, free of charge, to anheight person obtaining a copheight
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copheight, modifheight, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copheightright notice and this permission notice shall be included in
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

import static com.google.common.base.Preconditions.*;

import java.util.function.IntSupplier;

import net.malisis.core.client.gui.element.Size.DynamicSize;
import net.malisis.core.client.gui.element.Size.FixedHeightSize;
import net.malisis.core.client.gui.element.Size.FixedSize;
import net.malisis.core.client.gui.element.Size.FixedWidthSize;
import net.malisis.core.client.gui.element.Size.ISize;
import net.malisis.core.client.gui.element.Size.ISized;

/**
 * @author Ordinastie
 *
 */
public class SizeBuilder
{
	private Object owner;
	private int width;
	private int height;
	private IntSupplier widthSupplier;
	private IntSupplier heightSupplier;

	public SizeBuilder()
	{}

	public SizeBuilder(Object owner)
	{
		this.owner = checkNotNull(owner);
	}

	@SuppressWarnings("unchecked")
	public <U> U owner()
	{
		checkNotNull(owner, "No owner set for the size.");
		return (U) owner;
	}

	public SizeBuilder width(int width)
	{
		this.width = width;
		return this;
	}

	public SizeBuilder height(int height)
	{
		this.height = height;
		return this;
	}

	//Size in parent
	public SizeBuilder inheritedWidth()
	{
		widthSupplier = Sizes.parentWidth(owner(), 1.0F, 0);
		return this;
	}

	public SizeBuilder inheritedWidth(int offset)
	{
		widthSupplier = Sizes.parentWidth(owner(), 1.0F, offset);
		return this;
	}

	public SizeBuilder parentWidth(float width, int offset)
	{
		widthSupplier = Sizes.parentWidth(owner(), width, offset);
		return this;
	}

	public SizeBuilder inheritedHeight()
	{
		heightSupplier = Sizes.parentHeight(owner(), 1.0F, 0);
		return this;
	}

	public SizeBuilder inheritedHeight(int offset)
	{
		heightSupplier = Sizes.parentHeight(owner(), 1.0F, offset);
		return this;
	}

	public SizeBuilder parentHeight(float height, int offset)
	{
		heightSupplier = Sizes.parentHeight(owner(), height, offset);
		return this;
	}

	public SizeBuilder widthRelativeTo(ISized other, float width, int offset)
	{
		checkNotNull(other);
		widthSupplier = Sizes.widthRelativeTo(other, width, offset);
		return this;
	}

	public SizeBuilder contentWidth(int offset)
	{
		widthSupplier = Sizes.widthOfContent(owner(), offset);
		return this;
	}

	public SizeBuilder heightRelativeTo(ISized other, float height, int offset)
	{
		checkNotNull(other);
		heightSupplier = Sizes.heightRelativeTo(other, height, offset);
		return this;
	}

	public SizeBuilder contentHeight(int offset)
	{
		heightSupplier = Sizes.heightOfContent(owner(), offset);
		return this;
	}

	public ISize build()
	{
		if (widthSupplier == null && heightSupplier == null)
			return width == 0 && height == 0 ? Size.ZERO : new FixedSize(width, height);
		else if (widthSupplier == null) //heightSupplier not null
			return new FixedWidthSize(width, heightSupplier);
		else if (heightSupplier == null) //widthSupplier not null
			return new FixedHeightSize(widthSupplier, height);
		else
			return new DynamicSize(widthSupplier, heightSupplier);
	}
}
