package net.malisis.core.client.gui.component.decoration;

import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.renderer.DynamicTexture;
import net.malisis.core.client.gui.util.shape.Rectangle;
import net.malisis.core.util.RenderHelper;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.Array;

/**
 * UITooltip
 *
 * @author PaleoCrafter
 */
public class UITooltip extends UIComponent
{

    private static final DynamicTexture TEXTURE = new DynamicTexture(new ResourceLocation("malisiscore", "textures/gui/widgets/tooltip.png"), 15, 15, 0, 0, new Rectangle(0, 0, 5, 5), new Rectangle(5, 0, 5, 5), new Rectangle(5, 5, 5, 5));

    private String[] lines;

    public UITooltip()
    {
        this.zIndex = 301;
    }

    @Override
    public void draw(int mouseX, int mouseY)
    {
        this.setSize(mc.fontRenderer.getStringWidth(RenderHelper.getLongestString(lines)) + 8, lines.length * (2 + mc.fontRenderer.FONT_HEIGHT) + 6);
        TEXTURE.setSize(this.getSize());
        TEXTURE.draw(mouseX + 6, mouseY + 6);
        int startX = mouseX + 10;
        int startY = mouseY + 10;
        for (int i = 0; i < lines.length; i++)
        {
            String line = lines[i];
            RenderHelper.drawString(line, startX, startY + i * (2 + mc.fontRenderer.FONT_HEIGHT), zIndex, 0xEEEEEE, false);
        }
    }

    @Override
    public void update(int mouseX, int mouseY)
    {

    }

    public void addLine(String text)
    {
        lines = concatenate(lines, new String[] {text});
    }

    public void setText(String text)
    {
        reset();
        lines = text.split("\\n");
    }

    public <T> T[] concatenate(T[] A, T[] B)
    {
        int aLen = A.length;
        int bLen = B.length;

        @SuppressWarnings("unchecked")
        T[] C = (T[]) Array.newInstance(A.getClass().getComponentType(), aLen + bLen);
        System.arraycopy(A, 0, C, 0, aLen);
        System.arraycopy(B, 0, C, aLen, bLen);

        return C;
    }

    /**
     * Resets all the lines of this tooltip.
     */
    public void reset()
    {
        lines = new String[0];
    }

}
