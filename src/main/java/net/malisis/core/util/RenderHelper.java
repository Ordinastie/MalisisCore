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

import static org.lwjgl.opengl.GL20.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;

/**
 * RenderHelper
 *
 * @author PaleoCrafter
 */
public class RenderHelper
{

	public static void drawRectangleRepeated(ResourceLocation texture, int x, int y, int z, int width, int height, float u, float v, float uMax, float vMax, int tileWidth, int tileHeight)
	{
		drawRectangleRepeated(x, y, z, width, height, u, v, uMax, vMax, tileWidth, tileHeight);
	}

	public static void drawRectangleRepeated(int x, int y, int z, int width, int height, float u, float v, float uMax, float vMax, int tileWidth, int tileHeight)
	{
		loadShaders();
		shaders.activate();
		shaders.setUniform1i("tex", 0);
		shaders.setUniform2f("iconOffset", u, v);
		shaders.setUniform2f("iconSize", uMax - u, vMax - v);
		//drawQuad(x, y, z, width, height, 0, 0, (float) getScaledWidth(width) / tileWidth, (float) getScaledHeight(height) / tileHeight);
		shaders.deactivate();
	}

	public static void drawRectangleXRepeated(int x, int y, int z, int width, int height, float u, float v, float uMax, float vMax, int tileWidth)
	{
		loadShaders();
		shaders.activate();
		shaders.setUniform1i("tex", 0);
		shaders.setUniform2f("iconOffset", u, 0);
		shaders.setUniform2f("iconSize", uMax - u, 1);
		//drawQuad(x, y, z, width, height, 0, v, (float) getScaledWidth(width) / tileWidth, vMax);
		shaders.deactivate();
	}

	public static void drawRectangleYRepeated(int x, int y, int z, int width, int height, float u, float v, float uMax, float vMax, int tileHeight)
	{
		loadShaders();
		shaders.activate();
		shaders.setUniform1i("tex", 0);
		shaders.setUniform2f("iconOffset", 0, v);
		shaders.setUniform2f("iconSize", 1, vMax - v);
		//drawQuad(x, y, z, width, height, u, 0, uMax, (float) getScaledHeight(height) / tileHeight);
		shaders.deactivate();
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
		return new ScaledResolution(Minecraft.getMinecraft(), Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
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
