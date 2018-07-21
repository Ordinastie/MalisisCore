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
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;
import org.lwjgl.opengl.GL11;

import com.google.common.io.Files;

import net.malisis.core.MalisisCore;
import net.malisis.core.client.gui.element.IClipable.ClipArea;
import net.malisis.core.client.gui.render.GuiRenderer;
import net.malisis.core.client.gui.text.GuiText;
import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.element.face.SouthFace;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

/**
 * @author Ordinastie
 *
 */
public class MalisisFont
{
	public static String CHARLIST = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&\'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000";
	private static Pattern pattern = Pattern.compile("\\{(.*?)}");
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
	/** Whether the currently drawn text is the shadow part **/
	protected boolean drawingShadow = false;
	protected float zIndex = 0f;

	private boolean loaded = false;

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

	public Shape getShape(String text, float fontSize)
	{
		text = processString(text, null);
		List<Face> faces = new ArrayList<>();
		float offset = 0;
		float factor = fontGeneratorOptions.fontSize / fontSize;
		for (int i = 0; i < text.length(); i++)
		{
			CharData cd = getCharData(text.charAt(i));
			if (cd.getChar() != ' ')
			{
				Face f = new SouthFace();
				f.scale(cd.getFullWidth(fontGeneratorOptions) / factor, cd.getFullHeight(fontGeneratorOptions) / factor, 0);
				f.translate((offset - fontGeneratorOptions.mx) / factor, -fontGeneratorOptions.my / factor, 0);
				f.setTexture(cd.getIcon());

				faces.add(f);
			}
			offset += cd.getCharWidth();

		}

		return new Shape(faces).storeState();
	}

	//#region Prepare/Clean
	protected void prepare(GuiRenderer renderer, float x, float y, float z, FontOptions options)
	{
		renderer.next(GL11.GL_QUADS);

		Minecraft.getMinecraft().getTextureManager().bindTexture(textureRl);
		GL11.glPushMatrix();
		GL11.glTranslatef(x, y, 0);

		zIndex = z;
	}

	protected void clean(GuiRenderer renderer, boolean isDrawing)
	{
		if (isDrawing)
			renderer.next();
		else
			renderer.draw();
		if (renderer instanceof GuiRenderer)
			Minecraft.getMinecraft().getTextureManager().bindTexture(renderer.getDefaultTexture().getResourceLocation());
		GL11.glPopMatrix();

		zIndex = 0;
	}

	protected void prepareShadow(GuiRenderer renderer)
	{
		drawingShadow = true;
		if (renderer instanceof GuiRenderer)
			return;
		renderer.next();
		GL11.glPolygonOffset(3.0F, 3.0F);
		GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
	}

	protected void cleanShadow(GuiRenderer renderer)
	{
		drawingShadow = false;
		if (renderer instanceof GuiRenderer)
			return;
		renderer.next();
		GL11.glPolygonOffset(0.0F, 0.0F);
		GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
	}
	//#end Prepare/Clean

	private boolean isCharVisible(int x, int y, StringWalker walker, ClipArea area)
	{
		if (area == null || area.noClip())
			return true;
		if (area.fullClip())
			return false;

		return area.isInside(x, y) || area.isInside(x + (int) Math.ceil(walker.width()), y + (int) Math.ceil(walker.height()));
	}

