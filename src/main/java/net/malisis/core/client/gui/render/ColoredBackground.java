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

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.Padding;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.element.GuiShape;
import net.malisis.core.client.gui.element.SimpleGuiShape;
import net.malisis.core.client.gui.element.XYResizableGuiShape;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.animation.transformation.ITransformable;
import net.malisis.core.renderer.element.Face;

/**
 * @author Ordinastie
 *
 */
public class ColoredBackground implements IGuiRenderer, ITransformable.Color, ITransformable.Alpha
{
	protected GuiShape shape = new SimpleGuiShape();
	protected RenderParameters rp = new RenderParameters();
	/** Top left corner color **/
	protected int topLeftColor = -1;
	/** Top right corner color **/
	protected int topRightColor = -1;
	/** Bottom left corner color */
	protected int bottomLeftColor = -1;
	/** Bottom right corner color */
	protected int bottomRightColor = -1;
	/** Top left corner alpha **/
	protected int topLeftAlpha = 255;
	/** Top right corner alpha **/
	protected int topRightAlpha = 255;
	/** Bottom left corner alpha */
	protected int bottomLeftAlpha = 255;
	/** Bottom right corner alpha */
	protected int bottomRightAlpha = 255;
	/** Border size **/
	protected int borderSize = 0;
	/** Border color **/
	protected int borderColor = 0;
	/** Border alpha **/
	protected int borderAlpha = 0;

	protected Padding padding = Padding.NO_PADDING;

	public ColoredBackground(int color, int borderSize, int borderColor)
	{
		setColor(color);
		setBorder(borderColor, borderSize, 255);
	}

	public ColoredBackground(int color)
	{
		setColor(color);
	}

	/**
	 * Sets the border size and color for this {@link ColoredBackground}.
	 *
	 * @param color the color
	 * @param size the size
	 */
	public ColoredBackground setBorder(int color, int size, int alpha)
	{
		borderColor = color;
		borderSize = size;
		borderAlpha = alpha;
		if (size >= 0)
			shape = new XYResizableGuiShape(size);
		else
			shape = new SimpleGuiShape();

		padding = borderSize == 0 ? Padding.NO_PADDING : Padding.of(borderSize);
		return this;
	}

	@Override
	public Padding getPadding()
	{
		return padding;
	}

	/**
	 * Gets the top left color.
	 *
	 * @return the top left color
	 */
	public int getTopLeftColor()
	{
		return topLeftColor;
	}

	/**
	 * Sets the top left color.
	 *
	 * @param topLeftColor the new top left color
	 */
	public ColoredBackground setTopLeftColor(int topLeftColor)
	{
		this.topLeftColor = topLeftColor;
		return this;
	}

	/**
	 * Gets the top right color.
	 *
	 * @return the top right color
	 */
	public int getTopRightColor()
	{
		return topRightColor;
	}

	/**
	 * Sets the top right color.
	 *
	 * @param topRightColor the new top right color
	 */
	public ColoredBackground setTopRightColor(int topRightColor)
	{
		this.topRightColor = topRightColor;
		return this;
	}

	/**
	 * Gets the bottom left color.
	 *
	 * @return the bottom left color
	 */
	public int getBottomLeftColor()
	{
		return bottomLeftColor;
	}

	/**
	 * Sets the bottom left color.
	 *
	 * @param bottomLeftColor the new bottom left color
	 */
	public ColoredBackground setBottomLeftColor(int bottomLeftColor)
	{
		this.bottomLeftColor = bottomLeftColor;
		return this;
	}

	/**
	 * Gets the bottom right color.
	 *
	 * @return the bottom right color
	 */
	public int getBottomRightColor()
	{
		return bottomRightColor;
	}

	/**
	 * Sets the bottom right color.
	 *
	 * @param bottomRightColor the new bottom right color
	 */
	public ColoredBackground setBottomRightColor(int bottomRightColor)
	{
		this.bottomRightColor = bottomRightColor;
		return this;
	}

	/**
	 * Sets the top color.
	 *
	 * @param color the new top color
	 */
	public ColoredBackground setTopColor(int color)
	{
		setTopLeftColor(color);
		setTopRightColor(color);
		return this;
	}

	/**
	 * Sets the bottom color.
	 *
	 * @param color the new bottom color
	 */
	public ColoredBackground setBottomColor(int color)
	{
		setBottomLeftColor(color);
		setBottomRightColor(color);
		return this;
	}

	/**
	 * Sets the left color.
	 *
	 * @param color the new left color
	 */
	public ColoredBackground setLeftColor(int color)
	{
		setTopLeftColor(color);
		setBottomLeftColor(color);
		return this;
	}

