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

package net.malisis.core.client.gui.element;

import static net.malisis.core.client.gui.MalisisGui.*;

import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.renderer.icon.Icon;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * @author Ordinastie
 *
 */
public class GuiIcon
{
	public static final GuiIcon EMPTY = new GuiIcon();
	//UISlot
	public static final GuiIcon SLOT = new GuiIcon(VANILLAGUI_TEXTURE, 209, 30, 18, 18);
	//UIPanel
	public static final GuiIcon PANEL = new GuiIcon(VANILLAGUI_TEXTURE, 200, 15, 15, 15, 5);
	//UITooltip
	public static final GuiIcon TOOLTIP = new GuiIcon(VANILLAGUI_TEXTURE, 227, 31, 15, 15, 5);
	//UIWindow
	public static final GuiIcon WINDOW = new GuiIcon(VANILLAGUI_TEXTURE, 200, 0, 15, 15, 5);
	//UIProgressBar
	public static final GuiIcon ARROW_EMPTY = new GuiIcon(VANILLAGUI_TEXTURE, 246, 0, 22, 16);
	public static final GuiIcon ARROW_FILLED = new GuiIcon(VANILLAGUI_TEXTURE, 246, 16, 22, 16);
	//UISeparator
	public static final GuiIcon SEPARATOR = new GuiIcon(VANILLAGUI_TEXTURE, 200, 15, 15, 15, 3);
	//UIButton
	public static final GuiIcon BUTTON = new GuiIcon(VANILLAGUI_TEXTURE, 0, 20, 200, 20, 5);
	public static final GuiIcon BUTTON_HOVER = new GuiIcon(VANILLAGUI_TEXTURE, 0, 40, 200, 20, 5);
	public static final GuiIcon BUTTON_HOVER_PRESSED = BUTTON_HOVER.flip(true, true);
	public static final GuiIcon BUTTON_DISABLED = new GuiIcon(VANILLAGUI_TEXTURE, 0, 0, 200, 20, 5);
	//UICheckbox
	public static final GuiIcon CHECKBOX_BG = new GuiIcon(VANILLAGUI_TEXTURE, 242, 32, 10, 10);
	public static final GuiIcon CHECKBOX_HOVER_BG = new GuiIcon(VANILLAGUI_TEXTURE, 252, 32, 10, 10);
	public static final GuiIcon CHECKBOX = new GuiIcon(VANILLAGUI_TEXTURE, 242, 52, 12, 10);
	public static final GuiIcon CHECKBOX_HOVER = new GuiIcon(VANILLAGUI_TEXTURE, 254, 42, 12, 10);
	public static final GuiIcon CHECKBOX_DISABLED = new GuiIcon(VANILLAGUI_TEXTURE, 242, 42, 12, 10);
	//UIRadioButton
	public static final GuiIcon RADIO_BG = new GuiIcon(VANILLAGUI_TEXTURE, 200, 54, 8, 8);
	public static final GuiIcon RADIO_DISABLED_BG = new GuiIcon(VANILLAGUI_TEXTURE, 200, 62, 8, 8);
	public static final GuiIcon RADIO = new GuiIcon(VANILLAGUI_TEXTURE, 214, 54, 6, 6);
	public static final GuiIcon RADIO_HOVER = new GuiIcon(VANILLAGUI_TEXTURE, 220, 54, 6, 6);
	public static final GuiIcon RADIO_DISABLED = new GuiIcon(VANILLAGUI_TEXTURE, 208, 54, 6, 6);
	//UISelect
	public static final GuiIcon SELECT = new GuiIcon(VANILLAGUI_TEXTURE, 200, 30, 9, 12, 3);
	public static final GuiIcon SELECT_DISABLED = new GuiIcon(VANILLAGUI_TEXTURE, 200, 42, 9, 12, 3);
	public static final GuiIcon SELECT_BOX = new GuiIcon(VANILLAGUI_TEXTURE, 200, 30, 9, 12, 1);
	public static final GuiIcon SELECT_ARROW = new GuiIcon(VANILLAGUI_TEXTURE, 209, 48, 7, 4);
	//UISlider
	public static final GuiIcon SLIDER = new GuiIcon(VANILLAGUI_TEXTURE, 227, 46, 8, 20, 3);
	public static final GuiIcon SLIDER_BG = new GuiIcon(VANILLAGUI_TEXTURE, 0, 0, 200, 20, 5);
	//UITextfield
	public static final GuiIcon TEXTFIELD = new GuiIcon(VANILLAGUI_TEXTURE, 200, 30, 9, 12, 1);
	public static final GuiIcon TEXTFIELD_DISABLED = new GuiIcon(VANILLAGUI_TEXTURE, 200, 42, 9, 12, 1);
	//ControlComponents
	public static final GuiIcon CLOSE = new GuiIcon(VANILLAGUI_TEXTURE, 268, 30, 15, 15);
	public static final GuiIcon MOVE = new GuiIcon(VANILLAGUI_TEXTURE, 268, 15, 15, 15);
	public static final GuiIcon RESIZE = new GuiIcon(VANILLAGUI_TEXTURE, 268, 0, 15, 15);
	//UIScrollbar
	public static final GuiIcon SCROLLBAR_BG = new GuiIcon(VANILLAGUI_TEXTURE, 215, 0, 15, 15, 1);
	public static final GuiIcon SCROLLBAR_DISABLED_BG = new GuiIcon(VANILLAGUI_TEXTURE, 215, 15, 15, 15, 1);
	public static final GuiIcon SCROLLBAR_HORIZONTAL = new GuiIcon(VANILLAGUI_TEXTURE, 230, 15, 15, 8);
	public static final GuiIcon SCROLLBAR_HORIZONTAL_DISABLED = new GuiIcon(VANILLAGUI_TEXTURE, 230, 23, 15, 8);
	public static final GuiIcon SCROLLBAR_VERTICAL = new GuiIcon(VANILLAGUI_TEXTURE, 230, 0, 8, 15);
	public static final GuiIcon SCROLLBAR_VERTICAL_DISABLED = new GuiIcon(VANILLAGUI_TEXTURE, 238, 0, 8, 15);

