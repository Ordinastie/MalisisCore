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

package net.malisis.core.client.gui.renderer;

import net.malisis.core.client.gui.util.Size;

/**
 * Drawable
 *
 * @author PaleoCrafter
 */
public abstract class Drawable
{

    protected Size size;

    public Drawable()
    {
        this(0, 0);
    }

    public Drawable(int width, int height)
    {
        this.size = new Size(width, height);
    }

    public abstract void draw(int x, int y);

    /**
     * @return the size of this <code>DynamicTexture</code>
     * @see #size
     */
    public Size getSize()
    {
        return size;
    }

    /**
     * Set the size to given width and height.
     *
     * @param width  the new width for this <code>Drawable</code>
     * @param height the new height for this <code>Drawable</code>
     */
    public void setSize(int width, int height) {
        size.setWidth(width);
        size.setHeight(height);
    }

    /**
     * Set the {@link net.malisis.core.client.gui.util.Size size} of this <code>Drawable</code>.
     *
     * @param size the size for this <code>Drawable</code>
     */
    public void setSize(Size size) {
        this.size = size;
    }

}
