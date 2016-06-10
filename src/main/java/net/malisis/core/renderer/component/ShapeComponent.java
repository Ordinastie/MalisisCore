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

import net.malisis.core.block.component.DirectionalComponent;
import net.malisis.core.renderer.IRenderComponent;
import net.malisis.core.renderer.MalisisRenderer;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.RenderType;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.util.EnumFacingUtils;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

/**
 * @author Ordinastie
 *
 */
public class ShapeComponent implements IRenderComponent
{
	protected Shape shape;

	public ShapeComponent(Shape shape)
	{
		this.shape = shape;
	}

	@Override
	public void render(Block block, MalisisRenderer<TileEntity> renderer)
	{
		RenderParameters rp = new RenderParameters();
		rp.interpolateUV.set(false);
		shape.resetState();
		if (renderer.getRenderType() == RenderType.BLOCK)
		{
			EnumFacing direction = DirectionalComponent.getDirection(renderer.getBlockState());
			shape.rotate(90 * EnumFacingUtils.getRotationCount(direction), 0, 1, 0);
			shape.applyMatrix();
			shape.deductParameters();
		}
		renderer.drawShape(shape, rp);
	}
}
