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

package net.malisis.core.client.gui.render;

import java.util.EnumMap;
import java.util.Map;

import com.google.common.collect.Maps;

import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.render.shape.FacePosition;
import net.malisis.core.client.gui.render.shape.GuiShape;
import net.malisis.core.client.gui.render.shape.VertexPosition;

/**
 * @author Ordinastie
 *
 */
public class ColoredBackground implements IGuiRenderer
{
	/** Colors per vertex. */
	protected final Map<VertexPosition, Integer> colors;
	/** Alphas per vertex. */
	protected final Map<VertexPosition, Integer> alphas;

	/** Border size **/
	protected final int borderSize = 0;
	/** Border color **/
	protected final int borderColor = 0;
	/** Border alpha **/
	protected final int borderAlpha = 0;

	protected final GuiShape shape;

	public ColoredBackground(UIComponent component, EnumMap<VertexPosition, Integer> colors, EnumMap<VertexPosition, Integer> alphas, int borderSize, int borderColor, int borderAlpha)
	{
		this.colors = Maps.immutableEnumMap(colors);
		this.alphas = Maps.immutableEnumMap(alphas);
		shape = GuiShape.builder(component).color(this::color).alpha(this::alpha).border(borderSize, borderColor, borderAlpha).build();
	}

	public int color(FacePosition fp, VertexPosition vp)
	{
		if (borderSize > 0 && fp != FacePosition.CENTER)
			return borderColor;
		return colors.getOrDefault(vp, 0xFFFFFF);
	}

	public int alpha(FacePosition fp, VertexPosition vp)
	{
		return alphas.getOrDefault(vp, 255);
	}

	@Override
	public void render(GuiRenderer renderer)
	{
		shape.render(renderer);
	}

	public static Builder of(UIComponent component)
	{
		return new Builder(component);
	}

	public static class Builder
	{
		private UIComponent component;
		private int borderSize;
		private int borderColor;
		private int borderAlpha;
		private EnumMap<VertexPosition, Integer> colors = Maps.newEnumMap(VertexPosition.class);
		private EnumMap<VertexPosition, Integer> alphas = Maps.newEnumMap(VertexPosition.class);

		public Builder(UIComponent component)
		{
			this.component = component;
		}

		public Builder color(int color)
		{
			colors.put(VertexPosition.TOPLEFT, color);
			colors.put(VertexPosition.TOPRIGHT, color);
			colors.put(VertexPosition.BOTTOMLEFT, color);
			colors.put(VertexPosition.BOTTOMRIGHT, color);
			return this;
		}

		public Builder alpha(int alpha)
		{
			alphas.put(VertexPosition.TOPLEFT, alpha);
			alphas.put(VertexPosition.TOPRIGHT, alpha);
			alphas.put(VertexPosition.BOTTOMLEFT, alpha);
			alphas.put(VertexPosition.BOTTOMRIGHT, alpha);
			return this;
		}

		public Builder colorFor(VertexPosition position, int color)
		{
			colors.put(position, color);
			return this;
		}

		public Builder alphaFor(VertexPosition position, int alpha)
		{
			alphas.put(position, alpha);
			return this;
		}

		public Builder topColor(int color)
		{
			colors.put(VertexPosition.TOPLEFT, color);
			colors.put(VertexPosition.TOPRIGHT, color);
			return this;
		}

		public Builder bottomColor(int color)
		{
			colors.put(VertexPosition.BOTTOMLEFT, color);
			colors.put(VertexPosition.BOTTOMRIGHT, color);
			return this;
		}

		public Builder leftColor(int color)
		{
			colors.put(VertexPosition.TOPLEFT, color);
			colors.put(VertexPosition.BOTTOMLEFT, color);
			return this;
		}

		public Builder rightColor(int color)
		{
			colors.put(VertexPosition.TOPRIGHT, color);
			colors.put(VertexPosition.BOTTOMRIGHT, color);
			return this;
		}

		public Builder topAlpha(int alpha)
		{
			alphas.put(VertexPosition.TOPLEFT, alpha);
			alphas.put(VertexPosition.TOPRIGHT, alpha);
			return this;
		}

		public Builder bottomAlpha(int alpha)
		{
			alphas.put(VertexPosition.BOTTOMLEFT, alpha);
			alphas.put(VertexPosition.BOTTOMRIGHT, alpha);
			return this;
		}

		public Builder leftAlpha(int alpha)
		{
			alphas.put(VertexPosition.TOPLEFT, alpha);
			alphas.put(VertexPosition.BOTTOMLEFT, alpha);
			return this;
		}

		public Builder rightAlpha(int alpha)
		{
			alphas.put(VertexPosition.TOPRIGHT, alpha);
			alphas.put(VertexPosition.BOTTOMRIGHT, alpha);
			return this;
		}

		public Builder border(int size, int color)
		{
			borderSize = size;
			borderColor = color;
			borderAlpha = 255;
			return this;
		}

		public Builder border(int size, int color, int alpha)
		{
			borderSize = size;
			borderColor = color;
			borderAlpha = alpha;
			return this;
		}

		public ColoredBackground build()
		{
			return new ColoredBackground(component, colors, alphas, borderSize, borderColor, borderAlpha);
		}
	}
}
