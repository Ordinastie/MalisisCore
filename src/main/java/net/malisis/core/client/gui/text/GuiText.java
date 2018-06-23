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
import java.util.Objects;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.content.IContent;
import net.malisis.core.client.gui.element.IChild;
import net.malisis.core.client.gui.element.IClipable;
import net.malisis.core.client.gui.element.IClipable.ClipArea;
import net.malisis.core.client.gui.element.Size;
import net.malisis.core.client.gui.element.Size.ISize;
import net.malisis.core.client.gui.element.position.Position;
import net.malisis.core.client.gui.element.position.Position.IPosition;
import net.malisis.core.client.gui.element.position.PositionBuilder;
import net.malisis.core.client.gui.render.GuiRenderer;
import net.malisis.core.client.gui.render.IGuiRenderer;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.renderer.font.StringWalker;
import net.malisis.core.util.cacheddata.CachedData;
import net.malisis.core.util.cacheddata.FixedData;
import net.malisis.core.util.cacheddata.ICachedData;
import net.malisis.core.util.cacheddata.IntCachedData;
import net.malisis.core.util.cacheddata.IntCachedData.IntFixedData;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;

/**
 * The Class GuiString represents a String to be used and displayed in a GUI.<br>
 * It can be translated, and used with fixed or dynamic named parameters.<br>
 * Parameters are used with {key} markers in the text.<br>
 * The text will be automatically split in lines based on the wrap size.
 *
 * @author Ordinastie
 */
public class GuiText implements IGuiRenderer, IContent, IChild<UIComponent>
{
	public static int MULTILINE = 1;
	public static int TRANSLATED = 2;
	public static int LITTERAL = 4;

	/** Pattern for named parameters. */
	private static final Pattern pattern = Pattern.compile("\\{(?<key>.*?)\\}");

	/** Base text to be translated and parameterized. */
	private String base = "";
	/** Lines composing the text. */
	private final List<LineInfo> lines = Lists.newArrayList();
	/** Parameters. */
	private final Map<String, ICachedData<?>> parameters;

	/** The base font options to use to render. */
	private FontOptions fontOptions = FontOptions.EMPTY;
	/** Translated text with resolved parameters. */
	private String cache = null;

	/** Whether this text is multiLine. */
	private final boolean multiLine;
	/** Whether the text should be translated. */
	private final boolean translated;
	/** Whether the text should handle styles. */
	private final boolean literal;

	/** Text wrap size. 0 means do not wrap. */
	private IntCachedData wrapSize;

	private IPosition position;
	private final IPosition screenPosition = new Position.ScreenPosition(this);
	private ISize size = Size.ZERO;
	private IntSupplier zIndex = () -> 0;

	private boolean buildLines = true;
	private boolean buildCache = true;

	private UIComponent parent;

	public GuiText(String text, Map<String, ICachedData<?>> parameters, PositionBuilder<GuiText, Builder> positionBuilder, IntSupplier zIndex, UIComponent parent, FontOptions fontOptions, boolean multiLine, boolean translated, boolean literal, IntSupplier wrapSize)
	{
		this.base = text;
		this.parameters = parameters;
		this.position = positionBuilder != null ? positionBuilder.build(this) : Position.ZERO;
		this.zIndex = zIndex != null ? zIndex : () -> 0;
		this.parent = parent;
		this.fontOptions = fontOptions;
		this.multiLine = multiLine;
		this.translated = translated;
		this.literal = literal;
		this.wrapSize = wrapSize != null ? new IntCachedData(wrapSize) : new IntFixedData(0);
		this.parent = parent;
	}

