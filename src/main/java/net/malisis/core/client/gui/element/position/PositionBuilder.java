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

import net.malisis.core.client.gui.element.IChild;
import net.malisis.core.client.gui.element.Size.ISized;
import net.malisis.core.client.gui.element.position.Position.DynamicPosition;
import net.malisis.core.client.gui.element.position.Position.IPosition;
import net.malisis.core.client.gui.element.position.Position.IPositioned;

/**
 * @author Ordinastie
 *
 */
public class PositionBuilder<OWNER, BUILDER>
{
	private BUILDER originalBuilder;
	private boolean backed = false;
	private OWNER owner;
	private IPosition position;
	private int x;
	private int y;
	private Function<OWNER, IntSupplier> xFunction;
	private Function<OWNER, IntSupplier> yFunction;

	public PositionBuilder(OWNER owner)
	{
		this.owner = checkNotNull(owner);
	}

	public PositionBuilder(BUILDER originalBuilder, boolean composed)
	{
		this.originalBuilder = originalBuilder;
	}

	public PositionBuilder<OWNER, BUILDER> set(IPosition position)
	{
		this.position = position;
		return this;
	}

	public PositionBuilder<OWNER, BUILDER> x(int x)
	{
		this.x = x;
		return this;
	}

	public PositionBuilder<OWNER, BUILDER> y(int y)
	{
		this.y = y;
		return this;
	}

	public PositionBuilder<OWNER, BUILDER> x(IntSupplier xSupplier)
	{
		this.xFunction = owner -> xSupplier;
		return this;
	}

	public PositionBuilder<OWNER, BUILDER> y(IntSupplier ySupplier)
	{
		this.yFunction = owner -> ySupplier;
		return this;
	}

	public PositionBuilder<OWNER, BUILDER> x(Function<OWNER, IntSupplier> xFunction)
	{
		this.xFunction = xFunction;
		return this;
	}

	public PositionBuilder<OWNER, BUILDER> y(Function<OWNER, IntSupplier> yFunction)
	{
		this.yFunction = yFunction;
		return this;
	}

	//Position in parent

	public PositionBuilder<OWNER, BUILDER> leftAligned()
	{
		return leftAligned(0);
	}

	public PositionBuilder<OWNER, BUILDER> leftAligned(int x)
	{
		xFunction = owner -> Positions.leftAligned((IChild<?>) owner, x);
		return this;
	}

	public PositionBuilder<OWNER, BUILDER> topAligned()
	{
		return topAligned(0);
	}

	public PositionBuilder<OWNER, BUILDER> topAligned(int y)
	{
		yFunction = owner -> Positions.topAligned((IChild<?>) owner, y);
		return this;
	}

	public PositionBuilder<OWNER, BUILDER> centered()
	{
		return centered(0);
	}

	@SuppressWarnings("unchecked")
	public <T extends ISized & IChild<U>, U extends ISized> PositionBuilder<OWNER, BUILDER> centered(int offset)
	{
		xFunction = owner -> Positions.centered((T) owner, offset);
		return this;
	}

	public PositionBuilder<OWNER, BUILDER> rightAligned()
	{
		return rightAligned(0);
	}

	@SuppressWarnings("unchecked")
	public <T extends ISized & IChild<U>, U extends ISized> PositionBuilder<OWNER, BUILDER> rightAligned(int spacing)
	{
		xFunction = owner -> Positions.rightAligned((T) owner, spacing);
		return this;
	}

	public PositionBuilder<OWNER, BUILDER> middleAligned()
	{
		return middleAligned(0);
	}

	@SuppressWarnings("unchecked")
	public <T extends ISized & IChild<U>, U extends ISized> PositionBuilder<OWNER, BUILDER> middleAligned(int offset)
	{
		yFunction = owner -> Positions.middleAligned((T) owner, offset);
		return this;
	}

	public PositionBuilder<OWNER, BUILDER> bottomAligned()
	{
		return bottomAligned(0);
	}

	@SuppressWarnings("unchecked")
	public <T extends ISized & IChild<U>, U extends ISized> PositionBuilder<OWNER, BUILDER> bottomAligned(int spacing)
	{
		yFunction = owner -> Positions.bottomAligned((T) owner, spacing);
		return this;
	}

	//position relative to other

	public <U extends IPositioned & ISized> PositionBuilder<OWNER, BUILDER> leftOf(U other)
	{
		return leftOf(other, 0);
	}

	public <U extends IPositioned & ISized> PositionBuilder<OWNER, BUILDER> leftOf(U other, int spacing)
	{
		checkNotNull(other);
		xFunction = owner -> Positions.leftOf((ISized) owner, other, spacing);
		return this;
	}

