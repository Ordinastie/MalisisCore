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

package net.malisis.core.util.remapping;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.malisis.core.MalisisCore;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.FMLMissingMappingsEvent;
import net.minecraftforge.fml.common.event.FMLMissingMappingsEvent.MissingMapping;

/**
 * @author Ordinastie
 *
 */
public class RemappingTool
{
	private static RemappingTool instance = new RemappingTool();

	private Set<ModContainer> mods = new HashSet<>();
	private Map<String, Block> blocks = new HashMap<>();
	private Map<String, Item> items = new HashMap<>();

	private ModContainer getContainer(String name)
	{
		ModContainer mod = null;
		int index = name.indexOf(":");
		if (index == -1)
		{
			MalisisCore.log.warn("[RemappingTool] No mod ID found for {}, using current active container.", name);
			return Loader.instance().activeModContainer();
		}

		String modid = name.substring(0, index);
		mod = Loader.instance().getIndexedModList().get(modid);
		if (mod == null)
		{
			MalisisCore.log.warn("[RemappingTool] No mod found for {}, using dummy container.", name);
			ModMetadata md = new ModMetadata();
			md.modId = modid;
			return new DummyModContainer(md);
		}

		return mod;
	}

	public static void remap(String old, Block block)
	{
		instance.mods.add(instance.getContainer(old));
		instance.blocks.put(old, block);
		Item item = Item.getItemFromBlock(block);
		if (item instanceof ItemBlock)
			remap(old, item);
	}

	public static void remap(String old, Item item)
	{
		instance.mods.add(instance.getContainer(old));
		instance.items.put(old, item);
	}

	public static void processMissingMappings(FMLMissingMappingsEvent event)
	{
		for (ModContainer mod : instance.mods)
		{
			event.applyModContainer(mod);
			instance.processMappings(event.get());
		}

	}

	private void processMappings(List<MissingMapping> mappings)
	{
		for (FMLMissingMappingsEvent.MissingMapping missingMapping : mappings)
		{
			switch (missingMapping.type)
			{
				case BLOCK:
					if (instance.blocks.containsKey(missingMapping.name))
					{
						MalisisCore.log.info("Remapping {} to {}", missingMapping.name, instance.blocks.get(missingMapping.name));
						missingMapping.remap(instance.blocks.get(missingMapping.name));
					}
					break;
				case ITEM:
					if (instance.items.containsKey(missingMapping.name))
					{
						missingMapping.remap(instance.items.get(missingMapping.name));
						MalisisCore.log.info("Remapping {} to {}", missingMapping.name, instance.items.get(missingMapping.name));
					}

					break;
			}
		}
	}

	public static RemappingTool instance()
	{
		return instance;
	}

}
