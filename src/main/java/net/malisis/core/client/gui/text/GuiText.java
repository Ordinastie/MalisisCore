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

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.component.element.Size.DynamicSize;
import net.malisis.core.client.gui.component.element.Size.ISize;
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
 * Parameters are used with {key} markers in the text.<br>
 * {@link GuiText#setWrapSize(int)} will automatically split the text in lines, then the rendering can be done for only some of those lines.
 *
 * @author Ordinastie
 */
public class GuiText
{
	/** Pattern for named parameters. */
	private static final Pattern pattern = Pattern.compile("\\{(?<key>.*?)\\}");

	/** Base text to be translated and parameterized. */
	private String base = "";
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
	private boolean translated = true;
	/** Text wrap size. 0 means do not wrap. */
	private int wrapSize = 0;
	/** Space between each line. */
	private int lineSpacing = 2;
	/** Whether this text is multiline. */
	private boolean multiLine = false;

	private ISize size = new DynamicSize(o -> getMaxWidth(), o -> getLineHeight() * lines().size());

	private boolean buildLines = true;
	private boolean buildCache = true;

	/**
	 * Instantiates a new {@link GuiText}.
	 */
	public GuiText()
	{

	}

	/**
	 * Instantiates a new {@link GuiText}.
	 *
	 * @param text the text
	 */
	public GuiText(String text)
	{
		setText(text);
	}

	/**
	 * Sets the text for this {@link GuiText}.<br>
	 * Generates the cache and resolves parameters.
	 *
	 * @param text the new text
	 */
	public void setText(String text)
	{
		this.base = text != null ? text : "";
		buildCache = true;
	}

	/**
	 * Gets the raw text of this {@link GuiText}.
	 *
	 * @return the raw text
	 */
	public String getRawText()
	{
		return base;
	}

	/**
	 * Gets processed text, translated and with resolved parameters.
	 *
	 * @return the text
	 */
	public String getText()
	{
		update();
		return cache;
	}

	/**
	 * Gets the different lines.
	 *
	 * @return the list
	 */
	public List<String> lines()
	{
		update();
		return lines;
	}

	/**
	 * Sets whether the text should be translated.
	 *
	 * @param translate the translate
	 */
	public void setTranslated(boolean translate)
	{
		if (translated == translate) //no change, no rebuild
			return;
		translated = translate;
		buildCache = true;
	}

	/**
	 * Checks if the text should be translated.
	 *
	 * @return true, if is translated
	 */
	public boolean isTranslated()
	{
		return translated;
	}

	/**
	 * Sets the wrap size for the text.<br>
	 * Has no effect if this {@link GuiText} is not multilines.
	 *
	 * @param wrapSize the new wrap size
	 */
	public void setWrapSize(int wrapSize)
	{
		if (this.wrapSize == wrapSize)//no change, no rebuild
			return;
		if (!multiLine)
			wrapSize = 0;
		this.wrapSize = wrapSize;
		buildLines = true;
	}

	/**
	 * Gets the wrap size for the text.
	 *
	 * @return the wrap size
	 */
	public int getWrapSize()
	{
		return wrapSize;
	}

	/**
	 * Sets the line spacing for the text.
	 *
	 * @param spacing the new line spacing
	 */
	public void setLineSpacing(int spacing)
	{
		lineSpacing = spacing;
	}

	/**
	 * Gets the line spacing for the text.
	 *
	 * @return the line spacing
	 */
	public int getLineSpacing()
	{
		return lineSpacing;
	}

	/**
	 * Sets the font to use to render.
	 *
	 * @param font the new font
	 */
	public void setFont(MalisisFont font)
	{
		if (this.font == font) //no changes, no rebuild
			return;
		this.font = checkNotNull(font);
		buildLines = true;
	}

	/**
	 * Gets the font used to render the text.
	 *
	 * @return the font
	 */
	public MalisisFont getFont()
	{
		return font;
	}

	/**
	 * Sets the font options to use to render.
	 *
	 * @param fontOptions the new font options
	 */
	public void setFontOptions(FontOptions fontOptions)
	{
		this.fontOptions = checkNotNull(fontOptions);
		buildLines = this.fontOptions.isBold() != fontOptions.isBold() || this.fontOptions.getFontScale() != fontOptions.getFontScale();
	}

	/**
	 * Gets the font options used to render.
	 *
	 * @return the font options
	 */
	public FontOptions getFontOptions()
	{
		return fontOptions;
	}

	/**
	 * Sets whether the text is multiline.
	 *
	 * @param multiLine the new multiline
	 */
	public void setMultiline(boolean multiLine)
	{
		this.multiLine = multiLine;
		buildLines = true;
	}

	/**
	 * Checks if the text is multiline.
	 *
	 * @return true, if is multiline
	 */
	public boolean isMultiLine()
	{
		return multiLine;
	}

	public int getMaxWidth()
	{
		return lines().stream().mapToInt(l -> (int) font.getStringWidth(l, fontOptions)).max().orElse(0);
	}

	/**
	 * Gets the size of the text.<br>
	 * Width matches longest line, height is number of lines multiplied by line height.
	 *
	 * @return the i size
	 */
	public ISize size()
	{
		return size;
	}

	/**
	 * Gets the line height.
	 *
	 * @return the line height
	 */
	public int getLineHeight()
	{
		return (int) (font.getStringHeight(fontOptions) + (multiLine ? lineSpacing : 0));
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
		buildCache = true;
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
	 * Forces the cache to be update and the lines to be rebuilt.<br>
	 * Cache and lines are updated when queried.
	 */
	public void forceUpdate()
	{
		buildCache = true;
		buildLines = true;
	}

	private void update()
	{
		generateCache();
		buildLines();
	}

	/**
	 * Generates the text cache and rebuild the lines.<br>
	 * Translates and applies the parameters.
	 */
	private void generateCache()
	{
		buildCache |= hasParametersChanged();
		if (!buildCache)
			return;

		String str = base;
		if (translated)
			str = I18n.format(str);
		str = applyParameters(str);
		cache = str;
		buildCache = false;
		buildLines = true;

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
	 * Splits the cache in multiple lines to fit in the {@link #wrapSize}.
	 *
	 * @param str the str
	 */
	private void buildLines()
	{
		if (!buildLines)
			return;

		lines.clear();
		if (!multiLine)
		{
			lines.add(cache);
			buildLines = false;
			return;
		}

		String str = cache.replace("\r?(?<=\n)", "\r");

		StringBuilder line = new StringBuilder();
		StringBuilder word = new StringBuilder();
		//FontRenderOptions fro = new FontRenderOptions();

		int maxWidth = wrapSize - 4;
		float lineWidth = 0;
		float wordWidth = 0;

		StringWalker walker = new StringWalker(str, font, fontOptions);
		walker.skipChars(false);
		walker.applyStyles(true);
		while (walker.walk())
		{
			char c = walker.getChar();
			lineWidth += walker.getWidth();
			wordWidth += walker.getWidth();

			word.append(c);

			//we just ended a new word, add it to the current line
			if (Character.isWhitespace(c) || c == '-' || c == '.')
			{
				line.append(word);
				word.setLength(0);
				wordWidth = 0;
			}
			if (lineWidth >= maxWidth || c == '\n')
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

		buildLines = false;
	}

	/**
	 * Renders this {@link GuiText} at the specified coordinates.
	 *
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 */
	public void render(GuiRenderer renderer, int startLine, int endLine, int x, int y, int z)
	{
		MalisisFont font = this.font.isLoaded() ? this.font : MalisisFont.minecraftFont;
		font.render(renderer, lines, startLine, endLine, x, y, z, lineSpacing, fontOptions);
	}

	/**
	 * Creates and returns a {@link StringWalker} for this text.
	 *
	 * @return the string walker
	 */
	public StringWalker walker()
	{
		return new StringWalker(lines, font, fontOptions);
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		lines().forEach(str -> {
			sb.append(str);
			sb.append("\n");
		});
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

}
