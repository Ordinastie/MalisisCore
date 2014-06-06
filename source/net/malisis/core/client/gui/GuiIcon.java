package net.malisis.core.client.gui;

import net.minecraft.util.IIcon;

public class GuiIcon implements IIcon
{
	//@formatter:off
	public static GuiIcon iconFixedSized = new GuiIcon(0F, 0F, 1F, 1F);
	
	public static GuiIcon[] iconsXYResizable = new GuiIcon[] { 	new GuiIcon(0, 		0, 		0.33F, 	0.33F),
																new GuiIcon(0.33F, 	0, 		0.66F, 	0.33F),
																new GuiIcon(0.66F, 	0, 		1F, 	0.33F),
																new GuiIcon(0, 		0.33F, 	0.33F, 	0.66F),
																new GuiIcon(0.33F, 	0.33F, 	0.66F, 	0.66F),
																new GuiIcon(0.66F, 	0.33F, 	1.0F, 	0.66F),
																new GuiIcon(0, 		0.66F, 	0.33F, 	1.0F),
																new GuiIcon(0.33F, 	0.66F, 	0.66F, 	1.0F),
																new GuiIcon(0.66F, 	0.66F, 	1.0F, 	1.0F)};
	
	public static GuiIcon[] iconsXResizable = new GuiIcon[] { 	new GuiIcon(0, 		0, 		0.33F, 	1.0F),
																new GuiIcon(0.33F, 	0, 		0.66F, 	1.0F),
																new GuiIcon(0.66F, 	0, 		1.0F, 	1.0F)};
	//@formatter:on
	
	//TODO: dynamic load
	private static final int GUI_TEXTURE_WIDTH = 300;
	private static final int GUI_TEXTURE_HEIGHT = 100;
	
	private int x, y, width, height;
	private float u, v, U, V;

	public GuiIcon(float u, float v, float U, float V)
	{
		this.u = u;
		this.v = v;
		this.U = U;
		this.V = V;
		this.x = (int) (u * GUI_TEXTURE_WIDTH);
		this.y = (int) (v * GUI_TEXTURE_WIDTH);
		this.width = (int) (U * GUI_TEXTURE_WIDTH) - x;
		this.height = (int) (V * GUI_TEXTURE_WIDTH) - y;
	}
	
	public GuiIcon(int x, int y, int width, int height)
	{
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.u = (float) x / GUI_TEXTURE_WIDTH;
		this.U = (float) (x + width) / GUI_TEXTURE_WIDTH;
		this.v = (float) y / GUI_TEXTURE_HEIGHT;
		this.V = (float) (y + height) / GUI_TEXTURE_HEIGHT;		
	}

	@Override
	public int getIconWidth()
	{
		return width;
	}

	@Override
	public int getIconHeight()
	{
		return height;
	}

	@Override
	public float getMinU()
	{
		return u;
	}

	@Override
	public float getMaxU()
	{
		return U;
	}

	@Override
	public float getInterpolatedU(double f)
	{
		return (float) (U * f);
	}

	@Override
	public float getMinV()
	{
		return v;
	}

	@Override
	public float getMaxV()
	{
		return V;
	}

	@Override
	public float getInterpolatedV(double f)
	{
		return (float) (v * f);
	}

	@Override
	public String getIconName()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	public GuiIcon offset(int offsetX, int offsetY)
	{
		return new GuiIcon(x + offsetX, y + offsetY, width, height);
	}
	
}
