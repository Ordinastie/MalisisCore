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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.malisis.core.client.gui.ClipArea;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.IClipable;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.interaction.UISelect.Option;
import net.malisis.core.client.gui.element.GuiShape;
import net.malisis.core.client.gui.element.SimpleGuiShape;
import net.malisis.core.client.gui.element.XResizableGuiShape;
import net.malisis.core.client.gui.element.XYResizableGuiShape;
import net.malisis.core.client.gui.event.ComponentEvent.ValueChange;
import net.malisis.core.client.gui.event.KeyboardEvent;
import net.malisis.core.client.gui.event.MouseEvent;
import net.malisis.core.client.gui.icon.GuiIcon;
import net.malisis.core.util.MouseButton;

import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.google.common.eventbus.Subscribe;

/**
 * The Class UISelect.
 *
 * @author Ordinastie
 */
public class UISelect extends UIComponent<UISelect> implements Iterable<Option>, IClipable
{
	/** The {@link Option options} of this {@link UISelect}. */
	protected Map<Integer, Option> options;
	/** Currently selected option index. */
	protected int selectedOption = -1;
	/** Max width of the option container. */
	protected int maxExpandedWidth = -1;
	/** Max number displayed options. */
	protected int maxDisplayedOptions = -1;
	/** Whether this {@link UISelect} is expanded. */
	protected boolean expanded = false;
	/** Width of displayed {@link Option} box */
	protected int optionsWidth = 0;
	/** Height of displayed {@link Option} box */
	protected int optionsHeight = 0;
	/** Pattern to use for options labels. */
	protected String labelPattern;

	/** Text color. */
	protected int textColor = 0xFFFFFF;
	/** Background color */
	protected int bgColor = 0xFFFFFF;
	/** Hovered text color */
	protected int hoverTextColor = 0xDED89F;
	/** Hovered background color */
	protected int hoverBgColor = 0x5E789F;
	/** Selected text color */
	protected int selectTextColor = 0x9EA8DF;
	/** Text shadow */
	protected boolean textShadow = true;

	/** Shape used to draw the arrow. */
	protected GuiShape arrowShape;
	/** Shape used to draw the {@link Option options} box */
	protected GuiShape optionsShape;
	/** Shape used to draw the hovered {@link Option} background **/
	protected GuiShape optionBackground;
	/** Icon used to draw this {@link UISelect}. */
	protected GuiIcon iconsSelect;
	/** Icon used to draw this {@link UISelect} when disabled. */
	protected GuiIcon iconsSelectDisabled;
	/** Icon used to draw the option container. */
	protected GuiIcon iconsExpanded;
	/** Icon used to draw the arrow. */
	protected GuiIcon arrowIcon;

