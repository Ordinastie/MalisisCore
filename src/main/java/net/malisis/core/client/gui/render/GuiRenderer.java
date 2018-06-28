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

package net.malisis.core.client.gui.render;

import org.apache.logging.log4j.util.Strings;
import org.lwjgl.opengl.GL11;

import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.element.IClipable.ClipArea;
import net.malisis.core.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;

/**
 * Renderer to use for {@link MalisisGui}.
 *
 * @author Ordinastie
 *
 */
public class GuiRenderer
{
	/** Currently used buffer. */
	public static final BufferBuilder BUFFER = Tessellator.getInstance().getBuffer();

	/** RenderItem used to draw itemStacks. */
	public static RenderItem itemRenderer = Minecraft.getMinecraft().getRenderItem();
	/** Current component being drawn. */
	public UIComponent currentComponent;
	/** Multiplying factor between GUI size and pixel size. */
	private int scaleFactor;
	/** Should the rendering be done according to scaleFactor. */
	private boolean ignoreScale = false;
	/** Default {@link GuiTexture} to use for current {@link MalisisGui}. */
	private GuiTexture defaultGuiTexture;
	/** Currently used {@link GuiTexture}. */
	private GuiTexture currentTexture;

	/** Progression of current tick. */
	private float partialTick = 0;

	//	private static GuiShape rectangle = new SimpleGuiShape();

	/**
	 * Instantiates a new {@link GuiRenderer}.
	 */
	public GuiRenderer()
	{
		//defaultGuiTexture = MalisisGui.VANILLAGUI_TEXTURE;
		defaultGuiTexture = MalisisGui.VANILLAGUI_TEXTURE;
	}

	public BufferBuilder getBuffer()
	{
		return BUFFER;
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

	public float getPartialTick()
	{
		return partialTick;
	}

	/**
	 * Sets up the rendering and start drawing.
	 *
	 * @param partialTick the partial ticks
	 */
	public void setup(float partialTick)
	{
		this.partialTick = partialTick;

		currentTexture = null;
		bindDefaultTexture();

		GlStateManager.pushMatrix();
		if (ignoreScale)
		{
			GlStateManager.scale(1F / scaleFactor, 1F / scaleFactor, 1);
		}

		setupGl();
		startDrawing();
	}

	public void setupGl()
	{
		GlStateManager.enableTexture2D();
		GlStateManager.disableLighting();
		enableBlending();
	}

	/**
	 * Cleans up the rendering and trigger a draw.
	 */
	public void clean()
	{
		draw();

		GlStateManager.popMatrix();
		GlStateManager.enableDepth();
	}

	/**
	 * Checks whether the GUI should be drawn in a batch.
	 */
	public boolean isBatched()
	{
		return false;
	}

	/**
	 * Checks if the {@link Tessellator} is currently drawing.
	 *
	 * @return true, if is drawing
	 */
	public boolean isDrawing()
	{
		return BUFFER.isDrawing;
	}

	public void startDrawing()
	{
		startDrawing(GL11.GL_QUADS);
	}

	/**
	 * Tells the {@link Tessellator} to start drawing with specified <b>drawMode and specified {@link VertexFormat}.
	 *
	 * @param drawMode the draw mode
	 */
	public void startDrawing(int drawMode)
	{
		if (isBatched())
			return;
		draw();
		BUFFER.begin(drawMode, DefaultVertexFormats.POSITION_TEX_COLOR);
	}

	public void next()
	{
		next(GL11.GL_QUADS);
	}

	/**
	 * Triggers a draw and restarts drawing.<br>
	 * Rebinds the {@link #getDefaultTexture()} if necessary.
	 */
	public void next(int drawMode)
	{
		draw();
		startDrawing(drawMode);
		//bindDefaultTexture();
	}

	/**
	 * Triggers a draw.
	 */
	public void draw()
	{
		if (!isBatched() && isDrawing())
			Tessellator.getInstance().draw();
	}

	/**
	 * Enables the blending for the rendering.
	 */
	public void enableBlending()
	{
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		GlStateManager.enableColorMaterial();
	}

	/**
	 * Bind a new texture for rendering.
	 *
	 * @param texture the texture
	 */
	public void bindTexture(GuiTexture texture)
	{
		//no change needed
		if (texture == currentTexture)
			return;

		next();
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture.getResourceLocation());
		//System.out.println(currentComponent + " // Bound " + texture.getResourceLocation());

		currentTexture = texture;
	}

	/**
	 * Reset the texture to its {@link #defaultGuiTexture}.
	 */
	public void bindDefaultTexture()
	{
		bindTexture(defaultGuiTexture);
	}

	public void forceRebind()
	{
		Minecraft.getMinecraft().getTextureManager().bindTexture(currentTexture.getResourceLocation());
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
		//		if (relative && currentComponent != null)
		//		{
		//			x += currentComponent.screenX();
		//			y += currentComponent.screenY();
		//			z += currentComponent.getZIndex();
		//		}
		//
		//		rectangle.resetState();
		//		rectangle.setSize(width, height);
		//		rectangle.setPosition(x, y);
		//		rectangle.getFaces()[0].getParameters().colorMultiplier.set(color);
		//		rectangle.getFaces()[0].getParameters().alpha.set(alpha);
		//
		//		GlStateManager.pushMatrix();
		//		GlStateManager.translate(0, 0, z);
		//		disableTextures();
		//
		//		drawShape(rectangle);
		//		next();
		//		enableTextures();
		//
		//		GlStateManager.popMatrix();
	}

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

		float z = itemRenderer.zLevel;
		if (relative && currentComponent != null)
		{
			x += currentComponent.screenPosition().x();
			y += currentComponent.screenPosition().y();
			itemRenderer.zLevel = currentComponent.getZIndex();
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

		//RenderHelper.disableStandardItemLighting();
		RenderHelper.enableGUIStandardItemLighting();

		IBakedModel model = itemRenderer.getItemModelWithOverrides(itemStack, Utils.getClientWorld(), Utils.getClientPlayer());
		itemRenderer.renderItemModelIntoGUI(itemStack, x, y, model);
		itemRenderer.renderItemOverlayIntoGUI(fontRenderer, itemStack, x, y, label);

		RenderHelper.disableStandardItemLighting();
		//GlStateManager.enableBlend(); //Forge commented blend reenabling

		itemRenderer.zLevel = z;
		currentTexture = null;
		bindDefaultTexture();
		startDrawing();
	}

	/**
	 * Render the picked up itemStack at the cursor position.
	 *
	 * @param itemStack the item stack
	 */
	public boolean renderPickedItemStack(ItemStack itemStack)
	{
		if (itemStack == null || itemStack == ItemStack.EMPTY)
			return false;

		int size = itemStack.getCount();
		String label = null;
		if (size == 0)
		{
			itemStack.setCount(size != 0 ? size : 1);
			label = TextFormatting.YELLOW + "0";
		}

		itemRenderer.zLevel = 100;
		drawItemStack(itemStack, MalisisGui.MOUSE_POSITION.x() - 8, MalisisGui.MOUSE_POSITION.y() - 8, label, null, false);
		itemRenderer.zLevel = 0;
		return true;
	}

	/**
	 * Starts clipping an area to prevent drawing outside of it.
	 *
	 * @param area the area
	 */
	public void startClipping(ClipArea area)
	{
		if (area.noClip())
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
		if (area.noClip())
			return;

		next();
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
		GL11.glPopAttrib();
	}
}
