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

import net.malisis.core.client.gui.util.shape.Point;
import net.malisis.core.client.gui.util.shape.Rectangle;
import net.malisis.core.util.RenderHelper;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;

/**
 * DynamicTexture
 *
 * @author PaleoCrafter
 */
public class DynamicTexture extends Drawable
{

    private final ResourceLocation resource;

    private final int textureWidth, textureHeight;

    private final float scaleU, scaleV;

    private final Rectangle[] corners;

    private final Rectangle[] sides;

    private final Rectangle content;

    public DynamicTexture(ResourceLocation resource, int width, int height, Rectangle topLeftCorner, Rectangle topSide, Rectangle content)
    {
        this(resource, 256, 256, width, height, topLeftCorner, topSide, content);
    }

    public DynamicTexture(ResourceLocation resource, int textureWidth, int textureHeight, int width, int height, Rectangle topLeftCorner, Rectangle topSide, Rectangle content)
    {
        this(resource, textureWidth, textureHeight, width, height, new Rectangle[] {
                topLeftCorner,
                Rectangle.translate(topLeftCorner, topLeftCorner.getWidth() + topSide.getWidth(), 0),
                Rectangle.translate(topLeftCorner, 0, topLeftCorner.getHeight() + topSide.getWidth()),
                Rectangle.translate(topLeftCorner, topLeftCorner.getWidth() + topSide.getWidth(), topLeftCorner.getHeight() + topSide.getWidth()),
        }, new Rectangle[] {
                topSide,
                Rectangle.translate(topSide, 0, content.getHeight() + topSide.getHeight()),
                Rectangle.translate(topSide, -topLeftCorner.getWidth(), topSide.getHeight()).resize(topSide.getHeight(), topSide.getWidth()),
                Rectangle.translate(topSide, topSide.getWidth(), topSide.getHeight()).resize(topSide.getHeight(), topSide.getWidth())
        }, content);
    }

    public DynamicTexture(ResourceLocation resource, int textureWidth, int textureHeight, int width, int height, Rectangle[] corners, Rectangle[] sides, Rectangle content)
    {
        super(width, height);
        if (corners.length != 4)
            throw new IllegalArgumentException("You have to specify 4 corner rectangles!");
        if (sides.length != 4)
            throw new IllegalArgumentException("You have to specify 4 side rectangles.");
        this.resource = resource;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.corners = corners;
        this.sides = sides;
        this.content = content;
        scaleU = 1F / textureWidth;
        scaleV = 1F / textureHeight;
    }

    public DynamicTexture(ResourceLocation resource, int width, int height, Rectangle[] corners, Rectangle[] sides, Rectangle content)
    {
        this(resource, 256, 256, width, height, corners, sides, content);
    }

    @Override
    public void draw(int x, int y)
    {
        RenderHelper.bindTexture(resource);
        Rectangle tlC = corners[Corners.TOP_LEFT.ordinal()];
        Rectangle brC = corners[Corners.BOTTOM_RIGHT.ordinal()];
        for (Corners corner : Corners.values())
        {
            Rectangle rect = corners[corner.ordinal()];
            int tileX = x + corner.getOffX(rect, size.width);
            int tileY = y + corner.getOffY(rect, size.height);
            float uMin = getScaledU(rect.getStart());
            float vMin = getScaledV(rect.getStart());
            float uMax = getScaledU(rect.getEnd());
            float vMax = getScaledV(rect.getEnd());
            RenderHelper.drawQuad(tileX, tileY, 0, rect.getWidth(), rect.getHeight(), uMin, vMin, uMax, vMax);
        }

        for (Sides side : Sides.values())
        {
            Rectangle rect = sides[side.ordinal()];
            Rectangle firstCorner = corners[side.firstCorner.ordinal()];
            Rectangle secondCorner = corners[side.secondCorner.ordinal()];
            int tileX = x + side.getOffX(size.width, rect, firstCorner, secondCorner);
            int tileY = y + side.getOffY(size.height, rect, firstCorner, secondCorner);
            float uMin = getScaledU(rect.getStart());
            float vMin = getScaledV(rect.getStart());
            int tileWidth = side.getWidth(size.width, rect, firstCorner, secondCorner);
            int tileHeight = side.getHeight(size.height, rect, firstCorner, secondCorner);
            float uMax = getScaledU(rect.getEnd());
            float vMax = getScaledV(rect.getEnd());
            if (side.isVertical)
                RenderHelper.drawRectangleYRepeated(tileX, tileY, 0, tileWidth, tileHeight, uMin, vMin, uMax, vMax, rect.getHeight());
            else
                RenderHelper.drawRectangleXRepeated(tileX, tileY, 0, tileWidth, tileHeight, uMin, vMin, uMax, vMax, rect.getWidth());
        }

        RenderHelper.drawRectangleRepeated(x + tlC.getWidth(), y + tlC.getHeight(), 0,
                size.width - (tlC.getWidth() + brC.getWidth()), size.height - (tlC.getHeight() + brC.getHeight()),
                content.getStart().x * scaleU, content.getStart().y * scaleV,
                content.getEnd().x * scaleU, content.getEnd().y * scaleV,
                content.getWidth(), content.getHeight());
    }

