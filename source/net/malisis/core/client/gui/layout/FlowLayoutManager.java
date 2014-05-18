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

package net.malisis.core.client.gui.layout;

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.util.shape.Point;

import java.util.Iterator;

/**
 * FlowLayoutManager
 *
 * @author PaleoCrafter
 */
public class FlowLayoutManager extends LayoutManager<FlowLayoutManager.FlowConstraints>
{

    private int hGap, vGap;

    public FlowLayoutManager()
    {
        this(5, 5);
    }

    public FlowLayoutManager(int hGap, int vGap)
    {
        this.hGap = hGap;
        this.vGap = vGap;
    }

    @Override
    public FlowConstraints createDefaultConstraints()
    {
        return null;
    }

    @Override
    public Point getPositionForComponent(UIContainer container, UIComponent component)
    {
        if (this.getConstraints(component) == null)
        {
            Point pos = new Point(0, 0);
            PeekingIterator<UIComponent> components = Iterators.peekingIterator(container.components());
            int highest = 0;
            while (components.hasNext())
            {
                UIComponent current = components.next();
                if (current.getHeight() > highest)
                {
                    highest = current.getHeight();
                }
                if (current == component)
                    break;
                if (components.peek() == component)
                {
                    Point curPos = getConstraints(current).pos;
                    pos.set(curPos.x, curPos.y);
                    pos.translate(current.getWidth() + hGap, 0);
                    if (pos.x + component.getWidth() + container.getPadding().x * 2 > container.getWidth())
                    {
                        pos.set(0, pos.y + highest + vGap);
                        highest = 0;
                    }
                }
            }
            setConstraints(component, new FlowConstraints(pos));
            return pos;
        }
        else
        {
            return getConstraints(component).pos;
        }
    }

    @Override
    public int calculateWidth(UIContainer container)
    {
        Iterator<UIComponent> components = container.components();
        int width = 0;
        while (components.hasNext())
        {
            UIComponent current = components.next();
            Point pos = getPositionForComponent(container, current);
            if (pos.x + current.getWidth() > width)
            {
                width = pos.x + current.getWidth();
            }
        }
        return width;
    }

    @Override
    public int calculateHeight(UIContainer container)
    {
        Iterator<UIComponent> components = container.components();
        int height = 0;
        while (components.hasNext())
        {
            UIComponent current = components.next();
            Point pos = getPositionForComponent(container, current);
            if (pos.y + current.getHeight() > height)
            {
                height = pos.y + current.getHeight();
            }
        }
        return height;
    }

    public void setHorizontalGap(int hGap)
    {
        this.hGap = hGap;
    }

    public void setVerticalGap(int vGap)
    {
        this.vGap = vGap;
    }

    public static class FlowConstraints extends Constraints
    {

        public Point pos;

        public FlowConstraints(Point pos)
        {
            this.pos = pos;
        }

    }
}
