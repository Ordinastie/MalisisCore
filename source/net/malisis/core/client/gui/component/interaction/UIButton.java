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

package net.malisis.core.client.gui.component.interaction;

import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.event.ButtonPressedEvent;
import net.malisis.core.client.gui.event.MouseClickedEvent;
import net.malisis.core.client.gui.renderer.DynamicTexture;
import net.malisis.core.client.gui.renderer.GuiRenderer;
import net.malisis.core.client.gui.util.Size;
import net.malisis.core.client.gui.util.shape.Point;
import net.malisis.core.client.gui.util.shape.Rectangle;
import net.malisis.core.demo.test.GuiIcon;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.preset.ShapePreset;
import net.malisis.core.util.RenderHelper;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;

import com.google.common.eventbus.Subscribe;

/**
 * UIButton
 * 
 * @author PaleoCrafter
 */
public class UIButton extends UIComponent
{

	private static final DynamicTexture TEXTURE_NORMAL = new DynamicTexture(new ResourceLocation("malisiscore",
			"textures/gui/widgets/button.png"), 15, 15, 0, 0, new Rectangle(0, 0, 5, 5), new Rectangle(5, 0, 5, 5), new Rectangle(5, 5, 5,
			5));
	private static final DynamicTexture TEXTURE_HOVERED = new DynamicTexture(new ResourceLocation("malisiscore",
			"textures/gui/widgets/button_hovered.png"), 15, 15, 0, 0, new Rectangle(0, 0, 5, 5), new Rectangle(5, 0, 5, 5), new Rectangle(
			5, 5, 5, 5));
	private static final DynamicTexture TEXTURE_DISABLED = new DynamicTexture(new ResourceLocation("malisiscore",
			"textures/gui/widgets/button_disabled.png"), 15, 15, 0, 0, new Rectangle(0, 0, 5, 5), new Rectangle(5, 0, 5, 5), new Rectangle(
			5, 5, 5, 5));

	private String text;

	public UIButton()
	{
		this("", 0, 0);
	}

	public UIButton(String text, int width, int height)
	{
		this.text = text;
		this.setSize(width, height);
	}

	@Subscribe
	public void onMouseClick(MouseClickedEvent event)
	{
		if (this.isEnabled() && this.isHovered(event.getPosition()) && event.getButton().isLeft())
		{
			this.mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
			this.getContext().publish(new ButtonPressedEvent(this));
		}
	}

	@Override
	public void draw(int mouseX, int mouseY)
	{
		int textColor = 0xe0e0e0;
		DynamicTexture texture = TEXTURE_NORMAL;
		if (this.isEnabled())
		{
			if (this.isHovered(new Point(mouseX, mouseY)))
			{
				textColor = 0xffffa0;
				texture = TEXTURE_HOVERED;
			}
		}
		else
		{
			textColor = 0xa0a0a0;
			texture = TEXTURE_DISABLED;
		}

		texture.setSize(this.getSize());
		texture.draw(getScreenX(), getScreenY());

		RenderHelper.drawString(text, getScreenX(), getScreenY(), zIndex, getWidth(), getHeight(), textColor, true);
	}

	@Override
	public void update(int mouseX, int mouseY)
	{

	}

	@Override
	public String toString()
	{
		return this.getClass().getName() + "[ text=" + text + ", " + this.getPropertyString() + " ]";
	}

	/***
	 * V2 Ordinastie
	 */
	private ResourceLocation texture = new ResourceLocation("malisiscore", "textures/gui/widgets/button.png");
	//@formatter:off
	public GuiIcon[] icons = new GuiIcon[] { 
		new GuiIcon(0, 		0, 		0.33F, 	1.0F),
		new GuiIcon(0.33F, 	0, 		0.66F, 	1.0F),
		new GuiIcon(0.66F, 	0, 		1.0F, 	1.0F)};
	//@formatter:on
	
	@Override
	public GuiIcon getIcon(int face)
    {
    	if(face < 0 || face > icons.length)
    		return null;
    	
    	return icons[face];
    }
	
	@Override
	public ResourceLocation getTexture(int mouseX, int mouseY)
	{
		return texture;
	}

	@Override
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		Shape shape = ShapePreset.GuiXResizable(this.size.width, this.size.height);
		renderer.drawShape(shape);

	}

	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		Size textSize = renderer.textRenderSize(text);
		int x = (size.width - textSize.width) / 2;
		int y = (size.height - textSize.height) / 2;
				
		renderer.drawString(text, screenX() +  x, screenY() + y, 0, 0xFFFFFF, true);		
	}

}
