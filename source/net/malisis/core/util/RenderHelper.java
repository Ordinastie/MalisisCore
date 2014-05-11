package net.malisis.core.util;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.util.Color;

/**
 * RenderHelper
 *
 * @author PaleoCrafter
 */
public class RenderHelper
{

    public static void bindTexture(String path)
    {
        bindTexture(new ResourceLocation(path));
    }

    public static void bindTexture(ResourceLocation path)
    {
        FMLClientHandler.instance().getClient().getTextureManager().bindTexture(path);
    }

    public static int getColorFromRGB(int r, int g, int b)
    {
        return 0xFF0000 & r << 16 | 0x00FF00 & g << 8 | 0x0000FF & b;
    }

    public static int getColorFromRGB(Color color)
    {
        return 0xFF0000 & color.getRed() << 16 | 0x00FF00 & color.getGreen() << 8 | 0x0000FF & color.getBlue();
    }

    public static int getStringWidth(String text)
    {
        return getMC().fontRenderer.getStringWidth(text);
    }

    public static Minecraft getMC()
    {
        return FMLClientHandler.instance().getClient();
    }

    public static void drawString(String text, int x, int y, int color, boolean drawShadow, int zLevel)
    {
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glTranslatef(0, 0, zLevel);
        getMC().fontRenderer.drawString(text, x, y, color, drawShadow);
        GL11.glTranslatef(0, 0, -zLevel);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    public static void drawSplitString(String text, int x, int y, int color, boolean drawShadow)
    {
        String[] splits = text.split("<br>");
        for (int i = 0; i < splits.length; i++)
        {
            getMC().fontRenderer.drawString(splits[i], x, y + i * 10, color, drawShadow);
        }
    }

    public static void drawLine(int color, int startX, int startY, int endX, int endY, float width, int zLevel)
    {
        drawLine(color, 1F, startX, startY, endX, endY, width, zLevel);
    }

    public static void drawLine(int color, float alpha, int startX, int startY, int endX, int endY, float width, int zLevel)
    {
        Color rgb = RenderHelper.getRGBFromColor(color);
        rgb.setAlpha((int) (alpha * 255));
        drawLine(rgb, startX, startY, endX, endY, width, zLevel);
    }

    public static Color getRGBFromColor(int color)
    {
        return new Color((0xFF0000 & color) >> 16, (0x00FF00 & color) >> 8, 0x0000FF & color);
    }

    public static void drawLine(Color color, int startX, int startY, int endX, int endY, float width, int zLevel)
    {
        float colorMod = 1F / 255F;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(colorMod * color.getRed(), colorMod * color.getGreen(), colorMod * color.getBlue(), colorMod * color.getAlpha());
        GL11.glLineWidth(width);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3f(startX, startY, zLevel);
        GL11.glVertex3f(endX, endY, zLevel);
        GL11.glEnd();
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4f(1F, 1F, 1F, 1F);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    public static void drawRectangle(int color, int x, int y, int width, int height, int zLevel)
    {
        drawRectangle(color, 1F, x, y, width, height, zLevel);
    }

    public static void drawRectangle(int color, float alpha, int x, int y, int width, int height, int zLevel)
    {
        Color rgb = RenderHelper.getRGBFromColor(color);
        rgb.setAlpha((int) (alpha * 255));
        drawRectangle(rgb, x, y, width, height, zLevel);
    }

    public static void drawRectangle(Color color, int x, int y, int width, int height, int zLevel)
    {
        float colorMod = 1F / 255F;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(colorMod * color.getRed(), colorMod * color.getGreen(), colorMod * color.getBlue(), colorMod * color.getAlpha());
        drawRectangle(x, y, 0, 0, width, height, zLevel);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4f(1F, 1F, 1F, 1F);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    public static void drawRectangle(ResourceLocation texture, int x, int y, float u, float v, int width, int height, int zLevel)
    {
        RenderHelper.bindTexture(texture);
        float scaleU = 0.00390625F;
        float scaleV = 0.00390625F;
        if (u % 1 != 0) scaleU = 1;
        if (v % 1 != 0) scaleV = 1;
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x, y + height, zLevel, u * scaleU, (v + height) * scaleV);
        tessellator.addVertexWithUV(x + width, y + height, zLevel, (u + width) * scaleU, (v + height) * scaleV);
        tessellator.addVertexWithUV(x + width, y, zLevel, (u + width) * scaleU, v * scaleV);
        tessellator.addVertexWithUV(x, y, zLevel, u * scaleU, v * scaleV);
        tessellator.draw();
    }

    public static void drawRectangleStretched(ResourceLocation texture, int x, int y, float u, float v, int width, int height, float uOff, float vOff, int zLevel)
    {
        drawRectangleStretched(texture, x, y, u, v, width, height, u + uOff, v + vOff, true, zLevel);
    }

    public static void drawRectangleStretched(ResourceLocation texture, int x, int y, float u, float v, int width, int height, float uMax, float vMax, boolean max, int zLevel)
    {
        if (max)
        {
            bindTexture(texture);
            drawRectangleStretched(x, y, u, v, width, height, uMax, vMax, zLevel);
        }
        else
        {
            drawRectangleStretched(texture, x, y, u, v, width, height, uMax, vMax, zLevel);
        }
    }

    public static void drawRectangleStretched(int x, int y, float u, float v, int width, int height, float uMax, float vMax, int zLevel)
    {
        float scaleU = 0.00390625F;
        float scaleV = 0.00390625F;
        if (u % 1 != 0 || uMax % 1 != 0) scaleU = 1;
        if (v % 1 != 0 || vMax % 1 != 0) scaleV = 1;
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x, y + height, zLevel, u * scaleU, vMax * scaleV);
        tessellator.addVertexWithUV(x + width, y + height, zLevel, uMax * scaleU, vMax * scaleV);
        tessellator.addVertexWithUV(x + width, y, zLevel, uMax * scaleU, v * scaleV);
        tessellator.addVertexWithUV(x, y, zLevel, u * scaleU, v * scaleV);
        tessellator.draw();
    }

    public static void drawRectangleRepeated(ResourceLocation texture, int x, int y, float u, float v, float uvWidth, float uvHeight, int width, int height, int zLevel)
    {
        drawRectangleRepeated(texture, x, y, u, v, uvWidth, uvHeight, width, height, (int) uvWidth, (int) uvHeight, zLevel);
    }

    public static void drawRectangleRepeated(ResourceLocation texture, int x, int y, float u, float v, float uMax, float vMax, int width, int height, int tileWidth, int tileHeight, int zLevel)
    {
        float uvHeight = v - vMax;
        int numX = (int) Math.ceil((float) width / tileWidth);
        int numY = (int) Math.ceil((float) height / tileHeight);

        for (int y2 = 0; y2 < numY; ++y2)
            for (int x2 = 0; x2 < numX; ++x2)
            {
                int w = tileWidth;
                int h = tileHeight;

                float tileMaxU = uMax;
                float tileMaxV = vMax;

                float tileV = v;

                int tileX = w * x2;
                int tileY = h * y2 + h;

                if (tileWidth > width)
                {
                    w = width;
                    tileMaxU -= 0.00390625F * (float) w / tileWidth;
                    tileX = w * x2;
                }
                else if (x2 == numX - 1)
                {
                    if (tileWidth > width - x2 * tileWidth)
                    {
                        w = width - x2 * tileWidth;
                        tileMaxU -= 0.00390625F * (float) w / tileWidth;
                        tileX = tileWidth * x2;
                    }
                }

                if (tileHeight > height)
                {
                    h = height;
                    tileMaxV -= 0.00390625F * (float) h / tileHeight;
                    tileY = h * y2 + h;
                }
                else if (y2 == numY - 1)
                {
                    if (tileHeight > height - (y2 - 1) * tileHeight)
                    {
                        h = height - (y2 - 1) * tileHeight;
                        tileV += uvHeight - 0.00390625F * (float) h / tileHeight;
                        tileY = tileHeight * y2 + h;
                    }
                }

                drawRectangleStretched(texture, x + tileX, y + height - tileY, u, tileV, w, h, tileMaxU, tileMaxV, true, zLevel);
            }
    }

    public static void drawRectangleXRepeated(ResourceLocation texture, int x, int y, float u, float v, float uvWidth, float uvHeight, int width, int height, int zLevel)
    {
        RenderHelper.bindTexture(texture);
        float f = 0.00390625F;
        float f1 = 0.00390625F;
        if (u % 1 != 0) f = 1;
        if (v % 1 != 0) f1 = 1;
        Tessellator tessellator = Tessellator.instance;

        boolean flipX = width < 0;
        if (flipX) width *= -1;

        int numX = (int) Math.ceil((float) width / uvWidth);

        for (int x2 = 0; x2 < numX; ++x2)
        {
            float xOffset = x2 * uvWidth;
            if (flipX) xOffset = width - (x2 + 1) * uvWidth;

            tessellator.startDrawingQuads();
            tessellator.addVertexWithUV(x + xOffset, y + height, zLevel, u * f, (v + uvHeight) * f1);
            tessellator.addVertexWithUV(x + uvWidth + xOffset, y + height, zLevel, (u + uvWidth) * f, (v + uvHeight) * f1);
            tessellator.addVertexWithUV(x + uvWidth + xOffset, y, zLevel, (u + uvWidth) * f, v * f1);
            tessellator.addVertexWithUV(x + xOffset, y, zLevel, u * f, v * f1);
            tessellator.draw();
        }
    }

    public static void drawRectangleYRepeated(ResourceLocation texture, int x, int y, float u, float v, float uvWidth, float uvHeight, int width, int height, int zLevel)
    {
        RenderHelper.bindTexture(texture);
        float scaleU = 0.00390625F;
        float scaleV = 0.00390625F;
        if (u % 1 != 0) scaleU = 1;
        if (v % 1 != 0) scaleV = 1;
        Tessellator tessellator = Tessellator.instance;

        boolean flipY = height < 0;
        if (flipY) height *= -1;

        int numY = (int) Math.ceil((float) height / uvHeight);

        for (int y2 = 0; y2 < numY; ++y2)
        {
            float yOffset = y2 * uvHeight;
            if (flipY) yOffset = height - (y2 + 1) * uvHeight;

            tessellator.startDrawingQuads();
            tessellator.addVertexWithUV(x, y + uvHeight + yOffset, zLevel, u * scaleU, (v + uvHeight) * scaleV);
            tessellator.addVertexWithUV(x + width, y + uvHeight + yOffset, zLevel, (u + uvWidth) * scaleU, (v + uvHeight) * scaleV);
            tessellator.addVertexWithUV(x + width, y + yOffset, zLevel, (u + uvWidth) * scaleU, v * scaleV);
            tessellator.addVertexWithUV(x, y + yOffset, zLevel, u * scaleU, v * scaleV);
            tessellator.draw();
        }
    }

    public static int computeGuiScale()
    {
        Minecraft mc = Minecraft.getMinecraft();
        int scaleFactor = 1;

        int k = mc.gameSettings.guiScale;

        if (k == 0)
        {
            k = 1000;
        }

        while (scaleFactor < k && mc.displayWidth / (scaleFactor + 1) >= 320 && mc.displayHeight / (scaleFactor + 1) >= 240)
        {
            ++scaleFactor;
        }
        return scaleFactor;
    }

}
