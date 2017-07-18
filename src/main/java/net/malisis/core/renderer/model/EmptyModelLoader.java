/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Ordinastie
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

package net.malisis.core.renderer.model;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.vecmath.Matrix4f;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import net.malisis.core.block.IComponent;
import net.malisis.core.block.component.ItemTransformComponent;
import net.malisis.core.registry.AutoLoad;
import net.malisis.core.registry.ClientRegistry;
import net.malisis.core.renderer.IItemRenderer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author Ordinastie
 *
 */
@AutoLoad
@SideOnly(Side.CLIENT)
public class EmptyModelLoader implements ICustomModelLoader
{
	private static EmptyModelLoader INSTANCE = new EmptyModelLoader();
	private Map<ResourceLocation, DummyModel> locations = Maps.newHashMap();

	private EmptyModelLoader()
	{
		ModelLoaderRegistry.registerLoader(this);
	}

	@Override
	public boolean accepts(ResourceLocation modelLocation)
	{
		return locations.containsKey(modelLocation);
	}

	@Override
	public IModel loadModel(ResourceLocation modelLocation) throws Exception
	{
		return locations.get(modelLocation);
	}

	@Override
	public void onResourceManagerReload(IResourceManager resourceManager)
	{}

	public static void register(Item item)
	{
		ModelResourceLocation mrl = new ModelResourceLocation(item.getRegistryName(), "inventory");
		register(item, mrl);
	}

	public static void register(Item item, ModelResourceLocation mrl)
	{
		INSTANCE.locations.put(mrl, new DummyModel(item));
		ModelLoader.setCustomMeshDefinition(item, stack -> mrl);
	}

	public static class DummyModel implements IBakedModel, IModel
	{
		private Item item;
		private ModelResourceLocation mrl;

		public DummyModel(Item item)
		{
			this.item = item;
			this.mrl = new ModelResourceLocation(item.getRegistryName(), "inventory");
		}

		public ModelResourceLocation getResourceLocation()
		{
			return mrl;
		}

		@Override
		public Pair<? extends IBakedModel, Matrix4f> handlePerspective(TransformType transformType)
		{
			ItemTransformComponent itc = IComponent.getComponent(ItemTransformComponent.class, item);
			if (itc != null)
				return Pair.of(this, itc.getTransform(item, transformType));

			IItemRenderer itemRenderer = ClientRegistry.getItemRenderer(item);
			if (itemRenderer == null)
				return Pair.of(this, null);

			itemRenderer.setTransformType(transformType);
			return Pair.of(this, itemRenderer.getTransform(item, transformType));
		}

		@Override
		public boolean isGui3d()
		{
			IItemRenderer itemRenderer = ClientRegistry.getItemRenderer(item);
			return itemRenderer != null && itemRenderer.isGui3d();
		}

		//@formatter:off
		@Override public boolean isAmbientOcclusion() 					{ return false; }
		@Override public boolean isBuiltInRenderer() 					{ return false; }
		@Override public TextureAtlasSprite getParticleTexture() 		{ return null; }
		@Override public ItemCameraTransforms getItemCameraTransforms() { return ItemCameraTransforms.DEFAULT; }
		@Override public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand)	{ return ImmutableList.of(); }
		@Override public ItemOverrideList getOverrides() 				{ return ItemOverrideList.NONE; }
		//@formatter:on

		@Override
		public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter)
		{
			return this;
		}

		@Override
		public String toString()
		{
			return item.getRegistryName() + "[" + mrl + "]";
		}

	}

}
