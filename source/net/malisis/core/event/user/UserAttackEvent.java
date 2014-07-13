package net.malisis.core.event.user;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public class UserAttackEvent extends UserEvent
{
	public EntityPlayer player;
	public Entity target;

	public UserAttackEvent(EntityPlayer player, Entity target)
	{
		this.player = player;
		this.target = target;
	}
}
