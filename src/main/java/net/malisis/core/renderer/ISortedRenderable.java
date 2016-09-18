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

package net.malisis.core.renderer;

import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

/**
 * @author Ordinastie
 *
 */
public interface ISortedRenderable
{
	public BlockPos getPos();

	public boolean inFrustrum(ICamera camera);

	public boolean isPositionStillValid();

	public void render(float partialTick);

	/**
	 * {@link ISortedRenderable} wrapper for {@link TileEntity}.
	 */
	public static class TE implements ISortedRenderable
	{
		private TileEntity te;

		public TE(TileEntity te)
		{
			this.te = te;
		}

		@Override
		public boolean inFrustrum(ICamera camera)
		{
			return camera.isBoundingBoxInFrustum(te.getRenderBoundingBox());
		}

		@Override
		public boolean isPositionStillValid()
		{
			return true;
		}

		@Override
		public BlockPos getPos()
		{
			return te.getPos();
		}

		@Override
		public void render(float partialTicks)
		{
			TileEntityRendererDispatcher.instance.renderTileEntity(te, partialTicks, -1);
		}
	}

}