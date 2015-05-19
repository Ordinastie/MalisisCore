/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Ordinastie
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

package net.malisis.core.renderer.font;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import net.malisis.core.MalisisCore;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.renderer.MalisisRenderer;
import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.element.face.SouthFace;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.apache.commons.lang3.StringUtils;
import org.lwjgl.opengl.GL11;

import com.google.common.io.Files;

/**
 * @author Ordinastie
 *
 */
public class MalisisFont
{
	public static MalisisFont minecraftFont = new MinecraftFont();

	/** AWT font used **/
	protected Font font;
	/** Font render context **/
	protected FontRenderContext frc;
	/** Options for the font **/
	protected FontGeneratorOptions options = FontGeneratorOptions.DEFAULT;
	/** Data for each character **/
	protected CharData[] charData = new CharData[256];
	/** ResourceLocation for the texture **/
	protected ResourceLocation textureRl;
	/** Size of the texture (width and height) **/
	protected int size;

	public MalisisFont(File fontFile)
	{
		this(load(fontFile, FontGeneratorOptions.DEFAULT), null);
	}

	public MalisisFont(File fontFile, FontGeneratorOptions options)
	{
		this(load(fontFile, options), options);
	}

	public MalisisFont(ResourceLocation fontFile)
	{
		this(load(fontFile, FontGeneratorOptions.DEFAULT), null);
	}

	public MalisisFont(ResourceLocation fontFile, FontGeneratorOptions options)
	{
		this(load(fontFile, options), options);
	}

	public MalisisFont(Font font)
	{
		this(font, null);
	}

	public MalisisFont(Font font, FontGeneratorOptions options)
	{
		this.font = font;
		if (font == null)
			return;

		if (options != null)
			this.options = options;

		loadCharacterData();
		loadTexture(false);
	}

	public ResourceLocation getResourceLocation()
	{
		return textureRl;
	}

	public void generateTexture(boolean debug)
	{
		this.options.debug = debug;
		loadCharacterData();
		loadTexture(true);
	}

	public Shape getShape(String text, float fontSize)
	{
		text = processString(text, null);
		List<Face> faces = new ArrayList<>();
		float offset = 0;
		float factor = options.fontSize / fontSize;
		for (int i = 0; i < text.length(); i++)
		{
			CharData cd = charData[text.charAt(i)];
			if (cd.getChar() != ' ')
			{
				Face f = new SouthFace();
				f.factor(cd.getFullWidth(options) / factor, cd.getFullHeight(options) / factor, 0);
				f.translate((offset - options.mx) / factor, -options.my / factor, 0);
				f.setTexture(cd.getIcon());

				faces.add(f);
			}
			offset += cd.getCharWidth();

		}

		return new Shape(faces).storeState();
	}

	//#region Prepare/Clean
	private void prepare(MalisisRenderer renderer, float x, float y, float z, FontRenderOptions fro)
	{
		boolean isGui = renderer instanceof GuiRenderer;
		renderer.next(GL11.GL_QUADS);

		Minecraft.getMinecraft().getTextureManager().bindTexture(textureRl);
		GL11.glPushMatrix();
		GL11.glTranslatef(x, y + (isGui ? 0 : fro.fontScale), z);

		if (!isGui)
			GL11.glScalef(1 / 9F, -1 / 9F, 1 / 9F);
	}

	private void clean(MalisisRenderer renderer, boolean isDrawing)
	{
		if (isDrawing)
			renderer.next();
		else
			renderer.draw();
		if (renderer instanceof GuiRenderer)
			Minecraft.getMinecraft().getTextureManager().bindTexture(((GuiRenderer) renderer).getDefaultTexture().getResourceLocation());
		GL11.glPopMatrix();
	}

	private void prepareShadow(MalisisRenderer renderer)
	{
		if (renderer instanceof GuiRenderer)
			return;
		renderer.next();
		GL11.glPolygonOffset(3.0F, 3.0F);
		GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
	}

	private void cleanShadow(MalisisRenderer renderer)
	{
		if (renderer instanceof GuiRenderer)
			return;
		renderer.next();
		GL11.glPolygonOffset(0.0F, 0.0F);
		GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
	}

	private void prepareLines(MalisisRenderer renderer, FontRenderOptions fro)
	{
		renderer.next();
		renderer.disableTextures();
	}

	private void cleanLines(MalisisRenderer renderer)
	{
		renderer.next();
		renderer.enableTextures();
	}

	//#end Prepare/Clean

