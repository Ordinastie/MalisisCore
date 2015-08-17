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

import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.renderer.icon.GuiIcon;
import net.malisis.core.renderer.icon.MalisisIcon;

/**
 * @author Ordinastie
 *
 */
public class GuiIconProvider implements IGuiIconProvider
{
	protected MalisisIcon icon;
	protected MalisisIcon hoveredIcon;
	protected MalisisIcon disabledIcon;

	public GuiIconProvider(GuiIcon icon)
	{
		setIcon(icon);
	}

	public GuiIconProvider(MalisisIcon icon, MalisisIcon hoveredIcon, MalisisIcon disabledIcon)
	{
		setIcon(icon);
		setHoveredIcon(hoveredIcon);
		setDisabledIcon(disabledIcon);
	}

	public void setIcon(MalisisIcon icon)
	{
		this.icon = icon;
	}

	public void setHoveredIcon(MalisisIcon icon)
	{
		this.hoveredIcon = icon;
	}

	public void setDisabledIcon(MalisisIcon icon)
	{
		this.disabledIcon = icon;
	}

	@Override
	public MalisisIcon getIcon()
	{
		return icon;
	}

	@Override
	public MalisisIcon getIcon(UIComponent<?> component)
	{
		if (component.isDisabled())
			return disabledIcon != null ? disabledIcon : icon;
		if (component.isHovered())
			return hoveredIcon != null ? hoveredIcon : icon;
		return icon;
	}

}
