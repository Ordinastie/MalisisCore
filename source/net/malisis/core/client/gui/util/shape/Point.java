/*******************************************************************************
 The MIT License (MIT)

 Copyright (c) 2014 MineFormers

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 ******************************************************************************/

package net.malisis.core.client.gui.util.shape;

import java.io.Serializable;

/**
 * Point
 *
 * @author PaleoCrafter
 */
public class Point implements Cloneable, Serializable
{

    /**
     * The X coordinate of this <code>Point</code>.
     * If no coordinate set, will default to 0.
     *
     * @serial
     * @see #set(int, int)
     */
    public int x;

    /**
     * The Y coordinate of this <code>Point</code>.
     * If no coordinate set, will default to 0.
     *
     * @see #set(int, int)
     */
    public int y;

    /**
     * Construct a new <code>Point</code> at the origin
     * of the coordinate space (0, 0).
     */
    public Point()
    {
        this(0, 0);
    }

    /**
     * Construct a new <code>Point</code> with given coordinates.
     *
     * @param x the X coordinate of this <code>Point</code>
     * @param y the Y coordinate of this <code>Point</code>
     */
    public Point(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    /**
     * Construct a new <code>Point</code> with the coordinates
     * of the given point.
     *
     * @param p the <code>Point</code> to get X and Y from
     */
    public Point(Point p)
    {
        this(p.x, p.y);
    }

    /**
     * @return the X coordinate of this <code>Point</code>
     */
    public int getX()
    {
        return x;
    }

    /**
     * @return the Y coordinate of this <code>Point</code>
     */
    public int getY()
    {
        return y;
    }

    /**
     * Move the <code>Point</code> to the specified coordinates.
     *
     * @param x the new X cooordinate for this <code>Point</code>
     * @param y the new X cooordinate for this <code>Point</code>
     */
    public void set(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    /**
     * Move the <code>Point</code> the specified distance along
     * both axes.
     * Both values can be negative.
     *
     * @param dx the X distance to translate
     * @param dy the Y distance to translate
     */
    public void translate(int dx, int dy)
    {
        this.x += dx;
        this.y += dy;
    }

    /**
     * @return a clone of this instance
     */
    @Override
    protected final Object clone()
    {
        try
        {
            return super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new InternalError();
        }
    }

    /**
     * @param obj the obj to check equality against
     * @return true, if the supplied object is equal to this <code>Point</code>
     */
    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof Point && ((Point) obj).x == x && ((Point) obj).y == y;
    }

    /**
     * @return the {@link java.lang.String String} representation of this <code>Point</code>
     */
    @Override
    public String toString()
    {
        return getClass().getName() + "[ x=" + x + ", y=" + y + " ]";
    }

    /**
     * Add the two points together.
     *
     * @param point the point to add something to
     * @param toAdd the point to add
     * @return a new instance with added X and Y coordinates
     */
    public static Point add(Point point, Point toAdd)
    {
        return new Point(point.x + toAdd.x, point.y + toAdd.y);
    }

}
