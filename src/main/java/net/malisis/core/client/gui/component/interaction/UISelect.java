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

package net.malisis.core.client.gui.component.interaction;

import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;

import net.malisis.core.client.gui.ClipArea;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.IClipable;
import net.malisis.core.client.gui.component.IGuiText;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.control.IScrollable;
import net.malisis.core.client.gui.component.control.UIScrollBar;
import net.malisis.core.client.gui.component.control.UISlimScrollbar;
import net.malisis.core.client.gui.component.interaction.UISelect.Option;
import net.malisis.core.client.gui.element.GuiShape;
import net.malisis.core.client.gui.element.SimpleGuiShape;
import net.malisis.core.client.gui.element.XResizableGuiShape;
import net.malisis.core.client.gui.element.XYResizableGuiShape;
import net.malisis.core.client.gui.event.ComponentEvent.ValueChange;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.renderer.font.MalisisFont;
import net.malisis.core.renderer.icon.provider.GuiIconProvider;

/**
 * The Class UISelect.
 *
 * @author Ordinastie
 */
public class UISelect<T> extends UIComponent<UISelect<T>> implements Iterable<Option<T>>, IClipable, IGuiText<UISelect<T>>, IScrollable
{
	/** The {@link MalisisFont} to use for this {@link UISelect}. */
	protected MalisisFont font = MalisisFont.minecraftFont;
	/** The {@link FontOptions} to use for this {@link UISelect}. */
	protected FontOptions fontOptions = FontOptions.builder().color(0xFFFFFF).shadow().build();
	/** The {@link FontOptions} to use for this {@link UISelect} when option is hovered. */
	protected FontOptions hoveredFontOptions = FontOptions.builder().color(0xFED89F).shadow().build();
	/** The {@link FontOptions} to use for this {@link UISelect} when option is selected. */
	protected FontOptions selectedFontOptions = FontOptions.builder().color(0x9EA8DF).shadow().build();
	/** The {@link FontOptions} to use for this {@link UISelect} when option is disabled. */
	protected FontOptions disabledFontOptions = FontOptions.builder().color(0x444444).build();

	/** The {@link Option options} of this {@link UISelect}. */
	protected FluentIterable<Option<T>> options;
	/** Currently selected option index. */
	protected Option<T> selectedOption = null;
	/** Max width of the option container. */
	protected int maxExpandedWidth = -1;
	/** Max number displayed options. */
	protected int maxDisplayedOptions = Integer.MAX_VALUE;
	/** Whether this {@link UISelect} is expanded. */
	protected boolean expanded = false;
	/** Width of displayed {@link Option} box */
	protected int optionsWidth = 0;
	/** Height of displayed {@link Option} box */
	protected int optionsHeight = 0;
	/** Pattern to use for options labels. */
	protected String labelPattern;
	/** Function for option creation **/
	protected Function<T, ? extends Option<T>> optionFunction;
	/** Function for options label */
	protected Function<T, String> labelFunction;
	/** Predicate for option disability */
	protected Predicate<T> disablePredicate = Predicates.alwaysFalse();
	/** Default function to build options **/
	private Function<T, Option<T>> toOption = new Function<T, Option<T>>()
	{
		@Override
		public Option<T> apply(T input)
		{
			Option<T> option = optionFunction != null ? optionFunction.apply(input) : new Option<>(input);
			option.setLabel(labelFunction != null ? labelFunction.apply(input) : Objects.toString(input));
			option.setDisabled(disablePredicate.apply(input));
			return option;
		}
	};

	protected UISlimScrollbar scrollbar;
	protected int optionOffset;

	/** Background color */
	protected int bgColor = 0xFFFFFF;
	/** Hovered background color */
	protected int hoverBgColor = 0x5E789F;

	/** Shape used to draw the arrow. */
	protected GuiShape arrowShape;
	/** Shape used to draw the {@link Option options} box */
	protected GuiShape optionsShape;
	/** Shape used to draw the hovered {@link Option} background **/
	protected GuiShape optionBackground;
	/** Icon used to draw the option container. */
	protected GuiIconProvider iconsExpanded;
	/** Icon used to draw the arrow. */
	protected GuiIconProvider arrowIcon;

