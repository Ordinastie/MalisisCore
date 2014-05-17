package net.malisis.core.client.gui.layout;

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
        hGap = vGap = 5;
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
            Iterator<UIComponent> components = container.components();
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
                pos.translate(current.getWidth() + hGap, 0);
                if (pos.x + component.getWidth() + container.getPadding().x > container.getWidth())
                {
                    pos.set(0, pos.y + highest + vGap);
                    highest = 0;
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