	/**
	 * Instantiates a new {@link UISelect}
	 *
	 * @param gui the gui
	 * @param width the width
	 * @param options the options
	 */
	public UISelect(MalisisGui gui, int width, Map<Integer, Option> options)
	{
		super(gui);
		setSize(width, 12);
		setOptions(options);

		shape = new XResizableGuiShape(3);
		arrowShape = new SimpleGuiShape();
		arrowShape.setSize(7, 4);
		arrowShape.storeState();
		optionsShape = new XYResizableGuiShape(1);
		optionBackground = new SimpleGuiShape();

		iconsSelect = gui.getGuiTexture().getXResizableIcon(200, 30, 9, 12, 3);
		iconsSelectDisabled = gui.getGuiTexture().getXResizableIcon(200, 42, 9, 12, 3);
		iconsExpanded = gui.getGuiTexture().getXYResizableIcon(200, 30, 9, 12, 1);
		arrowIcon = gui.getGuiTexture().getIcon(209, 48, 7, 4);
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
	public int getTextColor()
	{
		return textColor;
	}

	public UISelect setTextColor(int textColor)
	{
		this.textColor = textColor;
		return this;
	}

	public int getBgColor()
	{
		return bgColor;
	}

	public UISelect setBgColor(int bgColor)
	{
		this.bgColor = bgColor;
		return this;
	}

	public int getHoverTextColor()
	{
		return hoverTextColor;
	}

	public UISelect setHoverTextColor(int hoverTextColor)
	{
		this.hoverTextColor = hoverTextColor;
		return this;
	}

	public int getHoverBgColor()
	{
		return hoverBgColor;
	}

	public UISelect setHoverBgColor(int hoverBgColor)
	{
		this.hoverBgColor = hoverBgColor;
		return this;
	}

	public int getSelectTextColor()
	{
		return selectTextColor;
	}

	public UISelect setSelectTextColor(int selectTextColor)
	{
		this.selectTextColor = selectTextColor;
		return this;
	}

	public boolean isTextShadow()
	{
		return textShadow;
	}

	public UISelect setTextShadow(boolean textShadow)
	{
		this.textShadow = textShadow;
		return this;
	}

	public UISelect setColors(int textColor, int bgColor, int hoverTextColor, int hoverBgColor, int selectTextColor, boolean textShadow)
	{
		this.textColor = textColor;
		this.bgColor = bgColor;
		this.hoverTextColor = hoverTextColor;
		this.hoverBgColor = hoverBgColor;
		this.textShadow = textShadow;
		this.selectTextColor = selectTextColor;

		return this;
	}

	//#end Getters/Setters

	@Override
	public void setFocused(boolean focused)
	{
		super.setFocused(focused);
		if (!focused && expanded)
			expanded = false;
	}

	/**
	 * Sets a pattern that will be used to format the option label.
	 *
	 * @param labelPattern the label pattern
	 * @return this {@link UISelect}
	 */
	public UISelect setLabelPattern(String labelPattern)
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
	public UISelect setMaxExpandedWidth(int width)
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
		optionsWidth = getWidth();
		for (Option<?> option : this)
			optionsWidth = Math.max(optionsWidth, GuiRenderer.getStringWidth(option.getLabel(labelPattern)));

		optionsWidth += 4;
		if (maxExpandedWidth > 0)
			optionsWidth = Math.min(maxExpandedWidth, optionsWidth);

		optionsHeight = 10 * (maxDisplayedOptions == -1 ? options.size() : maxDisplayedOptions) + 2;
	}

	/**
	 * Sets the maximum number of options displayed when expanded.
	 *
	 * @param amount the amount
	 * @return this {@link UISelect}
	 */
	public UISelect maxDisplayedOptions(int amount)
	{
		maxDisplayedOptions = amount;
		calcOptionsSize();
		return this;
	}

	/**
	 * Set the {@link Option options} to use for this {@link UISelect}
	 *
	 * @param options the options
	 * @return this {@link UISelect}
	 */
	public UISelect setOptions(Map<Integer, Option> options)
	{
		this.options = options;
		calcOptionsSize();
		return this;
	}

	/**
	 * Sets the selected {@link Option} from its position in the list.
	 *
	 * @param index the new selected option
	 */
	public void setSelectedOption(int index)
	{
		selectedOption = index;
	}

	/**
	 * Sets the selected {@link Option} from its containing key.
	 *
	 * @param obj the new selected option
	 */
	public void setSelectedOption(Object obj)
	{
		Option opt = getOption(obj);
		setSelectedOption(opt != null ? opt.index : -1);
	}

	/**
	 * Gets the option at the index.
	 *
	 * @param index the index
	 * @return the option
	 */
	private Option getOption(int index)
	{
		if (index < 0 || index >= options.size())
			return null;
		return options.get(index);
	}

	/**
	 * Gets the {@link Option} corresponding to the object.
	 *
	 * @param obj the key of the Option
	 * @return the option
	 */
	public Option getOption(Object obj)
	{
		for (Entry<Integer, UISelect.Option> entry : options.entrySet())
		{
			UISelect.Option option = entry.getValue();

			if (option.getKey() == obj)
				return option;
		}
		return null;
	}

	/**
	 * Gets the currently selected {@link Option}.
	 *
	 * @return the selected option
	 */
	public Option getSelectedOption()
	{
		return getOption(selectedOption);
	}