	/**
	 * Instantiates a new {@link UISelect}.
	 *
	 * @param gui the gui
	 * @param width the width
	 * @param values the values
	 */
	public UISelect(MalisisGui gui, int width, Iterable<T> values)
	{
		super(gui);
		setSize(width, 12);
		setOptions(values);

		scrollbar = new UISlimScrollbar(gui, this, UIScrollBar.Type.VERTICAL);
		scrollbar.setFade(false);
		scrollbar.setAutoHide(true);
		scrollbar.setOffset(0, 12);

		shape = new XResizableGuiShape(3);
		arrowShape = new SimpleGuiShape();
		arrowShape.setSize(7, 4);
		arrowShape.storeState();
		optionsShape = new XYResizableGuiShape(1);
		optionBackground = new SimpleGuiShape();

		iconProvider = new GuiIconProvider(gui.getGuiTexture().getXResizableIcon(200, 30, 9, 12, 3), null,
				gui.getGuiTexture().getXResizableIcon(200, 42, 9, 12, 3));

		iconsExpanded = new GuiIconProvider(gui.getGuiTexture().getXYResizableIcon(200, 30, 9, 12, 1));
		arrowIcon = new GuiIconProvider(gui.getGuiTexture().getIcon(209, 48, 7, 4));
	}

	/**
	 * Instantiates a new {@link UISelect}.
	 *
	 * @param gui the gui
	 * @param width the width
	 */
	public UISelect(MalisisGui gui, int width)
	{
		this(gui, width, null);
	}

	//#region Getters/Setters
	@Override
	public int getHeight()
	{
		return expanded ? optionsHeight : super.getHeight();
	}

	@Override
	public MalisisFont getFont()
	{
		return font;
	}

	@Override
	public UISelect<T> setFont(MalisisFont font)
	{
		this.font = font;
		calcOptionsSize();
		return this;
	}

	@Override
	public FontOptions getFontOptions()
	{
		return fontOptions;
	}

	@Override
	public UISelect<T> setFontOptions(FontOptions options)
	{
		this.fontOptions = options;
		calcOptionsSize();
		return this;
	}

	/**
	 * Gets the hovered {@link FontOptions}.
	 *
	 * @return the hoveredFontOptions
	 */
	public FontOptions getHoveredFontOptions()
	{
		return hoveredFontOptions;
	}

	/**
	 * Sets the selected {@link FontOptions}.
	 *
	 * @param options the options
	 * @return this {@link UISelect}
	 */
	public UISelect<T> setSelectedFontOptions(FontOptions options)
	{
		selectedFontOptions = options;
		return this;
	}

	/**
	 * Gets the selected {@link FontOptions}.
	 *
	 * @return the selectedFontOptions
	 */
	public FontOptions getSelectedFontOptions()
	{
		return selectedFontOptions;
	}

	/**
	 * Sets the disabled {@link FontOptions}.
	 *
	 * @param options the options
	 * @return this {@link UISelect}
	 */
	public UISelect<T> setDisabledFontOptions(FontOptions options)
	{
		disabledFontOptions = options;
		return this;
	}

	/**
	 * Gets the disabled {@link FontOptions}.
	 *
	 * @return the disabledFontOptions
	 */
	public FontOptions getDisabledFontOptions()
	{
		return disabledFontOptions;
	}

	/**
	 * Sets the hovered {@link FontOptions}.
	 *
	 * @param options the options
	 * @return this {@link UISelect}
	 */
	public UISelect<T> setHoveredFontOptions(FontOptions options)
	{
		hoveredFontOptions = options;
		return this;
	}

	public int getBgColor()
	{
		return bgColor;
	}

	public UISelect<T> setBgColor(int bgColor)
	{
		this.bgColor = bgColor;
		return this;
	}

	public int getHoverBgColor()
	{
		return hoverBgColor;
	}

