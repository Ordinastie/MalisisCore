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
import java.lang.reflect.Field;

import net.malisis.core.asm.AsmUtils;
import net.malisis.core.renderer.icon.MalisisIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import org.apache.commons.lang3.StringUtils;

import cpw.mods.fml.client.FMLClientHandler;

/**
 * @author Ordinastie
 *
 */
public class MinecraftFont extends MalisisFont
{
	private Field charWidthField;
	private FontRenderer fontRenderer;

	public MinecraftFont()
	{
		super((Font) null);

		this.options = new FontGeneratorOptions();
		this.options.fontSize = 9F;
		this.textureRl = new ResourceLocation("textures/font/ascii.png");
		this.size = 256;

		fontRenderer = Minecraft.getMinecraft().fontRendererObj;
		setField();
		loadCharacterData();
	}

	private void setField()
	{
		String srg = "field_78286_d";
		if (FMLClientHandler.instance().hasOptifine())
			srg = "d";

		charWidthField = AsmUtils.changeAccess(FontRenderer.class, "charWidth", srg);
	}

	protected float getWidth(char c)
	{
		try
		{
			if (c >= 0 && c < 256)
			{
				if (FMLClientHandler.instance().hasOptifine())
					return ((float[]) charWidthField.get(fontRenderer))[c];
				else
					return ((int[]) charWidthField.get(fontRenderer))[c];
			}
		}
		catch (IllegalArgumentException | IllegalAccessException e)
		{
			e.printStackTrace();
		}

		return 1;
	}

	@Override
	protected void loadCharacterData()
	{
		String ref = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&\'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000";
		for (char c = 0; c < 256; c++)
		{
			int pos = ref.indexOf(c);
			charData[c] = new MCCharData(c, pos);
		}
	}

	@Override
	protected void drawLineChar(CharData cd, float offsetX, float offsetY, float factor)
	{
		Tessellator t = Tessellator.instance;
		float w = cd.getFullWidth(options) * factor;
		float h = cd.getFullHeight(options) / 9F * factor;

		offsetY -= factor + h;
		w += 1.01F * factor;

		t.addVertex(offsetX, offsetY, 0);
		t.addVertex(offsetX, offsetY + h, 0);
		t.addVertex(offsetX + w, offsetY + h, 0);
		t.addVertex(offsetX + w, offsetY, 0);

	}

	@Override
	public float getStringWidth(String str, float fontScale)
	{
		if (StringUtils.isEmpty(str))
			return 0;

		str = processString(str, null);
		return fontRenderer.getStringWidth(str) * fontScale;
	}

	@Override
	public float getStringHeight(float fontScale)
	{
		return fontRenderer.FONT_HEIGHT * fontScale;
	}

	public class MCCharData extends CharData
	{
		int pos;

		public MCCharData(char c, int pos)
		{
			super(c, 0, 0, 0);
			this.pos = pos;
		}

		@Override
		public float u()
		{
			float col = pos % 16 * 8;
			return col / 128F;
		}

		@Override
		public float v()
		{
			float row = pos / 16 * 8;
			return row / 128F;
		}

		@Override
		public float U()
		{
			float col = pos % 16 * 8;
			return (col + getCharWidth() - 1.01F) / 128F;
		}

		@Override
		public float V()
		{
			float row = pos / 16 * 8;
			return (row + getCharHeight() - 1.01F) / 128F;
		}

		@Override
		public float getCharWidth()
		{
			return c == ' ' ? 4.0F : getWidth((char) pos);
		}

		@Override
		public float getCharHeight()
		{
			return fontRenderer.FONT_HEIGHT;
		}

		@Override
		public float getFullWidth(FontGeneratorOptions options)
		{
			return (getCharWidth() - 1.01F) /* * options.fontSize*/;
		}

		@Override
		public float getFullHeight(FontGeneratorOptions options)
		{
			return (getCharHeight() - 1.01F) /* * options.fontSize*/;
		}

		@Override
		public IIcon getIcon()
		{
			return new MalisisIcon("" + getChar(), u(), v(), U(), V());
		}
	}
}
