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

import org.lwjgl.util.vector.Matrix4f;

import com.google.common.collect.Lists;

import net.malisis.core.client.gui.element.GuiVertex.VertexPosition;
import net.minecraft.client.renderer.VertexBuffer;

/**
 * @author Ordinastie
 *
 */
public class GuiShape
{
	List<GuiVertex> vertexes = Lists.newArrayList();
	/** The matrix containing all the transformations applied to this {@link GuiShape}. */
	protected Matrix4f matrix = new Matrix4f();

	private int x;
	private int y;
	private int zIndex;
	private int width;
	private int height;

	private GuiIcon icon;

	public GuiShape()
	{
		for (VertexPosition p : VertexPosition.values())
			vertexes.add(new GuiVertex(p));
	}

	public GuiShape(GuiIcon icon)
	{
		this();
		setIcon(icon);
	}

	public GuiShape(int x, int y, int width, int height)
	{
		this();
		setPosition(x, y);
		setSize(width, height);
	}

	public GuiShape(int x, int y, int width, int height, GuiIcon icon)
	{
		this();
		setPosition(x, y);
		setSize(width, height);
		setIcon(icon);
	}

	public void setPosition(int x, int y)
	{
		this.x = x;
		this.y = y;
	}

	public void translate(int x, int y)
	{
		this.x += x;
		this.y += y;
	}

	public int getX()
	{
		return x;
	}

	public int getY()
	{
		return y;
	}

	public void setZIndex(int zIndex)
	{
		this.zIndex = zIndex;
	}

	public int getZIndex()
	{
		return zIndex;
	}

	public void setSize(int width, int height)
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

	public void setIcon(GuiIcon icon)
	{
		this.icon = checkNotNull(icon);
	}

	public GuiIcon getIcon()
	{
		return icon;
	}

	public void setColor(int color)
	{
		vertexes.forEach(v -> v.setColor(color));
	}

	public void setColor(VertexPosition position, int color)
	{
		getVertex(position).setColor(color);
	}

	public void setAlpha(int alpha)
	{
		vertexes.forEach(v -> v.setAlpha(alpha));
	}

	public void setAlpha(VertexPosition position, int alpha)
	{
		getVertex(position).setAlpha(alpha);
	}

	public GuiVertex getVertex(VertexPosition position)
	{
		return vertexes.get(position.ordinal());
	}

	public void render(VertexBuffer buffer)
	{
		renderAt(buffer, 0, 0, 0);
	}

	public void renderAt(VertexBuffer buffer, int offsetX, int offsetY, int offsetZ)
	{
		vertexes.forEach(v -> buffer.addVertexData(v.getVertexData(this, offsetX, offsetY, offsetZ)));
	}

}
