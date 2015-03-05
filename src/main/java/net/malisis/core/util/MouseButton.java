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

package net.malisis.core.util;

/**
 * MouseButton
 *
 * @author PaleoCrafter
 */
public enum MouseButton
{
	LEFT(0), RIGHT(1), MIDDLE(2), UNKNOWN(-1);

	public static MouseButton[] DEFAULT = { LEFT, RIGHT, MIDDLE };

	private int code;

	private MouseButton(int code)
	{
		this.code = code;
	}

	/**
	 * Gets the code for this {@link MouseButton}.
	 *
	 * @return the code
	 */
	public int getCode()
	{
		return code;
	}

	/**
	 * Gets the {@link MouseButton} from the event code.
	 *
	 * @param id the id
	 * @return the button
	 */
	public static MouseButton getButton(int id)
	{
		if (id < DEFAULT.length && id >= 0)
			return DEFAULT[id];
		return UNKNOWN;
	}
}
