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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import net.malisis.core.client.gui.ClipArea;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.element.GuiShape;
import net.malisis.core.client.gui.element.SimpleGuiShape;
import net.malisis.core.client.gui.element.XResizableGuiShape;
import net.malisis.core.client.gui.element.XYResizableGuiShape;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.malisis.core.client.gui.event.KeyboardEvent;
import net.malisis.core.client.gui.event.MouseEvent;
import net.malisis.core.client.gui.icon.GuiIcon;
import net.malisis.core.util.MouseButton;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.google.common.eventbus.Subscribe;

/**
 * The Class UISelect.
 *
 * @author Ordinastie
 */
public class UISelect extends UIComponent<UISelect>
{
	/** The options container. */
	protected OptionsContainer optionsContainer;
	/** Currently selected option index. */
	protected int selectedOption = -1;
	/** Max width of the option container. */
	protected int maxExpandedWidth = -1;
	/** Max number displayed options. */
	protected int maxDisplayedOptions = -1;
	/** Whether this {@link UISelect} is expanded. */
	protected boolean expanded = false;
	/** Pattern to use for options labels. */
	protected String labelPattern;

	/** Shape used to draw the arrow. */
	protected GuiShape arrowShape;
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
	public UISelect(MalisisGui gui, int width, HashMap<Integer, Option> options)
	{
		super(gui);
		setSize(width, 12);
		this.optionsContainer = new OptionsContainer(gui);
		setOptions(options);

		shape = new XResizableGuiShape(3);
		arrowShape = new SimpleGuiShape();
		arrowShape.setSize(7, 4);
		arrowShape.storeState();

		iconsSelect = gui.getGuiTexture().getXResizableIcon(200, 30, 9, 12, 3);
		iconsSelectDisabled = gui.getGuiTexture().getXResizableIcon(200, 42, 9, 12, 3);
		iconsExpanded = gui.getGuiTexture().getXYResizableIcon(200, 30, 9, 12, 3);
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

	@Override
	public void setParent(UIComponent parent)
	{
		super.setParent(parent);
		this.optionsContainer.setParent(parent);
	}

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
		for (Option option : optionsContainer.options.values())
		{
			optionsContainer.optionsLabel.get(option.getIndex()).setText(String.format(labelPattern, option.label));
		}
		optionsContainer.calcExpandedSize();
		return this;
	}

	/**
	 * Sets the max width of the option container.
	 *
	 * @param width the width
	 * @return this {@link UISelect}
	 */
	public UISelect maxExpandedWidth(int width)
	{
		maxExpandedWidth = width;
		optionsContainer.calcExpandedSize();
		return this;
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
		optionsContainer.calcExpandedSize();
		return this;
	}

