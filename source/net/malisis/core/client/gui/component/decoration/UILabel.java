package net.malisis.core.client.gui.component.decoration;

import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.util.RenderHelper;

import java.util.Arrays;

/**
 * UILabel
 *
 * @author PaleoCrafter
 */
public class UILabel extends UIComponent
{

    private String[] lines;
    private int color;
    private boolean drawShadow;

    public UILabel()
    {
        this("");
    }

    public UILabel(String text)
    {
        this(text, 0x404040);
    }

    public UILabel(String text, int color)
    {
        this.color = color;
        this.lines = text.split("\\n");
        this.setSize(RenderHelper.getStringWidth(RenderHelper.getLongestString(lines)), lines.length * mc.fontRenderer.FONT_HEIGHT + (lines.length - 1));
    }

    public void setText(String text)
    {
        lines = text.split("\\n");
        this.setSize(RenderHelper.getStringWidth(RenderHelper.getLongestString(lines)), lines.length * (mc.fontRenderer.FONT_HEIGHT + 1));
    }

    public void setColor(int color)
    {
        this.color = color;
    }

    public void setDrawShadow(boolean drawShadow)
    {
        this.drawShadow = drawShadow;
    }

    @Override
    public void draw(int mouseX, int mouseY)
    {
        for (int i = 0; i < lines.length; i++)
        {
            String line = lines[i];
            RenderHelper.drawString(line, getScreenX(), getScreenY() + i * (mc.fontRenderer.FONT_HEIGHT + 1), zIndex, color, drawShadow);
        }
    }

    @Override
    public void update(int mouseX, int mouseY)
    {

    }

    @Override
    public String toString()
    {
        return this.getClass().getName() + "[ text=" + Arrays.toString(lines) + ", color=0x" + Integer.toHexString(this.color) + ", " + this.getPropertyString() + " ]";
    }
}
