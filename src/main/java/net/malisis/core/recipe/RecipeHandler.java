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

package net.malisis.core.recipe;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import cpw.mods.fml.relauncher.ReflectionHelper;

/**
 * The Class RecipeHandler.
 *
 * @author Ordinastie
 * @param <T> the type of {@link IRecipe} handled
 */
public abstract class RecipeHandler<T extends IRecipe>
{
	private static HashMap<Class<? extends IRecipe>, RecipeHandler> handlers = new HashMap();

	public RecipeHandler(Class<T> clazz)
	{
		handlers.put(clazz, this);
	}

	/**
	 * Changes the access level for the specified field for a class.
	 *
	 * @param clazz the clazz
	 * @param fieldName the field name
	 * @return the field
	 */
	protected Field changeAccess(Class clazz, String fieldName)
	{
		try
		{
			// modify reference in Blocks class
			Field f = ReflectionHelper.findField(clazz, fieldName);
			Field modifiers = Field.class.getDeclaredField("modifiers");
			modifiers.setAccessible(true);
			modifiers.setInt(f, f.getModifiers() & ~Modifier.FINAL);

			return f;

		}
		catch (ReflectiveOperationException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Gets the {@link ItemStack} corresponding to the object. Obj is either {@link Item} or {@link Block}.
	 *
	 * @param obj the replacement
	 * @return the item stack
	 */
	protected ItemStack getItemStack(Object obj)
	{
		if (obj instanceof Item)
			return new ItemStack((Item) obj);
		if (obj instanceof Block)
			return new ItemStack((Block) obj);

		return null;
	}

	/**
	 * Checks if the object matches the speicified {@link ItemStack}.
	 *
	 * @param itemStack the item stack
	 * @param replaced the replaced
	 * @return true, if is matched
	 */
	protected boolean isMatched(ItemStack itemStack, Object replaced)
	{
		if (itemStack == null)
			return false;

		if (replaced instanceof Item)
			return itemStack.getItem() == replaced;
		if (replaced instanceof Block)
			return itemStack.getItem() instanceof ItemBlock && ((ItemBlock) itemStack.getItem()).field_150939_a == replaced;

		return false;
	}

	public abstract void replace(T recipe, Object vanilla, Object replacement);

	/**
	 * Gets the handler of a specific {@link IRecipe}.
	 *
	 * @param recipe the recipe
	 * @return the handler
	 */
	public static RecipeHandler getHandler(IRecipe recipe)
	{
		return handlers.get(recipe.getClass());
	}

}
