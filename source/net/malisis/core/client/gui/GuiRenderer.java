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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.decoration.UITooltip;
import net.malisis.core.renderer.BaseRenderer;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.Shape;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.client.FMLClientHandler;

public class GuiRenderer extends BaseRenderer
{
	/**
	 * Base texture for GUIs.
	 */
	private final ResourceLocation GUI_TEXTURE = new ResourceLocation("malisiscore", "textures/gui/gui.png");
	/**
	 * Font renderer used to draw strings.
	 */
	public static FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
	/**
	 * RenderItem used to draw itemStacks.
	 */
	public static RenderItem itemRenderer = new RenderItem();
	/**
	 * Font height.
	 */
	public static int FONT_HEIGHT = fontRenderer.FONT_HEIGHT;
	/**
	 * Current component being drawn.
	 */
	public UIComponent currentComponent;
	/**
	 * Width of the Minecraft window.
	 */
	private int displayWidth;
	/**
	 * Height of the Minecraft window.
	 */
	private int displayHeight;
	/**
	 * Multiplying factor between GUI size and pixel size.
	 */
	private int scaleFactor;
	/**
	 * Current X position of the mouse.
	 */
	public int mouseX;
	/**
	 * Current Y position of the mouse.
	 */
	public int mouseY;
	/**
	 * Determines whether the texture has been changed.
	 */
	private boolean defaultTexture = true;

	public GuiRenderer()
	{
		updateGuiScale();
	}

	/**
	 * Sets the width, height and scale factor.
	 */
	public void updateGuiScale()
	{
		Minecraft mc = Minecraft.getMinecraft();
		displayWidth = mc.displayWidth;
		displayHeight = mc.displayHeight;
		calcScaleFactor(mc.gameSettings.guiScale);
	}

	/**
	 * Sets the mouse position and the partial tick.
	 * 
	 * @param mouseX
	 * @param mouseY
	 * @param partialTicks
	 */
	public void set(int mouseX, int mouseY, float partialTicks)
	{
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		this.partialTick = partialTicks;
	}

	/**
	 * Draws the component to the screen.
	 * 
	 * @param container
	 * @param mouseX
	 * @param mouseY
	 * @param partialTick
	 */
	public void drawScreen(UIContainer container, int mouseX, int mouseY, float partialTick)
	{
		set(mouseX, mouseY, partialTick);

		if (container != null)
		{
			setDefaultTexture();
			startDrawing();
			container.draw(this, mouseX, mouseY, partialTick);
			draw();
		}
	}

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
	 * Draws a shape on the GUI.
	 */
	@Override
	public void drawShape(Shape s, RenderParameters rp)
	{
		drawShape(s, rp, (IIcon) null);
	}

	public void drawShape(Shape s, IIcon... icons)
	{
		drawShape(s, null, icons);
	}

	public void drawShape(Shape s, RenderParameters rp, IIcon... icons)
	{
		if (s == null)
			return;

		shape = s;
		// move the shape at the right coord on screen
		shape.translate(currentComponent.screenX(), currentComponent.screenY(), currentComponent.getZIndex());
		shapeParams = rp != null ? rp : new RenderParameters();
		s.applyMatrix();

		if (shapeParams.applyTexture.get())
			applyTexture(s, shapeParams);

		Face[] faces = s.getFaces();
		for (int i = 0; i < faces.length; i++)
		{
			if (icons != null && i < icons.length && icons[i] != null)
				faces[i].setTexture(icons[i], false, false, false);
			drawFace(faces[i], rp);
		}
	}

	public String clipString(String text, int width)
	{
		text = StatCollector.translateToLocal(text);
		StringBuilder ret = new StringBuilder();
		int strWidth = 0;
		int index = 0;
		while (index < text.length())
		{
			char c = text.charAt(index);
			strWidth += getCharWidth(c);
			if (strWidth < width)
				ret.append(c);
			else
				return ret.toString();
			index++;
		}

		return ret.toString();
	}

	public static List<String> wrapText(String text, int maxWidth)
	{
		List<String> lines = new ArrayList<>();
		StringBuilder line = new StringBuilder();
		StringBuilder word = new StringBuilder();

		int lineWidth = 0;
		int wordWidth = 0;
		int index = 0;
		while (index < text.length())
		{
			char c = text.charAt(index);
			int w = getCharWidth(c);
			lineWidth += w;
			wordWidth += w;
			word.append(c);
			if (c == ' ' || c == '-' || c == '.')
			{
				line.append(word);
				word.setLength(0);
				wordWidth = 0;
			}
			if (lineWidth >= maxWidth)
			{
				if (line.length() == 0)
				{
					line.append(word);
					word.setLength(0);
					wordWidth = 0;
				}
				lines.add(line.toString());
				line.setLength(0);
				lineWidth = wordWidth;
			}
			index++;
		}

		line.append(word);
		lines.add(line.toString());

		return lines;
	}

	public void drawText(String text)
	{
		drawText(text, 0, 0, 0, 0xFFFFFF, false);
	}

	public void drawText(String text, int color, boolean shadow)
	{
		drawText(text, 0, 0, 0, color, shadow);
	}

	public void drawText(String text, int x, int y, int color, boolean shadow)
	{
		drawText(text, x, y, 0, color, shadow);
	}

