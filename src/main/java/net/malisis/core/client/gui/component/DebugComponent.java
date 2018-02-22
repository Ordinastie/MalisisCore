/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 Ordinastie
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

package net.malisis.core.client.gui.component;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Sets;

import net.malisis.core.MalisisCore;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.element.Padding;
import net.malisis.core.client.gui.component.element.Padding.IPadded;
import net.malisis.core.client.gui.component.element.Size.DynamicSize;
import net.malisis.core.client.gui.component.element.Sizes;
import net.malisis.core.client.gui.render.ColoredBackground;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.renderer.font.MalisisFont;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.math.MathHelper;

/**
 * @author Ordinastie
 *
 */
public class DebugComponent extends UIComponent<DebugComponent> implements IPadded
{
	private HashMap<String, Supplier<String>> debugMap = new LinkedHashMap<>();
	private FontOptions fontOptions = FontOptions.builder().color(0xFFFFFF).shadow().build();
	private Padding padding = Padding.of(5, 5);

	public DebugComponent(MalisisGui gui)
	{
		super(gui);
		enabled = false;

		setSize(new DynamicSize(Sizes.relativeWidth(1.0f), owner -> {
			return (int) ((debugMap.size() + 1) * MalisisFont.minecraftFont.getStringHeight()) + padding.vertical();
		}));
		ColoredBackground background = new ColoredBackground(0x000000);
		background.setAlpha(0);
		setBackground(background);
	}

	@Override
	@Nonnull
	public Padding getPadding()
	{
		return padding;
	}

	@Override
	public DebugComponent setEnabled(boolean enabled)
	{
		if (enabled)
			getGui().addToScreen(this);
		else
			getGui().removeFromScreen(this);
		return super.setEnabled(enabled);
	}

	public void toggle()
	{
		setEnabled(!isEnabled());
	}

	public void clear()
	{
		debugMap.clear();
	}

	public void addDebug(String name, Supplier<String> supplier)
	{
		debugMap.put(name, supplier);
	}

	public void removeDebug(String name)
	{
		debugMap.remove(name);
	}

	@Override
	public boolean onKeyTyped(char keyChar, int keyCode)
	{
		return super.onKeyTyped(keyChar, keyCode);
	}

	@Override
	public boolean onScrollWheel(int x, int y, int delta)
	{
		if (!isHovered())
			return false;

		if (GuiScreen.isCtrlKeyDown())
		{
			float scale = fontOptions.getFontScale();
			scale += 1 / 3F * delta;
			scale = MathHelper.clamp(scale, 1 / 3F, 1);

			fontOptions = fontOptions.toBuilder().scale(scale).build();
			MalisisCore.message(scale);
		}
		else if (GuiScreen.isShiftKeyDown())
		{
			ColoredBackground background = (ColoredBackground) backgroundRenderer;
			int alpha = background.getTopLeftAlpha();
			alpha += 25 * delta;
			alpha = MathHelper.clamp(alpha, 0, 255);

			background.setAlpha(alpha);
		}

		return true;
	}

	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		UIComponent<?> hoveredComponent = MalisisGui.getHoveredComponent();

		drawHierarchy(renderer, hoveredComponent);

		//hard code mouse
		int dy = 1, oy = getPadding().top();
		MalisisFont font = MalisisFont.minecraftFont;

		String mouseText = "Mouse : " + mouseX + "," + mouseY;
		if (hoveredComponent != null)
			mouseText += "(" + hoveredComponent.relativeX(mouseX) + ", " + hoveredComponent.relativeY(mouseY) + ")";

		renderer.drawText(null, mouseText, getPadding().left(), getPadding().top(), 0, fontOptions, false);
		for (Entry<String, Supplier<String>> entry : debugMap.entrySet())
			renderer.drawText(	null,
								entry.getKey() + " : " + entry.getValue().get(),
								5,
								dy++ * font.getStringHeight(fontOptions) + oy,
								0,
								fontOptions,
								false);
	}

	public void drawHierarchy(GuiRenderer renderer, UIComponent<?> component)
	{
		if (component == null || !GuiScreen.isAltKeyDown())
			return;

		Set<UIComponent<?>> components = Sets.newLinkedHashSet();
		while (component != null)
		{
			if (component.getParent() != null) //don't add the screen component
				components.add(component);
			component = component.getParent();
		}

		//if only one component offset is never used
		int offset = components.size() > 1 ? Math.min(100, 200 / (components.size() - 1)) : 0;
		offset = 80;
		//green/blue
		int r = 255;
		int g = 0;
		int b = 0;
		int z = 100;

		renderer.next(GL11.GL_LINE_LOOP);
		GL11.glLineWidth(3);
		for (UIComponent<?> comp : components)
		{
			renderer.drawRectangle(	comp.screenX(),
									comp.screenY(),
									z--,
									comp.size().width(),
									comp.size().height(),
									(r << 16) + (g << 8) + b,
									255,
									false);
			r -= Math.max(offset, 0);
			g = Math.min(g + offset, 255);
			b = Math.min(b + 2 * offset, 255);
		}
		renderer.next(GL11.GL_QUADS);
	}
}
