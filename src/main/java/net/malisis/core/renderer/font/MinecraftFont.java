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

import net.malisis.core.MalisisCore;
import net.malisis.core.asm.AsmUtils;
import net.malisis.core.renderer.MalisisRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

import org.apache.commons.lang3.StringUtils;

import cpw.mods.fml.client.FMLClientHandler;

/**
 * @author Ordinastie
 *
 */
public class MinecraftFont extends MalisisFont
{
	private int[] mcCharWidth;
	private float[] optifineCharWidth;
	private byte[] glyphWidth;
	private ResourceLocation[] unicodePages;
	private ResourceLocation lastFontTexture;
	private FontRenderer fontRenderer;
	private MCCharData mcCharData = new MCCharData();
	private UnicodeCharData unicodeCharData = new UnicodeCharData();
	private MalisisRenderer renderer;

	public MinecraftFont()
	{
		super((Font) null);

		this.options = new FontGeneratorOptions();
		this.options.fontSize = 9F;
		this.textureRl = new ResourceLocation("textures/font/ascii.png");
		this.size = 256;

		fontRenderer = Minecraft.getMinecraft().fontRendererObj;
		setFields();
	}

	private void setFields()
	{
		String srg = "field_78286_d";
		//		if (FMLClientHandler.instance().hasOptifine())
		//			srg = "d";

		Field charWidthField = AsmUtils.changeFieldAccess(FontRenderer.class, "charWidth", srg);
		Field glyphWidthField = AsmUtils.changeFieldAccess(FontRenderer.class, "glyphWidth", "field_78287_e");
		Field unicodePagesField = AsmUtils.changeFieldAccess(FontRenderer.class, "unicodePageLocations", "field_111274_c");

		try
		{
			if (charWidthField == null)
				throw new IllegalStateException("charWidthField (" + srg + ") is null");
			if (fontRenderer == null)
				throw new IllegalStateException("fontRenderer not initialized");

			if (FMLClientHandler.instance().hasOptifine())
				optifineCharWidth = (float[]) charWidthField.get(fontRenderer);
			else
				mcCharWidth = (int[]) charWidthField.get(fontRenderer);
			glyphWidth = (byte[]) glyphWidthField.get(fontRenderer);
			unicodePages = (ResourceLocation[]) unicodePagesField.get(fontRenderer);

		}
		catch (IllegalStateException | IllegalArgumentException | IllegalAccessException e)
		{
			MalisisCore.log.error("[MinecraftFont] Failed to gets the FontRenderer fields :", e);
		}
	}

	private void bindFontTexture(CharData data)
	{
		ResourceLocation rl = textureRl;
		if (data instanceof UnicodeCharData)
		{
			int i = data.c / 256;
			if (unicodePages[i] == null)
				unicodePages[i] = new ResourceLocation(String.format("textures/font/unicode_page_%02x.png", i));
			rl = unicodePages[i];
		}
		if (rl != lastFontTexture)
		{
			renderer.next();
			Minecraft.getMinecraft().getTextureManager().bindTexture(rl);
			lastFontTexture = rl;
		}
	}

	@Override
	protected void prepare(MalisisRenderer renderer, float x, float y, float z, FontRenderOptions fro)
	{
		super.prepare(renderer, x, y, z, fro);
		this.renderer = renderer;
	}

	@Override
	protected void clean(MalisisRenderer renderer, boolean isDrawing)
	{
		super.clean(renderer, isDrawing);
		lastFontTexture = null;
	}

	@Override
	public CharData getCharData(char c)
	{
		if (c < 0 || c >= 256 || fontRenderer.getUnicodeFlag())
			return unicodeCharData.set(c);
		else
			return mcCharData.set(c);
	}

	@Override
	protected void drawChar(CharData cd, float offsetX, float offsetY, FontRenderOptions fro)
	{
		bindFontTexture(cd);
		if (drawingShadow && cd instanceof UnicodeCharData)
		{
			offsetX -= fro.fontScale / 2;
			offsetY -= fro.fontScale / 2;
		}

		super.drawChar(cd, offsetX, offsetY, fro);
	}

	@Override
	protected void drawLineChar(CharData cd, float offsetX, float offsetY, FontRenderOptions fro)
	{
		Tessellator t = Tessellator.instance;
		float factor = fro.fontScale / options.fontSize * 9;
		float w = cd.getFullWidth(options) * factor;
		float h = cd.getFullHeight(options) / 9F * factor;

		offsetY -= factor + h;
		w += 1.01F * factor;

		t.setColorOpaque_I(drawingShadow ? fro.getShadowColor() : fro.color);
		t.addVertex(offsetX, offsetY, 0);
		t.addVertex(offsetX, offsetY + h, 0);
		t.addVertex(offsetX + w, offsetY + h, 0);
		t.addVertex(offsetX + w, offsetY, 0);
	}

	@Override
	public float getStringWidth(String str, FontRenderOptions fro, int start, int end)
	{
		if (StringUtils.isEmpty(str))
			return 0;

		str = processString(str, null);
		float width = 0;
		StringWalker walker = new StringWalker(str, this, fro);
		walker.startIndex(start);
		walker.endIndex(end);
		while (walker.walk())
			width += walker.getWidth();

		return width;// * (fro != null ? fro.fontScale : 1);
	}

	@Override
	public float getStringHeight(FontRenderOptions fro)
	{
		return fontRenderer.FONT_HEIGHT * (fro != null ? fro.fontScale : 1);
	}

	public class MCCharData extends CharData
	{
		int pos;

		public MCCharData()
		{
			super('-', 0, 0, 0);
		}

		public CharData set(char c)
		{
			String ref = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&\'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000";
			if (c == '\u00a7')
				c = '&';
			this.c = c;
			this.pos = ref.indexOf(c);
			return this;
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
			if (c == ' ' || c < 0 || c >= 256 || pos == -1)
				return 4.0F;
			else if (FMLClientHandler.instance().hasOptifine())
				return optifineCharWidth[c];
			else
				return mcCharWidth[pos];
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
	}

	public class UnicodeCharData extends CharData
	{
		float pad;

		public UnicodeCharData()
		{
			super('-', 0, 0, 0);
		}

		public CharData set(char c)
		{
			this.c = c;
			this.width = glyphWidth[c] & 15;
			this.pad = glyphWidth[c] >>> 4;

			return this;
		}

		@Override
		public float u()
		{
			float col = c % 16 * 16 + pad;
			return col / 256F;
		}

		@Override
		public float v()
		{
			float row = (c & 255) / 16 * 16;
			return row / 256F;
		}

		@Override
		public float U()
		{
			float col = (c % 16 * 16 + pad) + (width + 1 - pad - 0.02F);
			return col / 256F;
		}

		@Override
		public float V()
		{
			float row = (c & 255) / 16 * 16;
			return (row + 15.98F) / 256F;
		}

		@Override
		public float getCharWidth()
		{
			if (width == 0 && pad == 0)
				return 0;

			if (c == ' ')
				return 4;

			if (width > 7)
			{
				width = 15;
				pad = 0;
			}

			return (int) (width + 1 - pad) / 2 + 1;
		}

		@Override
		public float getCharHeight()
		{
			return fontRenderer.FONT_HEIGHT;
		}

		@Override
		public float getFullWidth(FontGeneratorOptions options)
		{
			return (width + 1 - pad - 0.02F) / 2F;
		}

		@Override
		public float getFullHeight(FontGeneratorOptions options)
		{
			return (getCharHeight() - 1.01F) /* * options.fontSize*/;
		}
	}
}
