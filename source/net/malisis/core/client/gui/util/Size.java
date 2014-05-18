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

package net.malisis.core.client.gui.util;

/**
 * Size
 *
 * @author PaleoCrafter
 */
public class Size
{

    /**
     * The width of this <code>Size</code> instance
     */
    public int width;

    /**
     * The height of this <code>Size</code> instance
     */
    public int height;

    /**
     * Construct a new <code>Size</code> instance with
     * default width and height (0 and 0)
     */
    public Size()
    {
        this(0, 0);
    }

    /**
     * Construct a new <code>Size</code> instance with
     * given width and height.
     *
     * @param width  the width for this <code>Size</code>
     * @param height the height for this <code>Size</code>
     */
    public Size(int width, int height)
    {
        this.width = width;
        this.height = height;
    }

    /**
     * Construct a new <code>Size</code> instance with
     * the dimensions of the given size.
     * NOTE: Only copies the values.
     *
     * @param size the size to get dimensions from
     */
    public Size(Size size)
    {
        this(size.width, size.height);
    }

    /**
     * @return the width of this <code>Size</code>
     */
    public int getWidth()
    {
        return width;
    }

    /**
     * Set the width of this <code>Size</code>.
     *
     * @param width the new width for this <code>Size</code>
     */
    public void setWidth(int width)
    {
        this.width = width;
    }

    /**
     * @return the height of this <code>Size</code>
     */
    public int getHeight()
    {
        return height;
    }

    /**
     * Set the height of this <code>Size</code>
     *
     * @param height the new height for this <code>Size</code>
     */
    public void setHeight(int height)
    {
        this.height = height;
    }

    @Override
    public String toString()
    {
        return this.getClass().getName() + "[ width=" + width + ", height=" + height + " ]";
    }
}