	/**
	 * Select the {@link Option} using the index.
	 *
	 * @param index the index
	 * @return the option
	 */
	public Option select(int index)
	{
		Option newValue = getOption(index);

		if (!fireEvent(new SelectEvent(this, newValue)))
			return getSelectedOption();

		selectedOption = newValue != null ? newValue.index : -1;
		return newValue;
	}

	public Option select(Option option)
	{
		if (option == null)
			return null;

		if (options.get(option.getIndex()) != option)
			return null;

		return select(option.getIndex());
	}

	/**
	 * Select the {@link Option} corresponding to the object.
	 *
	 * @param obj the obj
	 * @return the option
	 */
	public Option select(Object obj)
	{
		Option opt = getOption(obj);
		return select(opt != null ? opt.index : -1);
	}

	protected Option getHoveredOption(int mouseX, int mouseY)
	{
		if (!isInsideBounds(mouseX, mouseY))
			return null;

		int y = relativeY(mouseY - 13);
		if (y < 0)
			return null;

		return getOption(relativeY(mouseY - 13) / 10);
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

	public boolean isOptionHovered(Option option)
	{
		return false;
	}

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

	@Override
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		shape.resetState();
		shape.setSize(super.getWidth(), super.getHeight());
		rp.icon.set(isDisabled() ? iconsSelectDisabled : iconsSelect);
		rp.colorMultiplier.set(bgColor);
		renderer.drawShape(shape, rp);
	}

	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		arrowShape.resetState();
		arrowShape.setPosition(width - 9, 4);
		if (isHovered() || expanded)
			rp.colorMultiplier.set(0xBEC8FF);
		else
			rp.colorMultiplier.reset();
		rp.icon.set(arrowIcon);
		renderer.drawShape(arrowShape, rp);

		if (selectedOption != -1)
		{
			String text = getOption(selectedOption).getLabel(labelPattern);
			if (!StringUtils.isEmpty(text))
				renderer.drawText(renderer.clipString(text, width - 15), 2, 2, 1, textColor, textShadow, true);
		}

		if (!expanded)
			return;

		renderer.next();

		ClipArea area = getClipArea();
		renderer.startClipping(area);

		optionsShape.resetState();
		optionsShape.setSize(optionsWidth, optionsHeight);
		optionsShape.translate(0, 12, 1);
		rp.icon.set(iconsExpanded);
		rp.colorMultiplier.set(bgColor);

		renderer.drawShape(optionsShape, rp);
		renderer.next();

		int y = 14;
		Option hover = getHoveredOption(mouseX, mouseY);
		for (Option option : this)
		{
			if (option == hover)
			{
				renderer.next();

				GL11.glDisable(GL11.GL_TEXTURE_2D);

				shape.resetState();
				shape.setSize(optionsWidth - 2, 10);
				shape.translate(1, y - 1, 1);

				rp.colorMultiplier.set(hoverBgColor);
				renderer.drawShape(shape, rp);
				renderer.next();

				GL11.glEnable(GL11.GL_TEXTURE_2D);
			}

			int color = option.getIndex() == selectedOption ? selectTextColor : textColor;
			if (option == hover)
				color = hoverTextColor;;
			String text = option.getLabel(labelPattern);
			renderer.drawText(text, 2, y, 2, color, textShadow, true);
			y += 10;
		}

