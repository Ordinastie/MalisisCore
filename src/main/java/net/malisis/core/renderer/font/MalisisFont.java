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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import com.google.common.io.Files;

import net.malisis.core.MalisisCore;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.renderer.MalisisRenderer;
import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.element.face.SouthFace;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

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
	protected FontGeneratorOptions fontGeneratorOptions = FontGeneratorOptions.DEFAULT;
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
			this.fontGeneratorOptions = options;

		loadCharacterData();
		loadTexture(false);
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
	protected void prepare(MalisisRenderer<?> renderer, float x, float y, float z, FontOptions options)
	{
		boolean isGui = renderer instanceof GuiRenderer;
		renderer.next(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

		Minecraft.getMinecraft().getTextureManager().bindTexture(textureRl);
		GL11.glPushMatrix();
		GL11.glTranslatef(x, y + (isGui ? 0 : options.getFontScale()), z);

		if (!isGui)
			GL11.glScalef(1 / 9F, -1 / 9F, 1 / 9F);
	}

	protected void clean(MalisisRenderer<?> renderer, boolean isDrawing)
	{
		if (isDrawing)
			renderer.next(MalisisRenderer.malisisVertexFormat);
		else
			renderer.draw();
		if (renderer instanceof GuiRenderer)
			Minecraft.getMinecraft().getTextureManager().bindTexture(((GuiRenderer) renderer).getDefaultTexture().getResourceLocation());
		GL11.glPopMatrix();
	}

	protected void prepareShadow(MalisisRenderer<?> renderer)
	{
		drawingShadow = true;
		if (renderer instanceof GuiRenderer)
			return;
		renderer.next();
		GL11.glPolygonOffset(3.0F, 3.0F);
		GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
	}

	protected void cleanShadow(MalisisRenderer<?> renderer)
	{
		drawingShadow = false;
		if (renderer instanceof GuiRenderer)
			return;
		renderer.next();
		GL11.glPolygonOffset(0.0F, 0.0F);
		GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
	}

	protected void prepareLines(MalisisRenderer<?> renderer, FontOptions options)
	{
		renderer.next(DefaultVertexFormats.POSITION_COLOR);
		renderer.disableTextures();
	}

	protected void cleanLines(MalisisRenderer<?> renderer)
	{
		renderer.next();
		renderer.enableTextures();
	}

	//#end Prepare/Clean

	public void render(MalisisRenderer<?> renderer, String text, float x, float y, float z, FontOptions options)
	{
		if (StringUtils.isEmpty(text))
			return;
		if (font == null && this != minecraftFont)
		{
			minecraftFont.render(renderer, text, x, y, z, options);
			return;
		}

		boolean isDrawing = renderer.isDrawing();

		prepare(renderer, x, y, z, options);

		text = processString(text, options);

		if (options.hasShadow())
		{
			prepareShadow(renderer);
			drawString(text, options);
			cleanShadow(renderer);
		}
		drawString(text, options);

		if (hasLines(text, options))
		{
			prepareLines(renderer, options);
			if (options.hasShadow())
			{
				prepareShadow(renderer);
				drawLines(text, options);
				cleanShadow(renderer);
			}
			drawLines(text, options);
			cleanLines(renderer);
		}

		clean(renderer, isDrawing);
	}

	protected void drawString(String text, FontOptions options)
	{
		float x = 0;

		options.resetLineOptions();
		StringWalker walker = new StringWalker(text, this, options);
		walker.applyStyles(true);
		while (walker.walk())
		{
			CharData cd = getCharData(walker.getChar());
			drawChar(cd, x, 0, options);
			if (options.isBold())
				drawChar(cd, x + options.getFontScale(), 0, options);
			x += walker.getWidth();
		}
	}

	protected void drawChar(CharData cd, float offsetX, float offsetY, FontOptions options)
	{
		if (Character.isWhitespace(cd.getChar()))
			return;

		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		float factor = options.getFontScale() / fontGeneratorOptions.fontSize * 9;
		float w = cd.getFullWidth(fontGeneratorOptions) * factor;
		float h = cd.getFullHeight(fontGeneratorOptions) * factor;
		float i = options.isItalic() ? options.getFontScale() : 0;
		int color = drawingShadow ? options.getShadowColor() : options.getColor();

		if (drawingShadow)
		{
			offsetX += options.getFontScale();
			offsetY += options.getFontScale();
		}

		buffer.pos(offsetX + i, offsetY, 0);
		buffer.tex(cd.u(), cd.v());
		buffer.color((color >> 16) & 255, (color >> 8) & 255, color & 255, 255);
		buffer.endVertex();

		buffer.pos(offsetX - i, offsetY + h, 0);
		buffer.tex(cd.u(), cd.V());
		buffer.color((color >> 16) & 255, (color >> 8) & 255, color & 255, 255);
		buffer.endVertex();

		buffer.pos(offsetX + w - i, offsetY + h, 0);
		buffer.tex(cd.U(), cd.V());
		buffer.color((color >> 16) & 255, (color >> 8) & 255, color & 255, 255);
		buffer.endVertex();

		buffer.pos(offsetX + w + i, offsetY, 0);
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

	protected void drawLines(String text, FontOptions options)
	{
		float x = 0;
		options.resetLineOptions();

		StringWalker walker = new StringWalker(text, this, options);
		walker.applyStyles(true);
		while (walker.walk())
		{
			if (!walker.isFormatted())
			{
				CharData cd = getCharData(walker.getChar());
				if (options.isUnderline())
					drawLineChar(cd, x, getStringHeight(options) + options.getFontScale(), options);
				if (options.isStrikethrough())
					drawLineChar(cd, x, getStringHeight(options) * 0.5F + options.getFontScale(), options);

				x += walker.getWidth();
			}
		}
	}

	protected void drawLineChar(CharData cd, float offsetX, float offsetY, FontOptions options)
	{
		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		float factor = options.getFontScale() / fontGeneratorOptions.fontSize * 9;
		float w = cd.getFullWidth(fontGeneratorOptions) * factor;
		float h = cd.getFullHeight(fontGeneratorOptions) / 9F * factor;
		int color = drawingShadow ? options.getShadowColor() : options.getColor();

		if (drawingShadow)
		{
			offsetX += options.getFontScale();
			offsetY += options.getFontScale();
		}

		buffer.pos(offsetX, offsetY, 0);
		buffer.color((color >> 16) & 255, (color >> 8) & 255, color & 255, 255);
		buffer.endVertex();

		buffer.pos(offsetX, offsetY + h, 0);
		buffer.color((color >> 16) & 255, (color >> 8) & 255, color & 255, 255);
		buffer.endVertex();

		buffer.pos(offsetX + w, offsetY + h, 0);
		buffer.color((color >> 16) & 255, (color >> 8) & 255, color & 255, 255);
		buffer.endVertex();

		buffer.pos(offsetX + w, offsetY, 0);
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
	/**
	 * Processes the passed string by translating it and replacing spacing characters and new lines.<br>
	 * Keeps the formatting if passed at the beginning of the translation key.
	 *
	 * @param str the str
	 * @return the string
	 */
	public String processString(String str, FontOptions options)
	{
		str = str.replaceAll("\r?\n", "");
		if (!options.shouldTranslate())
			return str;
		Pair<String, String> p = FontOptions.getStartFormat(str);
		return p.getLeft() + translate(p.getRight());
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

	private boolean hasLines(String text, FontOptions options)
	{
		return options.isUnderline() || options.isStrikethrough() || text.contains(TextFormatting.UNDERLINE.toString())
				|| text.contains(TextFormatting.STRIKETHROUGH.toString());
	}

	/**
	 * Clips a string to fit in the specified width.
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
		if (font == null) //couldn't load the font, use vanilla font size
			return minecraftFont.getStringWidth(str, options, start, end);

		if (start > end)
			return 0;

		if (options != null && !options.isFormattingDisabled())
			str = TextFormatting.getTextWithoutFormattingCodes(str);

		if (StringUtils.isEmpty(str))
			return 0;

		str = processString(str, options);
		return (float) font.getStringBounds(str, frc).getWidth() / fontGeneratorOptions.fontSize
				* (options != null ? options.getFontScale() : 1) * 9;
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

		StringWalker walker = new StringWalker(str, this, options);
		walker.startIndex(charOffset);
		walker.skipChars(true);
		return walker.walkToCoord(position);
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
	 * @param options the options
	 * @return list of lines that won't exceed maxWidth limit
	 */
	public List<String> wrapText(String str, int maxWidth, FontOptions options)
	{
		List<String> lines = new ArrayList<>();
		String[] texts = str.split("\r?(?<=\n)");
		if (texts.length > 1)
		{
			for (String t : texts)
				lines.addAll(wrapText(t, maxWidth, options));
			return lines;
		}

		StringBuilder line = new StringBuilder();
		StringBuilder word = new StringBuilder();
		//FontRenderOptions fro = new FontRenderOptions();

		maxWidth -= 4;
		//maxWidth /= (fro != null ? fro.fontScale : 1); //factor the position instead of the char widths
		float lineWidth = 0;
		float wordWidth = 0;

		str = processString(str, options);
		texts = str.split("\\\\r?\\\\n");
		if (texts.length > 1)
		{
			for (String t : texts)
				lines.addAll(wrapText(t, maxWidth, options));
			return lines;
		}

		StringWalker walker = new StringWalker(str, this, options);
		walker.skipChars(false);
		walker.applyStyles(true);
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

	protected void loadTexture(boolean forceGenerate)
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
