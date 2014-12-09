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
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.element.SimpleGuiShape;
import net.malisis.core.renderer.animation.transformation.ITransformable;

import org.lwjgl.opengl.GL11;

/**
 * @author Ordinastie
 *
 */
public class UIBackgroundContainer extends UIContainer<UIBackgroundContainer> implements ITransformable.Color
{
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

	/**
	 * Default constructor, creates the components list.
	 *
	 * @param gui the gui
	 */
	public UIBackgroundContainer(MalisisGui gui)
	{
		super(gui);

		shape = new SimpleGuiShape();
	}

	/**
	 * Instantiates a new {@link UIBackgroundContainer}.
	 *
	 * @param gui the gui
	 * @param title the title
	 */
	public UIBackgroundContainer(MalisisGui gui, String title)
	{
		this(gui);
		setTitle(title);
	}

	/**
	 * Instantiates a new {@link UIBackgroundContainer}.
	 *
	 * @param gui the gui
	 * @param width the width
	 * @param height the height
	 */
	public UIBackgroundContainer(MalisisGui gui, int width, int height)
	{
		this(gui);
		setSize(width, height);
	}

	/**
	 * Instantiates a new {@link UIBackgroundContainer}.
	 *
	 * @param gui the gui
	 * @param title the title
	 * @param width the width
	 * @param height the height
	 */
	public UIBackgroundContainer(MalisisGui gui, String title, int width, int height)
	{
		this(gui);
		setTitle(title);
		setSize(width, height);
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
	public void setTopLeftColor(int topLeftColor)
	{
		this.topLeftColor = topLeftColor;
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
	public void setTopRightColor(int topRightColor)
	{
		this.topRightColor = topRightColor;
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
	public void setBottomLeftColor(int bottomLeftColor)
	{
		this.bottomLeftColor = bottomLeftColor;
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
	public void setBottomRightColor(int bottomRightColor)
	{
		this.bottomRightColor = bottomRightColor;
	}

	/**
	 * Sets the top color.
	 *
	 * @param color the new top color
	 */
	public void setTopColor(int color)
	{
		setTopLeftColor(color);
		setTopRightColor(color);
	}

	/**
	 * Sets the bottom color.
	 *
	 * @param color the new bottom color
	 */
	public void setBottomColor(int color)
	{
		setBottomLeftColor(color);
		setBottomRightColor(color);
	}

	/**
	 * Sets the left color.
	 *
	 * @param color the new left color
	 */
	public void setLeftColor(int color)
	{
		setTopLeftColor(color);
		setBottomLeftColor(color);
	}

	/**
	 * Sets the right color.
	 *
	 * @param color the new right color
	 */
	public void setRightColor(int color)
	{
		setTopRightColor(color);
		setBottomRightColor(color);
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
	public void setTopLeftAlpha(int topLeftAlpha)
	{
		this.topLeftAlpha = topLeftAlpha;
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
	public void setTopRightAlpha(int topRightAlpha)
	{
		this.topRightAlpha = topRightAlpha;
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
	public void setBottomLeftAlpha(int bottomLeftAlpha)
	{
		this.bottomLeftAlpha = bottomLeftAlpha;
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
	public void setBottomRightAlpha(int bottomRightAlpha)
	{
		this.bottomRightAlpha = bottomRightAlpha;
	}

	/**
	 * Sets the top alpha.
	 *
	 * @param alpha the new top alpha
	 */
	public void setTopAlpha(int alpha)
	{
		setTopLeftAlpha(alpha);
		setTopRightAlpha(alpha);
	}

	/**
	 * Sets the bottom alpha.
	 *
	 * @param alpha the new bottom alpha
	 */
	public void setBottomAlpha(int alpha)
	{
		setBottomLeftAlpha(alpha);
		setBottomRightAlpha(alpha);
	}

	/**
	 * Sets the left alpha.
	 *
	 * @param alpha the new left alpha
	 */
	public void setLeftAlpha(int alpha)
	{
		setTopLeftAlpha(alpha);
		setBottomLeftAlpha(alpha);
	}

	/**
	 * Sets the right alpha.
	 *
	 * @param alpha the new right alpha
	 */
	public void setRightAlpha(int alpha)
	{
		setTopRightAlpha(alpha);
		setBottomRightAlpha(alpha);
	}

	/**
	 * Sets the alpha background of this {@link UIBackgroundContainer}.
	 *
	 * @param alpha the new alpha
	 */
	public void setBackgroundAlpha(int alpha)
	{
		setTopAlpha(alpha);
		setBottomAlpha(alpha);
	}

	@Override
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		if (alpha == 0)
			return;

		renderer.enableBlending();
		rp.usePerVertexColor.set(true);
		rp.usePerVertexAlpha.set(true);
		shape.getVertexes("TopLeft").get(0).setColor(topLeftColor).setAlpha(topLeftAlpha);
		shape.getVertexes("TopRight").get(0).setColor(topRightColor).setAlpha(topRightAlpha);
		shape.getVertexes("BottomLeft").get(0).setColor(bottomLeftColor).setAlpha(bottomLeftAlpha);
		shape.getVertexes("BottomRight").get(0).setColor(bottomRightColor).setAlpha(bottomRightAlpha);

		renderer.disableTextures();

		renderer.drawShape(shape, rp);
		renderer.next();

		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

}
