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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import net.malisis.core.MalisisCore;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.renderer.MalisisRenderer;
import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.element.Vertex;
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

	private static Pattern pattern = Pattern.compile("\\{(.*?)}");

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
	/** Whether the currently drawn text is the shadow part **/
	protected boolean drawingShadow = false;

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

	public CharData getCharData(char c)
	{
		if (c < 0 || c > charData.length)
			c = '?';
		return charData[c];
	}

	public Shape getShape(String text, float fontSize)
	{
		text = processString(text, null);
		List<Face> faces = new ArrayList<>();
		float offset = 0;
		float factor = options.fontSize / fontSize;
		for (int i = 0; i < text.length(); i++)
		{
			CharData cd = getCharData(text.charAt(i));
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
	protected void prepare(MalisisRenderer renderer, float x, float y, float z, FontRenderOptions fro)
	{
		boolean isGui = renderer instanceof GuiRenderer;
		renderer.next(GL11.GL_QUADS);

		Minecraft.getMinecraft().getTextureManager().bindTexture(textureRl);
		GL11.glPushMatrix();
		GL11.glTranslatef(x, y + (isGui ? 0 : fro.fontScale), z);

		if (!isGui)
			GL11.glScalef(1 / 9F, -1 / 9F, 1 / 9F);
	}

	protected void clean(MalisisRenderer renderer, boolean isDrawing)
	{
		if (isDrawing)
			renderer.next();
		else
			renderer.draw();
		if (renderer instanceof GuiRenderer)
			Minecraft.getMinecraft().getTextureManager().bindTexture(((GuiRenderer) renderer).getDefaultTexture().getResourceLocation());
		GL11.glPopMatrix();
	}

	protected void prepareShadow(MalisisRenderer renderer)
	{
		drawingShadow = true;
		if (renderer instanceof GuiRenderer)
			return;
		renderer.next();
		GL11.glPolygonOffset(3.0F, 3.0F);
		GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
	}

	protected void cleanShadow(MalisisRenderer renderer)
	{
		drawingShadow = false;
		if (renderer instanceof GuiRenderer)
			return;
		renderer.next();
		GL11.glPolygonOffset(0.0F, 0.0F);
		GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
	}

	protected void prepareLines(MalisisRenderer renderer, FontRenderOptions fro)
	{
		renderer.next();
		renderer.disableTextures();
	}

	protected void cleanLines(MalisisRenderer renderer)
	{
		renderer.next();
		renderer.enableTextures();
	}

	//#end Prepare/Clean

	public void render(MalisisRenderer renderer, String text, float x, float y, float z, FontRenderOptions fro)
	{
		if (StringUtils.isEmpty(text))
			return;

		boolean isDrawing = renderer.isDrawing();

		prepare(renderer, x, y, z, fro);

		text = processString(text, fro);

		if (fro.shadow)
		{
			prepareShadow(renderer);
			drawString(text, fro);
			cleanShadow(renderer);
		}
		drawString(text, fro);

		if (hasLines(text, fro))
		{
			prepareLines(renderer, fro);
			if (fro.shadow)
			{
				prepareShadow(renderer);
				drawLines(text, fro);
				cleanShadow(renderer);
			}
			drawLines(text, fro);
			cleanLines(renderer);
		}

		clean(renderer, isDrawing);
	}

	protected void drawString(String text, FontRenderOptions fro)
	{
		float x = 0;
		float f = fro.fontScale / options.fontSize * 9;

		fro.resetStylesLine();
		StringWalker walker = new StringWalker(text, this, fro);
		walker.applyStyles(true);
		while (walker.walk())
		{
			CharData cd = getCharData(walker.getChar());
			drawChar(cd, x, 0, fro);
			x += walker.getWidth() * f;
		}
	}

	protected void drawChar(CharData cd, float offsetX, float offsetY, FontRenderOptions fro)
	{
		if (Character.isWhitespace(cd.getChar()))
			return;

		Tessellator t = Tessellator.instance;
		float factor = fro.fontScale / options.fontSize * 9;
		float w = cd.getFullWidth(options) * factor;
		float h = cd.getFullHeight(options) * factor;
		float i = fro.italic ? fro.fontScale : 0;

		if (drawingShadow)
		{
			offsetX += fro.fontScale;
			offsetY += fro.fontScale;
		}

		t.setColorOpaque_I(drawingShadow ? fro.getShadowColor() : fro.color);
		t.setBrightness(Vertex.BRIGHTNESS_MAX);
		t.addVertexWithUV(offsetX + i, offsetY, 0, cd.u(), cd.v());
		t.addVertexWithUV(offsetX - i, offsetY + h, 0, cd.u(), cd.V());
		t.addVertexWithUV(offsetX + w - i, offsetY + h, 0, cd.U(), cd.V());
		t.addVertexWithUV(offsetX + w + i, offsetY, 0, cd.U(), cd.v());
	}

	protected void drawLines(String text, FontRenderOptions fro)
	{
		float x = 0;
		float f = fro.fontScale / options.fontSize * 9;

		fro.resetStylesLine();

		StringWalker walker = new StringWalker(text, this, fro);
		walker.applyStyles(true);
		while (walker.walk())
		{
			if (!walker.isFormatting())
			{
				CharData cd = getCharData(walker.getChar());
				if (fro.underline)
					drawLineChar(cd, x, getStringHeight(fro) + fro.fontScale, fro);
				if (fro.strikethrough)
					drawLineChar(cd, x, getStringHeight(fro) * 0.5F + fro.fontScale, fro);

				x += walker.getWidth() * f;
			}
		}
	}

	protected void drawLineChar(CharData cd, float offsetX, float offsetY, FontRenderOptions fro)
	{
		Tessellator t = Tessellator.instance;
		float factor = fro.fontScale / options.fontSize * 9;
		float w = cd.getFullWidth(options) * factor;
		float h = cd.getFullHeight(options) / 9F * factor;

		if (drawingShadow)
		{
			offsetX += fro.fontScale;
			offsetY += fro.fontScale;
		}

		t.setColorOpaque_I(drawingShadow ? fro.getShadowColor() : fro.color);
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
		str = translate(str);
		//str = str.replaceAll("\r?\n", "").replaceAll("\t", "    ");
		return str;
	}

	private String translate(String str)
	{
		if (str.indexOf('{') == -1 || str.indexOf('{') >= str.indexOf('}'))
			return StatCollector.translateToLocal(str);

		StringBuffer output = new StringBuffer();
		Matcher matcher = pattern.matcher(str);

		while (matcher.find())
			matcher.appendReplacement(output, StatCollector.translateToLocal(matcher.group(1)));

		matcher.appendTail(output);
		return output.toString();
	}

	private boolean hasLines(String text, FontRenderOptions fro)
	{
		return fro.underline || fro.strikethrough || text.contains(EnumChatFormatting.UNDERLINE.toString())
				|| text.contains(EnumChatFormatting.STRIKETHROUGH.toString());
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
	 * @param fro the fro
	 * @return the string
	 */
	public String clipString(String str, int width, FontRenderOptions fro)
	{
		return clipString(str, width, fro, false);
	}

	/**
	 * Clips a string to fit in the specified width. The string is translated before clipping.
	 *
	 * @param str the str
	 * @param width the width
	 * @param fro the fro
	 * @param appendPeriods the append periods
	 * @return the string
	 */
	public String clipString(String str, int width, FontRenderOptions fro, boolean appendPeriods)
	{
		str = processString(str, fro);
		if (appendPeriods)
			width -= 4;

		int pos = (int) getCharPosition(str, fro, width, 0);
		return str.substring(0, pos) + (appendPeriods ? "..." : "");
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
	 * @param fro the fro
	 * @param start the start
	 * @param end the end
	 * @return the string width
	 */
	public float getStringWidth(String str, FontRenderOptions fro, int start, int end)
	{
		if (start > end)
			return 0;

		if (fro != null && !fro.disableECF)
			str = EnumChatFormatting.getTextWithoutFormattingCodes(str);

		if (StringUtils.isEmpty(str))
			return 0;

		str = processString(str, fro);
		return (float) font.getStringBounds(str, frc).getWidth() / options.fontSize * (fro != null ? fro.fontScale : 1) * 9;
	}

	public float getStringWidth(String str, FontRenderOptions fro)
	{
		if (StringUtils.isEmpty(str))
			return 0;
		return getStringWidth(str, fro, 0, 0);
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
	 * @param fro the fro
	 * @return the string height
	 */
	public float getStringHeight(FontRenderOptions fro)
	{
		return (fro != null ? fro.fontScale : 1) * 9;
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
	 * @param fro the fro
	 * @return the max string width
	 */
	public float getMaxStringWidth(List<String> strings, FontRenderOptions fro)
	{
		float width = 0;
		for (String str : strings)
			width = Math.max(width, getStringWidth(str, fro));
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
		return getCharWidth(c, null);
	}

	/**
	 * Gets the rendering width of a char with the specified fontScale.
	 *
	 * @param c the c
	 * @param fro the fro
	 * @return the char width
	 */
	public float getCharWidth(char c, FontRenderOptions fro)
	{
		if (c == '\r' || c == '\n')
			return 0;
		if (c == '\t')
			return getCharWidth(' ', fro) * 4;

		return getCharData(c).getCharWidth() / options.fontSize * (fro != null ? fro.fontScale : 1) * 9;
	}

	/**
	 * Determines the character for a given X coordinate.
	 *
	 * @param str the str
	 * @param fro the fro
	 * @param position the position
	 * @param charOffset the char offset
	 * @return position
	 */
	public float getCharPosition(String str, FontRenderOptions fro, int position, int charOffset)
	{
		if (StringUtils.isEmpty(str))
			return 0;

		str = processString(str, fro);
		float fx = position / (fro != null ? fro.fontScale : 1); //factor the position instead of the char widths

		StringWalker walker = new StringWalker(str, this, fro);
		walker.startIndex(charOffset);
		walker.skipChars(true);
		return walker.walkTo(fx);
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
		return wrapText(text, maxWidth, null);
	}

	/**
	 * Splits the string in multiple lines to fit in the specified maxWidth using the specified fontScale.
	 *
	 * @param str the str
	 * @param maxWidth the max width
	 * @param fro the fro
	 * @return list of lines that won't exceed maxWidth limit
	 */
	public List<String> wrapText(String str, int maxWidth, FontRenderOptions fro)
	{
		List<String> lines = new ArrayList<>();
		String[] texts = str.split("\r?(?<=\n)");
		if (texts.length > 1)
		{
			for (String t : texts)
				lines.addAll(wrapText(t, maxWidth, fro));
			return lines;
		}

		StringBuilder line = new StringBuilder();
		StringBuilder word = new StringBuilder();
		//FontRenderOptions fro = new FontRenderOptions();

		maxWidth -= 4;
		maxWidth /= (fro != null ? fro.fontScale : 1); //factor the position instead of the char widths
		float lineWidth = 0;
		float wordWidth = 0;

		str = processString(str, fro);

		StringWalker walker = new StringWalker(str, this, fro);
		walker.skipChars(false);
		walker.applyStyles(false);
		while (walker.walk())
		{
			char c = walker.getChar();
			lineWidth += walker.getWidth();
			wordWidth += walker.getWidth();
			//			if (walker.isFormatting())
			//			{
			//				word.append(walker.getFormatting());
			//				continue;
			//			}
			//			else
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
