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

package net.malisis.core.renderer.component;

import net.malisis.core.block.component.SlopeComponent;
import net.malisis.core.renderer.MalisisRenderer;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.element.shape.InvSlope;
import net.malisis.core.renderer.element.shape.Slope;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;

/**
 * @author Ordinastie
 *
 */
public class SlopeShapeComponent extends ShapeComponent
{
	Shape slope;
	Shape invSlope = new InvSlope();

	public SlopeShapeComponent()
	{
		super(new Slope());
		slope = shape;
	}

	@Override
	public void render(Block block, MalisisRenderer<TileEntity> renderer)
	{
		boolean inverted = SlopeComponent.isInverted(renderer.getBlockState());
		shape = inverted ? invSlope : slope;
		super.render(block, renderer);
	}
}
