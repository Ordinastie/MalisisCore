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

package net.malisis.core.renderer.font;

import java.awt.Font;

/**
 * @author Ordinastie
 *
 */
public class FontGeneratorOptions
{
	public static FontGeneratorOptions DEFAULT = new FontGeneratorOptions();

	/** Type of font **/
	public int fontType = Font.TRUETYPE_FONT;
	/** Size of font **/
	public float fontSize = 128F;
	/** Whether to use AA for the rendering of the font **/
	public boolean antialias = false;
	/** Offset on the left of characters **/
	public float mx = 0;
	/** Offset on the right of characters **/
	public float px = 0;
	/** Offset on the top of characters **/
	public float my = 0;
	/** Offset on the bottom of characters **/
	public float py = 0;

	/** Whether to generate debug data in the texture **/
	public boolean debug;
}