	public void render(MalisisRenderer renderer, String text, float x, float y, float z, FontRenderOptions fro)
	{
		boolean isDrawing = renderer.isDrawing();

		fro.saveDefault();

		prepare(renderer, x, y, z, fro);

		text = processString(text, fro);

		if (fro.shadow)
		{
			prepareShadow(renderer);
			drawString(text, fro.fontScale, fro.fontScale, fro, true);
			cleanShadow(renderer);
		}
		drawString(text, 0, 0, fro, false);

		if (hasLines(text, fro))
		{
			prepareLines(renderer, fro);
			if (fro.shadow)
			{
				prepareShadow(renderer);
				drawLines(text, fro.fontScale, fro.fontScale, fro, true);
				cleanShadow(renderer);
			}
			drawLines(text, 0, 0, fro, false);
			cleanLines(renderer);
		}

		clean(renderer, isDrawing);
	}

	private void drawString(String text, float offsetX, float offsetY, FontRenderOptions fro, boolean shadow)
	{
		Tessellator t = Tessellator.instance;
		float x = 0;
		float f = fro.fontScale / options.fontSize * 9;

		fro.resetStyles();

		for (int i = 0; i < text.length(); i++)
		{
			i += fro.processStyles(text, i);
			if (i >= text.length())
				break;

			CharData cd = charData[text.charAt(i)];
			t.setColorOpaque_I(shadow ? fro.getShadowColor() : fro.color);
			drawChar(cd, offsetX + x, offsetY, fro);

			x += cd.getCharWidth() * f;
		}
	}

	private void drawChar(CharData cd, float offsetX, float offsetY, FontRenderOptions fro)
	{
		if (cd.getChar() == ' ')
			return;

		Tessellator t = Tessellator.instance;
		float factor = fro.fontScale / options.fontSize * 9;
		float w = cd.getFullWidth(options) * factor;
		float h = cd.getFullHeight(options) * factor;
		float i = fro.italic ? fro.fontScale : 0;

		t.addVertexWithUV(offsetX + i, offsetY, 0, cd.u(), cd.v());
		t.addVertexWithUV(offsetX - i, offsetY + h, 0, cd.u(), cd.V());
		t.addVertexWithUV(offsetX + w - i, offsetY + h, 0, cd.U(), cd.V());
		t.addVertexWithUV(offsetX + w + i, offsetY, 0, cd.U(), cd.v());
	}

	private void drawLines(String text, float offsetX, float offsetY, FontRenderOptions fro, boolean shadow)
	{
		Tessellator t = Tessellator.instance;
		float x = 0;
		float f = fro.fontScale / options.fontSize * 9;

		fro.resetStyles();

		for (int i = 0; i < text.length(); i++)
		{
			i += fro.processStyles(text, i);
			if (i >= text.length())
				break;

			CharData cd = charData[text.charAt(i)];
			t.setColorOpaque_I(shadow ? fro.getShadowColor() : fro.color);

			if (fro.underline)
				drawLineChar(cd, offsetX + x, offsetY + getStringHeight(fro.fontScale) + fro.fontScale, f);
			if (fro.strikethrough)
				drawLineChar(cd, offsetX + x, offsetY + getStringHeight(fro.fontScale) * 0.5F + fro.fontScale, f);

			x += cd.getCharWidth() * f;
		}
	}

	protected void drawLineChar(CharData cd, float offsetX, float offsetY, float factor)
	{
		Tessellator t = Tessellator.instance;
		float w = cd.getFullWidth(options) * factor;
		float h = cd.getFullHeight(options) / 9F * factor;

		t.addVertex(offsetX, offsetY, 0);
		t.addVertex(offsetX, offsetY + h, 0);
		t.addVertex(offsetX + w, offsetY + h, 0);
		t.addVertex(offsetX + w, offsetY, 0);

	}

	//#region String processing
	/**
	 * Processes the passed string by translating it and replacing spacing characters and new lines.<br>
	 * Keeps the formatting if passed at the beginning of the translation key.
	 *
	 * @param str the str
	 * @return the string
	 */
	public String processString(String str, FontRenderOptions fro)
	{
		//		if (fro == null)
		//			fro = new FontRenderOptions();
		//		str = fro.processStyles(str);
		str = StatCollector.translateToLocal(str);
		str = str.replaceAll("\r?\n", "").replaceAll("\t", "    ");
		return str;
	}

	private boolean hasLines(String text, FontRenderOptions fro)
	{
		return fro.underline || fro.strikethrough || text.contains(EnumChatFormatting.UNDERLINE.toString())
				|| text.contains(EnumChatFormatting.STRIKETHROUGH.toString());
	}

	/**
	 * Clips a string to fit in the specified width. The string is translated before clipping.
	 *
	 * @param text the text
	 * @param width the width
	 * @return the string
	 */
	public String clipString(String text, int width)
	{
		return clipString(text, width, 1, false);
	}

