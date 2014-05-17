package net.malisis.core.client.gui.component.interaction;

import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.event.MouseClickedEvent;
import net.malisis.core.client.gui.event.ValueChangedEvent;
import net.malisis.core.client.gui.util.shape.Point;
import net.malisis.core.util.RenderHelper;
import net.minecraft.util.ResourceLocation;

/**
 * UICheckBox
 *
 * @author PaleoCrafter
 */
public class UICheckBox extends UIComponent
{

    private static final ResourceLocation DEFAULT_TEXTURE = new ResourceLocation("malisiscore", "textures/gui/widgets/checkbox.png");
    private static final ResourceLocation CHECKED_TEXTURE = new ResourceLocation("malisiscore", "textures/gui/widgets/checkbox_checked.png");
    private static final ResourceLocation HOVERED_TEXTURE = new ResourceLocation("malisiscore", "textures/gui/widgets/checkbox_hovered.png");
    private String label;
    private boolean checked;

    public UICheckBox()
    {
        this("");
    }

    public UICheckBox(String label)
    {
        this.label = label;
        this.setSize(16 + RenderHelper.getStringWidth(label), 14);
    }

    @Override
    public void initComponent()
    {
        super.initComponent();
        this.getContext().register(this);
    }

    @Override
    public boolean isHovered(Point mousePosition)
    {
        return getScreenBounds().resize(14, 14).contains(mousePosition);
    }

    @Subscribe
    public void onMouseClick(MouseClickedEvent event)
    {
        if (this.isHovered(event.getPosition()) && event.getButton().isLeft())
        {
            this.checked = !checked;
            this.getContext().publish(new ValueChangedEvent(this));
        }
    }

    public void setLabel(String label)
    {
        this.label = label;
        this.setSize(16 + RenderHelper.getStringWidth(label), 14);
    }

    @Override
    public void draw(int mouseX, int mouseY)
    {
        RenderHelper.drawRectangle(checked ? CHECKED_TEXTURE : isHovered(new Point(mouseX, mouseY)) ? HOVERED_TEXTURE : DEFAULT_TEXTURE, getScreenX(), getScreenY(), zIndex, 14, 14, 0, 0, 14, 14);
        RenderHelper.drawString(label, getScreenX() + 16, getScreenY() + (getHeight() - mc.fontRenderer.FONT_HEIGHT + 1) / 2, zIndex, 0x404040, false);
    }

    @Override
    public void update(int mouseX, int mouseY)
    {

    }

    @Override
    public String toString()
    {
        return this.getClass().getName() + "[ text=" + label + ", checked=" + this.checked + ", " + this.getPropertyString() + " ]";
    }
}
