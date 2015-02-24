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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.decoration.UITooltip;
import net.malisis.core.client.gui.element.GuiShape;
import net.malisis.core.client.gui.icon.GuiIcon;
import net.malisis.core.renderer.MalisisRenderer;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.RenderType;
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

import org.apache.commons.lang3.StringUtils;
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
	/** Font height. */
	public static int FONT_HEIGHT = MalisisRenderer.getFontRenderer().FONT_HEIGHT;
	/** Map of EnumChatFormatting **/
	public static Map<Character, EnumChatFormatting> charFormats = new HashMap<>();
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
	/** Scale to use when drawing fonts. */
	private float fontScale = 1F;
	/** Current X position of the mouse. */
	public int mouseX;
	/** Current Y position of the mouse. */
	public int mouseY;
	/** Default {@link GuiTexture} to use for current {@link MalisisGui}. */
	private GuiTexture defaultGuiTexture;
	/** Currently used {@link GuiTexture} */
	private GuiTexture currentTexture;

	static
	{
		//could reflect to get EnumChatFormatting.formattingCodeMapping instead
		for (EnumChatFormatting ecf : EnumChatFormatting.values())
			charFormats.put(ecf.getFormattingCode(), ecf);
	}

	/**
	 * Instantiates a new {@link GuiRenderer}.
	 */
	public GuiRenderer()
	{
		defaultGuiTexture = new GuiTexture(new ResourceLocation("malisiscore", "textures/gui/gui.png"), 300, 100);
		updateGuiScale();
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

	public void setDefaultTexture(GuiTexture texture)
	{
		this.defaultGuiTexture = texture;
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
	 * Sets a custom font scale factor.
	 *
	 * @param scale the new font scale
	 */
	public void setFontScale(float scale)
	{
		fontScale = scale;
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
	 * Clips a string to fit in the specified width. The string is translated before clipping.
	 *
	 * @param text the text
	 * @param width the width
	 * @return the string
	 */
	public String clipString(String text, int width)
	{
		text = StatCollector.translateToLocal(text);
		StringBuilder ret = new StringBuilder();
		float strWidth = 0;
		int index = 0;
		while (index < text.length())
		{
			char c = text.charAt(index);
			strWidth += getCharWidth(c, fontScale);
			if (strWidth < width)
				ret.append(c);
			else
				return ret.toString();
			index++;
		}

		return ret.toString();
	}

	/**
	 * Splits the string in multiple lines to fit in the specified maxWidth.
	 *
	 * @param text the text
	 * @param maxWidth the max width
	 * @return list of lines that won't exceed maxWidth limit
	 */
	public static List<String> wrapText(String text, int maxWidth)
	{
		return wrapText(text, maxWidth, 1);
	}

	/**
	 * Splits the string in multiple lines to fit in the specified maxWidth using the specified fontScale.
	 *
	 * @param text the text
	 * @param maxWidth the max width
	 * @param fontScale the font scale
	 * @return list of lines that won't exceed maxWidth limit
	 */
	public static List<String> wrapText(String text, int maxWidth, float fontScale)
	{
		List<String> lines = new ArrayList<>();
		List<EnumChatFormatting> modifiers = new LinkedList<>();
		EnumChatFormatting ecf;
		StringBuilder line = new StringBuilder();
		StringBuilder word = new StringBuilder();

		float lineWidth = 0;
		float wordWidth = 0;
		int index = 0;
		while (index < text.length())
		{
			while ((ecf = getFormatting(text, index)) != null)
			{
				if (ecf == EnumChatFormatting.RESET || ecf.isColor())
					modifiers.clear();
				if (ecf != EnumChatFormatting.RESET)
					modifiers.add(ecf);
				index += 2;
				word.append(ecf.toString());
				if (index >= text.length())
					break;
			}

			char c = text.charAt(index);

			float w = getCharWidth(c, fontScale);
			lineWidth += w;
			wordWidth += w;
			word.append(c);
			//we just ended a new word, add it to the current line
			if (c == ' ' || c == '-' || c == '.')
			{
				line.append(word);
				word.setLength(0);
				wordWidth = 0;
			}
			if (lineWidth >= maxWidth)
			{
				//the first word on the line is too large, split anyway
				if (line.length() == 0)
				{
					line.append(word);
					word.setLength(0);
					wordWidth = 0;
				}
				//make a new line
				lines.add(line.toString());
				line.setLength(0);

				//add modifiers to the new line
				for (int i = 0; i < modifiers.size(); i++)
					line.insert(i * 2, modifiers.get(i));

				lineWidth = wordWidth;
			}
			index++;
		}

		line.append(word);
		lines.add(line.toString());

		return lines;
	}

	/**
	 * Gets the {@link EnumChatFormatting} at the specified position in the text.<br>
	 * Returns null if
	 *
	 * @param text the text
	 * @param index the index
	 * @return the formatting
	 */
	private static EnumChatFormatting getFormatting(String text, int index)
	{
		if (text == null || index >= text.length() - 3)
			return null;

		char c = text.charAt(index);
		if (c != '\u00a7')
			return null;
		return charFormats.get(text.charAt(index + 1));
	}

	//#region drawText()
	/**
	 * Draws a white text on the GUI without shadow.
	 *
	 * @param text the text
	 */
	public void drawText(String text)
	{
		drawText(text, 0, 0, 0, 0xFFFFFF, false, true);
	}

	/**
	 * Draws a text on the GUI with specified color and shadow.
	 *
	 * @param text the text
	 * @param color the color
	 * @param shadow the shadow
	 */
	public void drawText(String text, int color, boolean shadow)
	{
		drawText(text, 0, 0, 0, color, shadow, true);
	}

	/**
	 * Draws a white text on the GUI without shadow, at the specified coordinates, relative to its parent container.
	 *
	 * @param text the text
	 * @param x the x
	 * @param y the y
	 */
	public void drawText(String text, int x, int y)
	{
		drawText(text, x, y, 0, 0xFFFFFF, false, true);
	}

	/**
	 * Draws a text on the GUI at the specified coordinates, relative to its parent container, with color and shadow.
	 *
	 * @param text the text
	 * @param x the x
	 * @param y the y
	 * @param color the color
	 * @param shadow the shadow
	 */
	public void drawText(String text, int x, int y, int color, boolean shadow)
	{
		drawText(text, x, y, 0, color, shadow, true);
	}

	/**
	 * Draws a string at the specified coordinates, with color and shadow.<br>
	 * The string gets translated.<br>
	 * Uses FontRenderer.drawString().
	 *
	 * @param text the text
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @param color the color
	 * @param shadow the shadow
	 * @param relative true if the coordinates are relatives to current component
	 */
	public void drawText(String text, int x, int y, int z, int color, boolean shadow, boolean relative)
	{
		if (MalisisRenderer.getFontRenderer() == null)
			return;

		if (relative && currentComponent != null)
		{
			x += currentComponent.screenX();
			y += currentComponent.screenY();
			z += currentComponent.getZIndex();
		}

		text = StatCollector.translateToLocal(text);
		text = text.replaceAll("\r", "");
		GL11.glPushMatrix();
		GL11.glTranslatef(x * (1 - fontScale), y * (1 - fontScale), 0);
		GL11.glScalef(fontScale, fontScale, 1);
		GL11.glTranslatef(0, 0, z);
		// GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);

		MalisisRenderer.getFontRenderer().drawString(text, x, y, color, shadow);

		GL11.glPopMatrix();
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		// GL11.glEnable(GL11.GL_DEPTH_TEST);
		currentTexture = null;
		bindDefaultTexture();
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
			fontRenderer = MalisisRenderer.getFontRenderer();

		if (label == null)
			label = Integer.toString(itemStack.stackSize);
		if (format != null)
			label += format;

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
	 * Gets rendering width of a string.
	 *
	 * @param str the str
	 * @return the string width
	 */
	public static int getStringWidth(String str)
	{
		return getStringWidth(str, 1);
	}

	/**
	 * Gets rendering width of a string according to fontScale.
	 *
	 * @param str the str
	 * @param fontScale the font scale
	 * @return the string width
	 */
	public static int getStringWidth(String str, float fontScale)
	{
		if (StringUtils.isEmpty(str))
			return 0;

		str = StatCollector.translateToLocal(str);
		str = str.replaceAll("\r", "");
		return (int) Math.ceil(MalisisRenderer.getFontRenderer().getStringWidth(str) * fontScale);
	}

	/**
	 * Gets the rendering height of strings.
	 *
	 * @return the string height
	 */
	public static int getStringHeight()
	{
		return getStringHeight(1);
	}

	/**
	 * Gets the rendering height of strings according to fontscale.
	 *
	 * @param fontScale the font scale
	 * @return the string height
	 */
	public static int getStringHeight(float fontScale)
	{
		return (int) Math.ceil(FONT_HEIGHT * fontScale);
	}

	/**
	 * Gets the max string width.
	 *
	 * @param strings the strings
	 * @return the max string width
	 */
	public static int getMaxStringWidth(List<String> strings)
	{
		return getMaxStringWidth(strings, 1);
	}

	/**
	 * Gets max rendering width of an array of string.
	 *
	 * @param strings the strings
	 * @param fontScale the font scale
	 * @return the max string width
	 */
	public static int getMaxStringWidth(List<String> strings, float fontScale)
	{
		int width = 0;
		for (String str : strings)
			width = Math.max(width, getStringWidth(str, fontScale));
		return width;
	}

	/**
	 * Gets the rendering width of a char.
	 *
	 * @param c the c
	 * @return the char width
	 */
	public static float getCharWidth(char c)
	{
		if (c == '\r')
			return 0;
		return getCharWidth(c, 1);
	}

	/**
	 * Gets the rendering width of a char with the specified fontScale.
	 *
	 * @param c the c
	 * @param fontScale the font scale
	 * @return the char width
	 */
	public static float getCharWidth(char c, float fontScale)
	{
		float s = MalisisRenderer.getFontRenderer().getCharWidth(c) * fontScale;
		return s >= 0 ? s : 0;
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
	 * Next.
	 */
	@Override
	public void next()
	{
		super.next();
		bindDefaultTexture();
	}
}
