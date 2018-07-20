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

package net.malisis.core.client.gui.component.scrolling;

import java.util.function.Supplier;

import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.control.IScrollable;
import net.malisis.core.client.gui.element.size.Size;
import net.malisis.core.client.gui.render.GuiIcon;
import net.malisis.core.client.gui.render.shape.GuiShape;

/**
 * @author Ordinastie
 *
 */
public final class UIWindowScrollbar extends UIScrollBar
{
	public <T extends UIComponent & IScrollable> UIWindowScrollbar(T parent, UIScrollBar.Type type)
	{
		super(parent, type);
		Supplier<GuiIcon> scrollIcon;
		if (isHorizontal())
		{
			scrollSize = Size.of(15, 10);
			scrollIcon = GuiIcon.forComponent(this, GuiIcon.SCROLLBAR_HORIZONTAL, null, GuiIcon.SCROLLBAR_HORIZONTAL_DISABLED);
		}
		else
		{
			scrollSize = Size.of(10, 15);
			scrollIcon = GuiIcon.forComponent(this, GuiIcon.SCROLLBAR_VERTICAL, null, GuiIcon.SCROLLBAR_VERTICAL_DISABLED);
		}

		setBackground(GuiShape	.builder(this)
								.icon(GuiIcon.forComponent(this, GuiIcon.SCROLLBAR_BG, null, GuiIcon.SCROLLBAR_DISABLED_BG))
								.border(3)
								.build());
		setForeground(GuiShape.builder(this).position(scrollPosition).size(scrollSize).icon(scrollIcon).build());
	}
}