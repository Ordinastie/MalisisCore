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

package net.malisis.core.util.replacement;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import net.malisis.core.MalisisCore;
import net.malisis.core.asm.AsmUtils;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.stats.StatList;
import net.minecraft.util.RegistryNamespaced;
import net.minecraftforge.client.event.TextureStitchEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.FMLControlledNamespacedRegistry;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Ordinastie
 *
 */
public class ReplacementTool
{
	private static ReplacementTool instance = new ReplacementTool();

	/** List of original {@link Block} being replaced. The key is the replacement, the value is the Vanilla {@code Block}. */
	private HashMap<Block, Block> originalBlocks = new HashMap<>();
	/** List of original {@link Item} being replaced. The key is the replacement, the value is the Vanilla {@code Item}. */
	private HashMap<Item, Item> originalItems = new HashMap<>();

	private Class[] types = { Integer.TYPE, String.class, Object.class };
	private Method method = ReflectionHelper.findMethod(FMLControlledNamespacedRegistry.class, (FMLControlledNamespacedRegistry) null,
			new String[] { "addObjectRaw" }, types);

	private ReplacementTool()
	{
		new ShapedOreRecipeHandler();
		new ShapedRecipesHandler();
		new ShapelessRecipesHandler();
		new ShapelessOreRecipeHandler();
		new StatCraftingHandler();
	}

	/**
	 * Texture stitch event.<br>
	 * Used to register the icons of the replaced vanilla blocks since they're not in the registry anymore and won't be called to register
	 * their icons.
	 *
	 * @param event the event
	 */
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onTextureStitchEvent(TextureStitchEvent.Pre event)
	{
		if (event.map.getTextureType() == 1)
			return;

		for (Entry<Block, Block> entry : originalBlocks.entrySet())
		{
			Block block = entry.getValue();
			block.registerIcons(event.map);
		}
	}

	/**
	 * Get the {@link ReplacementTool} instance
	 *
	 * @return the replacement tool
	 */
	public static ReplacementTool instance()
	{
		return instance;
	}

	/**
	 * Replaces a vanilla {@link Block} or {@link Item} with a new one.<br>
	 * Changes the instance inside the registry<br>
	 * Changes the instance inside recipes<br>
	 * Changes the instance inside stats<br>
	 * For blocks, changes the instance inside the corresponding ItemBlock if any.
	 *
	 * @param id the id
	 * @param name the name
	 * @param srgFieldName the srg field name
	 * @param replacement the replacement
	 * @param vanilla the vanilla
	 */
	private void replaceVanilla(int id, String name, String srgFieldName, Object replacement, Object vanilla)
	{
		boolean block = replacement instanceof Block;
		RegistryNamespaced registry = block ? Block.blockRegistry : Item.itemRegistry;
		ItemBlock ib = block ? (ItemBlock) Item.getItemFromBlock((Block) vanilla) : null;
		Class<?> clazz = block ? Blocks.class : Items.class;
		HashMap map = block ? originalBlocks : originalItems;

		try
		{
			method.invoke(registry, id, "minecraft:" + name, replacement);
			Field f = AsmUtils.changeAccess(clazz, name, srgFieldName);
			f.set(null, replacement);

			if (ib != null)
				AsmUtils.changeAccess(ItemBlock.class, "blockInstance", "field_150939_a").set(ib, replacement);

			map.put(replacement, vanilla);
			replaceIn(CraftingManager.getInstance().getRecipeList(), vanilla, replacement);
			replaceIn(StatList.allStats, vanilla, replacement);
		}
		catch (ReflectiveOperationException e)
		{
			e.printStackTrace();
		}
	}

	public void replaceIn(List<?> list, Object vanilla, Object replacement) throws ReflectiveOperationException
	{
		for (Object object : list)
		{
			ReplacementHandler rh = ReplacementHandler.getHandler(object);
			if (rh != null)
			{
				if (rh.replace(object, vanilla, replacement))
					MalisisCore.log.info("Replaced {} by {} in {}", vanilla.getClass().getSimpleName(), replacement.getClass()
							.getSimpleName(), object.getClass().getSimpleName());
			}
		}
	}

	/**
	 * Replaces vanilla block with another one.<br>
	 * Changes the registry by removing the vanilla block and adding the replacement.
	 *
	 * @param id the id
	 * @param name the name
	 * @param srgFieldName the srg field name
	 * @param replacement the block
	 * @param vanilla the vanilla
	 */
	public static void replaceVanillaBlock(int id, String name, String srgFieldName, Block replacement, Block vanilla)
	{
		instance().replaceVanilla(id, name, srgFieldName, replacement, vanilla);
	}

	/**
	 * Replace vanilla item.
	 *
	 * @param id the id
	 * @param name the name
	 * @param srgFieldName the srg field name
	 * @param replacement the replacement
	 * @param vanilla the vanilla
	 */
	public static void replaceVanillaItem(int id, String name, String srgFieldName, Item replacement, Item vanilla)
	{
		instance().replaceVanilla(id, name, srgFieldName, replacement, vanilla);
	}

	/**
	 * Gets the original/vanilla block for the specified one.
	 *
	 * @param block the block
	 * @return the block
	 */
	public static Block orignalBlock(Block block)
	{
		return instance.originalBlocks.get(block);
	}

	/**
	 * Gets the orginal/vanilla item for the specified one.
	 *
	 * @param item the item
	 * @return the item
	 */
	public static Item originalItem(Item item)
	{
		return instance.originalItems.get(item);
	}
}
