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

import net.malisis.core.MalisisCore;
import net.malisis.core.asm.AsmUtils;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.stats.StatList;
import net.minecraft.util.IntIdentityHashBiMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraftforge.fml.common.registry.FMLControlledNamespacedRegistry;
import net.minecraftforge.fml.common.registry.GameData;
import net.minecraftforge.fml.common.registry.IForgeRegistryEntry;
import net.minecraftforge.fml.common.registry.RegistryDelegate.Delegate;

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

	private Class<?>[] types = { Integer.TYPE, ResourceLocation.class, IForgeRegistryEntry.class };
	private Method addObjectRaw = AsmUtils.changeMethodAccess(FMLControlledNamespacedRegistry.class, "addObjectRaw", types);
	private Method setName = AsmUtils.changeMethodAccess(Delegate.class, "setName", ResourceLocation.class);
	private Field underlyingMap = AsmUtils.changeFieldAccess(RegistryNamespaced.class, "underlyingIntegerMap", "field_148759_a");
	private Field objectArray = AsmUtils.changeFieldAccess(IntIdentityHashBiMap.class, "keys", "field_186818_b");

	private ReplacementTool()
	{
		new ShapedOreRecipeHandler();
		new ShapedRecipesHandler();
		new ShapelessRecipesHandler();
		new ShapelessOreRecipeHandler();
		new StatCraftingHandler();
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
	 * @param registryName the registry name
	 * @param fieldName the field name in {@link Blocks} or {@link Items} class.
	 * @param srgFieldName the srg field name in {@link Blocks} or {@link Items} class.
	 * @param replacement the replacement
	 * @param vanilla the vanilla
	 */
	private void replaceVanilla(int id, String registryName, String fieldName, String srgFieldName, Object replacement, Object vanilla)
	{
		boolean block = replacement instanceof Block;
		RegistryNamespaced<ResourceLocation, ?> registry = block ? Block.REGISTRY : Item.REGISTRY;
		ItemBlock ib = block ? (ItemBlock) Item.getItemFromBlock((Block) vanilla) : null;
		Class<?> clazz = block ? Blocks.class : Items.class;
		ResourceLocation rl = new ResourceLocation("minecraft", registryName);

		try
		{
			//set the delegate name manually
			setName.invoke(block ? ((Block) replacement).delegate : ((Item) replacement).delegate, rl);
			//add the replacement into the registry
			addObjectRaw.invoke(registry, id, rl, replacement);

			//remove the vanilla object from the underlying map, or it will take its place back when the capacity changes and is rehashed.
			Object[] objArray = (Object[]) objectArray.get(underlyingMap.get(registry));
			for (int i = 0; i < objArray.length; i++)
				if (objArray[i] == vanilla)
					objArray[i] = null;

			Field f = AsmUtils.changeFieldAccess(clazz, fieldName, srgFieldName);
			f.set(null, replacement);

			if (ib != null)
			{
				AsmUtils.changeFieldAccess(ItemBlock.class, "block", "field_150939_a").set(ib, replacement);
				GameData.getBlockItemMap().forcePut((Block) replacement, ib);
			}

			if (block)
				originalBlocks.put((Block) replacement, (Block) vanilla);
			else
				originalItems.put((Item) replacement, (Item) vanilla);

			replaceIn(CraftingManager.getInstance().getRecipeList(), vanilla, replacement);
			replaceIn(StatList.ALL_STATS, vanilla, replacement);

			//ReplacementTool.doubleCheck(registry, replacement, vanilla);
		}
		catch (ReflectiveOperationException e)
		{
			e.printStackTrace();
		}
	}

	protected static void doubleCheck(RegistryNamespaced<ResourceLocation, ?> registry, Object replacement, Object vanilla)
	{
		for (Object obj : registry)
		{
			if (obj == vanilla)
				MalisisCore.log.info("Found vanilla " + vanilla.getClass().getSimpleName() + " ( " + vanilla + ") in registry");
			if (obj == replacement)
				MalisisCore.log.info("Found replacement " + replacement.getClass().getSimpleName() + " ( " + replacement + ") in registry");
		}
	}

	public <T> void replaceIn(List<T> list, Object vanilla, Object replacement) throws ReflectiveOperationException
	{
		for (T object : list)
		{
			ReplacementHandler<T> rh = ReplacementHandler.getHandler(object);
			if (rh != null)
			{
				rh.replace(object, vanilla, replacement);
				//MalisisCore.log.info("Replaced {} by {} in {}", vanilla.getClass().getSimpleName(), replacement.getClass()
				//.getSimpleName(), object.getClass().getSimpleName());
			}
		}
	}

	/**
	 * Replaces vanilla block with another one.<br>
	 * Changes the registry by removing the vanilla block and adding the replacement.
	 *
	 * @param id the id
	 * @param registryName the registry name
	 * @param fieldName the field name in {@link Blocks} class.
	 * @param srgFieldName the srg field name in {@link Blocks} class.
	 * @param replacement the block
	 * @param vanilla the vanilla
	 */
	public static void replaceVanillaBlock(int id, String registryName, String fieldName, String srgFieldName, Block replacement, Block vanilla)
	{
		instance().replaceVanilla(id, registryName, fieldName, srgFieldName, replacement, vanilla);
	}

	/**
	 * Replaces vanilla item with another one.<br>
	 * Changes the registry by removing the vanilla item and adding the replacement.
	 *
	 * @param id the id
	 * @param registryName the registry name
	 * @param fieldName the field name in {@link Items} class.
	 * @param srgFieldName the srg field name in {@link Items} class.
	 * @param replacement the replacement
	 * @param vanilla the vanilla
	 */
	public static void replaceVanillaItem(int id, String registryName, String fieldName, String srgFieldName, Item replacement, Item vanilla)
	{
		instance().replaceVanilla(id, registryName, fieldName, srgFieldName, replacement, vanilla);
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
