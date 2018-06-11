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

package net.malisis.core.client.gui.element;

import net.malisis.core.client.gui.MalisisGui;

/**
 * That interfaces allows implementing classes to handle key strokes within a {@link MalisisGui}.<br>
 * {@link IKeyListener} can be registered with {@link MalisisGui#registerKeyListener(IKeyListener)} so they will always receive key typed.
 *
 * @author Ordinastie
 */
public interface IKeyListener
{
	/**
	 * Called when a key is typed inside {@link MalisisGui}.
	 *
	 * @param keyChar the key char
	 * @param keyCode the key code
	 * @return true, to prevent parents and gui to handle the key typed
	 */
	public boolean onKeyTyped(char keyChar, int keyCode);
}
