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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.imageio.ImageIO;

import net.malisis.core.MalisisCore;

import org.apache.commons.io.FileUtils;

/**
 * @author Ordinastie
 *
 */
public class FontGenerator
{
	static
	{
		File f = new File("fonts/");
		if (!f.exists())
			f.mkdir();
	}

	private Font font;
	private CharData[] charData;
	private FontGeneratorOptions options;

	public FontGenerator(Font font, CharData[] charData, FontGeneratorOptions options)
	{
		this.font = font;
		this.charData = charData;
		this.options = options;
	}

	public BufferedImage generate(int size, File textureFile, File uvFile)
	{
		BufferedImage img = generateTexture(size, textureFile);
		generateUVs(size, uvFile);
		return img;
	}

	private BufferedImage generateTexture(int size, File textureFile)
	{
		BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = initGraphics(img);

		int x = 10;
		int y = 10;
		for (char c = 35; c < 256; c++)
		{
			CharData cd = charData[c];
			if (x + cd.getFullWidth(options) > size)
			{
				x = (int) options.mx;
				y += options.fontSize + options.my + options.py + 15;
			}
			g.drawString("" + cd.getChar(), x, y + cd.getAscent());

			cd.setUVs(x, y, size, options);

			if (options.debug)
			{
				//baseLine
				g.setColor(Color.RED);
				g.drawLine(x, (int) (y + cd.getAscent()), (int) (x + cd.getFullWidth(options)), (int) (y + cd.getAscent()));

				g.setColor(Color.MAGENTA);
				g.drawRect(x, y, (int) (cd.getCharWidth()), (int) (cd.getCharHeight()));

				g.setColor(Color.BLACK);
			}

			x += cd.getFullWidth(options) + 15;
		}

		try
		{
			ImageIO.write(img, "png", textureFile);
			MalisisCore.log.info(textureFile.getName() + " (" + size + "x" + size + ") written.");
		}
		catch (IOException e)
		{
			MalisisCore.log.error("Failed to create font texture file for {}.", font.getName(), e);
		}

		return img;
	}

	private void generateUVs(int size, File uvFile)
	{
		try
		{
			StringBuilder sb = new StringBuilder();
			for (CharData cd : charData)
			{
				if (cd.getChar() < 35 || cd.getChar() == ';')
					sb.append((int) cd.getChar());
				else
					sb.append(cd.getChar());
				sb.append(";");
				sb.append(cd.u());
				sb.append(";");
				sb.append(cd.v());
				sb.append(";");
				sb.append(cd.U());
				sb.append(";");
				sb.append(cd.V());
				sb.append(";\n");
			}

			FileUtils.write(uvFile, sb, StandardCharsets.UTF_8);
		}
		catch (IOException e)
		{
			MalisisCore.log.error("Failed to create UV file for {}.", font.getName(), e);
		}
	}

	/**
	 * Initializes the {@link Graphics2D} object. Sets the font and the color for the graphics.<br>
	 * If debug mode, will also draw a grid on the image.
	 *
	 * @param img the img
	 * @return the graphics2 d
	 */
	private Graphics2D initGraphics(BufferedImage img)
	{
		Graphics2D g = img.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		if (options.antialias)
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g.setFont(font);

		if (options.debug)
		{
			int color = 0x666666;
			for (int i = 0; i < img.getWidth(); i += 25)
			{
				if (i % 500 == 0)
					color = 0x333333;
				else if (i % 100 == 0)
					color = 0x777777;
				else
					color = 0xBBBBBB;
				g.setColor(new Color(color));
				g.drawLine(i, 0, i, img.getWidth());
				g.drawLine(0, i, img.getHeight(), i);
			}
		}

		g.setColor(options.debug ? Color.BLACK : Color.WHITE);

		return g;
	}
}
