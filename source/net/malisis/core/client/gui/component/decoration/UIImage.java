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

package net.malisis.core.client.gui.component.decoration;

import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.util.Size;
import net.malisis.core.util.RenderHelper;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;

/**
 * UIImage
 *
 * @author PaleoCrafter
 */
public class UIImage extends UIComponent
{

    private float scale;
    private Size baseSize;
    private ResourceLocation texture;

    public UIImage()
    {
        this(1F, new ResourceLocation("minecraft", "null"));
    }

    public UIImage(float scale, ResourceLocation texture)
    {
        this.scale = scale;
        this.texture = texture;
        try
        {
            BufferedImage temp = ImageIO.read(mc.getResourceManager().getResource(texture).getInputStream());
            this.baseSize = new Size(temp.getWidth(), temp.getHeight());
            if (texture.getResourcePath().toLowerCase().contains("textures/blocks") && texture.getResourcePath().toLowerCase().contains("textures/items"))
                this.setSize((int) (16 * scale), (int) (16 * scale));
            else
                this.setSize((int) (temp.getWidth() * scale), (int) (temp.getHeight() * scale));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void setScale(float scale)
    {
        this.scale = scale;
        this.setSize((int) (baseSize.getWidth() * scale), (int) (baseSize.getHeight() * scale));
    }

    public void setTexture(ResourceLocation texture)
    {
        this.texture = texture;
        try
        {
            BufferedImage temp = ImageIO.read(mc.getResourceManager().getResource(texture).getInputStream());
            this.baseSize = new Size(temp.getWidth(), temp.getHeight());
            if (texture.getResourcePath().toLowerCase().contains("textures/blocks") && texture.getResourcePath().toLowerCase().contains("textures/items"))
                this.setSize((int) (16 * scale), (int) (16 * scale));
            else
                this.setSize((int) (temp.getWidth() * scale), (int) (temp.getHeight() * scale));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void draw(int mouseX, int mouseY)
    {
        if (texture.getResourcePath().toLowerCase().contains("textures/blocks") && texture.getResourcePath().toLowerCase().contains("textures/items"))
        {
            RenderHelper.drawRectangle(texture, getScreenX(), getScreenY(), zIndex, this.getWidth(), this.getHeight(), 0, 0);
        }
        else
        {
            RenderHelper.drawRectangle(texture, getScreenX(), getScreenY(), zIndex, this.getWidth(), this.getHeight(), 0, 0, this.getWidth(), this.getHeight());
        }
    }

    @Override
    public void update(int mouseX, int mouseY)
    {

    }

    @Override
    public String toString()
    {
        return this.getClass().getName() + "[ scale=" + scale + ", texture=" + this.texture + ", " + this.getPropertyString() + " ]";
    }
}
