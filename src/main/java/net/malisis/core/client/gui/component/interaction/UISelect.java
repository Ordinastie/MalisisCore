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

import static com.google.common.base.Preconditions.*;
import static net.malisis.core.client.gui.element.position.Positions.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import org.lwjgl.input.Keyboard;

import com.google.common.base.Predicates;

import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIListContainer;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.scrolling.UIScrollBar;
import net.malisis.core.client.gui.component.scrolling.UISlimScrollbar;
import net.malisis.core.client.gui.element.Padding;
import net.malisis.core.client.gui.element.Size;
import net.malisis.core.client.gui.element.position.Position;
import net.malisis.core.client.gui.element.position.Position.IPosition;
import net.malisis.core.client.gui.element.position.Positions;
import net.malisis.core.client.gui.event.ComponentEvent.ValueChange;
import net.malisis.core.client.gui.render.GuiIcon;
import net.malisis.core.client.gui.render.GuiRenderer;
import net.malisis.core.client.gui.render.shape.GuiShape;
import net.malisis.core.renderer.font.FontOptions;

/**
 * The Class UISelect.
 *
 * @author Ordinastie
 */
public class UISelect<T> extends UIComponent
{

	/** Make the options width match the longest option available. */
	public static int LONGEST_OPTION = -1;
	/** Make the options width match the {@link UISelect} width. */
	public static int SELECT_WIDTH = -2;

	protected int selectedIndex = -1;
	/** Max width of the option container. */
	protected int maxOptionsWidth = LONGEST_OPTION;
	/** Whether this {@link UISelect} is expanded. */
	protected boolean expanded = false;
	/** Container holding the {@link Option}. */
	protected OptionsContainer optionsContainer = new OptionsContainer();
	/** The string supplier to use for the default UILabel option. */
	protected Function<T, String> stringFunction = Objects::toString;
	/** Predicate for option disability */
	protected Predicate<T> disablePredicate = Predicates.alwaysFalse();

	/**
	 * Instantiates a new {@link UISelect}.
	 *
	 * @param width the width
	 * @param values the values
	 */
	public UISelect(int width, List<T> values)
	{
		setSize(Size.of(width, 12));
		setOptions(values);

		/** Shape used for the background of the select. */
		GuiShape background = GuiShape	.builder(this)
										.icon(GuiIcon.forComponent(this, GuiIcon.SELECT, null, GuiIcon.SELECT_DISABLED))
										.border(1)
										.build();
		/** Shape used to draw the arrow. */
		GuiShape arrowShape = GuiShape	.builder(this)
										.position(o -> rightAligned(o, 2), o -> middleAligned(o, 0))
										.size(7, 4)
										.color(() -> (isHovered() || expanded ? 0xBEC8FF : 0xFFFFFF))
										.icon(GuiIcon.SELECT_ARROW)
										.build();

		setBackground(background);
		setForeground(r -> {
			UIComponent c = optionsContainer.getElementComponent(selected());
			if (c != null)
			{
				IPosition old = c.position();
				IPosition pos = Position.of(Positions.leftAligned(c, 1), Positions.middleAligned(c, 1));
				c.setParent(this);
				c.setPosition(pos);
				((IOptionComponent) c).renderSelected(r);
				c.setPosition(old);
				c.setParent(optionsContainer);
			}
			arrowShape.render(r);
		});
	}

	/**
	 * Instantiates a new {@link UISelect}.
	 *
	 * @param width the width
	 */
	public UISelect(int width)
	{
		this(width, null);
	}

	@Override
	public void onAddedToScreen(MalisisGui gui)
	{
		this.gui = gui;
		gui.addToScreen(optionsContainer);
	}

	//#region Getters/Setters
	private List<T> options()
	{
		return (List<T>) optionsContainer.getElements();
	}

	public void setDisablePredicate(Predicate<T> predicate)
	{
		this.disablePredicate = checkNotNull(predicate);
	}

	public void setStringFunction(Function<T, String> stringFunction)
	{
		this.stringFunction = checkNotNull(stringFunction);
	}

