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

import static com.google.common.base.Preconditions.*;

import java.util.function.ToIntFunction;

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.element.GuiShape;
import net.malisis.core.client.gui.element.GuiShape.VertexPosition;
import net.malisis.core.renderer.animation.transformation.ITransformable;

/**
 * @author Ordinastie
 *
 */
public class UIBackgroundContainer extends UIContainer<UIBackgroundContainer> implements ITransformable.Color
{
	/** Colors for each vertex position **/
	protected int[] colors = { -1, -1, -1, -1 };
	/** Alpha values for each vertex position **/
	protected int[] alphas = { 255, 255, 255, 255 };

	/** Border size **/
	protected int borderSize = 0;
	/** Border color **/
	protected int borderColor = 0;
	/** Border alpha **/
	protected int borderAlpha = 0;

	protected GuiShape shape = GuiShape	.builder()
										.forComponent(this)
										.color(this::getColor)
										.alpha((ToIntFunction<VertexPosition>) this::getAlpha)
										.build();

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
	 * Instantiates a new {@link UIBackgroundContainer}.
	 *
	 * @param gui the gui
	 * @param title the title
	 */
	public UIBackgroundContainer(String title)
	{
		this(title, UIComponent.INHERITED, UIComponent.INHERITED);
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
		this(null, width, height);
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
	 * Sets the color for the {@link VertexPosition}.
	 *
	 * @param position the position
	 * @param color the color
	 * @return the UI background container
	 */
	public UIBackgroundContainer setColor(VertexPosition position, int color)
	{
		colors[checkNotNull(position).ordinal()] = color;
		return this;
	}

	/**
	 * Gets the color for the {@link VertexPosition}.
	 *
	 * @param position the position
	 * @return the color
	 */
	public int getColor(VertexPosition position)
	{
		return colors[checkNotNull(position).ordinal()];
	}

	/**
	 * Sets the top color.
	 *
	 * @param color the new top color
	 */
	public UIBackgroundContainer setTopColor(int color)
	{
		setColor(VertexPosition.TOPLEFT, color);
		setColor(VertexPosition.TOPRIGHT, color);
		return this;
	}

	/**
	 * Sets the bottom color.
	 *
	 * @param color the new bottom color
	 */
	public UIBackgroundContainer setBottomColor(int color)
	{
		setColor(VertexPosition.BOTTOMLEFT, color);
		setColor(VertexPosition.BOTTOMRIGHT, color);
		return this;
	}

	/**
	 * Sets the left color.
	 *
	 * @param color the new left color
	 */
	public UIBackgroundContainer setLeftColor(int color)
	{
		setColor(VertexPosition.TOPLEFT, color);
		setColor(VertexPosition.BOTTOMLEFT, color);
		return this;
	}

	/**
	 * Sets the right color.
	 *
	 * @param color the new right color
	 */
	public UIBackgroundContainer setRightColor(int color)
	{
		setColor(VertexPosition.TOPRIGHT, color);
		setColor(VertexPosition.BOTTOMRIGHT, color);
		return this;
	}

	/**
	 * Sets the color for all the {@link VertexPosition}.
	 *
	 * @param color the new color
	 */
	@Override
	public void setColor(int color)
	{
		colors = new int[] { color, color, color, color };
	}

	/**
	 * Sets the alpha for the {@link VertexPosition}.
	 *
	 * @param position the position
	 * @param alpha the alpha
	 * @return the UI background container
	 */
	public UIBackgroundContainer setAlpha(VertexPosition position, int alpha)
	{
		alphas[checkNotNull(position).ordinal()] = alpha;
		return this;
	}

	/**
	 * Gets the alpha for the {@link VertexPosition}.
	 *
	 * @param position the position
	 * @return the alpha
	 */
	public int getAlpha(VertexPosition position)
	{
		return alphas[checkNotNull(position).ordinal()];
	}

	/**
	 * Sets the top alpha.
	 *
	 * @param alpha the new top alpha
	 */
	public UIBackgroundContainer setTopAlpha(int alpha)
	{
		setAlpha(VertexPosition.TOPLEFT, alpha);
		setAlpha(VertexPosition.TOPRIGHT, alpha);
		return this;
	}

	/**
	 * Sets the bottom alpha.
	 *
	 * @param alpha the new bottom alpha
	 */
	public UIBackgroundContainer setBottomAlpha(int alpha)
	{
		setAlpha(VertexPosition.BOTTOMLEFT, alpha);
		setAlpha(VertexPosition.BOTTOMRIGHT, alpha);
		return this;
	}

	/**
	 * Sets the left alpha.
	 *
	 * @param alpha the new left alpha
	 */
	public UIBackgroundContainer setLeftAlpha(int alpha)
	{
		setAlpha(VertexPosition.TOPLEFT, alpha);
		setAlpha(VertexPosition.BOTTOMLEFT, alpha);
		return this;
	}

	/**
	 * Sets the right alpha.
	 *
	 * @param alpha the new right alpha
	 */
	public UIBackgroundContainer setRightAlpha(int alpha)
	{
		setAlpha(VertexPosition.TOPRIGHT, alpha);
		setAlpha(VertexPosition.BOTTOMRIGHT, alpha);
		return this;
	}

	/**
	 * Sets the alpha for all the {@link VertexPosition}.
	 *
	 * @param alpha the new alpha
	 */
	public void setBackgroundAlpha(int alpha)
	{
		alphas = new int[] { alpha, alpha, alpha, alpha };
	}

	@Override
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		if (borderSize > 0)
		{
			//			shape.setColor(borderColor);
			//			shape.setAlpha(borderAlpha);
			//			//top
			//			shape.setSize(getWidth(), borderSize);
			//			shape.setPosition(0, 0);
			//			renderer.drawShape(shape);
			//			//bottom
			//			//shape.setSize(getWidth(), borderSize);
			//			shape.setPosition(0, getHeight() - borderSize);
			//			renderer.drawShape(shape);
			//			//left
			//			shape.setSize(borderSize, getHeight() - borderSize * 2);
			//			shape.setPosition(0, borderSize);
			//			renderer.drawShape(shape);
			//			//right
			//			//shape.setSize(borderSize, getHeight()- borderSize * 2);
			//			shape.setPosition(getWidth() - borderSize, borderSize);
			//			renderer.drawShape(shape);
		}

		shape.render();
	}

}
