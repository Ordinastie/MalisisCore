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

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.IGuiText;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.element.GuiShape;
import net.malisis.core.client.gui.element.SimpleGuiShape;
import net.malisis.core.client.gui.element.XResizableGuiShape;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.renderer.font.MalisisFont;
import net.malisis.core.renderer.icon.provider.GuiIconProvider;
import net.malisis.core.util.MouseButton;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Ordinastie
 *
 */
public class UISlider extends UIComponent<UISlider> implements IGuiText<UISlider>
{
	public static int SLIDER_WIDTH = 8;

	protected GuiIconProvider sliderIcon;

	/** The {@link MalisisFont} to use for this {@link UISlider}. */
	protected MalisisFont font = MalisisFont.minecraftFont;
	/** The {@link FontOptions} to use for this {@link UISlider}. */
	protected FontOptions fontOptions = FontOptions.builder().color(0xFFFFFF).shadow().build();
	/** The {@link FontOptions} to use for this {@link UISlider} when hovered. */
	protected FontOptions hoveredFontOptions = FontOptions.builder().color(0xFFFFA0).shadow().build();

	private String text;
	private float minValue;
	private float maxValue;
	private float value;
	private float offset;

	private GuiShape sliderShape;

	public UISlider(MalisisGui gui, int width, float min, float max, String text)
	{
		super(gui);
		this.text = text;

		setSize(width, 20);

		minValue = min;
		maxValue = max;

		shape = new XResizableGuiShape();
		sliderShape = new SimpleGuiShape();
		sliderShape.setSize(8, 20);
		sliderShape.storeState();

		iconProvider = new GuiIconProvider(gui.getGuiTexture().getXResizableIcon(0, 0, 200, 20, 5));
		sliderIcon = new GuiIconProvider(gui.getGuiTexture().getIcon(227, 46, 8, 20));

	}

	public UISlider(MalisisGui gui, int width, float min, float max)
	{
		this(gui, width, min, max, null);
	}

	//#region Getters/Setters
	@Override
	public MalisisFont getFont()
	{
		return font;
	}

	@Override
	public UISlider setFont(MalisisFont font)
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
	public UISlider setFontOptions(FontOptions fro)
	{
		this.fontOptions = fro;
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
		slideBy(delta);
		return true;
	}

	@Override
	public boolean onDrag(int lastX, int lastY, int x, int y, MouseButton button)
	{
		slideTo(x);
		return true;
	}

	public void slideTo(int x)
	{
		int l = width - SLIDER_WIDTH;
		int pos = relativeX(x);
		pos = Math.max(0, Math.min(pos - SLIDER_WIDTH / 2, l));
		slideTo((float) pos / l);
	}

	public void slideTo(float offset)
	{
		if (isDisabled())
			return;

		if (offset < 0)
			offset = 0;
		if (offset > 1)
			offset = 1;

		this.offset = offset;
		float oldValue = this.value;
		float newValue = minValue + (maxValue - minValue) * offset;
		if (fireEvent(new ComponentEvent.ValueChange<>(this, oldValue, newValue)))
			value = newValue;
	}

	public void slideBy(float amount)
	{
		amount *= 0.05F;
		slideTo(offset + amount);
	}

	@Override
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		renderer.drawShape(shape, rp);
	}

	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		zIndex = 0;
		int ox = (int) (offset * (width - SLIDER_WIDTH));

		sliderShape.resetState();
		sliderShape.setPosition(ox, 0);

		rp.iconProvider.set(sliderIcon);
		renderer.drawShape(sliderShape, rp);

		renderer.next();
		//zIndex = 1;

		if (!StringUtils.isEmpty(text))
		{
			String str = String.format(text, value);
			int x = (int) ((width - font.getStringWidth(str, fontOptions)) / 2);
			int y = 6;

			renderer.drawText(font, str, x, y, 0, isHovered() ? hoveredFontOptions : fontOptions);
		}
	}

}
