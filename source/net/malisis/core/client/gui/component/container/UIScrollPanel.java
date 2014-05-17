package net.malisis.core.client.gui.component.container;

import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.interaction.UIScrollBar;
import net.malisis.core.client.gui.util.shape.Point;
import net.malisis.core.util.RenderHelper;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;

/**
 * UIScrollPanel
 *
 * @author PaleoCrafter
 */
public class UIScrollPanel extends UIContainer
{

    private Point actualScreenPosition;
    private Map<UIComponent, Boolean> componentStates;
    private UIScrollBar bar;

    public UIScrollPanel(int width, int height, int barWidth)
    {
        this.bar = new UIScrollBar(barWidth, height);
        this.setSize(width, height);
        actualScreenPosition = new Point(0, 0);
        componentStates = new HashMap<>();
    }

    @Override
    public void initComponent()
    {
        super.initComponent();
        bar.initComponent();
    }

    public UIScrollBar getBar()
    {
        return bar;
    }

    @Override
    public void drawBackground(int mouseX, int mouseY)
    {
        actualScreenPosition = getScreenPosition();
        int contentHeight = getContentHeight();
        if (contentHeight > getHeight())
        {
            float div = ((float) getHeight() / (float) (contentHeight - getHeight()));
            if (div < 1F)
                div += 1F;
            this.setScreenPosition(Point.add(actualScreenPosition, new Point(0, (int) ((float) -(bar.getOffset() * contentHeight / (getHeight() - bar.getBarHeight()))
                    / div))));
        }
        GL11.glPushAttrib(GL11.GL_SCISSOR_BIT);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        int scale = RenderHelper.computeGuiScale();
        GL11.glScissor(actualScreenPosition.x * scale, mc.displayHeight - (actualScreenPosition.y + getHeight()) * scale, getWidth() * scale, getHeight() * scale);

        super.drawBackground(mouseX, mouseY);

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glPopAttrib();
        if (contentHeight > getHeight())
        {
            bar.setScreenPosition(Point.add(actualScreenPosition, new Point(getWidth() - bar.getWidth(), 0)));
            bar.draw(mouseX, mouseY);
        }
    }

    @Override
    public void draw(int mouseX, int mouseY)
    {
        GL11.glPushAttrib(GL11.GL_SCISSOR_BIT);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        int scale = RenderHelper.computeGuiScale();
        GL11.glScissor(actualScreenPosition.x * scale, mc.displayHeight - (actualScreenPosition.y + getHeight()) * scale, getWidth() * scale, getHeight() * scale);

        super.draw(mouseX, mouseY);

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glPopAttrib();
    }

    @Override
    public void drawForeground(int mouseX, int mouseY)
    {
        GL11.glPushAttrib(GL11.GL_SCISSOR_BIT);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        int scale = RenderHelper.computeGuiScale();
        GL11.glScissor(actualScreenPosition.x * scale, mc.displayHeight - (actualScreenPosition.y + getHeight()) * scale, getWidth() * scale, getHeight() * scale);

        for (UIComponent component : components)
        {
            if (component.isVisible())
            {
                GL11.glColor4f(1F, 1F, 1F, 1F);
                component.drawForeground(mouseX, mouseY);
            }
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glPopAttrib();
    }

    @Override
    public void update(int mouseX, int mouseY)
    {
        super.update(mouseX, mouseY);
        bar.update(mouseX, mouseY);
        for (UIComponent component : components)
        {
            if (component.getScreenY() >= actualScreenPosition.y + getHeight() || component.getScreenY() <= actualScreenPosition.y)
            {
                if (component.isEnabled())
                {
                    componentStates.put(component, component.isEnabled());
                    component.setEnabled(false);
                }
            }
            else
            {
                if (componentStates.containsKey(component))
                    component.setEnabled(componentStates.get(component));
            }
        }
    }

}