	/**
	 * Clips a string to fit in the specified width with the fontScale. The string is translated before clipping.
	 *
	 * @param text the text
	 * @param width the width
	 * @param fontScale the font scale
	 * @return the string
	 */
	public String clipString(String text, int width, float fontScale)
	{
		return clipString(text, width, fontScale, false);
	}

	public String clipString(String text, int width, float fontScale, boolean appendPeriods)
	{
		text = StatCollector.translateToLocal(text);
		StringBuilder ret = new StringBuilder();
		float strWidth = 0;
		int index = 0;

		if (appendPeriods)
			width -= 4;

		while (index < text.length())
		{
			char c = text.charAt(index);
			strWidth += getCharWidth(c, fontScale);
			if (strWidth < width)
				ret.append(c);
			else
			{
				if (appendPeriods)
					ret.append("...");
				return ret.toString();
			}
			index++;
		}

		return ret.toString();
	}

	/**
	 * Gets rendering width of a string.
	 *
	 * @param str the str
	 * @return the string width
	 */
	public float getStringWidth(String str)
	{
		return getStringWidth(str, 1);
	}

	/**
	 * Gets rendering width of a string according to fontScale.
	 *
	 * @param str the str
	 * @param fontScale the font scale
	 * @return the string width
	 */
	public float getStringWidth(String str, float fontScale)
	{
		if (StringUtils.isEmpty(str))
			return 0;

		str = processString(str, null);
		return (float) font.getStringBounds(str, frc).getWidth() / options.fontSize * fontScale * 9;
	}

	/**
	 * Gets the rendering height of strings.
	 *
	 * @return the string height
	 */
	public float getStringHeight()
	{
		return getStringHeight(1);
	}

	/**
	 * Gets the rendering height of strings according to fontscale.
	 *
	 * @param fontScale the font scale
	 * @return the string height
	 */
	public float getStringHeight(float fontScale)
	{
		return fontScale * 9;
	}

	/**
	 * Gets the max string width.
	 *
	 * @param strings the strings
	 * @return the max string width
	 */
	public float getMaxStringWidth(List<String> strings)
	{
		return getMaxStringWidth(strings, 1);
	}

	/**
	 * Gets max rendering width of an array of string.
	 *
	 * @param strings the strings
	 * @param fontScale the font scale
	 * @return the max string width
	 */
	public float getMaxStringWidth(List<String> strings, float fontScale)
	{
		float width = 0;
		for (String str : strings)
			width = Math.max(width, getStringWidth(str, fontScale));
		return width;
	}

	/**
	 * Gets the rendering width of a char.
	 *
	 * @param c the c
	 * @return the char width
	 */
	public float getCharWidth(char c)
	{
		return getCharWidth(c, 1);
	}

	/**
	 * Gets the rendering width of a char with the specified fontScale.
	 *
	 * @param c the c
	 * @param fontScale the font scale
	 * @return the char width
	 */
	public float getCharWidth(char c, float fontScale)
	{
		if (c == '\r' || c == '\n')
			return 0;
		if (c == '\t')
			return getCharWidth(' ', fontScale) * 4;

		CharData cd = charData[c];
		if (cd == null)
			return 0;
		else
			return cd.getCharWidth() / options.fontSize * fontScale * 9;
	}

	/**
	 * Splits the string in multiple lines to fit in the specified maxWidth.
	 *
	 * @param text the text
	 * @param maxWidth the max width
	 * @return list of lines that won't exceed maxWidth limit
	 */
	public List<String> wrapText(String text, int maxWidth)
	{
		return wrapText(text, maxWidth, 1);
	}

	/**
	 * Splits the string in multiple lines to fit in the specified maxWidth using the specified fontScale.
	 *
	 * @param text the text
	 * @param maxWidth the max width
	 * @param fontScale the font scale
	 * @return list of lines that won't exceed maxWidth limit
	 */
	public List<String> wrapText(String text, int maxWidth, float fontScale)
	{
		List<String> lines = new ArrayList<>();
		String[] texts = text.split("\r?(?<=\n)");
		if (texts.length > 1)
		{
			for (String str : texts)
				lines.addAll(wrapText(str, maxWidth, fontScale));
			return lines;
		}

		StringBuilder line = new StringBuilder();
		StringBuilder word = new StringBuilder();
		//FontRenderOptions fro = new FontRenderOptions();

		float lineWidth = 0;
		float wordWidth = 0;
		int index = 0;

		text = StatCollector.translateToLocal(text);

		while (index < text.length())
		{
			char c = text.charAt(index);

			float w = getCharWidth(c, fontScale);
			lineWidth += w;
			wordWidth += w;
			word.append(c);
			//we just ended a new word, add it to the current line
			if (c == ' ' || c == '-' || c == '.')
			{
				line.append(word);
				word.setLength(0);
				wordWidth = 0;
			}
			if (lineWidth >= maxWidth)
			{
				//the first word on the line is too large, split anyway
				if (line.length() == 0)
				{
					line.append(word);
					word.setLength(0);
					wordWidth = 0;
				}
				//make a new line
				lines.add(line.toString());
				line.setLength(0);

				lineWidth = wordWidth;
			}
			index++;
		}

		line.append(word);
		lines.add(line.toString());

		return lines;
	}

