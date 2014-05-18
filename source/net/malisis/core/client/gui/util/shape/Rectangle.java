/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 PaleoCrafter, Ordinastie
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

package net.malisis.core.client.gui.util.shape;

/**
 * Rectangle
 *
 * @author PaleoCrafter
 */
public class Rectangle
{

    /**
     * The start {@link net.malisis.core.client.gui.util.shape.Point Point} of this
     * <code>Rectangle</code>.
     */
    private final Point start;

    /**
     * The end {@link net.malisis.core.client.gui.util.shape.Point Point} of this
     * <code>Rectangle</code>.
     */
    private final Point end;

    /**
     * Constructs a new <code>Rectangle</code> at (0, 0) with given
     * width and height.
     *
     * @param width  the width for this <code>Rectangle</code>
     * @param height the height for this <code>Rectangle</code>
     */
    public Rectangle(int width, int height)
    {
        this(new Point(), width, height);
    }

    /**
     * Constructs a new <code>Rectangle</code> with the given start point
     * and size.
     *
     * @param start  the start {@link net.malisis.core.client.gui.util.shape.Point Point}
     * @param width  the width for this <code>Rectangle</code>
     * @param height the height for this <code>Rectangle</code>
     */
    public Rectangle(Point start, int width, int height)
    {
        this(start, new Point(start.x + width, start.y + height));
    }

    /**
     * Constructs a new <code>Rectangle</code> with the given points.
     * The line between both points defines the diagonal of the <code>Rectangle</code>.
     *
     * @param start the start {@link net.malisis.core.client.gui.util.shape.Point Point}
     * @param end   the end {@link net.malisis.core.client.gui.util.shape.Point Point}
     */
    public Rectangle(Point start, Point end)
    {
        this.start = start;
        this.end = end;
    }

    /**
     * Constructs a new <code>Rectangle</code> with the given coordinates as
     * start point and the provided size.
     *
     * @param x      the X coordinate of this <code>Rectangle</code>
     * @param y      the Y coordinate of this <code>Rectangle</code>
     * @param width  the width for this <code>Rectangle</code>
     * @param height the height for this <code>Rectangle</code>
     */
    public Rectangle(int x, int y, int width, int height)
    {
        this(new Point(x, y), width, height);
    }

    /**
     * Add the given size to the specified <code>Rectangle</code> and
     * return a new, manipulated instance.
     *
     * @param rect   the base <code>Rectangle</code> to resize
     * @param width  the width to add to the <code>Rectangle</code>
     * @param height the height to add to the <code>Rectangle</code>
     * @return a new, manipulated <code>Rectangle</code> instance
     */
    public static Rectangle add(Rectangle rect, int width, int height)
    {
        return new Rectangle(rect.start.x, rect.start.y, rect.getWidth() + width, rect.getHeight() + height);
    }

    /**
     * NOTE: Will always return absolute values, even if the 'actual'
     * width is negative.
     *
     * @return the width of this <code>Rectangle</code>
     */
    public int getWidth()
    {
        return Math.abs(start.x - end.x);
    }

    /**
     * Set the width of this <code>Rectangle</code>.
     * Basically only manipulates the end point's X coordinate.
     *
     * @param width the new width for this <code>Rectangle</code>
     */
    public void setWidth(int width)
    {
        end.set(start.x + width, end.y);
    }

    /**
     * NOTE: Will always return absolute values, even if the 'actual'
     * height is negative.
     *
     * @return the height of this <code>Rectangle</code>
     */
    public int getHeight()
    {
        return Math.abs(start.y - end.y);
    }

    /**
     * Set the height of this <code>Rectangle</code>.
     * Basically only manipulates the end point's Y coordinate.
     *
     * @param height the new height for this <code>Rectangle</code>
     */
    public void setHeight(int height)
    {
        end.set(end.x, start.y + height);
    }

    /**
     * Create a moved instance of the given <code>Rectangle</code>.
     *
     * @param rect the base <code>Rectangle</code> to translate
     * @param dx   the distance to translate along the X axis
     * @param dy   the distance to translate along the Y axis
     * @return a new <code>Rectangle</code> instance with modified coordinates
     */
    public static Rectangle translate(Rectangle rect, int dx, int dy)
    {
        return new Rectangle(rect.start.x + dx, rect.start.y + dy, rect.getWidth(), rect.getHeight());
    }

    /**
     * Resize this <code>Rectangle</code>, can be used for chaining.
     *
     * @param newWidth  the new width for this <code>Rectangle</code>
     * @param newHeight the new height for this <code>Rectangle</code>
     * @return this <code>Rectangle</code>
     */
    public Rectangle resize(int newWidth, int newHeight)
    {
        this.setSize(newWidth, newHeight);
        return this;
    }

    /**
     * Set the size of this <code>Rectangle</code> based on the start point.
     * Basically only manipulates the end point.
     *
     * @param width  the new width for this <code>Rectangle</code>
     * @param height the new height for this <code>Rectangle</code>
     */
    public void setSize(int width, int height)
    {
        this.end.set(start.x + width, start.y + height);
    }

    /**
     * Translate this <code>Rectangle</code>, can be used for chaining.
     *
     * @param dx the distance to translate along the X axis
     * @param dy the distance to translate along the Y axis
     * @return this <code>Rectangle</code>
     */
    public Rectangle translate(int dx, int dy)
    {
        this.start.translate(dx, dy);
        this.end.translate(dx, dy);
        return this;
    }

    /**
     * Determines whether the specified point is within this rectangle's bounds.
     *
     * @param point the point to check against
     * @return true, if the point is inside the rectangle's bounds
     */
    public boolean contains(Point point)
    {
        return start.x < point.x && start.y < point.y && end.x > point.x && end.y > point.y;
    }

    /**
     * @return the start {@link net.malisis.core.client.gui.util.shape.Point Point} of this
     * <code>Rectangle</code>
     */
    public Point getStart()
    {
        return start;
    }

    /**
     * Set the start point of this <code>Rectangle</code>.
     * NOTE: Will only use the coordinates of the given point.
     *
     * @param start the {@link net.malisis.core.client.gui.util.shape.Point Point} to
     *              set coordinates from
     */
    public void setStart(Point start)
    {
        this.start.set(start.x, start.y);
    }

    /**
     * Set the start point to the specified coordinates.
     *
     * @param x the new X coordinate of this <code>Rectangle</code>
     * @param y the new Y coordinate of this <code>Rectangle</code>
     */
    public void setStart(int x, int y)
    {
        this.start.set(x, y);
    }

    /**
     * @return the end {@link net.malisis.core.client.gui.util.shape.Point Point} of this
     * <code>Rectangle</code>
     */
    public Point getEnd()
    {
        return end;
    }

    /**
     * Set the end point of this <code>Rectangle</code>.
     * NOTE: Will only use the coordinates of the given point.
     *
     * @param end the {@link net.malisis.core.client.gui.util.shape.Point Point} to
     *            set coordinates from
     */
    public void setEnd(Point end)
    {
        this.end.set(end.x, end.y);
    }

    /**
     * Set the end point to the specified coordinates.
     *
     * @param x the new X coordinate of this <code>Rectangle</code>'s end
     * @param y the new Y coordinate of this <code>Rectangle</code>'s end
     */
    public void setEnd(int x, int y)
    {
        this.end.set(x, y);
    }

    @Override
    public String toString()
    {
        return this.getClass().getName() + "[ start=" + start + ", end=" + end + ", width=" + getWidth() + ", height=" + getHeight() + " ]";
    }
}