	/**
	 * Set the {@link Option options} to use for this {@link UISelect}
	 *
	 * @param options the options
	 * @return this {@link UISelect}
	 */
	public UISelect setOptions(HashMap<Integer, Option> options)
	{
		optionsContainer.setOptions(options);
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
	 * Gets the {@link Option} at the specified index.
	 *
	 * @param index the index
	 * @return the option
	 */
	public Option getOption(int index)
	{
		return optionsContainer.getOption(index);
	}

	/**
	 * Gets the {@link Option} corresponding to the object.
	 *
	 * @param obj the obj
	 * @return the option
	 */
	public Option getOption(Object obj)
	{
		return optionsContainer.getOption(obj);
	}

	/**
	 * Gets the currently selected {@link Option}.
	 *
	 * @return the selected option
	 */
	public Option getSelectedOption()
	{
		return optionsContainer.getOption(selectedOption);
	}

	/**
	 * Select the {@link Option} using the index.
	 *
	 * @param index the index
	 * @return the option
	 */
	public Option select(int index)
	{
		Option oldValue = getOption(selectedOption);
		Option newValue = getOption(index);

		selectedOption = newValue != null ? newValue.index : -1;

		if (!fireEvent(new ComponentEvent.ValueChange(this, oldValue, newValue)))
		{
			selectedOption = oldValue.index;
			return oldValue;
		}
		return newValue;
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

	@Override
	public boolean isInsideBounds(int x, int y)
	{
		return super.isInsideBounds(x, y) || optionsContainer.isInsideBounds(x, y);
	}

	@Override
	public UIComponent getComponentAt(int x, int y)
	{
		if (super.isInsideBounds(x, y))
			return this;
		else if (optionsContainer.isInsideBounds(x, y))
			return optionsContainer.getComponentAt(x, y);
		return null;
	}

	@Override
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		rp.icon.set(isDisabled() ? iconsSelectDisabled : iconsSelect);
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
			if (text != null && text.length() != 0)
				renderer.drawText(renderer.clipString(text, width - 15), 2, 2, 0xFFFFFF, true);
		}
		// MalisisCore.message(this.optionsContainer.options.get(0).isHovered());

		renderer.next();

		ClipArea area = optionsContainer.getClipArea();
		renderer.startClipping(area);

		optionsContainer.draw(renderer, mouseX, mouseY, partialTick);

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

		if (super.isInsideBounds(event.getX(), event.getY()))
		{
			if (!expanded)
				optionsContainer.setFocused(true);
			expanded = !expanded;
		}
		else if (expanded)
		{
			int selected = ((optionsContainer.relativeY(event.getY()) - 1) / 10);
			select(selected);
			expanded = false;
			setFocused(true);
		}

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
			selected = Math.max(0, Math.min(optionsContainer.optionsLabel.size() - 1, selected));
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
				if (selectedOption < optionsContainer.options.size() - 1)
					select(selectedOption + 1);
				break;
			case Keyboard.KEY_HOME:
				select(0);
				break;
			case Keyboard.KEY_END:
				select(optionsContainer.optionsLabel.size() - 1);
				break;
		}
	}

	/**
	 * The Class OptionsContainer.
	 */
	private class OptionsContainer extends UIContainer
	{

		/** The gui. */
		protected MalisisGui gui;

		/** The options. */
		protected HashMap<Integer, Option> options;

		/** The options label. */
		protected ArrayList<UILabel> optionsLabel = new ArrayList<>();

		/**
		 * Instantiates a new options container.
		 *
		 * @param gui the gui
		 */
		public OptionsContainer(MalisisGui gui)
		{
			super(gui);
			this.gui = gui;
			this.zIndex = 101;

			shape = new XYResizableGuiShape(3);
		}

		/**
		 * Sets the options.
		 *
		 * @param options the options
		 */
		private void setOptions(HashMap<Integer, Option> options)
		{
			this.options = options;
			for (UILabel label : this.optionsLabel)
				unregister(label);
			this.optionsLabel.clear();
			int i = 0;

			for (Entry<Integer, UISelect.Option> entry : options.entrySet())
			{
				UILabel label = new UILabel(gui, entry.getValue().getLabel(labelPattern)).setPosition(2, 1 + 10 * i++)
						.setZIndex(zIndex + 1).setDrawShadow(true).register(UISelect.this);
				this.optionsLabel.add(label);
				this.add(label);
			}

			calcExpandedSize();
		}

		/**
		 * Calculates the size of this container base on the options.
		 */
		private void calcExpandedSize()
		{
			width = UISelect.this.width;
			height = 10 * (maxDisplayedOptions == -1 ? optionsLabel.size() : maxDisplayedOptions) + 1;
			//if (maxDisplayedOptions != -1 && maxDisplayedOptions < optionsLabel.size())
			for (UILabel label : optionsLabel)
				width = Math.max(width, label.getWidth() + 4);
			if (maxExpandedWidth > 0)
				width = Math.min(maxExpandedWidth, width);
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
		 * Gets the options corresponding to the object.
		 *
		 * @param obj the obj
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
		 * Sets the focused.
		 *
		 * @param focused the new focused
		 */
		@Override
		public void setFocused(boolean focused)
		{
			if (focused)
				UISelect.this.setFocused(focused);
		}

		/**
		 * Checks if is visible.
		 *
		 * @return true, if is visible
		 */
		@Override
		public boolean isVisible()
		{
			return expanded;
		}

		/**
		 * Screen x.
		 *
		 * @return the int
		 */
		@Override
		public int screenX()
		{
			return UISelect.this.screenX();
		}

		/**
		 * Screen y.
		 *
		 * @return the int
		 */
		@Override
		public int screenY()
		{
			return UISelect.this.screenY() + 12;
		}

		/**
		 * Gets the clip area.
		 *
		 * @return the clip area
		 */
		@Override
		public ClipArea getClipArea()
		{
			return new ClipArea(this, 0, false);
		}

		/**
		 * Draw background.
		 *
		 * @param renderer the renderer
		 * @param mouseX the mouse x
		 * @param mouseY the mouse y
		 * @param partialTick the partial tick
		 */
		@Override
		public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
		{
			rp.icon.set(iconsExpanded);
			renderer.drawShape(shape, rp);

			for (UILabel label : optionsLabel)
			{
				if (label.isHovered())
				{
					renderer.next();

					GL11.glDisable(GL11.GL_TEXTURE_2D);

					shape.resetState();
					shape.setSize(label.getWidth(), label.getHeight());
					shape.setPosition(componentX(label) - 1, componentY(label));

					rp.colorMultiplier.set(0x5E789F);
					rp.useTexture.set(false);
					renderer.drawShape(shape, rp);

					renderer.next();

					GL11.glEnable(GL11.GL_TEXTURE_2D);

				}
			}

		}

		/**
		 * Draw foreground.
		 *
		 * @param renderer the renderer
		 * @param mouseX the mouse x
		 * @param mouseY the mouse y
		 * @param partialTick the partial tick
		 */
		@Override
		public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
		{
			int i = 0;
			for (UILabel label : optionsLabel)
			{
				int color = label.isHovered() ? (i == selectedOption ? 0xDED89F : 0xFFFFFF) : (i == selectedOption ? 0x9EA8DF : 0xFFFFFF);
				label.setColor(color);
				label.draw(renderer, mouseX, mouseY, partialTick);
				i++;
			}
		}

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
		 * Instantiates a new option.
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
		 * Gets the index of this <code>Option</code>.
		 *
		 * @return the index
		 */
		public int getIndex()
		{
			return index;
		}

		/**
		 * Gets the key of this <code>Option</code>.
		 *
		 * @return the key
		 */
		public T getKey()
		{
			return key;
		}

		/**
		 * Gets the label of this <code>Option</code> using a pattern.
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
		 * Gets the base label of this <code>Option</code>.
		 *
		 * @return the label
		 */
		public String getLabel()
		{
			return label;
		}

		/**
		 * Creates an option HashMap for UISelect.setOptions() from a list of keys.<br>
		 *
		 * @param <T> the generic type
		 * @param list the list
		 * @return the hash map
		 */
		public static <T> HashMap<Integer, Option> fromList(List<T> list)
		{
			HashMap<Integer, Option> options = new HashMap<>();
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
		 * Creates an option HashMap for UISelect.setOptions() from a HashMap of keys -&gt; labels.<br>
		 *
		 * @param <T> the generic type
		 * @param list the list
		 * @return the hash map
		 */
		public static <T> HashMap<Integer, Option> fromList(HashMap<T, String> list)
		{
			HashMap<Integer, Option> options = new HashMap<>();
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
		 * Creates an option HashMap for UISelect.setOptions() from an Enum
		 *
		 * @param <T> the generic type
		 * @param <E> the element type
		 * @param enumClass the enum class
		 * @return the hash map
		 */
		public static <T, E extends Enum> HashMap<Integer, Option> fromEnum(Class<E> enumClass)
		{
			HashMap<Integer, Option> options = new HashMap<>();
			for (E e : enumClass.getEnumConstants())
			{
				Option<T> option = new Option(e.ordinal(), e, e.toString());
				options.put(e.ordinal(), option);
			}

			return options;

		}
	}

}
