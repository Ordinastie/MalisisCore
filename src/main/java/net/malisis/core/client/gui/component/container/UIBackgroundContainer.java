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

package net.malisis.core.client.gui.component.container;

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.element.GuiShape;
import net.malisis.core.client.gui.element.GuiVertex.VertexPosition;
import net.malisis.core.renderer.animation.transformation.ITransformable;

/**
 * @author Ordinastie
 *
 */
public class UIBackgroundContainer extends UIContainer<UIBackgroundContainer> implements ITransformable.Color
{
	protected GuiShape shape = new GuiShape();
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

	/**
	 * Instantiates a new {@link UIBackgroundContainer}.
	 *
	 * @param gui the gui
	 * @param title the title
	 */
	public UIBackgroundContainer(String title)
	{
		super(title);
	}

	/**
	 * Instantiates a new {@link UIBackgroundContainer}.
	 *
	 * @param gui the gui
	 * @param width the width
	 * @param height the height
	 */
	public UIBackgroundContainer(int width, int height)
	{
		super(width, height);
	}

	/**
	 * Instantiates a new {@link UIBackgroundContainer}.
	 *
	 * @param gui the gui
	 * @param title the title
	 * @param width the width
	 * @param height the height
	 */
	public UIBackgroundContainer(String title, int width, int height)
	{
		super(title, width, height);
	}

	/**
	 * Sets the border size and color for this {@link UIBackgroundContainer}.
	 *
	 * @param color the color
	 * @param size the size
	 */
	public UIBackgroundContainer setBorder(int color, int size, int alpha)
	{
		borderColor = color;
		borderSize = size;
		borderAlpha = alpha;
		return self();
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
	public UIBackgroundContainer setTopLeftColor(int topLeftColor)
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
	public UIBackgroundContainer setTopRightColor(int topRightColor)
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
	public UIBackgroundContainer setBottomLeftColor(int bottomLeftColor)
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
	public UIBackgroundContainer setBottomRightColor(int bottomRightColor)
	{
		this.bottomRightColor = bottomRightColor;
		return this;
	}

	/**
	 * Sets the top color.
	 *
	 * @param color the new top color
	 */
	public UIBackgroundContainer setTopColor(int color)
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
	public UIBackgroundContainer setBottomColor(int color)
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
	public UIBackgroundContainer setLeftColor(int color)
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
	public UIBackgroundContainer setRightColor(int color)
	{
		setTopRightColor(color);
		setBottomRightColor(color);
		return this;
	}

	/**
	 * Sets the color of this {@link UIBackgroundContainer}.
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
	public UIBackgroundContainer setTopLeftAlpha(int topLeftAlpha)
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
	public UIBackgroundContainer setTopRightAlpha(int topRightAlpha)
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
	public UIBackgroundContainer setBottomLeftAlpha(int bottomLeftAlpha)
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
	public UIBackgroundContainer setBottomRightAlpha(int bottomRightAlpha)
	{
		this.bottomRightAlpha = bottomRightAlpha;
		return this;
	}

	/**
	 * Sets the top alpha.
	 *
	 * @param alpha the new top alpha
	 */
	public UIBackgroundContainer setTopAlpha(int alpha)
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
	public UIBackgroundContainer setBottomAlpha(int alpha)
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
	public UIBackgroundContainer setLeftAlpha(int alpha)
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
	public UIBackgroundContainer setRightAlpha(int alpha)
	{
		setTopRightAlpha(alpha);
		setBottomRightAlpha(alpha);
		return this;
	}

	/**
	 * Sets the alpha background of this {@link UIBackgroundContainer}.
	 *
	 * @param alpha the new alpha
	 */
	public UIBackgroundContainer setBackgroundAlpha(int alpha)
	{
		setTopAlpha(alpha);
		setBottomAlpha(alpha);
		return this;
	}

	@Override
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		if (borderSize > 0)
		{
			shape.setColor(borderColor);
			shape.setAlpha(borderAlpha);
			//top
			shape.setSize(getWidth(), borderSize);
			shape.setPosition(0, 0);
			renderer.drawShape(shape);
			//bottom
			//shape.setSize(getWidth(), borderSize);
			shape.setPosition(0, getHeight() - borderSize);
			renderer.drawShape(shape);
			//left
			shape.setSize(borderSize, getHeight() - borderSize * 2);
			shape.setPosition(0, borderSize);
			renderer.drawShape(shape);
			//right
			//shape.setSize(borderSize, getHeight()- borderSize * 2);
			shape.setPosition(getWidth() - borderSize, borderSize);
			renderer.drawShape(shape);
		}

		shape.setPosition(getX() + borderSize, getY() + borderSize);
		shape.setSize(getWidth() - borderSize * 2, getHeight() - borderSize * 2);

		shape.setColor(VertexPosition.TOPLEFT, topLeftColor);
		shape.setAlpha(VertexPosition.TOPLEFT, topLeftAlpha);
		shape.setColor(VertexPosition.TOPRIGHT, topRightColor);
		shape.setAlpha(VertexPosition.TOPRIGHT, topRightAlpha);
		shape.setColor(VertexPosition.BOTTOMLEFT, bottomLeftColor);
		shape.setAlpha(VertexPosition.BOTTOMLEFT, bottomLeftAlpha);
		shape.setColor(VertexPosition.BOTTOMRIGHT, bottomRightColor);
		shape.setAlpha(VertexPosition.BOTTOMRIGHT, bottomRightAlpha);

		renderer.drawShape(shape);
	}

}