	public UISelect<T> setHoverBgColor(int hoverBgColor)
	{
		this.hoverBgColor = hoverBgColor;
		return this;
	}

	public UISelect<T> setColors(int bgColor, int hoverBgColor)
	{
		this.bgColor = bgColor;
		this.hoverBgColor = hoverBgColor;
		return this;
	}

	public UISelect<T> setOptionFunction(Function<T, ? extends Option<T>> func)
	{
		this.optionFunction = func;
		return this;
	}

	public UISelect<T> setLabelFunction(Function<T, String> func)
	{
		this.labelFunction = func;
		calcOptionsSize();
		return this;
	}

	public UISelect<T> setDisablePredicate(Predicate<T> predicate)
	{
		if (predicate == null)
			predicate = Predicates.alwaysFalse();
		this.disablePredicate = predicate;
		return this;
	}

	//#end Getters/Setters

	@Override
	public void setFocused(boolean focused)
	{
		super.setFocused(focused);
		if (!focused && expanded)
		{
			expanded = false;
			scrollbar.updateScrollbar();
		}
	}

	/**
	 * Sets a pattern that will be used to format the option label.
	 *
	 * @param labelPattern the label pattern
	 * @return this {@link UISelect}
	 */
	public UISelect<T> setLabelPattern(String labelPattern)
	{
		this.labelPattern = labelPattern;
		calcOptionsSize();
		return this;
	}

	/**
	 * Sets the max width of the option container.
	 *
	 * @param width the width
	 * @return this {@link UISelect}
	 */
	public UISelect<T> setMaxExpandedWidth(int width)
	{
		maxExpandedWidth = width;
		calcOptionsSize();
		return this;
	}

	/**
	 * Calculates the size of this container base on the options. TODO : handle maximum display options
	 */
	private void calcOptionsSize()
	{
		optionsWidth = getWidth() - 4;
		for (Option<?> option : this)
			optionsWidth = Math.max(optionsWidth, (int) MalisisFont.minecraftFont.getStringWidth(option.getLabel(labelPattern)));

		optionsWidth += 4;
		if (maxExpandedWidth > 0)
			optionsWidth = Math.min(maxExpandedWidth, optionsWidth);
	}

	/**
	 * Sets the maximum number of options displayed when expanded.
	 *
	 * @param amount the amount
	 * @return this {@link UISelect}
	 */
	public UISelect<T> maxDisplayedOptions(int amount)
	{
		maxDisplayedOptions = amount;
		calcOptionsSize();
		return this;
	}

	/**
	 * Set the {@link Option options} to use for this {@link UISelect}.
	 *
	 * @param values the values
	 * @return this {@link UISelect}
	 */
	public UISelect<T> setOptions(Iterable<T> values)
	{
		if (values == null)
			values = Collections.emptyList();

		options = FluentIterable.from(values).transform(toOption);

		calcOptionsSize();
		return this;
	}

	/**
	 * Gets the {@link Option} corresponding to the object.
	 *
	 * @param obj the key of the Option
	 * @return the option
	 */
	public Option<T> getOption(T obj)
	{
		if (obj == null)
			return null;

		for (Option<T> opt : this)
			if (obj.equals(opt.getKey()))
				return opt;
		return null;
	}

	/**
	 * Sets the selected {@link Option} from its containing key.
	 *
	 * @param obj the new selected option
	 */
	public void setSelectedOption(T obj)
	{
		setSelectedOption(getOption(obj));
	}

	/**
	 * Sets the selected {@link Option}.
	 *
	 * @param option the new selected option
	 */
	public void setSelectedOption(Option<T> option)
	{
		selectedOption = option;
	}

	/**
	 * Gets the currently selected {@link Option}.
	 *
	 * @return the selected option
	 */
	public Option<T> getSelectedOption()
	{
		return selectedOption;
	}

	/**
	 * Gets the value of the {@link #selectedOption}.
	 *
	 * @return the selected value
	 */
	public T getSelectedValue()
	{
		Option<T> opt = getSelectedOption();
		if (opt == null)
			return null;

		return opt.getKey();
	}