	@SuppressWarnings("unchecked")
	public <U extends UIComponent & IOptionComponent> void setComponentFactory(Function<T, U> factory)
	{
		optionsContainer.setComponentFactory((Function<T, UIComponent>) factory);
	}

	//#end Getters/Setters
	/**
	 * Set the options for this {@link UISelect}.
	 *
	 * @param elements the elements
	 * @return this {@link UISelect}
	 */
	public UISelect<T> setOptions(Collection<T> elements)
	{
		if (elements == null)
			elements = Collections.emptyList();

		optionsContainer.setElements(elements);
		return this;
	}

	/**
	 * Sets the selected option.<br>
	 * Does not trigger the SelectEvent
	 *
	 * @param option the new selected
	 */
	public void setSelected(T option)
	{
		selectedIndex = options().indexOf(option);
	}

	/**
	 * Gets the currently selected option.
	 *
	 * @return the selected option
	 */
	public T selected()
	{
		return selectedIndex < 0 || selectedIndex >= options().size() ? null : options().get(selectedIndex);
	}

	/**
	 * Selects the option.
	 *
	 * @param option the option
	 * @return the option value
	 */
	public T select(T option)
	{
		int index = options().indexOf(option);
		if (index == -1 || index == selectedIndex)
			return selected();

		if (fireEvent(new SelectEvent<>(this, option)))
			setSelected(option);

		return selected();
	}

	private T select(int index)
	{
		if (selectedIndex < 0 || selectedIndex >= options().size())
			index = -1;
		selectedIndex = index;
		return selected();
	}

	/**
	 * Selects the first option of this {@link UISelect}.
	 *
	 * @return the option value
	 */
	public T selectFirst()
	{
		return select(0);
	}

	/**
	 * Selects the last option of this {@link UISelect}.
	 *
	 * @return the option value
	 */
	public T selectLast()
	{
		return select(options().size() - 1);
	}

	/**
	 * Selects the option before the currently selected one.
	 *
	 * @return the option value
	 */
	public T selectPrevious()
	{
		if (selectedIndex <= 0)
			return selectFirst();
		return select(selectedIndex - 1);
	}

	/**
	 * Select the option after the currently selected one.
	 *
	 * @return the t
	 */
	public T selectNext()
	{
		if (selectedIndex >= options().size() - 1)
			return selectLast();
		return select(selectedIndex + 1);
	}

	@Override
	public boolean onClick()
	{
		if (!expanded)
			optionsContainer.display();
		else
			optionsContainer.hide();

		return true;
	}

	@Override
	public boolean onScrollWheel(int delta)
	{
		if (!isFocused())
			return true;

		if (delta < 0)
			selectNext();
		else
			selectPrevious();
		return true;
	}

