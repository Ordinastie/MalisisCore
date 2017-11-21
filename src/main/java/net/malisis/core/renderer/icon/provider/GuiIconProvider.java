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
import net.malisis.core.renderer.icon.Icon;

/**
 * @author Ordinastie
 *
 */
public class GuiIconProvider implements IGuiIconProvider
{
	protected Icon icon;
	protected Icon hoveredIcon;
	protected Icon disabledIcon;

	public GuiIconProvider(GuiIcon icon)
	{
		setIcon(icon);
	}

	public GuiIconProvider(Icon icon, Icon hoveredIcon, Icon disabledIcon)
	{
		setIcon(icon);
		setHoveredIcon(hoveredIcon);
		setDisabledIcon(disabledIcon);
	}

	public void setIcon(Icon icon)
	{
		this.icon = icon;
	}

	public void setHoveredIcon(Icon icon)
	{
		this.hoveredIcon = icon;
	}

	public void setDisabledIcon(Icon icon)
	{
		this.disabledIcon = icon;
	}

	@Override
	public Icon getIcon()
	{
		return icon;
	}

	@Override
	public Icon getIcon(UIComponent<?> component)
	{
		if (!component.isEnabled())
			return disabledIcon != null ? disabledIcon : icon;
		if (component.isHovered())
			return hoveredIcon != null ? hoveredIcon : icon;
		return icon;
	}

}
