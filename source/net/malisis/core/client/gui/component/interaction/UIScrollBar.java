package net.malisis.core.client.gui.component.interaction;

import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.event.MouseClickedEvent;
import net.malisis.core.client.gui.event.MouseScrolledEvent;
import net.malisis.core.client.gui.renderer.Drawable;
import net.malisis.core.client.gui.renderer.DynamicTexture;
import net.malisis.core.client.gui.util.shape.Rectangle;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;

/**
 * UIScrollBar
 *
 * @author PaleoCrafter
 */
public class UIScrollBar extends UIComponent
{

    private Drawable bar;
    private int barY;
    private int barHeight;
    private int step;
    private int initialClickY;

    public UIScrollBar(int width, int height)
    {
        this.setSize(width, height);
        this.barHeight = height / 4;
        this.step = 10;
        this.bar = new DynamicTexture(new ResourceLocation("malisiscore", "textures/gui/widgets/scrollbar.png"), 4, 4, width, barHeight, new Rectangle(0, 0, 1, 1), new Rectangle(1, 0, 2, 1), new Rectangle(1, 1, 2, 2));
    }

    @Override
    public void initComponent()
    {
        super.initComponent();
        getContext().register(this);
    }

    public void clamp()
    {
        this.barY = MathHelper.clamp_int(this.barY, 0, this.getHeight() - this.barHeight);
    }

    @Override
    public void draw(int mouseX, int mouseY)
    {
        bar.draw(getScreenX(), getScreenY() + barY);
    }

    @Override
    public void update(int mouseX, int mouseY)
    {
        if (Mouse.isButtonDown(0))
        {
            if (initialClickY >= 0)
            {
                barY = ((mouseY - getScreenY()) - initialClickY);
            }
        }
        else
        {
            initialClickY = -1;
        }
        clamp();
    }

    public int getBarHeight()
    {
        return barHeight;
    }

    public void setBarHeight(int barHeight)
    {
        bar.setSize(getWidth(), barHeight);
        this.barHeight = barHeight;
    }

    public int getOffset()
    {
        return barY;
    }

    @Subscribe
    public void mouseClick(MouseClickedEvent event)
    {
        if (this.isHovered(event.getPosition()) && event.getButton().isLeft())
        {
            initialClickY = event.getPosition().y - (getScreenY() + barY);
            barY = ((event.getPosition().y - getScreenY()) - initialClickY);
        }
    }

    @Subscribe
    public void mouseScroll(MouseScrolledEvent event)
    {
        barY -= event.getDir() * step;
        clamp();
    }
}