	/**
	 * Sets the right color.
	 *
	 * @param color the new right color
	 */
	public ColoredBackground setRightColor(int color)
	{
		setTopRightColor(color);
		setBottomRightColor(color);
		return this;
	}

	/**
	 * Sets the color of this {@link ColoredBackground}.
	 *
	 * @param color the new color
	 */
	@Override
	public void setColor(int color)
	{
		setTopColor(color);
		setBottomColor(color);
	}

	/**
	 * Gets the top left alpha.
	 *
	 * @return the top left alpha
	 */
	public int getTopLeftAlpha()
	{
		return topLeftAlpha;
	}

	/**
	 * Sets the top left alpha.
	 *
	 * @param topLeftAlpha the new top left alpha
	 */
	public ColoredBackground setTopLeftAlpha(int topLeftAlpha)
	{
		this.topLeftAlpha = topLeftAlpha;
		return this;
	}

	/**
	 * Gets the top right alpha.
	 *
	 * @return the top right alpha
	 */
	public int getTopRightAlpha()
	{
		return topRightAlpha;
	}

	/**
	 * Sets the top right alpha.
	 *
	 * @param topRightAlpha the new top right alpha
	 */
	public ColoredBackground setTopRightAlpha(int topRightAlpha)
	{
		this.topRightAlpha = topRightAlpha;
		return this;
	}

	/**
	 * Gets the bottom left alpha.
	 *
	 * @return the bottom left alpha
	 */
	public int getBottomLeftAlpha()
	{
		return bottomLeftAlpha;
	}

	/**
	 * Sets the bottom left alpha.
	 *
	 * @param bottomLeftAlpha the new bottom left alpha
	 */
	public ColoredBackground setBottomLeftAlpha(int bottomLeftAlpha)
	{
		this.bottomLeftAlpha = bottomLeftAlpha;
		return this;
	}

	/**
	 * Gets the bottom right alpha.
	 *
	 * @return the bottom right alpha
	 */
	public int getBottomRightAlpha()
	{
		return bottomRightAlpha;
	}

	/**
	 * Sets the bottom right alpha.
	 *
	 * @param bottomRightAlpha the new bottom right alpha
	 */
	public ColoredBackground setBottomRightAlpha(int bottomRightAlpha)
	{
		this.bottomRightAlpha = bottomRightAlpha;
		return this;
	}

	/**
	 * Sets the top alpha.
	 *
	 * @param alpha the new top alpha
	 */
	public ColoredBackground setTopAlpha(int alpha)
	{
		setTopLeftAlpha(alpha);
		setTopRightAlpha(alpha);
		return this;
	}

	/**
	 * Sets the bottom alpha.
	 *
	 * @param alpha the new bottom alpha
	 */
	public ColoredBackground setBottomAlpha(int alpha)
	{
		setBottomLeftAlpha(alpha);
		setBottomRightAlpha(alpha);
		return this;
	}

	/**
	 * Sets the left alpha.
	 *
	 * @param alpha the new left alpha
	 */
	public ColoredBackground setLeftAlpha(int alpha)
	{
		setTopLeftAlpha(alpha);
		setBottomLeftAlpha(alpha);
		return this;
	}

	/**
	 * Sets the right alpha.
	 *
	 * @param alpha the new right alpha
	 */
	public ColoredBackground setRightAlpha(int alpha)
	{
		setTopRightAlpha(alpha);
		setBottomRightAlpha(alpha);
		return this;
	}

	/**
	 * Sets the alpha background of this {@link ColoredBackground}.
	 *
	 * @param alpha the new alpha
	 */
	@Override
	public void setAlpha(int alpha)
	{
		setTopAlpha(alpha);
		setBottomAlpha(alpha);
	}

	@Override
	public void render(UIComponent<?> component, GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		shape.resetState();
		shape.setSize(component.getWidth(), component.getHeight());

		Face f = shape.getFaces()[0];
		if (borderSize != 0)
		{
			f = shape.getFaces()[4];
			rp.colorMultiplier.set(borderColor);
			rp.alpha.set(borderAlpha);
		}

		RenderParameters frp = f.getParameters();
		frp.usePerVertexColor.set(true);
		frp.usePerVertexAlpha.set(true);
		f.getVertexes("TopLeft").get(0).setColor(topLeftColor).setAlpha(topLeftAlpha);
		f.getVertexes("TopRight").get(0).setColor(topRightColor).setAlpha(topRightAlpha);
		f.getVertexes("BottomLeft").get(0).setColor(bottomLeftColor).setAlpha(bottomLeftAlpha);
		f.getVertexes("BottomRight").get(0).setColor(bottomRightColor).setAlpha(bottomRightAlpha);

		renderer.disableTextures();

		renderer.drawShape(shape, rp);
		renderer.next();

		renderer.enableTextures();
	}

}