	public <U extends IPositioned & ISized> PositionBuilder<OWNER, BUILDER> rightOf(U other)
	{
		return rightOf(other, 0);
	}

	public <U extends IPositioned & ISized> PositionBuilder<OWNER, BUILDER> rightOf(U other, int spacing)
	{
		checkNotNull(other);
		xFunction = owner -> Positions.rightOf(other, spacing);
		return this;
	}

	public PositionBuilder<OWNER, BUILDER> above(IPositioned other)
	{
		return above(other, 0);
	}

	public PositionBuilder<OWNER, BUILDER> above(IPositioned other, int spacing)
	{
		checkNotNull(other);
		yFunction = owner -> Positions.above((ISized) owner, other, spacing);
		return this;
	}

	public <U extends IPositioned & ISized> PositionBuilder<OWNER, BUILDER> below(U other)
	{
		return below(other, 0);
	}

	public <U extends IPositioned & ISized> PositionBuilder<OWNER, BUILDER> below(U other, int spacing)
	{
		checkNotNull(other);
		yFunction = owner -> Positions.below(other, spacing);
		return this;
	}

	//position aligned to other

	public PositionBuilder<OWNER, BUILDER> leftAlignedTo(IPositioned other)
	{
		return leftAlignedTo(other, 0);
	}

	public PositionBuilder<OWNER, BUILDER> leftAlignedTo(IPositioned other, int offset)
	{
		checkNotNull(other);
		xFunction = owner -> Positions.leftAlignedTo(other, offset);
		return this;
	}

	public <U extends IPositioned & ISized> PositionBuilder<OWNER, BUILDER> rightAlignedTo(U other)
	{
		return rightAlignedTo(other, 0);
	}

	public <U extends IPositioned & ISized> PositionBuilder<OWNER, BUILDER> rightAlignedTo(U other, int offset)
	{
		checkNotNull(other);
		xFunction = owner -> Positions.rightAlignedTo((ISized) owner, other, offset);
		return this;
	}

	public <U extends IPositioned & ISized> PositionBuilder<OWNER, BUILDER> centeredTo(U other)
	{
		return centeredTo(other, 0);
	}

	public <U extends IPositioned & ISized> PositionBuilder<OWNER, BUILDER> centeredTo(U other, int offset)
	{

		checkNotNull(other);
		xFunction = owner -> Positions.centeredTo((ISized) owner, other, offset);
		return this;
	}

	public PositionBuilder<OWNER, BUILDER> topAlignedTo(IPositioned other)
	{
		return topAlignedTo(other, 0);
	}

	public PositionBuilder<OWNER, BUILDER> topAlignedTo(IPositioned other, int offset)
	{

		checkNotNull(other);
		yFunction = owner -> Positions.topAlignedTo(other, offset);
		return this;
	}

	public <U extends IPositioned & ISized> PositionBuilder<OWNER, BUILDER> bottomAlignedTo(U other)
	{
		return bottomAlignedTo(other, 0);
	}

	public <U extends IPositioned & ISized> PositionBuilder<OWNER, BUILDER> bottomAlignedTo(U other, int offset)
	{
		checkNotNull(other);
		yFunction = owner -> Positions.bottomAlignedTo((ISized) owner, other, offset);
		return this;
	}

	public <U extends IPositioned & ISized> PositionBuilder<OWNER, BUILDER> middleAlignedTo(U other)
	{
		return middleAlignedTo(other, 0);
	}

	public <U extends IPositioned & ISized> PositionBuilder<OWNER, BUILDER> middleAlignedTo(U other, int offset)
	{
		checkNotNull(other);
		yFunction = owner -> Positions.middleAlignedTo((ISized) owner, other, offset);
		return this;
	}

	public BUILDER back()
	{
		backed = true;
		return originalBuilder;
	}

	public IPosition build(OWNER owner)
	{
		if (position != null)
			return position;
		return createPosition(owner);
	}

	public IPosition build()
	{
		if (position != null)
			return position;
		if (owner == null && originalBuilder != null && !backed)
			throw new IllegalStateException("Composed builder can't call build without an owner. Use PositionBuilder.build(owner) instead.");
		return createPosition(owner);
	}

	private IPosition createPosition(OWNER owner)
	{
		IntSupplier xSupplier = xFunction != null ? xFunction.apply(owner) : null;
		IntSupplier ySupplier = yFunction != null ? yFunction.apply(owner) : null;
		if (xFunction == null && yFunction == null && x == 0 && y == 0)
			return Position.ZERO;

		return new DynamicPosition(x, y, xSupplier, ySupplier);

	}
}
