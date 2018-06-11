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

import com.google.common.base.Converter;

import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.content.ITextHolder;
import net.malisis.core.client.gui.element.Size;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.malisis.core.client.gui.render.GuiIcon;
import net.malisis.core.client.gui.render.shape.GuiShape;
import net.malisis.core.client.gui.text.GuiText;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.util.MouseButton;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;

/**
 * @author Ordinastie
 *
 */
public class UISlider<T> extends UIComponent implements ITextHolder
{
	private static int SLIDER_WIDTH = 8;

	/** Text to display over the slider. */
	protected final GuiText text;

	/** Current value. */
	protected T value;
	/** Position offset of the slider. */
	protected float offset;

	/** Amount of offset scrolled by when using the scroll wheel. */
	protected float scrollStep = 0.05F;

	/** Converter from float (0-1 offset) to the value. */
	protected Converter<Float, T> converter;

	public UISlider(int width, Converter<Float, T> converter, String text)
	{
		this.converter = checkNotNull(converter);
		this.value = converter.convert(0F);

		this.text = GuiText	.of(this)
							.text(text)
							.position()
							.x(this::textPosition)
							.middleAligned()
							.back()
							.bind("value", this::getValue)
							.zIndex(this::getZIndex)
							.fontOptions(FontOptions.builder().color(0xFFFFFF).shadow().when(this::isHovered).color(0xFFFFA0).build())
							.build();

		setSize(Size.of(width, 20));

		GuiShape sliderShape = GuiShape	.builder(this)
										.position()
										.x(this::scrollPosition)
										.back()
										.size(Size.of(SLIDER_WIDTH, () -> size().height()))
										.icon(GuiIcon.SLIDER)
										.border(5)
										.build();
		setBackground(GuiShape.builder(this).icon(GuiIcon.SLIDER_BG).build());
		setForeground(this.text.and(sliderShape));
	}

	//#region Getters/Setters
	@Override
	public GuiText content()
	{
		return text;
	}

	/**
	 * Sets the value for this {@link UISlider}.
	 *
	 * @param value the value
	 * @return this UI slider
	 */
	public UISlider<T> setValue(T value)
	{
		if (this.value == value)
			return this;
		if (!fireEvent(new ComponentEvent.ValueChange<>(this, this.value, value)))
			return this;

		this.value = value;
		this.offset = MathHelper.clamp(converter.reverse().convert(value), 0, 1);
		return this;
	}

	/**
	 * Gets the value for this {@link UISlider}.
	 *
	 * @return the value
	 */
	public T getValue()
	{
		return value;
	}

	public void setFontOptions(FontOptions fontOptions)
	{
		text.setFontOptions(fontOptions);
	}

	public int scrollPosition()
	{
		return (int) (offset * (size().width() - SLIDER_WIDTH));
	}

	public int textPosition()
	{
		int w = size().width(); //width
		int tw = text.size().width(); //text width
		int tx = (w - tw) / 2; //text x
		int sx = scrollPosition(); //scroll x

		if (sx > w / 2)
		{
			if (tx + tw + 2 > sx)
				return sx - tw - 2;
		}
		else
		{
			if (sx + SLIDER_WIDTH + 2 > tx)
				return sx + SLIDER_WIDTH + 2;
		}
		return tx;
	}

	/**
	 * Sets the amount of offset to scroll with the wheel.
	 *
	 * @param scrollStep the scroll step
	 * @return the UI slider
	 */
	public UISlider<T> setScrollStep(float scrollStep)
	{
		this.scrollStep = scrollStep;
		return this;
	}

	//#end Getters/Setters
	@Override
	public boolean onClick()
	{
		slideTo();
		return true;
	}

	@Override
	public boolean onScrollWheel(int delta)
	{
		slideTo(offset + delta * scrollStep);
		return true;
	}

	@Override
	public boolean onDrag(MouseButton button)
	{
		slideTo();
		return true;
	}

	/**
	 * Slides the slider to the specified pixel position.<br>
	 */
	public void slideTo()
	{
		int l = size().width() - SLIDER_WIDTH;
		int pos = MathHelper.clamp(mousePosition().x() - SLIDER_WIDTH / 2, 0, l);
		slideTo((float) pos / l);
	}

	/**
	 * Slides the slider to the specified offset between 0 and 1.<br>
	 * Sets the value relative to the offset.
	 *
	 * @param offset the offset
	 */
	public void slideTo(float offset)
	{
		if (!isEnabled())
			return;

		setValue(converter.convert(MathHelper.clamp(offset, 0, 1)));
	}

	@Override
	public String getPropertyString()
	{
		return "[" + TextFormatting.GREEN + text + " | " + text.position() + "@" + text.size() + TextFormatting.RESET + "] "
				+ super.getPropertyString();
	}
}