	@Override
	public UIComponent getParent()
	{
		return parent;
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

	public void setParameters(Map<String, ICachedData<?>> params)
	{
		parameters.clear();
		parameters.putAll(checkNotNull(params));
	}

	public void setWrapSize(int size)
	{
		wrapSize = new IntFixedData(size);
		buildLines = true;
	}

	public void setWrapSize(IntSupplier supplier)
	{
		wrapSize = new IntCachedData(checkNotNull(supplier));
		buildLines = true;
	}

	/**
	 * Gets the different lines.
	 *
	 * @return the list
	 */
	public List<LineInfo> lines()
	{
		update();
		return lines;
	}

	public int length()
	{
		update();
		return cache.length();
	}

	/**
	 * Gets the line count of this text.
	 *
	 * @return the int
	 */
	public int lineCount()
	{
		return lines.size();
	}

	/**
	 * Checks if the text is multiLine.
	 *
	 * @return true, if is multiLine
	 */
	public boolean isMultiLine()
	{
		return multiLine;
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
	 * Checks if the text should be rendered without styles.
	 *
	 * @return true, if is literal
	 */
	public boolean isLitteral()
	{
		return literal;
	}

	/**
	 * Gets the wrap size for the text.
	 *
	 * @return the wrap size
	 */
	public int getWrapSize()
	{
		return wrapSize.get();
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
	 * Sets the font options to use to render.
	 *
	 * @param fontOptions the new font options
	 */
	public void setFontOptions(FontOptions fontOptions)
	{
		checkNotNull(fontOptions);
		buildLines = this.fontOptions.isBold() != fontOptions.isBold() || this.fontOptions.getFontScale() != fontOptions.getFontScale();
		this.fontOptions = fontOptions;
	}

	@Override
	public IPosition position()
	{
		return position;
	}

	public void setPosition(IPosition position)
	{
		this.position = position;
	}

	/**
	 * Gets the size of the text.<br>
	 * Width matches longest line, height is the sum of each line height
	 *
	 * @return the i size
	 */
	@Override
	public ISize size()
	{
		update(); //make sure to update cache
		return size;
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
		if (o == null) //not actual parameter : translate it
			return translated ? I18n.format(key) : key;
		return Objects.toString(o.get()).replace("$", "\\$");
	}

	/**
	 * Checks whether any parameter has changed.
	 *
	 * @return true, if successful
	 */
	private boolean hasParametersChanged()
	{
		boolean changed = false;
		for (ICachedData<?> data : parameters.values())
		{
			data.update();
			if (data.hasChanged())
				changed |= true; //can't return early, we need to call update on each data
		}
		return changed;
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
		if (StringUtils.isEmpty(base))
		{
			cache = "";
			return;
		}

		generateCache();
		buildLines();
	}

	private void updateSize()
	{
		int w = 0, h = 0;
		for (LineInfo info : lines)
		{
			w = Math.max(info.width(), w);
			h += info.height() + fontOptions.lineSpacing();
		}
		size = Size.of(w, h);
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
		str = sb.toString();
		return translated ? I18n.format(str) : str;
	}

	/**
	 * Splits the cache in multiple lines to fit in the {@link #wrapSize}.
	 *
	 * @param str the str
	 */
	private void buildLines()
	{
		wrapSize.update();
		buildLines |= wrapSize.hasChanged();
		if (!buildLines)
			return;

		lines.clear();

		String str = cache.replace("\r?(?<=\n)", "\n");

		StringBuilder line = new StringBuilder();
		StringBuilder word = new StringBuilder();
		//FontRenderOptions fro = new FontRenderOptions();

		int maxWidth = multiLine ? getWrapSize() : -1;
		float lineWidth = 0;
		float wordWidth = 0;
		float lineHeight = 0;

		StringWalker walker = new StringWalker(str, fontOptions);
		walker.skipChars(false);
		walker.applyStyles(true);
		while (walker.walk())
		{
			char c = walker.getChar();
			lineWidth += walker.width();
			wordWidth += walker.width();
			lineHeight = Math.max(lineHeight, walker.height());

			word.append(c);

			//we just ended a new word, add it to the current line
			if (Character.isWhitespace(c) || c == '-' || c == '.')
			{
				line.append(word);
				word.setLength(0);
				wordWidth = 0;
			}
			if (isMultiLine() && ((maxWidth > 0 && lineWidth >= maxWidth) || c == '\n'))
			{
				//the first word on the line is too large, split anyway
				if (line.length() == 0)
				{
					line.append(word);
					word.setLength(0);
					wordWidth = 0;
				}

				//remove current word from last line
				lineWidth -= wordWidth;
				//new line -> no justification
				float extra = c == '\n' ? 0 : maxWidth - lineWidth;

				//add the new line
				lines.add(new LineInfo(line.toString(), MathHelper.ceil(lineWidth), MathHelper.ceil(lineHeight), extra));
				line.setLength(0);

				lineWidth = wordWidth;
			}
		}

		line.append(word);
		lines.add(new LineInfo(line.toString(), MathHelper.ceil(lineWidth), MathHelper.ceil(lineHeight), 0));

		buildLines = false;
		updateSize();
	}

	/**
	 * Renders all the text based on its set position.
	 *
	 * @param renderer the renderer
	 */
	@Override
	public void render(GuiRenderer renderer)
	{
		update();
		if (StringUtils.isEmpty(cache))
			return;

		int x = screenPosition.x();
		int y = screenPosition.y();
		int z = zIndex.getAsInt();
		ClipArea area = null;
		if (parent instanceof UIComponent)
			z += parent.getZIndex();
		if (parent instanceof IClipable)
			area = ((IClipable) parent).getClipArea();
		render(renderer, x, y, z, area);
	}

	/**
	 * Renders lines between startLine and endLine of this {@link GuiText} at the coordinates.
	 *
	 * @param renderer the renderer
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @param area the area
	 */
	public void render(GuiRenderer renderer, int x, int y, int z, ClipArea area)
	{
		if (StringUtils.isEmpty(cache))
			return;

		fontOptions.getFont().render(renderer, this, x, y, z, fontOptions, area);
		renderer.forceRebind();
	}

	/**
	 * Creates and returns a {@link StringWalker} for this text.
	 *
	 * @return the string walker
	 */
	public StringWalker walker()
	{
		return new StringWalker(this, fontOptions);
	}

	@Override
	public String toString()
	{
		String str = position + "@" + size;
		if (isMultiLine())
			str += " (wrap: " + getWrapSize() + ")";
		return str;
		//return lines().stream().map(LineInfo::getText).collect(Collectors.joining());
	}

	public static Builder of(UIComponent parent)
	{
		return new Builder().parent(parent);
	}

	public static Builder of(String text)
	{
		return new Builder().text(text);
	}

	public static class Builder
	{
		private String text;
		private final Map<String, ICachedData<?>> parameters = Maps.newHashMap();
		private FontOptions fontOptions = FontOptions.EMPTY;
		private IntSupplier zIndex;
		private UIComponent parent;
		private boolean multiLine = false;
		private boolean translated = true;
		private boolean literal = false;
		private IntSupplier wrapSize;

		private PositionBuilder<GuiText, Builder> positionBuilder;;

		public Builder()
		{

		}

		public Builder text(String text)
		{
			this.text = text;
			if (text.contains("\r") || text.contains("\n"))
				multiLine = true;
			return this;
		}

		public Builder text(Supplier<String> supplier)
		{
			this.text = "{text}";
			return bind("text", supplier);
		}

		public PositionBuilder<GuiText, Builder> position()
		{
			positionBuilder = new PositionBuilder<>(this, true);
			return positionBuilder;
		}

		public Builder zIndex(int zIndex)
		{
			return zIndex(() -> zIndex);
		}

		public Builder zIndex(IntSupplier zIndex)
		{
			this.zIndex = zIndex;
			return this;
		}

		public Builder parent(UIComponent parent)
		{
			this.parent = parent;
			return this;
		}

		/**
		 * Binds a fixed value to the specified parameter.
		 *
		 * @param <T> the generic type
		 * @param key the key
		 * @param value the value
		 */
		public <T> Builder bind(String key, T value)
		{
			return bind(key, new FixedData<>(value));
		}

		/**
		 * Binds supplier to the specified parameter.
		 *
		 * @param <T> the generic type
		 * @param key the key
		 * @param supplier the supplier
		 */
		public <T> Builder bind(String key, Supplier<T> supplier)
		{
			return bind(key, new CachedData<>(supplier));
		}

		/**
		 * Binds {@link ICachedData} to the specified parameter.
		 *
		 * @param <T> the generic type
		 * @param key the key
		 * @param data the data
		 */
		public <T> Builder bind(String key, ICachedData<T> data)
		{
			parameters.put(key, data);
			return this;
		}

		public Builder fontOptions(FontOptions fontOptions)
		{
			this.fontOptions = fontOptions;
			return this;
		}

		public Builder multiLine(boolean multiLine)
		{
			this.multiLine = multiLine;
			return this;
		}

		public Builder multiLine()
		{
			multiLine = true;
			return this;
		}

		public Builder translated(boolean translated)
		{
			this.translated = translated;
			return this;
		}

		public Builder translated()
		{
			translated = true;
			return this;
		}

		public Builder literal(boolean literal)
		{
			this.literal = literal;
			return this;
		}

		public Builder literal()
		{
			literal = true;
			return this;
		}

		public Builder wrapSize(int size)
		{
			wrapSize = () -> size;
			return this;
		}

		public Builder wrapSize(IntSupplier supplier)
		{
			wrapSize = checkNotNull(supplier);
			return this;
		}

		public GuiText build()
		{
			if (parent != null && multiLine && wrapSize == null)
				wrapSize = () -> parent.innerSize().width();

			return new GuiText(text, parameters, positionBuilder, zIndex, parent, fontOptions, multiLine, translated, literal, wrapSize);
		}

	}

	public static class LineInfo implements ISize
	{
		private final String text;
		private final int width;
		private final int height;
		private final float spaceWidth;

		private LineInfo(String text, int width, int height, float spaceWidth)
		{
			this.text = text;
			this.width = width;
			this.height = height;
			this.spaceWidth = spaceWidth;
		}

		public String text()
		{
			return text;
		}

		@Override
		public int width()
		{
			return width;
		}

		@Override
		public int height()
		{
			return height;
		}

		public float spaceWidth()
		{
			return spaceWidth;
		}
	}

}
