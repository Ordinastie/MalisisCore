/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Ordinastie
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.malisis.core.inventory.cache;

import java.util.List;
import java.util.stream.Collectors;

import net.malisis.core.inventory.MalisisInventory;
import net.malisis.core.inventory.MalisisSlot;
import net.malisis.core.inventory.message.UpdateInventorySlotsMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * @author Ordinastie
 *
 */
public class InventoryCache
{
	private EntityPlayerMP player;
	private int inventoryId;
	private int windowId;
	private List<CachedSlot> slotCache;

	public InventoryCache(EntityPlayer player, MalisisInventory inventory, int windowId)
	{
		this.player = (EntityPlayerMP) player;
		inventoryId = inventory.getInventoryId();
		this.windowId = windowId;
		slotCache = inventory.getSlots().stream().map(CachedSlot::new).collect(Collectors.toList());
	}

	public void update()
	{
		slotCache.forEach(CachedSlot::update);
	}

	public List<MalisisSlot> getSlots(boolean sendAll)
	{
		return slotCache.stream()
						.peek(CachedSlot::update)
						.filter(cs -> sendAll || cs.hasChanged())
						.map(CachedSlot::getSlot)
						.collect(Collectors.toList());
	}

	private void sendSlots(boolean sendAll)
	{
		List<MalisisSlot> changedSlots = getSlots(sendAll);
		if (changedSlots.size() > 0)
			UpdateInventorySlotsMessage.updateSlots(inventoryId, changedSlots, player, windowId);
	}

	public void sendAll()
	{
		sendSlots(true);
	}

	public void sendChanges()
	{
		sendSlots(false);
	}
}
