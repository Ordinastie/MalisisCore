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

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * @author Ordinastie
 *
 */
public class AnimatedRenderer extends MalisisRenderer<TileEntity> implements IAnimatedRenderer
{
	IAnimatedRenderable renderable;

	public AnimatedRenderer()
	{

	}

	/**
	 * Gets the currently drawn {@link IAnimatedRenderable}.
	 *
	 * @return the renderable
	 */
	public IAnimatedRenderable getRenderable()
	{
		return renderable;
	}

	@Override
	public void renderAnimated(World world, BlockPos pos, IAnimatedRenderable renderable, double x, double y, double z, float partialTicks)
	{
		set(world, pos);
		buffer = Tessellator.getInstance().getBuffer();
		this.renderable = renderable;

		prepare(RenderType.ANIMATED, x, y, z);

		render();

		clean();
	}

	@Override
	public void prepare(RenderType renderType, double... data)
	{
		if (renderType != RenderType.ANIMATED)
			throw new IllegalArgumentException("Wrong renderType set for the AnimatedRenderer : " + renderType);
		this.renderType = renderType;

		GlStateManager.pushAttrib();
		GlStateManager.pushMatrix();
		GlStateManager.disableLighting();

		GlStateManager.translate(data[0], data[1], data[2]);

		bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

		startDrawing();
	}

	@Override
	public void clean()
	{
		draw();
		disableBlending();
		GlStateManager.enableLighting();
		GlStateManager.popMatrix();
		GlStateManager.popAttrib();
	}

	@Override
	public void reset()
	{
		super.reset();
		renderable = null;
	}

	@Override
	public void render()
	{
		renderable.renderAnimated(block, this);
	}
}
