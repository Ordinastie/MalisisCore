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

package net.malisis.core.renderer.component;

import net.malisis.core.block.MalisisBlock;
import net.malisis.core.renderer.IRenderComponent;
import net.malisis.core.renderer.MalisisRenderer;
import net.malisis.core.renderer.model.MalisisModel;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

/**
 * {@link ModelComponent} allows models to be rendered for {@link MalisisBlock}.
 *
 * @author Ordinastie
 */
public class ModelComponent implements IRenderComponent
{
	/** {@link ResourceLocation} for the model. */
	protected ResourceLocation resourceLocation;
	/** {@link MalisisModel} for this {@link ModelComponent}. */
	protected MalisisModel model;

	/**
	 * Instantiates a new {@link ModelComponent} and load its {@link MalisisModel}.
	 *
	 * @param modelName the model name
	 */
	public ModelComponent(String modelName)
	{
		this.resourceLocation = new ResourceLocation(modelName);
		loadModel();
	}

	/**
	 * Loads the {@link MalisisBlock} for this {@link ModelComponent}.
	 */
	protected void loadModel()
	{
		model = new MalisisModel(resourceLocation);
	}

	/**
	 * Gets the {@link MalisisModel} for this {@link ModelComponent}.
	 *
	 * @return the model
	 */
	public MalisisModel getModel()
	{
		return model;
	}

	@Override
	public void render(Block block, MalisisRenderer<TileEntity> renderer)
	{
		model.render(renderer);
	}
}
