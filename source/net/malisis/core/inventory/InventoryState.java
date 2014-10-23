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

package net.malisis.core.inventory;

/**
 * @author Ordinastie
 *
 */
public class InventoryState
{
	public static int PLAYER_INSERT = 1;
	public static int PLAYER_EXTRACT = 1 << 1;
	public static int AUTO_INSERT = 1 << 2;
	public static int AUTO_EXTRACT = 1 << 3;
	public static int FROZEN = 1 << 4;

	private int state = PLAYER_INSERT | PLAYER_EXTRACT | AUTO_INSERT | AUTO_EXTRACT;

	public int get()
	{
		return state;
	}

	public void set(int state)
	{
		this.state = state;
	}

	public void unset(int state)
	{
		this.state &= ~state;
	}

	public boolean is(int state)
	{
		return (this.state & state) != 0;
	}

	public void allowPlayer()
	{
		state |= PLAYER_INSERT | PLAYER_EXTRACT;
	}

	public void denyPlayer()
	{
		state &= ~(PLAYER_INSERT | PLAYER_EXTRACT);
	}

	public void allowAutomation()
	{
		state |= AUTO_INSERT | AUTO_EXTRACT;
	}

	public void denyAutomation()
	{
		state &= ~(AUTO_INSERT | AUTO_EXTRACT);
	}

	public void freeze()
	{
		state |= FROZEN;
	}

	public void unfreeze()
	{
		state &= ~FROZEN;
	}

}