	/**
	 * Draws a string in the currently drawn component. x, y, and zIndex are relatives to that component Uses FontRenderer.drawString().
	 * 
	 * @param text
	 * @param x
	 * @param y
	 * @param zIndex
	 * @param color
	 * @param shadow
	 */
	public void drawText(String text, int x, int y, int zIndex, int color, boolean shadow)
	{
		drawString(text, currentComponent.screenX() + x, currentComponent.screenY() + y, currentComponent.getZIndex() + zIndex, color,
				shadow);
	}

	public void drawString(String text, int x, int y, int z, int color, boolean shadow)
	{
		if (fontRenderer == null)
			return;

		text = StatCollector.translateToLocal(text);

		GL11.glPushMatrix();
		GL11.glTranslatef(0, 0, z);
		// GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		fontRenderer.drawString(text, x, y, color, shadow);
		GL11.glPopMatrix();
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		// GL11.glEnable(GL11.GL_DEPTH_TEST);
		setDefaultTexture();
	}

	/**
	 * Draws itemStack to the GUI. Uses RenderItem.renderItemAndEffectIntoGUI() and RenderItem.renderItemOverlayIntoGUI();
	 * 
	 * @param itemStack
	 * @param x
	 * @param y
	 */
	public void drawItemStack(ItemStack itemStack, int x, int y)
	{
		drawItemStack(itemStack, x, y, null);
	}

	/**
	 * Draws itemStack to the GUI. Uses RenderItem.renderItemAndEffectIntoGUI() and RenderItem.renderItemOverlayIntoGUI();
	 * 
	 * @param itemStack
	 * @param x
	 * @param y
	 * @param format
	 */
	public void drawItemStack(ItemStack itemStack, int x, int y, EnumChatFormatting format)
	{
		if (itemStack == null)
			return;

		FontRenderer fontRenderer = itemStack.getItem().getFontRenderer(itemStack);
		if (fontRenderer == null)
			fontRenderer = GuiRenderer.fontRenderer;

		String s = null;
		if (format != null)
			s = format + Integer.toString(itemStack.stackSize);

		t.draw();
		RenderHelper.enableGUIStandardItemLighting();

		itemRenderer.renderItemAndEffectIntoGUI(fontRenderer, Minecraft.getMinecraft().getTextureManager(), itemStack, x, y);
		itemRenderer.renderItemOverlayIntoGUI(fontRenderer, Minecraft.getMinecraft().getTextureManager(), itemStack, x, y, s);
		RenderHelper.disableStandardItemLighting();
		GL11.glColor4f(1, 1, 1, 1);
		setDefaultTexture();
		t.startDrawingQuads();
	}

	/**
	 * Starts clipping an area to prevent drawing outside of it.
	 * 
	 * @param area
	 */
	public void startClipping(ClipArea area)
	{
		if (area.noClip || area.width() <= 0 || area.height() <= 0)
			return;

		GL11.glPushAttrib(GL11.GL_SCISSOR_BIT);
		GL11.glEnable(GL11.GL_SCISSOR_TEST);

		int x = area.x * scaleFactor;
		int y = displayHeight - (area.y + area.height()) * scaleFactor;
		int w = area.width() * scaleFactor;
		int h = area.height() * scaleFactor;;
		GL11.glScissor(x, y, w, h);
	}

	/**
	 * Ends the clipping.
	 * 
	 * @param area
	 */
	public void endClipping(ClipArea area)
	{
		if (area.noClip || area.width() <= 0 || area.height() <= 0)
			return;

		GL11.glDisable(GL11.GL_SCISSOR_TEST);
		GL11.glPopAttrib();
	}

	/**
	 * Calculate GUI scale factor.
	 * 
	 * @param guiScale
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
	 * Render the picked up itemStack at the cursor position.
	 * 
	 * @param itemStack
	 */
	public void renderPickedItemStack(ItemStack itemStack)
	{
		if (itemStack == null)
			return;

		itemRenderer.zLevel = 100;
		t.startDrawingQuads();
		drawItemStack(itemStack, mouseX - 8, mouseY - 8, itemStack.stackSize == 0 ? EnumChatFormatting.YELLOW : null);
		t.draw();
		itemRenderer.zLevel = 0;
	}

	/**
	 * Gets rendering width of a string.
	 * 
	 * @param str
	 * @return
	 */
	public static int getStringWidth(String str)
	{
		str = StatCollector.translateToLocal(str);
		return fontRenderer.getStringWidth(str);
	}

	/**
	 * Gets max rendering width of an array of string
	 */
	public static int getMaxStringWidth(List<String> strings)
	{
		int width = 0;
		for (String str : strings)
			width = Math.max(width, getStringWidth(str));
		return width;
	}

	public static int getMaxStringWidth(String[] strings)
	{
		return getMaxStringWidth(Arrays.asList(strings));
	}

	/**
	 * Gets the rendering width of a char.
	 * 
	 * @param c
	 * @return
	 */
	public static int getCharWidth(char c)
	{
		return fontRenderer.getCharWidth(c);
	}

	/**
	 * Bind a new texture for rendering.
	 */
	@Override
	public void bindTexture(ResourceLocation rl)
	{
		if (rl == null)
			return;
		defaultTexture = false;
		FMLClientHandler.instance().getClient().getTextureManager().bindTexture(rl);
	}

	/**
	 * Reset the texture to its default GuiRenderer.GUI_TEXTURE.
	 */
	public void setDefaultTexture()
	{
		bindTexture(GUI_TEXTURE);
		defaultTexture = true;
	}

	@Override
	public void next()
	{
		super.next();
		if (!defaultTexture)
			setDefaultTexture();
	}
}
