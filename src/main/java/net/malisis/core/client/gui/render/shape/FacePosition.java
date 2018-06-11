package net.malisis.core.client.gui.render.shape;

import java.util.EnumSet;
import java.util.Set;

public enum FacePosition
{
	TOPLEFT(0, 0, 0xd95757),
	TOP(1, 0, 0x6cd957),
	TOPRIGHT(2, 0, 0x576cd9),
	LEFT(0, 1, 0x57d9a3),
	CENTER(1, 1, 0xad57d9),
	RIGHT(2, 1, 0xd9cb57),
	BOTTOMRIGHT(0, 2, 0x8ed957),
	BOTTOM(1, 2, 0xb86ad9),
	BOTTOMLEFT(2, 2, 0x6a9bd9);

	public final static Set<FacePosition> VALUES = EnumSet.allOf(FacePosition.class);
	private int x, y;

	FacePosition(int x, int y, int color)
	{
		this.x = x;
		this.y = y;
	}

	public int x()
	{
		return x;
	}

	public int y()
	{
		return y;
	}

	//if position coordinate > 0, a corner is "behind", if position coordinate > 1, a size dependant center is "behind"
	public int x(int width, int border)
	{
		return (x > 1 ? width : 0) + (x > 0 ? border : 0);
	}

	public int y(int height, int border)
	{
		return (y > 1 ? height : 0) + (y > 0 ? border : 0);
	}

	public int width(int width, int border)
	{
		return x == 1 ? width : border;
	}

	public int height(int height, int border)
	{
		return y == 1 ? height : border;
	}

}
