/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Ordinastie
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

package net.malisis.core.client.gui.text;

import static com.google.common.base.Preconditions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.renderer.font.MalisisFont;
import net.malisis.core.renderer.font.StringWalker;
import net.malisis.core.util.cacheddata.CachedData;
import net.malisis.core.util.cacheddata.FixedData;
import net.malisis.core.util.cacheddata.ICachedData;
import net.minecraft.client.resources.I18n;

/**
 * The Class GuiString represents a String to be used and displayed in a GUI.<br>
 * It can be translated, and used with fixed or dynamic named parameters.<br>
 * {@link GuiString#setWrapSize(int)} will automatically split the text in lines, then the rendering can be done for only some of those
 * lines.
 *
 * @author Ordinastie
 */
public class GuiString
{
	/** Pattern for named parameters. */
	private static final Pattern pattern = Pattern.compile("\\{(?<key>.*?)\\}");

	/** Base text to be translated and parameterized. */
	private final String base;
	/** Lines composing the text. */
	private final List<String> lines = Lists.newArrayList();
	/** Parameters. */
	private final Map<String, ICachedData<?>> parameters = Maps.newHashMap();

	/** The font to use to render. */
	private MalisisFont font = MalisisFont.minecraftFont;
	/** The base font options to use to render. */
	private FontOptions fontOptions = FontOptions.EMPTY;
	/** Translated text with resolved parameters. */
	private String cache = null;
	/** Whether the text should be translated. */
	private boolean shouldTranslate = true;
	/** Text wrap size. 0 means do not wrap. */
	private int wrapSize = 0;

	public GuiString(String base)
	{
		this.base = base;
		generateCache();
	}

	/**
	 * Sets whether the text should be translated.
	 *
	 * @param translate the translate
	 */
	public void shouldTranslate(boolean translate)
	{
		this.shouldTranslate = translate;
		generateCache();
	}

	/**
	 * Sets the wrap size.
	 *
	 * @param wrapSize the new wrap size
	 */
	public void setWrapSize(int wrapSize)
	{
		this.wrapSize = wrapSize;
		buildLines();
	}

	/**
	 * Sets the font to use to render.
	 *
	 * @param font the new font
	 */
	public void setFont(MalisisFont font)
	{
		this.font = checkNotNull(font);
		buildLines();
	}

	/**
	 * Sets the font options to use to render.
	 *
	 * @param fontOptions the new font options
	 */
	public void setFontOptions(FontOptions fontOptions)
	{
		boolean rebuild = this.fontOptions.isBold() != fontOptions.isBold()
				|| this.fontOptions.getFontScale() != fontOptions.getFontScale();
		this.fontOptions = fontOptions;
		if (rebuild)
			buildLines();
	}

	/**
	 * Binds a fixed value to the specified parameter.
	 *
	 * @param <T> the generic type
	 * @param key the key
	 * @param value the value
	 */
	public <T> void bind(String key, T value)
	{
		bind(key, new FixedData<>(value));
	}

	/**
	 * Binds supplier to the specified parameter.
	 *
	 * @param <T> the generic type
	 * @param key the key
	 * @param supplier the supplier
	 */
	public <T> void bind(String key, Supplier<T> supplier)
	{
		bind(key, new CachedData<>(supplier));
	}

	/**
	 * Binds {@link ICachedData} to the specified parameter.
	 *
	 * @param <T> the generic type
	 * @param key the key
	 * @param data the data
	 */
	public <T> void bind(String key, ICachedData<T> data)
	{
		parameters.put(key, data);
		generateCache();
	}

	/**
	 * Resolve parameter values to use in the text.
	 *
	 * @param key the key
	 * @return the string
	 */
	private String resolveParameter(String key)
	{
		ICachedData<?> o = parameters.get(key);
		return o != null && o.get() != null ? o.get().toString() : "";
	}

	/**
	 * Checks whether any parameter has changed.
	 *
	 * @return true, if successful
	 */
	private boolean hasParametersChanged()
	{
		for (ICachedData<?> data : parameters.values())
		{
			data.update();
			if (data.hasChanged())
				return true;
		}
		return false;
	}

	/**
	 * Updates cache and rebuild lines if necessary.
	 *
	 * @return the gui string
	 */
	public GuiString updateCache()
	{
		if (hasParametersChanged())
			generateCache();
		return this;
	}

	/**
	 * Generates the text cache and rebuild the lines.<br>
	 * Translates and applies the parameters.
	 */
	private void generateCache()
	{
		String str = base;
		if (shouldTranslate)
			str = I18n.format(str);
		str = applyParameters(str);
		cache = str;
		buildLines();

	}

	/**
	 * Applies parameters to the text.
	 *
	 * @param str the str
	 * @return the string
	 */
	public String applyParameters(String str)
	{
		Matcher matcher = pattern.matcher(str);
		StringBuffer sb = new StringBuffer();
		while (matcher.find())
			matcher.appendReplacement(sb, resolveParameter(matcher.group("key")));
		matcher.appendTail(sb);
		return sb.toString();
	}

	/**
	 * Splits the text into lines.<br>
	 * Wraps the text if necessary.
	 */
	private void buildLines()
	{
		lines.clear();
		lines.addAll(wrapText(cache));
	}

	/**
	 * Splits and wraps the text.
	 *
	 * @param text the text
	 * @return the list
	 */
	private List<String> wrapText(String text)
	{
		List<String> lines = new ArrayList<>();
		String[] texts = text.split("\r?(?<=\n)");
		if (texts.length > 1)
		{
			for (String t : texts)
				lines.addAll(wrapText(t));
			return lines;
		}

		if (wrapSize <= 0)
		{
			lines.add(text);
			return lines;
		}

		StringBuilder line = new StringBuilder();
		StringBuilder word = new StringBuilder();
		//FontRenderOptions fro = new FontRenderOptions();

		int maxWidth = wrapSize - 4; //keep some room
		//maxWidth /= (fro != null ? fro.fontScale : 1); //factor the position instead of the char widths
		float lineWidth = 0;
		float wordWidth = 0;

		StringWalker walker = new StringWalker(text, font, fontOptions);
		walker.skipFormattingChars(false);
		walker.applyStyles(true);
		while (walker.walk())
		{
			char c = walker.getChar();
			lineWidth += walker.getWidth();
			wordWidth += walker.getWidth();
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

	/**
	 * Renders this {@link GuiString} at the specified coordinates.
	 *
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 */
	public void render(int x, int y, int z)
	{
		MalisisFont font = this.font.isLoaded() ? this.font : MalisisFont.minecraftFont;
		font.render(lines, x, y, z, fontOptions);
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		lines.forEach(str -> {
			sb.append(str);
			sb.append("\n");
		});
		return sb.toString();

	}
}