	/**
	 * Selects the {@link Option}.
	 *
	 * @param option the option
	 * @return the option value
	 */
	public T select(Option<T> option)
	{
		//		if (option == null || option.isDisabled())
		//			return getSelectedValue();
		T value = option != null ? option.getKey() : null;
		if (Objects.equals(option, selectedOption))
			return value;

		if (fireEvent(new SelectEvent<>(this, value)))
			setSelectedOption(option);

		if (expanded && maxDisplayedOptions < options.size())
		{
			int i = getSelectedIndex();
			if (i < optionOffset)
				optionOffset = i;
			else if (i >= optionOffset + maxDisplayedOptions)
				optionOffset = i - maxDisplayedOptions + 1;
			optionOffset = Math.max(0, Math.min(options.size() - maxDisplayedOptions, optionOffset));
		}

		return getSelectedValue();
	}

	/**
	 * Selects the {@link Option} for the specified value.
	 *
	 * @param obj the obj
	 * @return the option value
	 */
	public T select(T obj)
	{
		return select(getOption(obj));
	}

	/**
	 * Selects the first {@link Option} of this {@link UISelect}.
	 *
	 * @return the option value
	 */
	public T selectFirst()
	{
		return select(Iterables.getFirst(options, null));
	}

	/**
	 * Selects the last {@link Option} of this {@link UISelect}.
	 *
	 * @return the option value
	 */
	public T selectLast()
	{
		return select(Iterables.getLast(options, null));
	}

	/**
	 * Selects the {@link Option} before the currently selected one.
	 *
	 * @return the option value
	 */
	public T selectPrevious()
	{
		if (selectedOption == null)
			return selectFirst();

		Option<T> option = null;
		for (Option<T> opt : this)
		{
			if (opt.isDisabled())
				continue;
			if (opt.equals(selectedOption))
				return select(option);
			option = opt;
		}
		//should not happen
		return null;
	}

	/**
	 * Select the {@link Option} after the currently selected one.
	 *
	 * @return the t
	 */
	public T selectNext()
	{
		if (selectedOption == null)
			return selectFirst();

		Option<T> option = null;
		for (Option<T> opt : this)
		{
			if (opt.isDisabled())
				continue;
			if (selectedOption.equals(option))
				return select(opt);
			option = opt;
		}
		//should not happen
		return null;
	}

	/**
	 * Gets the {@link Option} at the speicfied coordinates.
	 *
	 * @param mouseX the mouse x
	 * @param mouseY the mouse y
	 * @return the option
	 */
	protected Option<T> getOptionAt(int mouseX, int mouseY)
	{
		if (!isInsideBounds(mouseX, mouseY))
			return null;

		int y = relativeY(mouseY - 13);
		if (y < 0)
			return null;

		int cy = 0;
		for (int i = optionOffset; i < optionOffset + maxDisplayedOptions && i < options.size(); i++)
		{
			Option<T> option = options.get(i);
			if (cy + option.getHeight(this) > y)
				return option;
			cy += option.getHeight(this);
		}
		return null;
	}

	protected int getSelectedIndex()
	{
		if (selectedOption == null)
			return 0;

		for (int i = 0; i < options.size(); i++)
			if (options.get(i).equals(selectedOption))
				return i;
		return 0;
	}

	@Override
	public boolean isInsideBounds(int x, int y)
	{
		if (super.isInsideBounds(x, y))
			return true;

		if (!expanded || !isVisible())
			return false;

		return x >= screenX() && x <= screenX() + optionsWidth && y >= screenY() + 12 && y <= screenY() + 12 + optionsHeight;
	}

	@Override
	public int getZIndex()
	{
		return super.getZIndex() + (expanded ? 300 : 0);
	}

	//#region IClipable
	@Override
	public ClipArea getClipArea()
	{
		return new ClipArea(this, screenX(), screenY(), screenX() + optionsWidth, screenY() + optionsHeight + 12, false);
	}

