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

package net.malisis.core.renderer.handler;

import java.util.List;

import javax.vecmath.Vector3f;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemTransformVec3f;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author Ordinastie
 *
 */
@SuppressWarnings("deprecation")
public class BakeEventHandler
{
	//    "thirdperson": {
	//        "rotation": [ 10, -45, 170 ],
	//        "translation": [ 0, 1.5, -2.75 ] * 0.0625,
	//        "scale": [ 0.375, 0.375, 0.375 ]
	//    }
	//@formatter:off
	private static final BuiltIn builtin = new BuiltIn();
	private static class BuiltIn implements IBakedModel
	{
		private ItemTransformVec3f thirdPerson = new ItemTransformVec3f(new Vector3f(10, -45, 170),
																		new Vector3f(0, 0.09375F, -0.171875F),
																		new Vector3f(0.375F, 0.375F, 0.375F));
		private ItemCameraTransforms transforms = new ItemCameraTransforms(	thirdPerson,
																	ItemCameraTransforms.DEFAULT.firstPerson,
																	ItemCameraTransforms.DEFAULT.head,
																	ItemCameraTransforms.DEFAULT.gui);
		@Override
		public boolean isAmbientOcclusion() { return false; }
		@Override
		public boolean isGui3d() { return true; }
		@Override
		public boolean isBuiltInRenderer() { return false; }
		@Override
		public TextureAtlasSprite getTexture() { return null; }
		@Override
		public ItemCameraTransforms getItemCameraTransforms() { return transforms; }
		@Override
		public List<BakedQuad> getFaceQuads(EnumFacing side) { return null; }
		@Override
		public List<BakedQuad> getGeneralQuads() { return null; }

	}
	//@formatter:on

	//private final IItemRenderer renderer;
	private final ModelResourceLocation rl;

	public BakeEventHandler(/*IItermRenderer renderer,*/Item item)
	{
		rl = new ModelResourceLocation(item.getUnlocalizedName(), "inventory");
	}

	public BakeEventHandler(Block block)
	{
		String modid = Loader.instance().activeModContainer().getModId();
		String name = block.getUnlocalizedName().substring(5);
		rl = new ModelResourceLocation(modid + ":" + name, "inventory");
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0, rl);
	}

	@SubscribeEvent
	public void onModelBakeEvent(ModelBakeEvent event)
	{
		event.modelRegistry.putObject(rl, builtin);
	}
}