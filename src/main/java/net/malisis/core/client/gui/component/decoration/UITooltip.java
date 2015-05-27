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

package net.malisis.core.client.gui.component.decoration;

import java.util.Arrays;
import java.util.List;

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.IGuiText;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.element.XYResizableGuiShape;
import net.malisis.core.renderer.animation.Animation;
import net.malisis.core.renderer.animation.transformation.AlphaTransform;
import net.malisis.core.renderer.font.FontRenderOptions;
import net.malisis.core.renderer.font.MalisisFont;

/**
 * UITooltip
 *
 * @author PaleoCrafter
 */
public class UITooltip extends UIComponent implements IGuiText<UITooltip>
{
	/** The {@link MalisisFont} to use for this {@link UITooltip}. */
	protected MalisisFont font = MalisisFont.minecraftFont;
	/** The {@link FontRenderOptions} to use for this {@link UITooltip}. */
	protected FontRenderOptions fro = new FontRenderOptions();

	protected List<String> lines;
	protected int padding = 4;
	protected int delay = 0;
	protected Animation animation;

	public UITooltip(MalisisGui gui)
	{
		super(gui);
		setSize(16, 16);
		zIndex = 300;
		fro.color = 0xFFFFFF;
		fro.shadow = true;

		shape = new XYResizableGuiShape();
		icon = gui.getGuiTexture().getXYResizableIcon(227, 31, 15, 15, 5);

		animation = new Animation(this, new AlphaTransform(0, 255).forTicks(2));
	}

	public UITooltip(MalisisGui gui, String text)
	{
		this(gui);
		setText(text);
	}

	public UITooltip(MalisisGui gui, int delay)
	{
		this(gui);
		setDelay(delay);
	}

	public UITooltip(MalisisGui gui, String text, int delay)
	{
		this(gui);
		setText(text);
		setDelay(delay);
	}

	//#region Getters/Setters
	@Override
	public MalisisFont getFont()
	{
		return font;
	}

	@Override
	public UITooltip setFont(MalisisFont font)
	{
		this.font = font;
		calculateSize();
		return this;
	}

	@Override
	public FontRenderOptions getFontRenderOptions()
	{
		return fro;
	}

	@Override
	public UITooltip setFontRenderOptions(FontRenderOptions fro)
	{
		this.fro = fro;
		calculateSize();
		return this;
	}

	public UITooltip setText(String text)
	{
		lines = Arrays.asList(text.split("\\n"));
		calculateSize();
		return this;

	}

	public UITooltip setText(List<String> lines)
	{
		this.lines = lines;
		calculateSize();
		return this;
	}

	public UITooltip setDelay(int delay)
	{
		this.delay = delay;
		return this;
	}

	public int getDelay()
	{
		return delay;
	}

	protected int getOffsetX()
	{
		return 8;
	}

	protected int getOffsetY()
	{
		return -16;
	}

	//#end Getters/Setters

	protected void calculateSize()
	{

		width = Math.max(16, (int) font.getMaxStringWidth(lines, fro.fontScale));
		width += padding * 2;
		height = (int) (lines.size() > 1 ? font.getStringHeight(fro.fontScale) * lines.size() : 8);
		height += padding * 2;
	}

	public void animate()
	{
		if (delay == 0)
			return;

		setAlpha(0);
		getGui().animate(animation, delay);
	}

	@Override
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		shape.setPosition(mouseX + getOffsetX(), mouseY + getOffsetY());
		rp.icon.set(icon);
		renderer.drawShape(shape, rp);
	}

	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		int x = mouseX + getOffsetX() + padding;
		int y = mouseY + getOffsetY() + padding;
		int i = 0;
		for (String str : lines)
		{
			int sy = y;
			if (i > 0)
				sy += 2;
			renderer.drawText(font, str, x, sy + font.getStringHeight(fro.fontScale) * i, zIndex + 1, fro, false);
			i++;
		}
	}

}
