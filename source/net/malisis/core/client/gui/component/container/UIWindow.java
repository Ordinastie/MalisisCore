/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 PaleoCrafter, Ordinastie
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

package net.malisis.core.client.gui.component.container;

import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.ClipArea;
import net.malisis.core.client.gui.GuiIcon;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.event.MouseEvent;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.preset.ShapePreset;

import com.google.common.eventbus.Subscribe;

/**
 * UIWindow
 * 
 * @author PaleoCrafter
 */
public class UIWindow extends UIContainer
{
	//@formatter:off
	public static GuiIcon[] icons = new GuiIcon[] { new GuiIcon(200, 	0, 		5, 	5),
													new GuiIcon(205, 	0, 		5, 	5),
													new GuiIcon(210, 	0, 		5, 	5),
													new GuiIcon(200, 	5, 		5, 	5),
													new GuiIcon(205, 	5, 		5, 	5),
													new GuiIcon(210, 	5, 		5, 	5),
													new GuiIcon(200, 	10, 	5, 	5),
													new GuiIcon(205, 	10, 	5, 	5),
													new GuiIcon(210, 	10, 	5, 	5)};
	//@formatter:on

	public UIWindow(String title, int width, int height, int anchor)
	{
		super(title, width, height);
		setPadding(5, 5);
		this.anchor = anchor;
	}

	public UIWindow(String title, int width, int height)
	{
		this(title, width, height, Anchor.CENTER | Anchor.MIDDLE);
	}

	public UIWindow(int width, int height)
	{
		this(null, width, height, Anchor.CENTER | Anchor.MIDDLE);
	}

	@Override
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		Shape shape = ShapePreset.GuiXYResizable(width, height);
		renderer.drawShape(shape, icons);
	}

	@Subscribe
	public void onMouseMove(MouseEvent.Move event)
	{
		// MalisisCore.Message(event.getX() + ", " + event.getY());
	}

	@Override
	public ClipArea getClipArea()
	{
		return new ClipArea(this, 3);
	}

}
