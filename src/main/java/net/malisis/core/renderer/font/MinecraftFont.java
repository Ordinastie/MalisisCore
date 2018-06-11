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
import java.util.Map;

import com.google.common.collect.Maps;

import net.malisis.core.MalisisCore;
import net.malisis.core.asm.AsmUtils;
import net.malisis.core.client.gui.render.GuiRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;

/**
 * @author Ordinastie
 *
 */
public class MinecraftFont extends MalisisFont
{
	private int[] mcCharWidth;
	//private float[] optifineCharWidth;
	private byte[] glyphWidth;
	private ResourceLocation[] unicodePages;
	private ResourceLocation lastFontTexture;
	private FontRenderer fontRenderer;
	/** CharData for Unicode characters */
	protected Map<Character, CharData> unicodeCharData = Maps.newHashMap();
	/** Whether the character should drawn with unicode font even if unicode is disabled in MC options. */
	protected boolean forceUnicode = false;
	private GuiRenderer renderer;

	public MinecraftFont()
	{
		super((Font) null);

		this.fontGeneratorOptions = new FontGeneratorOptions();
		this.fontGeneratorOptions.fontSize = 9F;
		this.textureRl = new ResourceLocation("textures/font/ascii.png");
		this.size = 256;

		fontRenderer = Minecraft.getMinecraft().fontRenderer;
		setFields();
		fillCharData();
	}

	private void setFields()
	{
		String srg = "field_78286_d";
		Field charWidthField = AsmUtils.changeFieldAccess(FontRenderer.class, "charWidth", srg, true);
		if (charWidthField == null && FMLClientHandler.instance().hasOptifine())
		{
			srg = "d";
			charWidthField = AsmUtils.changeFieldAccess(FontRenderer.class, "charWidth", srg, true);
		}
		Field glyphWidthField = AsmUtils.changeFieldAccess(FontRenderer.class, "glyphWidth", "field_78287_e");
		Field unicodePagesField = AsmUtils.changeFieldAccess(FontRenderer.class, "UNICODE_PAGE_LOCATIONS", "field_111274_c");

		try
		{
			if (charWidthField == null)
				throw new IllegalStateException("charWidthField (" + srg + ") is null");
			if (fontRenderer == null)
				throw new IllegalStateException("fontRenderer not initialized");

			//https://github.com/sp614x/optifine/issues/438 : changed field back to int[]
			//			if (FMLClientHandler.instance().hasOptifine())
			//				optifineCharWidth = (float[]) charWidthField.get(fontRenderer);
			//			else
			mcCharWidth = (int[]) charWidthField.get(fontRenderer);
			glyphWidth = (byte[]) glyphWidthField.get(fontRenderer);
			unicodePages = (ResourceLocation[]) unicodePagesField.get(fontRenderer);

		}
		catch (IllegalStateException | IllegalArgumentException | IllegalAccessException e)
		{
			MalisisCore.log.error("[MinecraftFont] Failed to gets the FontRenderer fields :", e);
		}
	}

	private void fillCharData()
	{
		for (char c = 0; c < 256; c++)
		{
			charData[c] = new MCCharData(c);
			unicodeCharData.put(c, new UnicodeCharData(c));
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
	protected void prepare(GuiRenderer renderer, float x, float y, float z, FontOptions options)
	{
		super.prepare(renderer, x, y, z, options);
		this.renderer = renderer;
	}

	@Override
	protected void clean(GuiRenderer renderer, boolean isDrawing)
	{
		super.clean(renderer, isDrawing);
		lastFontTexture = null;
	}

	@Override
	public CharData getCharData(char c)
	{
		if (c < 0 || c >= 256 || fontRenderer.getUnicodeFlag() || forceUnicode)
		{
			if (!unicodeCharData.containsKey(c))
				unicodeCharData.put(c, new UnicodeCharData(c));
			return unicodeCharData.get(c);
		}

		return super.getCharData(c);
	}

	@Override
	protected void drawChar(CharData cd, float offsetX, float offsetY, FontOptions options, int color)
	{
		bindFontTexture(cd);
		if (drawingShadow && cd instanceof UnicodeCharData)
		{
			offsetX -= options.getFontScale() / 2;
			offsetY -= options.getFontScale() / 2;
		}

		super.drawChar(cd, offsetX, offsetY, options, color);
	}

	@Override
	public float getStringHeight(FontOptions options)
	{
		return fontRenderer.FONT_HEIGHT * (options != null ? options.getFontScale() : 1);
	}

	public class MCCharData extends CharData
	{
		/** Index in the CHARLIST string. */
		int pos;

		public MCCharData(char c)
		{
			super(c == '\u00a7' ? '&' : c, 0, 0, 0);
			this.pos = CHARLIST.indexOf(c);
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
			//			else if (FMLClientHandler.instance().hasOptifine())
			//				return optifineCharWidth[c];
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

		public UnicodeCharData(char c)
		{
			super(c, 0, glyphWidth[c] & 15, fontRenderer.FONT_HEIGHT);
			this.pad = glyphWidth[c] >>> 4;
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
