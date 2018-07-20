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

package net.malisis.core.client.gui.render;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.element.size.Size.ISize;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

/**
 * The Class GuiTexture represents the textures loaded and to be drawn inside a {@link MalisisGui}.<br>
 * The textures can used from a {@link ResourceLocation} if the resource is inside the project, from a {@link File} or directly from a
 * {@link BufferedImage}.<br>
 * In case of {@code ResourceLocation}, the original dimension should be specified for {@link GuiIcon} with size specified in pixels to
 * work.
 *
 * @author Ordinastie
 */
public class GuiTexture implements ISize
{
	protected ResourceLocation resourceLocation;
	protected int width;
	protected int height;

	/**
	 * Instantiates a new {@link GuiTexture}.
	 *
	 * @param rl the rl
	 * @param width the width
	 * @param height the height
	 */
	public GuiTexture(ResourceLocation rl, int width, int height)
	{
		this.resourceLocation = rl;
		this.width = width;
		this.height = height;
	}

	/**
	 * Instantiates a new {@link GuiTexture}. <br>
	 * Automatically determines the width and height for the {@link File}.
	 *
	 * @param file the file
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public GuiTexture(File file) throws IOException
	{
		this(ImageIO.read(file), file.getName());
	}

	/**
	 * Instantiates a new {@link GuiTexture}.<br>
	 * Automatically determines the width and height for the {@link BufferedImage}.
	 *
	 * @param image the image
	 * @param name the name
	 */
	public GuiTexture(BufferedImage image, String name)
	{
		DynamicTexture dynTex = new DynamicTexture(image);
		width = image.getWidth();
		height = image.getHeight();
		resourceLocation = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation(name, dynTex);
	}

	/**
	 * Gets the width of this {@link GuiTexture}.
	 *
	 * @return the width
	 */
	@Override
	public int width()
	{
		return width;
	}

	/**
	 * Gets the height of this {@link GuiTexture}.
	 *
	 * @return the height
	 */
	@Override
	public int height()
	{
		return height;
	}

	/**
	 * Get the pixel position.
	 *
	 * @param u the u
	 * @return the pixel
	 */
	public int pixelFromU(float u)
	{
		return (int) (u * width);
	}

	/**
	 * Get the pixel position.
	 *
	 * @param v the v
	 * @return the pixel
	 */
	public int pixelFromV(float v)
	{
		return (int) (v * height);
	}

	/**
	 * Gets the UV factor from pixel
	 *
	 * @param px the px
	 * @return the float
	 */
	public float pixelToU(int px)
	{
		return (float) px / width;
	}

	/**
	 * Gets the UV factor from pixel.
	 *
	 * @param px the px
	 * @return the float
	 */
	public float pixelToV(int px)
	{
		return (float) px / height;
	}

	/**
	 * Gets the {@link ResourceLocation} of this {@link GuiTexture}.
	 *
	 * @return the resource location
	 */
	public ResourceLocation getResourceLocation()
	{
		return this.resourceLocation;
	}

	/**
	 * Deletes this texture from the {@link TextureManager}.
	 */
	public void delete()
	{
		Minecraft.getMinecraft().getTextureManager().deleteTexture(resourceLocation);
	}

	@Override
	public String toString()
	{
		return resourceLocation + " [" + width + ", " + height + "]";
	}
}