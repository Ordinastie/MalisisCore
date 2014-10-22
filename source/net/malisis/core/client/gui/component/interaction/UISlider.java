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
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.element.GuiShape;
import net.malisis.core.client.gui.element.SimpleGuiShape;
import net.malisis.core.client.gui.element.XResizableGuiShape;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.malisis.core.client.gui.event.MouseEvent;
import net.malisis.core.client.gui.icon.GuiIcon;
import net.malisis.core.util.MouseButton;

import com.google.common.eventbus.Subscribe;

/**
 * @author Ordinastie
 *
 */
public class UISlider extends UIComponent<UISlider>
{
	public static int SLIDER_WIDTH = 8;

	protected GuiIcon iconBackground;
	protected GuiIcon sliderIcon;

	private String label;
	private float minValue;
	private float maxValue;
	private float value;
	private float offset;

	private GuiShape sliderShape;

	public UISlider(MalisisGui gui, int width, float min, float max, String label)
	{
		super(gui);
		setSize(width, 20);
		this.minValue = min;
		this.maxValue = max;
		this.label = label;

		shape = new XResizableGuiShape();
		sliderShape = new SimpleGuiShape();
		sliderShape.setSize(8, 20);
		sliderShape.storeState();

		iconBackground = gui.getGuiTexture().getXResizableIcon(0, 0, 200, 20, 5);
		sliderIcon = gui.getGuiTexture().getIcon(227, 46, 8, 20);

	}

	public UISlider(MalisisGui gui, int width, float min, float max)
	{
		this(gui, width, min, max, null);
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
		rp.icon.set(iconBackground);
		renderer.drawShape(shape, rp);
	}

	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		zIndex = 0;
		int ox = (int) (offset * (width - SLIDER_WIDTH));

		sliderShape.resetState();
		sliderShape.setPosition(ox, 0);

		rp.icon.set(sliderIcon);
		renderer.drawShape(sliderShape, rp);

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
