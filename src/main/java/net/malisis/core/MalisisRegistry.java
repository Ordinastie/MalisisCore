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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.malisis.core.renderer.IBlockRenderer;
import net.malisis.core.renderer.IItemRenderer;
import net.malisis.core.renderer.IItemRenderer.IItemRenderInfo;
import net.malisis.core.renderer.IRenderWorldLast;
import net.malisis.core.renderer.MalisisRenderer;
import net.malisis.core.renderer.icon.IIconProvider;
import net.malisis.core.renderer.icon.IMetaIconProvider;
import net.malisis.core.renderer.icon.MalisisIcon;
import net.malisis.core.renderer.icon.provider.IBlockIconProvider;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.collect.ImmutableMap;

/**
 * @author Ordinastie
 *
 */
public class MalisisRegistry
{
	private static MalisisRegistry instance = new MalisisRegistry();

	private MalisisRegistry()
	{
		MinecraftForge.EVENT_BUS.register(this);
	}

	//#region IBlockRenderer
	private Map<Block, IBlockRenderer> blockRenderers = new HashMap<>();
	private IStateMapper emptyMapper = new IStateMapper()
	{
		@Override
		public Map putStateModelLocations(Block block)
		{
			return ImmutableMap.of();
		}
	};

	public static void registerBlockRenderer(Block block, IBlockRenderer renderer, IItemRenderInfo renderInfos)
	{
		if (block.getRenderType() != MalisisCore.malisisRenderType)
		{
			MalisisCore.log.error("[MalisisRenderer] Tried to register {} block with wrong render type : {}", block.getUnlocalizedName(),
					block.getRenderType());
			return;
		}

		instance.blockRenderers.put(block, renderer);
		instance.itemRenderers.put(Item.getItemFromBlock(block), renderer);

		ModelLoader.setCustomStateMapper(block, instance.emptyMapper);
		MalisisRegistry.registerItemRenderInfos(block, renderInfos);

	}

	public static boolean renderBlock(WorldRenderer wr, IBlockAccess world, BlockPos pos, IBlockState state)
	{
		IBlockRenderer renderer = instance.blockRenderers.get(state.getBlock());
		if (renderer == null)
			return false;
		return renderer.renderBlock(wr, world, pos, state);
	}

	public static TextureAtlasSprite getParticleIcon(IBlockState state)
	{
		Block block = state.getBlock();
		MalisisIcon icon = null;
		if (block instanceof IIconProvider)
		{
			IIconProvider provider = ((IMetaIconProvider) block).getIconProvider();
			if (provider instanceof IBlockIconProvider)
				icon = ((IBlockIconProvider) provider).getParticleIcon(state);
			else if (provider != null)
				icon = provider.getIcon();
		}

		return icon != null ? icon : MalisisIcon.missing;
	}

	//#end IBlockRenderer

	//#region IItemRenderer
	private Map<Item, IItemRenderer> itemRenderers = new HashMap<>();

	public static void registerItemRenderer(Item item, IItemRenderer renderer, IItemRenderInfo renderInfos)
	{
		instance.itemRenderers.put(item, renderer);
		MalisisRegistry.registerItemRenderInfos(item, renderInfos);
	}

	public static boolean renderItem(ItemStack itemStack)
	{
		if (itemStack == null)
			return false;

		Item item = itemStack.getItem();
		IItemRenderer ir = instance.itemRenderers.get(item);
		if (ir == null)
			return false;

		ir.renderItem(itemStack, MalisisRenderer.getPartialTick());
		return true;
	}

	//#end IItemRenderer

	//#region IIconProvider
	@SideOnly(Side.CLIENT)
	private Set<IIconProvider> iconProviders = new HashSet<>();

	@SideOnly(Side.CLIENT)
	public static void registerIconProvider(IIconProvider iconProvider)
	{
		if (iconProvider != null)
			instance.iconProviders.add(iconProvider);
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onTextureStitchEvent(TextureStitchEvent.Pre event)
	{
		for (IIconProvider iconProvider : instance.iconProviders)
			iconProvider.registerIcons(event.map);
	}

	//#end IIconProvider

	//#region BakeEventHandler
	private HashMap<ModelResourceLocation, IItemRenderInfo> renderInfos = new HashMap<>();

	private static void registerItemRenderInfos(Item item, IItemRenderInfo renderInfos)
	{
		String modid = Loader.instance().activeModContainer().getModId();
		String name = item.getUnlocalizedName().substring(5);
		ModelResourceLocation rl = new ModelResourceLocation(modid + ":" + name, "inventory");
		ModelLoader.setCustomModelResourceLocation(item, 0, rl);
		instance.renderInfos.put(rl, renderInfos);
	}

	private static void registerItemRenderInfos(Block block, IItemRenderInfo renderInfos)
	{
		String modid = Loader.instance().activeModContainer().getModId();
		String name = block.getUnlocalizedName().substring(5);
		ModelResourceLocation rl = new ModelResourceLocation(modid + ":" + name, "inventory");
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0, rl);
		instance.renderInfos.put(rl, renderInfos);
	}

	@SubscribeEvent
	public void onModelBakeEvent(ModelBakeEvent event)
	{
		for (Entry<ModelResourceLocation, IItemRenderInfo> entry : renderInfos.entrySet())
			event.modelRegistry.putObject(entry.getKey(), entry.getValue());
	}

	//#end BakeEventHandler

	//#region RenderWorldLast
	private List<IRenderWorldLast> renderWorldLastRenderers = new ArrayList<>();

	public static void registerRenderWorldLast(IRenderWorldLast renderer)
	{
		instance.renderWorldLastRenderers.add(renderer);
	}

	public static void unregisterRenderWorldLast(IRenderWorldLast renderer)
	{
		instance.renderWorldLastRenderers.remove(renderer);
	}

	@SubscribeEvent
	public void onRenderLast(RenderWorldLastEvent event)
	{
		for (IRenderWorldLast renderer : renderWorldLastRenderers)
		{
			if (renderer.shouldRender(event, Minecraft.getMinecraft().theWorld))
				renderer.renderWorldLastEvent(event, Minecraft.getMinecraft().theWorld);
		}
	}
	//#end RenderWorldLast
}
