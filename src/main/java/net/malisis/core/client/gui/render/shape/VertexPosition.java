package net.malisis.core.client.gui.render.shape;

import java.util.EnumSet;
import java.util.Set;

public enum VertexPosition
{
	TOPLEFT(0, 0, 0xd9746a),
	BOTTOMLEFT(0, 1, 0x6abbd9),
	BOTTOMRIGHT(1, 1, 0x91d96a),
	TOPRIGHT(1, 0, 0xf588ec);

	public static final Set<VertexPosition> VALUES = EnumSet.allOf(VertexPosition.class);
	private int x, y;

	VertexPosition(int x, int y, int color)
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

	public int x(int width)
	{
		return x * width;
	}

	public int y(int height)
	{
		return y * height;
	}

}