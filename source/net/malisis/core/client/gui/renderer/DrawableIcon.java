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

package net.malisis.core.client.gui.renderer;

import net.malisis.core.util.RenderHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;

/**
 * DrawableIcon
 *
 * @author PaleoCrafter
 */
public class DrawableIcon extends Drawable
{

    public static final int BLOCK_SHEET = 0;
    public static final int ITEM_SHEET = 1;

    private IIcon icon;
    private int sheet;

    public DrawableIcon(IIcon icon, int sheet)
    {
        super(16, 16);
        this.icon = icon;
        this.sheet = sheet;
    }

    @Override
    public void draw(int x, int y)
    {
        if(sheet == BLOCK_SHEET)
            RenderHelper.bindTexture(TextureMap.locationBlocksTexture);
        else
            RenderHelper.bindTexture(TextureMap.locationItemsTexture);
        RenderHelper.drawIcon(icon, x, y, 0, size.width, size.height);
    }

}