	@Override
	public boolean onKeyTyped(char keyChar, int keyCode)
	{
		if (!isFocused() && !optionsContainer.isFocused())
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

	public class OptionsContainer extends UIListContainer<T>
	{
		/** The {@link UIScrollBar} used by this {@link OptionsContainer}. */
		protected UISlimScrollbar scrollbar;
		protected Padding padding = Padding.of(1);

		public OptionsContainer()
		{
			//TODO: place it above if room below is too small
			setPosition(Position.of(() -> UISelect.this.screenPosition().x(),
									() -> UISelect.this.screenPosition().y() + UISelect.this.size().height()));
			setSize(Size.of(this).widthRelativeTo(UISelect.this, 1.0F, 0).contentHeight(0).build());
			setZIndex(300);
			setBackground(GuiShape.builder(this).color(UISelect.this::getColor).icon(GuiIcon.SELECT_BOX).border(1).build());

			hide();

			setComponentFactory(Option::new);

			scrollbar = new UISlimScrollbar(this, UIScrollBar.Type.VERTICAL);
			scrollbar.setFade(false);
			scrollbar.setAutoHide(true);
		}

		private void display()
		{
			scrollbar.updateScrollbar();
			setFocused(true);
			setVisible(true);
			expanded = true;
		}

		private void hide()
		{
			if (isFocused())
				setFocused(false);
			setVisible(false);
			expanded = false;
		}

		//#region IScrollable
		@Override
		public Padding padding()
		{
			return padding;
		}

		//#end IScrollable
		@Override
		public void setFocused(boolean focused)
		{
			//			if (!focused && (MalisisGui.getFocusedComponent() == null || MalisisGui.getFocusedComponent().getParent() != this))
			//				hide();
			super.setFocused(focused);
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean onClick()
		{
			UIComponent comp = getComponentAt(MalisisGui.MOUSE_POSITION.x(), MalisisGui.MOUSE_POSITION.y());
			if (comp == null)
				return true;

			select((T) comp.getData());
			hide();
			UISelect.this.setFocused(true);
			return true;
		}

		@Override
		public boolean onKeyTyped(char keyChar, int keyCode)
		{
			return UISelect.this.onKeyTyped(keyChar, keyCode);
		}

		@Override
		public ClipArea getClipArea()
		{
			return ClipArea.from(this);
		}

		@Override
		public void setClipContent(boolean clip)
		{}

		@Override
		public boolean shouldClipContent()
		{
			return true;
		}
	}

	/*	public class OptionContainerSize implements ISize
		{
			private int width;
			private int height;

			private void update()
			{
				if (options == null)
					return;

				//calculate height
				int offset = UISelect.this.optionsContainer.optionOffset;
				height = 2;
				for (int i = offset; i < Math.min(offset + maxDisplayedOptions, options.size()); i++)
					height += options.get(i).getHeight(UISelect.this);

				//calculate width
				width = UISelect.this.size().width();
				if (maxOptionsWidth == SELECT_WIDTH)
					return;

				width -= 4;
				for (Option<?> option : UISelect.this)
					width = Math.max(width, (int) MalisisFont.minecraftFont.getStringWidth(option.getLabel(labelPattern), fontOptions));
				width += 4;
				if (width == LONGEST_OPTION)
					return;

				if (maxOptionsWidth >= UISelect.this.size().width())
					width = Math.min(maxOptionsWidth, width);
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
		}*/

	public class Option extends UILabel implements IOptionComponent
	{
		/** The default {@link FontOptions} to use for this {@link UISelect}. */
		protected FontOptions fontOptions = FontOptions	.builder()
														.color(0xFFFFFF)
														.shadow()
														.when(this::isHovered)
														.color(0xFED89F)
														.when(this::isSelected)
														.color(0x9EA8DF)
														.build();
		protected FontOptions selectedfontOptions = FontOptions.builder().color(0xFFFFFF).shadow().build();

		GuiShape background = GuiShape.builder(this).color(0x5E789F).alpha(() -> {
			if (isHovered())
				return 255;
			else
				return 0;
		}).build();

		public Option(T element)
		{
			super(false);
			text.setPosition(Position.of(1, 1));
			text.setText(stringFunction.apply(element));
			text.setFontOptions(fontOptions);

			attachData(element);

			setSize(Size.of(this).inheritedWidth().heightRelativeTo(text, 1.0F, 2).build());

			setBackground(background);
			setForeground(text);
		}

		public boolean isSelected()
		{
			return Objects.equals(selected(), getData());
		}

		@Override
		public void renderSelected(GuiRenderer renderer)
		{
			text.setFontOptions(selectedfontOptions);
			text.render(renderer);
			text.setFontOptions(fontOptions);
		}
	}

	public interface IOptionComponent
	{
		public default void renderSelected(GuiRenderer renderer)
		{
			((UIComponent) this).render(renderer);
		}
	}

	/**
	 * Event fired when a {@link UISelect} changes its selected option.<br>
	 * When catching the event, the state is not applied to the {@code UISelect} yet.<br>
	 * Canceling the event will prevent the {@code Option} to be set for the {@code UISelect} .
	 */
	public static class SelectEvent<T> extends ValueChange<UISelect<T>, T>
	{
		public SelectEvent(UISelect<T> component, T newValue)
		{
			super(component, component.selected(), newValue);
		}
	}
}