    private float getScaledU(Point p)
    {
        return p.x * scaleU;
    }

    private float getScaledV(Point p)
    {
        return p.y * scaleV;
    }

    @Override
    public String toString()
    {
        return this.getClass().getName() + "[ resource=" + resource + ", corners=" + Arrays.asList(corners) + ", sides=" + Arrays.asList(sides) + ", content=" + content + " ]";
    }

    public static enum Corners
    {
        TOP_LEFT()
                {
                    @Override
                    public int getOffX(Rectangle rect, int width)
                    {
                        return 0;
                    }

                    @Override
                    public int getOffY(Rectangle rect, int height)
                    {
                        return 0;
                    }
                },
        TOP_RIGHT()
                {
                    @Override
                    public int getOffX(Rectangle rect, int width)
                    {
                        return width - rect.getWidth();
                    }

                    @Override
                    public int getOffY(Rectangle rect, int height)
                    {
                        return 0;
                    }
                },
        BOTTOM_LEFT()
                {
                    @Override
                    public int getOffX(Rectangle rect, int width)
                    {
                        return 0;
                    }

                    @Override
                    public int getOffY(Rectangle rect, int height)
                    {
                        return height - rect.getHeight();
                    }
                },
        BOTTOM_RIGHT()
                {
                    @Override
                    public int getOffX(Rectangle rect, int width)
                    {
                        return width - rect.getWidth();
                    }

                    @Override
                    public int getOffY(Rectangle rect, int height)
                    {
                        return height - rect.getHeight();
                    }
                };

        public abstract int getOffX(Rectangle rect, int width);

        public abstract int getOffY(Rectangle rect, int height);
    }

    public static enum Sides
    {
        TOP(false, Corners.TOP_LEFT, Corners.TOP_RIGHT)
                {
                    @Override
                    public int getOffX(int width, Rectangle side, Rectangle firstCorner, Rectangle secondCorner)
                    {
                        return firstCorner.getWidth();
                    }

                    @Override
                    public int getOffY(int height, Rectangle side, Rectangle firstCorner, Rectangle secondCorner)
                    {
                        return 0;
                    }
                },
        BOTTOM(false, Corners.BOTTOM_LEFT, Corners.BOTTOM_RIGHT)
                {
                    @Override
                    public int getOffX(int width, Rectangle side, Rectangle firstCorner, Rectangle secondCorner)
                    {
                        return firstCorner.getWidth();
                    }

                    @Override
                    public int getOffY(int height, Rectangle side, Rectangle firstCorner, Rectangle secondCorner)
                    {
                        return height - side.getHeight();
                    }
                },
        LEFT(true, Corners.TOP_LEFT, Corners.BOTTOM_LEFT)
                {
                    @Override
                    public int getOffX(int width, Rectangle side, Rectangle firstCorner, Rectangle secondCorner)
                    {
                        return 0;
                    }

                    @Override
                    public int getOffY(int height, Rectangle side, Rectangle firstCorner, Rectangle secondCorner)
                    {
                        return firstCorner.getHeight();
                    }
                },
        RIGHT(true, Corners.TOP_RIGHT, Corners.BOTTOM_RIGHT)
                {
                    @Override
                    public int getOffX(int width, Rectangle side, Rectangle firstCorner, Rectangle secondCorner)
                    {
                        return width - side.getWidth();
                    }

                    @Override
                    public int getOffY(int height, Rectangle side, Rectangle firstCorner, Rectangle secondCorner)
                    {
                        return firstCorner.getHeight();
                    }
                };

        public final boolean isVertical;

        public final Corners firstCorner;

        public final Corners secondCorner;

        private Sides(boolean isVertical, Corners firstCorner, Corners secondCorner)
        {
            this.isVertical = isVertical;
            this.firstCorner = firstCorner;
            this.secondCorner = secondCorner;
        }

        public int getWidth(int width, Rectangle side, Rectangle firstCorner, Rectangle secondCorner)
        {
            if (isVertical)
            {
                return side.getWidth();
            }
            return width - (firstCorner.getWidth() + secondCorner.getWidth());
        }

        public int getHeight(int height, Rectangle side, Rectangle firstCorner, Rectangle secondCorner)
        {
            if (!isVertical)
            {
                return side.getHeight();
            }
            return height - (firstCorner.getHeight() + secondCorner.getHeight());
        }

        public abstract int getOffX(int width, Rectangle side, Rectangle firstCorner, Rectangle secondCorner);

        public abstract int getOffY(int height, Rectangle side, Rectangle firstCorner, Rectangle secondCorner);
    }

}
