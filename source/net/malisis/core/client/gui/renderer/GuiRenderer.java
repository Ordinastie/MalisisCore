package net.malisis.core.client.gui.renderer;

import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.util.Size;
import net.malisis.core.renderer.BaseRenderer;
import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.RenderParameters;
import net.malisis.core.renderer.element.Shape;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.client.FMLClientHandler;

public class GuiRenderer extends BaseRenderer
{
	private FontRenderer fontRenderer;
	public UIComponent currentComponent;
	public int mouseX;
	public int mouseY;

	
	public void set(int mouseX, int mouseY, float partialTicks)
	{
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		this.partialTick = partialTicks;
	}

	public void setFontRenderer(FontRenderer fontRenderer)
	{
		this.fontRenderer = fontRenderer;
	}
	
	public void drawScreen(UIContainer container, int mouseX, int mouseY, float partialTick)
	{
		set(mouseX, mouseY, partialTick);

		GL11.glEnable(GL11.GL_TEXTURE_2D);
		
		if (container != null)
		{
			t.startDrawingQuads();
			container.draw(this, mouseX, mouseY, partialTick);
			t.draw();
		}
	}
	
	public void drawShape(Shape s, RenderParameters rp)
	{
		if(s == null)
			return;
		
		s.translate(currentComponent.screenX(), currentComponent.screenY(), 0);
		shape = s;
		rp = new RenderParameters(rp);
		shapeParams = rp;
		s.applyMatrix();
		
		Face[] faces = s.getFaces();		
		for(int i = 0; i < faces.length; i++)
		{
			rp.icon = currentComponent.getIcon(i);
			drawFace(faces[i], rp);
		}
	}
	

	public void drawString(String text, int x, int y, int z, int color, boolean shadow)
	{
		if(fontRenderer == null)
			return;
		
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glTranslatef(0, 0, z);
        fontRenderer.drawString(text, x, y, color, shadow);
        GL11.glTranslatef(0, 0, -z);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
	}
	
	public Size textRenderSize(String text)
	{
		if(fontRenderer == null)
			return new Size(0, 0);
		
		return new Size(fontRenderer.getStringWidth(text), fontRenderer.FONT_HEIGHT);
	}


	public void bindTexture(ResourceLocation rl)
	{
		if(rl != null)
			FMLClientHandler.instance().getClient().getTextureManager().bindTexture(rl);
	}
}
