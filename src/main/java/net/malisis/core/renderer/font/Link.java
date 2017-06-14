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

import java.awt.Desktop;
import java.net.URI;

import org.apache.commons.lang3.StringUtils;

import net.malisis.core.MalisisCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiConfirmOpenLink;
import net.minecraft.client.gui.GuiYesNoCallback;

/**
 * @author Ordinastie
 *
 */
public class Link implements GuiYesNoCallback
{
	private int index = 0;
	private int textIndex = 0;
	private String url;
	private String text;
	private boolean isValid;

	public Link(int index, String str)
	{
		this.index = index;
		int i = str.indexOf('|');
		if (i == -1)
		{
			url = str;
			text = str;
			textIndex = 1;
		}
		else
		{
			url = str.substring(0, i);
			text = str.substring(i + 1, str.length());
			textIndex = i + 1;
		}

		checkUrl();
	}

	public Link(int index, String url, String text)
	{
		this.index = index;
		this.url = url;
		this.text = text;
	}

	private void checkUrl()
	{
		isValid = true;
	}

	public float getWidth(MalisisFont font, FontOptions fro)
	{
		return font.getStringWidth(StringUtils.isEmpty(text) ? url : text, fro);
	}

	public int indexAdvance()
	{
		return text != null ? url.length() + 2 : 1;
	}

	public boolean isValid()
	{
		return isValid;
	}

	public boolean isUrl(int index)
	{
		index += this.index;
		return index >= 0 && index <= url.length();
	}

	public boolean isText(int index)
	{
		index += this.index;
		return index >= textIndex && index <= text.length();
	}

	public void click()
	{
		Minecraft.getMinecraft().displayGuiScreen(new GuiConfirmOpenLink(this, url, 0, false));
	}

	@Override
	public void confirmClicked(boolean result, int id)
	{
		if (result)
		{
			MalisisCore.message("Opening " + url);
			try
			{
				Desktop.getDesktop().browse(new URI(url));
			}
			catch (Throwable throwable)
			{
				MalisisCore.log.error("Couldn't open link", throwable);
			}
		}
		else
		{
			MalisisCore.message("Cancel");
		}
	}

	@Override
	public String toString()
	{
		return "[" + url + (text != null ? "|" + text : "") + "]";
	}

	public static Link getLink(String str, int index)
	{
		Link link = null;
		int i = index;
		while (i > 0)
		{
			link = FontOptions.getLink(str, i--);
			if (link != null && index < link.index + link.toString().length())
				return link;
		}

		return null;
	}
}
