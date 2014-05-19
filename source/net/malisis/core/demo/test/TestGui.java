package net.malisis.core.demo.test;

import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.proxy.GuiScreenProxy;
import net.malisis.core.client.gui.renderer.GuiRenderer;
import net.malisis.core.client.gui.util.shape.Point;
import net.minecraft.client.Minecraft;

public class TestGui extends GuiScreenProxy
{ 
	private GuiRenderer renderer;
	private boolean renderGuiScreenBackground = true;
	boolean centerContainer = true;
	
	public TestGui(UIContainer container)
	{
		this.renderer = new GuiRenderer();
		this.container = container;
	}
	
	@Override
	public void setWorldAndResolution(Minecraft minecraft, int width, int height)
	{
		super.setWorldAndResolution(minecraft, width, height);
		renderer.setFontRenderer(fontRendererObj);
		if(centerContainer)
			container.setPosition(new Point((width - container.getWidth())/2, (height - container.getHeight())/2));
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
		if(renderGuiScreenBackground)
			drawWorldBackground(1);
		
		renderer.drawScreen(container, mouseX, mouseY, partialTicks);		
	}
	

	public void display()
	{
		Minecraft.getMinecraft().displayGuiScreen(this);
	}
}
