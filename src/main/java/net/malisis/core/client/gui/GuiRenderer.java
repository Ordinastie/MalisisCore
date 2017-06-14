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

package net.malisis.core.client.gui;

import org.apache.logging.log4j.util.Strings;
import org.lwjgl.opengl.GL11;

import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.decoration.UITooltip;
import net.malisis.core.client.gui.element.GuiShape;
import net.malisis.core.client.gui.element.SimpleGuiShape;
import net.malisis.core.renderer.MalisisRenderer;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.RenderType;
import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.renderer.font.MalisisFont;
import net.malisis.core.renderer.icon.GuiIcon;
import net.malisis.core.renderer.icon.Icon;
import net.malisis.core.renderer.icon.provider.IGuiIconProvider;
import net.malisis.core.renderer.icon.provider.IIconProvider;
import net.malisis.core.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;

/**
 * Renderer to use for {@link MalisisGui}.
 *
 * @author Ordinastie
 *
 */
public class GuiRenderer extends MalisisRenderer<TileEntity>
{
	/** RenderItem used to draw itemStacks. */
	public static RenderItem itemRenderer = Minecraft.getMinecraft().getRenderItem();
	/** Current component being drawn. */
	public UIComponent<?> currentComponent;
	/** Multiplying factor between GUI size and pixel size. */
	private int scaleFactor;
	/** Should the rendering be done according to scaleFactor. */
	private boolean ignoreScale = false;
	/** Current X position of the mouse. */
	public int mouseX;
	/** Current Y position of the mouse. */
	public int mouseY;
	/** Default {@link GuiTexture} to use for current {@link MalisisGui}. */
	private GuiTexture defaultGuiTexture;
	/** Currently used {@link GuiTexture}. */
	private GuiTexture currentTexture;

	private static GuiShape rectangle = new SimpleGuiShape();

	/**
	 * Instantiates a new {@link GuiRenderer}.
	 */
	public GuiRenderer()
	{
		defaultGuiTexture = new GuiTexture(new ResourceLocation("malisiscore", "textures/gui/gui.png"), 300, 100);
	}

	/**
	 * Gets the default {@link GuiTexture}.
	 *
	 * @return the defaultGuiTexture
	 */
	public GuiTexture getDefaultTexture()
	{
		return defaultGuiTexture;
	}

	/**
	 * Sets the default {@link GuiTexture} to use for this {@link GuiRenderer}.
	 *
	 * @param texture the new default texture
	 */
	public void setDefaultTexture(GuiTexture texture)
	{
		this.defaultGuiTexture = texture;
	}

	/**
	 * Sets the scale factor to use for this {@link GuiRenderer}.
	 *
	 * @param factor the new scale factor
	 */
	public void setScaleFactor(int factor)
	{
		scaleFactor = factor;
	}

	/**
	 * Gets the scale factor used for this {@link GuiRenderer}.
	 *
	 * @return the scale factor
	 */
	public int getScaleFactor()
	{
		return scaleFactor;
	}

	/**
	 * Sets whether to ignore default Minecraft GUI scale factor.<br>
	 * If set to true, 1 pixel size will be equal to 1 pixel on screen.
	 *
	 * @param ignore the new ignore scale
	 */
	public void setIgnoreScale(boolean ignore)
	{
		ignoreScale = ignore;
	}

	/**
	 * Checks if Minecraft GUI scale is ignored
	 *
	 * @return true if ignored
	 */
	public boolean isIgnoreScale()
	{
		return ignoreScale;
	}

	/**
	 * Sets the mouse position and the partial tick.
	 *
	 * @param mouseX the mouse x
	 * @param mouseY the mouse y
	 * @param partialTicks the partial ticks
	 */
	public void set(int mouseX, int mouseY, float partialTicks)
	{
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		this.partialTick = partialTicks;
	}

	@Override
	public void prepare(RenderType renderType, double... data)
	{
		_initialize();
		this.renderType = renderType;
		this.buffer = Tessellator.getInstance().getBuffer();

		currentTexture = null;
		bindDefaultTexture();

		if (ignoreScale)
		{
			GlStateManager.pushMatrix();
			GlStateManager.scale(1F / scaleFactor, 1F / scaleFactor, 1);
		}

		enableBlending();

		startDrawing();
	}

