package net.malisis.core.demo.test;

import net.minecraft.util.IIcon;

public class GuiIcon implements IIcon
{
	private float u, v, U, V;

	public GuiIcon(float u, float v, float U, float V)
	{
		this.u = u;
		this.v = v;
		this.U = U;
		this.V = V;
	}

	@Override
	public int getIconWidth()
	{
		return 0;
	}

	@Override
	public int getIconHeight()
	{
		return 0;
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
}