	public void render(GuiRenderer renderer, GuiText text, int x, int y, int z, FontOptions options, ClipArea clipArea)
	{
		if (text.length() <= 0)
			return;

		boolean isDrawing = renderer.isDrawing();
		prepare(renderer, x, y, z, options);

		try
		{
			StringWalker walker = new StringWalker(text, options);
			walker.applyStyles(true);

			float rx = 0;
			float ry = 0;

			while (walker.walk())
			{
				rx = walker.x();
				ry = walker.y();
				if (isCharVisible((int) (x + rx), (int) (y + ry), walker, clipArea))
				{
					options = walker.currentStyle();
					renderCharacter(walker.getChar(), rx, ry, options);
				}
				//rx += walker.width();

				//if justified
				//LineInfo info = text.lines().get(walker.lineIndex);
				//rx += info.spaceWidth() / info.text().length();

				//				if (walker.isEOL())
				//				{
				//					ry += walker.lineHeight();
				//					rx = 0;
				//				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		clean(renderer, isDrawing);
	}

	protected void renderCharacter(char c, float x, float y, FontOptions options)
	{
		CharData cd = getCharData(c);
		if (options.isObfuscated())
			cd = getRandomChar(cd);
		float fs = options.getFontScale();

		//draw shadow first
		if (options.hasShadow())
		{
			drawChar(cd, x + fs, y + fs, options, options.getShadowColor());
			if (options.isBold())
				drawChar(cd, x + 2 * fs, y + fs, options, options.getShadowColor());
			if (options.isUnderline())
				drawLine(cd, x + fs, y + 2 * fs, options, options.getShadowColor());
		}

		drawChar(cd, x, y, options, options.getColor());
		if (options.isBold())
			drawChar(cd, x + fs, y, options, options.getColor());
		if (options.isUnderline())
			drawLine(cd, x, y + fs, options, options.getColor());
	}

	protected void drawChar(CharData cd, float offsetX, float offsetY, FontOptions options, int color)
	{
		if (Character.isWhitespace(cd.getChar()))
			return;

		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		float factor = options.getFontScale() / fontGeneratorOptions.fontSize * 9;
		float w = cd.getFullWidth(fontGeneratorOptions) * factor;
		float h = cd.getFullHeight(fontGeneratorOptions) * factor;
		float i = options.isItalic() ? options.getFontScale() : 0;

		buffer.pos(offsetX + i, offsetY, zIndex);
		buffer.tex(cd.u(), cd.v());
		buffer.color((color >> 16) & 255, (color >> 8) & 255, color & 255, 255);
		buffer.endVertex();

		buffer.pos(offsetX - i, offsetY + h, zIndex);
		buffer.tex(cd.u(), cd.V());
		buffer.color((color >> 16) & 255, (color >> 8) & 255, color & 255, 255);
		buffer.endVertex();

		buffer.pos(offsetX + w - i, offsetY + h, zIndex);
		buffer.tex(cd.U(), cd.V());
		buffer.color((color >> 16) & 255, (color >> 8) & 255, color & 255, 255);
		buffer.endVertex();

		buffer.pos(offsetX + w + i, offsetY, zIndex);
		buffer.tex(cd.U(), cd.v());
		buffer.color((color >> 16) & 255, (color >> 8) & 255, color & 255, 255);
		buffer.endVertex();
	}

	protected void drawLine(CharData cd, float offsetX, float offsetY, FontOptions options, int color)
	{
		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		float factor = options.getFontScale() / fontGeneratorOptions.fontSize * 9;
		float w = cd.getFullWidth(fontGeneratorOptions) * factor + options.getFontScale();
		if (options.isBold())
			w += options.getFontScale();
		float h = cd.getFullHeight(fontGeneratorOptions) * factor;

		//use underscore char data for UVs
		cd = getCharData('_');

		buffer.pos(offsetX, offsetY, zIndex);
		buffer.tex(cd.u(), cd.v());
		buffer.color((color >> 16) & 255, (color >> 8) & 255, color & 255, 255);
		buffer.endVertex();

		buffer.pos(offsetX, offsetY + h, zIndex);
		buffer.tex(cd.u(), cd.V());
		buffer.color((color >> 16) & 255, (color >> 8) & 255, color & 255, 255);
		buffer.endVertex();

		buffer.pos(offsetX + w, offsetY + h, zIndex);
		buffer.tex(cd.U(), cd.V());
		buffer.color((color >> 16) & 255, (color >> 8) & 255, color & 255, 255);
		buffer.endVertex();

		buffer.pos(offsetX + w, offsetY, zIndex);
		buffer.tex(cd.U(), cd.v());
		buffer.color((color >> 16) & 255, (color >> 8) & 255, color & 255, 255);
		buffer.endVertex();
	}

	public CharData getRandomChar(CharData cd)
	{
		Random rand = Minecraft.getMinecraft().fontRenderer.fontRandom;
		float w = cd.getCharWidth();

		while (true)
		{
			int index = rand.nextInt(CHARLIST.length());
			cd = getCharData(CHARLIST.charAt(index));
			if (cd.getCharWidth() == w)
				return cd;
		}
	}

	//#region String processing
	/**
	 * Processes the passed string by translating it and replacing spacing characters and new lines.<br>
	 * Keeps the formatting if passed at the beginning of the translation key.
	 *
	 * @param str the str
	 * @return the string
	 */
	public String processString(String str, FontOptions options)
	{
		return str;
		//str = str.replaceAll("\r?\n", "");
		//		if (!options.shouldTranslate())
		//			return str;
		//		Pair<String, String> p = FontOptions.getStartFormat(str);
		//		return p.getLeft() + translate(p.getRight());
	}

	private String translate(String str)
	{
		if (str.indexOf('{') == -1 || str.indexOf('{') >= str.indexOf('}'))
			return I18n.format(str);

		StringBuffer output = new StringBuffer();
		Matcher matcher = pattern.matcher(str);

		while (matcher.find())
			matcher.appendReplacement(output, I18n.format(matcher.group(1)));

		matcher.appendTail(output);
		return output.toString();
	}

	/**
	 * Clips a string to fit in the specified width.
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
	 * Clips a string to fit in the specified width.
	 *
	 * @param str the str
	 * @param width the width
	 * @param options the options
	 * @param appendPeriods the append periods
	 * @return the string
	 */
	public String clipString(String str, int width, FontOptions options, boolean appendPeriods)
	{
		str = processString(str, options);
		if (appendPeriods)
			width -= 4;

		int pos = (int) getCharPosition(str, options, width, 0);
		return str.substring(0, pos) + (pos < str.length() && appendPeriods ? "..." : "");
	}

	/**
	 * Gets the rendering width of the text.
	 *
	 * @param text the text
	 * @param options the options
	 * @return the string width
	 */
	public float getStringWidth(String text, FontOptions options)
	{
		if (StringUtils.isEmpty(text))
			return 0;

		StringWalker walker = new StringWalker(text, options);
		walker.walkToEnd();
		return walker.width();
	}

	/**
	 * Gets the rendering height of strings.
	 *
	 * @return the string height
	 */
	public float getStringHeight(String text, FontOptions options)
	{
		StringWalker walker = new StringWalker(text, options);
		walker.walkToEnd();
		return walker.lineHeight();
	}

	/**
	 * Gets the rendering height of strings according to fontscale.
	 *
	 * @param options the options
	 * @return the string height
	 */
	public float getStringHeight(FontOptions options)
	{
		return (options != null ? options.getFontScale() : 1) * 9;
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
	 * Gets the rendering width of a character.
	 *
	 * @param c the c
	 * @param options the options
	 * @return the char width
	 */
	public float getCharWidth(char c, FontOptions options)
	{
		if (c == '\r' || c == '\n')
			return 0;
		if (c == '\t')
			return getCharWidth(' ', options) * 4;

		return getCharData(c).getCharWidth() / fontGeneratorOptions.fontSize * (options != null ? options.getFontScale() : 1) * 9;
	}

	/**
	 * Gets the rendering height of a character.
	 *
	 * @param c the c
	 * @param options the options
	 * @return the char height
	 */
	public float getCharHeight(char c, FontOptions options)
	{
		return getCharData(c).getCharHeight() / fontGeneratorOptions.fontSize * (options != null ? options.getFontScale() : 1) * 9;
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

		str = processString(str, options);
		//float fx = position / (fro != null ? fro.fontScale : 1); //factor the position instead of the char widths

		StringWalker walker = new StringWalker(str, options);
		//walker.startIndex(charOffset);
		walker.skipChars(true);
		return 0;//walker.walkToCoord(position);
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
		try (InputStream is = Minecraft.getMinecraft().getResourceManager().getResource(rl).getInputStream())
		{
			return load(is, options);
		}
		catch (IOException e)
		{
			MalisisCore.log.error("[MalisiFont] Couldn't load font from ResourceLocation.", e);
			return null;
		}
	}

	public static Font load(File file, FontGeneratorOptions options)
	{
		try (InputStream is = new FileInputStream(file))
		{
			return load(is, options);
		}
		catch (IOException e)
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