	@Override
	public void clean()
	{
		draw();

		if (ignoreScale)
			GlStateManager.popMatrix();

		reset();
	}

	/**
	 * Next.
	 */
	@Override
	public void next()
	{
		super.next();
		bindDefaultTexture();
	}

	/**
	 * Bind a new texture for rendering.
	 *
	 * @param texture the texture
	 */
	public void bindTexture(GuiTexture texture)
	{
		if (texture == null || texture == currentTexture)
			return;

		Minecraft.getMinecraft().getTextureManager().bindTexture(texture.getResourceLocation());
		currentTexture = texture;
	}

	/**
	 * Reset the texture to its {@link #defaultGuiTexture}.
	 */
	public void bindDefaultTexture()
	{
		bindTexture(defaultGuiTexture);
	}

	/**
	 * Applies the texture the {@link Shape}.
	 *
	 * @param shape the shape
	 * @param params the parameters
	 */
	@Override
	public void applyTexture(Shape shape, RenderParameters params)
	{
		Icon icon = params != null ? params.icon.get() : null;

		if (icon == null)
		{
			IIconProvider iconProvider = getIconProvider(params);
			if (iconProvider == null)
				return;

			if (iconProvider instanceof IGuiIconProvider)
				icon = ((IGuiIconProvider) iconProvider).getIcon(currentComponent);
			else
				icon = iconProvider.getIcon();
		}
		boolean isGuiIcon = icon instanceof GuiIcon;

		Face[] faces = shape.getFaces();
		for (int i = 0; i < faces.length; i++)
			faces[i].setTexture(isGuiIcon ? ((GuiIcon) icon).getIcon(i) : icon, false, false, false);
	}

	@Override
	protected IIconProvider getIconProvider(RenderParameters params)
	{
		if (params != null && params.iconProvider.get() != null)
			return params.iconProvider.get();

		if (currentComponent.getIconProvider() != null)
			return currentComponent.getIconProvider();

		return null;
	}

	@Override
	public void applyTexture(Face face, RenderParameters params)
	{
		//texture is already applied from the shape
	}

	/**
	 * Draws the component to the screen.
	 *
	 * @param container the container
	 * @param mouseX the mouse x
	 * @param mouseY the mouse y
	 * @param partialTick the partial tick
	 */
	public void drawScreen(UIContainer<?> container, int mouseX, int mouseY, float partialTick)
	{
		if (container == null)
			return;

		set(mouseX, mouseY, partialTick);
		prepare(RenderType.GUI);

		container.draw(this, mouseX, mouseY, partialTick);

		clean();
	}

	@Override
	public void drawShape(Shape shape)
	{
		drawShape(shape, null);
	}

	/**
	 * Draws a {@link Shape} to the GUI with the specified {@link RenderParameters}.
	 *
	 * @param shape the shape
	 * @param params the params
	 */
	public void drawShape(GuiShape shape, RenderParameters params)
	{
		if (shape == null)
			return;

		// move the shape at the right coord on screen
		shape.translate(currentComponent.screenX(), currentComponent.screenY(), currentComponent.getZIndex());
		shape.applyMatrix();

		applyTexture(shape, params);

		for (Face face : shape.getFaces())
			drawFace(face, params);
	}

	/**
	 * Draws an non-textured rectangle in the GUI at a position relative to {@link #currentComponent}.
	 *
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @param width the width
	 * @param height the height
	 * @param color the color
	 * @param alpha the alpha
	 */
	public void drawRectangle(int x, int y, int z, int width, int height, int color, int alpha)
	{
		drawRectangle(x, y, z, width, height, color, alpha, true);
	}

