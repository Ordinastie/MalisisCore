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
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;
import org.lwjgl.opengl.GL11;

import com.google.common.io.Files;

import net.malisis.core.MalisisCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

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
	protected FontGeneratorOptions fontGeneratorOptions = FontGeneratorOptions.DEFAULT;
	/** Data for each character **/
	protected CharData[] charData = new CharData[256];
	/** ResourceLocation for the texture **/
	protected ResourceLocation textureRl;
	/** Size of the texture (width and height) **/
	protected int size;

	protected boolean loaded = false;

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
			this.fontGeneratorOptions = options;

		loadCharacterData();
		loaded = loadTexture(false);
	}

	public boolean isLoaded()
	{
		return loaded;
	}

	public ResourceLocation getResourceLocation()
	{
		return textureRl;
	}

	public void generateTexture(boolean debug)
	{
		this.fontGeneratorOptions.debug = debug;
		loadCharacterData();
		loadTexture(true);
	}

	public CharData getCharData(char c)
	{
		if (c < 0 || c > charData.length)
			c = '?';
		return charData[c];
	}

	//#region Prepare/Clean
	protected void prepare()
	{
		VertexBuffer buffer = Tessellator.getInstance().getBuffer();
		if (buffer.isDrawing)
			Tessellator.getInstance().draw();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
		Minecraft.getMinecraft().getTextureManager().bindTexture(textureRl);
	}

	protected void clean()
	{
		Tessellator.getInstance().draw();
	}

	protected void prepareLines()
	{
		Tessellator.getInstance().getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		GlStateManager.disableTexture2D();
	}

	protected void cleanLines()
	{
		Tessellator.getInstance().getBuffer().finishDrawing();
		GlStateManager.enableTexture2D();
	}

	/**
	 * Render the
	 *
	 * @param lines the lines
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @param options the options
	 */
	//#end Prepare/Clean
	public void render(List<String> lines, float x, float y, float z, FontOptions options)
	{
		if (!isLoaded() || lines.isEmpty())
			return;

		boolean hasLines = false;
		float originY = y;
		prepare();

		StringWalker walker = new StringWalker(this, options);
		walker.applyStyles(true);

		for (String text : lines)
		{
			hasLines |= hasLines(text, options);
			walker.setText(text);
			if (options.hasShadow())
				drawString(walker, text, x, y, z, options, true);
			drawString(walker, text, x, y, z, options, false);
			y += getStringHeight(options) + 2;
		}
		clean();

		if (hasLines)
			return;

		//render underline or strikethrough
		y = originY;
		prepareLines();
		walker.reset();
		for (String text : lines)
		{
			if (options.hasShadow())
				drawLines(walker, text, x, y, z, options, true);
			drawLines(walker, text, x, y, z, options, false);
			y += getStringHeight(options) + 2;
		}
		cleanLines();
	}

	public void render(String text, float x, float y, float z, FontOptions options)
	{
		if (!isLoaded() || StringUtils.isEmpty(text))
			return;

		prepare();
		StringWalker walker = new StringWalker(text, this, options);
		walker.applyStyles(true);
		if (options.hasShadow())
			drawString(walker, text, x, y, z, options, true);
		drawString(walker, text, x, y, z, options, false);
		clean();

		if (!hasLines(text, options))
			return;

		walker.reset();
		prepareLines();
		if (options.hasShadow())
			drawLines(walker, text, x, y, z, options, true);
		drawLines(walker, text, x, y, z, options, false);
		cleanLines();

	}

	protected void drawString(StringWalker walker, String text, float x, float y, float z, FontOptions options, boolean shadow)
	{
		while (walker.walk())
		{
			CharData cd = getCharData(walker.getChar());
			drawChar(walker, cd, x, y, z, options, shadow);
			if (walker.isBold())
				drawChar(walker, cd, x + options.getFontScale(), y, z, options, shadow);
			x += walker.getWidth();
		}
	}

	protected void drawChar(StringWalker walker, CharData cd, float x, float y, float z, FontOptions options, boolean shadow)
	{
		if (Character.isWhitespace(cd.getChar()))
			return;

		VertexBuffer buffer = Tessellator.getInstance().getBuffer();
		float factor = options.getFontScale() / fontGeneratorOptions.fontSize * 9;
		float w = cd.getFullWidth(fontGeneratorOptions) * factor;
		float h = cd.getFullHeight(fontGeneratorOptions) * factor;
		float i = walker.isItalic() ? options.getFontScale() : 0;
		int color = walker.getColor();
		if (shadow)
		{
			x += options.getFontScale();
			y += options.getFontScale();
			color = FontOptions.getShadowColor(color);
		}

		//TODO: vertex data ?
		buffer.pos(x + i, y, z);
		buffer.tex(cd.u(), cd.v());
		buffer.color((color >> 16) & 255, (color >> 8) & 255, color & 255, 255);
		buffer.endVertex();

		buffer.pos(x - i, y + h, z);
		buffer.tex(cd.u(), cd.V());
		buffer.color((color >> 16) & 255, (color >> 8) & 255, color & 255, 255);
		buffer.endVertex();

		buffer.pos(x + w - i, y + h, z);
		buffer.tex(cd.U(), cd.V());
		buffer.color((color >> 16) & 255, (color >> 8) & 255, color & 255, 255);
		buffer.endVertex();

		buffer.pos(x + w + i, y, z);
		buffer.tex(cd.U(), cd.v());
		buffer.color((color >> 16) & 255, (color >> 8) & 255, color & 255, 255);
		buffer.endVertex();

		/*
		wr.setColorOpaque_I(drawingShadow ? fro.getShadowColor() : fro.color);
		wr.setBrightness(Vertex.BRIGHTNESS_MAX);
		wr.addVertexWithUV(offsetX + i, offsetY, 0, cd.u(), cd.v());
		wr.addVertexWithUV(offsetX - i, offsetY + h, 0, cd.u(), cd.V());
		wr.addVertexWithUV(offsetX + w - i, offsetY + h, 0, cd.U(), cd.V());
		wr.addVertexWithUV(offsetX + w + i, offsetY, 0, cd.U(), cd.v());
		*/
	}

	protected void drawLines(StringWalker walker, String text, float x, float y, float z, FontOptions options, boolean shadow)
	{
		while (walker.walk())
		{
			if (!walker.isFormatted())
			{
				CharData cd = getCharData(walker.getChar());
				if (options.isUnderline())
					drawLineChar(walker, cd, x, getStringHeight(options) + options.getFontScale(), z, options, shadow);
				if (options.isStrikethrough())
					drawLineChar(walker, cd, x, getStringHeight(options) * 0.5F + options.getFontScale(), z, options, shadow);

				x += walker.getWidth();
			}
		}
	}

	protected void drawLineChar(StringWalker walker, CharData cd, float x, float y, float z, FontOptions options, boolean shadow)
	{
		VertexBuffer buffer = Tessellator.getInstance().getBuffer();
		float factor = options.getFontScale() / fontGeneratorOptions.fontSize * 9;
		float w = cd.getFullWidth(fontGeneratorOptions) * factor;
		float h = cd.getFullHeight(fontGeneratorOptions) / 9F * factor;
		int color = walker.getColor();
		if (shadow)
		{
			x += options.getFontScale();
			y += options.getFontScale();
			color = FontOptions.getShadowColor(color);
		}

		buffer.pos(x, y, z);
		buffer.color((color >> 16) & 255, (color >> 8) & 255, color & 255, 255);
		buffer.endVertex();

		buffer.pos(x, y + h, z);
		buffer.color((color >> 16) & 255, (color >> 8) & 255, color & 255, 255);
		buffer.endVertex();

		buffer.pos(x + w, y + h, z);
		buffer.color((color >> 16) & 255, (color >> 8) & 255, color & 255, 255);
		buffer.endVertex();

		buffer.pos(x + w, y, z);
		buffer.color((color >> 16) & 255, (color >> 8) & 255, color & 255, 255);
		buffer.endVertex();

		/*
		wr.setColorOpaque_I(drawingShadow ? fro.getShadowColor() : fro.color);
		wr.addVertex(offsetX, offsetY, 0);
		wr.addVertex(offsetX, offsetY + h, 0);
		wr.addVertex(offsetX + w, offsetY + h, 0);
		wr.addVertex(offsetX + w, offsetY, 0);
		*/
	}

	//#region String processing

	private boolean hasLines(String text, FontOptions options)
	{
		return options.isUnderline() || options.isStrikethrough() || text.contains(TextFormatting.UNDERLINE.toString())
				|| text.contains(TextFormatting.STRIKETHROUGH.toString());
	}

	/**
	 * Clips a string to fit in the specified width. The string is translated before clipping.
	 *
	 * @param str the str
	 * @param width the width
	 * @return the string
	 */
	public String clipString(String str, int width)
	{
		return clipString(str, width, null, false);
	}

	/**
	 * Clips a string to fit in the specified width. The string is translated before clipping.
	 *
	 * @param str the str
	 * @param width the width
	 * @param options the options
	 * @return the string
	 */
	public String clipString(String str, int width, FontOptions options)
	{
		return clipString(str, width, options, false);
	}

	/**
	 * Clips a string to fit in the specified width. The string is translated before clipping.
	 *
	 * @param str the str
	 * @param width the width
	 * @param options the options
	 * @param appendPeriods the append periods
	 * @return the string
	 */
	public String clipString(String str, int width, FontOptions options, boolean appendPeriods)
	{
		if (appendPeriods)
			width -= 4;

		int pos = (int) getCharPosition(str, options, width, 0);
		return str.substring(0, pos) + (pos < str.length() && appendPeriods ? "..." : "");
	}

	/**
	 * Gets rendering width of a string.
	 *
	 * @param str the str
	 * @return the string width
	 */
	public float getStringWidth(String str)
	{
		return getStringWidth(str, null);
	}

	/**
	 * Gets rendering width of a string according to fontScale.
	 *
	 * @param str the str
	 * @param options the options
	 * @param start the start
	 * @param end the end
	 * @return the string width
	 */
	public float getStringWidth(String str, FontOptions options, int start, int end)
	{
		if (StringUtils.isEmpty(str) || start >= end)
			return 0;

		float width = 0;
		StringWalker walker = new StringWalker(this, options);
		walker.setText(str, start, end);
		while (walker.walk())
			width += walker.getWidth();

		return width;// * (fro != null ? fro.fontScale : 1);
	}

	public float getStringWidth(String str, FontOptions options)
	{
		if (StringUtils.isEmpty(str))
			return 0;
		return getStringWidth(str, options, 0, 0);
	}

	/**
	 * Gets the rendering height of strings.
	 *
	 * @return the string height
	 */
	public float getStringHeight()
	{
		return getStringHeight(null);
	}

	/**
	 * Gets the rendering height of strings according to fontscale.
	 *
	 * @param options the options
	 * @return the string height
	 */
	public float getStringHeight(FontOptions options)
	{
		return (options != null ? options.getFontScale() : 1) * 9; //fontRenderer.FONT_HEIGHT = 9;
	}

	/**
	 * Gets the max string width.
	 *
	 * @param strings the strings
	 * @return the max string width
	 */
	public float getMaxStringWidth(List<String> strings)
	{
		return getMaxStringWidth(strings, null);
	}

	/**
	 * Gets max rendering width of an array of string.
	 *
	 * @param strings the strings
	 * @param options the options
	 * @return the max string width
	 */
	public float getMaxStringWidth(List<String> strings, FontOptions options)
	{
		float width = 0;
		for (String str : strings)
			width = Math.max(width, getStringWidth(str, options));
		return width;
	}

	/**
	 * Gets the rendering width of a char.
	 *
	 * @param c the c
	 * @param options the options
	 * @return the char width
	 */
	public float getCharWidth(char c)
	{
		if (c == '\r' || c == '\n')
			return 0;
		if (c == '\t')
			return getCharWidth(' ') * 4;

		return getCharData(c).getCharWidth() / fontGeneratorOptions.fontSize * 9;
	}

	/**
	 * Determines the character for a given X coordinate.
	 *
	 * @param str the str
	 * @param options the options
	 * @param position the position
	 * @param charOffset the char offset
	 * @return position
	 */
	public float getCharPosition(String str, FontOptions options, int position, int charOffset)
	{
		if (StringUtils.isEmpty(str))
			return 0;

		//float fx = position / (fro != null ? fro.fontScale : 1); //factor the position instead of the char widths

		StringWalker walker = new StringWalker(this, options);
		walker.setText(str, charOffset, str.length());
		walker.skipFormattingChars(true);
		return walker.walkTo(position);
	}

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
			CharData cd = new CharData(c, lm.getAscent(), (float) bounds.getWidth(), fontGeneratorOptions.fontSize);
			charData[c] = cd;
			totalWidth += cd.getFullWidth(fontGeneratorOptions);
		}

		int split = 1;
		while (totalWidth / split > fontGeneratorOptions.fontSize * split)
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

	protected boolean loadTexture(boolean forceGenerate)
	{
		File textureFile = new File("fonts/" + font.getName() + ".png");
		File uvFile = new File("fonts/" + font.getName() + ".bin");
		BufferedImage img;
		if (!textureFile.exists() || !uvFile.exists() || forceGenerate)
		{
			MalisisCore.log.info("Generating files for " + font.getName());
			img = new FontGenerator(font, charData, fontGeneratorOptions).generate(size, textureFile, uvFile);
		}
		else
		{
			MalisisCore.log.info("Loading texture and data for " + font.getName());
			img = readTextureFile(textureFile);
			readUVFile(uvFile);
		}

		if (img == null)
			return false;

		if (textureRl != null)
			Minecraft.getMinecraft().getTextureManager().deleteTexture(textureRl);

		DynamicTexture dynTex = new DynamicTexture(img);
		textureRl = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation(font.getName(), dynTex);

		return true;
	}

	protected BufferedImage readTextureFile(File textureFile)
	{
		try
		{
			BufferedImage img = ImageIO.read(textureFile);
			if (img == null)
			{
				MalisisCore.log.error("Failed to read font texture, no image could be read from the file.");
				return null;
			}
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
