package net.malisis.core.event;

import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.eventhandler.Event;

public abstract class MCEvent extends Event
{
	
	
	public boolean post()
	{
		return MinecraftForge.EVENT_BUS.post(this);
	}
}
