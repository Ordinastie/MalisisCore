package net.malisis.core.client.gui;

public class Anchor
{
	public static int NONE = 0;
	public static int TOP = 1;
	public static int BOTTOM = 2;
	public static int MIDDLE = 3;
	public static int LEFT = 4;
	public static int RIGHT = 8;
	public static int CENTER = 12;
	
	public static int horizontal(int anchor)
	{
		return anchor & 12; 
	}
	
	public static int vertical(int anchor)
	{
		return anchor & 3;
	}
}
