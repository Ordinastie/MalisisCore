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

package net.malisis.core.client.gui.element;

import static com.google.common.base.Preconditions.*;

import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.renderer.element.Vertex;

/**
 * @author Ordinastie
 *
 */
public class GuiShape
{
	public static enum VertexPosition
	{
		TOPLEFT(0, 0),
		TOPRIGHT(1, 0),
		BOTTOMRIGHT(1, 1),
		BOTTOMLEFT(0, 1);

		//@formatter:off
		private int x, y;
		VertexPosition(int x, int y) { this.x = x; this.y = y; }
		public int getX() { return x; }
		public int getY() { return y; }
		public int getX(int x, int width) { return x + this.x * width; }
		public int getY(int y, int height) { return y + this.y * height; }
		//@formatter:on
	}

	public static final List<VertexPosition> positions = ImmutableList.of(	VertexPosition.TOPLEFT,
																			VertexPosition.BOTTOMLEFT,
																			VertexPosition.BOTTOMRIGHT,
																			VertexPosition.TOPRIGHT);

	private final Supplier<ShapePosition> position;
	private final IntSupplier zIndex;
	private final Supplier<ShapeSize> size;
	private final ToIntFunction<VertexPosition> color;
	private final ToIntFunction<VertexPosition> alpha;
	private final Supplier<GuiIcon> icon;

	private GuiShape(Supplier<ShapePosition> position, IntSupplier zIndex, Supplier<ShapeSize> size, ToIntFunction<VertexPosition> color, ToIntFunction<VertexPosition> alpha, Supplier<GuiIcon> icon)
	{
		this.position = position;
		this.zIndex = zIndex;
		this.size = size;
		this.color = color;
		this.alpha = alpha;
		this.icon = icon;
	}

	public ShapePosition getPosition()
	{
		return position != null ? position.get() : ShapePosition.ZERO;
	}

	public int getZIndex()
	{
		return zIndex != null ? zIndex.getAsInt() : 0;
	}

	public ShapeSize getSize()
	{
		return size != null ? size.get() : ShapeSize.ZERO;
	}

	public int getColor(VertexPosition vertexPosition)
	{
		if (color == null)
			return 0xFFFFFF;
		return color.applyAsInt(vertexPosition);
	}

	public int getAlpha(VertexPosition vertexPosition)
	{
		if (alpha == null)
			return 255;
		return alpha.applyAsInt(vertexPosition);
	}

	public GuiIcon getIcon()
	{
		if (icon == null)
			return GuiIcon.NONE;
		return Objects.firstNonNull(icon.get(), GuiIcon.NONE);
	}

	public void render()
	{
		ShapePosition p = getPosition();
		ShapeSize s = getSize();
		int z = getZIndex();
		GuiIcon i = getIcon();

		positions.forEach(vp -> GuiRenderer.buffer.addVertexData(getVertexData(vp, p, s, z, i)));
	}

	private int flipColor(int color)
	{
		int r = (color >> 16) & 0xFF;
		int g = (color >> 8) & 0xFF;
		int b = (color) & 0xFF;

		return (b << 16) + (g << 8) + r;
	}

	/**
	 * Gets the vertex data for this {@link Vertex}.
	 *
	 * @param vertexFormat the vertex format
	 * @param offset the offset
	 * @return the vertex data
	 */
	public int[] getVertexData(VertexPosition position, ShapePosition p, ShapeSize s, int z, GuiIcon icon)
	{
		float x = position.getX(p.getX(), s.getWidth());
		float y = position.getY(p.getY(), s.getHeight());
		int rgba = flipColor(getColor(position)) + (getAlpha(position) << 24);

		//index 0
		float u = icon.getInterpolatedU(position.getX());
		float v = icon.getInterpolatedV(position.getY());
		float px = position.getX();
		float py = position.getY();
		//index 1
		float minU = icon.getMinU();
		float minV = icon.getMinV();
		float maxU = icon.getMaxU();
		float maxV = icon.getMaxV();
		//index 2
		float textureWidth = icon.getTextureWidth();
		float textureHeight = icon.getTextureHeight();
		float width = s.getWidth();
		float height = s.getHeight();
		//index 3
		int border = icon.getBorder();

		return new int[] {	Float.floatToRawIntBits(x),
							Float.floatToRawIntBits(y),
							Float.floatToRawIntBits(z),
							rgba,
							//index 0
							Float.floatToRawIntBits(u),
							Float.floatToRawIntBits(v),
							Float.floatToRawIntBits(px),
							Float.floatToRawIntBits(py),
							//index 1
							Float.floatToRawIntBits(minU),
							Float.floatToRawIntBits(minV),
							Float.floatToRawIntBits(maxU),
							Float.floatToRawIntBits(maxV),
							//index 2
							Float.floatToRawIntBits(textureWidth),
							Float.floatToRawIntBits(textureHeight),
							Float.floatToRawIntBits(width),
							Float.floatToRawIntBits(height),
							Float.floatToRawIntBits(border) };
	}

