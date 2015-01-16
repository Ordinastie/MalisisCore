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

import net.malisis.core.MalisisCore;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipes;

/**
 * @author Ordinastie
 *
 */
public class ShapedRecipesHandler extends RecipeHandler<ShapedRecipes>
{
	private Field outputField;

	public ShapedRecipesHandler()
	{
		super(ShapedRecipes.class);
		outputField = changeAccess(ShapedRecipes.class, MalisisCore.isObfEnv ? "field_77575_e" : "recipeOutput");
	}

	@Override
	public void replace(ShapedRecipes recipe, Object vanilla, Object replacement)
	{
		try
		{
			if (isMatched(recipe.getRecipeOutput(), vanilla))
				outputField.set(recipe, getItemStack(replacement));

			ItemStack[] input = recipe.recipeItems;
			for (int i = 0; i < input.length; i++)
			{
				if (input[i] instanceof ItemStack && isMatched(input[i], vanilla))
					input[i] = getItemStack(replacement);
			}
		}
		catch (IllegalArgumentException | IllegalAccessException e)
		{
			e.printStackTrace();
		}
	}

}
