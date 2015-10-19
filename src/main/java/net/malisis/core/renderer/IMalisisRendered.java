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

package net.malisis.core.renderer;

import java.util.function.Consumer;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.registry.GameData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * This interface indicates the implementing {@link Block} or {@link Item} to be rendered using the provided {@link MalisisRenderer}.<br>
 * The renderer is fetched only once in {@link FMLInitializationEvent} phase.
 *
 * @author Ordinastie
 */
public interface IMalisisRendered
{

	/**
	 * Gets the {@link MalisisRenderer} to be used for this {@link Block} or {@link Item}.<br>
	 * If the renderer is used for other elements, register them returning it.
	 *
	 * @return the renderer
	 */
	@SideOnly(Side.CLIENT)
	public default MalisisRenderer getRenderer()
	{
		return this instanceof Block ? DefaultRenderer.block : DefaultRenderer.item;
	}

	/**
	 * Registers the {@link MalisisRenderer renderers} for all the {@link Block blocks} and {@link Item items} that implement
	 * {@link IMalisisRendered}.<br>
	 * Called during {@link FMLInitializationEvent} phase.
	 */
	public static void registerRenderers()
	{
		Consumer<?> consumer = (obj) -> {
			if (obj instanceof IMalisisRendered)
			{
				MalisisRenderer renderer = ((IMalisisRendered) obj).getRenderer();
				if (renderer == null)
					return;

				if (obj instanceof Block)
					renderer.registerFor((Block) obj);
				else
					renderer.registerFor((Item) obj);
			}
		};

		GameData.getBlockRegistry().forEach(consumer);
		GameData.getItemRegistry().forEach(consumer);
	}
}
