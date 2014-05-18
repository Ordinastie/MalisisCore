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

import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.util.shape.Point;

import java.util.LinkedHashMap;

/**
 * LayoutManager
 *
 * @author PaleoCrafter
 */
public abstract class LayoutManager<C extends Constraints>
{

    /**
     * A table of {@link net.malisis.core.client.gui.component.UIComponent component} -> {@link C constraints} mappings.
     * Used for positioning components and determining the required width and height.
     */
    protected final LinkedHashMap<UIComponent, C> constraints;

    /**
     * Default constructor, creates {@link #constraints constraints} table.
     */
    public LayoutManager()
    {
        constraints = new LinkedHashMap<>();
    }

    /**
     * Get the constraints for the specified component.
     * Will return default constraints if none are set.
     *
     * @param comp the component to get constraints for
     * @return the constraints for the specified component
     */
    public C getConstraints(UIComponent comp)
    {
        C constraints = this.constraints.get(comp);
        if (constraints == null)
        {
            setConstraints(comp, createDefaultConstraints());
            constraints = this.constraints.get(comp);
        }
        return constraints;
    }

    /**
     * Create the default {@link net.malisis.core.client.gui.layout.Constraints constraints} for this
     * <code>LayoutManager</code>.
     *
     * @return the default constraints
     */
    public abstract C createDefaultConstraints();

    /**
     * Set the constraints for the specified component.
     *
     * @param component   the component to set constraints for
     * @param constraints the constraints for the sepcified component
     */
    public void setConstraints(UIComponent component, Constraints constraints)
    {
        if (constraints != null)
            this.constraints.put(component, (C) constraints.clone());
        else
            this.constraints.put(component, null);
    }

    /**
     * Remove the constraints for the given component.
     *
     * @param component the component to remove constraints for
     */
    public void removeConstraints(UIComponent component)
    {
        constraints.remove(component);
    }

    /**
     * Get the position for the specified component based on its constraints.
     *
     * @param component the component to get the position for
     * @return the position for the specified component
     */
    public abstract Point getPositionForComponent(UIContainer container, UIComponent component);

    public abstract int calculateWidth(UIContainer container);

    public abstract int calculateHeight(UIContainer container);
}
