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

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.component.UIComponent;

/**
 * IControlledComponent are special components designed to affect other {@link UIComponent}.
 *
 * @author Ordinastie
 *
 */
public interface IControlComponent
{
	/**
	 * Sets the {@link UIComponent} controlled by this {@link IControlComponent}.
	 *
	 * @param component the parent
	 */
	public void setParent(UIComponent<?> component);

	/**
	 * Gets the {@link UIComponent} controlled by this {@link IControlComponent}.
	 *
	 * @return the parent
	 */
	public UIComponent<?> getParent();

	/**
	 * Gets the component at the specified coordinates. See {@link UIComponent#getComponentAt(int, int)}.
	 *
	 * @param x the x
	 * @param y the y
	 * @return the component at
	 */
	public UIComponent<?> getComponentAt(int x, int y);

	/**
	 * Called when a key is pressed when this {@link IControlComponent} or its parent is focused or hovered.<br>
	 * See {@link UIComponent#onKeyTyped(char, int)}.
	 *
	 * @param keyChar the key char
	 * @param keyCode the key code
	 * @return true, if successful
	 */
	public boolean onKeyTyped(char keyChar, int keyCode);

	/**
	 * Called when the scrollwheel is used when this {@link IControlComponent} or its parent is focused or hovered.<br>
	 * See {@link UIComponent#onScrollWheel(int, int, int)}
	 *
	 * @param x the x
	 * @param y the y
	 * @param delta the delta
	 * @return true, if successful
	 */
	public boolean onScrollWheel(int x, int y, int delta);

	/**
	 * Draws this {@link IControlComponent}. See {@link UIComponent#draw(GuiRenderer, int, int, float)}.
	 *
	 * @param renderer the renderer
	 * @param mouseX the mouse x
	 * @param mouseY the mouse y
	 * @param partialTick the partial tick
	 */
	public void draw(GuiRenderer renderer, int mouseX, int mouseY, float partialTick);
}
