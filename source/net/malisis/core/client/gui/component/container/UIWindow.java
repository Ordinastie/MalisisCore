package net.malisis.core.client.gui.component.container;

import net.malisis.core.client.gui.renderer.DynamicTexture;
import net.malisis.core.client.gui.util.shape.Rectangle;
import net.minecraft.util.ResourceLocation;

/**
 * UIWindow
 *
 * @author PaleoCrafter
 */
public class UIWindow extends UIContainer
{

    private static final ResourceLocation TEXTURE = new ResourceLocation("malisiscore", "textures/gui/widgets/window.png");

    public UIWindow(int width, int height)
    {
        super(width, height);
        this.setBackground(new DynamicTexture(TEXTURE, 15, 15, this.getWidth(), this.getHeight(), new Rectangle(0, 0, 5, 5), new Rectangle(5, 0, 5, 5), new Rectangle(5, 5, 5, 5)));
    }

}
