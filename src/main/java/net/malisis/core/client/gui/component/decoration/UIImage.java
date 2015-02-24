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
import net.malisis.core.client.gui.element.SimpleGuiShape;
import net.malisis.core.renderer.icon.MalisisIcon;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

/**
 * UIImage.
 *
 * @author Ordinastie
 */
public class UIImage extends UIComponent<UIImage>
{
	/** {@link ResourceLocation} for Block textures. */
	public static final ResourceLocation BLOCKS_TEXTURE = TextureMap.locationBlocksTexture;
	/** {@link ResourceLocation} for Item textures. */
	public static final ResourceLocation ITEMS_TEXTURE = TextureMap.locationItemsTexture;

	/** {@link GuiTexture} to use for the icon. */
	private GuiTexture texture;
	/** {@link IIcon} to use for the texture. */
	private IIcon icon = null;
	/** {@link ItemStack} to render. */
	private ItemStack itemStack;

	/**
	 * Instantiates a new {@link UIImage}.
	 *
	 * @param gui the gui
	 * @param texture the texture
	 * @param icon the icon
	 */
	public UIImage(MalisisGui gui, GuiTexture texture, IIcon icon)
	{
		super(gui);
		setIcon(texture, icon);
		setSize(16, 16);

		shape = new SimpleGuiShape();
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
		setItemStack(itemStack);
		setSize(16, 16);

		shape = new SimpleGuiShape();
	}

	/**
	 * Sets the icon for this {@link UIImage}.
	 *
	 * @param icon the icon
	 * @return this UIImage
	 */
	public UIImage setIcon(IIcon icon)
	{
		this.itemStack = null;
		this.icon = icon != null ? icon : new MalisisIcon();
		return this;
	}

	/**
	 * Sets the icon for this {@link UIImage} to be used with the specified {@link GuiTexture}.
	 *
	 * @param texture the texture
	 * @param icon the icon
	 * @return this UIImage
	 */
	public UIImage setIcon(GuiTexture texture, IIcon icon)
	{
		this.itemStack = null;
		this.icon = icon != null ? icon : new MalisisIcon();
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
		setSize(16, 16);
		return this;
	}

	/**
	 * Gets the {@link IIcon} for this {@link UIImage}.
	 *
	 * @return the icon
	 */
	public IIcon getIcon()
	{
		return icon;
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
	 * @param width the width
	 * @param height the height
	 * @return the UI image
	 */
	@Override
	public UIImage setSize(int width, int height)
	{
		if (itemStack != null)
		{
			width = 16;//UIImage for itemStack have a fixed 16*16 size
			height = 16;
		}
		return super.setSize(width, height);
	}

	@Override
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{}

	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		if (icon != null)
		{
			rp.icon.set(icon);
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
