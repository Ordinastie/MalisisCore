package net.malisis.core.client.gui.component.interaction;

import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.event.ButtonPressedEvent;
import net.malisis.core.client.gui.event.MouseClickedEvent;
import net.malisis.core.client.gui.renderer.DynamicTexture;
import net.malisis.core.client.gui.util.shape.Point;
import net.malisis.core.client.gui.util.shape.Rectangle;
import net.malisis.core.util.RenderHelper;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;

/**
 * UIButton
 *
 * @author PaleoCrafter
 */
public class UIButton extends UIComponent
{

    private static final DynamicTexture TEXTURE_NORMAL = new DynamicTexture(new ResourceLocation("malisiscore", "textures/gui/widgets/button.png"), 15, 15, 0, 0, new Rectangle(0, 0, 5, 5), new Rectangle(5, 0, 5, 5), new Rectangle(5, 5, 5, 5));
    private static final DynamicTexture TEXTURE_HOVERED = new DynamicTexture(new ResourceLocation("malisiscore", "textures/gui/widgets/button_hovered.png"), 15, 15, 0, 0, new Rectangle(0, 0, 5, 5), new Rectangle(5, 0, 5, 5), new Rectangle(5, 5, 5, 5));
    private static final DynamicTexture TEXTURE_DISABLED = new DynamicTexture(new ResourceLocation("malisiscore", "textures/gui/widgets/button_disabled.png"), 15, 15, 0, 0, new Rectangle(0, 0, 5, 5), new Rectangle(5, 0, 5, 5), new Rectangle(5, 5, 5, 5));

    private String text;

    public UIButton()
    {
        this("", 0, 0);
    }

    public UIButton(String text, int width, int height)
    {
        this.text = text;
        this.setSize(width, height);
    }

    @Override
    public void initComponent()
    {
        super.initComponent();
        this.getContext().register(this);
    }

    @Subscribe
    public void onMouseClick(MouseClickedEvent event)
    {
        if (this.isEnabled() && this.isHovered(event.getPosition()) && event.getButton().isLeft())
        {
            this.mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
            this.getContext().publish(new ButtonPressedEvent(this));
        }
    }

    @Override
    public void draw(int mouseX, int mouseY)
    {
        int textColor = 0xe0e0e0;
        DynamicTexture texture = TEXTURE_NORMAL;
        if (this.isEnabled())
        {
            if (this.isHovered(new Point(mouseX, mouseY)))
            {
                textColor = 0xffffa0;
                texture = TEXTURE_HOVERED;
            }
        }
        else
        {
            textColor = 0xa0a0a0;
            texture = TEXTURE_DISABLED;
        }

        texture.setSize(this.getSize());
        texture.draw(getScreenX(), getScreenY());

        RenderHelper.drawString(text, getScreenX(), getScreenY(), zIndex, getWidth(), getHeight(), textColor, true);
    }

    @Override
    public void update(int mouseX, int mouseY)
    {

    }

    @Override
    public String toString()
    {
        return this.getClass().getName() + "[ text=" + text + ", " + this.getPropertyString() + " ]";
    }
}
