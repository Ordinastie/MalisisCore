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

package net.malisis.core.inventory;

import net.malisis.core.client.gui.MalisisGui;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public interface IInventoryProvider
{
	/**
	 * Gets the {@link MalisisInventory} for this {@link IInventoryProvider}.
	 *
	 * @param data null for TileEntity, ItemStack for Item
	 * @return the inventories
	 */
	public MalisisInventory[] getInventories(Object... data);

	/**
	 * Gets the {@link MalisisInventory} for a specific side for this {@link IInventoryProvider}.
	 *
	 * @param side the side
	 * @param data null for TileEntity, ItemStack for Item
	 * @return the inventories
	 */
	public MalisisInventory[] getInventories(ForgeDirection side, Object... data);

	/**
	 * Gets the {@link MalisisGui} associated with the {@link MalisisInventory}.
	 *
	 * @param container the container
	 * @return the GUI to open
	 */
	@SideOnly(Side.CLIENT)
	public MalisisGui getGui(MalisisInventoryContainer container);

}
