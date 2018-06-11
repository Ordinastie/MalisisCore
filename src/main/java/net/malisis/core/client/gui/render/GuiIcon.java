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

import static net.malisis.core.client.gui.MalisisGui.*;

import java.util.function.Supplier;

import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
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
	public static final GuiIcon FULL = new GuiIcon(0, 0, 1, 1);
	public static final GuiIcon NONE = new GuiIcon(VANILLAGUI_TEXTURE, 0, 80, 15, 15);
	public static final GuiIcon BORDER = new GuiIcon(VANILLAGUI_TEXTURE, 15, 80, 15, 15);
	//BOX
	public static final GuiIcon BOX = new GuiIcon(VANILLAGUI_TEXTURE, 268, 66, 15, 15);
	//UISlot
	public static final GuiIcon SLOT = new GuiIcon(VANILLAGUI_TEXTURE, 209, 30, 18, 18);
	//UIPanel
	public static final GuiIcon PANEL = new GuiIcon(VANILLAGUI_TEXTURE, 200, 15, 15, 15);
	//UITooltip
	public static final GuiIcon TOOLTIP = new GuiIcon(VANILLAGUI_TEXTURE, 227, 31, 15, 15);
	//UIWindow
	public static final GuiIcon WINDOW = new GuiIcon(VANILLAGUI_TEXTURE, 200, 0, 15, 15);
	//UIProgressBar
	public static final GuiIcon ARROW_EMPTY = new GuiIcon(VANILLAGUI_TEXTURE, 246, 0, 22, 16);
	public static final GuiIcon ARROW_FILLED = new GuiIcon(VANILLAGUI_TEXTURE, 246, 16, 22, 16);
	//UISeparator
	public static final GuiIcon SEPARATOR = new GuiIcon(VANILLAGUI_TEXTURE, 200, 15, 15, 15);
	//UIButton
	public static final GuiIcon BUTTON = new GuiIcon(VANILLAGUI_TEXTURE, 0, 20, 200, 20);
	public static final GuiIcon BUTTON_HOVER = new GuiIcon(VANILLAGUI_TEXTURE, 0, 40, 200, 20);
	public static final GuiIcon BUTTON_HOVER_PRESSED = new GuiIcon(VANILLAGUI_TEXTURE, 0, 60, 200, 20);
	public static final GuiIcon BUTTON_DISABLED = new GuiIcon(VANILLAGUI_TEXTURE, 0, 0, 200, 20);
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
	public static final GuiIcon SELECT = new GuiIcon(VANILLAGUI_TEXTURE, 200, 30, 9, 12);
	public static final GuiIcon SELECT_DISABLED = new GuiIcon(VANILLAGUI_TEXTURE, 200, 42, 9, 12);
	public static final GuiIcon SELECT_BOX = new GuiIcon(VANILLAGUI_TEXTURE, 200, 30, 9, 12);
	public static final GuiIcon SELECT_ARROW = new GuiIcon(VANILLAGUI_TEXTURE, 209, 48, 7, 4);
	//UISlider
	public static final GuiIcon SLIDER = new GuiIcon(VANILLAGUI_TEXTURE, 227, 46, 8, 20);
	public static final GuiIcon SLIDER_BG = new GuiIcon(VANILLAGUI_TEXTURE, 0, 0, 200, 20);
	//UITextfield
	public static final GuiIcon TEXTFIELD = new GuiIcon(VANILLAGUI_TEXTURE, 200, 30, 9, 12);
	public static final GuiIcon TEXTFIELD_DISABLED = new GuiIcon(VANILLAGUI_TEXTURE, 200, 42, 9, 12);
	//ControlComponents
	public static final GuiIcon CLOSE = new GuiIcon(VANILLAGUI_TEXTURE, 268, 30, 15, 15);
	public static final GuiIcon MOVE = new GuiIcon(VANILLAGUI_TEXTURE, 268, 15, 15, 15);
	public static final GuiIcon RESIZE = new GuiIcon(VANILLAGUI_TEXTURE, 268, 0, 15, 15);
	//UIScrollbar
	public static final GuiIcon SCROLLBAR_BG = new GuiIcon(VANILLAGUI_TEXTURE, 215, 0, 15, 15);
	public static final GuiIcon SCROLLBAR_DISABLED_BG = new GuiIcon(VANILLAGUI_TEXTURE, 215, 15, 15, 15);
	public static final GuiIcon SCROLLBAR_HORIZONTAL = new GuiIcon(VANILLAGUI_TEXTURE, 230, 15, 15, 8);
	public static final GuiIcon SCROLLBAR_HORIZONTAL_DISABLED = new GuiIcon(VANILLAGUI_TEXTURE, 230, 23, 15, 8);
	public static final GuiIcon SCROLLBAR_VERTICAL = new GuiIcon(VANILLAGUI_TEXTURE, 230, 0, 8, 15);
	public static final GuiIcon SCROLLBAR_VERTICAL_DISABLED = new GuiIcon(VANILLAGUI_TEXTURE, 238, 0, 8, 15);
	//UITab window
	public static final GuiIcon TAB_WINDOW_TOP = new GuiIcon(VANILLAGUI_TEXTURE, 208, 66, 15, 15);
	public static final GuiIcon TAB_WINDOW_RIGHT = new GuiIcon(VANILLAGUI_TEXTURE, 223, 66, 15, 15);
	public static final GuiIcon TAB_WINDOW_LEFT = new GuiIcon(VANILLAGUI_TEXTURE, 208, 81, 15, 15);
	public static final GuiIcon TAB_WINDOW_BOTTOM = new GuiIcon(VANILLAGUI_TEXTURE, 223, 81, 15, 15);
	//UITab panel
	public static final GuiIcon TAB_PANEL_TOP = new GuiIcon(VANILLAGUI_TEXTURE, 238, 66, 15, 15);
	public static final GuiIcon TAB_PANEL_RIGHT = new GuiIcon(VANILLAGUI_TEXTURE, 251, 66, 15, 15);
	public static final GuiIcon TAB_PANEL_LEFT = new GuiIcon(VANILLAGUI_TEXTURE, 238, 81, 15, 15);
	public static final GuiIcon TAB_PANEL_BOTTOM = new GuiIcon(VANILLAGUI_TEXTURE, 251, 81, 15, 15);

	private GuiTexture texture = VANILLAGUI_TEXTURE;
	private float u = 0;
	private float v = 0;
	private float U = 1;
	private float V = 1;

	public GuiIcon(float u, float v, float U, float V)
	{
		this.u = u;
		this.v = v;
		this.U = U;
		this.V = V;
	}

	public GuiIcon(GuiTexture texture, float u, float v, float U, float V)
	{
		this(u, v, U, V);
		this.texture = texture;
	}

	public GuiIcon(GuiTexture texture, int x, int y, int width, int height)
	{
		this.texture = texture;

		this.u = texture.pixelToU(x);
		this.v = texture.pixelToV(y);
		this.U = texture.pixelToU(x + width);
		this.V = texture.pixelToV(y + height);
	}

	public GuiIcon(GuiTexture texture, TextureAtlasSprite icon)
	{
		this(texture, icon.getMinU(), icon.getMinV(), icon.getMaxU(), icon.getMaxV());
	}

	public float u()
	{
		return u;
	}

	public float v()
	{
		return v;
	}

	public float U()
	{
		return U;
	}

	public float V()
	{
		return V;
	}

	public float interpolatedU(float i)
	{
		return u() + i * (U() - u());
	}

	public float interpolatedV(float i)
	{
		return v() + i * (V() - v());
	}

	public float pixelU(int px)
	{
		float offset = texture != null ? texture.pixelToU(px) : 0;
		return px >= 0 ? u() + offset : U() + offset;
	}

	public float pixelV(int px)
	{
		float offset = texture != null ? texture.pixelToV(px) : 0;
		return px >= 0 ? v() + offset : V() + offset;
	}

	public GuiIcon flip(boolean horizontal, boolean vertical)
	{
		return new GuiIcon(texture, horizontal ? U() : u(), vertical ? V() : v(), horizontal ? u() : U(), vertical ? v() : V());
	}

	public GuiIcon clip(float xOffset, float yOffset, float width, float height)
	{
		xOffset = (U() - u()) * xOffset;
		yOffset = (V() - v()) * yOffset;
		width = (U() - u()) * width;
		height = (V() - v()) * height;
		return new GuiIcon(texture, u() + xOffset, v() + yOffset, u() + xOffset + width, v() + yOffset + height);
	}

	public GuiIcon copy()
	{
		return new GuiIcon(texture, u(), v(), U(), v());
	}

	public void bind(GuiRenderer renderer)
	{
		renderer.bindTexture(texture);
	}

	@Override
	public String toString()
	{
		String str = u() + "," + v() + " -> " + U() + "," + V();
		if (texture != null)
			str += " [" + texture.pixelFromU(u()) + "," + texture.pixelFromV(v()) + " -> " + texture.pixelFromU(U()) + ","
					+ texture.pixelFromV(V()) + "]";
		return str;
	}

	public static GuiIcon from(ItemStack itemStack)
	{
		if (Minecraft.getMinecraft().getRenderItem() == null)
			return FULL;
		TextureAtlasSprite icon = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getParticleIcon(itemStack.getItem(),
																												itemStack.getMetadata());

		return new GuiIcon(MalisisGui.BLOCK_TEXTURE, icon);
	}

	public static GuiIcon from(Item item)
	{
		return from(new ItemStack(item));
	}

	public static GuiIcon from(IBlockState state)
	{
		TextureAtlasSprite icon = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getTexture(state);

		return new GuiIcon(MalisisGui.BLOCK_TEXTURE, icon);
	}

	public static GuiIcon from(Block block)
	{
		return from(block.getDefaultState());
	}

	public static Supplier<GuiIcon> forComponent(UIComponent component, GuiIcon icon, GuiIcon hover, GuiIcon disabled)
	{
		return () -> {
			if (disabled != null && component.isDisabled())
				return disabled;
			else if (hover != null && component.isHovered())
				return hover;
			return icon;
		};
	}
}
