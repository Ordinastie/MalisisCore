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

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Converter;
import com.mojang.realmsclient.gui.ChatFormatting;

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.component.IGuiText;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.element.GuiIcon;
import net.malisis.core.client.gui.element.GuiShape;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.renderer.font.MalisisFont;
import net.malisis.core.util.MouseButton;
import net.malisis.core.util.Silenced;
import net.minecraft.util.math.MathHelper;

/**
 * @author Ordinastie
 *
 */
public class UISlider<T> extends UIComponent<UISlider<T>> implements IGuiText<UISlider<T>>
{
	public static int SLIDER_WIDTH = 8;

	/** The {@link MalisisFont} to use for this {@link UISlider}. */
	protected MalisisFont font = MalisisFont.minecraftFont;
	/** The {@link FontOptions} to use for this {@link UISlider}. */
	protected FontOptions fontOptions = FontOptions.builder().color(0xFFFFFF).shadow().build();
	/** The {@link FontOptions} to use for this {@link UISlider} when hovered. */
	protected FontOptions hoveredFontOptions = FontOptions.builder().color(0xFFFFA0).shadow().build();

	/** Text to display over the slider. */
	protected String text;
	/** Current value. */
	protected T value;
	/** Position offset of the slider. */
	protected float offset;

	/** Amount of offset scrolled by when using the scroll wheel. */
	protected float scrollStep = 0.05F;

	/** Converter from float (0-1 offset) to the value. */
	protected Converter<Float, T> converter;

	protected GuiShape shape = new GuiShape();

	public UISlider(int width, Converter<Float, T> converter, String text)
	{
		this.text = text;
		this.converter = checkNotNull(converter);
		this.value = converter.convert(0F);

		setSize(width, 20);
	}

	//	public UISlider(MalisisGui gui, int width, float min, float max)
	//	{
	//		this(gui, width, null, null);
	//	}

	//#region Getters/Setters
	@Override
	public MalisisFont getFont()
	{
		return font;
	}

	@Override
	public UISlider<T> setFont(MalisisFont font)
	{
		this.font = font;
		return this;
	}

	@Override
	public FontOptions getFontOptions()
	{
		return fontOptions;
	}

	@Override
	public UISlider<T> setFontOptions(FontOptions fro)
	{
		this.fontOptions = fro;
		return this;
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
	public boolean onClick(int x, int y)
	{
		slideTo(x);
		return true;
	}

	@Override
	public boolean onScrollWheel(int x, int y, int delta)
	{
		slideTo(offset + delta * scrollStep);
		return true;
	}

	@Override
	public boolean onDrag(int lastX, int lastY, int x, int y, MouseButton button)
	{
		slideTo(x);
		return true;
	}

	/**
	 * Slides the slider to the specified pixel position.<br>
	 *
	 * @param x the x
	 */
	public void slideTo(int x)
	{
		int l = width - SLIDER_WIDTH;
		int pos = relativeX(x);
		pos = MathHelper.clamp(pos - SLIDER_WIDTH / 2, 0, l);
		slideTo((float) pos / l);
	}

	/**
	 * Slides the slider to the specified offset between 0 and 1.<br>
	 * Sets the value relative to the offset between {@link #minValue} and {@link #maxValue}.
	 *
	 * @param offset the offset
	 */
	public void slideTo(float offset)
	{
		if (isDisabled())
			return;

		setValue(converter.convert(MathHelper.clamp(offset, 0, 1)));
	}

	@Override
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		setupShape(shape);
		shape.setIcon(GuiIcon.SLIDER_BG);
		renderer.drawShape(shape);
	}

	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		int ox = (int) (offset * (width - SLIDER_WIDTH));
		float factor = getHeight() / 20F;

		shape.setPosition(ox, 0);
		shape.setSize((int) (8 * factor), getHeight());
		shape.setIcon(GuiIcon.SLIDER);
		renderer.drawShape(shape);

		renderer.next();
		//zIndex = 1;

		if (!StringUtils.isEmpty(text))
		{
			String str = Silenced.get(() -> String.format(text, value));
			if (str == null)
				str = ChatFormatting.ITALIC + "Format error";
			int x = (int) ((getWidth() - font.getStringWidth(str, fontOptions)) / 2);
			int y = (int) Math.ceil((getHeight() - font.getStringHeight(fontOptions)) / 2);

			renderer.drawText(font, str, x, y, 0, isHovered() ? hoveredFontOptions : fontOptions);
		}
	}

}
