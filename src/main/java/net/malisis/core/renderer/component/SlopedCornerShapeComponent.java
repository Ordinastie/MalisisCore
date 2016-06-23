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

import net.malisis.core.block.component.SlopedCornerComponent;
import net.malisis.core.renderer.MalisisRenderer;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.element.shape.DownSlopedCorner;
import net.malisis.core.renderer.element.shape.InvertedDownSlopedCorner;
import net.malisis.core.renderer.element.shape.InvertedSlopedCorner;
import net.malisis.core.renderer.element.shape.SlopedCorner;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;

/**
 * @author Ordinastie
 *
 */
public class SlopedCornerShapeComponent extends ShapeComponent
{
	Shape slopedCorner = new SlopedCorner();
	Shape downSlopedCorner = new DownSlopedCorner();
	Shape invSlopedCorner = new InvertedSlopedCorner();
	Shape invDownSlopedCorner = new InvertedDownSlopedCorner();

	public SlopedCornerShapeComponent()
	{
		super(null); //this.shape set at render time
	}

	@Override
	public void render(Block block, MalisisRenderer<TileEntity> renderer)
	{
		boolean inverted = SlopedCornerComponent.isInverted(renderer.getBlockState());
		boolean isDown = SlopedCornerComponent.isDown(renderer.getBlockState());
		if (inverted)
		{
			shape = isDown ? invDownSlopedCorner : invSlopedCorner;
		}
		else
		{
			shape = isDown ? downSlopedCorner : slopedCorner;
		}

		super.render(block, renderer);
	}
}
