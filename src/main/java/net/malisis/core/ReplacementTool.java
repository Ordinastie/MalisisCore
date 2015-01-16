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

package net.malisis.core;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map.Entry;

import net.malisis.core.recipe.RecipeHandler;
import net.malisis.core.recipe.ShapedOreRecipeHandler;
import net.malisis.core.recipe.ShapedRecipesHandler;
import net.malisis.core.recipe.ShapelessOreRecipeHandler;
import net.malisis.core.recipe.ShapelessRecipesHandler;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
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

	private ReplacementTool()
	{
		new ShapedOreRecipeHandler();
		new ShapedRecipesHandler();
		new ShapelessRecipesHandler();
		new ShapelessOreRecipeHandler();
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
			block.registerBlockIcons(event.map);
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
		try
		{
			ItemBlock ib = (ItemBlock) Item.getItemFromBlock(vanilla);

			// add block to registry
			Class[] types = { Integer.TYPE, String.class, Object.class };
			Method method = ReflectionHelper.findMethod(FMLControlledNamespacedRegistry.class, (FMLControlledNamespacedRegistry) null,
					new String[] { "addObjectRaw" }, types);
			method.invoke(Block.blockRegistry, id, "minecraft:" + name, replacement);

			// modify reference in Blocks class
			Field f = ReflectionHelper.findField(Blocks.class, MalisisCore.isObfEnv ? srgFieldName : name);
			Field modifiers = Field.class.getDeclaredField("modifiers");
			modifiers.setAccessible(true);
			modifiers.setInt(f, f.getModifiers() & ~Modifier.FINAL);
			f.set(null, replacement);

			if (ib != null)
			{
				f = ReflectionHelper.findField(ItemBlock.class, "field_150939_a");
				modifiers = Field.class.getDeclaredField("modifiers");
				modifiers.setAccessible(true);
				modifiers.setInt(f, f.getModifiers() & ~Modifier.FINAL);
				f.set(ib, replacement);
			}

			instance().originalBlocks.put(replacement, vanilla);

			instance().replaceInRecipes(vanilla, replacement);

		}
		catch (ReflectiveOperationException e)
		{
			e.printStackTrace();
		}
	}

	public static void replaceVanillaItem(int id, String name, String srgFieldName, Item replacement, Item vanilla)
	{
		try
		{
			// add block to registry
			Class[] types = { Integer.TYPE, String.class, Object.class };
			Method method = ReflectionHelper.findMethod(FMLControlledNamespacedRegistry.class, (FMLControlledNamespacedRegistry) null,
					new String[] { "addObjectRaw" }, types);
			method.invoke(Item.itemRegistry, id, "minecraft:" + name, replacement);

			// modify reference in Item class
			Field f = ReflectionHelper.findField(Items.class, MalisisCore.isObfEnv ? srgFieldName : name);
			Field modifiers = Field.class.getDeclaredField("modifiers");
			modifiers.setAccessible(true);
			modifiers.setInt(f, f.getModifiers() & ~Modifier.FINAL);
			f.set(null, replacement);

			instance().originalItems.put(replacement, vanilla);

			instance().replaceInRecipes(vanilla, replacement);

		}
		catch (ReflectiveOperationException e)
		{
			e.printStackTrace();
		}
	}

	public void replaceInRecipes(Object vanilla, Object replacement) throws ReflectiveOperationException
	{
		if (!(vanilla instanceof Item || vanilla instanceof Block))
			return;
		if (!(replacement instanceof Item || replacement instanceof Block))
			return;

		ListIterator<IRecipe> iterator = CraftingManager.getInstance().getRecipeList().listIterator();
		while (iterator.hasNext())
		{
			IRecipe recipe = iterator.next();
			RecipeHandler rh = RecipeHandler.getHandler(recipe);
			if (rh != null)
				rh.replace(recipe, vanilla, replacement);

		}
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
