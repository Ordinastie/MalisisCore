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

import net.malisis.core.block.IComponentProvider;
import net.malisis.core.block.MalisisBlock;
import net.malisis.core.block.component.DirectionalComponent;
import net.malisis.core.renderer.IRenderComponent;
import net.malisis.core.renderer.MalisisRenderer;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.RenderType;
import net.malisis.core.renderer.icon.provider.IModelIconProvider;
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
	/** {@link RenderParameters} used for rendering. */
	protected RenderParameters renderParameters = new RenderParameters();
	/** {@link IModelIconProvider} used for rendering. */
	protected IModelIconProvider modelIconProvider;
	/** Shape/group visibility check. */
	protected IVisibilityProvider visibilityProvider;

	/**
	 * Instantiates a new {@link ModelComponent} with a {@link IVisibilityProvider} and load its {@link MalisisModel}.
	 *
	 * @param modelName the model name
	 */
	public ModelComponent(String modelName, IVisibilityProvider visibilityProvider)
	{
		this.resourceLocation = new ResourceLocation(modelName);
		this.visibilityProvider = visibilityProvider;
		loadModel();
	}

	/**
	 * Instantiates a new {@link ModelComponent} with a {@link IVisibilityProvider} and load its {@link MalisisModel}.
	 *
	 * @param modelName the model name
	 */
	public ModelComponent(String modelName)
	{
		this(modelName, null);
	}

	/**
	 * Sets the {@link IModelIconProvider} to use with this {@link ModelComponent}.
	 *
	 * @param iconProvider the new icon provider
	 */
	public void setIconProvider(IModelIconProvider iconProvider)
	{
		this.modelIconProvider = iconProvider;
	}

	@Override
	public void onComponentAdded(IComponentProvider provider)
	{
		//check if a IModelIconProvider was already added to the provider
		IModelIconProvider mip = provider.getComponent(IModelIconProvider.class);
		if (mip != null)
			setIconProvider(mip);
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
		loadModel();
		model.resetState();
		if (renderer.getRenderType() == RenderType.BLOCK)
			model.rotate(DirectionalComponent.getDirection(renderer.getBlockState()));

		for (String name : model.getShapeNames())
		{
			if (visibilityProvider == null || visibilityProvider.isVisible(renderer, name))
			{
				if (modelIconProvider != null)
					renderParameters.icon.set(modelIconProvider.getIcon(renderer, name));

				model.render(renderer, name, renderParameters);
			}
		}

	}

	/**
	 * IVisibilityProvider determines whether a specific shape/group should be rendered.
	 */
	public static interface IVisibilityProvider
	{

		/**
		 * Checks if the specified shape/group should be rendered.
		 *
		 * @param renderer the renderer
		 * @param shapeName the shape name
		 * @return true, if is visible
		 */
		public boolean isVisible(MalisisRenderer<TileEntity> renderer, String shapeName);
	}
}
