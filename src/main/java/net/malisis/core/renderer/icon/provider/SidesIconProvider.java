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

package net.malisis.core.renderer.icon.provider;

import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.icon.MalisisIcon;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumFacing;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Ordinastie
 *
 */
public class SidesIconProvider extends DefaultIconProvider
{
	private String[] names = new String[6];
	private MalisisIcon[] icons = new MalisisIcon[6];

	public SidesIconProvider(String defaultName, String[] iconNames)
	{
		super(defaultName);
		setIcons(iconNames);
	}

	@Override
	public void registerIcons(TextureMap map)
	{
		super.registerIcons(map);
		if (ArrayUtils.isEmpty(names))
			return;

		for (int i = 0; i < names.length; i++)
		{
			if (!StringUtils.isEmpty(names[i]))
			{
				icons[i] = new MalisisIcon(names[i]);
				icons[i].register(map);
			}
		}
	}

	public void setIcons(String[] names)
	{
		this.names = names;
	}

	public void setIcons(MalisisIcon[] icons)
	{
		this.icons = icons;
	}

	public MalisisIcon getIcon(EnumFacing dir)
	{
		if (dir == null || dir.getIndex() > icons.length)
			return null;

		return icons[dir.getIndex()];
	}

	@Override
	public MalisisIcon getIcon(Face face)
	{
		EnumFacing dir = face.getParameters().textureSide.get();
		if (dir == null)
			return icon;
		MalisisIcon dirIcon = getIcon(dir);
		return dirIcon != null ? dirIcon : icon;
	}
}
