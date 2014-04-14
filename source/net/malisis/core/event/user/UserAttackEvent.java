package net.malisis.core.event.user;

import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public class UserAttackEvent extends UserEvent
{
	public EntityClientPlayerMP player;
	public Entity target;
	
	public UserAttackEvent(EntityPlayer player, Entity target)
	{
		this.player = (EntityClientPlayerMP) player;
		this.target = target;
	}
}
