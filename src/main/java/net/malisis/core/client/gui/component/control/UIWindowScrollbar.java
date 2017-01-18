/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Ordinastie
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

import java.util.function.Supplier;

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.element.GuiIcon;
import net.malisis.core.client.gui.element.GuiShape;
import net.malisis.core.client.gui.element.GuiShape.ShapePosition;
import net.malisis.core.client.gui.element.GuiShape.ShapeSize;

/**
 * @author Ordinastie
 *
 */
public final class UIWindowScrollbar extends UIScrollBar<UIWindowScrollbar>
{
	private ShapeSize scrollSize;
	private Supplier<GuiIcon> scrollIcon;

	public final GuiShape background = GuiShape	.builder()
												.forComponent(this)
												.icon(GuiIcon.forComponent(this, GuiIcon.SCROLLBAR_BG, null, GuiIcon.SCROLLBAR_DISABLED_BG))
												.build();

	public final GuiShape scroll = GuiShape.builder().position(this::getScrollPosition).size(scrollSize).icon(scrollIcon).build();

	public <T extends UIComponent<T> & IScrollable> UIWindowScrollbar(T parent, Type type)
	{
		super(parent, type);
		if (isHorizontal())
		{
			scrollSize = ShapeSize.of(scrollHeight, scrollThickness);
			scrollIcon = GuiIcon.forComponent(this, GuiIcon.SCROLLBAR_HORIZONTAL, null, GuiIcon.SCROLLBAR_HORIZONTAL_DISABLED);
		}
		else
		{
			scrollSize = ShapeSize.of(scrollThickness, scrollHeight);
			scrollIcon = GuiIcon.forComponent(this, GuiIcon.SCROLLBAR_VERTICAL, null, GuiIcon.SCROLLBAR_VERTICAL_DISABLED);
		}
	}

	public ShapePosition getScrollPosition()
	{
		int l = getLength() - scrollHeight - 2;
		int ox = isHorizontal() ? (int) (getOffset() * l) : 1;
		int oy = isHorizontal() ? 1 : (int) (getOffset() * l);
		return ShapePosition.of(ox, oy);

	}

	@Override
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		background.render();
	}

	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		scroll.render();
	}

}
