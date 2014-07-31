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

import net.malisis.core.client.gui.GuiIcon;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.malisis.core.client.gui.event.MouseEvent;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.preset.ShapePreset;
import net.malisis.core.util.MouseButton;

import com.google.common.eventbus.Subscribe;

/**
 * @author Ordinastie
 * 
 */
public class UISlider extends UIComponent<UISlider>
{
	public static int SLIDER_WIDTH = 8;
	//@formatter:off
	public static GuiIcon[] iconBackground = new GuiIcon[] { 	new GuiIcon(0, 		0, 		5, 		20), 
																new GuiIcon(5, 		0, 		15, 	20),
																new GuiIcon(195, 	0, 		5, 		20)};
	public static GuiIcon sliderIcon = new GuiIcon(227, 46, 8, 20);
	//@formatter:on

	private String label;
	private float minValue;
	private float maxValue;
	private float value;
	private float offset;

	public UISlider(int width, float min, float max, String label)
	{
		this.height = 20;
		this.width = width;
		this.minValue = min;
		this.maxValue = max;
		this.label = label;
	}

	public UISlider(int width, float min, float max)
	{
		this(width, min, max, null);
	}

	@Subscribe
	public void onClick(MouseEvent.Press event)
	{
		if (event.getButton() != MouseButton.LEFT)
			return;

		int l = width - SLIDER_WIDTH;
		int pos = componentX(event.getX());
		pos = Math.max(0, Math.min(pos - SLIDER_WIDTH / 2, l));
		slideTo((float) pos / l);
	}

	@Subscribe
	public void onScrollWheel(MouseEvent.ScrollWheel event)
	{
		if (isFocused())
			slideBy(event.getDelta());
	}

	@Subscribe
	public void onDrag(MouseEvent.Drag event)
	{
		if (event.getButton() != MouseButton.LEFT)
			return;

		int l = width - SLIDER_WIDTH;
		int pos = componentX(event.getX());
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
		if (fireEvent(new ComponentEvent.ValueChanged(this, oldValue, newValue)))
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
		Shape shape = ShapePreset.GuiXResizable(width, 20);
		renderer.drawShape(shape, iconBackground);
	}

	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		zIndex = 0;
		int ox = (int) (offset * (width - SLIDER_WIDTH));

		Shape shape = ShapePreset.GuiElement(8, 20);
		shape.translate(ox, 0, 0);

		renderer.drawShape(shape, sliderIcon);

		renderer.next();
		//zIndex = 1;

		if (label != null)
		{
			String str = String.format(label, value);
			int x = (width - GuiRenderer.getStringWidth(str)) / 2;
			int y = 6;
			renderer.drawText(str, x, y, isHovered() ? 0xFFFFA0 : 0xFFFFFF, true);
		}
	}

}
