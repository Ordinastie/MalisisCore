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

package net.malisis.core.client.gui.component.control;

import com.google.common.eventbus.Subscribe;

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.element.SimpleGuiShape;
import net.malisis.core.client.gui.event.component.StateChangeEvent.HoveredStateChange;

/**
 * @author Ordinastie
 *
 */
public class UISlimScrollbar extends UIScrollBar
{
	/** Background color of the scroll. */
	protected int backgroundColor = 0x999999;
	/** Scroll color **/
	protected int scrollColor = 0xFFFFFF;
	/** Whether the scrollbar should fade in/out */
	protected boolean fade = true;

	public <T extends UIComponent<T> & IScrollable> UISlimScrollbar(MalisisGui gui, T parent, Type type)
	{
		super(gui, parent, type);
		setScrollSize(2, 15);
	}

	public void setFade(boolean fade)
	{
		this.fade = fade;
	}

	public boolean isFade()
	{
		return fade;
	}

	@Override
	protected void createShape(MalisisGui gui)
	{
		int w = type == Type.HORIZONTAL ? scrollHeight : scrollThickness;
		int h = type == Type.HORIZONTAL ? scrollThickness : scrollHeight;

		//background shape
		shape = new SimpleGuiShape();
		//scroll shape
		scrollShape = new SimpleGuiShape();
		scrollShape.setSize(w, h);
		scrollShape.storeState();
	}

	/**
	 * Sets the color of the scroll.
	 *
	 * @param scrollColor the new color
	 */
	@Override
	public void setColor(int scrollColor)
	{
		setColor(scrollColor, backgroundColor);
	}

	/**
	 * Sets the color of the scroll and the background.
	 *
	 * @param scrollColor the scroll color
	 * @param backgroundColor the background color
	 */
	public void setColor(int scrollColor, int backgroundColor)
	{
		this.scrollColor = scrollColor;
		this.backgroundColor = backgroundColor;
	}

	@Override
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		renderer.disableTextures();
		rp.colorMultiplier.set(backgroundColor);
		renderer.drawShape(shape, rp);
	}

	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		int ox = 0, oy = 0;
		int l = getLength() - scrollHeight;
		if (isHorizontal())
			ox = (int) (getOffset() * l);
		else
			oy = (int) (getOffset() * l);

		renderer.disableTextures();

		scrollShape.resetState();
		scrollShape.setPosition(ox, oy);
		rp.colorMultiplier.set(scrollColor);
		renderer.drawShape(scrollShape, rp);
	}

	@Subscribe
	public void onMouseOver(HoveredStateChange<?> event)
	{
		if (!fade)
			return;

		if (isFocused() && !event.getState())
			return;

		//int from = event.getState() ? 0 : 255;
		//int to = event.getState() ? 255 : 0;

		//Animation<ITransformable.Alpha> anim = new Animation<>(this, new AlphaTransform(from, to).forTicks(5));

		//event.getComponent().getGui().animate(anim);
	}
}
