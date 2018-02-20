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

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.element.Size;
import net.malisis.core.client.gui.component.element.Size.ISize;
import net.malisis.core.client.gui.element.SimpleGuiShape;
import net.malisis.core.renderer.icon.Icon;
import net.malisis.core.renderer.icon.provider.GuiIconProvider;
import net.minecraft.item.ItemStack;

/**
 * UIImage.
 *
 * @author Ordinastie
 */
public class UIImage extends UIComponent<UIImage>
{
	/** Fixed size of ItemStack UIImages. */
	private final ISize ITEMSTACK_SIZE = Size.of(16, 16);
	/** {@link GuiTexture} to use for the icon. */
	private GuiTexture texture;
	/** {@link Icon} to use for the texture. */
	private Icon icon = null;
	/** {@link ItemStack} to render. */
	private ItemStack itemStack;

	/**
	 * Instantiates a new {@link UIImage}.
	 *
	 * @param gui the gui
	 * @param texture the texture
	 * @param icon the icon
	 */
	public UIImage(MalisisGui gui, GuiTexture texture, Icon icon)
	{
		super(gui);
		iconProvider = new GuiIconProvider(null);
		setIcon(texture, icon);
		setSize(ITEMSTACK_SIZE);

		shape = new SimpleGuiShape();
		iconProvider = new GuiIconProvider(null);
	}

	/**
	 * Instantiates a new {@link UIImage}.
	 *
	 * @param gui the gui
	 * @param itemStack the item stack
	 */
	public UIImage(MalisisGui gui, ItemStack itemStack)
	{
		super(gui);

		iconProvider = new GuiIconProvider(null);

		setItemStack(itemStack);
		setSize(ITEMSTACK_SIZE);

		shape = new SimpleGuiShape();
	}

	/**
	 * Sets the icon for this {@link UIImage}.
	 *
	 * @param icon the icon
	 * @return this UIImage
	 */
	public UIImage setIcon(Icon icon)
	{
		this.itemStack = null;
		this.icon = icon != null ? icon : new Icon();
		return this;
	}

	/**
	 * Sets the icon for this {@link UIImage} to be used with the specified {@link GuiTexture}.
	 *
	 * @param texture the texture
	 * @param icon the icon
	 * @return this UIImage
	 */
	public UIImage setIcon(GuiTexture texture, Icon icon)
	{
		this.itemStack = null;
		this.icon = icon != null ? icon : new Icon();
		this.texture = texture;
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
		this.texture = null;
		this.itemStack = itemStack;
		setSize(ITEMSTACK_SIZE);
		return this;
	}

	/**
	 * Gets the {@link Icon} for this {@link UIImage}.
	 *
	 * @return the icon
	 */
	public Icon getIcon()
	{
		return iconProvider.getIcon();
	}

	/**
	 * Gets the {@link GuiTexture} for this {@link UIImage}.
	 *
	 * @return the texture
	 */
	public GuiTexture getTexture()
	{
		return texture;
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
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{}

	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		if (icon != null)
		{
			((GuiIconProvider) iconProvider).setIcon(icon);
			renderer.bindTexture(texture);
			renderer.drawShape(shape, rp);
		}
		else if (itemStack != null)
		{
			renderer.drawItemStack(itemStack);
		}
	}

	@Override
	public String getPropertyString()
	{
		return (itemStack != null ? itemStack : ("texture : " + this.texture + ", " + " icon : " + icon)) + super.getPropertyString();
	}

}