	@Override
	public void setClipContent(boolean clip)
	{}

	@Override
	public boolean shouldClipContent()
	{
		return expanded;
	}

	//#end IClipable

	//#region IScrollable
	@Override
	public int getContentHeight()
	{
		if (!expanded || maxDisplayedOptions > options.size())
			return getHeight();

		return optionsHeight * options.size();
	}

	@Override
	public int getContentWidth()
	{
		return 0;
	}

	@Override
	public float getOffsetX()
	{
		return 0;
	}

	@Override
	public void setOffsetX(float offsetX, int delta)
	{}

	@Override
	public float getOffsetY()
	{
		return (float) optionOffset / (options.size() - maxDisplayedOptions);
	}

	@Override
	public void setOffsetY(float offsetY, int delta)
	{
		optionOffset = Math.round(offsetY / getScrollStep());
		optionOffset = Math.max(0, Math.min(options.size() - maxDisplayedOptions, optionOffset));
	}

	@Override
	public float getScrollStep()
	{
		return (float) 1 / (options.size() - maxDisplayedOptions);
	}

	@Override
	public int getVerticalPadding()
	{
		return 1;
	}

	@Override
	public int getHorizontalPadding()
	{
		return 1;
	}

	//#end IScrollable

	@Override
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		shape.resetState();
		shape.setSize(super.getWidth(), super.getHeight());
		rp.colorMultiplier.set(bgColor);
		renderer.drawShape(shape, rp);
	}

	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		optionsHeight = 10 * Math.min(options.size(), maxDisplayedOptions) + 2;
		if (optionsHeight < 10)
			optionsHeight = 10;
		if (selectedOption != null)
			select(selectedOption.getKey());

		//draw regular select
		arrowShape.resetState();
		arrowShape.setPosition(width - 9, 4);
		if (isHovered() || expanded)
			rp.colorMultiplier.set(0xBEC8FF);
		else
			rp.colorMultiplier.reset();
		rp.iconProvider.set(arrowIcon);
		renderer.drawShape(arrowShape, rp);

		//draw selected value
		if (selectedOption != null)
		{
			selectedOption.draw(this, renderer, 2, 2, 2, partialTick, false, true);
		}

		if (!expanded)
			return;

		renderer.next();

		ClipArea area = getClipArea();
		renderer.startClipping(area);

		optionsShape.resetState();
		optionsShape.setSize(optionsWidth, optionsHeight);
		optionsShape.translate(0, 12, 1);
		rp.iconProvider.set(iconsExpanded);
		rp.colorMultiplier.set(bgColor);

		renderer.drawShape(optionsShape, rp);
		renderer.next();

		int y = 14;
		Option<T> hover = getOptionAt(mouseX, mouseY);
		for (int i = optionOffset; i < optionOffset + maxDisplayedOptions && i < options.size(); i++)
		{
			Option<T> option = options.get(i);
			option.draw(this, renderer, 0, y, 0, partialTick, option.equals(hover), false);
			y += option.getHeight(this);
		}

		renderer.endClipping(area);
	}

	@Override
	public boolean onClick(int x, int y)
	{
		if (!expanded)
		{
			expanded = true;
			optionOffset = Math.max(0, Math.min(options.size() - maxDisplayedOptions, getSelectedIndex()));
			scrollbar.updateScrollbar();
			return true;
		}

		Option<T> opt = getOptionAt(x, y);
		if (opt != null)
		{
			if (opt.isDisabled())
			{
				setFocused(true);
				return true;
			}

			select(opt);
		}
		expanded = false;
		scrollbar.updateScrollbar();
		setFocused(true);
		return true;
	}

	@Override
	public boolean onScrollWheel(int x, int y, int delta)
	{
		if (!isFocused() || maxDisplayedOptions < options.size())
			return super.onScrollWheel(x, y, delta);

		if (delta < 0)
			selectNext();
		else
			selectPrevious();
		return true;
	}

	@Override
	public boolean onKeyTyped(char keyChar, int keyCode)
	{
		if (!isFocused())
			return super.onKeyTyped(keyChar, keyCode);

		switch (keyCode)
		{
			case Keyboard.KEY_UP:
				selectPrevious();
				break;
			case Keyboard.KEY_DOWN:
				selectNext();
				break;
			case Keyboard.KEY_HOME:
				selectFirst();
				break;
			case Keyboard.KEY_END:
				selectLast();
				break;
			default:
				return super.onKeyTyped(keyChar, keyCode);
		}
		return true;
	}

	@Override
	public Iterator<Option<T>> iterator()
	{
		return options.iterator();
	}

	/**
	 * The Class Option.
	 *
	 * @param <T> the generic type
	 */
	public static class Option<T>
	{
		/** The key. */
		private T key;
		/** The label. */
		private String label;
		/** Whether this option is disabled */
		private boolean disabled;

		/**
		 * Instantiates a new {@link Option}.
		 *
		 * @param key the key
		 */
		public Option(T key)
		{
			this.key = key;
		}

		/**
		 * Instantiates a new {@link Option} with a label.
		 *
		 * @param key the key
		 * @param label the label
		 */
		public Option(T key, String label)
		{
			this.key = key;
			this.label = label;
		}

		/**
		 * Gets the key of this {@link Option}.
		 *
		 * @return the key
		 */
		public T getKey()
		{
			return key;
		}

		/**
		 * Gets the label of this {@link Option} using a pattern.
		 *
		 * @param pattern the pattern
		 * @return the label
		 */
		public String getLabel(String pattern)
		{
			if (pattern == null)
				return label;

			return String.format(pattern, label);
		}

		/**
		 * Gets the base label of this {@link Option}.
		 *
		 * @return the label
		 */
		public String getLabel()
		{
			return label;
		}

		/**
		 * Sets the label for this {@link Option}.
		 *
		 * @param label the new label
		 */
		public void setLabel(String label)
		{
			this.label = label;
		}

		/**
		 * Checks if this {@link Option} is disabled.
		 *
		 * @return true, if is disabled
		 */
		public boolean isDisabled()
		{
			return disabled;
		}

		/**
		 * Sets the disabled state of this {@link Option}.
		 *
		 * @param disabled the new disabled
		 */
		public void setDisabled(boolean disabled)
		{
			this.disabled = disabled;
		}

		public int getHeight(UISelect<T> select)
		{
			return (int) (select.font.getStringHeight(select.fontOptions) + 1);
		}

		public void draw(UISelect<T> select, GuiRenderer renderer, int x, int y, int z, float partialTick, boolean hovered, boolean isTop)
		{
			String text = getLabel(select.labelPattern);
			if (StringUtils.isEmpty(text))
				return;

			if (hovered && !disabled)
			{
				renderer.drawRectangle(x + 1, y - 1, z + 2, select.optionsWidth - 2, getHeight(select), select.getHoverBgColor(), 255);
			}

			if (isTop)
				text = MalisisFont.minecraftFont.clipString(text, select.getWidth() - 15);

			FontOptions options = select.getFontOptions();
			if (equals(select.getSelectedOption()) && !isTop)
				options = select.getSelectedFontOptions();
			if (hovered)
				options = select.getHoveredFontOptions();

			if (disabled)
				options = select.getDisabledFontOptions();

			renderer.drawText(select.font, text, x + 2, y, z + 2, options);
		}

		@Override
		public boolean equals(Object obj)
		{
			return obj != null && obj instanceof Option && key.equals(((Option<?>) obj).key);
		}
	}

	/**
	 * Event fired when a {@link UISelect} changes its selected {@link Option}.<br>
	 * When catching the event, the state is not applied to the {@code UISelect} yet.<br>
	 * Cancelling the event will prevent the {@code Option} to be set for the {@code UISelect} .
	 */
	public static class SelectEvent<T> extends ValueChange<UISelect<T>, T>
	{
		public SelectEvent(UISelect<T> component, T newValue)
		{
			super(component, component.getSelectedValue(), newValue);
		}
	}
}