	private int textureWidth = 1;
	private int textureHeight = 1;
	private float minU = 0;
	private float minV = 0;
	private float maxU = 1;
	private float maxV = 1;
	private int border = 0;

	public GuiIcon()
	{

	}

	public GuiIcon(float u, float v, float U, float V)
	{
		this.minU = u;
		this.minV = v;
		this.maxU = U;
		this.maxV = V;
	}

	public GuiIcon(GuiTexture texture, int x, int y, int width, int height, int border)
	{
		textureWidth = texture.getWidth();
		textureHeight = texture.getHeight();

		this.minU = (float) x / textureWidth;
		this.minV = (float) y / textureHeight;
		this.maxU = (float) (x + width) / textureWidth;
		this.maxV = (float) (y + height) / textureHeight;

		this.border = border;
	}

	public GuiIcon(GuiTexture texture, int x, int y, int width, int height)
	{
		this(texture, x, y, width, height, 0);
	}

	public GuiIcon(GuiIcon icon)
	{
		this(icon.getMinU(), icon.getMinV(), icon.getMaxU(), icon.getMaxV());
	}

	public GuiIcon(TextureAtlasSprite icon)
	{
		this(icon.getMinU(), icon.getMinV(), icon.getMaxU(), icon.getMaxV());
	}

	/**
	 * Gets the min u.
	 *
	 * @return the min u
	 */

	public float getMinU()
	{
		return minU;
	}

	/**
	 * Gets the max u.
	 *
	 * @return the max u
	 */

	public float getMaxU()
	{
		return maxU;
	}

	/**
	 * Gets the min v.
	 *
	 * @return the min v
	 */

	public float getMinV()
	{
		return minV;
	}

	/**
	 * Gets the max v.
	 *
	 * @return the max v
	 */

	public float getMaxV()
	{
		return maxV;
	}

	public float getInterpolatedU(float i)
	{
		return getMinU() + i * (getMaxU() - getMinU());
	}

	public float getInterpolatedV(float i)
	{
		return getMinV() + i * (getMaxV() - getMinV());
	}

	public int getBorder()
	{
		return border;
	}

	public int getTextureWidth()
	{
		return textureWidth;
	}

	public int getTextureHeight()
	{
		return textureHeight;
	}

	public GuiIcon clip(float fromU, float fromV, float toU, float toV)
	{
		return new GuiIcon(getInterpolatedU(fromU), getInterpolatedV(fromV), getInterpolatedU(toU), getInterpolatedV(toV));
	}

	/**
	 * Gets a new {@link GuiIcon} where the UVs are flipped
	 *
	 * @param horizontal whether to flip horizontally
	 * @param vertical whether to flip vertically
	 * @return this {@link Icon}
	 */
	public GuiIcon flip(boolean horizontal, boolean vertical)
	{
		return new GuiIcon(	horizontal ? getMaxU() : getMinU(),
							vertical ? getMaxV() : getMinV(),
							horizontal ? getMinU() : getMaxU(),
							vertical ? getMinV() : getMaxV());
	}

	public static GuiIcon from(ItemStack itemStack)
	{
		if (Minecraft.getMinecraft().getRenderItem() == null)
			return new GuiIcon();
		TextureAtlasSprite icon = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getParticleIcon(itemStack.getItem(),
																												itemStack.getMetadata());

		return new GuiIcon(icon);
	}

	public static GuiIcon from(Item item)
	{
		return from(new ItemStack(item));
	}

	public static GuiIcon from(IBlockState state)
	{
		TextureAtlasSprite icon = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getTexture(state);

		return new GuiIcon(icon);
	}

	public static GuiIcon from(Block block)
	{
		return from(block.getDefaultState());
	}

}
