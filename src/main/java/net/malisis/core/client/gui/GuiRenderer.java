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

import net.malisis.core.client.gui.component.IGuiText;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.decoration.UITooltip;
import net.malisis.core.client.gui.element.GuiShape;
import net.malisis.core.client.gui.element.SimpleGuiShape;
import net.malisis.core.client.gui.icon.GuiIcon;
import net.malisis.core.renderer.MalisisRenderer;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.RenderType;
import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.font.FontRenderOptions;
import net.malisis.core.renderer.font.MalisisFont;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

/**
 * Renderer to use for {@link MalisisGui}.
 *
 * @author Ordinastie
 *
 */
public class GuiRenderer extends MalisisRenderer
{
	/** RenderItem used to draw itemStacks. */
	public static RenderItem itemRenderer = new RenderItem();
	/** Current component being drawn. */
	public UIComponent currentComponent;
	/** Width of the Minecraft window. */
	private int displayWidth;
	/** Height of the Minecraft window. */
	private int displayHeight;
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
	/** Currently used {@link MalisisFont}. */
	private MalisisFont defaultFont = MalisisFont.minecraftFont;
	/** Currently used {@link FontRenderOptions}. */
	private FontRenderOptions defaultFro = new FontRenderOptions();

	private static GuiShape rectangle = new SimpleGuiShape();

	/**
	 * Instantiates a new {@link GuiRenderer}.
	 */
	public GuiRenderer()
	{
		defaultGuiTexture = new GuiTexture(new ResourceLocation("malisiscore", "textures/gui/gui.png"), 300, 100);
		defaultFro.color = 0x404040;
		updateGuiScale();
	}

