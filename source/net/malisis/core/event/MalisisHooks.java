package net.malisis.core.event;

import net.minecraftforge.common.MinecraftForge;

public class MalisisHooks
{
	public static boolean onKeyPressed()
	{
		return MinecraftForge.EVENT_BUS.post(new KeyboardEvent());
	}
}
