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

import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.element.SimpleGuiShape;
import net.malisis.core.client.gui.event.component.StateChangeEvent.HoveredStateChange;
import net.malisis.core.renderer.animation.Animation;
import net.malisis.core.renderer.animation.transformation.AlphaTransform;

import com.google.common.eventbus.Subscribe;

/**
 * @author Ordinastie
 *
 */
public class UISlimScrollbar extends UIScrollBar
{
	public <T extends UIComponent & IScrollable> UISlimScrollbar(MalisisGui gui, T parent, Type type)
	{
		super(gui, parent, type);
	}

	@Override
	protected void setPosition()
	{
		int vp = getScrollable().getVerticalPadding();
		int hp = getScrollable().getHorizontalPadding();

		if (type == Type.HORIZONTAL)
			setPosition(hp, -vp, Anchor.BOTTOM);
		else
			setPosition(-hp, vp, Anchor.RIGHT);
	}

	@Override
	protected void createShape(MalisisGui gui)
	{
		scrollThickness = 2;
		scrollHeight = 15;

		int w = type == Type.HORIZONTAL ? scrollHeight : scrollThickness;
		int h = type == Type.HORIZONTAL ? scrollThickness : scrollHeight;

		//background shape
		shape = new SimpleGuiShape();
		//scroller shape
		scrollShape = new SimpleGuiShape();
		scrollShape.setSize(w, h);
		scrollShape.storeState();
	}

	@Override
	public int getWidth()
	{
		int w = super.getWidth();
		if (type == Type.HORIZONTAL)
			w -= 2 * getScrollable().getHorizontalPadding();
		return w;
	}

	@Override
	public int getHeight()
	{
		int h = super.getHeight();
		if (type == Type.VERTICAL)
			h -= 2 * getScrollable().getVerticalPadding();
		return h;

	}

	@Override
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		renderer.disableTextures();
		rp.colorMultiplier.set(0x999999);
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
		rp.colorMultiplier.set(0xFFFFFF);
		renderer.drawShape(scrollShape, rp);
	}

	@Subscribe
	public void onMouseOver(HoveredStateChange event)
	{
		if (isFocused() && !event.getState())
			return;

		int from = event.getState() ? 0 : 255;
		int to = event.getState() ? 255 : 0;

		Animation anim = new Animation(this, new AlphaTransform(from, to).forTicks(5));

		event.getComponent().getGui().animate(anim);

	}
}