	/**
	 * Draws an non-textured rectangle in the GUI.
	 *
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @param width the width
	 * @param height the height
	 * @param color the color
	 * @param alpha the alpha
	 * @param relative true if the position of the rectangle is relative to {@link #currentComponent}
	 */
	public void drawRectangle(int x, int y, int z, int width, int height, int color, int alpha, boolean relative)
	{
		if (relative && currentComponent != null)
		{
			x += currentComponent.screenX();
			y += currentComponent.screenY();
			z += currentComponent.getZIndex();
		}

		rectangle.resetState();
		rectangle.setSize(width, height);
		rectangle.setPosition(x, y);
		rectangle.getFaces()[0].getParameters().colorMultiplier.set(color);
		rectangle.getFaces()[0].getParameters().alpha.set(alpha);

		GlStateManager.pushAttrib();
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 0, z);
		disableTextures();
		enableBlending();

		drawShape(rectangle);
		next();
		enableTextures();

		GlStateManager.popMatrix();
		GlStateManager.popAttrib();
	}

	/**
	 * Draws a {@link UITooltip} to the screen.
	 *
	 * @param tooltip the tooltip
	 */
	public void drawTooltip(UITooltip tooltip)
	{
		if (tooltip != null)
		{
			prepare(RenderType.GUI);
			startDrawing();
			tooltip.draw(this, mouseX, mouseY, partialTick);
			draw();
			clean();
		}
	}

	/**
	 * Draws text with default {@link MalisisFont} and {@link FontOptions}.
	 *
	 * @param text the text
	 */
	public void drawText(String text)
	{
		drawText(null, text, 0, 0, 0, null, true);
	}

	/**
	 * Draws text with default {@link MalisisFont} and {@link FontOptions} at the coordinates relative to {@link #currentComponent}.
	 *
	 * @param text the text
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 */
	public void drawText(String text, float x, float y, float z)
	{
		drawText(null, text, x, y, z, null, true);
	}

	/**
	 * Draw text with specified {@link MalisisFont} with {@link FontOptions}.
	 *
	 * @param font the font
	 * @param text the text
	 * @param fro the fro
	 */
	public void drawText(MalisisFont font, String text, FontOptions fro)
	{
		drawText(font, text, 0, 0, 0, fro, true);
	}

	/**
	 * Draws text with specified {@link MalisisFont} with {@link FontOptions} at the coordinates relative to {@link #currentComponent}
	 *
	 * @param font the font
	 * @param text the text
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @param fro the fro
	 */
	@Override
	public void drawText(MalisisFont font, String text, float x, float y, float z, FontOptions fro)
	{
		drawText(font, text, x, y, z, fro, true);
	}

	/**
	 * Draws text with specified {@link MalisisFont} with {@link FontOptions} at the coordinatesp passed.
	 *
	 * @param font the font
	 * @param text the text
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @param fro the fro
	 * @param relative true if the coordinates are relative to current component
	 */
	public void drawText(MalisisFont font, String text, float x, float y, float z, FontOptions fro, boolean relative)
	{
		if (relative && currentComponent != null)
		{
			x += currentComponent.screenX();
			y += currentComponent.screenY();
			z += currentComponent.getZIndex();
		}

		super.drawText(font, text, x, y, z, fro);
	}

	//#end drawText()

	/**
	 * Draws an itemStack to the GUI.
	 *
	 * @param itemStack the item stack
	 */
	public void drawItemStack(ItemStack itemStack)
	{
		drawItemStack(itemStack, 0, 0, null, null, true);
	}

	/**
	 * Draws an itemStack to the GUI at the specified coordinates.
	 *
	 * @param itemStack the item stack
	 * @param x the x
	 * @param y the y
	 */
	public void drawItemStack(ItemStack itemStack, int x, int y)
	{
		drawItemStack(itemStack, x, y, null, null, true);
	}

	/**
	 * Draws an itemStack to the GUI at the specified coordinates with a custom format for the label.
	 *
	 * @param itemStack the item stack
	 * @param x the x
	 * @param y the y
	 * @param format the format
	 */
	public void drawItemStack(ItemStack itemStack, int x, int y, Style format)
	{
		drawItemStack(itemStack, x, y, null, format, true);
	}

	/**
	 * Draws an itemStack to the GUI at the specified coordinates with a custom label.
	 *
	 * @param itemStack the item stack
	 * @param x the x
	 * @param y the y
	 * @param label the label
	 */
	public void drawItemStack(ItemStack itemStack, int x, int y, String label)
	{
		drawItemStack(itemStack, x, y, label, null, true);
	}

	/**
	 * Draws itemStack to the GUI at the specified coordinates with a custom formatted label.<br>
	 * Uses RenderItem.renderItemAndEffectIntoGUI() and RenderItem.renderItemOverlayIntoGUI()
	 *
	 * @param itemStack the item stack
	 * @param x the x
	 * @param y the y
	 * @param label the label to display, if null display the stack size
	 * @param format the format
	 * @param relative if true, coordinates are relative to current component
	 */
	public void drawItemStack(ItemStack itemStack, int x, int y, String label, Style format, boolean relative)
	{
		//we want to be able to render itemStack with 0 size, so we can't check for isEmpty
		if (itemStack == null || itemStack == ItemStack.EMPTY)
			return;

		if (relative && currentComponent != null)
		{
			x += currentComponent.screenX();
			y += currentComponent.screenY();
		}

		FontRenderer fontRenderer = itemStack.getItem().getFontRenderer(itemStack);
		if (fontRenderer == null)
			fontRenderer = Minecraft.getMinecraft().fontRenderer;

		String formatStr = format != null ? format.getFormattingCode() : null;
		if (label == null && (itemStack.getCount() > 1 || !Strings.isEmpty(formatStr)))
			label = Integer.toString(itemStack.getCount());
		if (label == null)
			label = "";
		if (!Strings.isEmpty(formatStr))
			label = formatStr + label;

		Tessellator.getInstance().draw();

		RenderHelper.disableStandardItemLighting();
		RenderHelper.enableGUIStandardItemLighting();

		IBakedModel model = itemRenderer.getItemModelWithOverrides(itemStack, Utils.getClientWorld(), Utils.getClientPlayer());
		itemRenderer.renderItemModelIntoGUI(itemStack, x, y, model);
		itemRenderer.renderItemOverlayIntoGUI(fontRenderer, itemStack, x, y, label);

		RenderHelper.enableStandardItemLighting();
		GlStateManager.enableBlend(); //Forge commented blend reenabling

		currentTexture = null;
		bindDefaultTexture();
		startDrawing();
	}

	/**
	 * Render the picked up itemStack at the cursor position.
	 *
	 * @param itemStack the item stack
	 */
	public void renderPickedItemStack(ItemStack itemStack)
	{
		if (itemStack == null || itemStack == ItemStack.EMPTY)
			return;

		int size = itemStack.getCount();
		String label = null;
		if (size == 0)
		{
			itemStack.setCount(size != 0 ? size : 1);
			label = TextFormatting.YELLOW + "0";
		}

		itemRenderer.zLevel = 100;
		prepare(RenderType.GUI);
		startDrawing();
		drawItemStack(itemStack, mouseX - 8, mouseY - 8, label, null, false);
		draw();
		itemStack.setCount(size);
		itemRenderer.zLevel = 0;
		clean();
	}

	/**
	 * Starts clipping an area to prevent drawing outside of it.
	 *
	 * @param area the area
	 */
	public void startClipping(ClipArea area)
	{
		if (area.noClip || area.width() <= 0 || area.height() <= 0)
			return;

		GL11.glPushAttrib(GL11.GL_SCISSOR_BIT);
		GL11.glEnable(GL11.GL_SCISSOR_TEST);

		int f = ignoreScale ? 1 : scaleFactor;
		int x = area.x * f;
		int y = Minecraft.getMinecraft().displayHeight - (area.y + area.height()) * f;
		int w = area.width() * f;
		int h = area.height() * f;;
		GL11.glScissor(x, y, w, h);
	}

	/**
	 * Ends the clipping.
	 *
	 * @param area the area
	 */
	public void endClipping(ClipArea area)
	{
		if (area.noClip || area.width() <= 0 || area.height() <= 0)
			return;

		GL11.glDisable(GL11.GL_SCISSOR_TEST);
		GL11.glPopAttrib();
	}
}