	/**
	 * Calculates GUI scale factor.
	 *
	 * @param guiScale the gui scale
	 */
	private void calcScaleFactor(int guiScale)
	{
		this.scaleFactor = 1;
		if (guiScale == 0)
			guiScale = 1000;

		while (this.scaleFactor < guiScale && this.displayWidth / (this.scaleFactor + 1) >= 320
				&& this.displayHeight / (this.scaleFactor + 1) >= 240)
			++this.scaleFactor;
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
	 * Gets the default {@link MalisisFont}.
	 *
	 * @return the default font
	 */
	public MalisisFont getDefaultFont()
	{
		return defaultFont;
	}

	/**
	 * Gets the default {@link FontRenderOptions}.
	 *
	 * @return the default font renderer options
	 */
	public FontRenderOptions getDefaultFontRendererOptions()
	{
		return defaultFro;
	}

	/**
	 * Sets the default {@link MalisisFont} to use for this {@link GuiRenderer}.
	 *
	 * @param font the new default font
	 */
	public void setDefaultFont(MalisisFont font, FontRenderOptions fro)
	{
		this.defaultFont = font;
		this.defaultFro = fro;
	}

	/**
	 * Gets the {@link MalisisFont} to be used for the specified {@link IGuiText}.
	 *
	 * @param guiText the gui text
	 * @return the font
	 */
	public MalisisFont getFont(IGuiText guiText)
	{
		return guiText == null || guiText.getFont() == null ? getDefaultFont() : guiText.getFont();
	}

	/**
	 * Gets the {@link FontRenderOptions} to be used for the specified {@link IGuiText}.
	 *
	 * @param guiText the gui text
	 * @return the font
	 */
	public FontRenderOptions getFontRendererOptions(IGuiText guiText)
	{
		return guiText == null || guiText.getFontRendererOptions() == null ? getDefaultFontRendererOptions() : guiText
				.getFontRendererOptions();
	}

	public int getStringHeight(IGuiText guiText)
	{
		return (int) Math.ceil(getFont(guiText).getStringHeight(getFontRendererOptions(guiText).fontScale));
	}

	public int getStringWidth(IGuiText guiText, String text)
	{
		return (int) Math.ceil(getFont(guiText).getStringWidth(text, getFontRendererOptions(guiText).fontScale));
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
	 * Sets the width, height and scale factor for this {@link GuiRenderer}.
	 *
	 * @return the the scale factor calculated
	 */
	public int updateGuiScale()
	{
		Minecraft mc = Minecraft.getMinecraft();
		displayWidth = mc.displayWidth;
		displayHeight = mc.displayHeight;
		calcScaleFactor(mc.gameSettings.guiScale);
		return scaleFactor;
	}

	/**
	 * Gets the scale factor used by the GUI.
	 *
	 * @return the scale factor
	 */
	public float getScaleFactor()
	{
		return scaleFactor;
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

		currentTexture = null;
		bindDefaultTexture();

		if (ignoreScale)
		{
			GL11.glPushMatrix();
			GL11.glScalef(1F / scaleFactor, 1F / scaleFactor, 1);
		}

		enableBlending();

		startDrawing();
	}

	@Override
	public void clean()
	{
		draw();

		if (ignoreScale)
			GL11.glPopMatrix();

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
	 * @param parameters the parameters
	 */
	@Override
	public void applyTexture(Shape shape, RenderParameters parameters)
	{
		if (parameters.icon.get() == null)
			return;

		Face[] faces = shape.getFaces();
		IIcon icon = parameters.icon.get();
		boolean isGuiIcon = icon instanceof GuiIcon;

		for (int i = 0; i < faces.length; i++)
			faces[i].setTexture(isGuiIcon ? ((GuiIcon) icon).getIcon(i) : icon, false, false, false);
	}

	/**
	 * Draws the component to the screen.
	 *
	 * @param container the container
	 * @param mouseX the mouse x
	 * @param mouseY the mouse y
	 * @param partialTick the partial tick
	 */
	public void drawScreen(UIContainer container, int mouseX, int mouseY, float partialTick)
	{
		if (container == null)
			return;

		set(mouseX, mouseY, partialTick);
		prepare(RenderType.GUI);

		container.draw(this, mouseX, mouseY, partialTick);

		clean();
	}

	/**
	 * Draws a {@link Shape} to the GUI with the specified {@link RenderParameters}.
	 *
	 * @param s the s
	 * @param params the params
	 */
	public void drawShape(GuiShape s, RenderParameters params)
	{
		if (s == null)
			return;

		shape = s;
		rp = params != null ? params : new RenderParameters();

		// move the shape at the right coord on screen
		shape.translate(currentComponent.screenX(), currentComponent.screenY(), currentComponent.getZIndex());
		shape.applyMatrix();

		applyTexture(shape, rp);

		for (Face face : s.getFaces())
			drawFace(face, face.getParameters());
	}

	public void drawRectangle(int x, int y, int z, int width, int height, int color, int alpha)
	{
		drawRectangle(x, y, z, width, height, color, alpha, true);
	}

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

		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		GL11.glPushMatrix();
		GL11.glTranslated(0, 0, z);
		disableTextures();
		enableBlending();

		drawShape(rectangle);
		next();
		enableTextures();

		GL11.glPopMatrix();
		GL11.glPopAttrib();
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
			t.startDrawingQuads();
			tooltip.draw(this, mouseX, mouseY, partialTick);
			t.draw();
		}
	}

	/**
	 * Draws text with default {@link MalisisFont} and {@link FontRenderOptions}.
	 *
	 * @param text the text
	 */
	public void drawText(String text)
	{
		drawText(null, text, 0, 0, 0, null, true);
	}

	/**
	 * Draws text with default {@link MalisisFont} and {@link FontRenderOptions} at the coordinates relative to {@link #currentComponent}.
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
	 * Draw text with specified {@link MalisisFont} with {@link FontRenderOptions}.
	 *
	 * @param font the font
	 * @param text the text
	 * @param fro the fro
	 */
	public void drawText(MalisisFont font, String text, FontRenderOptions fro)
	{
		drawText(font, text, 0, 0, 0, fro, true);
	}

	/**
	 * Draws text with specified {@link MalisisFont} with {@link FontRenderOptions} at the coordinates relative to {@link #currentComponent}
	 *
	 * @param font the font
	 * @param text the text
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @param fro the fro
	 */
	@Override
	public void drawText(MalisisFont font, String text, float x, float y, float z, FontRenderOptions fro)
	{
		drawText(font, text, x, y, z, fro, true);
	}

	/**
	 * Draws text with specified {@link MalisisFont} with {@link FontRenderOptions} at the coordinatesp passed.
	 *
	 * @param font the font
	 * @param text the text
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @param fro the fro
	 * @param relative true if the coordinates are relative to current component
	 */
	public void drawText(MalisisFont font, String text, float x, float y, float z, FontRenderOptions fro, boolean relative)
	{
		if (relative && currentComponent != null)
		{
			x += currentComponent.screenX();
			y += currentComponent.screenY();
			z += currentComponent.getZIndex();
		}

		if (font == null)
			font = defaultFont;
		if (fro == null)
			fro = defaultFro;

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
	public void drawItemStack(ItemStack itemStack, int x, int y, EnumChatFormatting format)
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
	public void drawItemStack(ItemStack itemStack, int x, int y, String label, EnumChatFormatting format, boolean relative)
	{
		if (itemStack == null)
			return;

		if (relative && currentComponent != null)
		{
			x += currentComponent.screenX();
			y += currentComponent.screenY();
		}

		FontRenderer fontRenderer = itemStack.getItem().getFontRenderer(itemStack);
		if (fontRenderer == null)
			fontRenderer = Minecraft.getMinecraft().fontRendererObj;

		if (label == null && (itemStack.stackSize > 1 || format != null))
			label = Integer.toString(itemStack.stackSize);
		if (label == null)
			label = "";
		if (format != null)
			label = format + label;

		t.draw();
		RenderHelper.enableGUIStandardItemLighting();
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);

		itemRenderer.renderItemAndEffectIntoGUI(fontRenderer, Minecraft.getMinecraft().getTextureManager(), itemStack, x, y);
		itemRenderer.renderItemOverlayIntoGUI(fontRenderer, Minecraft.getMinecraft().getTextureManager(), itemStack, x, y, label);

		RenderHelper.disableStandardItemLighting();
		GL11.glColor4f(1, 1, 1, 1);
		//	GL11.glDisable(GL11.GL_ALPHA_TEST);

		currentTexture = null;
		bindDefaultTexture();
		t.startDrawingQuads();
	}

	/**
	 * Render the picked up itemStack at the cursor position.
	 *
	 * @param itemStack the item stack
	 */
	public void renderPickedItemStack(ItemStack itemStack)
	{
		if (itemStack == null)
			return;

		itemRenderer.zLevel = 100;
		t.startDrawingQuads();
		drawItemStack(itemStack, mouseX - 8, mouseY - 8, null, itemStack.stackSize == 0 ? EnumChatFormatting.YELLOW : null, false);
		t.draw();
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
		int y = displayHeight - (area.y + area.height()) * f;
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
