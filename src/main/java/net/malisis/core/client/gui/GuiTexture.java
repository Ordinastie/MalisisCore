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

package net.malisis.core.client.gui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import net.malisis.core.client.gui.icon.GuiIcon;
import net.malisis.core.renderer.icon.MalisisIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

/**
 * The Class GuiTexture represents the textures loaded and to be drawn inside a {@link MalisisGui}.<br>
 * The textures can used from a {@link ResourceLocation} if the resource is inside the project, from a {@link File} or directly from a
 * {@link BufferedImage}.<br>
 * In case of {@code ResourceLocation}, the original dimension should be specified if parts of the texture is to be retrieved as
 * {@link MalisisIcon}.
 *
 * @author Ordinastie
 */
public class GuiTexture
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
	 * Instantiates a new {@link GuiTexture}.
	 *
	 * @param rl the rl
	 */
	public GuiTexture(ResourceLocation rl)
	{
		this(rl, 1, 1);
	}

	/**
	 * Gets the width of this {@link GuiTexture}.
	 *
	 * @return the width
	 */
	public int getWidth()
	{
		return width;
	}

	/**
	 * Gets the height of this {@link GuiTexture}.
	 *
	 * @return the height
	 */
	public int getHeight()
	{
		return height;
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
	 * Creates the {@link MalisisIcon} and initializes it.
	 *
	 * @param x the x
	 * @param y the y
	 * @param width the width
	 * @param height the height
	 * @return the {@link MalisisIcon}
	 */
	private MalisisIcon createIcon(int x, int y, int width, int height)
	{
		MalisisIcon icon = new MalisisIcon();
		icon.setSize(width, height);
		icon.initSprite(this.width, this.height, x, y, false);

		return icon;
	}

	/**
	 * Gets a {@link GuiIcon} from a single icon (used for single face shapes).
	 *
	 * @param x the x
	 * @param y the y
	 * @param width the width
	 * @param height the height
	 * @return the icon
	 */
	public GuiIcon getIcon(int x, int y, int width, int height)
	{
		return new GuiIcon(createIcon(x, y, width, height));
	}

	/**
	 * Gets a {@link GuiIcon} that is resizable from both X and Y axis.<br>
	 * The {@code GuiIcon} will hold 9 icons that will behave when resized :<br>
	 * - the top and bottom row will not change in height when resized<br>
	 * - left left and right row will not change in width when resized<br>
	 *
	 * @param x the x
	 * @param y the y
	 * @param width the width
	 * @param height the height
	 * @param corner the corner
	 * @return the XY resizable icon
	 */
	public GuiIcon getXYResizableIcon(int x, int y, int width, int height, int corner)
	{
		int w = width - corner * 2;
		int h = height - corner * 2;

		//@formatter:off
		MalisisIcon[] icons = new MalisisIcon[] {
				createIcon(x, 					y, 					corner, 	corner),
				createIcon(x + corner, 		y, 					w, 			corner),
				createIcon(x + corner + w, 	y, 					corner, 	corner),

				createIcon(x, 					y + corner, 		corner, 	h),
				createIcon(x + corner, 		y + corner, 		w, 			h),
				createIcon(x + corner + w, 	y + corner, 		corner, 	h),

				createIcon(x, 					y + corner + h, 	corner, 	corner),
				createIcon(x + corner, 		y + corner + h, 	w, 			corner),
				createIcon(x + corner + w, 	y + corner + h, 	corner, 	corner),
		};
		//@formatter:on

		return new GuiIcon(icons);
	}

	/**
	 * Gets a {@link GuiIcon} that is resizable only on the X axis.<br>
	 * The {@code GuiIcon} will hold 3 icons that will behave when resized :<br>
	 * - left left and right icon will not change in width when resized<br>
	 *
	 * @param x the x
	 * @param y the y
	 * @param width the width
	 * @param height the height
	 * @param side the side
	 * @return the x resizable icon
	 */
	public GuiIcon getXResizableIcon(int x, int y, int width, int height, int side)
	{
		int w = width - side * 2;
		int h = height;

		//@formatter:off
		MalisisIcon[] icons = new MalisisIcon[] {
				createIcon(x, 				y, 		side, 	h),
				createIcon(x + side, 		y, 		w, 		h),
				createIcon(x + side + w, 	y, 		side, 	h),
		};
		//@formatter:on

		return new GuiIcon(icons);
	}

	@Override
	public String toString()
	{
		return resourceLocation + " [" + width + ", " + height + "]";
	}
}