		renderer.endClipping(area);
	}

	/**
	 * On click.
	 *
	 * @param event the event
	 */
	@Subscribe
	public void onClick(MouseEvent.Release event)
	{
		if (event.getButton() != MouseButton.LEFT)
			return;

		if (!expanded)
		{
			expanded = true;
			return;
		}

		Option opt = getHoveredOption(event.getX(), event.getY());
		if (opt != null)
			select(opt);
		expanded = false;
		setFocused(true);
	}

	/**
	 * On scroll wheel.
	 *
	 * @param event the event
	 */
	@Subscribe
	public void onScrollWheel(MouseEvent.ScrollWheel event)
	{
		if (isFocused() && isHovered())
		{
			int selected = selectedOption + event.getDelta() * -1;
			selected = Math.max(0, Math.min(options.size() - 1, selected));
			select(selected);
		}
	}

	/**
	 * On key typed.
	 *
	 * @param event the event
	 */
	@Subscribe
	public void onKeyTyped(KeyboardEvent event)
	{
		if (!isFocused())
			return;

		int keyCode = event.getKeyCode();
		switch (keyCode)
		{
			case Keyboard.KEY_UP:
				if (selectedOption > 0)
					select(selectedOption - 1);
				break;
			case Keyboard.KEY_DOWN:
				if (selectedOption < options.size() - 1)
					select(selectedOption + 1);
				break;
			case Keyboard.KEY_HOME:
				select(0);
				break;
			case Keyboard.KEY_END:
				select(options.size() - 1);
				break;
		}
	}

	@Override
	public Iterator<Option> iterator()
	{
		return options.values().iterator();
	}

	/**
	 * The Class Option.
	 *
	 * @param <T> the generic type
	 */
	public static class Option<T>
	{
		/** The index. */
		private int index;
		/** The key. */
		private T key;
		/** The label. */
		private String label;

		/**
		 * Instantiates a new {@link Option}.
		 *
		 * @param index the index
		 * @param key the key
		 * @param value the value
		 */
		public Option(int index, T key, String value)
		{
			this.index = index;
			this.key = key;
			this.label = value;
		}

		/**
		 * Gets the index of this {@link Option}.
		 *
		 * @return the index
		 */
		public int getIndex()
		{
			return index;
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
		 * Creates an option Map for {@link UISelect#setOptions(Map)} from a list of keys.<br>
		 *
		 * @param <T> the generic type
		 * @param list the list
		 * @return the hash map
		 */
		public static <T> Map<Integer, Option> fromList(List<T> list)
		{
			Map<Integer, Option> options = new HashMap<>();
			int index = 0;
			for (T opt : list)
			{
				Option<T> option = new Option(index, opt, opt.toString());
				options.put(index, option);
				index++;
			}

			return options;
		}

		/**
		 * Creates an option Map for {@link UISelect#setOptions(Map)} from a HashMap of keys -&gt; labels.<br>
		 *
		 * @param <T> the generic type
		 * @param list the list
		 * @return the hash map
		 */
		public static <T> Map<Integer, Option> fromList(Map<T, String> list)
		{
			Map<Integer, Option> options = new HashMap<>();
			int index = 0;

			for (Entry<T, String> entry : list.entrySet())
			{
				Option<T> option = new Option(index, entry.getKey(), entry.getValue());
				options.put(index, option);
				index++;
			}

			return options;
		}

		/**
		 * Creates an option Map for {@link UISelect#setOptions(Map)} from an Enum
		 *
		 * @param <T> the generic type
		 * @param <E> the element type
		 * @param enumClass the enum class
		 * @return the hash map
		 */
		public static <T, E extends Enum> Map<Integer, Option> fromEnum(Class<E> enumClass)
		{
			Map<Integer, Option> options = new HashMap<>();
			for (E e : enumClass.getEnumConstants())
			{
				Option<T> option = new Option(e.ordinal(), e, e.toString());
				options.put(e.ordinal(), option);
			}

			return options;

		}
	}

	/**
	 * Event fired when a {@link UISelect} changes its selected {@link Option}.<br>
	 * When catching the event, the state is not applied to the {@code UISelect} yet.<br>
	 * Cancelling the event will prevent the {@code Option} to be set for the {@code UISelect} .
	 */
	public static class SelectEvent extends ValueChange<UISelect, Option>
	{
		public SelectEvent(UISelect component, Option newOption)
		{
			super(component, component.getSelectedOption(), newOption);
		}

		/**
		 * Gets the new {@link Option} to be set.
		 *
		 * @return the new option
		 */
		public Option getOption()
		{
			return newValue;
		}
	}
}
