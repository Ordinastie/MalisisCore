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

package net.malisis.core.client.gui.component.decoration;

import org.apache.commons.lang3.ObjectUtils;

import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.element.size.Size;
import net.malisis.core.client.gui.element.size.Size.ISize;
import net.malisis.core.client.gui.render.GuiIcon;
import net.malisis.core.client.gui.render.IGuiRenderer;
import net.malisis.core.client.gui.render.shape.GuiShape;
import net.malisis.core.renderer.icon.Icon;
import net.minecraft.item.ItemStack;

/**
 * UIImage.
 *
 * @author Ordinastie
 */
public class UIImage extends UIComponent
{
	/** Fixed size of ItemStack UIImages. */
	private final ISize ITEMSTACK_SIZE = Size.of(16, 16);
	/** {@link GuiIcon} to use for the texture. */
	private GuiIcon icon = null;
	/** {@link ItemStack} to render. */
	private ItemStack itemStack;

	private final IGuiRenderer ICON_RENDER = GuiShape.builder(this).icon(this::getIcon).build();
	private final IGuiRenderer IS_RENDER = (r) -> r.drawItemStack(itemStack);

	/**
	 * Instantiates a new {@link UIImage}.
	 * 
	 * @param icon the icon
	 */
	public UIImage(GuiIcon icon)
	{
		setIcon(icon);
		setSize(ITEMSTACK_SIZE);
	}

	/**
	 * Instantiates a new {@link UIImage}.
	 * 
	 * @param itemStack the item stack
	 */
	public UIImage(ItemStack itemStack)
	{
		setItemStack(itemStack);
	}

	/**
	 * Sets the icon for this {@link UIImage}.
	 *
	 * @param icon the icon
	 * @return this UIImage
	 */
	public UIImage setIcon(GuiIcon icon)
	{
		this.itemStack = null;
		this.icon = icon;
		setForeground(ICON_RENDER);
		return this;
	}

	/**
	 * Sets the {@link ItemStack} to render.
	 *
	 * @param itemStack the item stack
	 * @return this UIImage
	 */
	public UIImage setItemStack(ItemStack itemStack)
	{
		this.icon = null;
		this.itemStack = itemStack;
		setSize(ITEMSTACK_SIZE);
		setForeground(IS_RENDER);
		return this;
	}

	/**
	 * Gets the {@link Icon} for this {@link UIImage}.
	 *
	 * @return the icon
	 */
	public GuiIcon getIcon()
	{
		return icon;
	}

	/**
	 * Gets the {@link ItemStack} for this {@link UIImage}.
	 *
	 * @return the item stack
	 */
	public ItemStack getItemStack()
	{
		return itemStack;
	}

	/**
	 * Sets the size for this {@link UIImage}.<br>
	 * Has no effect if rendering an {@link ItemStack}.
	 *
	 * @param size the new size
	 */
	@Override
	public void setSize(ISize size)
	{
		//UIImage for itemStack have a fixed 16*16 size
		if (itemStack != null)
			size = ITEMSTACK_SIZE;
		super.setSize(size);
	}

	@Override
	public String getPropertyString()
	{
		return ObjectUtils.firstNonNull(itemStack, icon) + " " + super.getPropertyString();
	}

}
