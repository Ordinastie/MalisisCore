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

package net.malisis.core.renderer.model.loader;

import static net.minecraft.util.EnumFacing.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.element.face.BottomFace;
import net.malisis.core.renderer.element.face.EastFace;
import net.malisis.core.renderer.element.face.NorthFace;
import net.malisis.core.renderer.element.face.SouthFace;
import net.malisis.core.renderer.element.face.TopFace;
import net.malisis.core.renderer.element.face.WestFace;
import net.malisis.core.renderer.icon.Icon;
import net.malisis.core.renderer.model.IModelLoader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

/**
 * @author Ordinastie
 *
 */
public class TextureModelLoader implements IModelLoader
{
	private TextureAtlasSprite sprite;
	private int width;
	private int height;
	private int[] pixels;

	private Shape shape;

	public TextureModelLoader(ResourceLocation rl)
	{
		//TODO
	}

	public TextureModelLoader(TextureAtlasSprite sprite)
	{
		load(sprite);
	}

	private void load(TextureAtlasSprite sprite)
	{
		this.sprite = sprite;
		if (sprite.getFrameCount() == 0)
			return;
		//TODO: handle animation
		this.pixels = sprite.getFrameTextureData(0)[0];
		this.width = sprite.getIconWidth();
		this.height = sprite.getIconHeight();

		List<Face> faces = readTexture();

		shape = new Shape(faces);
		shape.translate(-1 / 2F, -1 / 2F, 0);
		shape.scale(1F / width);

	}

	private List<Face> readTexture()
	{
		List<Face> faces = new ArrayList<>();

		Face front = new SouthFace();
		front.scale(width, height, 1);
		front.translate(0, 0, width / 16F - 1);
		front.setTexture(new Icon(sprite));

		Face back = new NorthFace();
		back.scale(width, height, 1);
		back.setTexture(new Icon(sprite), true, false, false);

		faces.add(front);
		faces.add(back);

		RenderParameters params = new RenderParameters();
		params.renderAllFaces.set(true);
		params.calculateAOColor.set(false);
		params.useEnvironmentBrightness.set(false);
		//params.alpha.set(10);

		front.setParameters(params);
		back.setParameters(params);

		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
				addPixelFaces(faces, x, y);
		}

		return faces;
	}

	private void addPixelFaces(List<Face> faces, int x, int y)
	{
		if (isPixelTransparent(x, y))
			return;

		EnumFacing[] facings = new EnumFacing[] { WEST, UP, EAST, DOWN };
		for (EnumFacing facing : facings)
		{
			if (isPixelTransparent(facing, x, y))
			{
				Face face = getFace(facing);
				face.scale(1, 1, width / 16F);
				face.translate(x, height - y - 1, 0);
				//face.translate((faces.size() - 2) / 4, 0, 0);
				applyTexture(face, x, y);
				RenderParameters params = face.getParameters();
				params.renderAllFaces.set(true);
				params.interpolateUV.set(false);
				//params.useEnvironmentBrightness.set(false);
				faces.add(face);
			}
		}
	}

	private Face getFace(EnumFacing facing)
	{
		switch (facing)
		{
			case WEST:
				return new WestFace();
			case UP:
				return new TopFace();
			case EAST:
				return new EastFace();
			case DOWN:
				return new BottomFace();
			default:
				return null;
		}
	}

	private void applyTexture(Face face, int x, int y)
	{
		//x = 0;
		y++;
		float u = sprite.getInterpolatedU(((float) x / width) * 16F + 0.01F);
		float v = sprite.getInterpolatedV(((float) y / height) * 16F - 0.01F);
		float U = sprite.getInterpolatedU(((float) x / width) * 16 + 0.01F);
		float V = sprite.getInterpolatedV(((float) y / height) * 16 - 0.01F);
		face.setTexture(new Icon("", u, v, U, V));
	}

	private int getPixel(int x, int y)
	{
		if (x < 0 || x >= width || y < 0 || y >= height)
			return 0;
		int pos = x + y * width;
		return pixels[pos];
	}

	private boolean isPixelTransparent(int x, int y)
	{
		return (getPixel(x, y) >> 24 & 0xFF) == 0;
	}

	private boolean isPixelTransparent(EnumFacing facing, int x, int y)
	{
		return isPixelTransparent(x + facing.getFrontOffsetX(), y - facing.getFrontOffsetY());
	}

	@Override
	public Map<String, Shape> getShapes()
	{
		HashMap<String, Shape> map = new HashMap<>();
		if (shape != null)
			map.put("shape", shape);
		return map;
	}
}