	//#end String processing

	//#region Load font
	protected void loadCharacterData()
	{
		frc = new FontRenderContext(null, true, true);

		float totalWidth = 0;
		for (char c = 0; c < 256; c++)
		{
			String s = "" + c;
			LineMetrics lm = font.getLineMetrics(s, frc);
			Rectangle2D bounds = font.getStringBounds(s, frc);
			CharData cd = new CharData(c, lm.getAscent(), (float) bounds.getWidth(), options.fontSize);
			charData[c] = cd;
			totalWidth += cd.getFullWidth(options);
		}

		int split = 1;
		while (totalWidth / split > options.fontSize * split)
			split++;

		//size = (int) Math.max(totalWidth / (split - 1), options.fontSize * (split - 1));
		size = roundUp((int) totalWidth / (split - 1));
	}

	protected int roundUp(int n)
	{
		int r = n - 1;
		r |= r >> 1;
		r |= r >> 2;
		r |= r >> 4;
		r |= r >> 8;
		r |= r >> 16;
		return r + 1;
	}

	protected void loadTexture(boolean forceGenerate)
	{
		File textureFile = new File("fonts/" + font.getName() + ".png");
		File uvFile = new File("fonts/" + font.getName() + ".bin");
		BufferedImage img;
		if (!textureFile.exists() || !uvFile.exists() || forceGenerate)
		{
			MalisisCore.log.info("Generating files for " + font.getName());
			img = new FontGenerator(font, charData, options).generate(size, textureFile, uvFile);
		}
		else
		{
			MalisisCore.log.info("Loading texture and data for " + font.getName());
			img = readTextureFile(textureFile);
			readUVFile(uvFile);
		}

		if (img == null)
			return;

		if (textureRl != null)
			Minecraft.getMinecraft().getTextureManager().deleteTexture(textureRl);

		DynamicTexture dynTex = new DynamicTexture(img);
		textureRl = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation(font.getName(), dynTex);
	}

	protected BufferedImage readTextureFile(File textureFile)
	{
		try
		{
			BufferedImage img = ImageIO.read(textureFile);
			size = img.getWidth();
			return img;
		}
		catch (IOException e)
		{
			MalisisCore.log.error("Failed to read font texture.", e);
		}

		return null;
	}

	protected void readUVFile(File uvFile)
	{
		int i = 0;
		try
		{
			for (String str : Files.readLines(uvFile, StandardCharsets.UTF_8))
			{
				String[] split = str.split(";");
				CharData cd = charData[i++];
				cd.setUVs(Float.parseFloat(split[1]), Float.parseFloat(split[2]), Float.parseFloat(split[3]), Float.parseFloat(split[4]));
			}
		}
		catch (IOException | NumberFormatException e)
		{
			MalisisCore.log.error("Failed to read font data. (Line " + i + " (" + (char) i + ")", e);
		}

	}

	//#end Load font

	//#region Font load
	public static Font load(ResourceLocation rl, FontGeneratorOptions options)
	{
		try
		{
			return load(Minecraft.getMinecraft().getResourceManager().getResource(rl).getInputStream(), options);
		}
		catch (IOException e)
		{
			MalisisCore.log.error("[MalisiFont] Couldn't load font from ResourceLocation.", e);
			return null;
		}
	}

	public static Font load(File file, FontGeneratorOptions options)
	{
		try
		{
			return load(new FileInputStream(file), options);
		}
		catch (FileNotFoundException e)
		{
			MalisisCore.log.error("[MalisiFont] Couldn't load font from File.", e);
			return null;
		}
	}

	public static Font load(InputStream is, FontGeneratorOptions options)
	{
		try
		{
			Font font = Font.createFont(options.fontType, is);
			return font.deriveFont(options.fontSize);
		}
		catch (IOException | FontFormatException e)
		{
			MalisisCore.log.error("[MalisiFont] Couldn't load font from InputStream.", e);
			return null;
		}
	}

	//#end Font load
}
