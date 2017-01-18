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

import org.apache.logging.log4j.core.helpers.Strings;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import net.malisis.core.MalisisCore;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.decoration.UITooltip;
import net.malisis.core.client.gui.shader.Shader;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.renderer.font.MalisisFont;
import net.malisis.core.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
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
	public static final VertexBuffer buffer = Tessellator.getInstance().getBuffer();

	public static VertexFormat vertexFormat = new VertexFormat()
	{
		{
			addElement(new VertexFormatElement(0, VertexFormatElement.EnumType.FLOAT, VertexFormatElement.EnumUsage.POSITION, 3));
			addElement(new VertexFormatElement(0, VertexFormatElement.EnumType.UBYTE, VertexFormatElement.EnumUsage.COLOR, 4));
			addElement(new VertexFormatElement(0, VertexFormatElement.EnumType.FLOAT, VertexFormatElement.EnumUsage.UV, 2));
			//Element filled with UV min/max of the GuiIcon
			addElement(new VertexFormatElement(1, VertexFormatElement.EnumType.FLOAT, VertexFormatElement.EnumUsage.UV, 4));
			//width, height, border
			addElement(new VertexFormatElement(2, VertexFormatElement.EnumType.FLOAT, VertexFormatElement.EnumUsage.UV, 3));
			//relative x/y in the quad
			addElement(new VertexFormatElement(3, VertexFormatElement.EnumType.FLOAT, VertexFormatElement.EnumUsage.UV, 2));
		}
	};

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

	/** Progression of current tick. */
	private float partialTick = 0;

	public static Shader repeatShader = new Shader(new ResourceLocation(MalisisCore.modid, "shaders/repeat.frag"));

	/**
	 * Instantiates a new {@link GuiRenderer}.
	 */
	public GuiRenderer()
	{
		defaultGuiTexture = MalisisGui.VANILLAGUI_TEXTURE;
	}

	public VertexBuffer getBuffer()
	{
		return buffer;
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

	public void init()
	{
		vertexFormat = new VertexFormat()
		{
			{
				addElement(new VertexFormatElement(0, VertexFormatElement.EnumType.FLOAT, VertexFormatElement.EnumUsage.POSITION, 3));
				addElement(new VertexFormatElement(0, VertexFormatElement.EnumType.UBYTE, VertexFormatElement.EnumUsage.COLOR, 4));
				//pack UV and relative pos
				addElement(new VertexFormatElement(0, VertexFormatElement.EnumType.FLOAT, VertexFormatElement.EnumUsage.UV, 4));
				//UV min/max of the GuiIcon
				addElement(new VertexFormatElement(1, VertexFormatElement.EnumType.FLOAT, VertexFormatElement.EnumUsage.UV, 4));
				//texture size and quad size in pixels
				addElement(new VertexFormatElement(2, VertexFormatElement.EnumType.FLOAT, VertexFormatElement.EnumUsage.UV, 4));
				//border size in pixels
				addElement(new VertexFormatElement(3, VertexFormatElement.EnumType.FLOAT, VertexFormatElement.EnumUsage.UV, 1));
			}
		};

		repeatShader.load();
	}

	/**
	 * Sets up the rendering and start drawing.
	 *
	 * @param mouseX the mouse x
	 * @param mouseY the mouse y
	 * @param partialTicks the partial ticks
	 */
	public void setup(int mouseX, int mouseY, float partialTicks)
	{
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		this.partialTick = partialTicks;

		currentTexture = null;
		bindDefaultTexture();

		GlStateManager.pushMatrix();
		if (ignoreScale)
		{
			GlStateManager.scale(1F / scaleFactor, 1F / scaleFactor, 1);
		}

		setupGl();
		repeatShader.start();
		startDrawing();
	}

	public void setupGl()
	{
		RenderHelper.disableStandardItemLighting();
		RenderHelper.enableGUIStandardItemLighting();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_LIGHTING);
		GlStateManager.setActiveTexture(GL13.GL_TEXTURE1);
		GlStateManager.matrixMode(GL11.GL_TEXTURE);
		GlStateManager.loadIdentity();
		GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
		GlStateManager.matrixMode(GL11.GL_MODELVIEW);
		enableBlending();
	}

	/**
	 * Cleans up the rendering and trigger a draw.
	 */
	public void clean()
	{
		draw();

		repeatShader.stop();
		GlStateManager.popMatrix();
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
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
		return buffer.isDrawing;
	}

	public void startDrawing()
	{
		startDrawing(GL11.GL_QUADS);
	}

	/**
	 * Tells the {@link Tessellator} to start drawing with specified <b>drawMode and specified {@link VertexFormat}.
	 *
	 * @param drawMode the draw mode
	 * @param vertexFormat the vertex format
	 */
	public void startDrawing(int drawMode)
	{
		if (isBatched())
			return;
		draw();
		buffer.begin(drawMode, vertexFormat);
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
		bindDefaultTexture();
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

		if (texture == null)
			GlStateManager.disableTexture2D();
		else
		{
			if (currentTexture == null)
				GlStateManager.enableTexture2D();
			Minecraft.getMinecraft().getTextureManager().bindTexture(texture.getResourceLocation());
		}

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
	 * Enables textures and binds the default texture for the GUI.
	 */
	public void enableTextures()
	{
		bindDefaultTexture();
	}

	/**
	 * Disables textures.
	 */
	public void disableTextures()
	{
		bindTexture(null);
	}

	/**
	 * Draws a {@link UITooltip} to the screen.
	 *
	 * @param tooltip the tooltip
	 */
	public void drawTooltip(UITooltip tooltip)
	{
		if (tooltip == null)
			return;
		startDrawing();
		tooltip.draw(this, mouseX, mouseY, partialTick);
		draw();
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

		if (font == null)
			font = MalisisFont.minecraftFont;
		if (fro == null)
			fro = FontOptions.builder().build();

		GlStateManager.pushMatrix();
		GlStateManager.scale(fro.getFontScale(), fro.getFontScale(), z);

		FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
		fontRenderer.drawString(text, x / fro.getFontScale(), y / fro.getFontScale(), fro.getColor(), fro.hasShadow());

		GlStateManager.popMatrix();

		currentTexture = null;
		bindDefaultTexture();

		//font.render(this, text, x, y, z, fro);
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
			fontRenderer = Minecraft.getMinecraft().fontRendererObj;

		String formatStr = format != null ? format.getFormattingCode() : null;
		if (label == null && (itemStack.getCount() > 1 || !Strings.isEmpty(formatStr)))
			label = Integer.toString(itemStack.getCount());
		if (label == null)
			label = "";
		if (!Strings.isEmpty(formatStr))
			label = formatStr + label;

		draw();

		RenderHelper.disableStandardItemLighting();
		RenderHelper.enableGUIStandardItemLighting();

		IBakedModel model = itemRenderer.getItemModelWithOverrides(itemStack, Utils.getClientWorld(), Utils.getClientPlayer());
		itemRenderer.renderItemModelIntoGUI(itemStack, x, y, model);
		itemRenderer.renderItemOverlayIntoGUI(fontRenderer, itemStack, x, y, label);

		setupGl();

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

		startDrawing();

		int size = itemStack.getCount();
		String label = null;
		if (size == 0)
		{
			itemStack.setCount(size != 0 ? size : 1);
			label = TextFormatting.YELLOW + "0";
		}

		itemRenderer.zLevel = 100;

		drawItemStack(itemStack, mouseX - 8, mouseY - 8, label, null, false);
		draw();
		itemStack.setCount(size);
		itemRenderer.zLevel = 0;
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