	public static Builder builder()
	{
		return new Builder();
	}

	public static Builder builder(UIComponent<?> component)
	{
		return new Builder().forComponent(component);
	}

	public static class Builder
	{
		private Supplier<ShapePosition> position;
		private IntSupplier zIndex;
		private Supplier<ShapeSize> size;
		private ToIntFunction<VertexPosition> color;
		private ToIntFunction<VertexPosition> alpha;
		private Supplier<GuiIcon> icon;

		public Builder forComponent(UIComponent<?> component)
		{
			position = ShapePosition.fromComponent(component);
			size = ShapeSize.fromComponent(component);
			return this;
		}

		public Builder position(int x, int y)
		{
			position = () -> ShapePosition.of(x, y);
			return this;
		}

		public Builder position(ShapePosition sp)
		{
			checkNotNull(sp);
			position = () -> sp;
			return this;
		}

		public Builder position(Supplier<ShapePosition> supplier)
		{
			position = checkNotNull(supplier);
			return this;
		}

		public Builder zIndex(int z)
		{
			zIndex = () -> z;
			return this;
		}

		public Builder zIndex(IntSupplier supplier)
		{
			zIndex = checkNotNull(supplier);
			return this;
		}

		public Builder size(int w, int h)
		{
			size = () -> ShapeSize.of(w, h);
			return this;
		}

		public Builder size(ShapeSize ss)
		{
			checkNotNull(ss);
			size = () -> ss;
			return this;
		}

		public Builder size(Supplier<ShapeSize> supplier)
		{
			size = checkNotNull(supplier);
			return this;
		}

		public Builder color(int c)
		{
			color = (vp) -> c;
			return this;
		}

		public Builder color(IntSupplier supplier)
		{
			color = (vp) -> supplier.getAsInt();
			return this;
		}

		public Builder color(ToIntFunction<VertexPosition> func)
		{
			color = checkNotNull(func);
			return this;
		}

		public Builder alpha(int a)
		{
			this.alpha = (vp) -> a;
			return this;
		}

		public Builder alpha(IntSupplier supplier)
		{
			alpha = (vp) -> supplier.getAsInt();
			return this;
		}

		public Builder alpha(ToIntFunction<VertexPosition> func)
		{
			alpha = checkNotNull(func);
			return this;
		}

		public Builder icon(GuiIcon i)
		{
			icon = () -> i;
			return this;
		}

		public Builder icon(Supplier<GuiIcon> supplier)
		{
			icon = checkNotNull(supplier);
			return this;
		}

		public GuiShape build()
		{
			if (size == null)
				throw new IllegalStateException("No size specified for the GuiShape.");
			return new GuiShape(position, zIndex, size, color, alpha, icon);
		}
	}

	public static class ShapePosition
	{
		private static final ShapePosition ZERO = ShapePosition.of(0, 0);
		private final int x;
		private final int y;

		private ShapePosition(int x, int y)
		{
			this.x = x;
			this.y = y;
		}

		public int getX()
		{
			return x;
		}

		public int getY()
		{
			return y;
		}

		public static ShapePosition of(int x, int y)
		{
			return new ShapePosition(x, y);
		}

		public static Supplier<ShapePosition> fromComponent(UIComponent<?> component, int offsetX, int offsetY)
		{
			return () -> ShapePosition.of(component.screenX() + offsetX, component.screenY() + offsetY);
		}

		public static Supplier<ShapePosition> fromComponent(UIComponent<?> component)
		{
			return fromComponent(component, 0, 0);
		}
	}

	public static class ShapeSize
	{
		private static final ShapeSize ZERO = ShapeSize.of(0, 0);
		private final int width;
		private final int height;

		private ShapeSize(int width, int height)
		{
			this.width = width;
			this.height = height;
		}

		public int getWidth()
		{
			return width;
		}

		public int getHeight()
		{
			return height;
		}

		public static ShapeSize of(int width, int height)
		{
			return new ShapeSize(width, height);
		}

		public static Supplier<ShapeSize> fromComponent(UIComponent<?> component)
		{
			return () -> ShapeSize.of(component.getWidth(), component.getHeight());
		}
	}

}