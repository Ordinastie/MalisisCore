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

package net.malisis.core.util;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.util.Color;

import cpw.mods.fml.client.FMLClientHandler;

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

	public static String getLongestString(String... strings)
	{
		String s = "";
		int longest = 0;

		for (String string : strings)
		{
			if (longest < string.length())
			{
				s = string;
				longest = string.length();
			}
		}

		return s;
	}

	public static Minecraft getMC()
	{
		return FMLClientHandler.instance().getClient();
	}

	public static void drawString(String text, int x, int y, int z, int canvasWidth, int canvasHeight, int color, boolean drawShadow)
	{
		drawString(text, x + (canvasWidth - getStringWidth(text)) / 2, y + (canvasHeight - getMC().fontRenderer.FONT_HEIGHT) / 2, z, color,
				drawShadow);
	}

	public static void drawString(String text, int x, int y, int z, int color, boolean drawShadow)
	{
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		GL11.glTranslatef(0, 0, z);
		getMC().fontRenderer.drawString(text, x, y, color, drawShadow);
		GL11.glTranslatef(0, 0, -z);
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
		float multiplier = 1F / 255F;
		glDisable(GL11.GL_TEXTURE_2D);
		glEnable(GL11.GL_BLEND);
		glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		glColor4f(multiplier * color.getRed(), multiplier * color.getGreen(), multiplier * color.getBlue(), multiplier * color.getAlpha());
		glLineWidth(width);
		glBegin(GL11.GL_LINES);
		glVertex3f(startX, startY, zLevel);
		glVertex3f(endX, endY, zLevel);
		glEnd();
		glDisable(GL11.GL_BLEND);
		glColor4f(1F, 1F, 1F, 1F);
		glEnable(GL11.GL_TEXTURE_2D);
	}

	public static void drawRectangle(int color, int x, int y, int z, int width, int height)
	{
		drawRectangle(color, 1F, x, y, z, width, height);
	}

	public static void drawRectangle(int color, float alpha, int x, int y, int z, int width, int height)
	{
		Color rgb = RenderHelper.getRGBFromColor(color);
		rgb.setAlpha((int) (alpha * 255));
		drawRectangle(rgb, x, y, z, width, height);
	}

	public static void drawRectangle(Color color, int x, int y, int z, int width, int height)
	{
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glColor4f(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, color.getAlpha() / 255F);
		drawQuad(x, y, z, width, height, 0, 0, 1, 1);
		GL11.glColor4f(1F, 1F, 1F, 1F);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	public static void drawRectangle(ResourceLocation texture, int x, int y, int z, int width, int height, int u, int v)
	{
		drawRectangle(texture, x, y, z, width, height, u, v, 256, 256);
	}

	public static void drawRectangle(ResourceLocation texture, int x, int y, int z, int width, int height, int u, int v, int textureWidth, int textureHeight)
	{
		bindTexture(texture);
		drawRectangle(x, y, z, width, height, u, v, textureWidth, textureHeight);
	}

	public static void drawRectangle(int x, int y, int z, int width, int height, int u, int v)
	{
		drawRectangle(x, y, z, width, height, u, v, 256, 256);
	}

	public static void drawRectangle(int x, int y, int z, int width, int height, int u, int v, int textureWidth, int textureHeight)
	{
		drawQuad(x, y, z, width, height, (float) u / textureWidth, (float) v / textureHeight, (float) (u + width) / textureWidth,
				(float) (v + height) / textureHeight);
	}

	public static void drawQuad(int x, int y, int z, int width, int height, float u, float v, float uMax, float vMax)
	{
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(x, y + height, z, u, vMax);
		tessellator.addVertexWithUV(x + width, y + height, z, uMax, vMax);
		tessellator.addVertexWithUV(x + width, y, z, uMax, v);
		tessellator.addVertexWithUV(x, y, z, u, v);
		tessellator.draw();
		glDisable(GL_BLEND);
	}

	public static void drawRectangleRepeated(ResourceLocation texture, int x, int y, int z, int width, int height, float u, float v, float uMax, float vMax, int tileWidth, int tileHeight)
	{
		RenderHelper.bindTexture(texture);
		drawRectangleRepeated(x, y, z, width, height, u, v, uMax, vMax, tileWidth, tileHeight);
	}

	public static void drawRectangleRepeated(int x, int y, int z, int width, int height, float u, float v, float uMax, float vMax, int tileWidth, int tileHeight)
	{
		loadShaders();
		shaders.activate();
		shaders.setUniform1i("tex", 0);
		shaders.setUniform2f("iconOffset", u, v);
		shaders.setUniform2f("iconSize", uMax - u, vMax - v);
		drawQuad(x, y, z, width, height, 0, 0, (float) getScaledWidth(width) / tileWidth, (float) getScaledHeight(height) / tileHeight);
		shaders.deactivate();
	}

	public static void drawRectangleXRepeated(int x, int y, int z, int width, int height, float u, float v, float uMax, float vMax, int tileWidth)
	{
		loadShaders();
		shaders.activate();
		shaders.setUniform1i("tex", 0);
		shaders.setUniform2f("iconOffset", u, 0);
		shaders.setUniform2f("iconSize", uMax - u, 1);
		drawQuad(x, y, z, width, height, 0, v, (float) getScaledWidth(width) / tileWidth, vMax);
		shaders.deactivate();
	}

	public static void drawRectangleYRepeated(int x, int y, int z, int width, int height, float u, float v, float uMax, float vMax, int tileHeight)
	{
		loadShaders();
		shaders.activate();
		shaders.setUniform1i("tex", 0);
		shaders.setUniform2f("iconOffset", 0, v);
		shaders.setUniform2f("iconSize", 1, vMax - v);
		drawQuad(x, y, z, width, height, u, 0, uMax, (float) getScaledHeight(height) / tileHeight);
		shaders.deactivate();
	}

	public static void drawIcon(IIcon icon, int x, int y, int z, int width, int height)
	{
		drawQuad(x, y, z, width, height, icon.getMinU(), icon.getMinV(), icon.getMaxU(), icon.getMaxV());
	}

	private static ShaderSystem shaders;

	private static final String REPEAT_SHADER = "#version 120\n"
			+ "uniform sampler2D tex; uniform vec2 iconOffset; uniform vec2 iconSize;\n" + "void main() {\n"
			+ "gl_FragColor = texture2D(tex, iconOffset + fract(gl_TexCoord[0].st) * iconSize) * gl_Color;\n" + "}";

	public static void loadShaders()
	{
		if (shaders == null)
		{
			shaders = new ShaderSystem();
			shaders.addShader(REPEAT_SHADER, GL_FRAGMENT_SHADER);
		}
	}

	public static ScaledResolution getScaledResolution()
	{
		return null;
	}

	public static int getScaledWidth(int width)
	{
		return width / getScaledResolution().getScaleFactor();
	}

	public static int getScaledHeight(int height)
	{
		return height / getScaledResolution().getScaleFactor();
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
